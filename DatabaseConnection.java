// package com.keyin.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String url = "jdbc:postgresql://localhost:5432/HealthApp";
    private static final String user = "postgres";
    private static final String password = "Keyin2021";
    private static boolean isConnected = false;

    public static Connection getCon() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static boolean isConnectedToHealthApp() {
        try (Connection con = getCon()) {
            if (con != null) {
                isConnected = true;
            } else {
                isConnected = false;
            }
        } catch (SQLException e) {
            isConnected = false;
            e.printStackTrace();
        }
        return isConnected;
    }

}
