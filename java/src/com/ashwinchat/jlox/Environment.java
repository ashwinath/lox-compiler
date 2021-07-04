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
        }
        throw new RuntimeError(name, "Undefined variable ' " + name.lexeme + "'.");
    }
}
