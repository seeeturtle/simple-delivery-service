package ui;

import database.Store;

import javax.swing.*;

public class MenuAndOrder extends JFrame {
    public MenuAndOrder(String storeName) {
        super("메뉴 선택");
        setSize(1500, 800);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
