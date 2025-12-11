import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import java.util.List;
/**
 * Główna klasa języka programowania
 * Obsługuje uruchamianie kodu z pliku oraz tryb interaktywny (REPL)
 */
public class CodeLanguage {
    private interpreter interpreter;
    private boolean verbose;

    public CodeLanguage() {
        this.interpreter = new interpreter();
        this.verbose = false;
    }

    public CodeLanguage(boolean verbose) {
        this.interpreter = new interpreter();
        this.verbose = verbose;
    }

    /**
     * Uruchamia kod z pliku
     */
    public void runFile(String filepath) {
        try {
            File file = new File(filepath);
            if (!file.exists()) {
                System.err.println("Error: File not found: " + filepath);
                System.exit(1);
            }

            String code = Files.readString(file.toPath());
            run(code);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }

    /**
     * Uruchamia tryb interaktywny (REPL)
     */
    public void runREPL() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== CodeLanguage REPL ===");
        System.out.println("Type 'exit' or 'quit' to exit");
        System.out.println("Type 'reset' to clear all variables");
        System.out.println("Type 'help' for help");
        System.out.println();

        while (true) {
            System.out.print(">>> ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine().trim();

            // Komendy specjalne
            if (line.equals("exit") || line.equals("quit")) {
                System.out.println("Goodbye!");
                break;
            }

            if (line.equals("reset")) {
                interpreter.reset();
                System.out.println("Environment reset.");
                continue;
            }

            if (line.equals("help")) {
                printHelp();
                continue;
            }

            if (line.isEmpty()) {
                continue;
            }

            // Obsługa wieloliniowych bloków kodu
            if (line.endsWith("{")) {
                StringBuilder multiline = new StringBuilder(line);
                multiline.append("\n");
                int braceCount = 1;

                while (braceCount > 0 && scanner.hasNextLine()) {
                    System.out.print("... ");
                    String nextLine = scanner.nextLine();
                    multiline.append(nextLine).append("\n");

                    for (char c : nextLine.toCharArray()) {
                        if (c == '{') braceCount++;
                        if (c == '}') braceCount--;
                    }
                }

                line = multiline.toString();
            }

            try {
                run(line);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                if (verbose) {
                    e.printStackTrace();
                }
            }
        }

        scanner.close();
    }

    /**
     * Wykonuje kod źródłowy
     */
    private void run(String code) {
        if (verbose) {
            System.out.println("=== Tokenizing ===");
        }

        // Tokenizacja
        List<Token> tokens = Token.tokenize(code);

        if (verbose) {
            System.out.println("Tokens:");
            for (Token token : tokens) {
                System.out.println("  " + token);
            }
            System.out.println();
        }

        if (verbose) {
            System.out.println("=== Parsing ===");
        }

        // Parsowanie
        Parser parser = new Parser(tokens);
        Program program = parser.parse();

        if (verbose) {
            System.out.println("AST created successfully");
            System.out.println();
            System.out.println("=== Interpreting ===");
        }

        // Interpretacja
        interpreter.interpret(program);

        if (verbose) {
            System.out.println();
            System.out.println("=== Execution completed ===");
        }
    }

    /**
     * Wyświetla pomoc
     */
    private void printHelp() {
        System.out.println("=== CodeLanguage Help ===");
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("  exit, quit  - Exit REPL");
        System.out.println("  reset       - Clear all variables and functions");
        System.out.println("  help        - Show this help");
        System.out.println();
        System.out.println("Example code:");
        System.out.println("  int32 x = 5;");
        System.out.println("  println(x);");
        System.out.println();
        System.out.println("  fn int32 add(int32 a, int32 b) {");
        System.out.println("    return a + b;");
        System.out.println("  }");
        System.out.println();
    }

    /**
     * Punkt wejścia programu
     */
    public static void main(String[] args) {
        boolean verbose = false;
        String filepath = null;

        // Parsowanie argumentów
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-v", "--verbose" -> verbose = true;
                case "-h", "--help" -> {
                    printUsage();
                    System.exit(0);
                }
                default -> {
                    if (filepath == null && !args[i].startsWith("-")) {
                        filepath = args[i];
                    } else {
                        System.err.println("Unknown option: " + args[i]);
                        printUsage();
                        System.exit(1);
                    }
                }
            }
        }

        CodeLanguage language = new CodeLanguage(verbose);

        if (filepath != null) {
            // Tryb pliku
            language.runFile(filepath);
        } else {
            // Tryb REPL
            language.runREPL();
        }
    }

    /**
     * Wyświetla informacje o użyciu
     */
    private static void printUsage() {
        System.out.println("Usage: java CodeLanguage [options] [file]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -v, --verbose    Enable verbose output");
        System.out.println("  -h, --help       Show this help message");
        System.out.println();
        System.out.println("If no file is specified, starts in REPL mode.");
    }
}