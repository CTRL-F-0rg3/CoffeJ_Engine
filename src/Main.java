import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        IO.println(String.format("Hello and welcome!"));

        for (int i = 1; i <= 5; i++) {
            IO.println("i = " + i);
        }

        // Test parsera
        testParser();
    }

    static void testParser() {
        String code = "int32 x = 5 ;";
        List<Token> tokens = Token.tokenize(code);
        Parser parser = new Parser(tokens);
        Program program = parser.parse();
        IO.println("Parser test completed!");
    }

}

class IO {
    static void println(String msg) {
        System.out.println(msg);
    }
}

class Lexer {
    public enum TokenType {
        // ===== KEYWORDS =====
        IF, ELSE, ELSEIF,
        WHILE, FOR,
        RETURN,
        CLASS, FN,
        TRUE, FALSE,
        INT32, INT64, INT128,
        LONG, DOUBLE, FLOAT,
        STRING_TYPE, BOOL_TYPE, VOID,

        // ===== IDENTIFIERS & LITERALS =====
        IDENTIFIER,
        NUMBER,
        STRING,

        // ===== OPERATORS =====
        PLUS,           // +
        MINUS,          // -
        STAR,           // *
        SLASH,          // /
        MOD,            // %

        EQUAL,          // =
        EQUAL_EQUAL,    // ==
        BANG,           // !
        BANG_EQUAL,     // !=
        GREATER,        // >
        GREATER_EQUAL,  // >=
        LESS,           // <
        LESS_EQUAL,     // <=

        // ===== LOGICAL =====
        AND,            // &&
        OR,             // ||

        // ===== STRUCTURE =====
        LBRACE,         // {
        RBRACE,         // }
        LPAREN,         // (
        RPAREN,         // )
        LBRACKET,       // [
        RBRACKET,       // ]

        COMMA,          // ,
        DOT,            // .
        SEMICOLON,      // ;
        COLON,          // :

        // ===== SPECIAL =====
        EOF
    }
}

class Token {
    final Lexer.TokenType type;
    final String value;

    Token(Lexer.TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type + (value != null ? " (" + value + ")" : "");
    }

