package src;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.logging.*;
import java.util.ArrayList;
import java.util.List;

public class GUI extends Application implements Solver.SolverListener {
    private Board board;
    private GridPane boardGrid;
    private GridPane piecesPreviewGrid;
    private Label statusLabel;
    private Label timeLabel;
    private Label movesLabel;
    private Label iterationsLabel;
    private ToggleGroup modeGroup;
    private Slider speedSlider;
    private CheckBox debugModeCheckBox;
    private CheckBox showStepsCheckBox;
    private Button solveButton;
    private Button loadButton;
    private Button resetButton;
    private Button saveImageButton;
    private Button stopButton;
    private ProgressBar progressBar;
    private TextArea debugOutput;
    private double cellSize = 40;
    private List<Piece> availablePieces = new ArrayList<>();
    private StringProperty currentModeProperty = new SimpleStringProperty("DEFAULT");
    private static final Logger logger = Logger.getLogger(GUI.class.getName());
    private Solver currentSolver;

    @Override
    public void onSolutionFound(boolean found, long iterations, long timeTaken) {
        Platform.runLater(() -> {
            statusLabel.setText(found ? "Solution found!" : "No solution exists");
            timeLabel.setText("Time: " + timeTaken + " ms");
            iterationsLabel.setText("Iterations: " + iterations);
        });
    }

    @Override
    public void onIterationComplete(long iteration, String message) {
        Platform.runLater(() -> {
            iterationsLabel.setText("Iterations: " + iteration);
            debugOutput.appendText(message + "\n");
        });
    }

