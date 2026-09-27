package com.vectasheet.formula;

import java.util.HashSet;
import java.util.Set;

/**
 * Walks a parsed formula's AST and collects every (row, col) cell it depends on,
 * expanding ranges. Used to build the sheet's dependency graph for recalculation
 * ordering and circular-reference detection.
 */
public final class DependencyExtractor {

    private DependencyExtractor() {}

    public record Coord(int row, int col) {}

    public static Set<Coord> extract(Node node) {
        Set<Coord> deps = new HashSet<>();
        walk(node, deps);
        return deps;
    }

    private static void walk(Node node, Set<Coord> deps) {
        switch (node) {
            case Node.CellRefNode n -> deps.add(new Coord(n.row(), n.col()));
            case Node.RangeNode n -> {
                for (int r = n.startRow(); r <= n.endRow(); r++) {
                    for (int c = n.startCol(); c <= n.endCol(); c++) {
                        deps.add(new Coord(r, c));
                    }
                }
            }
            case Node.BinaryOpNode n -> {
                walk(n.left(), deps);
                walk(n.right(), deps);
            }
            case Node.UnaryOpNode n -> walk(n.operand(), deps);
            case Node.FunctionCallNode n -> n.args().forEach(arg -> walk(arg, deps));
            case Node.NumberNode ignored -> {}
            case Node.StringNode ignored -> {}
            case Node.BooleanNode ignored -> {}
        }
    }
}