    // ===== MINI LEXER W KLASIE TOKEN =====
    static List<Token> tokenize(String code) {
        List<Token> tokens = new ArrayList<>();

        String[] words = code.split("\\s+"); // dzieli po spacji (prosty start)
        for (String word : words) {
            switch (word) {
                // ===== KEYWORDS =====
                case "if" -> tokens.add(new Token(Lexer.TokenType.IF, null));
                case "else" -> tokens.add(new Token(Lexer.TokenType.ELSE, null));
                case "elseif" -> tokens.add(new Token(Lexer.TokenType.ELSEIF, null));
                case "while" -> tokens.add(new Token(Lexer.TokenType.WHILE, null));
                case "for" -> tokens.add(new Token(Lexer.TokenType.FOR, null));
                case "return" -> tokens.add(new Token(Lexer.TokenType.RETURN, null));
                case "class" -> tokens.add(new Token(Lexer.TokenType.CLASS, null));
                case "fn" -> tokens.add(new Token(Lexer.TokenType.FN, null));
                case "true" -> tokens.add(new Token(Lexer.TokenType.TRUE, null));
                case "false" -> tokens.add(new Token(Lexer.TokenType.FALSE, null));

                // ===== TYPES =====
                case "int32" -> tokens.add(new Token(Lexer.TokenType.INT32, null));
                case "int64" -> tokens.add(new Token(Lexer.TokenType.INT64, null));
                case "int128" -> tokens.add(new Token(Lexer.TokenType.INT128, null));
                case "long" -> tokens.add(new Token(Lexer.TokenType.LONG, null));
                case "double" -> tokens.add(new Token(Lexer.TokenType.DOUBLE, null));
                case "float" -> tokens.add(new Token(Lexer.TokenType.FLOAT, null));
                case "string" -> tokens.add(new Token(Lexer.TokenType.STRING_TYPE, null));
                case "bool" -> tokens.add(new Token(Lexer.TokenType.BOOL_TYPE, null));
                case "void" -> tokens.add(new Token(Lexer.TokenType.VOID, null));

                // ===== OPERATORS =====
                case "+" -> tokens.add(new Token(Lexer.TokenType.PLUS, null));
                case "-" -> tokens.add(new Token(Lexer.TokenType.MINUS, null));
                case "*" -> tokens.add(new Token(Lexer.TokenType.STAR, null));
                case "/" -> tokens.add(new Token(Lexer.TokenType.SLASH, null));
                case "%" -> tokens.add(new Token(Lexer.TokenType.MOD, null));
                case "=" -> tokens.add(new Token(Lexer.TokenType.EQUAL, null));
                case "==" -> tokens.add(new Token(Lexer.TokenType.EQUAL_EQUAL, null));
                case "!" -> tokens.add(new Token(Lexer.TokenType.BANG, null));
                case "!=" -> tokens.add(new Token(Lexer.TokenType.BANG_EQUAL, null));
                case ">" -> tokens.add(new Token(Lexer.TokenType.GREATER, null));
                case ">=" -> tokens.add(new Token(Lexer.TokenType.GREATER_EQUAL, null));
                case "<" -> tokens.add(new Token(Lexer.TokenType.LESS, null));
                case "<=" -> tokens.add(new Token(Lexer.TokenType.LESS_EQUAL, null));
                case "&&" -> tokens.add(new Token(Lexer.TokenType.AND, null));
                case "||" -> tokens.add(new Token(Lexer.TokenType.OR, null));

                // ===== STRUCTURE =====
                case "{" -> tokens.add(new Token(Lexer.TokenType.LBRACE, null));
                case "}" -> tokens.add(new Token(Lexer.TokenType.RBRACE, null));
                case "(" -> tokens.add(new Token(Lexer.TokenType.LPAREN, null));
                case ")" -> tokens.add(new Token(Lexer.TokenType.RPAREN, null));
                case "[" -> tokens.add(new Token(Lexer.TokenType.LBRACKET, null));
                case "]" -> tokens.add(new Token(Lexer.TokenType.RBRACKET, null));
                case "," -> tokens.add(new Token(Lexer.TokenType.COMMA, null));
                case "." -> tokens.add(new Token(Lexer.TokenType.DOT, null));
                case ";" -> tokens.add(new Token(Lexer.TokenType.SEMICOLON, null));
                case ":" -> tokens.add(new Token(Lexer.TokenType.COLON, null));

                // ===== LITERALS =====
                default -> {
                    if (word.matches("-?\\d+(\\.\\d+)?")) { // liczba całkowita lub zmiennoprzecinkowa
                        tokens.add(new Token(Lexer.TokenType.NUMBER, word));
                    } else if (word.startsWith("\"") && word.endsWith("\"")) { // string w cudzysłowie
                        tokens.add(new Token(Lexer.TokenType.STRING, word.substring(1, word.length() - 1)));
                    } else { // identyfikator
                        tokens.add(new Token(Lexer.TokenType.IDENTIFIER, word));
                    }
                }
            }
        }

        tokens.add(new Token(Lexer.TokenType.EOF, null)); // koniec kodu
        return tokens;
    }
}

// ===== KLASY AST (Abstract Syntax Tree) =====
abstract class ASTNode {}

// ===== WYRAŻENIA =====
abstract class Expression extends ASTNode {}

class NumberLiteral extends Expression {
    final String value;
    NumberLiteral(String value) { this.value = value; }
}

class StringLiteral extends Expression {
    final String value;
    StringLiteral(String value) { this.value = value; }
}

class BooleanLiteral extends Expression {
    final boolean value;
    BooleanLiteral(boolean value) { this.value = value; }
}

class Identifier extends Expression {
    final String name;
    Identifier(String name) { this.name = name; }
}

class BinaryOperation extends Expression {
    final Expression left;
    final Lexer.TokenType operator;
    final Expression right;

    BinaryOperation(Expression left, Lexer.TokenType operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
}

class UnaryOperation extends Expression {
    final Lexer.TokenType operator;
    final Expression operand;

    UnaryOperation(Lexer.TokenType operator, Expression operand) {
        this.operator = operator;
        this.operand = operand;
    }
}

class FunctionCall extends Expression {
    final String functionName;
    final List<Expression> arguments;

