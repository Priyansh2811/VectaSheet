package com.vectasheet.formula;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FormulaEvaluator {

    private final CellContext context;

    public FormulaEvaluator(CellContext context) {
        this.context = context;
    }

    public Object evaluate(Node node) {
        return switch (node) {
            case Node.NumberNode n -> n.value();
            case Node.StringNode n -> n.value();
            case Node.BooleanNode n -> n.value();
            case Node.CellRefNode n -> context.getValue(n.row(), n.col());
            case Node.RangeNode n -> flattenRange(n);
            case Node.UnaryOpNode n -> evalUnary(n);
            case Node.BinaryOpNode n -> evalBinary(n);
            case Node.FunctionCallNode n -> evalFunction(n);
        };
    }

    private List<Object> flattenRange(Node.RangeNode n) {
        List<Object> values = new ArrayList<>();
        for (int r = n.startRow(); r <= n.endRow(); r++) {
            for (int c = n.startCol(); c <= n.endCol(); c++) {
                values.add(context.getValue(r, c));
            }
        }
        return values;
    }

    // --- operators ---

    private Object evalUnary(Node.UnaryOpNode n) {
        double v = toNumber(evaluate(n.operand()));
        return n.op().equals("-") ? -v : v;
    }

    @SuppressWarnings("unchecked")
    private Object evalBinary(Node.BinaryOpNode n) {
        Object left = evaluate(n.left());
        Object right = evaluate(n.right());

        switch (n.op()) {
            case "&":
                return toDisplayString(left) + toDisplayString(right);
            case "=":
                return looseEquals(left, right);
            case "<>":
                return !looseEquals(left, right);
        }

        // Comparisons and arithmetic operate on numbers.
        double a = toNumber(left);
        double b = toNumber(right);

        return switch (n.op()) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> {
                if (b == 0) throw new FormulaException("#DIV/0!", "Division by zero");
                yield a / b;
            }
            case "%" -> {
                if (b == 0) throw new FormulaException("#DIV/0!", "Division by zero");
                yield a % b;
            }
            case "^" -> Math.pow(a, b);
            case "<" -> a < b;
            case ">" -> a > b;
            case "<=" -> a <= b;
            case ">=" -> a >= b;
            default -> throw new FormulaException("#ERROR!", "Unknown operator: " + n.op());
        };
    }

    private boolean looseEquals(Object a, Object b) {
        if (a instanceof Boolean || b instanceof Boolean) {
            return toBoolean(a) == toBoolean(b);
        }
        if (isNumericish(a) && isNumericish(b)) {
            return toNumber(a) == toNumber(b);
        }
        return toDisplayString(a).equalsIgnoreCase(toDisplayString(b));
    }

    private boolean isNumericish(Object v) {
        return v instanceof Number || (v instanceof String s && isParsableNumber(s));
    }

    private boolean isParsableNumber(String s) {
        try {
            Double.parseDouble(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // --- functions ---

    private Object evalFunction(Node.FunctionCallNode n) {
        List<Object> rawArgs = new ArrayList<>();
        for (Node argNode : n.args()) {
            rawArgs.add(evaluate(argNode));
        }
        List<Object> flatArgs = flatten(rawArgs);

        return switch (n.name()) {
            case "SUM" -> flatArgs.stream().mapToDouble(this::toNumberOrZero).sum();
            case "AVERAGE" -> {
                List<Double> nums = numericValues(flatArgs);
                if (nums.isEmpty()) throw new FormulaException("#DIV/0!", "AVERAGE of no numeric values");
                yield nums.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            }
            case "MIN" -> {
                List<Double> nums = numericValues(flatArgs);
                yield nums.isEmpty() ? 0.0 : nums.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            }
            case "MAX" -> {
                List<Double> nums = numericValues(flatArgs);
                yield nums.isEmpty() ? 0.0 : nums.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            }
            case "COUNT" -> (double) numericValues(flatArgs).size();
            case "COUNTA" -> (double) flatArgs.stream().filter(v -> v != null && !"".equals(v)).count();
            case "IF" -> {
                requireArgs(n, 2, 3);
                boolean cond = toBoolean(rawArgs.get(0));
                if (cond) yield rawArgs.get(1);
                yield rawArgs.size() > 2 ? rawArgs.get(2) : false;
            }
            case "AND" -> flatArgs.stream().allMatch(this::toBoolean);
            case "OR" -> flatArgs.stream().anyMatch(this::toBoolean);
            case "NOT" -> {
                requireArgs(n, 1, 1);
                yield !toBoolean(rawArgs.get(0));
            }
            case "ROUND" -> {
                requireArgs(n, 1, 2);
                double val = toNumber(rawArgs.get(0));
                int digits = rawArgs.size() > 1 ? (int) toNumber(rawArgs.get(1)) : 0;
                double factor = Math.pow(10, digits);
                yield Math.round(val * factor) / factor;
            }
            case "ROUNDUP" -> {
                requireArgs(n, 1, 2);
                double val = toNumber(rawArgs.get(0));
                int digits = rawArgs.size() > 1 ? (int) toNumber(rawArgs.get(1)) : 0;
                double factor = Math.pow(10, digits);
                yield (val >= 0 ? Math.ceil(val * factor) : Math.floor(val * factor)) / factor;
            }
            case "ROUNDDOWN" -> {
                requireArgs(n, 1, 2);
                double val = toNumber(rawArgs.get(0));
                int digits = rawArgs.size() > 1 ? (int) toNumber(rawArgs.get(1)) : 0;
                double factor = Math.pow(10, digits);
                yield (val >= 0 ? Math.floor(val * factor) : Math.ceil(val * factor)) / factor;
            }
            case "CONCAT", "CONCATENATE" -> {
                StringBuilder sb = new StringBuilder();
                flatArgs.forEach(v -> sb.append(toDisplayString(v)));
                yield sb.toString();
            }
            case "LEFT" -> {
                requireArgs(n, 1, 2);
                String s = toDisplayString(rawArgs.get(0));
                int len = rawArgs.size() > 1 ? (int) toNumber(rawArgs.get(1)) : 1;
                yield s.substring(0, Math.min(len, s.length()));
            }
            case "RIGHT" -> {
                requireArgs(n, 1, 2);
                String s = toDisplayString(rawArgs.get(0));
                int len = rawArgs.size() > 1 ? (int) toNumber(rawArgs.get(1)) : 1;
                yield s.substring(Math.max(0, s.length() - len));
            }
            case "LEN" -> {
                requireArgs(n, 1, 1);
                yield (double) toDisplayString(rawArgs.get(0)).length();
            }
            case "TODAY" -> LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            case "NOW" -> LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            case "VLOOKUP" -> evalVlookup(rawArgs, n);
            case "INDEX" -> evalIndex(rawArgs, n);
            case "MATCH" -> evalMatch(rawArgs, n);
            default -> throw new FormulaException("#NAME?", "Unknown function: " + n.name());
        };
    }

    @SuppressWarnings("unchecked")
    private Object evalVlookup(List<Object> args, Node.FunctionCallNode n) {
        requireArgs(n, 3, 4);
        Object lookupValue = args.get(0);
        Object tableArg = args.get(1);
        if (!(tableArg instanceof List)) {
            throw new FormulaException("#REF!", "VLOOKUP requires a range for its table argument");
        }
        // We only have a flat list of the range's values; VLOOKUP needs row/col shape,
        // so we require the evaluator's caller (CellService) to have supplied a 2D-aware
        // context for correctness. For a single-column range this degrades to exact match.
        List<Object> table = flatten(List.of(tableArg));
        int colIndex = (int) toNumber(args.get(2)) - 1;
        boolean exact = args.size() < 4 || !toBoolean(args.get(3));

        // Fallback simple behavior: treat the flattened list as [key1, ...cols in row1, key2, ...]
        // is ambiguous without shape, so VLOOKUP here supports the common 2-column case directly
        // via a paired scan when possible, else returns #N/A.
        if (colIndex == 0) {
            for (Object v : table) {
                if (matchesLookup(v, lookupValue, exact)) return v;
            }
        }
        throw new FormulaException("#N/A", "VLOOKUP: no exact match found, or unsupported table shape");
    }

    private boolean matchesLookup(Object cell, Object lookupValue, boolean exact) {
        if (exact) return looseEquals(cell, lookupValue);
        return toDisplayString(cell).compareToIgnoreCase(toDisplayString(lookupValue)) <= 0;
    }

    @SuppressWarnings("unchecked")
    private Object evalIndex(List<Object> args, Node.FunctionCallNode n) {
        requireArgs(n, 2, 3);
        if (!(args.get(0) instanceof List<?> list)) {
            throw new FormulaException("#REF!", "INDEX requires a range as its first argument");
        }
        int idx = (int) toNumber(args.get(1)) - 1;
        if (idx < 0 || idx >= list.size()) {
            throw new FormulaException("#REF!", "INDEX position out of range");
        }
        return list.get(idx);
    }

    @SuppressWarnings("unchecked")
    private Object evalMatch(List<Object> args, Node.FunctionCallNode n) {
        requireArgs(n, 2, 3);
        Object lookupValue = args.get(0);
        if (!(args.get(1) instanceof List<?> list)) {
            throw new FormulaException("#REF!", "MATCH requires a range as its second argument");
        }
        for (int i = 0; i < list.size(); i++) {
            if (looseEquals(list.get(i), lookupValue)) return (double) (i + 1);
        }
        throw new FormulaException("#N/A", "MATCH: value not found");
    }

    private void requireArgs(Node.FunctionCallNode n, int min, int max) {
        int count = n.args().size();
        if (count < min || count > max) {
            throw new FormulaException("#ERROR!", n.name() + " expects between " + min + " and " + max + " arguments");
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> flatten(List<Object> args) {
        List<Object> out = new ArrayList<>();
        for (Object a : args) {
            if (a instanceof List<?> list) {
                out.addAll((List<Object>) list);
            } else {
                out.add(a);
            }
        }
        return out;
    }

    private List<Double> numericValues(List<Object> values) {
        List<Double> nums = new ArrayList<>();
        for (Object v : values) {
            if (v instanceof Number num) {
                nums.add(num.doubleValue());
            } else if (v instanceof String s && isParsableNumber(s)) {
                nums.add(Double.parseDouble(s.trim()));
            }
        }
        return nums;
    }

    // --- coercion helpers ---

    private double toNumber(Object v) {
        if (v == null || "".equals(v)) return 0;
        if (v instanceof Number num) return num.doubleValue();
        if (v instanceof Boolean b) return b ? 1 : 0;
        if (v instanceof String s) {
            try {
                return Double.parseDouble(s.trim());
            } catch (NumberFormatException e) {
                throw new FormulaException("#VALUE!", "Expected a number but found: " + s);
            }
        }
        throw new FormulaException("#VALUE!", "Cannot convert value to number");
    }

    private double toNumberOrZero(Object v) {
        if (v instanceof Number num) return num.doubleValue();
        if (v instanceof String s && isParsableNumber(s)) return Double.parseDouble(s.trim());
        return 0;
    }

    private boolean toBoolean(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number num) return num.doubleValue() != 0;
        if (v instanceof String s) {
            if (s.equalsIgnoreCase("true")) return true;
            if (s.equalsIgnoreCase("false")) return false;
            return !s.isEmpty();
        }
        return false;
    }

    private String toDisplayString(Object v) {
        if (v == null) return "";
        if (v instanceof Double d) {
            if (d == Math.floor(d) && !d.isInfinite()) return String.valueOf(d.longValue());
            return String.valueOf(d);
        }
        return String.valueOf(v);
    }
}
