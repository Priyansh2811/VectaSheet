package com.vectasheet.formula;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns a formula string (without the leading '=') into a flat list of tokens.
 * Supports numbers, strings, booleans, cell references (A1, $A$1), ranges (A1:B2),
 * identifiers/function names, and the operators + - * / ^ % = <> < > <= >= & , ( ) : $
 */
public class Lexer {

    public enum TokenType {
        NUMBER, STRING, IDENTIFIER, CELL_REF, OPERATOR, LPAREN, RPAREN, COMMA, COLON, EOF
    }

    public record Token(TokenType type, String text) {}

    private final String input;
    private int pos = 0;

    public Lexer(String input) {
        this.input = input == null ? "" : input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < input.length()) {
            char c = input.charAt(pos);

            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }
            if (c == '(') { tokens.add(new Token(TokenType.LPAREN, "(")); pos++; continue; }
            if (c == ')') { tokens.add(new Token(TokenType.RPAREN, ")")); pos++; continue; }
            if (c == ',') { tokens.add(new Token(TokenType.COMMA, ",")); pos++; continue; }
            if (c == ':') { tokens.add(new Token(TokenType.COLON, ":")); pos++; continue; }

            if (c == '"') {
                tokens.add(readString());
                continue;
            }

            if (Character.isDigit(c) || (c == '.' && pos + 1 < input.length() && Character.isDigit(input.charAt(pos + 1)))) {
                tokens.add(readNumber());
                continue;
            }

            if (c == '$' || Character.isLetter(c)) {
                tokens.add(readIdentifierOrCellRef());
                continue;
            }

            if (c == '<' || c == '>' || c == '=') {
                tokens.add(readComparisonOperator());
                continue;
            }

            if ("+-*/^%&".indexOf(c) >= 0) {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(c)));
                pos++;
                continue;
            }

            throw new FormulaException("#ERROR!", "Unexpected character '" + c + "' in formula");
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private Token readString() {
        StringBuilder sb = new StringBuilder();
        pos++; // skip opening quote
        while (pos < input.length() && input.charAt(pos) != '"') {
            sb.append(input.charAt(pos));
            pos++;
        }
        if (pos >= input.length()) {
            throw new FormulaException("#ERROR!", "Unterminated string literal");
        }
        pos++; // skip closing quote
        return new Token(TokenType.STRING, sb.toString());
    }

    private Token readNumber() {
        int start = pos;
        while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
            pos++;
        }
        return new Token(TokenType.NUMBER, input.substring(start, pos));
    }

    private Token readIdentifierOrCellRef() {
        int start = pos;
        while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '$' || input.charAt(pos) == '_')) {
            pos++;
        }
        String text = input.substring(start, pos);
        if (CellRef.isCellRef(text)) {
            return new Token(TokenType.CELL_REF, text);
        }
        return new Token(TokenType.IDENTIFIER, text);
    }

    private Token readComparisonOperator() {
        char c = input.charAt(pos);
        if (pos + 1 < input.length()) {
            char next = input.charAt(pos + 1);
            if ((c == '<' && next == '>') || (c == '<' && next == '=') || (c == '>' && next == '=')) {
                pos += 2;
                return new Token(TokenType.OPERATOR, "" + c + next);
            }
        }
        pos++;
        return new Token(TokenType.OPERATOR, String.valueOf(c));
    }
}
