package com.vectasheet.service;

import com.vectasheet.dto.CellDto;
import com.vectasheet.dto.UpdateCellRequest;
import com.vectasheet.entity.Cell;
import com.vectasheet.entity.CellValueType;
import com.vectasheet.entity.Sheet;
import com.vectasheet.entity.Spreadsheet;
import com.vectasheet.entity.WorkspaceRole;
import com.vectasheet.exception.ApiException;
import com.vectasheet.formula.CellContext;
import com.vectasheet.formula.DependencyExtractor;
import com.vectasheet.formula.FormulaEvaluator;
import com.vectasheet.formula.FormulaException;
import com.vectasheet.formula.Node;
import com.vectasheet.formula.Parser;
import com.vectasheet.repository.CellRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Recalculation strategy: on every write, the whole sheet's cells are loaded, a
 * dependency graph is built from every formula's referenced cells, and formula
 * cells are recalculated in topological order (so a cell is only evaluated after
 * everything it depends on). This avoids maintaining a separate edge table, at
 * the cost of re-parsing every formula on the sheet on each edit — fine for the
 * sheet sizes this phase targets, but the first thing to optimize (an indexed,
 * incrementally-updated dependency graph) if sheets grow into the thousands of
 * formula cells the original spec calls for.
 */
@Service
public class CellService {

    private final CellRepository cellRepository;
    private final SheetService sheetService;
    private final SpreadsheetService spreadsheetService;
    private final WorkspaceService workspaceService;

    public CellService(
            CellRepository cellRepository,
            SheetService sheetService,
            SpreadsheetService spreadsheetService,
            WorkspaceService workspaceService
    ) {
        this.cellRepository = cellRepository;
        this.sheetService = sheetService;
        this.spreadsheetService = spreadsheetService;
        this.workspaceService = workspaceService;
    }

    public List<CellDto> listCells(UUID userId, UUID sheetId) {
        Sheet sheet = sheetService.findOrThrow(sheetId);
        Spreadsheet spreadsheet = spreadsheetService.findOrThrow(sheet.getSpreadsheetId());
        workspaceService.requireMembership(spreadsheet.getWorkspaceId(), userId);

        return cellRepository.findBySheetId(sheetId).stream()
                .filter(c -> c.getRawInput() != null && !c.getRawInput().isEmpty())
                .map(CellDto::from)
                .toList();
    }

    @Transactional
    public List<CellDto> updateCell(UUID userId, UUID sheetId, UpdateCellRequest request) {
        Sheet sheet = sheetService.findOrThrow(sheetId);
        Spreadsheet spreadsheet = spreadsheetService.findOrThrow(sheet.getSpreadsheetId());
        workspaceService.requireRoleAtLeast(spreadsheet.getWorkspaceId(), userId, WorkspaceRole.EDITOR);

        if (request.getRow() < 0 || request.getCol() < 0) {
            throw ApiException.badRequest("Row and column must be non-negative");
        }

        // Load the whole sheet into memory so the dependency graph can be built and
        // recalculated together; this is the "load once, recalc many" tradeoff noted above.
        Map<Key, Cell> cells = new HashMap<>();
        for (Cell c : cellRepository.findBySheetId(sheetId)) {
            cells.put(new Key(c.getRowIndex(), c.getColIndex()), c);
        }

        Key targetKey = new Key(request.getRow(), request.getCol());
        Cell target = cells.get(targetKey);
        if (target == null) {
            target = new Cell();
            target.setSheetId(sheetId);
            target.setRowIndex(request.getRow());
            target.setColIndex(request.getCol());
            cells.put(targetKey, target);
        }

        if (request.getExpectedVersion() >= 0 && target.getVersion() != request.getExpectedVersion()) {
            throw ApiException.conflict(
                    "This cell was changed by another user. Your version: " + request.getExpectedVersion()
                    + ", current version: " + target.getVersion());
        }

        String rawInput = request.getRawInput();
        target.setRawInput(rawInput == null || rawInput.isBlank() ? null : rawInput);
        target.setVersion(target.getVersion() + 1);
        target.setModifiedBy(userId);
        target.setModifiedAt(java.time.Instant.now());

        Set<Key> changed = recalculateSheet(cells);
        changed.add(targetKey);

        List<Cell> toSave = changed.stream().map(cells::get).filter(Objects::nonNull).toList();
        cellRepository.saveAll(toSave);

        return toSave.stream().map(CellDto::from).toList();
    }

