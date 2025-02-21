package src;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("IQ Puzzler Pro Solver");
        System.out.print("Choose view mode (1: Terminal, 2: GUI): ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice == 1) {
            try {
                System.out.print("Enter file path: ");
                String filePath = scanner.nextLine().trim();
                File file = new File(filePath);
                
                if (!file.exists()) {
                    System.out.println("Error: File not found");
                    return;
                }
                
                long startTime = System.currentTimeMillis();
                Board board = Board.fromFile(file);
                Solver solver = new Solver(board, null);  
                System.out.println("\nSolving puzzle...");
                boolean solved = solver.solve();
                long endTime = System.currentTimeMillis();
                
                System.out.println("\nResults:");
                System.out.println("=========");
                if (solved) {
                    System.out.println("\nSolution found:");
                    board.print();
                } else {
                    System.out.println("No solution exists.");
                }
                
                System.out.println("\nTime taken: " + (endTime - startTime) + " ms");
                System.out.println("Iterations: " + solver.getIterationCount());
                
                System.out.print("\nDo you want to save the solution? (y/n): ");
                String save = scanner.nextLine().trim().toLowerCase();
                if (save.startsWith("y")) {
                    System.out.print("Enter output file path: ");
                    String outputPath = scanner.nextLine().trim();
                    board.saveToFile(outputPath);
                    System.out.println("Solution saved to: " + outputPath);
                }
                
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            
        } else if (choice == 2) {

            Application.launch(GUI.class, args);

        } else {
            System.out.println("Invalid choice!");
        }
        
        scanner.close();
    }
}
