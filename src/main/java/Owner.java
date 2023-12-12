import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class Owner {
    private DatabaseAuthInformation dbAuth;
    private int ownerID;
    private JPanel ownerPanel;
    private String dbUrl;
    private JScrollPane scrollPane;
    private JTable table;

    public Owner(DatabaseAuthInformation dbAuth) {
        this.dbAuth = dbAuth;
        this.ownerID = ownerID;
        this.dbUrl = "jdbc:mysql://" + dbAuth.getHost() + ":" + dbAuth.getPort() + "/" + dbAuth.getDatabase_name();
    }

    public Owner(DatabaseAuthInformation dbAuth, int ownerID) {
        this.dbAuth = dbAuth;
        this.ownerID = ownerID;
        this.dbUrl = "jdbc:mysql://" + dbAuth.getHost() + ":" + dbAuth.getPort() + "/" + dbAuth.getDatabase_name();
    }

    public JPanel createOwnerPanel() {
        ownerPanel = new JPanel(new BorderLayout());
        addUnacceptedOrderTable();

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        JButton acceptButton = new JButton("Accept");
        acceptButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (int i = 0; i < table.getRowCount(); i++) {
                    if ((Boolean) table.getModel().getValueAt(i, 4)) {
                        Integer standByOrderId = (Integer) table.getModel().getValueAt(i, 0);
                        updateAsAccepted(standByOrderId);
                    }
                }
                updateState();
            }
        });
        JButton rejectButton = new JButton("Reject");
        rejectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (int i = 0; i < table.getRowCount(); i++) {
                    if ((Boolean) table.getModel().getValueAt(i, 4)) {
                        Integer standByOrderId = (Integer) table.getModel().getValueAt(i, 0);
                        updateAsRejected(standByOrderId);
                    }
                }
                updateState();
            }
        });
        buttonPanel.add(acceptButton);
        buttonPanel.add(rejectButton);

        ownerPanel.add(buttonPanel, BorderLayout.SOUTH);

        return ownerPanel;
    }

    public void updateState() {
        ownerPanel.remove(scrollPane);
        addUnacceptedOrderTable();
        ownerPanel.repaint();
        ownerPanel.revalidate();
    }

    private void updateAsRejected(Integer standByOrderId) {
        String updateQuery = "UPDATE standby_order SET status = 0 WHERE standby_order_id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement preparedStatement = conn.prepareStatement(updateQuery)) {
            preparedStatement.setInt(1, standByOrderId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateAsAccepted(Integer standByOrderId) {
        String updateQuery = "UPDATE standby_order SET owner_accept = 1 WHERE standby_order_id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement preparedStatement = conn.prepareStatement(updateQuery)) {
            preparedStatement.setInt(1, standByOrderId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addUnacceptedOrderTable() {
        table = new JTable(getUnacceptedOrderModel());
//        table.removeColumn(table.getColumnModel().getColumn(0));
        scrollPane = new JScrollPane(table);
        ownerPanel.add(scrollPane);
    }

    private TableModel getUnacceptedOrderModel() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"StandByOrder ID", "To Owner", "To Rider",
                "Total Price", "Select"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Integer.class;
                    case 1:
                        return String.class;
                    case 2:
                        return String.class;
                    case 3:
                        return Integer.class;
                    case 4:
                        return Boolean.class;
                }
                return null;
            }
        };

        String selectQuery = "SELECT sbo.standby_order_id, o.to_owner_memo, o.to_rider_memo, o.total_price " +
                "FROM standby_order sbo JOIN `order` o ON sbo.order_id2 = o.order_id " +
                "WHERE sbo.status = 1 AND sbo.owner_accept = 0 AND o.owner_id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbAuth.getUsername(), dbAuth.getPassword());
             PreparedStatement preparedStatement = conn.prepareStatement(selectQuery)) {
            preparedStatement.setInt(1, ownerID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int standByOrderId = resultSet.getInt("standby_order_id");
                String toOwner = resultSet.getString("to_owner_memo");
                String toRider = resultSet.getString("to_rider_memo");
                int totalPrice = resultSet.getInt("total_price");
                model.addRow(new Object[] {standByOrderId, toOwner, toRider, totalPrice});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return model;
    }


    // Owner methods
    private void addStandbyOrderTable() {
        JTable orderTable = getStandbyOrderTableFromDb();
        orderTable.removeColumn(orderTable.getColumnModel().getColumn(0));
        JScrollPane scrollPane = new JScrollPane(orderTable);
        ownerPanel.add(scrollPane, BorderLayout.CENTER);

        orderTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int row = orderTable.getSelectedRow();
                    int categoryId = (Integer) orderTable.getModel().getValueAt(row, 0); // 예시로 첫 번째 열이 menu_id라고 가정
                    orderTable.remove(scrollPane);
                    // 다음 화면
                    ownerPanel.revalidate();
                    ownerPanel.repaint();
                }
            }
        });
    }

    private JTable getStandbyOrderTableFromDb() {
        DefaultTableModel model = new DefaultTableModel(new String[]{}, 0);
        String query = ""; // order 내용과 stanby_status를 보여줘야 함. order_id 참조

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
}
