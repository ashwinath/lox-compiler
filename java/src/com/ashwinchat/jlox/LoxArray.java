package com.ashwinchat.jlox;

import java.util.List;

public class LoxArray extends LoxInstance {
    private final Object[] elements;

    public LoxArray(int size) {
        super(null);
        this.elements = new Object[size];
    }

    @Override
    Object get(Token name) {
        /**
         * Theres three methods we add to this array.
         * 1. array.set(position, value).
         * 2. array.get(position) returns the value.
         * 3. array.length returns the length of the array.
         */
        if (name.lexeme.equals("get")) {
            return new LoxCallable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    int index = (int)(double)arguments.get(0);
                    return elements[index];
                }
            };
        } else if (name.lexeme.equals("set")) {
            return new LoxCallable() {
                @Override
                public int arity() {
                    return 2;
                }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    int index = (int)(double)arguments.get(0);
                    Object value = arguments.get(1);
                    return elements[index] = value;
                }
            };
        } else if (name.lexeme.equals("length")) {
            return elements.length;
        }

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    @Override
    void set(Token name, Object value) {
        throw new RuntimeError(name, "Can't add properties to arrays.");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < this.elements.length; ++i) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(this.elements[i]);
        }
        builder.append("]");
        return builder.toString();
    }
}
