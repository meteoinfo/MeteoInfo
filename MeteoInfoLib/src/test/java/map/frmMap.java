package map;

import javax.swing.*;

public class frmMap extends JFrame {

    private JMenuBar menuBar;
    private JMenu menuFile;
    private JSplitPane panel1;

    public frmMap() {
        initComponents();
    }

    private void initComponents() {
        //Add menu bar
        menuBar = new JMenuBar();
        menuFile = new JMenu("File");
        menuBar.add(menuFile);
        this.setJMenuBar(menuBar);
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //new frmMain().setVisible(true);
                frmMap frame = new frmMap();
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setSize(800, 600);
                frame.setVisible(true);
            }
        });
    }
}
