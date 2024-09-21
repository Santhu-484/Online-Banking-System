package com.bank;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class TransactionServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            // Redirect to login if session is invalid
            response.sendRedirect("login.html");
            return;
        }
        
        String username = (String) session.getAttribute("username");
        String action = request.getParameter("action");  // Can be "Deposit" or "Withdraw"
        double amount = Double.parseDouble(request.getParameter("amount"));

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // Connect to the database
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank", "root", "Santhu1@");

            // Get current balance of the user
            String sql = "SELECT balance FROM users WHERE username=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            double currentBalance = 0;
            
            if (rs.next()) {
                currentBalance = rs.getDouble("balance");
            }

            if (action.equals("Withdraw")) {
                if (amount > currentBalance) {
                    // Insufficient balance
                    response.getWriter().println("Insufficient balance for withdrawal.");
                    return;
                }
                currentBalance -= amount; // Deduct amount from current balance
            } else if (action.equals("Deposit")) {
                currentBalance += amount; // Add amount to current balance
            }

            // Update the new balance in the users table
            sql = "UPDATE users SET balance=? WHERE username=?";
            ps = conn.prepareStatement(sql);
            ps.setDouble(1, currentBalance);
            ps.setString(2, username);
            ps.executeUpdate();

            // Record the transaction in the transactions table
            sql = "INSERT INTO transactions (username, type, amount) VALUES (?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, action);
            ps.setDouble(3, amount);
            ps.executeUpdate();

            // Redirect to dashboard with a success message
            session.setAttribute("message", "Transaction successful.");
            response.sendRedirect("dashboard.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error processing transaction.");
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
