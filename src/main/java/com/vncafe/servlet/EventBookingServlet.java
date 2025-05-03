package com.vncafe.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

public class EventBookingServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vncafe";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "9080089287";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            // Get form data
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String eventDate = request.getParameter("eventDate");
            String packageType = request.getParameter("package");
            int guests = Integer.parseInt(request.getParameter("guests"));
            String message = request.getParameter("message");

            // Validate required fields
            if (name == null || email == null || phone == null ||
                    eventDate == null || packageType == null || message == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"All fields are required\"}");
                return;
            }

            // Connect to database
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "INSERT INTO event_bookings (name, email, phone, event_date, package_type, guests, special_requirements) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    stmt.setString(2, email);
                    stmt.setString(3, phone);
                    stmt.setString(4, eventDate);
                    stmt.setString(5, packageType);
                    stmt.setInt(6, guests);
                    stmt.setString(7, message);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.println("{\"success\": true, \"message\": \"Booking submitted successfully!\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.println("{\"error\": \"Failed to save booking\"}");
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"MySQL JDBC driver not found\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Database connection error: \" + e.getMessage()}");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Invalid number format for guests\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"An unexpected error occurred\"}");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Handle GET requests
        response.setContentType("text/html");
        response.getWriter().println("Please use POST method to submit booking");
    }
}