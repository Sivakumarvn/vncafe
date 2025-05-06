package com.vncafe.servlet;

import com.vncafe.ApiUtils;
import com.vncafe.constants.ResponseStatusCode;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.json.JSONObject;

import java.io.*;
import java.sql.*;

public class SigninServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vncafe";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "9080089287";
    private ApiUtils apiUtils = ApiUtils.getInstance();
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JSONObject input_data = new JSONObject(request.getParameter("input_data"));
        String email =input_data.getString("email");
        String password =input_data.getString("password");
        boolean isValidUser = false;
        JSONObject responseJson = new JSONObject();
        JSONObject responseStatus = new JSONObject();
        String generatedToken;
        try(Connection conn = apiUtils.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM usercredentials WHERE Email = ? AND PasswordHash = ?");
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                isValidUser = true;
                // 2. Generate new token
                generatedToken = apiUtils.generateToken();

                // 3. Update token into database
                PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE usercredentials SET AuthToken = ?, AuthTokenCreatedAt = NOW() WHERE Email = ?");
                updateStmt.setString(1, generatedToken);
                updateStmt.setString(2, email);
                updateStmt.executeUpdate();
                updateStmt.close();

                stmt = conn.prepareStatement(
                        "SELECT * FROM usercredentials WHERE AuthToken = ? ");
                stmt.setString(1, generatedToken);
                rs = stmt.executeQuery();
                if (rs.next()) {
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
            }
            rs.close();
            stmt.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        if (isValidUser) {
            responseStatus.put("status", "success");
            responseStatus.put("status_code", ResponseStatusCode.OK);
        } else {
            responseStatus = new JSONObject();
            responseStatus.put("status","failed");
            responseStatus.put("status_code",ResponseStatusCode.RESOURCE_NOT_FOUND);
            responseStatus.put("message","Invalid username or password");
        }
        responseJson.put("response_status",responseStatus);
        out.print(responseJson);
    }
}

