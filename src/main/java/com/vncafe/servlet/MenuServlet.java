package com.vncafe.servlet;

import java.io.*;

import com.vncafe.ApiUtils;
import com.vncafe.constants.ResponseStatusCode;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

public class MenuServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/vncafe";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "9080089287";
    private ApiUtils apiUtils = ApiUtils.getInstance();

    // READ - Get all menu items
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject responseJson = new JSONObject();
        JSONObject responseStatus = new JSONObject();
        JSONObject input_data = new JSONObject();
        if(request.getParameter("input_data") != null) {
            System.out.println(request.getParameter("input_data"));
            input_data = new JSONObject(request.getParameter("input_data"));
        }
        System.out.println(input_data);
        try(Connection conn = apiUtils.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {
            JSONArray menuArray = new JSONArray();
            PreparedStatement stmt;
            ResultSet rs;
            if(!input_data.isEmpty()) {
                JSONArray ids = input_data.optJSONArray("ids");
//                System.out.println("ids: " + ids);
                if(ids != null) {
                    for(int i = 0; i < ids.length(); i++) {
                        JSONObject menu = ids.optJSONObject(i);
                        stmt = conn.prepareStatement("SELECT * FROM menu where id = ?");
                        stmt.setInt(1, menu.getInt("id"));
                        rs = stmt.executeQuery();
                        if(rs.next()) {
                            JSONObject menuJson = new JSONObject();
                            menuJson.put("id", rs.getInt("id"));
                            menuJson.put("name", rs.getString("name"));
                            menuJson.put("description", rs.getString("description"));
                            menuJson.put("price", rs.getDouble("price"));
                            menuJson.put("category", rs.getString("category"));
                            menuJson.put("imgpath", rs.getString("imgPath"));
                            menuJson.put("stock_count", rs.getInt("stockCount"));
                            menuJson.put("available", rs.getBoolean("available"));
                            menuArray.put(menuJson);
                        }
                    }
                }
                else{
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    responseStatus.put("status", "failed");
                    responseStatus.put("status_code", ResponseStatusCode.MANDATORY_FIELD_MISSING);
                    responseJson.put("response_status", responseStatus);
                    out.print(responseJson);
                    return;
                }
            }
            else{
                stmt = conn.prepareStatement("SELECT * FROM menu");
                rs = stmt.executeQuery();
                while (rs.next()) {
                    JSONObject menuJson = new JSONObject();
                    menuJson.put("id", rs.getInt("id"));
                    menuJson.put("name", rs.getString("name"));
                    menuJson.put("description", rs.getString("description"));
                    menuJson.put("price", rs.getDouble("price"));
                    menuJson.put("category", rs.getString("category"));
                    menuJson.put("imgpath", rs.getString("imgPath"));
                    menuJson.put("stock_count", rs.getInt("stockCount"));
                    menuJson.put("available", rs.getBoolean("available"));
                    menuArray.put(menuJson);
                }
            }
            responseStatus.put("status", "success");
            responseStatus.put("status_code", ResponseStatusCode.OK);
            responseJson.put("response_status", responseStatus);
            responseJson.put("menu", menuArray);
            out.print(responseJson);
        } catch (SQLException e) {
            e.printStackTrace(out);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // CREATE - Add new menu item
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JSONObject responseJson = new JSONObject();
        JSONObject responseStatus = new JSONObject();
        JSONObject inputData = new JSONObject(request.getParameter("input_data"));
        String name = inputData.getString("name");
        String description = inputData.getString("description");
        double price = inputData.getDouble("price");
        String category = inputData.getString("category");
        int stockCount = inputData.getInt("stock_count");
        String imgPath = inputData.getString("imgPath");

        try (Connection conn = apiUtils.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM menu WHERE name = ?");
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                responseStatus.put("status", "failed");
                responseStatus.put("status_code", ResponseStatusCode.NOT_UNIQUE);
                responseStatus.put("message", "Product already exists");
            }
            else {
                stmt = conn.prepareStatement(
                        "INSERT INTO menu (name, description, price, category,stockCount,imgPath) VALUES (?, ?, ?, ?,?,?)"
                );
                stmt.setString(1, name);
                stmt.setString(2, description);
                stmt.setDouble(3, price);
                stmt.setString(4, category);
                stmt.setInt(5, stockCount);
                stmt.setString(6, imgPath);
                int row = stmt.executeUpdate();
                if (row > 0) {
                    stmt = conn.prepareStatement(
                            "SELECT * FROM menu WHERE name = ?");
                    stmt.setString(1, name);
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        responseStatus.put("status", "success");
                        responseStatus.put("status_code", ResponseStatusCode.OK);
                        JSONObject menuJson = new JSONObject();
                        menuJson.put("id", rs.getInt("id"));
                        menuJson.put("name", rs.getString("name"));
                        menuJson.put("description", rs.getString("description"));
                        menuJson.put("price", rs.getDouble("price"));
                        menuJson.put("category", rs.getString("category"));
                        menuJson.put("stock_count", rs.getInt("stockCount"));
                        menuJson.put("available", rs.getBoolean("available"));
                        responseJson.put("menu", menuJson);
                    }
                }
            }
            responseJson.put("response_status", responseStatus);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(responseJson);
    }

    // UPDATE - Edit a menu item
    @Override
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
        int stockCount = inputData.getInt("stock_count");
        boolean available = inputData.getBoolean("available");
        JSONObject responseJson = new JSONObject();
        JSONObject responseStatus = new JSONObject();
        try (Connection conn = apiUtils.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM menu WHERE id = ?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                responseStatus.put("status", "failed");
                responseStatus.put("status_code", ResponseStatusCode.RESOURCE_NOT_FOUND);
                responseStatus.put("message", "Product Not Found");
            } else {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE menu SET name=?, description=?, price=?, category=?, available=?, stockCount=? WHERE id=?"
                );
                ps.setString(1, name);
                ps.setString(2, description);
                ps.setDouble(3, price);
                ps.setString(4, category);
                ps.setBoolean(5, available);
                ps.setInt(6, stockCount);
                ps.setInt(7, id);
                int row = ps.executeUpdate();
                if (row > 0) {
                    stmt = conn.prepareStatement(
                            "SELECT * FROM menu WHERE name = ?");
                    stmt.setString(1, name);
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        responseStatus.put("status", "success");
                        responseStatus.put("status_code", ResponseStatusCode.OK);
                        JSONObject menuJson = new JSONObject();
                        menuJson.put("id", rs.getInt("id"));
                        menuJson.put("name", rs.getString("name"));
                        menuJson.put("description", rs.getString("description"));
                        menuJson.put("price", rs.getDouble("price"));
                        menuJson.put("category", rs.getString("category"));
                        menuJson.put("stockCount", rs.getInt("stockCount"));
                        menuJson.put("available", rs.getBoolean("available"));
                        responseJson.put("menu", menuJson);
                    }
                }
            }
            responseJson.put("response_status", responseStatus);
            response.setContentType("application/json");
            response.getWriter().print(responseJson);
        } catch (SQLException | ClassNotFoundException e) {
            throw new ServletException(e);
        }
    }

    // DELETE - Remove a menu item
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JSONObject inputData = new JSONObject(request.getParameter("input_data"));
        int id = inputData.getInt("id");
        JSONObject responseJson = new JSONObject();
        JSONObject responseStatus = new JSONObject();
        try (Connection conn = apiUtils.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM menu WHERE id = ?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                responseStatus.put("status", "failed");
                responseStatus.put("status_code", ResponseStatusCode.RESOURCE_NOT_FOUND);
                responseStatus.put("message", "Product Not Found");
            } else {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM menu WHERE id=?");
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
