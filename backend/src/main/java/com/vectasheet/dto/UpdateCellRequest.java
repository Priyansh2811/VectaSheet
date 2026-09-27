package com.vectasheet.dto;

public class UpdateCellRequest {
    private int row;
    private int col;
    /** Raw text the user typed; null or empty clears the cell. */
    private String rawInput;
    /** The version the client last saw, for optimistic-concurrency conflict detection. -1 skips the check. */
    private long expectedVersion = -1;

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }
    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }
    public String getRawInput() { return rawInput; }
    public void setRawInput(String rawInput) { this.rawInput = rawInput; }
    public long getExpectedVersion() { return expectedVersion; }
    public void setExpectedVersion(long expectedVersion) { this.expectedVersion = expectedVersion; }
}
