package src;

import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Solver {
    private final Board board;
    private long iterationCount;
    private long startTime;
    private GridPane boardGrid;
    private long delayMs = 100;
    private final AtomicBoolean isSolving;
    private List<SolverListener> listeners;
    private boolean debugMode = true;
    private boolean showSteps = true;
    
    public interface SolverListener {
        void onIterationComplete(long iteration, String message);
        void onSolutionFound(boolean found, long iterations, long timeTaken);
    }
    
    public Solver(Board board, GridPane boardGrid) {
        this.board = board;
        this.boardGrid = boardGrid;
        this.iterationCount = 0;
        this.startTime = System.currentTimeMillis();
        this.isSolving = new AtomicBoolean(false);
        this.listeners = new ArrayList<>();
    }
    
    public void addListener(SolverListener listener) {
        listeners.add(listener);
    }
    
    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
    }
    
    public void setShowSteps(boolean enabled) {
        this.showSteps = enabled;
    }
    
    public boolean solve() {
        if (isSolving.get()) {
            return false;
        }
        
        isSolving.set(true);
        startTime = System.currentTimeMillis();
        iterationCount = 0;
        
        debugPrint("Starting solving process...");
        List<Piece> pieces = new ArrayList<>(board.getPieces());
        boolean result = tryAllPieces(pieces, 0);
        
        long timeTaken = System.currentTimeMillis() - startTime;
        notifyListeners(result, timeTaken);
        isSolving.set(false);
        
        return result;
    }
    
    public void stop() {
        isSolving.set(false);
    }
    
    private boolean tryAllPieces(List<Piece> pieces, int currentIndex) {
        if (!isSolving.get()) {
            return false;
        }
        
        if (currentIndex >= pieces.size()) {
            boolean complete = board.isComplete();
            debugPrint("Checking completion: " + complete);
            return complete;
        }
        
        Piece currentPiece = pieces.get(currentIndex);
        debugPrint("Trying piece " + currentPiece.getIdentifier() + " (index " + currentIndex + ")");
        
        List<Piece> variations = currentPiece.getAllOrientations();
        debugPrint("Generated " + variations.size() + " variations for piece " + currentPiece.getIdentifier());
        
        for (Piece variation : variations) {
            for (int row = 0; row < board.getRows(); row++) {
                for (int col = 0; col < board.getCols(); col++) {
                    iterationCount++;
                    
                    if (iterationCount % 1000 == 0) {
                        debugPrint(String.format("Iteration %d: Trying piece %c at (%d,%d)", 
                            iterationCount, variation.getIdentifier(), row, col));
                    }
                    
                    String message = String.format("Trying piece %c at position (%d,%d)", 
                        variation.getIdentifier(), row, col);
                    notifyIterationComplete(message);
                    
                    if (board.canPlacePiece(variation, row, col)) {
                        debugPrint(String.format("Placing piece %c at (%d,%d)", 
                            variation.getIdentifier(), row, col));
                        board.placePiece(variation, row, col);
                        
                        updateBoardDisplay();
                        
                        if (showSteps && boardGrid != null) {
                            try {
                                Thread.sleep(delayMs);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return false;
                            }
                        }
                        
                        if (tryAllPieces(pieces, currentIndex + 1)) {
                            return true;
                        }
                        
                        debugPrint(String.format("Backtracking: Removing piece %c from (%d,%d)", 
                            variation.getIdentifier(), row, col));
                        board.removePiece(variation, row, col);
                        updateBoardDisplay();
                    }
                }
            }
        }
        
        return false;
    }
    
    private void updateBoardDisplay() {
        if (boardGrid != null) {
            Platform.runLater(() -> {
                boardGrid.getChildren().clear();
                char[][] grid = board.getGrid();
                
                for (int i = 0; i < board.getRows(); i++) {
                    for (int j = 0; j < board.getCols(); j++) {
                        Rectangle cell = createCell(grid[i][j], i, j);
                        boardGrid.add(cell, j, i);
                    }
                }
            });
        }
    }
    
    private Rectangle createCell(char value, int row, int col) {
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
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PURPLE,
            Color.CYAN, Color.ORANGE, Color.PINK, Color.BROWN, Color.GRAY
        };
        return colors[index % colors.length];
    }
    
    private void debugPrint(String message) {
        if (debugMode) {
            System.out.println("[DEBUG] " + message);
        }
    }
    
    private void notifyListeners(boolean solutionFound, long timeTaken) {
        for (SolverListener listener : listeners) {
            listener.onSolutionFound(solutionFound, iterationCount, timeTaken);
        }
    }
    
    private void notifyIterationComplete(String message) {
        for (SolverListener listener : listeners) {
            listener.onIterationComplete(iterationCount, message);
        }
    }
    
    public long getIterationCount() {
        return iterationCount;
    }
    
    public void setDelay(long milliseconds) {
        this.delayMs = milliseconds;
    }
    
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    public boolean isRunning() {
        return isSolving.get();
    }
}