/**
 * MainApp.java
 * 
 * PURPOSE:
 * Application entry point with clean interface.
 * 
 * CHANGES MADE:
 * - Removed all special characters
 * - Clean ASCII formatting
 */

package frontend;

import java.util.Scanner;

public class MainApp {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("     SMART BILLING SYSTEM");
        System.out.println("     Stationery Shop");
        System.out.println("=".repeat(50));
        System.out.println("1. Customer Mode");
        System.out.println("2. Admin Mode");
        System.out.println("3. Exit");
        System.out.println("-".repeat(50));
        System.out.print("Enter your choice: ");
        
        int choice;
        try {
            choice = scanner.nextInt();
        } catch (Exception e) {
            System.out.println("Invalid input!");
            scanner.close();
            return;
        }
        
        switch (choice) {
            case 1:
                CustomerUI customerUI = new CustomerUI();
                customerUI.start();
                break;
            case 2:
                AdminUI adminUI = new AdminUI();
                adminUI.start();
                break;
            case 3:
                System.out.println("\nGoodbye!");
                break;
            default:
                System.out.println("Invalid choice!");
        }
        
        scanner.close();
    }
}