package org.gwanhee.groupproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {
    private static final String URL = "jdbc:sqlite:C:/Users/zxcvb/OneDrive/database_Example.db";

    public static Connection getConnection() throws SQLException {
        System.out.println("Connecting to database: " + URL);
        return DriverManager.getConnection(URL);
    }
}