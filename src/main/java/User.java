import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.BitSet;


// define user class
public class User {
    private DatabaseAuthInformation dbAuth;
    private int userId; // 미리 정해진 user_id
    private JPanel userPanel;
    private String dbUrl;
    private JScrollPane scrollPane;


    public User(DatabaseAuthInformation dbAuth, int userId) {
        this.dbAuth = dbAuth;
        this.userId = userId;
        this.dbUrl = "jdbc:mysql://" + dbAuth.getHost() + ":" + dbAuth.getPort() + "/" + dbAuth.getDatabase_name();
    }

    public JPanel createUserPanel() {
        userPanel = new JPanel(new BorderLayout());
        addCategoryTable();

        return userPanel;
    }

    // user side's first panel
    private void addCategoryTable() {
        JTable categoryTable = getCategoryTableFromDb();
        categoryTable.removeColumn(categoryTable.getColumnModel().getColumn(0));
        scrollPane = new JScrollPane(categoryTable);
        userPanel.add(scrollPane, BorderLayout.CENTER);

        categoryTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int row = categoryTable.getSelectedRow();
                    int categoryId = (Integer) categoryTable.getModel().getValueAt(row, 0); // 예시로 첫 번째 열이 menu_id라고 가정
                    userPanel.remove(scrollPane);
                    addStoreTable(categoryId);
                    userPanel.revalidate();
                    userPanel.repaint();
                }
            }
        });
    }

    // get storeTable from category
    private void addStoreTable(int categoryId) {
        JTable storeTable = getStoreTableFromDb(categoryId);
        storeTable.removeColumn(storeTable.getColumnModel().getColumn(0));
        scrollPane = new JScrollPane(storeTable);
        userPanel.add(scrollPane, BorderLayout.CENTER);

        storeTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {   // modified click from double-click
                    int row = storeTable.getSelectedRow();
                    int storeId = (Integer) storeTable.getModel().getValueAt(row, 0); // 예시로 첫 번째 열이 menu_id라고 가정
                    userPanel.remove(scrollPane);
                    addMenuTable(storeId);
                    userPanel.revalidate();
                    userPanel.repaint();
                }
            }
        });
    }

    // get menuTable from store. ADD checklist + quantity option
    private void addMenuTable(int storeId) {
        JTable menuTable = getStoreMenuTableFromDb(storeId);
        menuTable.removeColumn(menuTable.getColumnModel().getColumn(0));
        scrollPane = new JScrollPane(menuTable);
        userPanel.add(scrollPane, BorderLayout.CENTER);

        menuTable.setRowSelectionAllowed(true);
        menuTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // "Add To Cart"
        JButton addToCartButton = new JButton("Add To Cart");
        addToCartButton.addActionListener(e -> {
            for (int row = 0; row < menuTable.getModel().getRowCount(); row++) {
                if ((Integer) menuTable.getModel().getValueAt(row, 3) > 0) {
                    int menuId = (Integer) menuTable.getModel().getValueAt(row, 0); // menu_id
                    int price = (Integer) menuTable.getModel().getValueAt(row, 2); // 가격
                    int quantity = (Integer) menuTable.getModel().getValueAt(row, 3); // 수량
                    addToCart(storeId, menuId, quantity, price); // addToCart 함수 호출
                }
            }
        });

        // "Move To Cart"
        JButton moveToCartButton = new JButton("Move To Cart");
        moveToCartButton.addActionListener(e -> showCartData(storeId)); // showOrderData 함수 호출

        // add button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addToCartButton);
        buttonPanel.add(moveToCartButton);
        userPanel.add(buttonPanel, BorderLayout.SOUTH);
    }



    private JTable getCategoryTableFromDb() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"category_id", "title"}, 0);
        String query = "SELECT category_id, title FROM store_category"; // query

        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int categoryId = rs.getInt("category_id");
                String name = rs.getString("title");
                model.addRow(new Object[]{categoryId, name});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new JTable(model);
    }


    private JTable getStoreTableFromDb(int categoryId) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Store ID", "Name"}, 0);
        String insertQuery = "SELECT s.store_id, si.name FROM store s INNER JOIN store_information si ON s.store_id = si.store_id WHERE s.category_id = ?";   // ? is parameter

        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int storeId = rs.getInt("store_id");
                String name = rs.getString("name");
                model.addRow(new Object[]{storeId, name});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new JTable(model);
    }


    private JTable getStoreMenuTableFromDb(int storeID) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Menu ID", "Name", "Price", "Quantity"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Integer.class;
                    case 1:
                        return String.class;
                    case 2:
                        return Integer.class;
                    case 3:
                        return Integer.class;
                }
                return null;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        String insertQuery = "SELECT store_id, menu_id, name, price FROM store_menu WHERE store_id=?"; // query

        try (Connection conn = DriverManager.getConnection(
                dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             Statement stmt = conn.createStatement();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setInt(1, storeID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int menuId = rs.getInt("menu_id");
                String name = rs.getString("name");
                int price = (int) rs.getDouble("price");
                model.addRow(new Object[]{menuId, name, price, 0});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JTable menuTable = new JTable(model);

        TableColumn quantityColumn = menuTable.getColumnModel().getColumn(3); // "Quantity" 열
        JComboBox<Integer> quantityComboBox = new JComboBox<>(new Integer[]{0, 1, 2, 3, 4, 5});
        quantityColumn.setCellEditor(new DefaultCellEditor(quantityComboBox));

        return menuTable;
    }


    private void addMenuToOrder(int menuId) {
        String insertQuery = "INSERT INTO order_menu (menu_id2, order_menu_quantity) VALUES (?, 1) ON DUPLICATE KEY UPDATE order_menu_quantity = order_menu_quantity + 1";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setInt(1, menuId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addToCart(int storeId, int menuId, int quantity, int price) {
        int cartId = createOrGetCart(storeId);

        int order_menu_id = -1;
        String checkMenuAlreadyExistsQuery = "SELECT * FROM order_menu WHERE cart_id = ? AND menu_id2 = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(checkMenuAlreadyExistsQuery)) {

            pstmt.setInt(1, cartId);
            pstmt.setInt(2, menuId);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                order_menu_id = resultSet.getInt("order_menu_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (order_menu_id >= 0) {
            String updateQuery = "UPDATE order_menu SET order_menu_quantity = order_menu_quantity + ? WHERE order_menu_id = ?";

            try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
                 PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

                pstmt.setInt(1, quantity);
                pstmt.setInt(2, order_menu_id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String insertQuery = "INSERT INTO order_menu (cart_id, menu_id2, order_menu_quantity, price, status) VALUES (?, ?, ?, ?, 1) ";

            try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
                 PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

                pstmt.setInt(1, cartId);
                pstmt.setInt(2, menuId);
                pstmt.setInt(3, quantity);
                pstmt.setInt(4, price);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        updateCartTotalPrice(cartId);
    }

    private int createOrGetCart(int storeId) {
        String checkCartExistsQuery = "SELECT * FROM order_cart WHERE store_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(checkCartExistsQuery)) {

            pstmt.setInt(1, storeId);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("cart_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String createCartQuery = "INSERT INTO order_cart (store_id, total_price, status) VALUES (?, 0, 1)";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(createCartQuery, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, storeId);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // 생성된 cart_id를 반환
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private void addToCart2(int storeId, int menuId, int quantity, int price) {
        // 새로운 cart 생성하고 get cart_id
        int cartId = createNewCart(storeId); // storeId는 현재 선택된 가게의 ID

        // order_menu 테이블에 메뉴 항목을 추가합니다.
        String insertOrderMenuQuery = "INSERT INTO order_menu (cart_id, menu_id2, order_menu_quantity, price, status) VALUES (?, ?, ?, ?, 1)";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(insertOrderMenuQuery)) {

            pstmt.setInt(1, cartId);
            pstmt.setInt(2, menuId);
            pstmt.setInt(3, quantity);
            pstmt.setInt(4, price);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 총 가격을 업데이트
        updateCartTotalPrice(cartId);
    }

    private int createNewCart(int storeId) {
        // 새로운 cart를 order_cart 테이블에 추가하고 생성된 cart_id를 반환
        String createCartQuery = "INSERT INTO order_cart (store_id, total_price, status) VALUES (?, 0, 1)";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(createCartQuery, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, storeId);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // 생성된 cart_id를 반환
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // cart 생성 실패 시
    }


    private void updateCartTotalPrice(int cartId) {
        // order_cart 테이블의 total_price를 업데이트
        String updateTotalPriceQuery = "UPDATE order_cart SET total_price = (SELECT SUM(price * order_menu_quantity) FROM order_menu WHERE cart_id = ?) WHERE cart_id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(updateTotalPriceQuery)) {

            pstmt.setInt(1, cartId);
            pstmt.setInt(2, cartId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showCartData(int storeId) {
        int cartId = createOrGetCart(storeId);

        DefaultTableModel model = getCartTableModel(storeId, cartId);

        JButton order = new JButton("Order");
        order.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                userPanel.remove(scrollPane);
                addOrderPanel(storeId, cartId);
                userPanel.revalidate();
                userPanel.repaint();
                JOptionPane.getRootFrame().dispose();
            }
        });

        Object[] options = new Object[]{ order};

        JTable cartTable = new JTable(model);
        JOptionPane.showOptionDialog(null, new JScrollPane(cartTable), "Cart", JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
    }

    private DefaultTableModel getCartTableModel(int storeId, int cartId) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Menu", "Quantity", "Price"}, 0);

        String query = "SELECT sm.name as menu, om.order_menu_quantity as quantity, om.price as price " +
                "FROM order_menu om JOIN store_menu sm ON om.cart_id = ? AND om.menu_id2 = sm.menu_id AND sm.store_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, cartId);
            stmt.setInt(2, storeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String menu = rs.getString("menu");
                Integer quantity = rs.getInt("quantity");
                Integer price = rs.getInt("price");
                model.addRow(new Object[] {menu, quantity, price});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }

    private void addOrderPanel(int storeId, int cartId) {
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.add(new JTable(getCartTableModel(storeId, cartId)), BorderLayout.NORTH);

        JPanel orderInfoPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        orderInfoPanel.add(new JLabel("Address"));
        orderInfoPanel.add(new JLabel(getUserAddress(1)));

        orderInfoPanel.add(new JLabel("To Owner"));
        orderInfoPanel.add(new JTextArea());

        orderInfoPanel.add(new JLabel("To Rider"));
        orderInfoPanel.add(new JTextArea());

        orderPanel.add(orderInfoPanel, BorderLayout.CENTER);

        orderPanel.add(new JButton("Complete Order"), BorderLayout.SOUTH);
        scrollPane = new JScrollPane(orderPanel);
        userPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private String getUserAddress(int userId) {
        return "서울특별시 동작구 흑석로 84 309관 724호";
    }

    // 수정해야 함
    private void showOrderData() {
        String query = "SELECT * FROM `order`";

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



