package src;

import java.io.*;
import java.util.*;

public class Board {
   private final int rows;
   private final int cols;
   private final char[][] grid;
   private final List<Piece> pieces;
   private String puzzleType;
   private static final Set<String> VALID_PUZZLE_TYPES = new HashSet<>(Arrays.asList("DEFAULT", "CUSTOM", "PYRAMID"));
   
   // ANSI color
   private static final String[] COLORS = {
       "\u001B[31m", // Red
       "\u001B[32m", // Green
       "\u001B[33m", // Yellow
       "\u001B[34m", // Blue
       "\u001B[35m", // Magenta
       "\u001B[36m", // Cyan
       "\u001B[91m", // Bright Red
       "\u001B[92m", // Bright Green
       "\u001B[93m", // Bright Yellow
       "\u001B[94m", // Bright Blue
       "\u001B[95m", // Bright Magenta
       "\u001B[96m", // Bright Cyan
       "\u001B[41m", // Red Background
       "\u001B[42m", // Green Background
       "\u001B[43m", // Yellow Background
       "\u001B[44m", // Blue Background
       "\u001B[45m", // Magenta Background
       "\u001B[46m", // Cyan Background
       "\u001B[101m", // Bright Red Background
       "\u001B[102m", // Bright Green Background
       "\u001B[103m", // Bright Yellow Background
       "\u001B[104m", // Bright Blue Background
       "\u001B[105m", // Bright Magenta Background
       "\u001B[106m", // Bright Cyan Background
       "\u001B[90m", // Dark Gray
       "\u001B[97m"  // White
   };
   private static final String RESET = "\u001B[0m";

   public Board(int rows, int cols) {
       if (rows <= 0 || cols <= 0) {
           throw new IllegalArgumentException("Board dimensions must be positive");
       }
       this.rows = rows;
       this.cols = cols;
       this.grid = new char[rows][cols];
       this.pieces = new ArrayList<>();
       this.puzzleType = "DEFAULT";
       initializeGrid();
   }

   private void initializeGrid() {
       for (int i = 0; i < rows; i++) {
           Arrays.fill(grid[i], '.');
       }
   }

   public static Board fromFile(File file) throws IOException {
       try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
           String firstLine = reader.readLine();
           if (firstLine == null) {
               throw new IOException("File is empty");
           }
           
           String[] dimensions = firstLine.trim().split("\\s+");
           if (dimensions.length != 3) {
               throw new IOException("First line must contain exactly 3 numbers (N M P)");
           }

           try {
               int rows = Integer.parseInt(dimensions[0]);
               int cols = Integer.parseInt(dimensions[1]);
               int expectedPieces = Integer.parseInt(dimensions[2]);

               if (expectedPieces > 26) {
                   throw new IOException("Number of pieces cannot exceed 26");
               }

               String puzzleType = reader.readLine();
               if (puzzleType == null) {
                   throw new IOException("Missing puzzle type");
               }
               puzzleType = puzzleType.trim();
               if (!VALID_PUZZLE_TYPES.contains(puzzleType)) {
                   throw new IOException("Invalid puzzle type: " + puzzleType);
               }

               Board board = new Board(rows, cols);
               board.puzzleType = puzzleType;

               Map<Character, List<String>> pieceGroups = new HashMap<>();
               String line;
               char currentIdentifier = ' ';
               List<String> currentPiece = new ArrayList<>();

               while ((line = reader.readLine()) != null) {
                   line = line.trim();
                   
                   if (line.isEmpty()) {
                       if (!currentPiece.isEmpty()) {
                           pieceGroups.put(currentIdentifier, currentPiece);
                           currentPiece = new ArrayList<>();
                       }
                       continue;
                   }

                   char identifier = ' ';
                   for (char c : line.toCharArray()) {
                       if (c != '.' && c != ' ') {
                           identifier = c;
                           break;
                       }
                   }

                   if (identifier == ' ') {
                       throw new IOException("Invalid line: no piece identifier found");
                   }

                   if (currentIdentifier != identifier) {
                       if (!currentPiece.isEmpty()) {
                           pieceGroups.put(currentIdentifier, currentPiece);
                           currentPiece = new ArrayList<>();
                       }
                       currentIdentifier = identifier;
                   }

                   currentPiece.add(line);
               }

               if (!currentPiece.isEmpty()) {
                   pieceGroups.put(currentIdentifier, currentPiece);
               }

               for (Map.Entry<Character, List<String>> entry : pieceGroups.entrySet()) {
                   board.pieces.add(new Piece(entry.getValue()));
               }

               if (board.pieces.size() != expectedPieces) {
                   throw new IOException(String.format("Expected %d pieces but found %d", 
                       expectedPieces, board.pieces.size()));
               }

               return board;

           } catch (NumberFormatException e) {
               throw new IOException("Invalid number format in dimensions");
           }
       }
   }

   public boolean placePiece(Piece piece, int row, int col) {
       if (!canPlacePiece(piece, row, col)) {
           return false;
       }

       char[][] shape = piece.getShape();
       for (int i = 0; i < shape.length; i++) {
           for (int j = 0; j < shape[0].length; j++) {
               if (shape[i][j] == piece.getIdentifier()) {
                   grid[row + i][col + j] = shape[i][j];
               }
           }
       }
       return true;
   }

   public void removePiece(Piece piece, int row, int col) {
       char[][] shape = piece.getShape();
       for (int i = 0; i < shape.length; i++) {
           for (int j = 0; j < shape[0].length; j++) {
               if (shape[i][j] == piece.getIdentifier()) {
                   grid[row + i][col + j] = '.';
               }
           }
       }
   }

   public boolean canPlacePiece(Piece piece, int row, int col) {
       char[][] shape = piece.getShape();
       if (row < 0 || col < 0 || row + shape.length > rows || col + shape[0].length > cols) {
           return false;
       }
       for (int i = 0; i < shape.length; i++) {
           for (int j = 0; j < shape[0].length; j++) {
               if (shape[i][j] == piece.getIdentifier() && grid[row + i][col + j] != '.') {
                   return false;
               }
           }
       }
       return true;
   }

   public boolean isComplete() {
       for (int i = 0; i < rows; i++) {
           for (int j = 0; j < cols; j++) {
               if (grid[i][j] == '.') {
                   return false;
               }
           }
       }
       return true;
   }

   public void print() {
       for (int i = 0; i < rows; i++) {
           for (int j = 0; j < cols; j++) {
               if (grid[i][j] != '.') {
                   int colorIndex = grid[i][j] - 'A';
                   System.out.print(COLORS[colorIndex % COLORS.length] + grid[i][j] + RESET);
               } else {
                   System.out.print(grid[i][j]);
               }
           }
           System.out.println();
       }
   }

   public void saveToFile(String filepath) throws IOException {
       try (PrintWriter writer = new PrintWriter(new FileWriter(filepath))) {
           for (int i = 0; i < rows; i++) {
               for (int j = 0; j < cols; j++) {
                   writer.print(grid[i][j]);
               }
               writer.println();
           }
       }
   }

   public int getRows() {
       return rows;
   }

   public int getCols() {
       return cols;
   }

   public char[][] getGrid() {
       return grid;
   }

   public List<Piece> getPieces() {
       return Collections.unmodifiableList(pieces);
   }

   public String getPuzzleType() {
       return puzzleType;
   }

   public void setPuzzleType(String type) {
       if (!VALID_PUZZLE_TYPES.contains(type)) {
           throw new IllegalArgumentException("Invalid puzzle type: " + type);
       }
       this.puzzleType = type;
   }
}
