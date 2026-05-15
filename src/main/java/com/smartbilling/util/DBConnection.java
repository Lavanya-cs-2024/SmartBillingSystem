package com.smartbilling.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    
    private static final String URL = DBConfig.URL;
    private static final String USERNAME = DBConfig.USERNAME;
    private static final String PASSWORD = DBConfig.PASSWORD;
    
    // Connection properties for better performance
    private static final Properties props = new Properties();
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Optimize connection properties
            props.setProperty("user", USERNAME);
            props.setProperty("password", PASSWORD);
            props.setProperty("useSSL", "false");
            props.setProperty("serverTimezone", "UTC");
            props.setProperty("autoReconnect", "true");
            props.setProperty("connectTimeout", "5000");      // 5 sec timeout
            props.setProperty("socketTimeout", "10000");      // 10 sec timeout
            props.setProperty("cachePrepStmts", "true");      // Cache prepared statements
            props.setProperty("useServerPrepStmts", "true");   // Server-side prepared statements
            props.setProperty("prepStmtCacheSize", "250");     // Cache size
            props.setProperty("prepStmtCacheSqlLimit", "2048");// SQL cache limit
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, props);
    }
}