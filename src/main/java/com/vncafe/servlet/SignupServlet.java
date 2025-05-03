package com.vncafe.servlet;

import com.vncafe.ApiUtils;
import com.vncafe.constants.ResponseStatusCode;
import org.json.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class SignupServlet extends HttpServlet {
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
        String username=input_data.getString("username");
        boolean isRegistered = false;
        JSONObject responseJson = new JSONObject();
        JSONObject responseStatus = new JSONObject();
        try(Connection conn = apiUtils.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM usercredentials WHERE Email = ?");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                responseStatus.put("status", "failed");
                responseStatus.put("status_code", ResponseStatusCode.NOT_UNIQUE);
                responseStatus.put("message", "Email already exists");
            }else{
                stmt = conn.prepareStatement(
                        "INSERT INTO usercredentials (Username,Email, PasswordHash) VALUES (?, ?, ?)");
                stmt.setString(2, email);
                stmt.setString(3, password);
                stmt.setString(1,username);
                int row = stmt.executeUpdate();
                if(row>0) {
                    responseStatus.put("status", "success");
                    responseStatus.put("status_code",ResponseStatusCode.OK);
                }else{
                    responseStatus.put("status", "failed");
                    responseStatus.put("status_code",ResponseStatusCode.INTERNAL_ERROR);
                }
            }
            responseJson.put("response_status", responseStatus);
            stmt.close();
            conn.close();
        } catch (SQLIntegrityConstraintViolationException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(responseJson);
    }
}

