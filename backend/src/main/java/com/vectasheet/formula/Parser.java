package com.vectasheet.formula;

import java.util.ArrayList;
import java.util.List;

import static com.vectasheet.formula.Lexer.TokenType;
import static com.vectasheet.formula.Lexer.Token;

/**
 * Precedence (low to high):
 *   comparison (= <> < > <= >=)
 *   concatenation (&)
 *   additive (+ -)
 *   multiplicative (* /)
 *   unary (- +)
 *   power (^)
 *   primary (numbers, strings, cell refs, ranges, function calls, parens)
 */
public class Parser {

    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public static Node parse(String formula) {
        List<Token> tokens = new Lexer(formula).tokenize();
        Parser parser = new Parser(tokens);
        Node node = parser.parseComparison();
        parser.expect(TokenType.EOF);
        return node;
    }

    private Token peek() { return tokens.get(pos); }
    private Token advance() { return tokens.get(pos++); }

    private Token expect(TokenType type) {
        Token t = peek();
        if (t.type() != type) {
            throw new FormulaException("#ERROR!", "Unexpected token '" + t.text() + "' in formula");
        }
        return advance();
    }

    private boolean isOperator(String... ops) {
        if (peek().type() != TokenType.OPERATOR) return false;
        for (String op : ops) {
            if (peek().text().equals(op)) return true;
        }
        return false;
    }

    private Node parseComparison() {
        Node left = parseConcat();
        while (isOperator("=", "<>", "<", ">", "<=", ">=")) {
            String op = advance().text();
            Node right = parseConcat();
            left = new Node.BinaryOpNode(op, left, right);
        }
        return left;
    }

    private Node parseConcat() {
        Node left = parseAdditive();
        while (isOperator("&")) {
            String op = advance().text();
            Node right = parseAdditive();
            left = new Node.BinaryOpNode(op, left, right);
        }
        return left;
    }

    private Node parseAdditive() {
        Node left = parseMultiplicative();
        while (isOperator("+", "-")) {
            String op = advance().text();
            Node right = parseMultiplicative();
            left = new Node.BinaryOpNode(op, left, right);
        }
        return left;
    }

    private Node parseMultiplicative() {
        Node left = parseUnary();
        while (isOperator("*", "/", "%")) {
            String op = advance().text();
            Node right = parseUnary();
            left = new Node.BinaryOpNode(op, left, right);
        }
        return left;
    }

    private Node parseUnary() {
        if (isOperator("-", "+")) {
            String op = advance().text();
            return new Node.UnaryOpNode(op, parseUnary());
        }
        return parsePower();
    }

    private Node parsePower() {
        Node left = parsePrimary();
        if (isOperator("^")) {
            advance();
            Node right = parseUnary();
            return new Node.BinaryOpNode("^", left, right);
        }
        return left;
    }

    private Node parsePrimary() {
        Token t = peek();

        switch (t.type()) {
            case NUMBER -> {
                advance();
                return new Node.NumberNode(Double.parseDouble(t.text()));
            }
            case STRING -> {
                advance();
                return new Node.StringNode(t.text());
            }
            case LPAREN -> {
                advance();
                Node inner = parseComparison();
                expect(TokenType.RPAREN);
                return inner;
            }
            case CELL_REF -> {
                advance();
                CellRef.Coord start = CellRef.parse(t.text());
                if (peek().type() == TokenType.COLON) {
                    advance();
                    Token endTok = expect(TokenType.CELL_REF);
                    CellRef.Coord end = CellRef.parse(endTok.text());
                    return new Node.RangeNode(
                            Math.min(start.row(), end.row()), Math.min(start.col(), end.col()),
                            Math.max(start.row(), end.row()), Math.max(start.col(), end.col())
                    );
                }
                return new Node.CellRefNode(start.row(), start.col());
            }
            case IDENTIFIER -> {
                advance();
                String name = t.text();

                if (name.equalsIgnoreCase("TRUE")) return new Node.BooleanNode(true);
                if (name.equalsIgnoreCase("FALSE")) return new Node.BooleanNode(false);

                if (peek().type() == TokenType.LPAREN) {
                    advance();
                    List<Node> args = new ArrayList<>();
                    if (peek().type() != TokenType.RPAREN) {
                        args.add(parseComparison());
                        while (peek().type() == TokenType.COMMA) {
                            advance();
                            args.add(parseComparison());
                        }
                    }
                    expect(TokenType.RPAREN);
                    return new Node.FunctionCallNode(name.toUpperCase(), args);
                }
                throw new FormulaException("#NAME?", "Unknown name: " + name);
            }
            default -> throw new FormulaException("#ERROR!", "Unexpected token '" + t.text() + "' in formula");
        }
    }
}
