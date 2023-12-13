import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.*;


public class Rider {
    private DatabaseAuthInformation dbAuth;
    private int riderID;
    private JPanel riderPanel;
    private JScrollPane scrollPane;
    private JButton allocateButton;
    private JTable table;
    private String dbUrl;
    private ImageIcon mapIcon;

    public Rider(DatabaseAuthInformation dbAuth, int riderID) {
        this.dbAuth = dbAuth;
        this.riderID = riderID;
        this.dbUrl = "jdbc:mysql://" + dbAuth.getHost() + ":" + dbAuth.getPort() + "/" + dbAuth.getDatabase_name();
        try {
            this.mapIcon = new ImageIcon(ImageIO.read(ClassLoader.getSystemResource("map_screenshot.jpg")));
            Image image = mapIcon.getImage();
            mapIcon.setImage(image.getScaledInstance((int) (500.0 * mapIcon.getIconWidth() / mapIcon.getIconHeight()),
                    500, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JPanel createRiderPanel() {
        riderPanel = new JPanel(new BorderLayout());
        addUnallocatedUi();

        return riderPanel;
    }

    private void addUnallocatedUi() {
        addUnallocatedOrderTable();

        allocateButton = new JButton("Allocate");
        riderPanel.add(allocateButton, BorderLayout.SOUTH);
        allocateButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow();
                int standByOrderId = (int) table.getModel().getValueAt(selectedRow, 0);
                updateAsAllocated(standByOrderId);
                riderPanel.remove(scrollPane);
                riderPanel.remove(allocateButton);
                addRidingUi(standByOrderId);
                riderPanel.repaint();
                riderPanel.revalidate();
            }
        });

        riderPanel.add(allocateButton, BorderLayout.SOUTH);
    }

    private void addRidingUi(int standByOrderId) {
        JLabel mapLabel = new JLabel(mapIcon);
        riderPanel.add(mapLabel, BorderLayout.CENTER);

        JButton completeButton = new JButton("Complete");
        riderPanel.add(completeButton, BorderLayout.SOUTH);
        completeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                updateAsComplete(standByOrderId);
                riderPanel.remove(mapLabel);
                riderPanel.remove(completeButton);
                addUnallocatedUi();
                riderPanel.repaint();
                riderPanel.revalidate();

                JOptionPane.showMessageDialog(null, "배달이 완료되었습니다!", "알림", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void updateAsComplete(int standByOrderId) {
        String updateAllocation = "UPDATE rider_order_allocation SET status = 0 WHERE standby_order_id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(updateAllocation)) {

            pstmt.setInt(1, standByOrderId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateAsAllocated(int standByOrderId) {
        String insertAllocationQuery = "INSERT INTO rider_order_allocation (standby_order_id, allocation_status, status) " +
                "VALUES (?, 1, 1)";
        String updateStandByOrderQuery = "UPDATE standby_order SET standby_status = 0, status = 0 WHERE standby_order_id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(insertAllocationQuery);
             PreparedStatement preparedStatement = conn.prepareStatement(updateStandByOrderQuery)) {

            pstmt.setInt(1, standByOrderId);
            preparedStatement.setInt(1, standByOrderId);
            pstmt.executeUpdate();
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addUnallocatedOrderTable() {
        table = new JTable(getUnallocatedOrderModel());
        scrollPane = new JScrollPane(table);
        riderPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private TableModel getUnallocatedOrderModel() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"StandByOrder ID", "Address", "Delivery Fee"}, 0);

        String orderQuery = "SELECT sbo.standby_order_id, ua.address, o.delivery_fee " +
                "FROM standby_order sbo JOIN `order` o ON sbo.order_id2 = o.order_id " +
                "JOIN user_address ua ON ua.address_id = o.address_id " +
                "WHERE sbo.rider_id = ? AND owner_accept = 1 AND sbo.status = 1";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement preparedStatement = conn.prepareStatement(orderQuery)) {
            preparedStatement.setInt(1, riderID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int standByOrderId = resultSet.getInt("standby_order_id");
                String address = resultSet.getString("address");
                int deliveryFee = resultSet.getInt("delivery_fee");

                model.addRow(new Object[]{standByOrderId, address, deliveryFee});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return model;
    }

    // Rider methods
    private void addOrderTable() {
        JTable orderTable = getOrderTableFromDb();
        orderTable.removeColumn(orderTable.getColumnModel().getColumn(0));
        JScrollPane scrollPane = new JScrollPane(orderTable);
        riderPanel.add(scrollPane, BorderLayout.CENTER);

        orderTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int row = orderTable.getSelectedRow();
                    int categoryId = (Integer) orderTable.getModel().getValueAt(row, 0); // 예시로 첫 번째 열이 menu_id라고 가정
                    orderTable.remove(scrollPane);
                    // 다음 화면
                    riderPanel.revalidate();
                    riderPanel.repaint();
                }
            }
        });
    }

    private JTable getOrderTableFromDb() {
        DefaultTableModel model = new DefaultTableModel(new String[]{}, 0);
        return new JTable(model);
    }

    public void updateState() {
        if (scrollPane != null) {
            riderPanel.remove(scrollPane);
            riderPanel.remove(allocateButton);
            addUnallocatedUi();
        }
    }
}
