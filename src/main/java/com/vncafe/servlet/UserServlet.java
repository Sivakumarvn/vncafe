package com.vncafe.servlet;

import com.vncafe.ApiUtils;
import com.vncafe.constants.ResponseStatusCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;

public class UserServlet extends HttpServlet {
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
        if(isValidToken) {
            String authToken = request.getHeader("Authorization").substring(7);
            try(Connection conn = apiUtils.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM usercredentials WHERE AuthToken = ? ");
                stmt.setString(1, authToken);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    responseStatus.put("status", "success");
                    responseStatus.put("status_code", ResponseStatusCode.OK);
                    JSONObject userJson = new JSONObject();
                    userJson.put("id",rs.getString("UserID"));
                    userJson.put("username",rs.getString("Username"));
                    userJson.put("role",rs.getString("Role"));
                    userJson.put("email",rs.getString("Email"));
                    userJson.put("auth_token",rs.getString("AuthToken"));
                    userJson.put("address",rs.getString("Address")!=null?rs.getString("Address"):"");
                    userJson.put("contact_no",rs.getString("ContactNumber")!=null?rs.getString("ContactNumber"):"");
                    responseJson.put("user",userJson);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
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
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject responseJson = new JSONObject();
        JSONObject responseStatus = new JSONObject();
        boolean isValidToken;
        try {
            isValidToken = apiUtils.verifyToken(request);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if(isValidToken) {
            String authToken = request.getHeader("Authorization").substring(7);
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject json = new JSONObject(sb.toString());
            JSONObject inputData = json.getJSONObject("input_data");
            String address = inputData.getString("address");
            String contactNo = inputData.getString("contact_no");
            try(Connection conn = apiUtils.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE usercredentials SET Address=?, ContactNumber=? WHERE AuthToken=?");
                stmt.setString(1, address);
                stmt.setString(2, contactNo);
                stmt.setString(3, authToken);
                int rows = stmt.executeUpdate();
                if(rows > 0) {
                    responseStatus.put("status", "success");
                    responseStatus.put("status_code", ResponseStatusCode.OK);
                    stmt = conn.prepareStatement("SELECT * FROM usercredentials WHERE AuthToken = ?");
                    stmt.setString(1, authToken);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        JSONObject userJson = new JSONObject();
                        userJson.put("id",rs.getString("UserID"));
                        userJson.put("username",rs.getString("Username"));
                        userJson.put("role",rs.getString("Role"));
                        userJson.put("email",rs.getString("Email"));
                        userJson.put("auth_token",rs.getString("AuthToken"));
                        userJson.put("address",rs.getString("Address"));
                        userJson.put("contact_no",rs.getString("ContactNumber"));
                        responseJson.put("user",userJson);
                    }
                }else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    responseStatus.put("status", "failed");
                    responseStatus.put("message", "Update Operation Failed");
                    responseStatus.put("status_code", ResponseStatusCode.INTERNAL_ERROR);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
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
}
