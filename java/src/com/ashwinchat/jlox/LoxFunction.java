package com.ashwinchat.jlox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.isInitializer = isInitializer;
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return this.declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < this.declaration.params.size(); i++) {
            environment.define(this.declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(this.declaration.body, environment);
        } catch (Return returnValue) {
            if (this.isInitializer) {
                return closure.getAt(0, "this");
            }
            return returnValue.value;
        }

        if (this.isInitializer) {
            return this.closure.getAt(0, "this");
        }

        return null;
    }

    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(this.closure);
        environment.define("this", instance);
        return new LoxFunction(this.declaration, environment, this.isInitializer);
    }

    @Override
    public String toString() {
        return "<fn " + this.declaration.name.lexeme + ">";
    }
}
