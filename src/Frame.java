import javax.swing.*;

public class Frame extends JFrame {
    final int size = 500;

    public Frame(){
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // this closes the display on exit
        this.setSize(size, size); // sets the size

        this.setTitle("Fluid Simulation"); // sets the title

        Panel panel;
        panel = new Panel(500, 500);

        this.add(panel);
        this.pack();

        this.setLocationRelativeTo(null);
        this.setVisible(true);

        panel.start();
    }

    public static void main(String[] args) {
        new Frame();
    }
}
