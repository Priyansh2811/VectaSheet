package com.vectasheet.formula;

/**
 * Supplies the current computed value of another cell in the same sheet during
 * formula evaluation. An unset cell is treated as empty (0 for numeric context,
 * "" for string context) rather than an error, matching normal spreadsheet behavior.
 */
public interface CellContext {
    Object getValue(int row, int col);
}
