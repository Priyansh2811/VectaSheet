package com.vectasheet.formula;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts between A1-style references ("B3", "$A$1") and zero-based (row, col) coordinates.
 */
public final class CellRef {

    private static final Pattern CELL_PATTERN = Pattern.compile("^\\$?([A-Za-z]{1,3})\\$?(\\d+)$");

    private CellRef() {}

    public static boolean isCellRef(String text) {
        return CELL_PATTERN.matcher(text).matches();
    }

    public record Coord(int row, int col) {}

    public static Coord parse(String ref) {
        Matcher m = CELL_PATTERN.matcher(ref);
        if (!m.matches()) {
            throw new FormulaException("#REF!", "Invalid cell reference: " + ref);
        }
        String colLetters = m.group(1).toUpperCase();
        int rowNum = Integer.parseInt(m.group(2));

        int col = 0;
        for (int i = 0; i < colLetters.length(); i++) {
            col = col * 26 + (colLetters.charAt(i) - 'A' + 1);
        }
        col -= 1; // zero-based
        int row = rowNum - 1; // zero-based

        return new Coord(row, col);
    }

    public static String format(int row, int col) {
        StringBuilder colStr = new StringBuilder();
        int c = col + 1;
        while (c > 0) {
            int rem = (c - 1) % 26;
            colStr.insert(0, (char) ('A' + rem));
            c = (c - 1) / 26;
        }
        return colStr + String.valueOf(row + 1);
    }
}