    private void updateBoardDisplay() {
        if (board == null || boardGrid == null) return;
        
        boardGrid.getChildren().clear();
        char[][] grid = board.getGrid();
        
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Rectangle cell = createBoardCell(grid[i][j], i, j);
                boardGrid.add(cell, j, i);
            }
        }
    }

    private Rectangle createBoardCell(char value, int row, int col) {
        Rectangle cell = new Rectangle(cellSize, cellSize);
        if (value == '.') {
            cell.setFill(Color.WHITE);
        } else {
            int colorIndex = value - 'A';
            cell.setFill(getColorForPiece(colorIndex));
        }
        cell.setStroke(Color.BLACK);
        cell.setStrokeWidth(0.5);
        return cell;
    }

    private Color getColorForPiece(int index) {
        Color[] colors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PURPLE,
            Color.CYAN, Color.ORANGE, Color.PINK, Color.BROWN, Color.GRAY
        };
        return colors[index % colors.length];
    }

    private void updatePiecesPreview() {
        if (piecesPreviewGrid == null || availablePieces == null) return;
        
        piecesPreviewGrid.getChildren().clear();
        int row = 0;
        for (Piece piece : availablePieces) {
            GridPane pieceGrid = createPiecePreview(piece);
            piecesPreviewGrid.add(pieceGrid, 0, row++);
        }
    }

    private GridPane createPiecePreview(Piece piece) {
        GridPane grid = new GridPane();
        grid.setHgap(1);
        grid.setVgap(1);
        
        char[][] shape = piece.getShape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[0].length; j++) {
                if (shape[i][j] == piece.getIdentifier()) {
                    Rectangle cell = new Rectangle(cellSize/2, cellSize/2);
                    cell.setFill(getColorForPiece(piece.getIdentifier() - 'A'));
                    cell.setStroke(Color.BLACK);
                    cell.setStrokeWidth(0.5);
                    grid.add(cell, j, i);
                }
            }
        }
        return grid;
    }

    private void resetStatistics() {
        timeLabel.setText("Time: 0 ms");
        iterationsLabel.setText("Iterations: 0");
        movesLabel.setText("Moves: 0");
        debugOutput.clear();
    }

    private void enableControls(boolean enable) {
        solveButton.setDisable(!enable);
        loadButton.setDisable(!enable);
        resetButton.setDisable(!enable);
        saveImageButton.setDisable(!enable);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void saveAsImage() {
        if (board == null) {
            showError("Error", "No solution to save");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PNG files", "*.png")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                WritableImage image = boardGrid.snapshot(null, null);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                statusLabel.setText("Image saved to: " + file.getName());
            } catch (Exception e) {
                showError("Error saving image", e.getMessage());
            }
        }
    }

    private void resetBoard() {
        if (board != null) {
            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getCols(); j++) {
                    board.getGrid()[i][j] = '.';
                }
            }
            updateBoardDisplay();
            resetStatistics();
        }
    }

    private void stopSolving() {
        if (currentSolver != null) {
            currentSolver.stop();
            statusLabel.setText("Solving stopped");
            enableControls(true);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        setupLogger();
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        VBox topSection = createTopSection();
        root.setTop(topSection);
        
        VBox controlSection = createControlSection();
        root.setLeft(controlSection);
        
        VBox boardSection = createBoardSection();
        root.setCenter(boardSection);
        
        VBox piecesSection = createPiecesSection();
        root.setRight(piecesSection);
        
        VBox bottomSection = createBottomSection();
        root.setBottom(bottomSection);
        
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("IQ Puzzler Pro Solver");
        primaryStage.setScene(scene);
        
        board = new Board(5, 5);
        updateBoardDisplay();
        
        primaryStage.show();
    }
    
    private VBox createTopSection() {
        VBox topSection = new VBox(10);
        topSection.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("IQ Puzzler Pro Solver");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        HBox modeSelection = createModeSelection();
        
        topSection.getChildren().addAll(titleLabel, modeSelection);
        return topSection;
    }
    
    private HBox createModeSelection() {
        HBox modeBox = new HBox(20);
        modeBox.setAlignment(Pos.CENTER);
        modeBox.setPadding(new Insets(10));
        
        modeGroup = new ToggleGroup();
        
        RadioButton defaultMode = new RadioButton("Default Mode");
        RadioButton customMode = new RadioButton("Custom Mode");
        RadioButton pyramidMode = new RadioButton("Pyramid Mode");
        
        defaultMode.setToggleGroup(modeGroup);
        customMode.setToggleGroup(modeGroup);
        pyramidMode.setToggleGroup(modeGroup);
        
        defaultMode.setSelected(true);
        
        modeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                RadioButton selected = (RadioButton) newVal;
                String mode = selected.getText().toUpperCase().replace(" MODE", "");
                currentModeProperty.set(mode);
                if (board != null) {
                    board.setPuzzleType(mode);
                }
                logger.info("Mode changed to: " + mode);
            }
        });
        
        modeBox.getChildren().addAll(defaultMode, customMode, pyramidMode);
        return modeBox;
    }
    
    private VBox createControlSection() {
        VBox controlSection = new VBox(10);
        controlSection.setPadding(new Insets(10));
        controlSection.setPrefWidth(250);
        
        // File controls
        TitledPane fileControls = createFileControlsGroup();
        
        // Solver controls
        TitledPane solverControls = createSolverControlsGroup();
        
        // Display settings
        TitledPane displaySettings = createDisplaySettingsGroup();
        
        // Debug settings
        TitledPane debugSettings = createDebugSettingsGroup();
        
        controlSection.getChildren().addAll(fileControls, solverControls, displaySettings, debugSettings);
        return controlSection;
    }
    
    private TitledPane createFileControlsGroup() {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10));
        
        loadButton = new Button("Load Puzzle");
        saveImageButton = new Button("Save as Image");
        resetButton = new Button("Reset Board");
        
        loadButton.setMaxWidth(Double.MAX_VALUE);
        saveImageButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setMaxWidth(Double.MAX_VALUE);
        
        loadButton.setOnAction(e -> loadPuzzle());
        saveImageButton.setOnAction(e -> saveAsImage());
        resetButton.setOnAction(e -> resetBoard());
        
        controls.getChildren().addAll(loadButton, saveImageButton, resetButton);
        
        TitledPane fileControls = new TitledPane("File Controls", controls);
        fileControls.setCollapsible(false);
        return fileControls;
    }
    
    private TitledPane createSolverControlsGroup() {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10));
        
        solveButton = new Button("Start Solving");
        stopButton = new Button("Stop Solving");
        stopButton.setDisable(true);
        
        Label speedLabel = new Label("Solving Speed:");
        speedSlider = new Slider(0, 1000, 100);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        
        solveButton.setMaxWidth(Double.MAX_VALUE);
        stopButton.setMaxWidth(Double.MAX_VALUE);
        
        solveButton.setOnAction(e -> startSolving());
        stopButton.setOnAction(e -> stopSolving());
        
        controls.getChildren().addAll(solveButton, stopButton, speedLabel, speedSlider);
        
        TitledPane solverControls = new TitledPane("Solver Controls", controls);
        solverControls.setCollapsible(false);
        return solverControls;
    }
    
    private TitledPane createDisplaySettingsGroup() {
        VBox settings = new VBox(10);
        settings.setPadding(new Insets(10));
        
        Label sizeLabel = new Label("Cell Size:");
        Slider sizeSlider = new Slider(20, 60, 40);
        sizeSlider.setShowTickLabels(true);
        sizeSlider.setShowTickMarks(true);
        
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            cellSize = newVal.doubleValue();
            updateBoardDisplay();
            updatePiecesPreview();
        });
        
        settings.getChildren().addAll(sizeLabel, sizeSlider);
        
        TitledPane displaySettings = new TitledPane("Display Settings", settings);
        displaySettings.setCollapsible(false);
        return displaySettings;
    }
    
    private TitledPane createDebugSettingsGroup() {
        VBox settings = new VBox(10);
        settings.setPadding(new Insets(10));
        
        debugModeCheckBox = new CheckBox("Debug Mode");
        showStepsCheckBox = new CheckBox("Show Steps");
        
        debugModeCheckBox.setSelected(true);
        showStepsCheckBox.setSelected(true);
        
        settings.getChildren().addAll(debugModeCheckBox, showStepsCheckBox);
        
        TitledPane debugSettings = new TitledPane("Debug Settings", settings);
        debugSettings.setCollapsible(false);
        return debugSettings;
    }
    
    private VBox createBoardSection() {
        VBox boardSection = new VBox(10);
        boardSection.setAlignment(Pos.CENTER);
        
        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(1);
        boardGrid.setVgap(1);
        boardGrid.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: gray; -fx-border-width: 1;");
        
        ScrollPane scrollPane = new ScrollPane(boardGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefViewportWidth(500);
        scrollPane.setPrefViewportHeight(500);
        
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        timeLabel = new Label("Time: 0 ms");
        iterationsLabel = new Label("Iterations: 0");
        movesLabel = new Label("Moves: 0");
        statsBox.getChildren().addAll(timeLabel, iterationsLabel, movesLabel);
        
        boardSection.getChildren().addAll(scrollPane, statsBox);
        return boardSection;
    }
    
    private VBox createPiecesSection() {
        VBox piecesSection = new VBox(10);
        piecesSection.setPadding(new Insets(10));
        piecesSection.setPrefWidth(200);
        
        Label piecesLabel = new Label("Available Pieces");
        piecesLabel.setStyle("-fx-font-weight: bold;");
        
        piecesPreviewGrid = new GridPane();
        piecesPreviewGrid.setAlignment(Pos.CENTER);
        piecesPreviewGrid.setHgap(10);
        piecesPreviewGrid.setVgap(10);
        
        ScrollPane scrollPane = new ScrollPane(piecesPreviewGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(400);
        
        piecesSection.getChildren().addAll(piecesLabel, scrollPane);
        return piecesSection;
    }
    
    private VBox createBottomSection() {
        VBox bottomSection = new VBox(10);
        bottomSection.setPadding(new Insets(10));
        
        HBox statusBar = new HBox(10);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel = new Label("Ready");
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        
        statusBar.getChildren().addAll(statusLabel, progressBar);
        
        debugOutput = new TextArea();
        debugOutput.setPrefRowCount(5);
        debugOutput.setEditable(false);
        debugOutput.setWrapText(true);
        
        bottomSection.getChildren().addAll(statusBar, debugOutput);
        return bottomSection;
    }
    
    private void loadPuzzle() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                board = Board.fromFile(file);
                board.setPuzzleType(currentModeProperty.get());
                availablePieces = new ArrayList<>(board.getPieces());
                updateBoardDisplay();
                updatePiecesPreview();
                resetStatistics();
                enableControls(true);
                statusLabel.setText("Puzzle loaded: " + file.getName());
                logger.info("Loaded puzzle from: " + file.getAbsolutePath());
            } catch (Exception e) {
                showError("Error loading puzzle", e.getMessage());
                logger.severe("Error loading puzzle: " + e.getMessage());
            }
        }
    }
    
    private void startSolving() {
        if (board == null) {
            showError("Error", "No puzzle loaded");
            return;
        }
        
        solveButton.setDisable(true);
        stopButton.setDisable(false);
        loadButton.setDisable(true);
        resetButton.setDisable(true);
        statusLabel.setText("Solving puzzle...");
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        
        currentSolver = new Solver(board, boardGrid);
        currentSolver.addListener(this);
        currentSolver.setDelay((long) speedSlider.getValue());
        currentSolver.setDebugMode(debugModeCheckBox.isSelected());
        currentSolver.setShowSteps(showStepsCheckBox.isSelected());
        
        Thread solverThread = new Thread(() -> {
            try {
                boolean solved = currentSolver.solve();
                Platform.runLater(() -> {
                    solveButton.setDisable(false);
                    stopButton.setDisable(true);
                    loadButton.setDisable(false);
                    resetButton.setDisable(false);
                    progressBar.setProgress(0);
                    statusLabel.setText(solved ? "Solution found!" : "No solution exists");
                    
                    Alert alert = new Alert(solved ? AlertType.INFORMATION : AlertType.WARNING);
                    alert.setTitle(solved ? "Solution Found" : "No Solution");
                    alert.setHeaderText(null);
                    alert.setContentText(String.format(
                        "%s\nTime taken: %d ms\nIterations: %d",
                        solved ? "Solution found!" : "No solution exists",
                        currentSolver.getElapsedTime(),
                        currentSolver.getIterationCount()
                    ));
                    alert.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error during solving", e.getMessage());
                    enableControls(true);
                    progressBar.setProgress(0);
                    statusLabel.setText("Error occurred");
                    
                    logger.severe("Error during solving: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
        
        solverThread.setDaemon(true);
        solverThread.start();
    }

    private void setupLogger() {
        try {
            FileHandler fh = new FileHandler("solver.log", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            System.err.println("Could not set up logger: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + " ms";
        }
        long seconds = milliseconds / 1000;
        milliseconds %= 1000;
        if (seconds < 60) {
            return String.format("%d.%03d s", seconds, milliseconds);
        }
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%d:%02d.%03d", minutes, seconds, milliseconds);
    }

    private void saveSolutionToFile() {
        if (board == null) {
            showError("Error", "No solution to save");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                board.saveToFile(file.getAbsolutePath());
                statusLabel.setText("Solution saved to: " + file.getName());
                logger.info("Saved solution to: " + file.getAbsolutePath());
            } catch (Exception e) {
                showError("Error saving solution", e.getMessage());
                logger.severe("Error saving solution: " + e.getMessage());
            }
        }
    }

    private void toggleFullScreen(Stage stage) {
        stage.setFullScreen(!stage.isFullScreen());
    }

    private void showAboutDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About IQ Puzzler Pro Solver");
        alert.setHeaderText("IQ Puzzler Pro Solver");
        alert.setContentText(
            "Version 1.0\n\n" +
            "A solver for IQ Puzzler Pro puzzles using brute force algorithm.\n\n" +
            "Created as part of IF2211 Strategi Algoritma\n" +
            "Institut Teknologi Bandung\n\n" +
            "© 2025"
        );
        alert.showAndWait();
    }

    private void showHelpDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("How to Use IQ Puzzler Pro Solver");
        alert.setContentText(
            "1. Load a puzzle file using the 'Load Puzzle' button\n" +
            "2. Select solving mode (Default/Custom/Pyramid)\n" +
            "3. Adjust solver settings if needed:\n" +
            "   - Solving speed\n" +
            "   - Debug mode\n" +
            "   - Show steps\n" +
            "4. Click 'Start Solving' to begin\n" +
            "5. Use 'Stop Solving' to halt the process\n" +
            "6. Save solution as image or text file\n\n" +
            "You can also manually place pieces by dragging them from the pieces panel."
        );
        alert.showAndWait();
    }

    @Override
    public void stop() {
        if (currentSolver != null && currentSolver.isRunning()) {
            currentSolver.stop();
        }
        // Close all file handlers
        for (Handler handler : logger.getHandlers()) {
            handler.close();
        }
    }

    public static void main(String[] args) {
        System.setProperty("javafx.preloader", "true");
        launch(args);
    }
}