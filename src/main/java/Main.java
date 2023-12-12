import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        DatabaseAuthInformation dbAuth = new DatabaseAuthInformation();
        if(!dbAuth.parse_auth_info("src/main/resources/mysql.auth")) {
            System.out.println("Failed to parse database credentials.");
            return;
        }

        Owner owner = new Owner(dbAuth, 1);
        User user = new User(dbAuth, 1, owner); // example user_id for program is 1.
        Rider rider = new Rider(dbAuth, 1);

        JFrame frame = new JFrame("Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(1, 3));

        frame.add(user.createUserPanel());
        frame.add(owner.createOwnerPanel());
        frame.add(rider.createRiderPanel());

        frame.pack();
        frame.setVisible(true);
    }
}
