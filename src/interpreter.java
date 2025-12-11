import java.util.*;

// ===== WARTOŚCI W INTERPRETERZE =====
abstract class Value {
    abstract Object getValue();
    abstract String getTypeName();
}

class IntValue extends Value {
    final long value;
    IntValue(long value) { this.value = value; }

    @Override
    Object getValue() { return value; }

    @Override
    String getTypeName() { return "int"; }

    @Override
    public String toString() { return String.valueOf(value); }
}

class DoubleValue extends Value {
    final double value;
    DoubleValue(double value) { this.value = value; }

    @Override
    Object getValue() { return value; }

    @Override
    String getTypeName() { return "double"; }

    @Override
    public String toString() { return String.valueOf(value); }
}

class StringValue extends Value {
    final String value;
    StringValue(String value) { this.value = value; }

    @Override
    Object getValue() { return value; }

    @Override
    String getTypeName() { return "string"; }

    @Override
    public String toString() { return value; }
}

class BoolValue extends Value {
    final boolean value;
    BoolValue(boolean value) { this.value = value; }

    @Override
    Object getValue() { return value; }

    @Override
    String getTypeName() { return "bool"; }

    @Override
    public String toString() { return String.valueOf(value); }
}

class VoidValue extends Value {
    static final VoidValue INSTANCE = new VoidValue();
    private VoidValue() {}

    @Override
    Object getValue() { return null; }

    @Override
    String getTypeName() { return "void"; }

    @Override
    public String toString() { return "void"; }
}

// ===== WYJĄTKI =====
class ReturnException extends RuntimeException {
    final Value value;
    ReturnException(Value value) { this.value = value; }
}

class RuntimeError extends RuntimeException {
    RuntimeError(String message) { super(message); }
}

// ===== ŚRODOWISKO (ZMIENNE) =====
class Environment {
    private final Map<String, Value> variables = new HashMap<>();
    private final Environment parent;

    Environment() {
        this.parent = null;
    }

    Environment(Environment parent) {
        this.parent = parent;
    }

    void define(String name, Value value) {
        variables.put(name, value);
    }

    Value get(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        if (parent != null) {
            return parent.get(name);
        }
        throw new RuntimeError("Undefined variable: " + name);
    }

    void assign(String name, Value value) {
        if (variables.containsKey(name)) {
            variables.put(name, value);
            return;
        }
        if (parent != null) {
            parent.assign(name, value);
            return;
        }
        throw new RuntimeError("Undefined variable: " + name);
    }

    boolean isDefined(String name) {
        if (variables.containsKey(name)) return true;
        if (parent != null) return parent.isDefined(name);
        return false;
    }
}

// ===== FUNKCJA =====
class Function {
    final String name;
    final List<Parameter> parameters;
    final List<Statement> body;
    final Environment closure;

    Function(String name, List<Parameter> parameters, List<Statement> body, Environment closure) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.closure = closure;
    }
}

// ===== INTERPRETER =====
class interpreter {
    private Environment globals = new Environment();
    private Environment environment = globals;
    private Map<String, Function> functions = new HashMap<>();

    public interpreter() {
        // Rejestracja wbudowanych funkcji
        registerBuiltins();
    }

    private void registerBuiltins() {
        // Możesz tutaj dodać wbudowane funkcje
    }

    // ===== INTERPRETACJA PROGRAMU =====
    public void interpret(Program program) {
        try {
            for (Statement statement : program.statements) {
                execute(statement);
            }
        } catch (RuntimeError e) {
            System.err.println("Runtime Error: " + e.getMessage());
            throw e;
        }
    }

    // ===== WYKONYWANIE INSTRUKCJI =====
    private void execute(Statement statement) {
        if (statement instanceof VariableDeclaration) {
            executeVariableDeclaration((VariableDeclaration) statement);
        } else if (statement instanceof Assignment) {
            executeAssignment((Assignment) statement);
        } else if (statement instanceof IfStatement) {
            executeIfStatement((IfStatement) statement);
        } else if (statement instanceof WhileStatement) {
            executeWhileStatement((WhileStatement) statement);
        } else if (statement instanceof ForStatement) {
            executeForStatement((ForStatement) statement);
        } else if (statement instanceof ReturnStatement) {
            executeReturnStatement((ReturnStatement) statement);
        } else if (statement instanceof ExpressionStatement) {
            evaluate(((ExpressionStatement) statement).expression);
        } else if (statement instanceof BlockStatement) {
            executeBlock((BlockStatement) statement, new Environment(environment));
        } else if (statement instanceof FunctionDeclaration) {
            executeFunctionDeclaration((FunctionDeclaration) statement);
        }
    }

