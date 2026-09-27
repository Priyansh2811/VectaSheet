package com.vectasheet.dto;

import com.vectasheet.entity.Sheet;
import java.util.UUID;

public class SheetDto {
    private UUID id;
    private UUID spreadsheetId;
    private String name;
    private int position;
    private int rowCount;
    private int colCount;

    public static SheetDto from(Sheet s) {
        SheetDto dto = new SheetDto();
        dto.id = s.getId();
        dto.spreadsheetId = s.getSpreadsheetId();
        dto.name = s.getName();
        dto.position = s.getPosition();
        dto.rowCount = s.getRowCount();
        dto.colCount = s.getColCount();
        return dto;
    }

    public UUID getId() { return id; }
    public UUID getSpreadsheetId() { return spreadsheetId; }
    public String getName() { return name; }
    public int getPosition() { return position; }
    public int getRowCount() { return rowCount; }
    public int getColCount() { return colCount; }
}
