package src;
import java.util.*;

public class Piece {
    private char[][] shape;
    private final char identifier;
    private int rows;
    private int cols;
    
    public Piece(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Empty piece definition");
        }

        this.identifier = findIdentifier(lines);        
        List<String> normalizedLines = new ArrayList<>();
        for (String line : lines) {
            line = line.replace(' ', '.');
            normalizedLines.add(line);
        }
        
        int maxCols = 0;
        for (String line : normalizedLines) {
            maxCols = Math.max(maxCols, line.length());
        }
        
        this.rows = normalizedLines.size();
        this.cols = maxCols;
        this.shape = new char[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            Arrays.fill(shape[i], '.');
        }
        
        for (int i = 0; i < rows; i++) {
            String line = normalizedLines.get(i);
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) == identifier) {
                    shape[i][j] = identifier;
                }
            }
        }
        
        trimEmptySpace();
    }
    
    private char findIdentifier(List<String> lines) {
        for (String line : lines) {
            for (char c : line.toCharArray()) {
                if (c != '.' && c != ' ') {
                    return c;
                }
            }
        }
        throw new IllegalArgumentException("No identifier found in piece");
    }
    
    private void trimEmptySpace() {
        int minRow = rows, maxRow = -1;
        int minCol = cols, maxCol = -1;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (shape[i][j] == identifier) {
                    minRow = Math.min(minRow, i);
                    maxRow = Math.max(maxRow, i);
                    minCol = Math.min(minCol, j);
                    maxCol = Math.max(maxCol, j);
                }
            }
        }
        
        if (maxRow < minRow || maxCol < minCol) {
            throw new IllegalArgumentException("Invalid piece: no cells found");
        }
        rows = maxRow - minRow + 1;
        cols = maxCol - minCol + 1;
        char[][] trimmed = new char[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                trimmed[i][j] = shape[i + minRow][j + minCol];
            }
        }
        
        this.shape = trimmed;
    }
    
    public List<Piece> getAllOrientations() {
        Set<String> seen = new HashSet<>();
        List<Piece> orientations = new ArrayList<>();
        Piece current = this;        
        for (int rot = 0; rot < 4; rot++) {
            addUniqueOrientation(current, orientations, seen);
            Piece flipped = current.flip();
            addUniqueOrientation(flipped, orientations, seen);
            current = current.rotate();
        }
        
        return orientations;
    }
    
    private void addUniqueOrientation(Piece piece, List<Piece> orientations, Set<String> seen) {
        String key = piece.toString();
        if (!seen.contains(key)) {
            seen.add(key);
            orientations.add(piece);
        }
    }
    
    public Piece rotate() {
        char[][] rotated = new char[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                rotated[j][rows - 1 - i] = shape[i][j];
            }
        }
        return new Piece(Arrays.asList(shapeToStrings(rotated)));
    }
    
    public Piece flip() {
        char[][] flipped = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                flipped[i][cols - 1 - j] = shape[i][j];
            }
        }
        return new Piece(Arrays.asList(shapeToStrings(flipped)));
    }
    
    private String[] shapeToStrings(char[][] shape) {
        String[] result = new String[shape.length];
        for (int i = 0; i < shape.length; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < shape[i].length; j++) {
                sb.append(shape[i][j] == 0 ? identifier : shape[i][j]);
            }
            result[i] = sb.toString();
        }
        return result;
    }
    
    public char[][] getShape() {
        return shape;
    }
    
    public char getIdentifier() {
        return identifier;
    }
    
    public int getRows() {
        return rows;
    }
    
    public int getCols() {
        return cols;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sb.append(shape[i][j]);
            }
            if (i < rows - 1) sb.append('\n');
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return Arrays.deepEquals(shape, piece.shape);
    }
    
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(shape);
    }
}