    private void executeVariableDeclaration(VariableDeclaration stmt) {
        Value value = VoidValue.INSTANCE;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name, value);
    }

    private void executeAssignment(Assignment stmt) {
        Value value = evaluate(stmt.value);
        environment.assign(stmt.name, value);
    }

    private void executeIfStatement(IfStatement stmt) {
        Value condition = evaluate(stmt.condition);
        if (isTruthy(condition)) {
            for (Statement s : stmt.thenBranch) {
                execute(s);
            }
        } else if (stmt.elseBranch != null) {
            for (Statement s : stmt.elseBranch) {
                execute(s);
            }
        }
    }

    private void executeWhileStatement(WhileStatement stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            for (Statement s : stmt.body) {
                execute(s);
            }
        }
    }

    private void executeForStatement(ForStatement stmt) {
        Environment previous = environment;
        try {
            environment = new Environment(environment);

            if (stmt.initializer != null) {
                execute(stmt.initializer);
            }

            while (stmt.condition == null || isTruthy(evaluate(stmt.condition))) {
                for (Statement s : stmt.body) {
                    execute(s);
                }

                if (stmt.increment != null) {
                    execute(stmt.increment);
                }
            }
        } finally {
            environment = previous;
        }
    }

    private void executeReturnStatement(ReturnStatement stmt) {
        Value value = VoidValue.INSTANCE;
        if (stmt.value != null) {
            value = evaluate(stmt.value);
        }
        throw new ReturnException(value);
    }

    private void executeBlock(BlockStatement block, Environment env) {
        Environment previous = environment;
        try {
            environment = env;
            for (Statement statement : block.statements) {
                execute(statement);
            }
        } finally {
            environment = previous;
        }
    }

    private void executeFunctionDeclaration(FunctionDeclaration stmt) {
        Function function = new Function(stmt.name, stmt.parameters, stmt.body, environment);
        functions.put(stmt.name, function);
    }

    // ===== EWALUACJA WYRAŻEŃ =====
    private Value evaluate(Expression expr) {
        if (expr instanceof NumberLiteral) {
            return evaluateNumberLiteral((NumberLiteral) expr);
        } else if (expr instanceof StringLiteral) {
            return new StringValue(((StringLiteral) expr).value);
        } else if (expr instanceof BooleanLiteral) {
            return new BoolValue(((BooleanLiteral) expr).value);
        } else if (expr instanceof Identifier) {
            return environment.get(((Identifier) expr).name);
        } else if (expr instanceof BinaryOperation) {
            return evaluateBinaryOperation((BinaryOperation) expr);
        } else if (expr instanceof UnaryOperation) {
            return evaluateUnaryOperation((UnaryOperation) expr);
        } else if (expr instanceof FunctionCall) {
            return evaluateFunctionCall((FunctionCall) expr);
        }

        throw new RuntimeError("Unknown expression type: " + expr.getClass().getName());
    }

    private Value evaluateNumberLiteral(NumberLiteral literal) {
        String value = literal.value;
        if (value.contains(".")) {
            return new DoubleValue(Double.parseDouble(value));
        } else {
            return new IntValue(Long.parseLong(value));
        }
    }

    private Value evaluateBinaryOperation(BinaryOperation expr) {
        Value left = evaluate(expr.left);
        Value right = evaluate(expr.right);

        switch (expr.operator) {
            case PLUS:
                if (left instanceof IntValue && right instanceof IntValue) {
                    return new IntValue(((IntValue) left).value + ((IntValue) right).value);
                }
                if (left instanceof DoubleValue || right instanceof DoubleValue) {
                    return new DoubleValue(toDouble(left) + toDouble(right));
                }
                if (left instanceof StringValue || right instanceof StringValue) {
                    return new StringValue(left.toString() + right.toString());
                }
                break;

            case MINUS:
                if (left instanceof IntValue && right instanceof IntValue) {
                    return new IntValue(((IntValue) left).value - ((IntValue) right).value);
                }
                return new DoubleValue(toDouble(left) - toDouble(right));

            case STAR:
                if (left instanceof IntValue && right instanceof IntValue) {
                    return new IntValue(((IntValue) left).value * ((IntValue) right).value);
                }
                return new DoubleValue(toDouble(left) * toDouble(right));

            case SLASH:
                if (left instanceof IntValue && right instanceof IntValue) {
                    long r = ((IntValue) right).value;
                    if (r == 0) throw new RuntimeError("Division by zero");
                    return new IntValue(((IntValue) left).value / r);
                }
                double dr = toDouble(right);
                if (dr == 0) throw new RuntimeError("Division by zero");
                return new DoubleValue(toDouble(left) / dr);

            case MOD:
                if (left instanceof IntValue && right instanceof IntValue) {
                    return new IntValue(((IntValue) left).value % ((IntValue) right).value);
                }
                break;

            case EQUAL_EQUAL:
                return new BoolValue(isEqual(left, right));

            case BANG_EQUAL:
                return new BoolValue(!isEqual(left, right));

            case GREATER:
                return new BoolValue(toDouble(left) > toDouble(right));

            case GREATER_EQUAL:
                return new BoolValue(toDouble(left) >= toDouble(right));

            case LESS:
                return new BoolValue(toDouble(left) < toDouble(right));

            case LESS_EQUAL:
                return new BoolValue(toDouble(left) <= toDouble(right));

            case AND:
                return new BoolValue(isTruthy(left) && isTruthy(right));

            case OR:
                return new BoolValue(isTruthy(left) || isTruthy(right));
        }

        throw new RuntimeError("Unsupported binary operation: " + expr.operator);
    }

    private Value evaluateUnaryOperation(UnaryOperation expr) {
        Value operand = evaluate(expr.operand);

        switch (expr.operator) {
            case MINUS:
                if (operand instanceof IntValue) {
                    return new IntValue(-((IntValue) operand).value);
                }
                if (operand instanceof DoubleValue) {
                    return new DoubleValue(-((DoubleValue) operand).value);
                }
                throw new RuntimeError("Operand must be a number");

            case BANG:
                return new BoolValue(!isTruthy(operand));
        }

        throw new RuntimeError("Unsupported unary operation: " + expr.operator);
    }

    private Value evaluateFunctionCall(FunctionCall call) {
        // Sprawdź wbudowane funkcje
        if (call.functionName.equals("print") || call.functionName.equals("println")) {
            for (Expression arg : call.arguments) {
                Value val = evaluate(arg);
                System.out.print(val);
            }
            if (call.functionName.equals("println")) {
                System.out.println();
            }
            return VoidValue.INSTANCE;
        }

        // Sprawdź zdefiniowane funkcje
        if (!functions.containsKey(call.functionName)) {
            throw new RuntimeError("Undefined function: " + call.functionName);
        }

        Function function = functions.get(call.functionName);

        if (call.arguments.size() != function.parameters.size()) {
            throw new RuntimeError("Expected " + function.parameters.size() +
                    " arguments but got " + call.arguments.size());
        }

        // Utwórz nowe środowisko dla funkcji
        Environment functionEnv = new Environment(function.closure);

        // Przypisz argumenty do parametrów
        for (int i = 0; i < function.parameters.size(); i++) {
            Value argValue = evaluate(call.arguments.get(i));
            functionEnv.define(function.parameters.get(i).name, argValue);
        }

        // Wykonaj ciało funkcji
        Environment previous = environment;
        try {
            environment = functionEnv;
            for (Statement stmt : function.body) {
                execute(stmt);
            }
        } catch (ReturnException e) {
            return e.value;
        } finally {
            environment = previous;
        }

        return VoidValue.INSTANCE;
    }

    // ===== METODY POMOCNICZE =====
    private boolean isTruthy(Value value) {
        if (value instanceof BoolValue) {
            return ((BoolValue) value).value;
        }
        if (value instanceof IntValue) {
            return ((IntValue) value).value != 0;
        }
        if (value instanceof DoubleValue) {
            return ((DoubleValue) value).value != 0;
        }
        if (value instanceof StringValue) {
            return !((StringValue) value).value.isEmpty();
        }
        return value != VoidValue.INSTANCE;
    }

    private boolean isEqual(Value a, Value b) {
        if (a instanceof IntValue && b instanceof IntValue) {
            return ((IntValue) a).value == ((IntValue) b).value;
        }
        if (a instanceof DoubleValue || b instanceof DoubleValue) {
            return Math.abs(toDouble(a) - toDouble(b)) < 0.0000001;
        }
        if (a instanceof BoolValue && b instanceof BoolValue) {
            return ((BoolValue) a).value == ((BoolValue) b).value;
        }
        if (a instanceof StringValue && b instanceof StringValue) {
            return ((StringValue) a).value.equals(((StringValue) b).value);
        }
        return false;
    }

    private double toDouble(Value value) {
        if (value instanceof IntValue) {
            return ((IntValue) value).value;
        }
        if (value instanceof DoubleValue) {
            return ((DoubleValue) value).value;
        }
        throw new RuntimeError("Cannot convert to number: " + value.getTypeName());
    }

    // ===== PUBLICZNE API =====
    public void executeCode(String code) {
        List<Token> tokens = Token.tokenize(code);
        Parser parser = new Parser(tokens);
        Program program = parser.parse();
        interpret(program);
    }

    public Environment getGlobalEnvironment() {
        return globals;
    }

    public void reset() {
        globals = new Environment();
        environment = globals;
        functions.clear();
        registerBuiltins();
    }
}