/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.desktop.forms;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.SVGUtils;
import org.meteoinfo.desktop.config.GenericFileFilter;
import org.meteoinfo.data.DataMath;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.StationData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.DrawMeteoData;
import org.meteoinfo.data.meteodata.DrawType2D;
import org.meteoinfo.data.meteodata.GridDataSetting;
import org.meteoinfo.data.meteodata.MeteoDataInfo;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.data.meteodata.MeteoUVSet;
import org.meteoinfo.data.meteodata.PlotDimension;
import org.meteoinfo.data.meteodata.StationInfoData;
import org.meteoinfo.data.meteodata.StationModelData;
import org.meteoinfo.data.meteodata.TrajDataInfo;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.data.meteodata.grads.GrADSDataInfo;
import org.meteoinfo.data.meteodata.micaps.MICAPS7DataInfo;
import org.meteoinfo.data.meteodata.netcdf.NetCDFDataInfo;
import org.meteoinfo.global.colors.ColorUtil;
import org.meteoinfo.legend.*;
import org.meteoinfo.geoprocess.analysis.InterpolationMethods;
import org.meteoinfo.geoprocess.analysis.InterpolationSetting;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.PointF;
import org.meteoinfo.image.AnimatedGifEncoder;
import org.meteoinfo.layer.LayerTypes;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.layer.RasterLayer;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.shape.ShapeTypes;

/**
 *
 * @author yaqiang
 */
public class FrmMeteoData extends javax.swing.JDialog {

    // <editor-fold desc="Variables">
    //ResourceBundle bundle;
    private final FrmMain _parent;
    private final List<MeteoDataInfo> _dataInfoList;
    private MeteoDataInfo _meteoDataInfo = new MeteoDataInfo();
    private MeteoUVSet meteoUVSet = new MeteoUVSet();
    DrawType2D _2DDrawType;
    private GridData _gridData = new GridData();
    private StationData _stationData = new StationData();
    boolean _useSameLegendScheme = false;
    boolean _useSameGridInterSet = false;
    private LegendScheme _legendScheme = null;
    double[] _cValues;
    Color[] _cColors;
    int _lastAddedLayerHandle = -1;
    private int _selectedIndex = -1;
    private int _strmDensity = 4;
    private int _skipY = 1;
    private int _skipX = 1;
    private boolean _hasUndefData;
    private boolean _isLoading = false;
    private InterpolationSetting _interpolationSetting = new InterpolationSetting();
    private boolean _enableAnimation = true;
    private boolean _isRunning = false;
    private boolean windColor = false;
    private boolean smooth = true;
    private String weatherString = "All Weather";
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Creates new form FrmMeteoData
     *
     * @param parent
     * @param modal
     */
    public FrmMeteoData(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        _parent = (FrmMain) parent;

        this.setIconImages(SVGUtils.createWindowIconImages("/org/meteoinfo/desktop/icons/meteo-data.svg"));

        initComponents();

        this._dataInfoList = new ArrayList<>();

        int height = this.jToolBar1.getHeight() + this.jComboBox_DrawType.getY()
                + this.jComboBox_DrawType.getHeight() + 90;
//        int height = this.jMenuBar_Main.getHeight() + this.jToolBar1.getHeight() + this.jComboBox_DrawType.getY()
//                + this.jComboBox_DrawType.getHeight() + 90;
        this.setSize(500, height);

        this.jComboBox_Variable.setEditable(true);
//        Graphics g = this.getGraphics();
//        FontMetrics metrics = g.getFontMetrics(this.jLabel_Variable.getFont());
//        this.jLabel_Variable.setSize(metrics.stringWidth(this.jLabel_Variable.getText()), metrics.getHeight());
//        this.jLabel_Variable.setLocation(this.jComboBox_Variable.getX() - this.jLabel_Variable.getWidth() - 4, this.jLabel_Variable.getY());
        initialize();
    }

    private void initialize() {
        this.setTitle("Meteo Data");
        this.jList_DataFiles.setModel(new DefaultListModel());
        this.jComboBox_Variable.removeAllItems();
        this.jComboBox_Time.removeAllItems();
        this.jComboBox_Level.removeAllItems();
        this.jComboBox_DrawType.removeAllItems();

        //Control enable set
        for (Component aItem : this.jToolBar1.getComponents()) {
            aItem.setEnabled(false);
        }
        //this.jButton_OpenData.setEnabled(true);
        this.jSplitButton_OpenData.setEnabled(true);
        this.jPanel_DataSet.setEnabled(false);
        this.jCheckBox_ColorVar.setVisible(false);
        this.jCheckBox_Big_Endian.setVisible(false);
    }

    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel_DataSet = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jComboBox_Time = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jComboBox_Level = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jComboBox_DrawType = new javax.swing.JComboBox();
        jCheckBox_ColorVar = new javax.swing.JCheckBox();
        jLabel_Variable = new javax.swing.JLabel();
        jComboBox_Variable = new javax.swing.JComboBox();
        jCheckBox_Big_Endian = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jButton_RemoveAllData = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList_DataFiles = new javax.swing.JList();
        jToolBar1 = new javax.swing.JToolBar();
        //jButton_OpenData = new javax.swing.JButton();        
        jSplitButton_OpenData = new org.meteoinfo.ui.JSplitButton();
        jMenuBar_Main = new javax.swing.JMenuBar();
        jMenu_OpenData = new javax.swing.JMenu();
        jPopupMenu_OpenData = new javax.swing.JPopupMenu();
        jMenuItem_NetCDF = new javax.swing.JMenuItem();
        jMenuItem_GrADS = new javax.swing.JMenuItem();
        jMenuItem_ARL = new javax.swing.JMenuItem();
        jMenu_HYSPLIT = new javax.swing.JMenu();
        jMenuItem_HYSPLIT_Traj = new javax.swing.JMenuItem();
        jMenuItem_HYSPLIT_Conc = new javax.swing.JMenuItem();
        jMenuItem_HYSPLIT_Particle = new javax.swing.JMenuItem();
        jMenu_ASCII = new javax.swing.JMenu();
        jMenuItem_ASCII_LonLat = new javax.swing.JMenuItem();
        jMenuItem_ASCII_SYNOP = new javax.swing.JMenuItem();
        jMenuItem_ASCII_METAR = new javax.swing.JMenuItem();
        jMenuItem_ASCII_EsriGrid = new javax.swing.JMenuItem();
        jMenuItem_ASCII_SurferGrid = new javax.swing.JMenuItem();
        jMenuItem_MICAPS = new javax.swing.JMenuItem();
        jMenu_MM5 = new javax.swing.JMenu();
        jMenuItem_MM5_Output = new javax.swing.JMenuItem();
        jMenuItem_MM5_Inter = new javax.swing.JMenuItem();
        jMenuItem_AWX = new javax.swing.JMenuItem();
        jButton_DataInfo = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton_Draw = new javax.swing.JButton();
        jButton_ViewData = new javax.swing.JButton();
        jButton_ClearDraw = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton_PreTime = new javax.swing.JButton();
        jButton_NexTime = new javax.swing.JButton();
        jButton_Animator = new javax.swing.JButton();
        jButton_CreateAnimatorFile = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jButton_DrawSetting = new javax.swing.JButton();
        jButton_Setting = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jButton_SectionPlot = new javax.swing.JButton();
        jButton_1DPlot = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        jSplitButton_Stat = new org.meteoinfo.ui.JSplitButton();
        jPopupMenu_Stat = new javax.swing.JPopupMenu();
        jMenuItem_ArrivalTime = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setDividerSize(4);

        jPanel_DataSet.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("bundle/Bundle_FrmMeteoData"); // NOI18N
        jLabel2.setText(bundle.getString("FrmMeteoData.jLabel2.text")); // NOI18N

        jComboBox_Time.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
        jComboBox_Time.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_TimeActionPerformed(evt);
            }
        });

        jLabel3.setText(bundle.getString("FrmMeteoData.jLabel3.text")); // NOI18N

        jComboBox_Level.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
        jComboBox_Level.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_LevelActionPerformed(evt);
            }
        });

        jLabel4.setText(bundle.getString("FrmMeteoData.jLabel4.text")); // NOI18N

        jComboBox_DrawType.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
        jComboBox_DrawType.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_DrawTypeActionPerformed(evt);
            }
        });

        jCheckBox_ColorVar.setText(bundle.getString("FrmMeteoData.jCheckBox_ColorVar.text")); // NOI18N
        jCheckBox_ColorVar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_ColorVarActionPerformed(evt);
            }
        });

        jLabel_Variable.setText(bundle.getString("FrmMeteoData.jLabel_Variable.text")); // NOI18N

        jComboBox_Variable.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
        jComboBox_Variable.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_VariableActionPerformed(evt);
            }
        });

        jCheckBox_Big_Endian.setText(bundle.getString("FrmMeteoData.jCheckBox_Big_Endian.text")); // NOI18N
        jCheckBox_Big_Endian.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_Big_EndianActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel_DataSetLayout = new javax.swing.GroupLayout(jPanel_DataSet);
        jPanel_DataSet.setLayout(jPanel_DataSetLayout);
        jPanel_DataSetLayout.setHorizontalGroup(
                jPanel_DataSetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel_DataSetLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel_DataSetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel_Variable)
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel2)
                                        .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel_DataSetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jComboBox_Level, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jComboBox_Time, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jComboBox_DrawType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jComboBox_Variable, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel_DataSetLayout.createSequentialGroup()
                                                .addComponent(jCheckBox_ColorVar)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jCheckBox_Big_Endian)))
                                .addContainerGap()));
        jPanel_DataSetLayout.setVerticalGroup(
                jPanel_DataSetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel_DataSetLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel_DataSetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel_Variable)
                                        .addComponent(jComboBox_Variable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel_DataSetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(jComboBox_Time, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel_DataSetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(jComboBox_Level, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel_DataSetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(jComboBox_DrawType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                                .addGroup(jPanel_DataSetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jCheckBox_ColorVar)
                                        .addComponent(jCheckBox_Big_Endian))
                                .addContainerGap()));

        jSplitPane1.setRightComponent(jPanel_DataSet);

        jButton_RemoveAllData.setText(bundle.getString("FrmMeteoData.jButton_RemoveAllData.text")); // NOI18N
        jButton_RemoveAllData.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_RemoveAllDataActionPerformed(evt);
            }
        });

        jList_DataFiles.setModel(new javax.swing.AbstractListModel() {
            String[] strings = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"};

            @Override
            public int getSize() {
                return strings.length;
            }

            @Override
            public Object getElementAt(int i) {
                return strings[i];
            }
        });
        jList_DataFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList_DataFiles.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList_DataFilesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jList_DataFiles);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addComponent(jButton_RemoveAllData)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton_RemoveAllData)
                                .addGap(10, 10, 10)));

        jSplitPane1.setLeftComponent(jPanel1);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jToolBar1.setRollover(true);

//        jButton_OpenData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Folder_1_16x16x8.png"))); // NOI18N
//        jButton_OpenData.setToolTipText(bundle.getString("FrmMeteoData.jButton_OpenData.toolTipText")); // NOI18N
//        jButton_OpenData.setFocusable(false);
//        jButton_OpenData.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
//        jButton_OpenData.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
//        jButton_OpenData.addMouseListener(new java.awt.event.MouseAdapter() {
//            @Override
//            public void mouseClicked(java.awt.event.MouseEvent evt) {
//                jButton_OpenDataMouseClicked(evt);
//            }
//        });
//        jToolBar1.add(jButton_OpenData);
        //jSplitButton_OpenData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Folder_1_16x16x8.png"))); // NOI18N
        jSplitButton_OpenData.setIcon(new FlatSVGIcon("org/meteoinfo/icons/file-open.svg"));
        jSplitButton_OpenData.setText("  ");
        jSplitButton_OpenData.setToolTipText(bundle.getString("FrmMeteoData.jButton_OpenData.toolTipText"));
        jSplitButton_OpenData.setFocusable(false);
        jSplitButton_OpenData.setArrowColor(ColorUtil.parseToColor("#6E6E6E"));
