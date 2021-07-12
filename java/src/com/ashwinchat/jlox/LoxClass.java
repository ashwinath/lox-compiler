package com.ashwinchat.jlox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    final String name;
    final LoxClass superclass;
    private final Map<String, LoxFunction> methods;

    LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
        this.superclass = superclass;
    }

    @Override
    public int arity() {
        LoxFunction initializer = this.findMethod("init");
        if (initializer == null) {
            return 0;
        }
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = this.findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    LoxFunction findMethod(String name) {
        if (this.methods.containsKey(name)) {
            return methods.get(name);
        }

        if (this.superclass != null) {
            return this.superclass.findMethod(name);
        }
        return null;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
