package src;

import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Solver {
    private final Board board;
    private final GridPane boardGrid;
    private long iterationCount;
    private long startTime;
    private long delayMs = 0;
    private final AtomicBoolean isSolving;
    private List<SolverListener> listeners;
    private boolean debugMode = false;
    private boolean showSteps = false;
    private char[][] targetConfig;
    private Set<Character> usedPieces;
    private int targetCells = 0;

    public interface SolverListener {
        void onIterationComplete(long iteration, String message);
        void onSolutionFound(boolean found, long iterations, long timeTaken);
    }

    public Solver(Board board) {
        this(board, null);
    }

    public Solver(Board board, GridPane boardGrid) {
        this.board = board;
        this.boardGrid = boardGrid;
        this.iterationCount = 0;
        this.startTime = 0;
        this.isSolving = new AtomicBoolean(false);
        this.listeners = new ArrayList<>();
        this.usedPieces = new HashSet<>();
        
        if ("CUSTOM".equals(board.getPuzzleType())) {
            this.targetConfig = board.getTargetConfig();
            // Count target cells
            if (targetConfig != null) {
                for (int i = 0; i < board.getRows(); i++) {
                    for (int j = 0; j < board.getCols(); j++) {
                        if (targetConfig[i][j] == 'X') {
                            targetCells++;
                        }
                    }
                }
                System.out.println("Target cells count: " + targetCells);
            }
        }
    }

    public boolean solve() {
        if (isSolving.get()) return false;

        isSolving.set(true);
        startTime = System.currentTimeMillis();
        iterationCount = 0;
        usedPieces.clear();
        List<Piece> pieces = new ArrayList<>(board.getPieces());
        boolean result = tryAllPieces(pieces, 0);
        long timeTaken = System.currentTimeMillis() - startTime;
        notifyListeners(result, iterationCount, timeTaken);
        isSolving.set(false);

        return result;
    }

    private void notifyListeners(boolean solutionFound, long iterations, long timeTaken) {
        for (SolverListener listener : listeners) {
            listener.onSolutionFound(solutionFound, iterations, timeTaken);
        }
    }

    private boolean tryAllPieces(List<Piece> pieces, int currentIndex) {
        if (!isSolving.get()) return false;
        if (currentIndex >= pieces.size()) {
            return isComplete();
        }
        Piece currentPiece = pieces.get(currentIndex);
        if (usedPieces.contains(currentPiece.getIdentifier())) {
            return tryAllPieces(pieces, currentIndex + 1);
        }

        List<int[]> positions = getAllPositions();
        List<Piece> orientations = currentPiece.getAllOrientations();

        for (int[] pos : positions) {
            int row = pos[0];
            int col = pos[1];
            
            for (Piece orientation : orientations) {
                iterationCount++;
                
                if (iterationCount % 100000 == 0 && debugMode) {
                    debugPrint("Iteration " + iterationCount + ": Trying piece " + orientation.getIdentifier() + " at (" + row + "," + col + ")");
                }

                if (canPlacePiece(orientation, row, col)) {
                    board.placePiece(orientation, row, col);
                    usedPieces.add(orientation.getIdentifier());
                    
                    if (showSteps) {
                        updateBoardDisplay();
                    }
                    
                    if (tryAllPieces(pieces, currentIndex + 1)) {
                        return true;
                    }

                    board.removePiece(orientation, row, col);
                    usedPieces.remove(orientation.getIdentifier());
                }

                if (!isSolving.get()) return false;
            }
        }

        return false;
    }

    private void debugPrint(String message) {
        if (debugMode) {
            System.out.println("[DEBUG] " + message);
            for (SolverListener listener : listeners) {
                listener.onIterationComplete(iterationCount, message);
            }
        }
    }

    private List<int[]> getAllPositions() {
        List<int[]> positions = new ArrayList<>();
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                positions.add(new int[]{i, j});
            }
        }
        return positions;
    }

    // cek apakah piece dapat ditempatkan pada posisi (row, col)
    private boolean canPlacePiece(Piece piece, int row, int col) {
        // cek apakah piece dapat ditempatkan secara tidak tumpang tindih atau keluar batas
        if (!board.canPlacePiece(piece, row, col)) {
            return false;
        }

        // kasus CUSTOM, periksa apakah piece menutupi setidaknya satu target
        if ("CUSTOM".equals(board.getPuzzleType()) && targetConfig != null) {
            char[][] shape = piece.getShape();
            boolean coversTarget = false;
            
            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[0].length; j++) {
                    if (shape[i][j] == piece.getIdentifier()) {
                        int boardRow = row + i;
                        int boardCol = col + j;
                        if (isValidPosition(boardRow, boardCol)) {
                            if (targetConfig[boardRow][boardCol] == 'X') {
                                coversTarget = true;
                            } else if (targetConfig[boardRow][boardCol] == '.') {
                                // piece mengenai non-target ini tidak valid untuk kasus CUSTOM
                                return false;
                            }
                        }
                    }
                }
            }
            return coversTarget;
        }
        
        return true;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < board.getRows() && col >= 0 && col < board.getCols();
    }

    private boolean isComplete() {
        if ("CUSTOM".equals(board.getPuzzleType()) && targetConfig != null) {
            char[][] grid = board.getGrid();            
            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getCols(); j++) {
                    if (targetConfig[i][j] == 'X' && grid[i][j] == '.') {
                        return false;
                    }
                }
            }
            
            if (debugMode) {
                System.out.println("Board state at completion check:");
                board.print();
            }
            
            return true;
        }
        return board.isComplete();
    }

    private void updateBoardDisplay() {
        if (boardGrid == null || !showSteps) return;
        
        Platform.runLater(() -> {
            boardGrid.getChildren().clear();
            char[][] grid = board.getGrid();
            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getCols(); j++) {
                    Rectangle cell = createCell(grid[i][j]);
                    boardGrid.add(cell, j, i);
                }
            }
        });

        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Rectangle createCell(char value) {
        Rectangle cell = new Rectangle(40, 40);
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
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
            Color.PURPLE, Color.CYAN, Color.ORANGE, Color.PINK,
            Color.BROWN, Color.GRAY
        };
        return colors[index % colors.length];
    }

    public void addListener(SolverListener listener) {
        listeners.add(listener);
    }

    public void stop() {
        isSolving.set(false);
    }

    public long getIterationCount() {
        return iterationCount;
    }

    public void setDelay(long milliseconds) {
        this.delayMs = milliseconds;
    }

    public void setShowSteps(boolean enabled) {
        this.showSteps = enabled;
    }

    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    public boolean isRunning() {
        return isSolving.get();
    }
}
