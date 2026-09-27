package com.vectasheet.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cells", uniqueConstraints = @UniqueConstraint(columnNames = {"sheet_id", "row_index", "col_index"}))
public class Cell {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "sheet_id", nullable = false)
    private UUID sheetId;

    @Column(name = "row_index", nullable = false)
    private int rowIndex;

    @Column(name = "col_index", nullable = false)
    private int colIndex;

    /** What the user typed: a literal, or a formula starting with '='. Null means the cell is empty. */
    @Column(columnDefinition = "TEXT")
    private String rawInput;

    /** Cached evaluated result, stored as its string form for simplicity; valueType says how to read it. */
    @Column(columnDefinition = "TEXT")
    private String computedValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CellValueType valueType = CellValueType.EMPTY;

    private String errorCode;

    @Column(nullable = false)
    private long version = 0;

    private UUID modifiedBy;

    @Column(nullable = false)
    private Instant modifiedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSheetId() { return sheetId; }
    public void setSheetId(UUID sheetId) { this.sheetId = sheetId; }
    public int getRowIndex() { return rowIndex; }
    public void setRowIndex(int rowIndex) { this.rowIndex = rowIndex; }
    public int getColIndex() { return colIndex; }
    public void setColIndex(int colIndex) { this.colIndex = colIndex; }
    public String getRawInput() { return rawInput; }
    public void setRawInput(String rawInput) { this.rawInput = rawInput; }
    public String getComputedValue() { return computedValue; }
    public void setComputedValue(String computedValue) { this.computedValue = computedValue; }
    public CellValueType getValueType() { return valueType; }
    public void setValueType(CellValueType valueType) { this.valueType = valueType; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    public UUID getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(UUID modifiedBy) { this.modifiedBy = modifiedBy; }
    public Instant getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(Instant modifiedAt) { this.modifiedAt = modifiedAt; }
}
