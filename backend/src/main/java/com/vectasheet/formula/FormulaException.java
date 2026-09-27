package com.vectasheet.formula;

/**
 * Raised when a formula can't be parsed or evaluated. The errorCode follows
 * spreadsheet conventions (#DIV/0!, #REF!, #CIRCULAR!, #ERROR!, #NAME?) so the
 * frontend can render a meaningful, familiar error in the cell.
 */
public class FormulaException extends RuntimeException {
    private final String errorCode;

    public FormulaException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
