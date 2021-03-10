package map;

import org.meteoinfo.common.GenericFileFilter;
import org.meteoinfo.geo.mapdata.MapDataManage;
import org.meteoinfo.geo.layer.MapLayer;
import org.meteoinfo.geo.layout.MapLayout;
import org.meteoinfo.geo.legend.LayersLegend;
import org.meteoinfo.geo.mapview.MapView;
import org.meteoinfo.geo.mapview.MouseTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class frmMap extends JFrame {

    private JMenuBar menuBar;
    private JMenu menuFile;
    private JToolBar toolBar;
    private JButton buttonAddLayer;
    private JSplitPane splitPane;
    private LayersLegend mapDocument;
    private JTabbedPane tabbedPane;
    private JPanel jPanel_MapTab;
    private MapView mapView;
    private MapLayout mapLayout;

    public frmMap() {
        initComponents();

        mapDocument.getActiveMapFrame().setMapView(mapView);
        mapDocument.setMapLayout(mapLayout);
        mapLayout.setLockViewUpdate(true);
        this.mapView.setMouseTool(MouseTools.Pan);

        this.addLayers();
    }

    private void initComponents() {
        //Add menu bar
        menuBar = new JMenuBar();
        menuFile = new JMenu("File");
        menuBar.add(menuFile);
        this.setJMenuBar(menuBar);

        //Add tool bar
        toolBar = new JToolBar();
        this.add(toolBar, BorderLayout.NORTH);
        //Add layer button
        buttonAddLayer = new JButton();
        buttonAddLayer.setIcon(new ImageIcon(getClass().getResource("/images/Add_Layer.png")));
        buttonAddLayer.setToolTipText("Add Layer");
        buttonAddLayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addLayerClick(e);
            }
        });
        toolBar.add(buttonAddLayer);

        //Add split pane
        splitPane = new JSplitPane();
        splitPane.setDividerLocation(180);
        this.add(splitPane, BorderLayout.CENTER);

        //Add map document
        mapDocument = new LayersLegend();
        splitPane.setLeftComponent(mapDocument);

        //Add map view and layer
        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPane_MainStateChanged(evt);
            }
        });
        splitPane.setRightComponent(tabbedPane);
        mapView = new MapView();
        mapLayout = new MapLayout();
        jPanel_MapTab = new JPanel();
        jPanel_MapTab.setLayout(new BorderLayout());
        tabbedPane.addTab("Map", jPanel_MapTab);
        jPanel_MapTab.add(mapView, BorderLayout.CENTER);
        tabbedPane.addTab("Layout", mapLayout);
    }

    private void tabbedPane_MainStateChanged(javax.swing.event.ChangeEvent evt) {
        // TODO add your handling code here:
        int selIndex = this.tabbedPane.getSelectedIndex();
        switch (selIndex) {
            case 0:    //MapView
                this.mapLayout.setLockViewUpdate(true);
                mapView.zoomToExtent(mapView.getViewExtent());
                break;
            case 1:    //MapLayout
                this.mapLayout.setLockViewUpdate(false);
                this.mapLayout.paintGraphics();
                break;
        }
    }

    private void setMapView() {
        //Add map view
        mapView.setLockViewUpdate(true);
        this.jPanel_MapTab.removeAll();
        this.jPanel_MapTab.add(mapView, BorderLayout.CENTER);
        mapView.setLockViewUpdate(false);

        mapView.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                //mapView_MouseMoved(e);
            }
        });

        mapView.setFocusable(true);
        mapView.requestFocusInWindow();
    }

    void addLayerClick(ActionEvent e) {
        String path = System.getProperty("user.dir");
        File pathDir = new File(path);
        JFileChooser aDlg = new JFileChooser();
        aDlg.setAcceptAllFileFilterUsed(false);
        aDlg.setCurrentDirectory(pathDir);
        String[] fileExts = new String[]{"shp", "bmp", "gif", "jpg", "png"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "Supported Formats");
        aDlg.setFileFilter(mapFileFilter);
        fileExts = new String[]{"shp"};
        mapFileFilter = new GenericFileFilter(fileExts, "Shape File (*.shp)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File aFile = aDlg.getSelectedFile();
            System.setProperty("User.dir", aFile.getParent());
            MapLayer aLayer = null;
            try {
                //aLayer=ShapeFileManage.loadShapeFile(aFile.getAbsolutePath());
                aLayer = MapDataManage.loadLayer(aFile.getAbsolutePath());
            } catch (IOException ex) {
                Logger.getLogger(frmMap.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(frmMap.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (aLayer != null) {
                this.mapDocument.getActiveMapFrame().addLayer(aLayer);
            }
        }
    }

    private void addLayers() {
        String fn = "D:/Temp/Map/country1.shp";
        try {
            MapLayer layer = MapDataManage.loadLayer(fn);
            this.mapDocument.getActiveMapFrame().addLayer(layer);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                //frame.setLocationRelativeTo(null);
                frame.setSize(1000, 600);
                frame.setVisible(true);
            }
        });
    }
}