//        jSplitButton_OpenData.addSplitButtonActionListener(new SplitButtonActionListener() {
//            @Override
//            public void buttonClicked(java.awt.event.ActionEvent evt) {
//                jButton_OpenDataActionPerformed(evt);
//            }
//
//            @Override
//            public void splitButtonClicked(ActionEvent e) {
//                
//            }
//        });
        jMenuItem_NetCDF.setText("NetCDF, GRIB, HDF...");
        jMenuItem_NetCDF.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onNetCDFDataClick(e);
            }
        });
        jPopupMenu_OpenData.add(jMenuItem_NetCDF);
        //jMenu_OpenData.add(jMenuItem_NetCDF);

        jMenuItem_GrADS.setText("GrADS Data");
        jMenuItem_GrADS.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onGrADSDataClick(e);
            }
        });
        jPopupMenu_OpenData.add(jMenuItem_GrADS);
        //jMenu_OpenData.add(jMenuItem_GrADS);

        jMenuItem_ARL.setText("ARL Data");
        jMenuItem_ARL.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onARLDataClick(e);
            }
        });
        jPopupMenu_OpenData.add(jMenuItem_ARL);
        //jMenu_OpenData.add(jMenuItem_ARL);

        jMenu_HYSPLIT.setText("HYSPLIT Data");
        jMenuItem_HYSPLIT_Traj.setText("Trajectory Data");
        jMenuItem_HYSPLIT_Traj.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onHYSPLITTrajDataClick(e);
            }
        });
        jMenu_HYSPLIT.add(jMenuItem_HYSPLIT_Traj);
        jMenuItem_HYSPLIT_Conc.setText("Concentration Data");
        jMenuItem_HYSPLIT_Conc.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onHYSPLITConcDataClick(e);
            }
        });
        jMenu_HYSPLIT.add(jMenuItem_HYSPLIT_Conc);
        jMenuItem_HYSPLIT_Particle.setText("Particle Data");
        jMenuItem_HYSPLIT_Particle.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onHYSPLITPartDataClick(e);
            }
        });
        jMenu_HYSPLIT.add(jMenuItem_HYSPLIT_Particle);
        jPopupMenu_OpenData.add(jMenu_HYSPLIT);
        //jMenu_OpenData.add(jMenu_HYSPLIT);

        jMenu_ASCII.setText("ASCII Data");
        jMenuItem_ASCII_LonLat.setText("Lon/Lat Station Data");
        jMenuItem_ASCII_LonLat.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onLonLatStationsClick(e);
            }
        });
        jMenu_ASCII.add(jMenuItem_ASCII_LonLat);
        jMenuItem_ASCII_SYNOP.setText("SYNOP Data");
        jMenuItem_ASCII_SYNOP.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSYNOPClick(e);
            }
        });
        jMenu_ASCII.add(jMenuItem_ASCII_SYNOP);
        jMenuItem_ASCII_METAR.setText("METAR Data");
        jMenuItem_ASCII_METAR.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onMETARClick(e);
            }
        });
        jMenu_ASCII.add(jMenuItem_ASCII_METAR);
        jMenu_ASCII.add(new JSeparator());
        jMenuItem_ASCII_EsriGrid.setText("Esri ASCII Grid Data");
        jMenuItem_ASCII_EsriGrid.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onASCIIGridDataClick(e);
            }
        });
        jMenu_ASCII.add(jMenuItem_ASCII_EsriGrid);
        jMenuItem_ASCII_SurferGrid.setText("Surfer ASCII Grid Data");
        jMenuItem_ASCII_SurferGrid.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSurferGridDataClick(e);
            }
        });
        jMenu_ASCII.add(jMenuItem_ASCII_SurferGrid);
        jPopupMenu_OpenData.add(jMenu_ASCII);
        //jMenu_OpenData.add(jMenu_ASCII);

        jMenuItem_MICAPS.setText("MICAPS Data");
        jMenuItem_MICAPS.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onMICAPSDataClick(e);
            }
        });
        jPopupMenu_OpenData.add(jMenuItem_MICAPS);
        //jMenu_OpenData.add(jMenuItem_MICAPS);

        jMenu_MM5.setText("MM5 Data");
        jMenuItem_MM5_Output.setText("MM5 Output Data");
        jMenuItem_MM5_Output.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onMM5DataClick(e);
            }
        });
        jMenu_MM5.add(jMenuItem_MM5_Output);
        jMenuItem_MM5_Inter.setText("MM5 Intermediate Data");
        jMenuItem_MM5_Inter.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onMM5IMDataClick(e);
            }
        });
        jMenu_MM5.add(jMenuItem_MM5_Inter);
        jPopupMenu_OpenData.add(jMenu_MM5);
        //jMenu_OpenData.add(jMenu_MM5);

        jMenuItem_AWX.setText("AWX Data");
        jMenuItem_AWX.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAWXDataClick(e);
            }
        });
        jPopupMenu_OpenData.add(jMenuItem_AWX);
        //jMenu_OpenData.add(jMenuItem_AWX);

        //jMenu_OpenData.setText(bundle.getString("FrmMeteoData.jMenu_OpenData.text"));
        //jMenu_OpenData.setMnemonic(KeyEvent.VK_O);
        //jMenuBar_Main.add(jMenu_OpenData);
        //setJMenuBar(jMenuBar_Main);
        jSplitButton_OpenData.setPopupMenu(jPopupMenu_OpenData);
        jToolBar1.add(jSplitButton_OpenData);

        //jButton_DataInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/information.png"))); // NOI18N
        jButton_DataInfo.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/information.svg"));
        jButton_DataInfo.setToolTipText(bundle.getString("FrmMeteoData.jButton_DataInfo.toolTipText")); // NOI18N
        jButton_DataInfo.setFocusable(false);
        jButton_DataInfo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_DataInfo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_DataInfo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_DataInfoActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_DataInfo);
        jToolBar1.add(jSeparator1);

        //jButton_Draw.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_Draw.Image.png"))); // NOI18N
        jButton_Draw.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/draw-layer.svg"));
        jButton_Draw.setToolTipText(bundle.getString("FrmMeteoData.jButton_Draw.toolTipText")); // NOI18N
        jButton_Draw.setFocusable(false);
        jButton_Draw.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Draw.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Draw.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_DrawActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_Draw);

        //jButton_ViewData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_ViewData.Image.png"))); // NOI18N
        jButton_ViewData.setIcon(new FlatSVGIcon("org/meteoinfo/icons/table.svg"));
        jButton_ViewData.setToolTipText(bundle.getString("FrmMeteoData.jButton_ViewData.toolTipText")); // NOI18N
        jButton_ViewData.setFocusable(false);
        jButton_ViewData.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ViewData.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ViewData.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ViewDataActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_ViewData);

        //jButton_ClearDraw.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_ClearDrawing.Image.png"))); // NOI18N
        jButton_ClearDraw.setIcon(new FlatSVGIcon("org/meteoinfo/icons/delete.svg"));
        jButton_ClearDraw.setToolTipText(bundle.getString("FrmMeteoData.jButton_ClearDraw.toolTipText")); // NOI18N
        jButton_ClearDraw.setFocusable(false);
        jButton_ClearDraw.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ClearDraw.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ClearDraw.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ClearDrawActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_ClearDraw);
        jToolBar1.add(jSeparator2);

        //jButton_PreTime.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_PreTime.Image.png"))); // NOI18N
        jButton_PreTime.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/left-arrow.svg"));
        jButton_PreTime.setToolTipText(bundle.getString("FrmMeteoData.jButton_PreTime.toolTipText")); // NOI18N
        jButton_PreTime.setFocusable(false);
        jButton_PreTime.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_PreTime.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_PreTime.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_PreTimeActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_PreTime);

        //jButton_NexTime.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NextTime.Image.png"))); // NOI18N
        jButton_NexTime.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/right-arrow.svg"));
        jButton_NexTime.setToolTipText(bundle.getString("FrmMeteoData.jButton_NexTime.toolTipText")); // NOI18N
        jButton_NexTime.setFocusable(false);
        jButton_NexTime.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NexTime.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NexTime.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NexTimeActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_NexTime);

        //jButton_Animator.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/animation-1.png"))); // NOI18N
        jButton_Animator.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/animator.svg"));
        jButton_Animator.setToolTipText(bundle.getString("FrmMeteoData.jButton_Animator.toolTipText")); // NOI18N
        jButton_Animator.setFocusable(false);
        jButton_Animator.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Animator.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Animator.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_AnimatorActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_Animator);

        //jButton_CreateAnimatorFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Animation-2.png"))); // NOI18N
        jButton_CreateAnimatorFile.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/animator-file.svg"));
        jButton_CreateAnimatorFile.setToolTipText(bundle.getString("FrmMeteoData.jButton_CreateAnimatorFile.toolTipText")); // NOI18N
        jButton_CreateAnimatorFile.setFocusable(false);
        jButton_CreateAnimatorFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_CreateAnimatorFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_CreateAnimatorFile.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_CreateAnimatorFileActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_CreateAnimatorFile);
        jToolBar1.add(jSeparator3);

        //jButton_DrawSetting.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_DrawSetting.Image.png"))); // NOI18N
        jButton_DrawSetting.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/legend-setting.svg"));
        jButton_DrawSetting.setToolTipText(bundle.getString("FrmMeteoData.jButton_DrawSetting.toolTipText")); // NOI18N
        jButton_DrawSetting.setFocusable(false);
        jButton_DrawSetting.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_DrawSetting.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_DrawSetting.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_DrawSettingActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_DrawSetting);

        //jButton_Setting.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Setting-1.png"))); // NOI18N
        jButton_Setting.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/tools.svg"));
        jButton_Setting.setToolTipText(bundle.getString("FrmMeteoData.jButton_Setting.toolTipText")); // NOI18N
        jButton_Setting.setFocusable(false);
        jButton_Setting.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Setting.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Setting.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SettingActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_Setting);
        jToolBar1.add(jSeparator4);

        //jButton_SectionPlot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/chart-5.png"))); // NOI18N
        jButton_SectionPlot.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/section-chart.svg"));
        jButton_SectionPlot.setToolTipText(bundle.getString("FrmMeteoData.jButton_SectionPlot.toolTipText")); // NOI18N
        jButton_SectionPlot.setFocusable(false);
        jButton_SectionPlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_SectionPlot.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_SectionPlot.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SectionPlotActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_SectionPlot);

        //jButton_1DPlot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/chart.png"))); // NOI18N
        jButton_1DPlot.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/chart-line-bar.svg"));
        jButton_1DPlot.setToolTipText(bundle.getString("FrmMeteoData.jButton_1DPlot.toolTipText")); // NOI18N
        jButton_1DPlot.setFocusable(false);
        jButton_1DPlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_1DPlot.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_1DPlot.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_1DPlotActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_1DPlot);
        /*jToolBar1.add(jSeparator5);

        jSplitButton_Stat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Statictics.png"))); // NOI18N
        jSplitButton_Stat.setText("  ");
        jSplitButton_Stat.setToolTipText(bundle.getString("FrmMeteoData.jSplitButton_Stat.toolTipText"));
        jSplitButton_Stat.setFocusable(false);
        jMenuItem_ArrivalTime.setText(bundle.getString("FrmMeteoData.jMenuItem_ArrivalTime.text"));
        jMenuItem_ArrivalTime.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jMenuTime_ArrivalTimeActionPerformed(e);
            }
        });
        jPopupMenu_Stat.add(jMenuItem_ArrivalTime);
        jSplitButton_Stat.setPopupMenu(jPopupMenu_Stat);
        jToolBar1.add(jSplitButton_Stat);*/

        getContentPane().add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        pack();
    }

    private void jButton_OpenDataMouseClicked(java.awt.event.MouseEvent evt) {
        // TODO add your handling code here:
        JPopupMenu menu_OpenData = new JPopupMenu();
        JMenuItem dataMI = new JMenuItem("NetCDF, GRIB, HDF...");
        dataMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onNetCDFDataClick(e);
            }
        });
        menu_OpenData.add(dataMI);

        dataMI = new JMenuItem("GrADS Data");
        dataMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onGrADSDataClick(e);
            }
        });
        menu_OpenData.add(dataMI);

        JMenuItem arlDataMI = new JMenuItem("ARL Data");
        arlDataMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onARLDataClick(e);
            }
        });
        menu_OpenData.add(arlDataMI);

        JMenu hysplitM = new JMenu("HYSPLIT Data");
        menu_OpenData.add(hysplitM);

        dataMI = new JMenuItem("Trajectory Data");
        dataMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onHYSPLITTrajDataClick(e);
            }
        });
        hysplitM.add(dataMI);

        dataMI = new JMenuItem("Concentration Data");
        dataMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onHYSPLITConcDataClick(e);
            }
        });
        hysplitM.add(dataMI);

        dataMI = new JMenuItem("Particle Data");
        dataMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onHYSPLITPartDataClick(e);
            }
        });
        hysplitM.add(dataMI);

        JMenu asciiM = new JMenu("ASCII Data");
        menu_OpenData.add(asciiM);

        dataMI = new JMenuItem("Lon/Lat Station Data");
        dataMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onLonLatStationsClick(e);
            }
        });
        asciiM.add(dataMI);

        dataMI = new JMenuItem("SYNOP Data");
        dataMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSYNOPClick(e);
            }
        });
        asciiM.add(dataMI);

        asciiM.add(new JSeparator());

        dataMI = new JMenuItem("ASCII Grid Data");
        dataMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onASCIIGridDataClick(e);
            }
        });
        asciiM.add(dataMI);

        dataMI = new JMenuItem("Surfer ASCII Grid Data");
        dataMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSurferGridDataClick(e);
            }
        });
        asciiM.add(dataMI);

        dataMI = new JMenuItem("MICAPS Data");
        dataMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onMICAPSDataClick(e);
            }
        });
        menu_OpenData.add(dataMI);

        JMenu mm5M = new JMenu("MM5 Data");
        menu_OpenData.add(mm5M);

        dataMI = new JMenuItem("MM5 Output Data");
        dataMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onMM5DataClick(e);
            }
        });
        mm5M.add(dataMI);

        dataMI = new JMenuItem("MM5 Intermediate Data");
        dataMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onMM5IMDataClick(e);
            }
        });
        mm5M.add(dataMI);

        dataMI = new JMenuItem("AWX Data");
        dataMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAWXDataClick(e);
            }
        });
        menu_OpenData.add(dataMI);

        menu_OpenData.show(this, evt.getX(), evt.getY());
    }

    private void jList_DataFilesMouseClicked(java.awt.event.MouseEvent evt) {
        // TODO add your handling code here:
        //JOptionPane.showMessageDialog(null, this.jList_DataFiles.getSelectedValue().toString(), "", JOptionPane.INFORMATION_MESSAGE);
        if (evt.getButton() == MouseEvent.BUTTON1) {
            if (this.jList_DataFiles.getSelectedIndex() < 0 || this.jList_DataFiles.getSelectedIndex() == _selectedIndex) {
                return;
            }
            _meteoDataInfo = _dataInfoList.get(this.jList_DataFiles.getSelectedIndex());

            updateParameters();
            _selectedIndex = this.jList_DataFiles.getSelectedIndex();
        } else if (evt.getButton() == MouseEvent.BUTTON3) {
            JPopupMenu mnuLayer = new JPopupMenu();
            JMenuItem removeLayerMI = new JMenuItem("Remove Data File");
            removeLayerMI.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onRemoveDataClick(e);
                }
            });
            mnuLayer.add(removeLayerMI);
            mnuLayer.show(this.jList_DataFiles, evt.getX(), evt.getY());
        }
    }

    private void jComboBox_VariableActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (_isLoading) {
            return;
        }

        if (this.jComboBox_Variable.getItemCount() > 0) {
            int i;
            DataInfo aDataInfo = _meteoDataInfo.getDataInfo();
            Variable var = aDataInfo.getVariable(this.jComboBox_Variable.getSelectedItem().toString());
            if (var == null) {
                return;
            }

            //Set times
            //Lab_Time.Text = Resources.GlobalResource.ResourceManager.GetString("Time_Text");
            this.jComboBox_Time.removeAllItems();
            if (var.getTDimension() != null) {
                DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                List<LocalDateTime> times = var.getTimes();
                for (i = 0; i < times.size(); i++) {
                    this.jComboBox_Time.addItem(sdf.format(times.get(i)));
                }
                if (this.jComboBox_Time.getItemCount() > _meteoDataInfo.getTimeIndex()) {
                    this.jComboBox_Time.setSelectedIndex(_meteoDataInfo.getTimeIndex());
                }
            }

            //Set levels
            this.jComboBox_Level.removeAllItems();
            if (var.getZDimension() == null) {
                if (this._meteoDataInfo.isSWATHData()) {
                    Variable lonvar = _meteoDataInfo.getDataInfo().getVariable("longitude");
                    org.meteoinfo.ndarray.Dimension ldim = var.getLevelDimension(lonvar);
                    if (ldim == null) {
                        this.jComboBox_Level.addItem("Surface");
                        this.jComboBox_Level.setSelectedIndex(0);
                    } else {
                        for (i = 0; i < ldim.getLength(); i++) {
                            this.jComboBox_Level.addItem(String.valueOf(ldim.getDimValue().get(i)));
                        }
                        if (this.jComboBox_Level.getItemCount() > _meteoDataInfo.getLevelIndex()) {
                            this.jComboBox_Level.setSelectedIndex(_meteoDataInfo.getLevelIndex());
                        }
                    }
                } else {
                    this.jComboBox_Level.addItem("Surface");
                    this.jComboBox_Level.setSelectedIndex(0);
                }
            } else {
                for (i = 0; i < var.getZDimension().getLength(); i++) {
                    this.jComboBox_Level.addItem(String.valueOf(var.getZDimension().getDimValue().get(i)));
                }
                if (this.jComboBox_Level.getItemCount() > _meteoDataInfo.getLevelIndex()) {
                    this.jComboBox_Level.setSelectedIndex(_meteoDataInfo.getLevelIndex());
                }
            }
        }
    }

    private void jButton_OpenDataActionPerformed(java.awt.event.ActionEvent evt) {
        FrmOpenData frm = new FrmOpenData(this, false);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }

    private void jButton_DataInfoActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmDataInfo frmDI = new FrmDataInfo();
        frmDI.setLocationRelativeTo(this);
        frmDI.setText(_meteoDataInfo.getInfoText());
        frmDI.setVisible(true);
    }

    private void jButton_DrawActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        //String fieldName = this.jComboBox_Variable.getSelectedItem().toString();
        display();
    }

    private void jComboBox_DrawTypeActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (this.jComboBox_DrawType.getItemCount() > 0) {
            _2DDrawType = DrawType2D.valueOf(this.jComboBox_DrawType.getSelectedItem().toString());
            this.jButton_Draw.setEnabled(true);
            _useSameLegendScheme = false;

            //_useSameGridInterSet = false;
            this.jButton_Animator.setEnabled(false);
            this.jButton_CreateAnimatorFile.setEnabled(false);
            this.jButton_PreTime.setEnabled(false);
            this.jButton_NexTime.setEnabled(false);

            switch (_meteoDataInfo.getDataType()) {
                case MICAPS_7:
                case HYSPLIT_Traj:
                    this.jButton_Animator.setEnabled(true);
                    this.jButton_CreateAnimatorFile.setEnabled(true);
                    break;
            }

            //Set CHB_ColorVar visible
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("bundle/Bundle_FrmMeteoData");
            switch (_2DDrawType) {
                case Vector:
                case Barb:
                    this.jCheckBox_ColorVar.setText(bundle.getString("FrmMeteoData.jCheckBox_ColorVar.text"));
                    this.jCheckBox_ColorVar.setVisible(true);
                    this.jCheckBox_ColorVar.setSelected(this.windColor);
                    break;
                case Contour:
                case Shaded:
                    this.jCheckBox_ColorVar.setText(bundle.getString("FrmMeteoData.jCheckBox_Smooth.text"));
                    this.jCheckBox_ColorVar.setVisible(true);
                    this.jCheckBox_ColorVar.setSelected(this.smooth);
                    break;
                default:
                    this.jCheckBox_ColorVar.setVisible(false);
                    break;
            }
        }
    }

    private void jButton_ViewDataActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (_meteoDataInfo.isGridData()) {
            viewGridData();
        } else if (_meteoDataInfo.isStationData()) {
            viewStationData();
            if (_gridData.xArray != null && _gridData.yArray != null) {
                if (_gridData.getXNum() > 0 && _gridData.getYNum() > 0) {
                    viewGridData();
                }
            }
        }
    }

    private void viewGridData() {
        if (_gridData == null) {
            return;
        }

        if (_gridData.data == null) {
            return;
        }

        if (_gridData.data.length == 0) {
            return;
        }

        FrmViewData frmData = new FrmViewData();
        //frmData.setProjectionInfo(_meteoDataInfo.getProjectionInfo());
        frmData.setGridData(_gridData);
        frmData.setLocationRelativeTo(this);
        frmData.setVisible(true);
    }

    private void viewStationData() {
        if (_stationData == null) {
            return;
        }

        if (_stationData.data.length == 0) {
            return;
        }

        String[] colNames = new String[]{"Stid", "Longitude", "Latitude", this.jComboBox_Variable.getSelectedItem().toString()};
        FrmViewData frmData = new FrmViewData(colNames);
        //frmData.setProjectionInfo(_meteoDataInfo.getProjectionInfo());
        frmData.setStationData(_stationData);
        frmData.setLocationRelativeTo(this._parent);
        frmData.setVisible(true);
    }

    private void jButton_ClearDrawActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        //Remove last layer            
        _parent.getMapDocument().getActiveMapFrame().removeLayerByHandle(_lastAddedLayerHandle);
        //this.jButton_Draw.setEnabled(true);
    }

    private void jButton_PreTimeActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _useSameLegendScheme = true;
        _useSameGridInterSet = true;
        switch (_meteoDataInfo.getDataType()) {            
            case MICAPS_2:
            case MICAPS_3:
            case MICAPS_4:
            case MICAPS_7:
            case MICAPS_11:
            case MICAPS_13:
                LocalDateTime tt = _meteoDataInfo.getDataInfo().getTimes().get(0);
                tt = tt.minusHours(3);
                String aFile = _meteoDataInfo.getFileName();
                String path = new File(aFile).getParent();
                DateTimeFormatter format = DateTimeFormatter.ofPattern("yyMMddHH");
                for (int i = 0; i < 100; i++) {
                    aFile = path + File.separator + format.format(tt) + ".000";
                    if (new File(aFile).exists()) {
                        break;
                    }
                    tt = tt.minusHours(3);
                }
                if (new File(aFile).exists()) {
                    _meteoDataInfo.openMICAPSData(aFile);
                    DefaultListModel listModel = (DefaultListModel) this.jList_DataFiles.getModel();
                    listModel.set(this.jList_DataFiles.getSelectedIndex(), new File(aFile).getName());
                    format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    this.jComboBox_Time.removeAllItems();
                    this.jComboBox_Time.addItem(format.format(tt));
                }
                break;
            case MICAPS_1:
            case MICAPS_120:
                tt = _meteoDataInfo.getDataInfo().getTimes().get(0);
                tt = tt.minusHours(1);
                aFile = _meteoDataInfo.getFileName();
                path = new File(aFile).getParent();
                switch (_meteoDataInfo.getDataType()) {
                    case MICAPS_120:
                        format = DateTimeFormatter.ofPattern("yyyyMMddHH");
                        break;
                    default:
                        format = DateTimeFormatter.ofPattern("yyMMddHH");
                        break;
                }   
                for (int i = 0; i < 100; i++) {
                    aFile = path + File.separator + format.format(tt) + ".000";
                    if (new File(aFile).exists()) {
                        break;
                    }
                    tt = tt.minusHours(1);
                }
                if (new File(aFile).exists()) {
                    _meteoDataInfo.openMICAPSData(aFile);
                    DefaultListModel listModel = (DefaultListModel) this.jList_DataFiles.getModel();
                    listModel.set(this.jList_DataFiles.getSelectedIndex(), new File(aFile).getName());
                    format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    this.jComboBox_Time.removeAllItems();
                    this.jComboBox_Time.addItem(format.format(tt));
                }
                break;
            case ISH:
//                    aTime = ((ISHDataInfo)_meteoDataInfo.DataInfo).dateTime.AddHours(-1);
//                    aFile = ((ISHDataInfo)_meteoDataInfo.DataInfo).fileName;
//                    for (int i = 0; i < 100; i++)
//                    {
//                        aFile = Path.Combine(Path.GetDirectoryName(aFile), "ISH_" + aTime.ToString("yyyyMMddHH") + ".txt");
//                        if (File.Exists(aFile))
//                        {
//                            break;
//                        }
//                        aTime = aTime.AddHours(-1);
//                    }
//                    if (File.Exists(aFile))
//                    {
//                        OpenISHFile(aFile, true);
//                    }
                break;
            default:
                if (this.jComboBox_Time.getSelectedIndex() > 0) {
                    this.jComboBox_Time.setSelectedIndex(this.jComboBox_Time.getSelectedIndex() - 1);
                } else {
                    this.jComboBox_Time.setSelectedIndex(this.jComboBox_Time.getItemCount() - 1);
                }
                break;
        }

        _parent.getMapDocument().getActiveMapFrame().getMapView().setLockViewUpdate(true);
        //Remove last layer
        _parent.getMapDocument().getActiveMapFrame().removeLayerByHandle(_lastAddedLayerHandle);

        _parent.getMapDocument().getActiveMapFrame().getMapView().setLockViewUpdate(false);
        this.jButton_Draw.doClick();
    }

    private void jButton_NexTimeActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        _useSameLegendScheme = true;
        _useSameGridInterSet = true;
        switch (_meteoDataInfo.getDataType()) {
            case MICAPS_2:
            case MICAPS_3:
            case MICAPS_4:
            case MICAPS_7:
            case MICAPS_11:
            case MICAPS_13:
                LocalDateTime tt = _meteoDataInfo.getDataInfo().getTimes().get(0);
                tt = tt.plusHours(3);
                String aFile = _meteoDataInfo.getFileName();
                String path = new File(aFile).getParent();
                DateTimeFormatter format = DateTimeFormatter.ofPattern("yyMMddHH");
                for (int i = 0; i < 100; i++) {
                    aFile = path + File.separator + format.format(tt) + ".000";
                    if (new File(aFile).exists()) {
                        break;
                    }
                    tt = tt.plusHours(3);
                }
                if (new File(aFile).exists()) {
                    _meteoDataInfo.openMICAPSData(aFile);
                    DefaultListModel listModel = (DefaultListModel) this.jList_DataFiles.getModel();
                    listModel.set(this.jList_DataFiles.getSelectedIndex(), new File(aFile).getName());
                    format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    this.jComboBox_Time.removeAllItems();
                    this.jComboBox_Time.addItem(format.format(tt));
                }
                break;
            case MICAPS_1:
            case MICAPS_120:
                tt = _meteoDataInfo.getDataInfo().getTimes().get(0);
                tt = tt.plusHours(1);
                aFile = _meteoDataInfo.getFileName();
                path = new File(aFile).getParent();
                switch (_meteoDataInfo.getDataType()) {
                    case MICAPS_120:
                        format = DateTimeFormatter.ofPattern("yyyyMMddHH");
                        break;
                    default:
                        format = DateTimeFormatter.ofPattern("yyMMddHH");
                        break;
                }                
                for (int i = 0; i < 100; i++) {
                    aFile = path + File.separator + format.format(tt) + ".000";
                    if (new File(aFile).exists()) {
                        break;
                    }
                    tt = tt.plusHours(1);
                }
                if (new File(aFile).exists()) {
                    _meteoDataInfo.openMICAPSData(aFile);
                    DefaultListModel listModel = (DefaultListModel) this.jList_DataFiles.getModel();
                    listModel.set(this.jList_DataFiles.getSelectedIndex(), new File(aFile).getName());
                    format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    this.jComboBox_Time.removeAllItems();
                    this.jComboBox_Time.addItem(format.format(tt));
                }
                break;
            case ISH:
//                    aTime = ((ISHDataInfo)_meteoDataInfo.DataInfo).dateTime.AddHours(1);
//                    aFile = ((ISHDataInfo)_meteoDataInfo.DataInfo).fileName;
//                    for (int i = 0; i < 100; i++)
//                    {
//                        aFile = Path.Combine(Path.GetDirectoryName(aFile), "ISH_" + aTime.ToString("yyyyMMddHH") + ".txt");
//                        if (File.Exists(aFile))
//                        {
//                            break;
//                        }
//                        aTime = aTime.AddHours(1);
//                    }
//                    if (File.Exists(aFile))
//                    {
//                        OpenISHFile(aFile, true);
//                    }
                break;
            default:
                if (this.jComboBox_Time.getSelectedIndex() < this.jComboBox_Time.getItemCount() - 1) {
                    this.jComboBox_Time.setSelectedIndex(this.jComboBox_Time.getSelectedIndex() + 1);
                } else {
                    this.jComboBox_Time.setSelectedIndex(0);
                }
                break;
        }

        _parent.getMapDocument().getActiveMapFrame().getMapView().setLockViewUpdate(true);
        //Remove last layer            
        _parent.getMapDocument().getActiveMapFrame().removeLayerByHandle(_lastAddedLayerHandle);

        _parent.getMapDocument().getActiveMapFrame().getMapView().setLockViewUpdate(false);
        this.jButton_Draw.doClick();
    }

    private void jButton_AnimatorActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (this._isRunning) {
            this._enableAnimation = false;
        } else {
            run_Animation(false);
            //this._enableAnimation = true;
            //this._isRunning = false;
            //this.jButton_Animator.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/animation-1.png")));
        }
    }

    private void run_Animation(final boolean isCreateFile) {
        File file;
        final AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        if (isCreateFile) {
            JFileChooser aDlg = new JFileChooser();
            String[] fileExts = new String[]{"gif"};
            GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "Gif File (*.gif)");
            aDlg.setFileFilter(mapFileFilter);
            File dir = new File(System.getProperty("user.dir"));
            if (dir.isDirectory()) {
                aDlg.setCurrentDirectory(dir);
            }
            aDlg.setAcceptAllFileFilterUsed(false);
            if (aDlg.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                file = aDlg.getSelectedFile();
                System.setProperty("user.dir", file.getParent());
                String extent = ((GenericFileFilter) aDlg.getFileFilter()).getFileExtent();
                String fileName = file.getAbsolutePath();
                if (!fileName.substring(fileName.length() - extent.length()).equals(extent)) {
                    fileName = fileName + "." + extent;
                }
                encoder.setRepeat(0);
                encoder.setDelay(1000);
                encoder.start(fileName);
            }
        }

        switch (_meteoDataInfo.getDataType()) {
            case MICAPS_7:
            case HYSPLIT_Traj:
                MICAPS7DataInfo aM7DataInfo = (MICAPS7DataInfo) _meteoDataInfo.getDataInfo();
                List<List<Object>> trajPoints = aM7DataInfo.getATrajData(this.jComboBox_Time.getSelectedIndex());
                Graphics2D g = (Graphics2D) _parent.getMapDocument().getActiveMapFrame().getMapView().getGraphics();
                PointF prePoint = new PointF();
                for (int i = 0; i < trajPoints.size(); i++) {
//                        if (!_enableAnimation)
//                            return;

                    List<Object> pList = trajPoints.get(i);
                    PointBreak aPB = new PointBreak();
                    aPB.setStyle(PointStyle.Circle);
                    aPB.setColor(Color.red);
                    aPB.setOutlineColor(Color.black);
                    aPB.setSize(10);
                    aPB.setDrawOutline(true);
                    aPB.setDrawFill(true);
                    PointF aPoint = new PointF();
                    PointD aPD = (PointD) pList.get(0);
                    double[] sxy = _parent.getMapDocument().getActiveMapFrame().getMapView().lonLatToScreen(aPD.X, aPD.Y);
                    aPoint.X = (float) sxy[0];
                    aPoint.Y = (float) sxy[1];

                    g.setColor(Color.blue);
                    Font wFont = new Font("Weather", Font.PLAIN, 12);
                    String text = String.valueOf((char) (186));
                    FontMetrics metrics = g.getFontMetrics(wFont);
                    Dimension sf = new Dimension(metrics.stringWidth(text), metrics.getHeight());
                    PointF sPoint = aPoint;
                    sPoint.X = aPoint.X - (float) sf.getWidth() / 2;
                    sPoint.Y = aPoint.Y - (float) sf.getHeight() / 2;
                    g.setFont(wFont);
                    g.drawString(text, sPoint.X, sPoint.Y + metrics.getHeight() * 3 / 4);
//                        if (ifCreateFile)
//                            bmG.DrawString(text, wFont, aBrush, sPoint);

                    if (i > 0) {
                        g.setColor(Color.red);
                        BasicStroke stroke = new BasicStroke(2);
                        g.setStroke(stroke);
                        g.draw(new Line2D.Float(prePoint.X, prePoint.Y, aPoint.X, aPoint.Y));
//                            if (ifCreateFile) {
//                                bmG.DrawLine(aPen, prePoint, aPoint);
//                            }
                    }

                    prePoint = aPoint;
                    //                        if (ifCreateFile)
                    //                        {
                    //                            String pFile = aFile.replace(".gif", String.valueOf(i) + ".gif");                           
                    //                            aBitmap.Save(pFile, System.Drawing.Imaging.ImageFormat.Gif);
                    //                            fileList.Add(pFile);
                    //                        }
                    //                        else  
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FrmMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            default:
                if (this.jComboBox_Time.getItemCount() > 1) {
                    SwingWorker worker = new SwingWorker<String, String>() {
                        @Override
                        protected String doInBackground() throws Exception {
                            //jButton_Animator.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/stop.png")));
                            jButton_Animator.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/stop.svg"));
                            _isRunning = true;
                            _useSameLegendScheme = true;
                            _useSameGridInterSet = true;
                            for (int i = 0; i < jComboBox_Time.getItemCount(); i++) {
                                if (!_enableAnimation) {
                                    _enableAnimation = true;
                                    _isRunning = false;
                                    //jButton_Animator.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/animation-1.png")));
                                    jButton_Animator.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/animator.svg"));
                                    return "";
                                }

                                jComboBox_Time.setSelectedIndex(i);
                                _parent.getMapDocument().getActiveMapFrame().getMapView().setLockViewUpdate(true);
                                //Remove last layer
                                _parent.getMapDocument().getActiveMapFrame().removeLayerByHandle(_lastAddedLayerHandle);
                                _parent.getMapDocument().getActiveMapFrame().getMapView().setLockViewUpdate(false);
                                display();

                                if (isCreateFile) {
                                    if (_parent.getMainTab().getSelectedIndex() == 0) {
                                        encoder.addFrame(_parent.getMapDocument().getActiveMapFrame().getMapView().getViewImage());
                                    } else {
                                        encoder.addFrame(_parent.getMapDocument().getMapLayout().getViewImage());
                                    }
                                } else {
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(FrmMeteoData.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }

                            _enableAnimation = true;
                            _isRunning = false;
                            //jButton_Animator.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/animation-1.png")));
                            jButton_Animator.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/animator.svg"));
                            encoder.finish();
                            return "";
                        }
                    };
                    worker.execute();
                }
                break;
        }
    }

    private void jButton_CreateAnimatorFileActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        run_Animation(true);
        this.setCursor(Cursor.getDefaultCursor());
    }

    private void jButton_DrawSettingActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        switch (_meteoDataInfo.getDataType()) {
//                case MICAPS_13:
//                //case MeteoDataType.AWX:
//                    MapLayer aLayer = _parent.getMapDocument().getActiveMapFrame().getMapView().getLayerFromHandle(_lastAddedLayerHandle);
//                    if (aLayer == null) {
//            return;
//        }
//
//                    OpenFileDialog aDlg = new OpenFileDialog();
//                    aDlg.Filter = "Palette File (*.pal)|*.pal";
//                    aDlg.InitialDirectory=Application .StartupPath +"\\pal";
//                    if (aDlg.ShowDialog() == DialogResult.OK)
//                    {
//                        RasterLayer aILayer = (RasterLayer)aLayer;
//                        aILayer.SetPalette(aDlg.FileName);
//                        //DrawMeteoData.SetPalette(aDlg.FileName, aILayer.Image);
//                        frmMain.CurrentWin.MapDocument.ActiveMapFrame.MapView.PaintLayers();
//                    }
//                    break;
            default:
                //MapLayer mLayer = _parent.getMapDocument().getActiveMapFrame().getMapView().getLayerByHandle(_lastAddedLayerHandle);
                FrmLegendSet aFrmLS = new FrmLegendSet(this, true);
                aFrmLS.setLegendScheme(_legendScheme);
                String fieldName = this.jComboBox_Variable.getSelectedItem().toString();
                aFrmLS.setLocationRelativeTo(this);
                aFrmLS.setVisible(true);
                if (aFrmLS.isOK()) {
                    _parent.getMapDocument().getActiveMapFrame().removeLayerByHandle(_lastAddedLayerHandle);
                    this._legendScheme = aFrmLS.getLegendScheme();
                    drawMeteoMap(false, _legendScheme, fieldName);
                    _parent.getMapDocument().getActiveMapFrame().getMapView().paintLayers();
                }
//                    else
//                    {
//                        if (aFrmLS.GetIsApplied())
//                        {
//                            frmMain.CurrentWin.MapDocument.ActiveMapFrame.MapView.RemoveLayerHandle(_lastAddedLayerHandle);
//                            DrawMeteoMap(false, _legendScheme, fieldName);
//                            frmMain.CurrentWin.MapDocument.ActiveMapFrame.MapView.PaintLayers();
//                        }
//                    }
                break;
        }
    }

    private void jButton_SettingActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (_meteoDataInfo.isStationData() || _meteoDataInfo.isSWATHData()) {
            switch (_2DDrawType) {
                case Contour:
                case Shaded:
                case Grid_Point:
                case Raster:
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                    FrmInterpolate frmInter = new FrmInterpolate(frame, true);
                    frmInter.setParameters(this._interpolationSetting);
                    frmInter.setLocationRelativeTo(this);
                    frmInter.setVisible(true);
                    if (frmInter.isOK()) {
                        this._interpolationSetting = frmInter.getParameters();
                        this.jButton_Draw.setEnabled(true);
                        _useSameGridInterSet = true;
                    }
                    break;
                case Weather_Symbol:
                    Object[] possibleValues = {"All Weather", "SDS", "SDS, Haze", "Smoke", "Haze", "Mist", "Smoke, Haze, Mist", "Fog"};
                    Object selectedValue = JOptionPane.showInputDialog(null,
                            "Choose one", "Input", JOptionPane.INFORMATION_MESSAGE,
                            null, possibleValues, this.weatherString);
                    if (selectedValue != null) {
                        this.weatherString = selectedValue.toString();
                        VectorLayer layer = DrawMeteoData.createWeatherSymbolLayer(_stationData, this.weatherString, "Weather");
                        layer.setProjInfo(this._meteoDataInfo.getProjectionInfo());
                        this._parent.getMapDocument().getActiveMapFrame().removeLayerByHandle(this._lastAddedLayerHandle);
                        this._lastAddedLayerHandle = this._parent.getMapDocument().getActiveMapFrame().addLayer(layer);
                    }
                    break;
                case Vector:
                case Barb:
                case Streamline:
                    this.meteoUVSet.setUV(false);
                    this.setMeteoUV();
                    break;
            }
        } else if (_meteoDataInfo.isGridData()) {
            switch (_2DDrawType) {
                case Contour:
                case Grid_Fill:
                case Grid_Point:
                case Shaded:
                case Raster:
//                        frmGridViewSet aFrmGVS = new frmGridViewSet();
//                        aFrmGVS.SetParameters(m_IfInterpolateGrid);
//                        if (aFrmGVS.ShowDialog() == DialogResult.OK)
//                        {
//                            m_IfInterpolateGrid = aFrmGVS.GetParameters();
//                        }
                    break;
                case Streamline:
                    String strmDen = JOptionPane.showInputDialog("Streamline density", 4);
                    int den = Integer.parseInt(strmDen);
                    if (den < 1 || den > 10) {
                        JOptionPane.showMessageDialog(null, "The streamline density must be set between 1 - 10");
                    } else {
                        _strmDensity = den;
                    }
                    break;
                case Vector:
                case Barb:
                    this.setMeteoUV();
                    break;
            }
        }
    }

    private void setMeteoUV() {
        if (this.meteoUVSet.getUDataInfo() == null || (!this._dataInfoList.contains(this.meteoUVSet.getUDataInfo()))){
            this.meteoUVSet.setUDataInfo(_meteoDataInfo);
            this.meteoUVSet.setVDataInfo(_meteoDataInfo);
            this.meteoUVSet.setFixUVStr(_meteoDataInfo.getMeteoUVSet().isFixUVStr());
            this.meteoUVSet.setUV(_meteoDataInfo.getMeteoUVSet().isUV());
            this.meteoUVSet.setUStr(_meteoDataInfo.getMeteoUVSet().getUStr());
            this.meteoUVSet.setVStr(_meteoDataInfo.getMeteoUVSet().getVStr());
        }
        FrmUVSet aFrmUVSet = new FrmUVSet((JFrame) SwingUtilities.getWindowAncestor(this), true);
        if (this.meteoUVSet.isUV()) {
            aFrmUVSet.setUV(true);
        } else {
            aFrmUVSet.setUV(false);
        }
        aFrmUVSet.setUVData(_dataInfoList);
        aFrmUVSet.setUData(this.meteoUVSet.getUDataInfo());
        aFrmUVSet.setVData(this.meteoUVSet.getVDataInfo());
        aFrmUVSet.setUStr(this.meteoUVSet.getUStr());
        aFrmUVSet.setVStr(this.meteoUVSet.getVStr());
        aFrmUVSet.setXSkip(_skipX);
        aFrmUVSet.setYSkip(_skipY);
        aFrmUVSet.setLocationRelativeTo(this);
        aFrmUVSet.setVisible(true);
        if (aFrmUVSet.isOK()) {
            String[] uvStr = aFrmUVSet.getUVItems();
            String uStr = uvStr[0];
            String vStr = uvStr[1];
            this.meteoUVSet.setUV(aFrmUVSet.isUV());
            this.meteoUVSet.setUDataInfo(aFrmUVSet.getUData());
            this.meteoUVSet.setUStr(uStr);
            this.meteoUVSet.setVDataInfo(aFrmUVSet.getVData());
            this.meteoUVSet.setVStr(vStr);
            this.meteoUVSet.setFixUVStr(true);
            _skipX = aFrmUVSet.getXSkip();
            _skipY = aFrmUVSet.getYSkip();
        } else {
            //JOptionPane.showMessageDialog(null, "U/V (Direction/Speed) variables were not set!");
        }
    }

    private void jButton_SectionPlotActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmSectionPlot afrm = new FrmSectionPlot(_meteoDataInfo);
        afrm.setLocationRelativeTo(this._parent);
        afrm.setVisible(true);
    }

    private void jButton_1DPlotActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        FrmOneDim afrm = new FrmOneDim(this._parent, _meteoDataInfo);
        afrm.setLocationRelativeTo(this._parent);
        afrm.setVisible(true);
    }

    private void jButton_RemoveAllDataActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        removeAllMeteoData();
    }

    private void jMenuTime_ArrivalTimeActionPerformed(ActionEvent evt) {
        String threshold = JOptionPane.showInputDialog(this, "Threshold value:", 0.0);
        if (threshold != null) {
            double th = Double.parseDouble(threshold);
            String varName = this.jComboBox_Variable.getEditor().getItem().toString();
            GridData gData = this._meteoDataInfo.getArrivalTimeData(varName, th);
            this.display(gData);
        }
    }

    private void jComboBox_TimeActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (this.jComboBox_Time.getItemCount() > 0) {
            _meteoDataInfo.setTimeIndex(this.jComboBox_Time.getSelectedIndex());
        }
    }

    private void jComboBox_LevelActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        //TSB_Draw.Enabled = true;
        this.jButton_Animator.setEnabled(false);
        this.jButton_PreTime.setEnabled(false);
        this.jButton_NexTime.setEnabled(false);
        _useSameLegendScheme = false;
        _meteoDataInfo.setLevelIndex(this.jComboBox_Level.getSelectedIndex());
    }

    private void jCheckBox_ColorVarActionPerformed(java.awt.event.ActionEvent evt) {
        switch (_2DDrawType) {
            case Vector:
            case Barb:
                this.windColor = this.jCheckBox_ColorVar.isSelected();
                break;
            case Contour:
            case Shaded:
                this.smooth = this.jCheckBox_ColorVar.isSelected();
                break;
        }
    }

    private void jCheckBox_Big_EndianActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        switch (_meteoDataInfo.getDataType()) {
            case GrADS_Grid:
            case GrADS_Station:
                GrADSDataInfo aDataInfo = (GrADSDataInfo) _meteoDataInfo.getDataInfo();
                aDataInfo.setBigEndian(this.jCheckBox_Big_Endian.isSelected());
                break;
        }
    }

    private String getStartupPath() {
        return System.getProperty("user.dir");

//        if (new File(this._parent.getCurrentDataFolder()).isDirectory()) {
//            return this._parent.getCurrentDataFolder();
//        }
//
//        File directory = new File(".");
//        String fn = "";
//        try {
//            fn = directory.getCanonicalPath();
//        } catch (IOException ex) {
//            Logger.getLogger(FrmMeteoData.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        String path = fn + File.separator + "sample";
//        //String path = "D:\\Temp";
//
//        return path;
    }

    /**
     * Open GrADS data file
     *
     * @param fileName GrADS data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openGrADSData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openGrADSData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open ARL data file
     *
     * @param fileName ARL data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openARLData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openARLData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open ASCII grid data file
     *
     * @param fileName ASCII grid data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openASCIIGridData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openASCIIGridData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open Surfer grid data file
     *
     * @param fileName Surfer grid data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openSurferGridData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openSurferGridData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open HYSPLIT concentration data file
     *
     * @param fileName HYSPLIT concentration data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openHYSPLITConcData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openHYSPLITConcData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open HYSPLIT particle data file
     *
     * @param fileName HYSPLIT particle data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openHYSPITPartData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openHYSPLITPartData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open HYSPLIT trajectory data file
     *
     * @param fileName HYSPLIT trajectory data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openHYSPLITTrajData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openHYSPLITTrajData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

//    /**
//     * Open HYSPLIT trajectory data files
//     *
//     * @param fileNames HYSPLIT trajectory data file names
//     * @return MeteoDataInfo
//     */
//    public MeteoDataInfo openHYSPLITTrajData(String[] fileNames) {
//        MeteoDataInfo aDataInfo = new MeteoDataInfo();
//        aDataInfo.openHYSPLITTrajData(fileNames);
//        addMeteoData(aDataInfo);
//
//        return aDataInfo;
//    }

    /**
     * Open NetCDF, GRIB, HDF... data file
     *
     * @param fileName NetCDF, GRIB, HDF... data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openNetCDFData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openNetCDFData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open Lon/Lat station data file
     *
     * @param fileName Lon/Lat station data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openLonLatData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openLonLatData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open MICAPS data file
     *
     * @param fileName MICAPS data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openMICAPSData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openMICAPSData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open MM5 output data file
     *
     * @param fileName MM5 output data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openMM5Data(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openMM5Data(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    /**
     * Open MM5 intermedia data file
     *
     * @param fileName MM5 intermedia data file name
     * @return MeteoDataInfo
     */
    public MeteoDataInfo openMM5IMData(String fileName) {
        MeteoDataInfo aDataInfo = new MeteoDataInfo();
        aDataInfo.openMM5IMData(fileName);
        addMeteoData(aDataInfo);

        return aDataInfo;
    }

    private void onGrADSDataClick(ActionEvent e) {
        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        //aDlg.setAcceptAllFileFilterUsed(false);
        String[] fileExts = new String[]{"ctl"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "GrADS Data (*.ctl)");
        aDlg.setFileFilter(mapFileFilter);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File file = aDlg.getSelectedFile();
            //this._parent.setCurrentDataFolder(file.getParent());
            System.setProperty("user.dir", file.getParent());

            MeteoDataInfo aDataInfo = new MeteoDataInfo();
            aDataInfo.openGrADSData(file.getAbsolutePath());
            addMeteoData(aDataInfo);
        }
    }

    private void onRemoveDataClick(ActionEvent e) {
        int selIdx = this.jList_DataFiles.getSelectedIndex();
        removeMeteoData(selIdx);
    }

    private void onARLDataClick(ActionEvent e) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        //aDlg.setAcceptAllFileFilterUsed(false);
        //String[] fileExts = new String[]{"ctl"};
        //GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "GrADS Data (*.ctl)");
        //aDlg.setFileFilter(mapFileFilter);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File file = aDlg.getSelectedFile();
            //this._parent.setCurrentDataFolder(file.getParent());
            System.setProperty("user.dir", file.getParent());

            MeteoDataInfo aDataInfo = new MeteoDataInfo();
            aDataInfo.openARLData(file.getAbsolutePath());
            addMeteoData(aDataInfo);
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void onASCIIGridDataClick(ActionEvent e) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        //aDlg.setAcceptAllFileFilterUsed(false);
        //String[] fileExts = new String[]{"ctl"};
        //GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "GrADS Data (*.ctl)");
        //aDlg.setFileFilter(mapFileFilter);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File file = aDlg.getSelectedFile();
            //this._parent.setCurrentDataFolder(file.getParent());
            System.setProperty("user.dir", file.getParent());

            MeteoDataInfo aDataInfo = new MeteoDataInfo();
            aDataInfo.openASCIIGridData(file.getAbsolutePath());
            aDataInfo.getDataInfo().setProjectionInfo(_parent.getMapDocument().getActiveMapFrame().getMapView().getProjection().getProjInfo());
            addMeteoData(aDataInfo);
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void onSurferGridDataClick(ActionEvent e) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        //aDlg.setAcceptAllFileFilterUsed(false);
        //String[] fileExts = new String[]{"ctl"};
        //GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "GrADS Data (*.ctl)");
        //aDlg.setFileFilter(mapFileFilter);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File file = aDlg.getSelectedFile();
            //this._parent.setCurrentDataFolder(file.getParent());
            System.setProperty("user.dir", file.getParent());

            MeteoDataInfo aDataInfo = new MeteoDataInfo();
            aDataInfo.openSurferGridData(file.getAbsolutePath());
            aDataInfo.getDataInfo().setProjectionInfo(_parent.getMapDocument().getActiveMapFrame().getMapView().getProjection().getProjInfo());
            addMeteoData(aDataInfo);
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void onHYSPLITConcDataClick(ActionEvent e) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        //aDlg.setAcceptAllFileFilterUsed(false);
        //String[] fileExts = new String[]{"ctl"};
        //GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "GrADS Data (*.ctl)");
        //aDlg.setFileFilter(mapFileFilter);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File file = aDlg.getSelectedFile();
            //this._parent.setCurrentDataFolder(file.getParent());
            System.setProperty("user.dir", file.getParent());

            MeteoDataInfo aDataInfo = new MeteoDataInfo();
            aDataInfo.openHYSPLITConcData(file.getAbsolutePath());
            addMeteoData(aDataInfo);
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void onHYSPLITPartDataClick(ActionEvent e) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File file = aDlg.getSelectedFile();
            //this._parent.setCurrentDataFolder(file.getParent());
            System.setProperty("user.dir", file.getParent());

            MeteoDataInfo aDataInfo = new MeteoDataInfo();
            aDataInfo.openHYSPLITPartData(file.getAbsolutePath());
            addMeteoData(aDataInfo);
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void onHYSPLITTrajDataClick(ActionEvent e) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        aDlg.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File[] files = aDlg.getSelectedFiles();
            System.setProperty("user.dir", files[0].getParent());
            for (File file : files) {
                MeteoDataInfo aDataInfo = new MeteoDataInfo();
                aDataInfo.openHYSPLITTrajData(file.getAbsolutePath());
                addMeteoData(aDataInfo);
            }
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void onNetCDFDataClick(ActionEvent e) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setMultiSelectionEnabled(true);
        aDlg.setCurrentDirectory(pathDir);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File[] files = aDlg.getSelectedFiles();
            System.setProperty("user.dir", files[0].getParent());
            for (File file : files) {
                MeteoDataInfo aDataInfo = new MeteoDataInfo();
                aDataInfo.openNetCDFData(file.getAbsolutePath());
                addMeteoData(aDataInfo);
            }
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void onLonLatStationsClick(ActionEvent e) {
        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        aDlg.setAcceptAllFileFilterUsed(false);
        String[] fileExts = new String[]{"csv"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "CSV File (*.csv)");
        aDlg.setFileFilter(mapFileFilter);
        fileExts = new String[]{"txt"};
        mapFileFilter = new GenericFileFilter(fileExts, "Text File (*.txt)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File file = aDlg.getSelectedFile();
            //this._parent.setCurrentDataFolder(file.getParent());
            System.setProperty("user.dir", file.getParent());

            MeteoDataInfo aDataInfo = new MeteoDataInfo();
            aDataInfo.openLonLatData(file.getAbsolutePath());
            addMeteoData(aDataInfo);
        }
    }

    private void onSYNOPClick(ActionEvent e) {
        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File file = aDlg.getSelectedFile();
            //this._parent.setCurrentDataFolder(file.getParent());
            System.setProperty("user.dir", file.getParent());

            MeteoDataInfo aDataInfo = new MeteoDataInfo();
            String stFile = this._parent.getStartupPath() + "\\station\\SYNOP_Stations.csv";
            aDataInfo.openSYNOPData(file.getAbsolutePath(), stFile);
            addMeteoData(aDataInfo);
        }
    }

    private void onMETARClick(ActionEvent e) {
        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File file = aDlg.getSelectedFile();
            //this._parent.setCurrentDataFolder(file.getParent());
            System.setProperty("user.dir", file.getParent());

            MeteoDataInfo aDataInfo = new MeteoDataInfo();
            String stFile = this._parent.getStartupPath() + "\\station\\METAR_Stations.csv";
            aDataInfo.openMETARData(file.getAbsolutePath(), stFile);
            addMeteoData(aDataInfo);
        }
    }

    private void onMICAPSDataClick(ActionEvent e) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File file = aDlg.getSelectedFile();
            //this._parent.setCurrentDataFolder(file.getParent());
            System.setProperty("user.dir", file.getParent());

            MeteoDataInfo aDataInfo = new MeteoDataInfo();
            aDataInfo.openMICAPSData(file.getAbsolutePath());
            addMeteoData(aDataInfo);
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void onMM5DataClick(ActionEvent e) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File file = aDlg.getSelectedFile();
            //this._parent.setCurrentDataFolder(file.getParent());
            System.setProperty("user.dir", file.getParent());

            MeteoDataInfo aDataInfo = new MeteoDataInfo();
            aDataInfo.openMM5Data(file.getAbsolutePath());
            addMeteoData(aDataInfo);
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void onMM5IMDataClick(ActionEvent e) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File file = aDlg.getSelectedFile();
            //this._parent.setCurrentDataFolder(file.getParent());
            System.setProperty("user.dir", file.getParent());

            MeteoDataInfo aDataInfo = new MeteoDataInfo();
            aDataInfo.openMM5IMData(file.getAbsolutePath());
            addMeteoData(aDataInfo);
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void onAWXDataClick(ActionEvent e) {
        String path = this.getStartupPath();
        File pathDir = new File(path);

        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        aDlg.setAcceptAllFileFilterUsed(false);
        String[] fileExts = new String[]{"awx"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "AWX Data (*.awx)");
        aDlg.setFileFilter(mapFileFilter);
        aDlg.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File[] files = aDlg.getSelectedFiles();
            System.setProperty("user.dir", files[0].getParent());
            for (File file : files) {
                MeteoDataInfo aDataInfo = new MeteoDataInfo();
                aDataInfo.openAWXData(file.getAbsolutePath());
                addMeteoData(aDataInfo);
            }
        }
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get last add layer
     *
     * @return Last added layer
     */
    public MapLayer getLastAddLayer() {
        MapLayer layer = null;
        if (this._lastAddedLayerHandle >= 0) {
            layer = this._parent.getMapView().getLayerByHandle(this._lastAddedLayerHandle);
        }

        return layer;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // <editor-fold desc="MeteoDataInfo">

    /**
     * Add a meteo data info
     *
     * @param aDataInfo The meteo data info
     */
    public void addMeteoData(MeteoDataInfo aDataInfo) {
        _dataInfoList.add(aDataInfo);
        DefaultListModel listModel = (DefaultListModel) this.jList_DataFiles.getModel();
        listModel.addElement(new File(aDataInfo.getFileName()).getName());
        this.jList_DataFiles.setModel(listModel);
        this.jList_DataFiles.setSelectedIndex(listModel.size() - 1);
        _meteoDataInfo = _dataInfoList.get(this.jList_DataFiles.getSelectedIndex());
        updateParameters();
    }

    private void updateParameters() {
        int i;
        DataInfo aDataInfo = _meteoDataInfo.getDataInfo();
        switch (_meteoDataInfo.getDataType()) {
            case GrADS_Grid:
            case GrADS_Station:
                this.jCheckBox_Big_Endian.setVisible(true);
                this.jCheckBox_Big_Endian.setSelected(((GrADSDataInfo) aDataInfo).isBigEndian());
                break;
            default:
                this.jCheckBox_Big_Endian.setVisible(false);
                break;
        }
        String dataType = _meteoDataInfo.getDataType().toString();
        if (_meteoDataInfo.getDataType() == MeteoDataType.NetCDF) {
            dataType = ((NetCDFDataInfo) _meteoDataInfo.getDataInfo()).getFileTypeId();
        }
        this.setTitle(this.getTitle().split("-")[0].trim() + " - " + dataType);

        this.jPanel_DataSet.setEnabled(true);
        for (Component aItem : this.jToolBar1.getComponents()) {
            aItem.setEnabled(true);
        }
        this.jButton_DrawSetting.setEnabled(false);
        this.jButton_Animator.setEnabled(false);
        this.jButton_PreTime.setEnabled(false);
        this.jButton_NexTime.setEnabled(false);
        this.jButton_Setting.setEnabled(true);
        switch (_meteoDataInfo.getDataType()) {
            case HYSPLIT_Traj:
                this.jButton_SectionPlot.setEnabled(false);
                break;
        }

        //Projection
        updateProjection();

        //Set draw type
        this.jComboBox_DrawType.removeAllItems();
        if (_meteoDataInfo.isGridData()) {
            this.jComboBox_DrawType.addItem(DrawType2D.Raster.toString());
            this.jComboBox_DrawType.addItem(DrawType2D.Contour.toString());
            this.jComboBox_DrawType.addItem(DrawType2D.Shaded.toString());
            this.jComboBox_DrawType.addItem(DrawType2D.Grid_Fill.toString());
            this.jComboBox_DrawType.addItem(DrawType2D.Grid_Point.toString());
            this.jComboBox_DrawType.addItem(DrawType2D.Vector.toString());
            this.jComboBox_DrawType.addItem(DrawType2D.Barb.toString());
            this.jComboBox_DrawType.addItem(DrawType2D.Streamline.toString());
        } else if (_meteoDataInfo.isStationData()) {
            switch (_meteoDataInfo.getDataType()) {
                case HYSPLIT_Particle:
                    this.jComboBox_DrawType.addItem(DrawType2D.Station_Point.toString());
                    break;
                default:
                    this.jComboBox_DrawType.addItem(DrawType2D.Station_Point.toString());
                    this.jComboBox_DrawType.addItem(DrawType2D.Station_Info.toString());
                    this.jComboBox_DrawType.addItem(DrawType2D.Weather_Symbol.toString());
                    this.jComboBox_DrawType.addItem(DrawType2D.Station_Model.toString());
                    this.jComboBox_DrawType.addItem(DrawType2D.Vector.toString());
                    this.jComboBox_DrawType.addItem(DrawType2D.Barb.toString());
                    this.jComboBox_DrawType.addItem(DrawType2D.Contour.toString());
                    this.jComboBox_DrawType.addItem(DrawType2D.Shaded.toString());
                    this.jComboBox_DrawType.addItem(DrawType2D.Streamline.toString());
                    break;
            }
        } else if (_meteoDataInfo.isSWATHData()) {
            this.jComboBox_DrawType.addItem(DrawType2D.Raster.toString());
            this.jComboBox_DrawType.addItem(DrawType2D.Station_Point.toString());
        } else {
            this.jComboBox_DrawType.addItem(DrawType2D.Traj_Line.toString());
            this.jComboBox_DrawType.addItem(DrawType2D.Traj_Point.toString());
            this.jComboBox_DrawType.addItem(DrawType2D.Traj_StartPoint.toString());
        }
        this.jComboBox_DrawType.setSelectedIndex(0);

        //Set vars
        _isLoading = true;
        this.jComboBox_Variable.setEnabled(true);
        this.jComboBox_Variable.removeAllItems();
        List<String> varNames = new ArrayList<>();
        for (i = 0; i < aDataInfo.getVariables().size(); i++) {
            Variable var = aDataInfo.getVariables().get(i);
            if (_meteoDataInfo.isSWATHData()) {
                Variable lonvar = _meteoDataInfo.getDataInfo().getVariable("longitude");
                if (lonvar != null) {
                    if (var.dimensionContains(lonvar)) {
                        this.jComboBox_Variable.addItem(var.getName());
                        varNames.add(var.getName());
                    }
                }
            } else {
                if (var.isPlottable()) {
                    this.jComboBox_Variable.addItem(var.getName());
                    varNames.add(var.getName());
                }
            }
        }
        _isLoading = false;
        if (_meteoDataInfo.getVariableName() == null) {
            this.jComboBox_Variable.setSelectedIndex(0);
        } else {
            if (varNames.contains(_meteoDataInfo.getVariableName()))
                this.jComboBox_Variable.setSelectedItem(_meteoDataInfo.getVariableName());
            else
                this.jComboBox_Variable.setSelectedIndex(0);
        }
    }

    private void updateProjection() {
        ProjectionInfo dataProj = _meteoDataInfo.getProjectionInfo();
        ProjectionInfo mapViewProj = _parent.getMapDocument().getActiveMapFrame().getMapView().getProjection().getProjInfo();
        if (!dataProj.equals(mapViewProj)) {
            if (JOptionPane.showConfirmDialog(null, "Different projection! If project?", "Conform", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                _parent.getMapDocument().getActiveMapFrame().getMapView().projectLayers(_meteoDataInfo.getProjectionInfo());
            }
        }
    }

    /**
     * Remove a meteorological data by index
     *
     * @param idx Index
     */
    public void removeMeteoData(int idx) {
        _dataInfoList.remove(idx);
        if (this.jList_DataFiles.getModel().getSize() > 1) {
            if (idx >= 1) {
                this.jList_DataFiles.setSelectedIndex(idx - 1);
            } else {
                this.jList_DataFiles.setSelectedIndex(0);
            }
        }
        ((DefaultListModel) this.jList_DataFiles.getModel()).remove(idx);

        if (_dataInfoList.isEmpty()) {
            initialize();
        }
    }

    /**
     * Remove a meteorological data
     *
     * @param aDataInfo Meteo data
     */
    public void removeMeteoData(MeteoDataInfo aDataInfo) {
        _dataInfoList.remove(aDataInfo);
        if (_dataInfoList.isEmpty()) {
            initialize();
        }
    }

    /**
     * Remove all meteo data
     */
    public void removeAllMeteoData() {
        _dataInfoList.clear();
        ((DefaultListModel) this.jList_DataFiles.getModel()).removeAllElements();
        initialize();
    }
    // </editor-fold>

    // <editor-fold desc="Display - Crate data layer">
    /**
     * Display - Draw meteo data
     *
     * @return Map layer
     */
    public MapLayer display() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        //this.jButton_Draw.setEnabled(false);
        this.jButton_ViewData.setEnabled(true);
        this.jButton_DrawSetting.setEnabled(true);
        if (this.jComboBox_Time.getItemCount() > 1) {
            this.jButton_NexTime.setEnabled(true);
            this.jButton_PreTime.setEnabled(true);
            this.jButton_Animator.setEnabled(true);
            this.jButton_CreateAnimatorFile.setEnabled(true);
        }
        this.jButton_Setting.setEnabled(true);

        _meteoDataInfo.setDimensionSet(PlotDimension.Lat_Lon);

        MapLayer aLayer = null;
        String fieldName = "";
        if (this.jComboBox_Variable.getSelectedItem() != null) {
            fieldName = this.jComboBox_Variable.getEditor().getItem().toString();
            //fieldName = this.jComboBox_Variable.getSelectedItem().toString();
        }

        if (_meteoDataInfo.isGridData()) {
            aLayer = drawGrid(fieldName);
        } else if (_meteoDataInfo.isStationData()) {
            aLayer = drawStation(fieldName);
            _useSameLegendScheme = true;
        } else if (_meteoDataInfo.isTrajData()) {
            aLayer = drawTraj();
            this.jButton_DrawSetting.setEnabled(false);
            this.jButton_NexTime.setEnabled(false);
            this.jButton_PreTime.setEnabled(false);
        } else if (_meteoDataInfo.isSWATHData()) {
            aLayer = drawStation(fieldName);
        }

        switch (_meteoDataInfo.getDataType()) {
            case MICAPS_1:
            case MICAPS_2:
            case MICAPS_3:
            case MICAPS_4:
            case MICAPS_7:
            case MICAPS_11:
            case MICAPS_13:
            case MICAPS_120:
                this.jButton_NexTime.setEnabled(true);
                this.jButton_PreTime.setEnabled(true);
                break;
        }

        this.setCursor(Cursor.getDefaultCursor());

        return aLayer;
    }

    private MapLayer display(GridData gData) {
        _gridData = gData;

        if (_gridData == null) {
            return null;
        }

        if (_useSameLegendScheme) {
            if (_legendScheme.getLegendType() == LegendType.GraduatedColor) {
                double[] maxmin = new double[2];
                boolean hasUndef = _gridData.getMaxMinValue(maxmin);
                double minValue = maxmin[1];
                double maxValue = maxmin[0];
                if (Double.parseDouble(_legendScheme.getLegendBreaks().get(0).getStartValue().toString())
                        < Double.parseDouble(_legendScheme.getLegendBreaks().get(0).getEndValue().toString())) {
                    if (minValue < Double.parseDouble(_legendScheme.getLegendBreaks().get(0).getStartValue().toString())) {
                        _legendScheme.getLegendBreaks().get(0).setStartValue(minValue);
                    }
                    if (maxValue > Double.parseDouble(_legendScheme.getLegendBreaks().get(_legendScheme.getBreakNum() - 1).getEndValue().toString())) {
                        _legendScheme.getLegendBreaks().get(_legendScheme.getBreakNum() - 1).setEndValue(maxValue);
                    }
                } else {
                    if (maxValue > Double.parseDouble(_legendScheme.getLegendBreaks().get(0).getEndValue().toString())) {
                        _legendScheme.getLegendBreaks().get(0).setEndValue(maxValue);
                    }
                    if (minValue < Double.parseDouble(_legendScheme.getLegendBreaks().get(_legendScheme.getBreakNum() - 1).getStartValue().toString())) {
                        _legendScheme.getLegendBreaks().get(_legendScheme.getBreakNum() - 1).setStartValue(minValue);
                    }
                }
            }
        } else {
            createLegendScheme_Grid();
        }

        return drawMeteoMap_Grid(true, _legendScheme, "Data");
    }

    private MapLayer drawGrid(String fieldName) {
        //this.jComboBox_Variable.actionPerformed(null);
        //String vName = this.jComboBox_Variable.getSelectedItem().toString();
        String vName = fieldName;
        _gridData = _meteoDataInfo.getGridData(vName);

        if (_gridData == null) {
            return null;
        }

        if (_useSameLegendScheme) {
            if (_legendScheme.getLegendType() == LegendType.GraduatedColor) {
                double[] maxmin = new double[2];
                boolean hasUndef = _gridData.getMaxMinValue(maxmin);
                double minValue = maxmin[1];
                double maxValue = maxmin[0];
                if (Double.parseDouble(_legendScheme.getLegendBreaks().get(0).getStartValue().toString())
                        < Double.parseDouble(_legendScheme.getLegendBreaks().get(0).getEndValue().toString())) {
                    if (minValue < Double.parseDouble(_legendScheme.getLegendBreaks().get(0).getStartValue().toString())) {
                        _legendScheme.getLegendBreaks().get(0).setStartValue(minValue);
                    }
                    if (maxValue > Double.parseDouble(_legendScheme.getLegendBreaks().get(_legendScheme.getBreakNum() - 1).getEndValue().toString())) {
                        _legendScheme.getLegendBreaks().get(_legendScheme.getBreakNum() - 1).setEndValue(maxValue);
                    }
                } else {
                    if (maxValue > Double.parseDouble(_legendScheme.getLegendBreaks().get(0).getEndValue().toString())) {
                        _legendScheme.getLegendBreaks().get(0).setEndValue(maxValue);
                    }
                    if (minValue < Double.parseDouble(_legendScheme.getLegendBreaks().get(_legendScheme.getBreakNum() - 1).getStartValue().toString())) {
                        _legendScheme.getLegendBreaks().get(_legendScheme.getBreakNum() - 1).setStartValue(minValue);
                    }
                }
            }
        } else {
            createLegendScheme_Grid();
        }

        return drawMeteoMap_Grid(true, _legendScheme, fieldName);
    }

    private MapLayer drawStation(String fieldName) {
        //this.jComboBox_Variable.actionPerformed(null);
        String vName = fieldName;
        _stationData = _meteoDataInfo.getStationData(vName);

        Extent aExtent = _stationData.dataExtent;

        if (_stationData.getStNum() > 5 && aExtent.getWidth() > 0 && aExtent.getHeight() > 0) {
            if (!_useSameGridInterSet) {
                GridDataSetting aGDP = this._interpolationSetting.getGridDataSetting();
                aGDP.dataExtent = aExtent;
                //_interpolationSetting.setGridDataSetting(aGDP);
                _interpolationSetting.setRadius((float) ((aGDP.dataExtent.maxX
                        - aGDP.dataExtent.minX) / aGDP.xNum * 2));
                _useSameGridInterSet = true;
                if (_meteoDataInfo.isSWATHData()) {
                    _interpolationSetting.setInterpolationMethod(InterpolationMethods.AssignPointToGrid);
                    aGDP.xNum = 1000;
                    aGDP.yNum = 1000;
                }
            }
        }

        switch (_2DDrawType) {
            case Station_Point:
            case Vector:
            case Barb:
            case Weather_Symbol:
            case Station_Model:
            case Station_Info:
                if (!_useSameLegendScheme) {
                    createLegendScheme_Station();
                }
                return drawMeteoMap_Station(true, _legendScheme, fieldName);
            default:
                _gridData = _stationData.interpolateData(_interpolationSetting);
                if (!_useSameLegendScheme) {
                    createLegendScheme_Station();
                }
                return drawMeteoMap_Station(true, _legendScheme, fieldName);
        }
    }

    private MapLayer drawTraj() {
        DataInfo aDataInfo = _meteoDataInfo.getDataInfo();
        VectorLayer aLayer = null;
        switch (_2DDrawType) {
            case Traj_Line:
                aLayer = ((TrajDataInfo) aDataInfo).createTrajLineLayer();
//                PolylineBreak aPLB;
//                for (int i = 0; i < aLayer.getLegendScheme().getBreakNum(); i++) {
//                    aPLB = (PolylineBreak) aLayer.getLegendScheme().getLegendBreaks().get(i);
//                    aPLB.setSize(2);
//                }
                _lastAddedLayerHandle = _parent.getMapDocument().getActiveMapFrame().insertPolylineLayer(aLayer);
                break;
            case Traj_StartPoint:
                aLayer = ((TrajDataInfo) aDataInfo).createTrajStartPointLayer();
                PointBreak aPB = (PointBreak) aLayer.getLegendScheme().getLegendBreaks().get(0);
                aPB.setStyle(PointStyle.UpTriangle);
                _lastAddedLayerHandle = _parent.getMapDocument().getActiveMapFrame().insertPolylineLayer(aLayer);
                break;
            case Traj_Point:
                aLayer = ((TrajDataInfo) aDataInfo).createTrajPointLayer();
                _lastAddedLayerHandle = _parent.getMapDocument().getActiveMapFrame().insertPolylineLayer(aLayer);
                break;
        }

        return aLayer;
    }

    private void createLegendScheme_Grid() {
        switch (_2DDrawType) {
            case Contour:
                _legendScheme = LegendManage.createLegendSchemeFromGridData(_gridData,
                        LegendType.UniqueValue, ShapeTypes.Polyline);
                break;
            case Shaded:
            case Grid_Fill:
                _legendScheme = LegendManage.createLegendSchemeFromGridData(_gridData,
                        LegendType.GraduatedColor, ShapeTypes.Polygon);
                break;
            case Grid_Point:
                _legendScheme = LegendManage.createLegendSchemeFromGridData(_gridData,
                        LegendType.GraduatedColor, ShapeTypes.Point);
                break;
            case Vector:
            case Barb:
                if (this.jCheckBox_ColorVar.isSelected()) {
                    _legendScheme = LegendManage.createLegendSchemeFromGridData(_gridData,
                            LegendType.GraduatedColor, ShapeTypes.Point);
                    PointBreak aPB;
                    for (int i = 0; i < _legendScheme.getBreakNum(); i++) {
                        aPB = (PointBreak) _legendScheme.getLegendBreaks().get(i);
                        aPB.setSize(10);
                    }
                } else {
                    _legendScheme = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, Color.blue, 10);
                }
                break;
            case Streamline:
                _legendScheme = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polyline, Color.blue, 1);
                _legendScheme.setLegendBreak(0, new StreamlineBreak((PolylineBreak)_legendScheme.getLegendBreak(0)));
                break;
            case Raster:
                _legendScheme = LegendManage.createLegendSchemeFromGridData(_gridData, LegendType.GraduatedColor,
                        ShapeTypes.Image);
                break;
        }
    }

    private LegendScheme createLegendScheme_Station() {
        switch (_2DDrawType) {
            case Station_Point:
                _legendScheme = LegendManage.createLegendSchemeFromStationData(_stationData,
                        LegendType.GraduatedColor, ShapeTypes.Point);
                if (_meteoDataInfo.getDataType() == MeteoDataType.HDF) {
                    for (int i = 0; i < _legendScheme.getBreakNum(); i++) {
                        ((PointBreak) _legendScheme.getLegendBreaks().get(i)).setDrawOutline(false);
                    }
                }
                break;
            case Grid_Point:
                _legendScheme = LegendManage.createLegendSchemeFromGridData(_gridData,
                        LegendType.GraduatedColor, ShapeTypes.Point);
                break;
            case Contour:
                _legendScheme = LegendManage.createLegendSchemeFromGridData(_gridData,
                        LegendType.UniqueValue, ShapeTypes.Polyline);
                break;
            case Shaded:
                _legendScheme = LegendManage.createLegendSchemeFromGridData(_gridData,
                        LegendType.GraduatedColor, ShapeTypes.Polygon);
                break;
            case Raster:
                _legendScheme = LegendManage.createLegendSchemeFromGridData(_gridData, LegendType.GraduatedColor,
                        ShapeTypes.Image);
                break;
            case Vector:
            case Barb:
                if (this.jCheckBox_ColorVar.isSelected()) {
                    _legendScheme = LegendManage.createLegendSchemeFromStationData(_stationData,
                            LegendType.GraduatedColor, ShapeTypes.Point);
                    for (int i = 0; i < _legendScheme.getLegendBreaks().size(); i++) {
                        PointBreak aPB = (PointBreak) _legendScheme.getLegendBreaks().get(i);
                        aPB.setSize(10);
                    }
                } else {
                    _legendScheme = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, Color.blue, 10);
                }
                break;
            case Streamline:
                _legendScheme = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Polyline, Color.blue, 1);
                _legendScheme.setLegendBreak(0, new StreamlineBreak((PolylineBreak)_legendScheme.getLegendBreak(0)));
                break;
            case Weather_Symbol:
                _legendScheme = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, Color.blue, 12);
                break;
            case Station_Model:
                _legendScheme = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, Color.blue, 12);
                break;
            case Station_Info:
                _legendScheme = LegendManage.createSingleSymbolLegendScheme(ShapeTypes.Point, Color.red, 8);
                break;
        }

        return _legendScheme;
    }

    public MapLayer drawMeteoMap(boolean isNew, LegendScheme aLS, String fieldName) {
        if (_meteoDataInfo.isGridData()) {
            return drawMeteoMap_Grid(isNew, aLS, fieldName);
        } else if (_meteoDataInfo.isStationData() || _meteoDataInfo.isSWATHData()) {
            return drawMeteoMap_Station(isNew, aLS, fieldName);
        } else {
            return null;
        }
    }

    private MapLayer drawMeteoMap_Grid(boolean isNew, LegendScheme aLS, String fieldName) {
        MapLayer aLayer = new MapLayer();
        //VectorLayer aLayer = new VectorLayer(ShapeTypes.Point);
        String varName = this.jComboBox_Variable.getSelectedItem().toString();
//            if (CHB_NewVariable.Checked)
//                varName = "NewVar";
//
        String lName = "";
        if (this.jComboBox_Level.getItemCount() > 0) {
            lName = this.jComboBox_Level.getSelectedItem().toString();
        }
        if (this.jComboBox_Time.getItemCount() > 0) {
            if (!lName.isEmpty()) {
                lName = lName + "_";
            }
            lName = lName + this.jComboBox_Time.getSelectedItem().toString();
        }
        if (lName.isEmpty()) {
            lName = varName;
        } else {
            lName = varName + "_" + lName;
        }

        boolean ifAddLayer = true;

//            //Interpolate grid if set  
//            if (isNew)
//            {
//                switch (_2DDrawType)
//                {
//                    case DrawType2D.Contour:
//                    case DrawType2D.Shaded:
//                        if (m_IfInterpolateGrid)
//                        {
//                            _gridData = ContourDraw.Interpolate_Grid(_gridData);
//                        }
//                        break;
//                }
//            }
        //Extent to global if the data is global
        if (_gridData.isGlobal()) {
            _gridData.extendToGlobal();
        }

        //Create layer
        switch (_2DDrawType) {
            case Contour:
                lName = "Contour_" + lName;
                //LegendManage.setContoursAndColors(aLS, _cValues, _cColors);
                aLayer = DrawMeteoData.createContourLayer(_gridData, aLS, lName, fieldName, smooth);
                if (aLayer != null) {
                    ((VectorLayer) aLayer).getLabelSet().setShadowColor(_parent.getMapDocument().getActiveMapFrame().getMapView().getBackground());
                    ((VectorLayer) aLayer).addLabelsContourDynamic(_parent.getMapDocument().getActiveMapFrame().getMapView().getViewExtent());
                }
                break;
            case Shaded:
                lName = "Shaded_" + lName;
                //LegendManage.setContoursAndColors(aLS, _cValues, _cColors);
                aLayer = DrawMeteoData.createShadedLayer(_gridData, aLS, lName, fieldName, smooth);
                break;
            case Grid_Fill:
                lName = "GridFill_" + lName;
                //LegendManage.SetContoursAndColors(aLS, ref _cValues, ref _cColors);
                aLayer = DrawMeteoData.createGridFillLayer(_gridData, aLS, lName, fieldName);
                break;
            case Grid_Point:
                lName = "GridPoint_" + lName;
                //LegendManage.SetContoursAndColors(aLS, ref _cValues, ref _cColors);
                aLayer = DrawMeteoData.createGridPointLayer(_gridData, aLS, lName, fieldName);
                break;
            case Vector:
                GridData[] uvData = getUVGridData();
                if (uvData != null) {
                    GridData uData = uvData[0];
                    GridData vData = uvData[1];
                    lName = "Vector_" + lName;
                    if (this.windColor) {
                        if (_skipX != 1 || _skipY != 1) {
                            _gridData = _gridData.skip(_skipY, _skipX);
                        }
                    }
                    aLayer = DrawMeteoData.createGridVectorLayer(uData, vData, _gridData, aLS, this.windColor,
                            lName, this.meteoUVSet.isUV());
                } else {
                    ifAddLayer = false;
                }
                break;
            case Streamline:
                uvData = getUVGridData();
                if (uvData != null) {
                    GridData uData = uvData[0];
                    GridData vData = uvData[1];
                    if (uData.isGlobal()) {
                        uData.extendToGlobal();
                        vData.extendToGlobal();
                    }
                    //lName = lNameS;
                    lName = "Streamline_" + lName;
                    aLayer = DrawMeteoData.createStreamlineLayer(uData, vData, _strmDensity, aLS, lName, this.meteoUVSet.isUV());
                } else {
                    ifAddLayer = false;
                }
                break;
            case Barb:
                uvData = getUVGridData();
                if (uvData != null) {
                    GridData uData = uvData[0];
                    GridData vData = uvData[1];
                    lName = "Barb_" + lName;
                    if (this.windColor) {
                        if (_skipX != 1 || _skipY != 1) {
                            _gridData = _gridData.skip(_skipY, _skipX);
                        }
                    }
                    aLayer = DrawMeteoData.createGridBarbLayer(uData, vData, _gridData, aLS, this.windColor, lName, this.meteoUVSet.isUV());
                } else {
                    ifAddLayer = false;
                }
                break;
            case Raster:
                lName = "Raster_" + lName;
//                    if (_meteoDataInfo.getDataType() == MeteoDataType.MICAPS_13)
//                    {
//                        String aFile = Application.StartupPath + "\\pal\\I-01.pal";
//                        aLayer = DrawMeteoData.createRasterLayer(_gridData, lName, aFile);
//                    }
//                    else
                aLayer = DrawMeteoData.createRasterLayer(_gridData, lName, aLS);
                break;
        }

        if (!ifAddLayer || aLayer == null) {
            return null;
        }

        aLayer.setProjInfo(_meteoDataInfo.getProjectionInfo());
        if (aLayer.getLayerType() == LayerTypes.VectorLayer) {
            VectorLayer aVLayer = (VectorLayer) aLayer;
            aVLayer.setMaskout(true);
            if (aVLayer.getShapeType() == ShapeTypes.Polygon) {
                _lastAddedLayerHandle = _parent.getMapDocument().getActiveMapFrame().insertPolygonLayer(aVLayer);
            } else {
                if (_2DDrawType == DrawType2D.Vector || _2DDrawType == DrawType2D.Barb) {
                    _lastAddedLayerHandle = _parent.getMapDocument().getActiveMapFrame().addWindLayer(aVLayer,
                            _meteoDataInfo.EarthWind);
                } else {
                    _lastAddedLayerHandle = _parent.getMapDocument().getActiveMapFrame().insertPolylineLayer(aVLayer);
                }
            }
        } else {
            RasterLayer aILayer = (RasterLayer) aLayer;
            _lastAddedLayerHandle = _parent.getMapDocument().getActiveMapFrame().insertImageLayer(aILayer);
        }

        return aLayer;
    }

    public MapLayer drawMeteoMap_Station(boolean isNew, LegendScheme aLS, String fieldName) {
        boolean hasNoData = _hasUndefData;
        MapLayer aLayer = new MapLayer();
        //String LNameS = this.jComboBox_Level.getSelectedItem().toString() + "_" + this.jComboBox_Time.getSelectedItem().toString();
        String LNameM = this.jComboBox_Variable.getSelectedItem().toString() + "_";
        String LName = LNameM;
        if (_interpolationSetting.getInterpolationMethod() == InterpolationMethods.IDW_Neighbors) {
            hasNoData = false;
        }
        boolean ifAddLayer = true;
        switch (_2DDrawType) {
            case Station_Point:
                LegendManage.setContoursAndColors(aLS, _cValues, _cColors);
                LName = "StationPoint_" + LName;
                aLayer = DrawMeteoData.createSTPointLayer(_stationData, aLS, LName, fieldName);
                switch (_meteoDataInfo.getDataType()) {
                    case HYSPLIT_Particle:
                    case NetCDF:
                        for (ColorBreak cb : aLayer.getLegendScheme().getLegendBreaks()) {
                            ((PointBreak) cb).setDrawOutline(false);
                        }
                }
                break;
            case Grid_Point:
                LegendManage.setContoursAndColors(aLS, _cValues, _cColors);
                LName = "GridPoint_" + LName;
                aLayer = DrawMeteoData.createGridPointLayer(_gridData, aLS, LName, fieldName);
                break;
            case Contour:
                LegendManage.setContoursAndColors(aLS, _cValues, _cColors);
                LName = "Contour_" + LName;
                aLayer = DrawMeteoData.createContourLayer(_gridData, aLS, LName, fieldName, smooth);
                break;
            case Shaded:
                LegendManage.setContoursAndColors(aLS, _cValues, _cColors);
                LName = "Shaded_" + LName;
                aLayer = DrawMeteoData.createShadedLayer(_gridData, aLS, LName, fieldName, smooth);
                break;
            case Raster:
                LName = "Raster_" + LName;
                aLayer = DrawMeteoData.createRasterLayer(_gridData, LName, aLS);
                break;
            case Vector:
            case Barb:
            case Streamline:
                StationData[] stUVData = this.getUVStationData();
                if (stUVData != null) {
                    StationData stUData = stUVData[0];
                    StationData stVData = stUVData[1];
                    switch (_2DDrawType) {
                        case Vector:
                        case Barb:
                            if (this.windColor) {
                                LegendManage.setContoursAndColors(aLS, _cValues, _cColors);
                            }

                            if (_2DDrawType == DrawType2D.Vector) {
                                LName = "Vector_" + LName;
                                if (this.windColor) {
                                    aLayer = DrawMeteoData.createSTVectorLayer(stUData, stVData, _stationData,
                                            aLS, LName, this.meteoUVSet.isUV());
                                } else {
                                    aLayer = DrawMeteoData.createSTVectorLayer(stUData, stVData, aLS, LName,
                                            this.meteoUVSet.isUV());
                                }
                            } else {
                                LName = "Barb_" + LName;
                                if (this.windColor) {
                                    aLayer = DrawMeteoData.createSTBarbLayer(stUData, stVData, _stationData,
                                            aLS, LName, this.meteoUVSet.isUV());
                                } else {
                                    aLayer = DrawMeteoData.createSTBarbLayer(stUData, stVData, aLS, LName,
                                            this.meteoUVSet.isUV());
                                }
                            }
                            break;
                        case Streamline:
                            StationData nstUData;
                            StationData nstVData;
                            if (_meteoDataInfo.getMeteoUVSet().isUV()) {
                                nstUData = stUData;
                                nstVData = stVData;
                            } else {
                                StationData[] uvData = DataMath.getUVFromDS(stUData, stVData);
                                nstUData = uvData[0];
                                nstVData = uvData[1];
                            }
                            GridData UData = nstUData.interpolateData(_interpolationSetting);
                            GridData VData = nstVData.interpolateData(_interpolationSetting);
                            LName = "Streamline_" + LName;
                            aLayer = DrawMeteoData.createStreamlineLayer(UData, VData, _strmDensity, aLS, LName,
                                    true);
                            break;
                    }
                } else {
                    ifAddLayer = false;
                }
                break;
            case Weather_Symbol:
                LName = "Weather_" + LName;
//                aLayer = DrawMeteoData.createWeatherSymbolLayer(_stationData,
//                        _meteoDataDrawSet.getWeatherType(), LName);
                aLayer = DrawMeteoData.createWeatherSymbolLayer(_stationData, this.weatherString, LName);
                break;
            case Station_Model:
                StationModelData stationModelData = _meteoDataInfo.getStationModelData();
                LName = "StationModel_" + LName;
                boolean isSurface = true;
                if (_meteoDataInfo.getDataType() == MeteoDataType.MICAPS_2) {
                    isSurface = false;
                }
                aLayer = DrawMeteoData.createStationModelLayer(stationModelData, aLS, LName, isSurface);
                break;
            case Station_Info:
                StationInfoData stInfoData;
                if (_meteoDataInfo.getDataType() == MeteoDataType.GrADS_Station) {
                    stInfoData = _meteoDataInfo.getStationInfoData(this.jComboBox_Time.getSelectedIndex());
                } else {
                    stInfoData = _meteoDataInfo.getStationInfoData();
                }
                if (stInfoData != null) {
                    aLS.setUndefValue(_meteoDataInfo.getMissingValue());
                    LName = "StationInfo_" + LName;
                    aLayer = DrawMeteoData.createSTInfoLayer(stInfoData, aLS, LName);
                } else {
                    ifAddLayer = false;
                }
                break;
        }

        if (aLayer == null) {
            return null;
        }

        aLayer.setMaskout(true);
        aLayer.setProjInfo(_meteoDataInfo.getProjectionInfo());
        if (aLayer.getLayerType() == LayerTypes.VectorLayer) {
            if (ifAddLayer) {
                if (aLayer.getShapeType() == ShapeTypes.Polygon) {
                    _lastAddedLayerHandle = _parent.getMapDocument().getActiveMapFrame().insertPolygonLayer(aLayer);
                } else {
                    _lastAddedLayerHandle = _parent.getMapDocument().getActiveMapFrame().insertPolylineLayer((VectorLayer) aLayer);
                }
            }
        } else {
            if (ifAddLayer) {
                RasterLayer aILayer = (RasterLayer) aLayer;
                _lastAddedLayerHandle = _parent.getMapDocument().getActiveMapFrame().insertImageLayer(aILayer);
            }
        }

        return aLayer;
    }

    private GridData[] getUVGridData() {
        if (this.meteoUVSet.getUDataInfo() == null || (!this._dataInfoList.contains(this.meteoUVSet.getUDataInfo()))){
            this.meteoUVSet.setUDataInfo(_meteoDataInfo);
            this.meteoUVSet.setVDataInfo(_meteoDataInfo);
            this.meteoUVSet.setFixUVStr(_meteoDataInfo.getMeteoUVSet().isFixUVStr());
            this.meteoUVSet.setUV(_meteoDataInfo.getMeteoUVSet().isUV());
            this.meteoUVSet.setUStr(_meteoDataInfo.getMeteoUVSet().getUStr());
            this.meteoUVSet.setVStr(_meteoDataInfo.getMeteoUVSet().getVStr());
        }

        //Set U/V strings
        if (!this.meteoUVSet.isFixUVStr()) {
            List<String> vList = new ArrayList<>();
            for (int i = 0; i < this.jComboBox_Variable.getItemCount(); i++) {
                vList.add(this.jComboBox_Variable.getItemAt(i).toString());
            }

            if (!this.meteoUVSet.autoSetUVStr(vList)) {
                this.setMeteoUV();
            }
        }

        //Get U/V grid data            
        GridData udata = this.meteoUVSet.getUDataInfo().getGridData(this.meteoUVSet.getUStr());
        GridData vdata = this.meteoUVSet.getVDataInfo().getGridData(this.meteoUVSet.getVStr());

        if (udata == null || vdata == null) {
            return null;
        }

        //Un stag
        if (udata.isXStagger()) {
            udata = udata.unStagger_X();
        }
        if (vdata.isYStagger()) {
            vdata = vdata.unStagger_Y();
        }

        //Skip the grid data
        if (_skipY != 1 || _skipX != 1) {
            udata = udata.skip(_skipY, _skipX);
            vdata = vdata.skip(_skipY, _skipX);
        }

        return new GridData[]{udata, vdata};
    }

    private StationData[] getUVStationData() {
        if (this.meteoUVSet.getUDataInfo() == null || (!this._dataInfoList.contains(this.meteoUVSet.getUDataInfo()))){
            this.meteoUVSet.setUDataInfo(_meteoDataInfo);
            this.meteoUVSet.setVDataInfo(_meteoDataInfo);
            this.meteoUVSet.setFixUVStr(_meteoDataInfo.getMeteoUVSet().isFixUVStr());
            this.meteoUVSet.setUV(_meteoDataInfo.getMeteoUVSet().isUV());
            this.meteoUVSet.setUStr(_meteoDataInfo.getMeteoUVSet().getUStr());
            this.meteoUVSet.setVStr(_meteoDataInfo.getMeteoUVSet().getVStr());
        }
        
        //Set U/V strings
        if (!this.meteoUVSet.isFixUVStr()) {
            List<String> vList = new ArrayList<>();
            for (int i = 0; i < this.jComboBox_Variable.getItemCount(); i++) {
                vList.add(this.jComboBox_Variable.getItemAt(i).toString());
            }

            if (!this.meteoUVSet.autoSetUVStr(vList)) {
                this.meteoUVSet.setUV(false);
                this.setMeteoUV();
            }
        }

        //Get U/V station data
        StationData uData = this.meteoUVSet.getUDataInfo().getStationData(this.meteoUVSet.getUStr());
        StationData vData = this.meteoUVSet.getVDataInfo().getStationData(this.meteoUVSet.getVStr());

        if (uData == null || vData == null) {
            return null;
        } else {
            return new StationData[]{uData, vData};
        }
    }

    // </editor-fold>
    // </editor-fold>
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmMeteoData.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                FrmMeteoData dialog = new FrmMeteoData(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify                     
    private javax.swing.JButton jButton_1DPlot;
    private javax.swing.JButton jButton_Animator;
    private javax.swing.JButton jButton_ClearDraw;
    private javax.swing.JButton jButton_CreateAnimatorFile;
    private javax.swing.JButton jButton_DataInfo;
    private javax.swing.JButton jButton_Draw;
    private javax.swing.JButton jButton_DrawSetting;
    private javax.swing.JButton jButton_NexTime;
    //private javax.swing.JButton jButton_OpenData;
    private org.meteoinfo.ui.JSplitButton jSplitButton_OpenData;
    private javax.swing.JPopupMenu jPopupMenu_OpenData;
    private javax.swing.JMenuBar jMenuBar_Main;
    private javax.swing.JMenu jMenu_OpenData;
    private javax.swing.JMenuItem jMenuItem_NetCDF;
    private javax.swing.JMenuItem jMenuItem_GrADS;
    private javax.swing.JMenuItem jMenuItem_ARL;
    private javax.swing.JMenu jMenu_HYSPLIT;
    private javax.swing.JMenuItem jMenuItem_HYSPLIT_Traj;
    private javax.swing.JMenuItem jMenuItem_HYSPLIT_Conc;
    private javax.swing.JMenuItem jMenuItem_HYSPLIT_Particle;
    private javax.swing.JMenu jMenu_ASCII;
    private javax.swing.JMenuItem jMenuItem_ASCII_LonLat;
    private javax.swing.JMenuItem jMenuItem_ASCII_SYNOP;
    private javax.swing.JMenuItem jMenuItem_ASCII_METAR;
    private javax.swing.JMenuItem jMenuItem_ASCII_EsriGrid;
    private javax.swing.JMenuItem jMenuItem_ASCII_SurferGrid;
    private javax.swing.JMenuItem jMenuItem_MICAPS;
    private javax.swing.JMenu jMenu_MM5;
    private javax.swing.JMenuItem jMenuItem_MM5_Output;
    private javax.swing.JMenuItem jMenuItem_MM5_Inter;
    private javax.swing.JMenuItem jMenuItem_AWX;
    private javax.swing.JButton jButton_PreTime;
    private javax.swing.JButton jButton_RemoveAllData;
    private javax.swing.JButton jButton_SectionPlot;
    private javax.swing.JButton jButton_Setting;
    private javax.swing.JButton jButton_ViewData;
    private javax.swing.JCheckBox jCheckBox_Big_Endian;
    private javax.swing.JCheckBox jCheckBox_ColorVar;
    private javax.swing.JComboBox jComboBox_DrawType;
    private javax.swing.JComboBox jComboBox_Level;
    private javax.swing.JComboBox jComboBox_Time;
    private javax.swing.JComboBox jComboBox_Variable;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel_Variable;
    private javax.swing.JList jList_DataFiles;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel_DataSet;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private org.meteoinfo.ui.JSplitButton jSplitButton_Stat;
    private javax.swing.JPopupMenu jPopupMenu_Stat;
    private javax.swing.JMenuItem jMenuItem_ArrivalTime;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration    
}
