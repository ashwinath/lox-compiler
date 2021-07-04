package com.ashwinchat.jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ashwinchat.jlox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
        keywords.put("break", BREAK);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            this.start = this.current;
            scanToken();
        }

        this.tokens.add(new Token(EOF, "", null, this.line));
        return tokens;
    }

    private boolean isAtEnd() {
        return this.current >= this.source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': this.addToken(LEFT_PAREN); break;
            case ')': this.addToken(RIGHT_PAREN); break;
            case '{': this.addToken(LEFT_BRACE); break;
            case '}': this.addToken(RIGHT_BRACE); break;
            case ',': this.addToken(COMMA); break;
            case '.': this.addToken(DOT); break;
            case '-': this.addToken(MINUS); break;
            case '+': this.addToken(PLUS); break;
            case ';': this.addToken(SEMICOLON); break;
            case '*': this.addToken(STAR); break;
            case '!':
                this.addToken(this.match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                this.addToken(this.match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                this.addToken(this.match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                this.addToken(this.match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (this.match('/')) {
                    // A comment goes until the end of the line.
                    while (this.peek() != '\n' && !this.isAtEnd()) {
                        this.advance();
                    }
                } else if (this.match('*')) {
                    while (!this.isAtEnd()) {
                        if (this.peek() == '*' && this.peekNext() == '/') {
                            this.advance();
                            this.advance();
                            break;
                        }
                        if (this.peek() == '\n') {
                            this.line++;
                        }
                        this.advance();
                    }
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
                // fallthrough
            case '\r':
                // fallthrough
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"': this.string(); break;
            default:
                if (this.isDigit(c)) {
                    this.number();
                } else if (this.isAlpha(c)) {
                    this.identifier();
                } else {
                    Lox.error(this.line, "Unexpected character.");
                }
                break;
        }
    }

    private char advance() {
        return this.source.charAt(current++);
    }

    private void addToken(TokenType type) {
        this.addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = this.source.substring(this.start, this.current);
        this.tokens.add(new Token(type, text, literal, this.line));
    }

    private boolean match(char expected) {
        if (this.isAtEnd()) {
            return false;
        }

        if (this.source.charAt(this.current) != expected) {
            return false;
        }

        this.current++;
        return true;
    }

    private char peek() {
        if (this.isAtEnd()) {
            return '\0';
        }
        return this.source.charAt(this.current);
    }

    private void string() {
        while (this.peek() != '"' && !this.isAtEnd()) {
            if (this.peek() == '\n') {
                this.line++;
            }
            this.advance();
        }

        if (this.isAtEnd()) {
            Lox.error(this.line, "Unterminated string.");
            return;
        }

        // The closing ".
        this.advance();

        // Trim the surrounding quotes.
        String value = this.source.substring(this.start + 1, this.current -1);
        this.addToken(STRING, value);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (this.isDigit(this.peek())) {
            advance();
        }

        // Look for a fractional part.
        if (this.peek() == '.' && this.isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (this.isDigit(this.peek())) {
                advance();
            }
        }

        this.addToken(
                NUMBER,
                Double.parseDouble(
                        this.source.substring(this.start, this.current)
                )
        );
    }

    private char peekNext() {
        if (current + 1 > this.source.length()) {
            return '\0';
        }

        return this.source.charAt(current + 1);
    }

    private void identifier() {
        while (this.isAlphaNumeric(this.peek())) {
            advance();
        }

        String text = this.source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) {
            type = IDENTIFIER;
        }
        addToken(type);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
