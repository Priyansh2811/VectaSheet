package com.vectasheet.formula;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FormulaEngineTests {

    private Object eval(String formula, Map<String, Object> cellValues) {
        Node ast = Parser.parse(formula);
        CellContext ctx = (row, col) -> cellValues.get(CellRef.format(row, col));
        return new FormulaEvaluator(ctx).evaluate(ast);
    }

    @Test
    void basicArithmetic() {
        assertEquals(7.0, eval("3+4", Map.of()));
        assertEquals(1.0, eval("10/5-1", Map.of()));
        assertEquals(8.0, eval("2^3", Map.of()));
    }

    @Test
    void cellReferencesAndSum() {
        Map<String, Object> cells = new HashMap<>();
        cells.put("A1", 10.0);
        cells.put("B1", 20.0);
        assertEquals(30.0, eval("A1+B1", cells));
        assertEquals(30.0, eval("SUM(A1:B1)", cells));
    }

    @Test
    void ifFunction() {
        Map<String, Object> cells = Map.of("A1", 10.0);
        assertEquals("big", eval("IF(A1>5,\"big\",\"small\")", cells));
        assertEquals("small", eval("IF(A1<5,\"big\",\"small\")", cells));
    }

    @Test
    void divisionByZeroThrows() {
        FormulaException ex = assertThrows(FormulaException.class, () -> eval("5/0", Map.of()));
        assertEquals("#DIV/0!", ex.getErrorCode());
    }

    @Test
    void unknownFunctionThrowsNameError() {
        FormulaException ex = assertThrows(FormulaException.class, () -> eval("NOTAREALFN(1)", Map.of()));
        assertEquals("#NAME?", ex.getErrorCode());
    }

    @Test
    void textFunctions() {
        Map<String, Object> cells = Map.of("A1", "VectaSheet");
        assertEquals("Vec", eval("LEFT(A1,3)", cells));
        assertEquals("eet", eval("RIGHT(A1,3)", cells));
        assertEquals(10.0, eval("LEN(A1)", cells));
        assertEquals("Hello VectaSheet", eval("CONCAT(\"Hello \",A1)", cells));
    }

    @Test
    void roundingFunctions() {
        assertEquals(3.14, (Double) eval("ROUND(3.14159,2)", Map.of()), 0.0001);
        assertEquals(4.0, (Double) eval("ROUNDUP(3.1,0)", Map.of()), 0.0001);
        assertEquals(3.0, (Double) eval("ROUNDDOWN(3.9,0)", Map.of()), 0.0001);
    }

    @Test
    void dependencyExtractionFindsRangeCells() {
        Node ast = Parser.parse("SUM(A1:B2)+C1");
        var deps = DependencyExtractor.extract(ast);
        assertEquals(5, deps.size()); // A1,A2,B1,B2,C1
    }

    @Test
    void cellRefRoundTrip() {
        var coord = CellRef.parse("C3");
        assertEquals(2, coord.row());
        assertEquals(2, coord.col());
        assertEquals("C3", CellRef.format(2, 2));
        assertEquals("AA1", CellRef.format(0, 26));
    }
}
