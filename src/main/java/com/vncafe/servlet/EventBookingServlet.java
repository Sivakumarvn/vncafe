package com.vncafe.servlet;

import com.vncafe.ApiUtils;
import com.vncafe.constants.ResponseStatusCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

public class EventBookingServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vncafe";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "9080089287";
    private ApiUtils apiUtils = ApiUtils.getInstance();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject responseJson = new JSONObject();
        JSONObject responseStatus = new JSONObject();
        boolean isValidToken;
        try {
            isValidToken = apiUtils.verifyToken(request);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (isValidToken) {
            JSONObject input_data = new JSONObject(request.getParameter("input_data"));
            int userId = input_data.optInt("user_id");
            if (userId == 0) {
                responseStatus.put("status", "failed");
                responseStatus.put("message", "Missing user_id");
                responseStatus.put("status_code", ResponseStatusCode.MANDATORY_FIELD_MISSING);
                responseJson.put("response_status", responseStatus);
                response.getWriter().write(responseJson.toString());
                return;
            }

            try (Connection conn = apiUtils.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Check if user exists
                PreparedStatement userStmt = conn.prepareStatement("SELECT * FROM usercredentials WHERE UserId = ?");
                userStmt.setInt(1, userId);
                ResultSet userRs = userStmt.executeQuery();

                if (!userRs.next()) {
                    responseStatus.put("status", "failed");
                    responseStatus.put("message", "User not found");
                    responseStatus.put("status_code", ResponseStatusCode.UNAUTHORIZED_USER);
                    responseJson.put("response_status", responseStatus);
                    response.getWriter().write(responseJson.toString());
                    return;
                }
                if (request.getRequestURI().contains("checkavailability")) {
                    String eventDate = input_data.getString("event_date");
                    int packageId = input_data.getInt("package_id");
                    int vacancy = input_data.getInt("vacancy");
                    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM packagebooking WHERE package_id = ? and event_date = ?");
                    stmt.setInt(1, packageId);
                    stmt.setString(2, eventDate);
                    ResultSet rs = stmt.executeQuery();
                    int rowCount = 0;
                    while (rs.next()) {
                        rowCount++;
                    }
                    if(rowCount>=vacancy){
                        responseJson.put("isavaiable", false);
                    }else {
                        responseJson.put("isavaiable", true);
                    }
                    response.getWriter().write(responseJson.toString());
                    return;
                } else {
                    PreparedStatement bookingStmt = conn.prepareStatement("SELECT * FROM packagebooking WHERE user_id = ? ORDER BY event_date DESC");
                    bookingStmt.setInt(1, userId);
                    ResultSet bookingRs = bookingStmt.executeQuery();
                    JSONArray bookingArray = new JSONArray();
                    while (bookingRs.next()) {
                        int bookingId = bookingRs.getInt("id");
                        Timestamp eventDate = bookingRs.getTimestamp("event_date");
                        JSONObject packageJson = new JSONObject();
                        PreparedStatement packageStmt = conn.prepareStatement("SELECT * FROM eventpackage WHERE id = ?");
                        packageStmt.setInt(1, bookingRs.getInt("package_id"));
                        ResultSet packageRs = packageStmt.executeQuery();
                        while (packageRs.next()) {
                            packageJson.put("id", packageRs.getInt("id"));
                            packageJson.put("name", packageRs.getString("name"));
                            packageJson.put("price", packageRs.getBigDecimal("price"));
                            packageJson.put("category", packageRs.getString("category"));
                        }
                        JSONObject bookedPackageJson = new JSONObject();
                        bookedPackageJson.put("booking_id", bookingId);
                        bookedPackageJson.put("event_date", eventDate.toString());
                        bookedPackageJson.put("package", packageJson);
                        bookingArray.put(bookedPackageJson);
                    }
                    responseStatus.put("status", "success");
                    responseStatus.put("status_code", ResponseStatusCode.OK);
                    responseJson.put("booked_packages", bookingArray);
                }
            } catch (Exception e) {
                e.printStackTrace();
                responseStatus.put("status", "failed");
                responseStatus.put("message", e.getMessage());
                responseStatus.put("status_code", ResponseStatusCode.INTERNAL_ERROR);
                responseJson.put("response_status", responseStatus);
                response.getWriter().write(responseJson.toString());
            }
        }
        else{
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseStatus.put("status", "failed");
            responseStatus.put("message", "Invalid token! Please Login.");
            responseStatus.put("status_code", ResponseStatusCode.UNAUTHORIZED_USER);
        }
        response.setContentType("application/json");
        responseJson.put("response_status", responseStatus);
        response.getWriter().write(responseJson.toString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject responseJson = new JSONObject();
        JSONObject responseStatus = new JSONObject();
        boolean isValidToken;
        try {
            isValidToken = apiUtils.verifyToken(request);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (isValidToken) {
            JSONObject input_data = new JSONObject(request.getParameter("input_data"));
            int userId = input_data.optInt("user_id");
            try (Connection conn = apiUtils.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                if (userId == 0) {
                    responseStatus.put("status", "failed");
                    responseStatus.put("message", "User Not Found");
                    responseStatus.put("status_code", ResponseStatusCode.MANDATORY_FIELD_MISSING);
                    responseJson.put("response_status", responseStatus);
                    response.getWriter().write(responseJson.toString());
                    return;
                }
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM usercredentials WHERE UserID = ?");
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    JSONObject eventBooking = input_data.getJSONObject("event_booking");
                    int packageId = eventBooking.optInt("package_id");
                    if (packageId>0) {
                        String eventDate = eventBooking.optString("event_date");
                        String sr = eventBooking.optString("special_requirement");
                        stmt = conn.prepareStatement("INSERT INTO packagebooking(user_id,package_id,event_date,special_requirement) VALUES (?,?,?,?)");
                        stmt.setInt(1, userId);
                        stmt.setInt(2, packageId);
                        stmt.setString(3, eventDate);
                        stmt.setString(4, sr);
                        int row = stmt.executeUpdate();
                        if (row > 0) {
                            responseStatus.put("status", "success");
                            responseStatus.put("status_code", ResponseStatusCode.OK);
                        }
                    }
                    else{
                        responseStatus.put("status", "failed");
                        responseStatus.put("message", "Mandatory field is empty");
                        responseStatus.put("status_code", ResponseStatusCode.MANDATORY_FIELD_MISSING);
                    }
                } else {
                    responseStatus.put("status", "failed");
                    responseStatus.put("message", "Invalid User");
                    responseStatus.put("status_code", ResponseStatusCode.UNAUTHORIZED_USER);
                }
            } catch (Exception e) {
                e.printStackTrace();
                responseStatus.put("status", "failed");
                responseStatus.put("message", e.getMessage());
                responseStatus.put("status_code", ResponseStatusCode.INTERNAL_ERROR);
                responseJson.put("response_status", responseStatus);
                response.getWriter().write(responseJson.toString());
                throw new RuntimeException(e);
            }
        }
        else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseStatus.put("status", "failed");
            responseStatus.put("message", "Invalid token! Please Login.");
            responseStatus.put("status_code", ResponseStatusCode.UNAUTHORIZED_USER);
        }
        response.setContentType("application/json");
        responseJson.put("response_status", responseStatus);
        response.getWriter().write(responseJson.toString());


    }

}
