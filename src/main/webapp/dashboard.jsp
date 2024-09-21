<%@ page import="java.sql.*, javax.servlet.http.*, javax.servlet.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Dashboard</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>

    <h1>Welcome, <%= session.getAttribute("username") != null ? session.getAttribute("username") : "Guest" %></h1>
    
    <div>
        <% 
            if (session.getAttribute("success") != null) {
        %>
            <p style="color: green;"><%= session.getAttribute("success") %></p>
        <% 
            session.removeAttribute("success"); 
            }
        %>
        
        <% 
            if (session.getAttribute("error") != null) {
        %>
            <p style="color: red;"><%= session.getAttribute("error") %></p>
        <% 
            session.removeAttribute("error");
            }
        %>
    </div>

    <!-- Display current balance -->
    <div>
        <%
            String username = (String) session.getAttribute("username");
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            double balance = 0;
            
            if (username != null) {
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank", "root", "password");
                    String sql = "SELECT balance FROM users WHERE username=?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, username);
                    rs = ps.executeQuery();

                    if (rs.next()) {
                        balance = rs.getDouble("balance");
                    } else {
                        session.setAttribute("error", "User not found.");
                    }
                } catch (Exception e) {
                    session.setAttribute("error", "Database error: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    try {
                        if (rs != null) rs.close();
                        if (ps != null) ps.close();
                        if (conn != null) conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
        %>
                <p>Current Balance: $<%= balance %></p>
        <%
            } else {
                session.setAttribute("error", "Please log in first.");
            }
        %>
    </div>

    <!-- Transaction form -->
    <form action="TransactionServlet" method="POST">
        <label for="action">Transaction Type:</label>
        <select name="action" id="action" required>
            <option value="Deposit">Deposit</option>
            <option value="Withdraw">Withdraw</option>
        </select>

        <label for="amount">Amount:</label>
        <input type="number" name="amount" id="amount" required>

        <button type="submit">Submit</button>
    </form>

</body>
</html>
