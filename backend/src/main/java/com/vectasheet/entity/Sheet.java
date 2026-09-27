package com.vectasheet.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sheets")
public class Sheet {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID spreadsheetId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int position = 0;

    @Column(nullable = false)
    private int rowCount = 50;

    @Column(nullable = false)
    private int colCount = 20;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSpreadsheetId() { return spreadsheetId; }
    public void setSpreadsheetId(UUID spreadsheetId) { this.spreadsheetId = spreadsheetId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public int getRowCount() { return rowCount; }
    public void setRowCount(int rowCount) { this.rowCount = rowCount; }
    public int getColCount() { return colCount; }
    public void setColCount(int colCount) { this.colCount = colCount; }
    public Instant getCreatedAt() { return createdAt; }
}
