package com.ashwinchat.jlox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ashwinchat.jlox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;
    private int loopDepth = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!this.isAtEnd()) {
            statements.add(this.declaration());
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            if (this.match(VAR)) {
                return this.varDeclaration();
            }
            return this.statement();
        } catch (ParseError error) {
            this.synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = this.consume(IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (this.match(EQUAL)) {
            initializer = this.expression();
        }
        this.consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (this.match(PRINT)) {
            return this.printStatement();
        }
        if (this.match(WHILE)) {
            return this.whileStatement();
        }
        if (this.match(LEFT_BRACE)) {
            return new Stmt.Block(this.block());
        }
        if (this.match(FOR)) {
            return this.forStatement();
        }
        if (this.match(IF)) {
            return this.ifStatement();
        }
        if (this.match(BREAK)) {
            return this.breakStatement();
        }

        return this.expressionStatement();
    }

    private Stmt breakStatement() {
        if (loopDepth == 0) {
            this.error(this.previous(), "Must be inside a loop to use 'break'.");
        }
        this.consume(SEMICOLON, "Expect ';' after 'break'.");
        return new Stmt.Break();
    }

    private Stmt forStatement() {
        this.consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initalizer;
        if (this.match(SEMICOLON)) {
            initalizer = null;
        } else if (this.match(VAR)) {
            initalizer = varDeclaration();
        } else {
            initalizer = this.expressionStatement();
        }

        Expr condition = null;
        if (!this.check(SEMICOLON)) {
            condition = this.expression();
        }
        this.consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!this.check(RIGHT_PAREN)) {
            increment = this.expression();
        }
        this.consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        try {
            loopDepth++;
            Stmt body = this.statement();
            if (increment != null) {
                body = new Stmt.Block(
                        Arrays.asList(body, new Stmt.Expression(increment))
                );
            }

            if (condition == null) {
                condition = new Expr.Literal(true);
            }
            body = new Stmt.While(condition, body);

            if (initalizer != null) {
                body = new Stmt.Block(Arrays.asList(initalizer, body));
            }

            return body;
        } finally {
            loopDepth--;
        }

    }

    private Stmt whileStatement() {
        this.consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = this.expression();
        this.consume(RIGHT_PAREN, "expect ')' after condition.");

        try {
            loopDepth++;
            Stmt body = this.statement();
            return new Stmt.While(condition, body);
        } finally {
            loopDepth--;
        }
    }

    private Stmt ifStatement() {
        this.consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = this.expression();
        this.consume(RIGHT_PAREN, "expect ')' after if condition.");

        Stmt thenBranch = this.statement();
        Stmt elseBranch = null;
        if (this.match(ELSE)) {
            elseBranch = this.statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!this.check(RIGHT_BRACE) && !this.isAtEnd()) {
            statements.add(this.declaration());
        }
        this.consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt expressionStatement() {
        Expr expr = this.expression();
        this.consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Expression(expr);
    }

    private Stmt printStatement() {
        Expr value = this.expression();
        this.consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Expr expression() {
        return this.assignment();
    }

    private Expr assignment() {
        /*
         * This is interesting because you want to evaluate the L-Value as well.
         * Example: NewObject(x, y).x = (5 + 2) * 3;
         * L-Value must be evaluated before the assignment happens.
         */
        Expr expr = this.or();
        if (this.match(EQUAL)) {
            Token equals = this.previous();
            Expr value = this.assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }
            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr or() {
        Expr expr = this.and();

        while (this.match(OR)) {
            Token operator = this.previous();
            Expr right = this.and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = this.equality();

        while (this.match(AND)) {
            Token operator = this.previous();
            Expr right = this.equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = this.comparison();

        while (this.match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = this.previous();
            Expr right = this.comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = this.term();

        while (this.match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = this.previous();
            Expr right = this.term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = this.factor();

        while (this.match(MINUS, PLUS)) {
            Token operator = this.previous();
            Expr right = this.factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = this.unary();

        while (this.match(SLASH, STAR)) {
            Token operator = this.previous();
            Expr right = this.unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (this.match(BANG, MINUS)) {
            Token operator = this.previous();
            Expr right = this.unary();
            return new Expr.Unary(operator, right);
        }
        return this.primary();
    }

    private Expr primary() {
        if (this.match(FALSE)) {
            return new Expr.Literal(false);
        }
        if (this.match(TRUE)) {
            return new Expr.Literal(true);
        }
        if (this.match(NIL)) {
            return new Expr.Literal(null);
        }

        if (this.match(NUMBER, STRING)) {
            return new Expr.Literal(this.previous().literal);
        }

        if (this.match(LEFT_PAREN)) {
            Expr expr = this.expression();
            this.consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        if (this.match(IDENTIFIER)) {
            return new Expr.Variable(this.previous());
        }

        throw error(this.peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (this.check(type)) {
                this.advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (this.isAtEnd()) {
            return false;
        }
        return this.peek().type == type;
    }

    private Token advance() {
        if (!this.isAtEnd()) {
            this.current++;
        }
        return this.previous();
    }

    private boolean isAtEnd() {
        return this.peek().type == EOF;
    }

    private Token peek() {
        return this.tokens.get(current);
    }

    private Token previous() {
        return this.tokens.get(this.current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (this.check(type)) {
            return advance();
        }
        throw this.error(this.peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        this.advance();

        while (!this.isAtEnd()) {
            if (this.previous().type == SEMICOLON) {
                return;
            }
            switch (this.peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            this.advance();
        }
    }
}
