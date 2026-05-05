/**
 * DBConfig_TEMPLATE.java
 * 
 * PURPOSE:
 * Template for database configuration.
 * Each team member creates their own DBConfig.java from this template.
 * 
 * INSTRUCTIONS FOR TEAM MEMBERS:
 * 1. Copy this file and rename to DBConfig.java
 * 2. Update PASSWORD with your MySQL password
 * 3. DO NOT commit DBConfig.java to git (it's in .gitignore)
 * 
 * WHY THIS IS NEEDED:
 * - Everyone has different MySQL passwords
 * - Prevents password from being exposed on GitHub
 * - Makes project portable for any computer
 */

package backend.util;

public class DBConfig_TEMPLATE {
    
    // Database connection settings
    // Change "localhost" if MySQL is on a different machine
    public static final String URL = "jdbc:mysql://localhost:3306/stationery_billing";
    
    // MySQL username (usually "root" for local installation)
    public static final String USERNAME = "root";
    
    // ⚠️ IMPORTANT: Change this to YOUR MySQL password!
    // If you don't have a password, leave it as empty string: ""
    public static final String PASSWORD = "YOUR_PASSWORD_HERE";
    
    // Optional: For advanced users who want to use environment variables
    // public static final String PASSWORD = System.getenv("MYSQL_PASSWORD");
}