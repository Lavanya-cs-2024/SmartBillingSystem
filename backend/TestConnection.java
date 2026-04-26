package backend;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/stationery_billing";
        String user = "root";
        String password = "Lavanya*9106*"; // ← change this

        try {
            Connection con = DriverManager.getConnection(url, user, password);
            if (con != null) {
                System.out.println("Connected successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}