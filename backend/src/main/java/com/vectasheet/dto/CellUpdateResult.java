package com.vectasheet.dto;

import java.util.List;

/** Returned after a cell edit: the target cell plus every dependent cell that was recalculated. */
public class CellUpdateResult {
    private List<CellDto> updatedCells;

    public CellUpdateResult(List<CellDto> updatedCells) {
        this.updatedCells = updatedCells;
    }

    public List<CellDto> getUpdatedCells() { return updatedCells; }
}
