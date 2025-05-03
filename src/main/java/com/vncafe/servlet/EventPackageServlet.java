package com.vncafe.servlet;

import com.vncafe.ApiUtils;
import com.vncafe.constants.ResponseStatusCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EventPackageServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vncafe";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "9080089287";
    private ApiUtils apiUtils = ApiUtils.getInstance();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject responseJson = new JSONObject();
        JSONObject responseStatus = new JSONObject();
        try(Connection conn = apiUtils.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement("select * from EventPackage");
            ResultSet rs = stmt.executeQuery();
            JSONArray packageJsonArray = new JSONArray();
            while(rs.next()) {
                JSONObject packageJson = new JSONObject();
                packageJson.put("id", rs.getInt("ID"));
                packageJson.put("name", rs.getString("Name"));
                packageJson.put("description", rs.getString("Description"));
                packageJson.put("price", rs.getBigDecimal("Price"));
                packageJson.put("category", rs.getString("Category"));
                packageJson.put("available", rs.getBoolean("Available"));
                packageJson.put("vacancy", rs.getInt("Vacancy"));
                packageJsonArray.put(packageJson);
            }
            responseJson.put("packages", packageJsonArray);
            responseStatus.put("status", "success");
            responseStatus.put("status_code", ResponseStatusCode.OK);
            responseJson.put("response_status", responseStatus);
            response.getWriter().write(responseJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseStatus.put("status", "failed");
            responseStatus.put("status_code", ResponseStatusCode.INTERNAL_ERROR);
            responseJson.put("response_status", responseStatus);
            response.getWriter().write(responseJson.toString());
            throw new RuntimeException(e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject responseJson = new JSONObject();
        JSONObject responseStatus = new JSONObject();
        JSONObject inputData = new JSONObject(request.getParameter("input_data"));
        String name = inputData.getString("name");
        String description = inputData.getString("description");
        double price = inputData.getDouble("price");
        String category = inputData.getString("category");
        boolean available = inputData.getBoolean("available");
        int vacancy = inputData.getInt("vacancy");
        try (Connection conn = apiUtils.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM EventPackage WHERE name = ?");
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                responseStatus.put("status", "failed");
                responseStatus.put("status_code", ResponseStatusCode.NOT_UNIQUE);
                responseStatus.put("message", "Event Package already exists");
            }
            else {
                stmt = conn.prepareStatement(
                        "INSERT INTO EventPackage (name, description, price, category,available,vacancy) VALUES (?, ?, ?, ?,?,?)"
                );
                stmt.setString(1, name);
                stmt.setString(2, description);
                stmt.setDouble(3, price);
                stmt.setString(4, category);
                stmt.setBoolean(5, available);
                stmt.setInt(6, vacancy);
                int row = stmt.executeUpdate();
                if (row > 0) {
                    stmt = conn.prepareStatement(
                            "SELECT * FROM EventPackage WHERE name = ?");
                    stmt.setString(1, name);
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        responseStatus.put("status", "success");
                        responseStatus.put("status_code", ResponseStatusCode.OK);
                        JSONObject packageJson = new JSONObject();
                        packageJson.put("id", rs.getInt("id"));
                        packageJson.put("name", rs.getString("name"));
                        packageJson.put("description", rs.getString("description"));
                        packageJson.put("price", rs.getDouble("price"));
                        packageJson.put("category", rs.getString("category"));
                        packageJson.put("vacancy", rs.getInt("vacancy"));
                        packageJson.put("available", rs.getBoolean("available"));
                        responseJson.put("package", packageJson);
                    }
                }
            }
            responseJson.put("response_status", responseStatus);
            response.getWriter().write(responseJson.toString());
        }catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseStatus.put("status", "failed");
            responseStatus.put("status_code", ResponseStatusCode.INTERNAL_ERROR);
            responseJson.put("response_status", responseStatus);
            response.getWriter().write(responseJson.toString());
            throw new RuntimeException(e);
        }
    }
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        JSONObject json = new JSONObject(sb.toString());
        JSONObject inputData = json.getJSONObject("input_data");
        int id = inputData.getInt("id");
        String name = inputData.getString("name");
        String description = inputData.getString("description");
        double price = inputData.getDouble("price");
        String category = inputData.getString("category");
        boolean available = inputData.getBoolean("available");
        int vacancy = inputData.getInt("vacancy");
        JSONObject responseJson = new JSONObject();
        JSONObject responseStatus = new JSONObject();
        try (Connection conn = apiUtils.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM EventPackage WHERE id = ?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                responseStatus.put("status", "failed");
                responseStatus.put("status_code", ResponseStatusCode.RESOURCE_NOT_FOUND);
                responseStatus.put("message", "Event Package Not Found");
            } else {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE EventPackage SET name=?, description=?, price=?, category=?, available=?, vacancy=? WHERE id=?"
                );
                ps.setString(1, name);
                ps.setString(2, description);
                ps.setDouble(3, price);
                ps.setString(4, category);
                ps.setBoolean(5, available);
                ps.setInt(6, vacancy);
                ps.setInt(7, id);
                int row = ps.executeUpdate();
                if (row > 0) {
                    stmt = conn.prepareStatement(
                            "SELECT * FROM EventPackage WHERE name = ?");
                    stmt.setString(1, name);
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        responseStatus.put("status", "success");
                        responseStatus.put("status_code", ResponseStatusCode.OK);
                        JSONObject packageJson = new JSONObject();
                        packageJson.put("id", rs.getInt("id"));
                        packageJson.put("name", rs.getString("name"));
                        packageJson.put("description", rs.getString("description"));
                        packageJson.put("price", rs.getDouble("price"));
                        packageJson.put("category", rs.getString("category"));
                        packageJson.put("vacancy", rs.getInt("vacancy"));
                        packageJson.put("available", rs.getBoolean("available"));
                        responseJson.put("package", packageJson);
                    }
                }
            }
            responseJson.put("response_status", responseStatus);
            response.setContentType("application/json");
            response.getWriter().print(responseJson);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JSONObject inputData = new JSONObject(request.getParameter("input_data"));
        int id = inputData.getInt("id");
        JSONObject responseJson = new JSONObject();
        JSONObject responseStatus = new JSONObject();
        try (Connection conn = apiUtils.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM EventPackage WHERE id = ?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                responseStatus.put("status", "failed");
                responseStatus.put("status_code", ResponseStatusCode.RESOURCE_NOT_FOUND);
                responseStatus.put("message", "Package Not Found");
            } else {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM EventPackage WHERE id=?");
                ps.setInt(1, id);
                int row = ps.executeUpdate();
                if (row > 0) {
                    responseStatus.put("status", "success");
                    responseStatus.put("status_code", ResponseStatusCode.OK);
                } else {
                    responseStatus.put("status", "failed");
                    responseStatus.put("status_code", ResponseStatusCode.INTERNAL_ERROR);
                }
            }
            responseJson.put("response_status", responseStatus);
            response.setContentType("application/json");
            response.getWriter().println(responseJson);
        } catch (SQLException | ClassNotFoundException e) {
            throw new ServletException(e);
        }
    }
}