    /**
     * Re-parses every formula cell, builds the dependency graph, detects cycles, and
     * evaluates in topological order. Returns the set of cell keys whose computed
     * value changed as a result (used to decide what to persist and push to clients).
     */
    private Set<Key> recalculateSheet(Map<Key, Cell> cells) {
        Map<Key, Node> formulaAsts = new HashMap<>();
        Map<Key, Set<Key>> dependsOn = new HashMap<>();

        for (Map.Entry<Key, Cell> entry : cells.entrySet()) {
            Cell cell = entry.getValue();
            String raw = cell.getRawInput();

            if (raw == null || raw.isEmpty()) {
                cell.setComputedValue(null);
                cell.setValueType(CellValueType.EMPTY);
                cell.setErrorCode(null);
                continue;
            }

            if (!raw.startsWith("=")) {
                applyLiteral(cell, raw);
                continue;
            }

            try {
                Node ast = Parser.parse(raw.substring(1));
                formulaAsts.put(entry.getKey(), ast);
                Set<Key> deps = new HashSet<>();
                for (DependencyExtractor.Coord c : DependencyExtractor.extract(ast)) {
                    deps.add(new Key(c.row(), c.col()));
                }
                dependsOn.put(entry.getKey(), deps);
            } catch (FormulaException e) {
                cell.setValueType(CellValueType.ERROR);
                cell.setErrorCode(e.getErrorCode());
                cell.setComputedValue(e.getErrorCode());
            }
        }

        List<Key> order = topologicalSort(formulaAsts.keySet(), dependsOn, cells);

        for (Key key : order) {
            Cell cell = cells.get(key);
            Node ast = formulaAsts.get(key);
            if (ast == null) continue; // marked as circular / errored during sort

            try {
                CellContext context = (row, col) -> {
                    Cell c = cells.get(new Key(row, col));
                    if (c == null) return null;
                    return valueOf(c);
                };
                Object result = new FormulaEvaluator(context).evaluate(ast);
                applyComputed(cell, result);
            } catch (FormulaException e) {
                cell.setValueType(CellValueType.ERROR);
                cell.setErrorCode(e.getErrorCode());
                cell.setComputedValue(e.getErrorCode());
            } catch (Exception e) {
                cell.setValueType(CellValueType.ERROR);
                cell.setErrorCode("#ERROR!");
                cell.setComputedValue("#ERROR!");
            }
        }

        Set<Key> touched = new HashSet<>(formulaAsts.keySet());
        touched.addAll(cells.keySet().stream()
                .filter(k -> {
                    Cell c = cells.get(k);
                    return c.getRawInput() != null && !c.getRawInput().startsWith("=");
                })
                .toList());
        return touched;
    }

    /** Kahn's algorithm; any cell left unvisited is part of a cycle and gets #CIRCULAR!. */
    private List<Key> topologicalSort(Set<Key> formulaCells, Map<Key, Set<Key>> dependsOn, Map<Key, Cell> cells) {
        Map<Key, Integer> inDegree = new HashMap<>();
        Map<Key, List<Key>> dependents = new HashMap<>();

        for (Key k : formulaCells) {
            inDegree.putIfAbsent(k, 0);
            for (Key dep : dependsOn.getOrDefault(k, Set.of())) {
                if (formulaCells.contains(dep)) {
                    dependents.computeIfAbsent(dep, d -> new ArrayList<>()).add(k);
                    inDegree.merge(k, 1, Integer::sum);
                }
            }
        }

        Deque<Key> queue = new ArrayDeque<>();
        for (Key k : formulaCells) {
            if (inDegree.getOrDefault(k, 0) == 0) queue.add(k);
        }

        List<Key> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            Key k = queue.poll();
            order.add(k);
            for (Key dependent : dependents.getOrDefault(k, List.of())) {
                int updated = inDegree.merge(dependent, -1, Integer::sum);
                if (updated == 0) queue.add(dependent);
            }
        }

        if (order.size() < formulaCells.size()) {
            Set<Key> cyclic = new HashSet<>(formulaCells);
            cyclic.removeAll(order);
            for (Key k : cyclic) {
                Cell cell = cells.get(k);
                cell.setValueType(CellValueType.ERROR);
                cell.setErrorCode("#CIRCULAR!");
                cell.setComputedValue("#CIRCULAR!");
            }
        }

        return order;
    }

    private void applyLiteral(Cell cell, String raw) {
        try {
            double num = Double.parseDouble(raw.trim());
            cell.setValueType(CellValueType.NUMBER);
            cell.setComputedValue(formatNumber(num));
            cell.setErrorCode(null);
        } catch (NumberFormatException e) {
            if (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("false")) {
                cell.setValueType(CellValueType.BOOLEAN);
                cell.setComputedValue(raw.toLowerCase());
            } else {
                cell.setValueType(CellValueType.TEXT);
                cell.setComputedValue(raw);
            }
            cell.setErrorCode(null);
        }
    }

    private void applyComputed(Cell cell, Object result) {
        cell.setErrorCode(null);
        if (result instanceof Double d) {
            cell.setValueType(CellValueType.NUMBER);
            cell.setComputedValue(formatNumber(d));
        } else if (result instanceof Boolean b) {
            cell.setValueType(CellValueType.BOOLEAN);
            cell.setComputedValue(String.valueOf(b));
        } else if (result instanceof List<?>) {
            // A bare range with no aggregating function (e.g. "=A1:A3") — show the count as a hint.
            cell.setValueType(CellValueType.TEXT);
            cell.setComputedValue("#ARRAY");
        } else {
            cell.setValueType(CellValueType.TEXT);
            cell.setComputedValue(String.valueOf(result));
        }
    }

    private Object valueOf(Cell cell) {
        if (cell.getValueType() == null) return null;
        return switch (cell.getValueType()) {
            case EMPTY -> null;
            case NUMBER -> cell.getComputedValue() == null ? null : Double.parseDouble(cell.getComputedValue());
            case BOOLEAN -> Boolean.parseBoolean(cell.getComputedValue());
            case TEXT, ERROR -> cell.getComputedValue();
        };
    }

    private String formatNumber(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    private record Key(int row, int col) {}
}
