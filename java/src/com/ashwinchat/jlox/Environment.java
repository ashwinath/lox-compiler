package com.ashwinchat.jlox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    void define(String name, Object value) {
        this.values.put(name, value);
    }

    Environment() {
        this.enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }
        return environment;
    }

    Object get(Token name) {
        if (this.values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (this.enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable ' " + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        if (this.values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        if (this.enclosing != null) {
            this.enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable ' " + name.lexeme + "'.");
    }

    Object getAt(int distance, String name) {
        return this.ancestor(distance).values.get(name);
    }

    void assignAt(int distance, Token name, Object value) {
        this.ancestor(distance).values.put(name.lexeme, value);
    }
}
