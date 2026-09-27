package com.vectasheet.formula;

import java.util.List;

/**
 * AST node hierarchy for parsed formulas. Sealed so the evaluator's switch
 * over node types stays exhaustive and safe to extend.
 */
public sealed interface Node {

    record NumberNode(double value) implements Node {}

    record StringNode(String value) implements Node {}

    record BooleanNode(boolean value) implements Node {}

    record CellRefNode(int row, int col) implements Node {}

    record RangeNode(int startRow, int startCol, int endRow, int endCol) implements Node {}

    record BinaryOpNode(String op, Node left, Node right) implements Node {}

    record UnaryOpNode(String op, Node operand) implements Node {}

    record FunctionCallNode(String name, List<Node> args) implements Node {}
}
