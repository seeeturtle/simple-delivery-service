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

    public Owner(DatabaseAuthInformation dbAuth) {
        this.dbAuth = dbAuth;
        this.ownerID = ownerID;
        this.dbUrl = "jdbc:mysql://" + dbAuth.getHost() + ":" + dbAuth.getPort() + "/" + dbAuth.getDatabase_name();
    }

    public JPanel createOwnerPanel() {
        JPanel ownerPanel = new JPanel();
//        addStanbyOrderTable();

        return ownerPanel;
    }


    // Owner methods
    private void addStanbyOrderTable() {
        JTable orderTable = getStanbyOrderTableFromDb();
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

    private JTable getStanbyOrderTableFromDb() {
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
