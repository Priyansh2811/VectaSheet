package com.vectasheet.dto;

import com.vectasheet.entity.Cell;
import com.vectasheet.formula.CellRef;

public class CellDto {
    private String ref;
    private int row;
    private int col;
    private String rawInput;
    private String computedValue;
    private String valueType;
    private String errorCode;
    private long version;
    private String modifiedBy;
    private String modifiedAt;

    public static CellDto from(Cell c) {
        CellDto dto = new CellDto();
        dto.ref = CellRef.format(c.getRowIndex(), c.getColIndex());
        dto.row = c.getRowIndex();
        dto.col = c.getColIndex();
        dto.rawInput = c.getRawInput();
        dto.computedValue = c.getComputedValue();
        dto.valueType = c.getValueType().name();
        dto.errorCode = c.getErrorCode();
        dto.version = c.getVersion();
        dto.modifiedBy = c.getModifiedBy() != null ? c.getModifiedBy().toString() : null;
        dto.modifiedAt = c.getModifiedAt() != null ? c.getModifiedAt().toString() : null;
        return dto;
    }

    public String getRef() { return ref; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public String getRawInput() { return rawInput; }
    public String getComputedValue() { return computedValue; }
    public String getValueType() { return valueType; }
    public String getErrorCode() { return errorCode; }
    public long getVersion() { return version; }
    public String getModifiedBy() { return modifiedBy; }
    public String getModifiedAt() { return modifiedAt; }
}
