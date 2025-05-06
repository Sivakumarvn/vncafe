package com.vncafe.servlet;


import com.vncafe.ApiUtils;
import com.vncafe.constants.ResponseStatusCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

public class OrderServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vncafe";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "9080089287";
    private ApiUtils apiUtils = ApiUtils.getInstance();

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
            System.out.println("ORDER DATA: " + input_data);
            int userId = input_data.optInt("user_id");
            try(Connection conn = apiUtils.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {
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
                if(rs.next()) {
                    JSONObject order = input_data.getJSONObject("order");
                    JSONArray menu = order.optJSONArray("menu");
                    if (menu != null && !menu.isEmpty()) {
                        String orderSQL = "INSERT INTO orders (user_id, total_amount) VALUES (?, ?)";
                        PreparedStatement orderStmt = conn.prepareStatement(orderSQL, Statement.RETURN_GENERATED_KEYS);

                        BigDecimal totalAmount = BigDecimal.ZERO;

                        // Calculate total price
                        for (int i = 0; i < menu.length(); i++) {
                            JSONObject menuItem = menu.getJSONObject(i);
                            String priceSQL = "SELECT price FROM menu WHERE id = ?";
                            PreparedStatement priceStmt = conn.prepareStatement(priceSQL);
                            priceStmt.setInt(1, menuItem.getInt("id"));
                            rs = priceStmt.executeQuery();
                            if (rs.next()) {
                                BigDecimal price = rs.getBigDecimal("price");
                                int qty = menuItem.getInt("quantity");
                                totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(qty)));
                            }
                        }

                        orderStmt.setInt(1, userId);
                        orderStmt.setBigDecimal(2, totalAmount);
                        orderStmt.executeUpdate();

                        rs = orderStmt.getGeneratedKeys();
                        int orderId = 0;
                        if (rs.next()) {
                            orderId = rs.getInt(1);
                        }

                        // Insert into order_items
                        String itemSQL = "INSERT INTO order_items (order_id, menu_id, quantity) VALUES (?, ?, ?)";
                        PreparedStatement itemStmt = conn.prepareStatement(itemSQL);

                        for (int i = 0; i < menu.length(); i++) {
                            JSONObject menuItem = menu.getJSONObject(i);
                            itemStmt.setInt(1, orderId);
                            itemStmt.setInt(2, menuItem.getInt("id"));
                            itemStmt.setInt(3, menuItem.getInt("quantity"));
                            itemStmt.addBatch();
                        }

                        itemStmt.executeBatch();
//                    conn.commit();
                        responseStatus.put("status", "success");
                        responseStatus.put("status_code",ResponseStatusCode.OK);
                        responseJson.put("order", new JSONObject().put("id", orderId));
                    }
                }
                else{
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
            JSONObject input_data = new JSONObject(request.getParameter("input_data"));
            int userId = input_data.optInt("user_id");
            if (userId==0) {
                responseStatus.put("status", "failed");
                responseStatus.put("message", "Missing user_id");
                responseStatus.put("status_code", ResponseStatusCode.MANDATORY_FIELD_MISSING);
                responseJson.put("response_status", responseStatus);
                response.getWriter().write(responseJson.toString());
                return;
            }

            try(Connection conn = apiUtils.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {
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

                // Get orders
                PreparedStatement orderStmt = conn.prepareStatement(
                        "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC"
                );
                orderStmt.setInt(1, userId);
                ResultSet orderRs = orderStmt.executeQuery();

                JSONArray ordersArray = new JSONArray();

                while (orderRs.next()) {
                    int orderId = orderRs.getInt("order_id");
                    BigDecimal totalAmount = orderRs.getBigDecimal("total_amount");
                    Timestamp orderDate = orderRs.getTimestamp("order_date");

                    // Get order items for this order
                    PreparedStatement itemStmt = conn.prepareStatement(
                            "SELECT menu_id, quantity FROM order_items WHERE order_id = ?"
                    );
                    itemStmt.setInt(1, orderId);
                    ResultSet itemsRs = itemStmt.executeQuery();

                    JSONArray menuArray = new JSONArray();
                    while (itemsRs.next()) {
                        JSONObject itemJson = new JSONObject();
                        PreparedStatement menuStmt = conn.prepareStatement("SELECT * FROM menu WHERE id = ?");
                        menuStmt.setInt(1, itemsRs.getInt("menu_id"));
                        ResultSet menuRs = menuStmt.executeQuery();
                        while (menuRs.next()) {
                            itemJson.put("id", menuRs.getInt("id"));
                            itemJson.put("name", menuRs.getString("name"));
                            itemJson.put("price", menuRs.getBigDecimal("price"));
                            itemJson.put("category", menuRs.getString("category"));
                        }
                        itemJson.put("quantity", itemsRs.getInt("quantity"));
                        menuArray.put(itemJson);
                    }

                    JSONObject orderJson = new JSONObject();
                    orderJson.put("order_id", orderId);
                    orderJson.put("total_amount", totalAmount);
                    orderJson.put("order_date", orderDate.toString());
                    orderJson.put("menu", menuArray);
                    ordersArray.put(orderJson);
                }

                responseStatus.put("status", "success");
                responseStatus.put("status_code", ResponseStatusCode.OK);
                responseJson.put("orders", ordersArray);
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

}