    FunctionCall(String functionName, List<Expression> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }
}

// ===== INSTRUKCJE =====
abstract class Statement extends ASTNode {}

class VariableDeclaration extends Statement {
    final Lexer.TokenType type;
    final String name;
    final Expression initializer;

    VariableDeclaration(Lexer.TokenType type, String name, Expression initializer) {
        this.type = type;
        this.name = name;
        this.initializer = initializer;
    }
}

class Assignment extends Statement {
    final String name;
    final Expression value;

    Assignment(String name, Expression value) {
        this.name = name;
        this.value = value;
    }
}

class IfStatement extends Statement {
    final Expression condition;
    final List<Statement> thenBranch;
    final List<Statement> elseBranch;

    IfStatement(Expression condition, List<Statement> thenBranch, List<Statement> elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
}

class WhileStatement extends Statement {
    final Expression condition;
    final List<Statement> body;

    WhileStatement(Expression condition, List<Statement> body) {
        this.condition = condition;
        this.body = body;
    }
}

class ForStatement extends Statement {
    final Statement initializer;
    final Expression condition;
    final Statement increment;
    final List<Statement> body;

    ForStatement(Statement initializer, Expression condition, Statement increment, List<Statement> body) {
        this.initializer = initializer;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
    }
}

class ReturnStatement extends Statement {
    final Expression value;
    ReturnStatement(Expression value) { this.value = value; }
}

class ExpressionStatement extends Statement {
    final Expression expression;
    ExpressionStatement(Expression expression) { this.expression = expression; }
}

class BlockStatement extends Statement {
    final List<Statement> statements;
    BlockStatement(List<Statement> statements) { this.statements = statements; }
}

class FunctionDeclaration extends Statement {
    final Lexer.TokenType returnType;
    final String name;
    final List<Parameter> parameters;
    final List<Statement> body;

    FunctionDeclaration(Lexer.TokenType returnType, String name, List<Parameter> parameters, List<Statement> body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }
}

class Parameter {
    final Lexer.TokenType type;
    final String name;

