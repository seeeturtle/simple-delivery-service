import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;


public class Rider {
    private DatabaseAuthInformation dbAuth;
    private int riderID;
    private JPanel riderPanel;
    private String dbUrl;

    public Rider(DatabaseAuthInformation dbAuth, int riderID) {
        this.dbAuth = dbAuth;
        this.riderID = riderID;
        this.dbUrl = "jdbc:mysql://" + dbAuth.getHost() + ":" + dbAuth.getPort() + "/" + dbAuth.getDatabase_name();
    }

    public JPanel createRiderPanel() {
        riderPanel = new JPanel(new BorderLayout());
//        addOrderTable();

        return riderPanel;
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
}
