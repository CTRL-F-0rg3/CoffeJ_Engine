import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class main_window extends Application {

    // Kolory motywu
    private static final String BG_DARK = "#1e1e1e";
    private static final String BG_DARKER = "#141414";
    private static final String BG_PANEL = "#252526";
    private static final String ACCENT_RED = "#e74856";
    private static final String ACCENT_RED_DARK = "#c93946";
    private static final String TEXT_PRIMARY = "#e0e0e0";
    private static final String TEXT_SECONDARY = "#9d9d9d";
    private static final String BORDER_COLOR = "#3e3e42";

    private CodeArea codeEditor;
    private TextArea consoleOutput;
    private TreeView<String> fileTree;
    private Label statusLabel;
    private TabPane editorTabs;
    private interpreter interpreter;
    private File currentFile;

    @Override
    public void start(Stage primaryStage) {
        interpreter = new interpreter();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        MenuBar menuBar = createMenuBar(primaryStage);
        ToolBar toolBar = createToolBar(primaryStage);
        VBox topContainer = new VBox(menuBar, toolBar);
        root.setTop(topContainer);

        VBox leftPanel = createLeftPanel();
        VBox centerPanel = createCenterPanel();
        VBox bottomPanel = createBottomPanel();

        SplitPane horizontalSplit = new SplitPane();
        horizontalSplit.setOrientation(Orientation.HORIZONTAL);
        horizontalSplit.getItems().addAll(leftPanel, centerPanel);
        horizontalSplit.setDividerPositions(0.2);

        SplitPane verticalSplit = new SplitPane();
        verticalSplit.setOrientation(Orientation.VERTICAL);
        verticalSplit.getItems().addAll(horizontalSplit, bottomPanel);
        verticalSplit.setDividerPositions(0.7);

        root.setCenter(verticalSplit);

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-background-color: " + ACCENT_RED_DARK + "; " +
                "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                "-fx-padding: 5px; " +
                "-fx-font-size: 12px;");
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 1400, 900);
        applySyntaxStyles(scene);

        primaryStage.setTitle("CodeLanguage Editor - Godot Style");
        primaryStage.setScene(scene);
        primaryStage.show();

        codeEditor.replaceText("""
                // Welcome to CodeLanguage Editor!
                
                int32 x = 10;
                int32 y = 20;
                
                fn int32 add(int32 a, int32 b) {
                    return a + b;
                }
                
                int32 result = add(x, y);
                println("Result: ");
                println(result);
                """);
    }

    private void applySyntaxStyles(Scene scene) {
        String css = """
            .keyword { -fx-fill: #ff6b9d; -fx-font-weight: bold; }
            .string { -fx-fill: #a5d6a7; }
            .comment { -fx-fill: #6a9955; -fx-font-style: italic; }
            .number { -fx-fill: #b5cea8; }
            .paren, .brace, .bracket { -fx-fill: #ffd700; -fx-font-weight: bold; }
            .semicolon { -fx-fill: #e0e0e0; }
            """;
        scene.getRoot().setStyle(css);
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: " + BG_DARKER + ";");

        Menu fileMenu = new Menu("File");
        MenuItem newFile = new MenuItem("New");
        newFile.setOnAction(e -> newFile());

        MenuItem openFile = new MenuItem("Open...");
        openFile.setOnAction(e -> openFile(stage));

        MenuItem saveFile = new MenuItem("Save");
        saveFile.setOnAction(e -> saveFile(stage));

        MenuItem saveAsFile = new MenuItem("Save As...");
        saveAsFile.setOnAction(e -> saveFileAs(stage));

        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> Platform.exit());

        fileMenu.getItems().addAll(newFile, openFile, saveFile, saveAsFile, new SeparatorMenuItem(), exit);

        Menu editMenu = new Menu("Edit");
        MenuItem undo = new MenuItem("Undo");
        undo.setOnAction(e -> codeEditor.undo());

        MenuItem redo = new MenuItem("Redo");
        redo.setOnAction(e -> codeEditor.redo());

        MenuItem cut = new MenuItem("Cut");
        cut.setOnAction(e -> codeEditor.cut());

        MenuItem copy = new MenuItem("Copy");
        copy.setOnAction(e -> codeEditor.copy());

        MenuItem paste = new MenuItem("Paste");
        paste.setOnAction(e -> codeEditor.paste());

        editMenu.getItems().addAll(undo, redo, new SeparatorMenuItem(), cut, copy, paste);

        Menu runMenu = new Menu("Run");
        MenuItem runCode = new MenuItem("Run (F5)");
        runCode.setOnAction(e -> runCode());

        MenuItem stopCode = new MenuItem("Stop");
        stopCode.setOnAction(e -> stopCode());

        MenuItem clearConsole = new MenuItem("Clear Console");
        clearConsole.setOnAction(e -> clearConsole());

        runMenu.getItems().addAll(runCode, stopCode, new SeparatorMenuItem(), clearConsole);

        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> showAbout());

        MenuItem docs = new MenuItem("Documentation");
        docs.setOnAction(e -> showDocs());

        helpMenu.getItems().addAll(docs, about);

        menuBar.getMenus().addAll(fileMenu, editMenu, runMenu, helpMenu);

        for (Menu menu : menuBar.getMenus()) {
            menu.setStyle("-fx-text-fill: " + TEXT_PRIMARY + ";");
        }

        return menuBar;
    }

    private ToolBar createToolBar(Stage stage) {
        ToolBar toolBar = new ToolBar();
        toolBar.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-border-color: " + BORDER_COLOR + ";");

        Button newBtn = createToolButton("📄", "New File");
        newBtn.setOnAction(e -> newFile());

        Button openBtn = createToolButton("📂", "Open File");
        openBtn.setOnAction(e -> openFile(stage));

        Button saveBtn = createToolButton("💾", "Save File");
        saveBtn.setOnAction(e -> saveFile(stage));

        Separator sep1 = new Separator(Orientation.VERTICAL);

        Button runBtn = createToolButton("▶", "Run Code");
        runBtn.setStyle("-fx-background-color: " + ACCENT_RED + "; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8px 15px;");
        runBtn.setOnAction(e -> runCode());

        Button stopBtn = createToolButton("⏹", "Stop");
        stopBtn.setOnAction(e -> stopCode());

        Separator sep2 = new Separator(Orientation.VERTICAL);

        Button clearBtn = createToolButton("🗑", "Clear Console");
        clearBtn.setOnAction(e -> clearConsole());

        toolBar.getItems().addAll(newBtn, openBtn, saveBtn, sep1, runBtn, stopBtn, sep2, clearBtn);

        return toolBar;
    }

    private Button createToolButton(String text, String tooltip) {
        Button btn = new Button(text);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle("-fx-background-color: " + BG_DARK + "; " +
                "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 5px 10px;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + ACCENT_RED_DARK + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 5px 10px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + BG_DARK + "; " +
                "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 5px 10px;"));
        return btn;
    }

    private VBox createLeftPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-border-color: " + BORDER_COLOR + ";");
        panel.setPrefWidth(250);

        Label title = new Label("📁 Project Files");
        title.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-font-weight: bold;");

        fileTree = new TreeView<>();
        TreeItem<String> rootItem = new TreeItem<>("Project");
        rootItem.setExpanded(true);

        TreeItem<String> exampleFile = new TreeItem<>("example.cl");
        TreeItem<String> mainFile = new TreeItem<>("main.cl");
        rootItem.getChildren().addAll(exampleFile, mainFile);

        fileTree.setRoot(rootItem);
        fileTree.setStyle("-fx-background-color: " + BG_DARK + "; " +
                "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                "-fx-border-color: " + BORDER_COLOR + ";");

        VBox.setVgrow(fileTree, Priority.ALWAYS);
        panel.getChildren().addAll(title, fileTree);

        return panel;
    }

    private VBox createCenterPanel() {
        VBox panel = new VBox(5);
        panel.setStyle("-fx-background-color: " + BG_DARK + ";");

        editorTabs = new TabPane();
        editorTabs.setStyle("-fx-background-color: " + BG_PANEL + ";");

        Tab mainTab = new Tab("Untitled");
        mainTab.setClosable(false);

        codeEditor = new CodeArea();
        codeEditor.setParagraphGraphicFactory(LineNumberFactory.get(codeEditor));
        codeEditor.setStyle("-fx-background-color: " + BG_DARK + "; " +
                "-fx-control-inner-background: " + BG_DARK + "; " +
                "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                "-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                "-fx-font-size: 14px;");

        codeEditor.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .subscribe(ignore -> codeEditor.setStyleSpans(0, computeHighlighting(codeEditor.getText())));

        mainTab.setContent(codeEditor);
        editorTabs.getTabs().add(mainTab);

        VBox.setVgrow(editorTabs, Priority.ALWAYS);
        panel.getChildren().add(editorTabs);

        return panel;
    }

    private VBox createBottomPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(5));
        panel.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-border-color: " + BORDER_COLOR + ";");
        panel.setPrefHeight(200);

        Label consoleTitle = new Label("📟 Console Output");
        consoleTitle.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-font-weight: bold;");

        consoleOutput = new TextArea();
        consoleOutput.setEditable(false);
        consoleOutput.setStyle("-fx-control-inner-background: " + BG_DARKER + "; " +
                "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                "-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                "-fx-font-size: 13px;");
        consoleOutput.setText("Console ready...\n");

        VBox.setVgrow(consoleOutput, Priority.ALWAYS);
        panel.getChildren().addAll(consoleTitle, consoleOutput);

        return panel;
    }

    private static final String[] KEYWORDS = new String[] {
            "if", "else", "elseif", "while", "for", "return", "class", "fn",
            "int32", "int64", "int128", "long", "double", "float", "string", "bool", "void",
            "true", "false"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "[(){}\\[\\]]";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String NUMBER_PATTERN = "\\b\\d+(\\.\\d+)?\\b";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
    );

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("STRING") != null ? "string" :
                                            matcher.group("COMMENT") != null ? "comment" :
                                                    matcher.group("NUMBER") != null ? "number" :
                                                            null;

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private void newFile() {
        codeEditor.clear();
        currentFile = null;
        updateStatus("New file created");
    }

    private void openFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CodeLanguage Files", "*.cl")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                String content = Files.readString(file.toPath());
                codeEditor.replaceText(content);
                currentFile = file;
                updateStatus("Opened: " + file.getName());
            } catch (IOException e) {
                showError("Error opening file: " + e.getMessage());
            }
        }
    }

    private void saveFile(Stage stage) {
        if (currentFile != null) {
            try {
                Files.writeString(currentFile.toPath(), codeEditor.getText());
                updateStatus("Saved: " + currentFile.getName());
            } catch (IOException e) {
                showError("Error saving file: " + e.getMessage());
            }
        } else {
            saveFileAs(stage);
        }
    }

    private void saveFileAs(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File As");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CodeLanguage Files", "*.cl")
        );

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                Files.writeString(file.toPath(), codeEditor.getText());
                currentFile = file;
                updateStatus("Saved as: " + file.getName());
            } catch (IOException e) {
                showError("Error saving file: " + e.getMessage());
            }
        }
    }

    private void runCode() {
        consoleOutput.clear();
        consoleOutput.appendText("=== Running Code ===\n\n");
        updateStatus("Running...");

        PrintStream ps = new PrintStream(new ConsoleOutputStream());
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        System.setOut(ps);
        System.setErr(ps);

        try {
            String code = codeEditor.getText();
            interpreter.reset();
            interpreter.executeCode(code);
            consoleOutput.appendText("\n=== Execution completed successfully ===\n");
            updateStatus("Execution completed");
        } catch (Exception e) {
            consoleOutput.appendText("\n[ERROR] " + e.getMessage() + "\n");
            updateStatus("Error: " + e.getMessage());
        } finally {
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
    }

    private void stopCode() {
        updateStatus("Stopped");
        consoleOutput.appendText("\n=== Execution stopped ===\n");
    }

    private void clearConsole() {
        consoleOutput.clear();
        updateStatus("Console cleared");
    }

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("CodeLanguage Editor");
        alert.setContentText("A modern code editor with Godot-inspired theme\n\nVersion 1.0\n© 2024");
        styleDialog(alert);
        alert.showAndWait();
    }

    private void showDocs() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Documentation");
        alert.setHeaderText("Quick Reference");
        alert.setContentText("""
                Keywords: if, else, while, for, return, fn
                Types: int32, int64, float, double, bool, string
                
                Example:
                fn int32 add(int32 a, int32 b) {
                    return a + b;
                }
                """);
        styleDialog(alert);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }

    private void styleDialog(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + BG_PANEL + ";");
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private class ConsoleOutputStream extends OutputStream {
        @Override
        public void write(int b) {
            Platform.runLater(() -> consoleOutput.appendText(String.valueOf((char) b)));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}