    Parameter(Lexer.TokenType type, String name) {
        this.type = type;
        this.name = name;
    }
}

class Program extends ASTNode {
    final List<Statement> statements;
    Program(List<Statement> statements) { this.statements = statements; }
}

// ===== PARSER =====
class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // ===== METODY POMOCNICZE =====
    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type == Lexer.TokenType.EOF;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean check(Lexer.TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean match(Lexer.TokenType... types) {
        for (Lexer.TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(Lexer.TokenType type, String message) {
        if (check(type)) return advance();
        throw new RuntimeException(message + " at " + peek());
    }

    // ===== PARSOWANIE PROGRAMU =====
    public Program parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return new Program(statements);
    }

    // ===== DEKLARACJE =====
    private Statement declaration() {
        try {
            if (match(Lexer.TokenType.FN)) return functionDeclaration();
            if (isTypeKeyword(peek().type)) return variableDeclaration();
            return statement();
        } catch (RuntimeException e) {
            synchronize();
            throw e;
        }
    }

    private boolean isTypeKeyword(Lexer.TokenType type) {
        return type == Lexer.TokenType.INT32 || type == Lexer.TokenType.INT64 ||
                type == Lexer.TokenType.INT128 || type == Lexer.TokenType.LONG ||
                type == Lexer.TokenType.DOUBLE || type == Lexer.TokenType.FLOAT ||
                type == Lexer.TokenType.STRING_TYPE || type == Lexer.TokenType.BOOL_TYPE ||
                type == Lexer.TokenType.VOID;
    }

    private FunctionDeclaration functionDeclaration() {
        Lexer.TokenType returnType = advance().type;
        Token name = consume(Lexer.TokenType.IDENTIFIER, "Expected function name");

        consume(Lexer.TokenType.LPAREN, "Expected '(' after function name");
        List<Parameter> parameters = new ArrayList<>();

        if (!check(Lexer.TokenType.RPAREN)) {
            do {
                Lexer.TokenType paramType = advance().type;
                Token paramName = consume(Lexer.TokenType.IDENTIFIER, "Expected parameter name");
                parameters.add(new Parameter(paramType, paramName.value));
            } while (match(Lexer.TokenType.COMMA));
        }

        consume(Lexer.TokenType.RPAREN, "Expected ')' after parameters");
        consume(Lexer.TokenType.LBRACE, "Expected '{' before function body");

        List<Statement> body = block();

        return new FunctionDeclaration(returnType, name.value, parameters, body);
    }

    private VariableDeclaration variableDeclaration() {
        Lexer.TokenType type = advance().type;
        Token name = consume(Lexer.TokenType.IDENTIFIER, "Expected variable name");

        Expression initializer = null;
        if (match(Lexer.TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(Lexer.TokenType.SEMICOLON, "Expected ';' after variable declaration");
        return new VariableDeclaration(type, name.value, initializer);
    }

    // ===== INSTRUKCJE =====
    private Statement statement() {
        if (match(Lexer.TokenType.IF)) return ifStatement();
        if (match(Lexer.TokenType.WHILE)) return whileStatement();
        if (match(Lexer.TokenType.FOR)) return forStatement();
        if (match(Lexer.TokenType.RETURN)) return returnStatement();
        if (match(Lexer.TokenType.LBRACE)) {
            List<Statement> statements = block();
            return new BlockStatement(statements);
        }

        return expressionStatement();
    }

    private IfStatement ifStatement() {
        consume(Lexer.TokenType.LPAREN, "Expected '(' after 'if'");
        Expression condition = expression();
        consume(Lexer.TokenType.RPAREN, "Expected ')' after condition");

        consume(Lexer.TokenType.LBRACE, "Expected '{' before if body");
        List<Statement> thenBranch = block();

        List<Statement> elseBranch = null;
        if (match(Lexer.TokenType.ELSE)) {
            consume(Lexer.TokenType.LBRACE, "Expected '{' before else body");
            elseBranch = block();
        }

        return new IfStatement(condition, thenBranch, elseBranch);
    }

    private WhileStatement whileStatement() {
        consume(Lexer.TokenType.LPAREN, "Expected '(' after 'while'");
        Expression condition = expression();
        consume(Lexer.TokenType.RPAREN, "Expected ')' after condition");

        consume(Lexer.TokenType.LBRACE, "Expected '{' before while body");
        List<Statement> body = block();

        return new WhileStatement(condition, body);
    }

    private ForStatement forStatement() {
        consume(Lexer.TokenType.LPAREN, "Expected '(' after 'for'");

        Statement initializer = null;
        if (!check(Lexer.TokenType.SEMICOLON)) {
            if (isTypeKeyword(peek().type)) {
                initializer = variableDeclaration();
            } else {
                initializer = expressionStatement();
            }
        } else {
            advance();
        }

        Expression condition = null;
        if (!check(Lexer.TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(Lexer.TokenType.SEMICOLON, "Expected ';' after loop condition");

        Statement increment = null;
        if (!check(Lexer.TokenType.RPAREN)) {
            Expression expr = expression();
            increment = new ExpressionStatement(expr);
        }
        consume(Lexer.TokenType.RPAREN, "Expected ')' after for clauses");

        consume(Lexer.TokenType.LBRACE, "Expected '{' before for body");
        List<Statement> body = block();

        return new ForStatement(initializer, condition, increment, body);
    }

    private ReturnStatement returnStatement() {
        Expression value = null;
        if (!check(Lexer.TokenType.SEMICOLON)) {
            value = expression();
        }
        consume(Lexer.TokenType.SEMICOLON, "Expected ';' after return value");
        return new ReturnStatement(value);
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();

        while (!check(Lexer.TokenType.RBRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(Lexer.TokenType.RBRACE, "Expected '}' after block");
        return statements;
    }

    private Statement expressionStatement() {
        Expression expr = expression();

        // Sprawdź czy to przypisanie
        if (match(Lexer.TokenType.EQUAL)) {
            if (expr instanceof Identifier) {
                Expression value = expression();
                consume(Lexer.TokenType.SEMICOLON, "Expected ';' after assignment");
                return new Assignment(((Identifier) expr).name, value);
            }
            throw new RuntimeException("Invalid assignment target");
        }

        consume(Lexer.TokenType.SEMICOLON, "Expected ';' after expression");
        return new ExpressionStatement(expr);
    }

    // ===== WYRAŻENIA =====
    private Expression expression() {
        return logicalOr();
    }

    private Expression logicalOr() {
        Expression expr = logicalAnd();

        while (match(Lexer.TokenType.OR)) {
            Lexer.TokenType operator = previous().type;
            Expression right = logicalAnd();
            expr = new BinaryOperation(expr, operator, right);
        }

        return expr;
    }

    private Expression logicalAnd() {
        Expression expr = equality();

        while (match(Lexer.TokenType.AND)) {
            Lexer.TokenType operator = previous().type;
            Expression right = equality();
            expr = new BinaryOperation(expr, operator, right);
        }

        return expr;
    }

    private Expression equality() {
        Expression expr = comparison();

        while (match(Lexer.TokenType.EQUAL_EQUAL, Lexer.TokenType.BANG_EQUAL)) {
            Lexer.TokenType operator = previous().type;
            Expression right = comparison();
            expr = new BinaryOperation(expr, operator, right);
        }

        return expr;
    }

    private Expression comparison() {
        Expression expr = term();

        while (match(Lexer.TokenType.GREATER, Lexer.TokenType.GREATER_EQUAL,
                Lexer.TokenType.LESS, Lexer.TokenType.LESS_EQUAL)) {
            Lexer.TokenType operator = previous().type;
            Expression right = term();
            expr = new BinaryOperation(expr, operator, right);
        }

        return expr;
    }

    private Expression term() {
        Expression expr = factor();

        while (match(Lexer.TokenType.PLUS, Lexer.TokenType.MINUS)) {
            Lexer.TokenType operator = previous().type;
            Expression right = factor();
            expr = new BinaryOperation(expr, operator, right);
        }

        return expr;
    }

    private Expression factor() {
        Expression expr = unary();

        while (match(Lexer.TokenType.STAR, Lexer.TokenType.SLASH, Lexer.TokenType.MOD)) {
            Lexer.TokenType operator = previous().type;
            Expression right = unary();
            expr = new BinaryOperation(expr, operator, right);
        }

        return expr;
    }

    private Expression unary() {
        if (match(Lexer.TokenType.BANG, Lexer.TokenType.MINUS)) {
            Lexer.TokenType operator = previous().type;
            Expression right = unary();
            return new UnaryOperation(operator, right);
        }

        return call();
    }

    private Expression call() {
        Expression expr = primary();

        if (match(Lexer.TokenType.LPAREN)) {
            if (expr instanceof Identifier) {
                List<Expression> arguments = new ArrayList<>();

                if (!check(Lexer.TokenType.RPAREN)) {
                    do {
                        arguments.add(expression());
                    } while (match(Lexer.TokenType.COMMA));
                }

                consume(Lexer.TokenType.RPAREN, "Expected ')' after arguments");
                return new FunctionCall(((Identifier) expr).name, arguments);
            }
        }

        return expr;
    }

    private Expression primary() {
        if (match(Lexer.TokenType.TRUE)) return new BooleanLiteral(true);
        if (match(Lexer.TokenType.FALSE)) return new BooleanLiteral(false);

        if (match(Lexer.TokenType.NUMBER)) {
            return new NumberLiteral(previous().value);
        }

        if (match(Lexer.TokenType.STRING)) {
            return new StringLiteral(previous().value);
        }

        if (match(Lexer.TokenType.IDENTIFIER)) {
            return new Identifier(previous().value);
        }

        if (match(Lexer.TokenType.LPAREN)) {
            Expression expr = expression();
            consume(Lexer.TokenType.RPAREN, "Expected ')' after expression");
            return expr;
        }

        throw new RuntimeException("Expected expression at " + peek());
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == Lexer.TokenType.SEMICOLON) return;

            switch (peek().type) {
                case CLASS, FN, IF, WHILE, FOR, RETURN -> {
                    return;
                }
            }

            advance();
        }
    }
}

class Interpreter {
    // TODO: Implementacja interpretera
}

