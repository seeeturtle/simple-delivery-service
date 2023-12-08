package org.DBTermProject_env_unification;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;


public class User {
    private DatabaseAuthInformation dbAuth;
    private int userId; // 미리 정해진 user_id

    public User(DatabaseAuthInformation dbAuth, int userId) {
        this.dbAuth = dbAuth;
        this.userId = userId;
    }

    // 이곳에 전의 panel 코드 넣어주세요

    public JPanel createUserPanel() {
        JPanel userPanel = new JPanel(new BorderLayout());
        JTable menuTable = getStoreMenuTable();
        JScrollPane scrollPane = new JScrollPane(menuTable);
        userPanel.add(scrollPane, BorderLayout.CENTER);

        JButton orderButton = new JButton("주문");
        userPanel.add(orderButton, BorderLayout.SOUTH);

        menuTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = menuTable.getSelectedRow();
                    int menuId = (Integer) menuTable.getValueAt(row, 0); // 예시로 첫 번째 열이 menu_id라고 가정
                    addToOrderMenu(menuId);
                }
            }
        });

        orderButton.addActionListener(e -> showOrderData());

        return userPanel;
    }

    private JTable getStoreMenuTable() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Menu ID", "Name", "Price"}, 0);
        String query = "SELECT menu_id, name, price FROM store_menu"; // query

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://" + dbAuth.getHost() + ":" + dbAuth.getPort() + "/" + dbAuth.getDatabase_name(),
                dbAuth.getUsername(),
                dbAuth.getPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int menuId = rs.getInt("menu_id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                model.addRow(new Object[]{menuId, name, price});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new JTable(model);
    }


    private void addToOrderMenu(int menuId) {
        String insertQuery = "INSERT INTO order_menu (menu_id, quantity) VALUES (?, 1) ON DUPLICATE KEY UPDATE quantity = quantity + 1";
        String dbUrl = "jdbc:mysql://" + dbAuth.getHost() + ":" + dbAuth.getPort() + "/" + dbAuth.getDatabase_name();

        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setInt(1, menuId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void showOrderData() {
        String query = "SELECT * FROM `order`"; // SQL에서 'order'는 예약어이므로 백틱(`)을 사용
        String dbUrl = "jdbc:mysql://" + dbAuth.getHost() + ":" + dbAuth.getPort() + "/" + dbAuth.getDatabase_name();

        DefaultTableModel model = new DefaultTableModel(new String[]{"Order ID", "Cart ID", "User ID", "Store ID"}, 0);

        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                int cartId = rs.getInt("cart_id");
                int userId = rs.getInt("user_id");
                int storeId = rs.getInt("store_id");
                model.addRow(new Object[]{orderId, cartId, userId, storeId});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JTable orderTable = new JTable(model);
        JOptionPane.showMessageDialog(null, new JScrollPane(orderTable));
    }



}


