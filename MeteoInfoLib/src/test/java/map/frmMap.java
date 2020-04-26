package map;

import org.meteoinfo.data.mapdata.MapDataManage;
import org.meteoinfo.data.mapdata.ShapeFileManage;
import org.meteoinfo.global.GenericFileFilter;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.layout.MapLayout;
import org.meteoinfo.legend.LayersLegend;
import org.meteoinfo.map.MapView;
import org.meteoinfo.map.MouseTools;
import org.meteoinfo.projection.info.ProjectionInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private MapView mapView;
    private MapLayout mapLayout;

    public frmMap() {
        initComponents();

        mapDocument.getActiveMapFrame().setMapView(mapView);
        //mapDocument.setMapLayout(mapLayout);
        this.mapView.setMouseTool(MouseTools.Pan);
        //this.mapView.zoomToExtent(mapView.getViewExtent());

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
        splitPane.setRightComponent(tabbedPane);
        mapView = new MapView();
        mapLayout = new MapLayout();
        tabbedPane.addTab("Map", mapView);
        tabbedPane.addTab("Layout", mapLayout);
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
