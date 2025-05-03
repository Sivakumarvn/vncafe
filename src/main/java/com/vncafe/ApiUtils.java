package com.vncafe;

import jakarta.servlet.http.HttpServletRequest;

import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;

public class ApiUtils {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vncafe";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "9080089287";
    private static ApiUtils instance;
    private ApiUtils() {};
    public static ApiUtils getInstance() {
        if (instance == null) {
            instance = new ApiUtils();
        }
        return new ApiUtils();
    }
    public Connection getConnection(String DB_URL,String DB_USER,String DB_PASSWORD) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    // Token generator method
    public String generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private boolean validateToken(String token) throws SQLException, ClassNotFoundException {
        Connection conn = getConnection(DB_URL,DB_USER,DB_PASSWORD);
        PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT AuthTokenCreatedAt FROM usercredentials WHERE AuthToken = ?"
        );
        checkStmt.setString(1, token);
        ResultSet rs = checkStmt.executeQuery();
//        System.out.println("IS ROW FOUND----> "+rs.next());
        if (rs.next()) {
            Timestamp tokenCreatedAt = rs.getTimestamp("AuthTokenCreatedAt");
            System.out.println("Token created at: " + tokenCreatedAt);
            Timestamp now = new Timestamp(System.currentTimeMillis());

            long differenceInMillis = now.getTime() - tokenCreatedAt.getTime();
            long differenceInHours = differenceInMillis / (1000 * 60 * 60);
            System.out.println("TIME DIFF-------> "+differenceInHours);
            if (differenceInHours > 24) {
                // Token expired
                return false;
            } else {
                // Token valid
                return true;
            }
        }
        return false;
    }
    public boolean verifyToken(HttpServletRequest request) throws SQLException, ClassNotFoundException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // remove "Bearer " prefix
//            System.out.println("Received token: " + token);
            return validateToken(token);
        } else {
            return false;
        }
    }
}
