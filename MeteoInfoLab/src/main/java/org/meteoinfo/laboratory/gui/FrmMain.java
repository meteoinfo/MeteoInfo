/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.gui;

import bibliothek.gui.dock.ScreenDockStation;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;

import bibliothek.gui.dock.common.theme.ThemeMap;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.meteoinfo.console.ConsoleExecEvent;
import org.meteoinfo.console.IConsoleExecListener;
import org.meteoinfo.console.editor.TextEditor;
import org.meteoinfo.console.jython.PythonInteractiveInterpreter;
import org.meteoinfo.global.colors.ColorMap;
import org.meteoinfo.global.colors.ColorUtil;
import org.meteoinfo.laboratory.Options;
import org.meteoinfo.laboratory.application.AppCollection;
import org.meteoinfo.laboratory.application.Application;
import org.meteoinfo.laboratory.event.CurrentPathChangedEvent;
import org.meteoinfo.laboratory.event.ICurrentPathChangedListener;
import org.meteoinfo.legend.LayersLegend;
import org.meteoinfo.map.MapView;
import org.meteoinfo.plugin.IApplication;
import org.meteoinfo.plugin.IPlugin;
import org.meteoinfo.ui.ColorListCellRender;
import org.python.core.PyInstance;
import org.python.core.PyJavaType;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.util.PythonInterpreter;
import org.xml.sax.SAXException;

/**
 *
 * @author wyq
 */
public class FrmMain extends javax.swing.JFrame implements IApplication {

    //private final OutputDockable outputDock;
    private final EditorDockable editorDock;
    private final ConsoleDockable consoleDock;
    private final FigureDockable figuresDock;
    private final VariableDockable variableDock;
    private final FileDockable fileDock;
    private String startupPath;
    private Options options = new Options();
    private AppCollection apps = new AppCollection();
    private List<String> loadObjects = new ArrayList<>();

    /**
     * Creates new form FrmMain
     * @param startupPath Startup path
     * @param options Options
     */
    public FrmMain(String startupPath, Options options) {
        initComponents();

        this.startupPath = startupPath;
        this.options = options;
        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");

        //Set icon image
        System.out.println("Set icon image...");
        BufferedImage image = null;
        try {
            image = ImageIO.read(this.getClass().getResource("/images/MeteoLab_32.png"));
        } catch (IOException e) {
        }
        this.setIconImage(image);

        //Load configure file
        //System.out.println("Load configure file...");
        //this.loadConfigureFile();
        this.setLocation(this.options.getMainFormLocation());
        this.setSize(this.options.getMainFormSize());

        //Current folder
        this.jComboBox_CurrentFolder.removeAllItems();
        for (String cf : this.options.getRecentFolders()) {
            if (new File(cf).isDirectory()) {
                this.jComboBox_CurrentFolder.addItem(cf);
            }
        }
        String cf = this.options.getCurrentFolder();
        if (cf != null) {
            if (!new File(cf).isDirectory()) {
                cf = this.startupPath;
                this.options.setCurrentFolder(cf);
            }
            if (!this.options.getRecentFolders().contains(cf)) {
                this.jComboBox_CurrentFolder.addItem(cf);
            }
            this.jComboBox_CurrentFolder.setSelectedItem(cf);
        }

        //Add dockable panels
        System.out.println("Add dockable panels...");
        CControl control = new CControl(this);
        this.add(control.getContentArea());
        if (this.options.isDockWindowDecorated()) {
            control.putProperty(ScreenDockStation.WINDOW_FACTORY, new CustomWindowFactory());
        }
        control.setTheme(ThemeMap.KEY_FLAT_THEME);
        control.getIcons().setIconClient("locationmanager.minimize", new FlatSVGIcon("org/meteoinfo/laboratory/icons/minimize.svg"));
        control.getIcons().setIconClient("locationmanager.maximize", new FlatSVGIcon("org/meteoinfo/laboratory/icons/maximize.svg"));
        control.getIcons().setIconClient("locationmanager.externalize", new FlatSVGIcon("org/meteoinfo/laboratory/icons/outgoing.svg"));
        control.getIcons().setIconClient("locationmanager.unexternalize", new FlatSVGIcon("org/meteoinfo/laboratory/icons/incoming.svg"));
        control.getIcons().setIconClient("locationmanager.normalize", new FlatSVGIcon("org/meteoinfo/laboratory/icons/restore.svg"));
        control.getIcons().setIconClient("locationmanager.unmaximize_externalized", new FlatSVGIcon("org/meteoinfo/laboratory/icons/restore.svg"));

        System.out.println("Editor and Console panels...");
        CGrid grid = new CGrid(control);

        consoleDock = new ConsoleDockable(this, this.startupPath, "Console", "Console");
        consoleDock.getConsole().setFont(this.options.getTextFont());

        this.editorDock = new EditorDockable(this, "Editor", "Editor");
        this.editorDock.setInterp(this.consoleDock.getInterpreter());
        this.editorDock.addNewTextEditor("New file");
        this.editorDock.openFiles(this.options.getOpenedFiles());
        this.editorDock.setTextFont(this.options.getTextFont());

        //Load toolbox applications        
        System.out.println("Load toolbox applications...");
        String toolboxPath = this.startupPath + File.separator + "toolbox";
        if (isDebug) {
            toolboxPath = "D:/MyProgram/Java/MeteoInfoDev/toolbox";
        }
        String appConfFn = toolboxPath + File.separator + "apps.xml";
        if (new File(appConfFn).exists()) {
            try {
                this.apps.setPluginPath(toolboxPath);
                this.apps.loadConfigFile(appConfFn);
                if (this.apps.size() > 0) {
                    for (Application app : apps) {
                        if (app.isLoad()) {
                            this.loadApplication(app);
                        }
                    }
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException | SAXException ex) {
                Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        final PythonInteractiveInterpreter interp = this.consoleDock.getInterpreter();
        PyStringMap locals = (PyStringMap) interp.getLocals();
        PyList items = locals.items();
        String name;
        for (Object a : items) {
            PyTuple at = (PyTuple) a;
            name = at.__getitem__(0).toString();
            this.loadObjects.add(name);
        }
        interp.addConsoleExecListener(new IConsoleExecListener() {
            @Override
            public void consoleExecEvent(ConsoleExecEvent event) {

                PyStringMap locals = (PyStringMap) interp.getLocals();
                PyList items = locals.items();
                String name, className, size, value;
                PyObject var;
                List<Object[]> vars = new ArrayList<>();
                for (Object a : items) {
                    PyTuple at = (PyTuple) a;
                    name = at.__getitem__(0).toString();
                    if (!FrmMain.this.loadObjects.contains(name)) {
                        var = at.__getitem__(1);
                        if (var instanceof PyInstance) {
                            className = ((PyInstance) var).instclass.__name__;
                            switch (className) {
                                case "DimArray":
                                case "NDArray":
                                    if (var.__len__() <= 10) {
                                        value = var.__str__().toString();
                                    } else {
                                        value = "";
                                    }
                                    vars.add(new Object[]{name, className, var.__getattr__("sizestr"), value});
                                    break;
                                default:
                                    vars.add(new Object[]{name, className, "", ""});
                                    break;
                            }
                        } else if (var instanceof PyType || var instanceof PyJavaType) {
                            
                        } else {
                            className = var.getClass().getSimpleName();
                            value = "";
                            size = "";
                            boolean isAdd = true;
                            switch (className) {
                                case "PyInteger":
                                case "PyFloat":
                                case "PyString":
                                    value = var.toString();
                                    size = "1";
                                    break;
                                case "PyList":
                                case "PyTuple":
                                    if (var.__len__() <= 10) {
                                        value = var.toString();
                                    }
                                    size = String.valueOf(var.__len__());
                                    break;
                                case "PyObjectDerived":
                                    className = var.getType().getName();
                                    switch (className) {
                                        case "DimArray":
                                        case "NDArray":
                                            if (var.__len__() <= 10) {
                                                value = var.__str__().toString();
                                            } else {
                                                value = "";
                                            }
                                            size = var.__getattr__("shape").toString();
                                            break;
                                    }
                                    break;
                                default:
                                    isAdd = false;
                                    break;
                            }
                            if (isAdd)
                                vars.add(new Object[]{name, className, size, value});
                        }
                    }
                }
                if (FrmMain.this.variableDock != null) {
                    FrmMain.this.variableDock.getVariableExplorer().updateVariables(vars);
                }
            }

        });
        
        //Add figure panel
        System.out.println("Add figure panel...");
        figuresDock = new FigureDockable(this, "Figures", "Figures");
        figuresDock.setDoubleBuffer(this.options.isDoubleBuffer());

        //Add variable panel
        this.variableDock = new VariableDockable("Variables", "Variable explorer");
        
        //Add file panel
        System.out.println("Add file panel...");
        this.fileDock = new FileDockable("Files", "File explorer");
        if (cf != null) {
            this.fileDock.setPath(new File(cf));
        }
        this.fileDock.getFileExplorer().addCurrentPathChangedListener(new ICurrentPathChangedListener() {
            @Override
            public void currentPathChangedEvent(CurrentPathChangedEvent event) {
                FrmMain.this.setCurrentPath(FrmMain.this.fileDock.getFileExplorer().getPath().getAbsolutePath());
            }

        });
        this.fileDock.getFileExplorer().getTable().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = ((JTable) e.getSource()).getSelectedRow();
                    if (row >= 0) {
                        if (((JTable) e.getSource()).getValueAt(row, 2).toString().equals("py")) {
                            File file = new File(FrmMain.this.fileDock.getFileExplorer().getPath().getAbsoluteFile()
                                    + File.separator + ((JTable) e.getSource()).getValueAt(row, 0).toString());
                            FrmMain.this.editorDock.openFile(file);
                        }
                    }
                }
            }

        });
        //grid.add(0, 0, 5, 5, this.outputDock);
        grid.add(0, 0, 5, 5, editorDock);
        grid.add(0, 5, 5, 5, consoleDock);
        grid.add(5, 0, 5, 5, this.variableDock);
        grid.add(5, 0, 5, 5, this.fileDock);
        grid.add(5, 5, 5, 5, figuresDock);
        control.getContentArea().deploy(grid);
        
        System.out.println("Main form is loaded!");

        System.setOut(this.consoleDock.getConsole().getOut());
    }

    /**
     * Get applications
     *
     * @return Applications
     */
    public AppCollection getApplications() {
        return this.apps;
    }

    /**
     * Get startup path
     *
     * @return Startup path
     */
    public String getStartupPath() {
        return this.startupPath;
    }

    /**
     * Get configure options
     *
     * @return Configure options
     */
    public Options getOptions() {
        return this.options;
    }

    /**
     * Load an application
     *
     * @param plugin Application
     */
    public void loadApplication(Application plugin) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            PythonInterpreter interp = this.getConsoleDockable().getInterpreter();
            String path = plugin.getPath();
            interp.exec("import " + path);
            interp.exec("from " + path + ".loadApp import LoadApp");
            PyObject loadClass = interp.get("LoadApp");
            PyObject loadObj = loadClass.__call__();
            IPlugin instance = (IPlugin) loadObj.__tojava__(IPlugin.class);
            instance.setApplication(FrmMain.this);
            instance.setName(plugin.getName());
            plugin.setPluginObject(instance);
            plugin.setLoad(true);
            instance.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setCursor(Cursor.getDefaultCursor());
    }

//    /**
//     * Load an application
//     *
//     * @param plugin Application
//     */
//    public void loadApplication_bak(Application plugin) {
//        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//        URL url = null;
//        try {
//            url = new URL("file:" + plugin.getJarFileName());
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});
//        try {
//            Class<?> clazz = urlClassLoader.loadClass(plugin.getClassName());
//            IPlugin instance = (IPlugin) clazz.newInstance();
//            instance.setApplication(FrmMain.this);
//            instance.setName(plugin.getName());
//            plugin.setPluginObject(instance);
//            plugin.setLoad(true);
//            instance.load();
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        this.setCursor(Cursor.getDefaultCursor());
//    }
    /**
     * Unload an application
     *
     * @param plugin Application
     */
    public void unloadApplication(Application plugin) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (plugin.getPluginObject() != null) {
            plugin.getPluginObject().unload();
            plugin.setPluginObject(null);
            plugin.setLoad(false);
        }
        this.setCursor(Cursor.getDefaultCursor());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cControl1 = new bibliothek.gui.dock.common.CControl();
        jPanel_Toolbar = new javax.swing.JPanel();
        jToolBar_Editor = new javax.swing.JToolBar();
        jButton_NewFile = new javax.swing.JButton();
        jButton_OpenFile = new javax.swing.JButton();
        jButton_SaveFile = new javax.swing.JButton();
        jButton_SaveAs = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton_Undo = new javax.swing.JButton();
        jButton_Redo = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton_RunScript = new javax.swing.JButton();
        jToolBar_CurrentFolder = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        jComboBox_CurrentFolder = new javax.swing.JComboBox();
        jButton_CurrentFolder = new javax.swing.JButton();
        jPanel_Status = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu_File = new javax.swing.JMenu();
        jMenuItem_NewFile = new javax.swing.JMenuItem();
        jMenuItem_OpenFile = new javax.swing.JMenuItem();
        jMenuItem_SaveFile = new javax.swing.JMenuItem();
        jMenuItem_SaveAs = new javax.swing.JMenuItem();
        jMenuItem_CloseAllFiles = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_Exit = new javax.swing.JMenuItem();
        jMenu_Editor = new javax.swing.JMenu();
        jMenuItem_Cut = new javax.swing.JMenuItem();
        jMenuItem_Copy = new javax.swing.JMenuItem();
        jMenuItem_Paste = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_FindReplace = new javax.swing.JMenuItem();
        jMenuItem_Comment = new javax.swing.JMenuItem();
        jMenuItem_InsertTab = new javax.swing.JMenuItem();
        jMenuItem_DeleteTab = new javax.swing.JMenuItem();
        jMenu_Options = new javax.swing.JMenu();
        jMenuItem_Setting = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_ColorMaps = new javax.swing.JMenuItem();
        jMenuItem_ColorDialog = new javax.swing.JMenuItem();
        jMenu_Apps = new javax.swing.JMenu();
        jMenuItem_AppsManager = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenu_Help = new javax.swing.JMenu();
        jMenuItem_About = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_Help = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MeteoInfoLab");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel_Toolbar.setLayout(new java.awt.BorderLayout());

        jToolBar_Editor.setRollover(true);
        jToolBar_Editor.setPreferredSize(new java.awt.Dimension(250, 25));

        //jButton_NewFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NewFile.Image.png"))); // NOI18N
        jButton_NewFile.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/file-new.svg"));
        jButton_NewFile.setToolTipText("New File");
        jButton_NewFile.setFocusable(false);
        jButton_NewFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewFileActionPerformed(evt);
            }
        });
        jToolBar_Editor.add(jButton_NewFile);

        //jButton_OpenFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Folder_1_16x16x8.png"))); // NOI18N
        jButton_OpenFile.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/file-open.svg"));
        jButton_OpenFile.setToolTipText("Open File");
        jButton_OpenFile.setFocusable(false);
        jButton_OpenFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_OpenFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_OpenFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OpenFileActionPerformed(evt);
            }
        });
        jToolBar_Editor.add(jButton_OpenFile);

        //jButton_SaveFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Disk_1_16x16x8.png"))); // NOI18N
        jButton_SaveFile.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/file-save.svg"));
        jButton_SaveFile.setToolTipText("Save File");
        jButton_SaveFile.setFocusable(false);
        jButton_SaveFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_SaveFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_SaveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SaveFileActionPerformed(evt);
            }
        });
        jToolBar_Editor.add(jButton_SaveFile);

        //jButton_SaveAs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save_16.png"))); // NOI18N
        jButton_SaveAs.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/save-as.svg"));
        jButton_SaveAs.setToolTipText("Save As");
        jButton_SaveAs.setFocusable(false);
        jButton_SaveAs.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_SaveAs.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_SaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SaveAsActionPerformed(evt);
            }
        });
        jToolBar_Editor.add(jButton_SaveAs);
        jToolBar_Editor.add(jSeparator1);

        //jButton_Undo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_Undo.Image.png"))); // NOI18N
        jButton_Undo.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/undo.svg"));
        jButton_Undo.setToolTipText("Undo");
        jButton_Undo.setFocusable(false);
        jButton_Undo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Undo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Undo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_UndoActionPerformed(evt);
            }
        });
        jToolBar_Editor.add(jButton_Undo);

        //jButton_Redo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_Redo.Image.png"))); // NOI18N
        jButton_Redo.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/redo.svg"));
        jButton_Redo.setToolTipText("Redo");
        jButton_Redo.setFocusable(false);
        jButton_Redo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Redo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Redo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_RedoActionPerformed(evt);
            }
        });
        jToolBar_Editor.add(jButton_Redo);
        jToolBar_Editor.add(jSeparator2);

        //jButton_RunScript.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_RunScript.Image.png"))); // NOI18N
        jButton_RunScript.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/execute.svg"));
        jButton_RunScript.setToolTipText("Run Script");
        jButton_RunScript.setFocusable(false);
        jButton_RunScript.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_RunScript.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_RunScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_RunScriptActionPerformed(evt);
            }
        });
        jToolBar_Editor.add(jButton_RunScript);

        jPanel_Toolbar.add(jToolBar_Editor, java.awt.BorderLayout.LINE_START);

        jToolBar_CurrentFolder.setRollover(true);

        jLabel1.setText("Current Folder: ");
        jToolBar_CurrentFolder.add(jLabel1);

        jComboBox_CurrentFolder.setEditable(true);
        jComboBox_CurrentFolder.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_CurrentFolder.setPreferredSize(new java.awt.Dimension(400, 21));
        jComboBox_CurrentFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_CurrentFolderActionPerformed(evt);
            }
        });
        jToolBar_CurrentFolder.add(jComboBox_CurrentFolder);

        //jButton_CurrentFolder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/folder.png"))); // NOI18N
        jButton_CurrentFolder.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/file-open.svg"));
        jButton_CurrentFolder.setFocusable(false);
        jButton_CurrentFolder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_CurrentFolder.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_CurrentFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_CurrentFolderActionPerformed(evt);
            }
        });
        jToolBar_CurrentFolder.add(jButton_CurrentFolder);

        jPanel_Toolbar.add(jToolBar_CurrentFolder, java.awt.BorderLayout.LINE_END);

        getContentPane().add(jPanel_Toolbar, java.awt.BorderLayout.NORTH);

        jPanel_Status.setPreferredSize(new java.awt.Dimension(588, 25));

        javax.swing.GroupLayout jPanel_StatusLayout = new javax.swing.GroupLayout(jPanel_Status);
        jPanel_Status.setLayout(jPanel_StatusLayout);
        jPanel_StatusLayout.setHorizontalGroup(
            jPanel_StatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 777, Short.MAX_VALUE)
        );
        jPanel_StatusLayout.setVerticalGroup(
            jPanel_StatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        //getContentPane().add(jPanel_Status, java.awt.BorderLayout.PAGE_END);

        jMenu_File.setText("File");
        jMenu_File.setMnemonic(KeyEvent.VK_F);

        jMenuItem_NewFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_NewFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NewFile.Image.png"))); // NOI18N
        jMenuItem_NewFile.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/file-new.svg"));
        jMenuItem_NewFile.setText("New");
        jMenuItem_NewFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_NewFileActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_NewFile);

        jMenuItem_OpenFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_OpenFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Folder_1_16x16x8.png"))); // NOI18N
        jMenuItem_OpenFile.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/file-open.svg"));
        jMenuItem_OpenFile.setText("Open ...");
        jMenuItem_OpenFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_OpenFileActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_OpenFile);

        jMenuItem_SaveFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_SaveFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Disk_1_16x16x8.png"))); // NOI18N
        jMenuItem_SaveFile.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/file-save.svg"));
        jMenuItem_SaveFile.setText("Save");
        jMenuItem_SaveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SaveFileActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_SaveFile);

        //jMenuItem_SaveAs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save_16.png"))); // NOI18N
        jMenuItem_SaveAs.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/save-as.svg"));
        jMenuItem_SaveAs.setText("Save As ...");
        jMenuItem_SaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SaveAsActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_SaveAs);

        //jMenuItem_CloseAllFiles.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/closefile.png"))); // NOI18N
        jMenuItem_CloseAllFiles.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/file-close-all.svg"));
        jMenuItem_CloseAllFiles.setText("Close All Files");
        jMenuItem_CloseAllFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_CloseAllFilesActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_CloseAllFiles);
        jMenu_File.add(jSeparator3);

        jMenuItem_Exit.setText("Exit");
        jMenuItem_Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ExitActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_Exit);

        jMenuBar1.add(jMenu_File);

        jMenu_Editor.setMnemonic('E');
        jMenu_Editor.setText("Edit");
        jMenu_Editor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_EditorActionPerformed(evt);
            }
        });

        jMenuItem_Cut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_Cut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSMI_EditCut.Image.png"))); // NOI18N
        jMenuItem_Cut.setIcon(new FlatSVGIcon( "org/meteoinfo/laboratory/icons/menu-cut.svg" ));
        jMenuItem_Cut.setText("Cut");
        jMenuItem_Cut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_CutActionPerformed(evt);
            }
        });
        jMenu_Editor.add(jMenuItem_Cut);

        jMenuItem_Copy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_Copy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/menuEditCopy.Image.png"))); // NOI18N
        jMenuItem_Copy.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/copy.svg"));
        jMenuItem_Copy.setText("Copy");
        jMenuItem_Copy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_CopyActionPerformed(evt);
            }
        });
        jMenu_Editor.add(jMenuItem_Copy);

        jMenuItem_Paste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_Paste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pasteToolStripButton.Image.png"))); // NOI18N
        jMenuItem_Paste.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/menu-paste.svg"));
        jMenuItem_Paste.setText("Paste");
        jMenuItem_Paste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_PasteActionPerformed(evt);
            }
        });
        jMenu_Editor.add(jMenuItem_Paste);
        jMenu_Editor.add(jSeparator6);

        jMenuItem_FindReplace.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_FindReplace.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/find.png"))); // NOI18N
        jMenuItem_FindReplace.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/find.svg"));
        jMenuItem_FindReplace.setText("Find & Replace");
        jMenuItem_FindReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_FindReplaceActionPerformed(evt);
            }
        });
        jMenu_Editor.add(jMenuItem_FindReplace);

        jMenuItem_Comment.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_Comment.setText("Toggle Comment");
        jMenuItem_Comment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_CommentActionPerformed(evt);
            }
        });
        jMenu_Editor.add(jMenuItem_Comment);

        jMenuItem_InsertTab.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, 0));
        jMenuItem_InsertTab.setText("Insert Tab (4 spaces)");
        jMenuItem_InsertTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_InsertTabActionPerformed(evt);
            }
        });
        jMenu_Editor.add(jMenuItem_InsertTab);

        jMenuItem_DeleteTab.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, java.awt.event.InputEvent.SHIFT_MASK));
        jMenuItem_DeleteTab.setText("Delete Tab (4 spaces)");
        jMenuItem_DeleteTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_DeleteTabActionPerformed(evt);
            }
        });
        jMenu_Editor.add(jMenuItem_DeleteTab);

        jMenuBar1.add(jMenu_Editor);

        jMenu_Options.setMnemonic(KeyEvent.VK_O);
        jMenu_Options.setText("Options");

        //jMenuItem_Setting.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/setting.png"))); // NOI18N
        jMenuItem_Setting.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/gear.svg"));
        jMenuItem_Setting.setText("Setting");
        jMenuItem_Setting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SettingActionPerformed(evt);
            }
        });
        jMenu_Options.add(jMenuItem_Setting);
        jMenu_Options.add(jSeparator4);

        //jMenuItem_ColorMaps.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/colors.png"))); // NOI18N
        jMenuItem_ColorMaps.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/color-wheel.svg"));
        jMenuItem_ColorMaps.setText("Color Maps");
        jMenuItem_ColorMaps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ColorMapsActionPerformed(evt);
            }
        });
        jMenu_Options.add(jMenuItem_ColorMaps);

        //jMenuItem_ColorDialog.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Color_Wheel.png"))); // NOI18N
        jMenuItem_ColorDialog.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/colors.svg"));
        jMenuItem_ColorDialog.setText("Color Dialog");
        jMenuItem_ColorDialog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ColorDialogActionPerformed(evt);
            }
        });
        jMenu_Options.add(jMenuItem_ColorDialog);

        jMenuBar1.add(jMenu_Options);

        jMenu_Apps.setText("Apps");
        jMenu_Apps.setMnemonic(KeyEvent.VK_A);

        //jMenuItem_AppsManager.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/plugin_edit_green.png"))); // NOI18N
        jMenuItem_AppsManager.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/toolbox.svg"));
        jMenuItem_AppsManager.setText("Application Manager");
        jMenuItem_AppsManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_AppsManagerActionPerformed(evt);
            }
        });
        jMenu_Apps.add(jMenuItem_AppsManager);
        jMenu_Apps.add(jSeparator5);

        jMenuBar1.add(jMenu_Apps);

        jMenu_Help.setText("Help");
        jMenu_Help.setMnemonic(KeyEvent.VK_H);

        //jMenuItem_About.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/information.png"))); // NOI18N
        jMenuItem_About.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/information.svg"));
        jMenuItem_About.setText("About");
        jMenuItem_About.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_AboutActionPerformed(evt);
            }
        });
        jMenu_Help.add(jMenuItem_About);
        jMenu_Help.add(jSeparator7);

        //jMenuItem_Help.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/help.png"))); // NOI18N
        jMenuItem_Help.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/help.svg"));
        jMenuItem_Help.setText("Help");
        jMenuItem_Help.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_HelpActionPerformed(evt);
            }
        });
        jMenu_Help.add(jMenuItem_Help);

        jMenuBar1.add(jMenu_Help);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_NewFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewFileActionPerformed
        // TODO add your handling code here:
        this.editorDock.addNewTextEditor("New file");
    }//GEN-LAST:event_jButton_NewFileActionPerformed

    private void jButton_OpenFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OpenFileActionPerformed
        // TODO add your handling code here:
        this.editorDock.doOpen_Jython();
    }//GEN-LAST:event_jButton_OpenFileActionPerformed

    private void jButton_SaveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_SaveFileActionPerformed
        // TODO add your handling code here:
        TextEditor textEditor = this.editorDock.getActiveTextEditor();
        if (textEditor != null) {
            this.editorDock.doSave(textEditor);
        }
    }//GEN-LAST:event_jButton_SaveFileActionPerformed

    private void jButton_UndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_UndoActionPerformed
        // TODO add your handling code here:
        TextEditor textEditor = this.editorDock.getActiveTextEditor();
        textEditor.getTextArea().undoLastAction();
    }//GEN-LAST:event_jButton_UndoActionPerformed

    private void jButton_RedoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_RedoActionPerformed
        // TODO add your handling code here:
        TextEditor textEditor = this.editorDock.getActiveTextEditor();
        textEditor.getTextArea().redoLastAction();
    }//GEN-LAST:event_jButton_RedoActionPerformed

    private void jButton_RunScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_RunScriptActionPerformed
        TextEditor te = this.editorDock.getActiveTextEditor();
        if (!te.getFileName().isEmpty() && te.getTextArea().isDirty()) {
            te.saveFile(te.getFile());
        }
        if (te.getFileName().isEmpty()) {
            String code = te.getTextArea().getText();
            try {
                this.consoleDock.runPythonScript(code);
            } catch (InterruptedException ex) {
                Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            this.consoleDock.execfile(te.getFileName());
        }
    }//GEN-LAST:event_jButton_RunScriptActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        formClose();
    }//GEN-LAST:event_formWindowClosing

    private void formClose() {
        this.saveConfigureFile();
        boolean isDispose = true;
        for (int i = 0; i < this.editorDock.getTabbedPane().getTabCount(); i++) {
            TextEditor textEditor = (TextEditor) this.editorDock.getTabbedPane().getComponentAt(i);
            if (textEditor != null) {
                boolean ifClose = true;
                if (textEditor.getTextArea().isDirty()) {
                    String fName = textEditor.getFileName();
                    if (fName.isEmpty()) {
                        fName = "New file";
                    }
                    int result = JOptionPane.showConfirmDialog(null, MessageFormat.format("Save changes to \"{0}\"", fName), "Save?", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        if (!this.editorDock.doSave(textEditor)) {
                            ifClose = false;
                        }
                    } else if (result == JOptionPane.CANCEL_OPTION) {
                        ifClose = false;
                    }
                }

                if (!ifClose) {
                    isDispose = false;
                    break;
                }
            }
        }
        if (isDispose) {
            System.exit(0);
        }
    }

    private void jButton_SaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_SaveAsActionPerformed
        // TODO add your handling code here:
        TextEditor editor = this.editorDock.getActiveTextEditor();
        if (editor != null) {
            this.editorDock.doSaveAs_Jython(editor);
        }
    }//GEN-LAST:event_jButton_SaveAsActionPerformed

    private void jMenuItem_CutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_CutActionPerformed
        // TODO add your handling code here:
        TextEditor textEditor = this.editorDock.getActiveTextEditor();
        textEditor.getTextArea().cut();
    }//GEN-LAST:event_jMenuItem_CutActionPerformed

    private void jMenuItem_CopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_CopyActionPerformed
        // TODO add your handling code here:
        TextEditor textEditor = this.editorDock.getActiveTextEditor();
        textEditor.getTextArea().copy();
    }//GEN-LAST:event_jMenuItem_CopyActionPerformed

    private void jMenuItem_PasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_PasteActionPerformed
        // TODO add your handling code here:
        TextEditor textEditor = this.editorDock.getActiveTextEditor();
        textEditor.getTextArea().paste();
    }//GEN-LAST:event_jMenuItem_PasteActionPerformed

    private void jMenuItem_AboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_AboutActionPerformed
        // TODO add your handling code here:
        FrmAbout frmAbout = new FrmAbout(this, false);
        frmAbout.setLocationRelativeTo(this);
        frmAbout.setVisible(true);
    }//GEN-LAST:event_jMenuItem_AboutActionPerformed

    private void jComboBox_CurrentFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_CurrentFolderActionPerformed
        // TODO add your handling code here:   
        if (this.fileDock == null) {
            return;
        }

        if (this.jComboBox_CurrentFolder.getItemCount() > 0) {
            Object obj = this.jComboBox_CurrentFolder.getSelectedItem();
            String path = obj.toString();
            if (new File(path).isDirectory()) {
                this.fileDock.setPath(new File(path));
            }
            this.jComboBox_CurrentFolder.removeItem(obj);
            this.jComboBox_CurrentFolder.addItem(obj);
            this.jComboBox_CurrentFolder.setSelectedItem(obj);
        }
    }//GEN-LAST:event_jComboBox_CurrentFolderActionPerformed

    private void jButton_CurrentFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_CurrentFolderActionPerformed
        // TODO add your handling code here:
        JFileChooser aDlg = new JFileChooser();
        //String path = System.getProperty("user.dir");
        File pathDir = new File(this.jComboBox_CurrentFolder.getSelectedItem().toString());
        if (pathDir.isDirectory()) {
            aDlg.setCurrentDirectory(pathDir);
        }
        aDlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File aFile = aDlg.getSelectedFile();
            String path = aFile.getAbsolutePath();
            this.setCurrentPath(path);
            this.fileDock.setPath(new File(this.options.getCurrentFolder()));
        }

//        String os = System.getProperty("os.name").toLowerCase();
//        //Mac  
//        if (os.contains("mac")) {
//            System.setProperty("apple.awt.fileDialogForDirectories", "true");
//            FileDialog fd = new FileDialog(this, "Choose a folder", FileDialog.LOAD);
//            File pathDir = new File(this.jComboBox_CurrentFolder.getSelectedItem().toString());
//            if (pathDir.isDirectory()) {
//                fd.setDirectory(pathDir.getAbsolutePath());
//            }
//            fd.setVisible(true);
//            if (fd.getFile() != null){
//                this.setCurrentPath(fd.getDirectory());
//                this.fileDock.setPath(new File(this.options.getCurrentFolder()));                
//            }
////            String fileName = fd.getDirectory();
////            if (fileName != null) {
////                this.setCurrentPath(fileName);
////                this.fileDock.setPath(new File(this.options.getCurrentFolder()));
////            }
//            System.setProperty("apple.awt.fileDialogForDirectories", "false");
//        } else {
//            JFileChooser aDlg = new JFileChooser();
//            //String path = System.getProperty("user.dir");
//            File pathDir = new File(this.jComboBox_CurrentFolder.getSelectedItem().toString());
//            if (pathDir.isDirectory()) {
//                aDlg.setCurrentDirectory(pathDir);
//            }
//            aDlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//            if (JFileChooser.APPROVE_OPTION == aDlg.showDialog(this, "Open")) {
//                File aFile = aDlg.getSelectedFile();
//                String path = aFile.getAbsolutePath();
//                this.setCurrentPath(path);
//                this.fileDock.setPath(new File(this.options.getCurrentFolder()));
//            }
//        }
    }//GEN-LAST:event_jButton_CurrentFolderActionPerformed

    private void jMenuItem_ExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_ExitActionPerformed
        // TODO add your handling code here:
        formClose();
    }//GEN-LAST:event_jMenuItem_ExitActionPerformed

    private void jMenuItem_CloseAllFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_CloseAllFilesActionPerformed
        // TODO add your handling code here:
        this.editorDock.closeAllFiles();
    }//GEN-LAST:event_jMenuItem_CloseAllFilesActionPerformed

    private void jMenuItem_ColorMapsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_ColorMapsActionPerformed
        // TODO add your handling code here:        
        try {
            ColorMap[] colorTables = ColorUtil.getColorTables();
            ColorListCellRender render = new ColorListCellRender();
            render.setPreferredSize(new Dimension(62, 21));
            Object[][] elements = new Object[colorTables.length][2];
            for (int i = 0; i < colorTables.length; i++) {
                elements[i][0] = colorTables[i];
                elements[i][1] = String.valueOf(i);
            }
            final JList jlist = new JList(elements);
            jlist.setCellRenderer(render);
            jlist.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        int idx = jlist.getSelectedIndex();
                        if (idx >= 0) {
                            idx += 1;
                            if (idx == jlist.getModel().getSize() - 1) {
                                idx = 0;
                            }
                            jlist.setSelectedIndex(idx);
                        }
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }

                @Override
                public void keyTyped(KeyEvent e) {
                }

            });
            JDialog colorMapDialog = new JDialog(this, false);
            colorMapDialog.setTitle("Color Map");
            colorMapDialog.setFocusableWindowState(false);
            colorMapDialog.add(new JScrollPane(jlist));
            colorMapDialog.setSize(this.getWidth() / 2, this.getHeight() * 2 / 3);
            colorMapDialog.setLocationRelativeTo(this);
            colorMapDialog.setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItem_ColorMapsActionPerformed

    private void jMenuItem_AppsManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_AppsManagerActionPerformed
        // TODO add your handling code here:
        FrmAppsManager frm = new FrmAppsManager(this, true);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }//GEN-LAST:event_jMenuItem_AppsManagerActionPerformed

    private void jMenuItem_ColorDialogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_ColorDialogActionPerformed
        // TODO add your handling code here:
        JDialog colorMapDialog = new JDialog(this, false);
        colorMapDialog.setTitle("Color Dialog");
        colorMapDialog.setFocusableWindowState(false);
        colorMapDialog.add(new JColorChooser(Color.black));
        colorMapDialog.setSize(600, 400);
        colorMapDialog.setLocationRelativeTo(this);
        colorMapDialog.setVisible(true);
    }//GEN-LAST:event_jMenuItem_ColorDialogActionPerformed

    private void jMenuItem_NewFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_NewFileActionPerformed
        // TODO add your handling code here:
        this.jButton_NewFile.doClick();
    }//GEN-LAST:event_jMenuItem_NewFileActionPerformed

    private void jMenuItem_OpenFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_OpenFileActionPerformed
        // TODO add your handling code here:
        this.jButton_OpenFile.doClick();
    }//GEN-LAST:event_jMenuItem_OpenFileActionPerformed

    private void jMenuItem_SaveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_SaveFileActionPerformed
        // TODO add your handling code here:
        this.jButton_SaveFile.doClick();
    }//GEN-LAST:event_jMenuItem_SaveFileActionPerformed

    private void jMenuItem_SaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_SaveAsActionPerformed
        // TODO add your handling code here:
        this.jButton_SaveAs.doClick();
    }//GEN-LAST:event_jMenuItem_SaveAsActionPerformed

    private void jMenu_EditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_EditorActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jMenu_EditorActionPerformed

    private void jMenuItem_FindReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_FindReplaceActionPerformed
        // TODO add your handling code here:
        FrmFindReplace frm = new FrmFindReplace(this, false, this.editorDock.getActiveTextEditor());    
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }//GEN-LAST:event_jMenuItem_FindReplaceActionPerformed

    private void jMenuItem_SettingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_SettingActionPerformed
        // TODO add your handling code here:
        FrmSetting frm = new FrmSetting(this, false);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }//GEN-LAST:event_jMenuItem_SettingActionPerformed

    private void jMenuItem_CommentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_CommentActionPerformed
        // TODO add your handling code here:
        this.editorDock.Comment();
    }//GEN-LAST:event_jMenuItem_CommentActionPerformed

    private void jMenuItem_InsertTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_InsertTabActionPerformed
        // TODO add your handling code here:
        this.editorDock.insertTab();
    }//GEN-LAST:event_jMenuItem_InsertTabActionPerformed

    private void jMenuItem_DeleteTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_DeleteTabActionPerformed
        // TODO add your handling code here:
        this.editorDock.delTab();
    }//GEN-LAST:event_jMenuItem_DeleteTabActionPerformed

    private void jMenuItem_HelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_HelpActionPerformed
        // TODO add your handling code here:
        try {
            URI uri = new URI("http://www.meteothink.org/docs/meteoinfolab/index.html");
            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
            }
            if (desktop != null) {
                desktop.browse(uri);
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(FrmAbout.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioe) {
        }
    }//GEN-LAST:event_jMenuItem_HelpActionPerformed

    /**
     * Get figure dockable
     *
     * @return Figure dockable
     */
    public FigureDockable getFigureDock() {
        return this.figuresDock;
    }
    
    /**
     * Get editor dockable
     * @return Editor dockable
     */
    public EditorDockable getEditorDock() {
        return this.editorDock;
    }

    /**
     * Get current folder
     *
     * @return Current folder
     */
    public String getCurrentFolder() {
        return this.options.getCurrentFolder();
    }

    /**
     * Get console dockable
     *
     * @return Console dockable
     */
    public ConsoleDockable getConsoleDockable() {
        return this.consoleDock;
    }

    /**
     * Load configure file
     */
    public final void loadConfigureFile() {
        String fn = this.startupPath + File.separator + "milconfig.xml";
        if (new File(fn).exists()) {
            try {
                this.options.loadConfigFile(fn);
            } catch (SAXException | IOException | ParserConfigurationException ex) {
                Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Save configure file
     */
    public final void saveConfigureFile() {
        String fn = this.options.getFileName();
        try {
            List<String> cfolders = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                if (i >= this.jComboBox_CurrentFolder.getItemCount()) {
                    break;
                }
                cfolders.add(this.jComboBox_CurrentFolder.getItemAt(i).toString());
            }
            this.options.setRecentFolders(cfolders);
            List<String> ofiles = this.editorDock.getOpenedFiles();
            this.options.setOpenedFiles(ofiles);
            this.options.setMainFormLocation(this.getLocation());
            this.options.setMainFormSize(this.getSize());
            this.options.saveConfigFile(fn);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Set current path
     *
     * @param path Current path
     */
    public void setCurrentPath(String path) {
        this.options.setCurrentFolder(path);
        List<String> paths = new ArrayList<>();
        if (this.jComboBox_CurrentFolder.getItemCount() > 15) {
            this.jComboBox_CurrentFolder.removeItemAt(0);
        }
        for (int i = 0; i < this.jComboBox_CurrentFolder.getItemCount(); i++) {
            paths.add(this.jComboBox_CurrentFolder.getItemAt(i).toString());
        }
        if (!paths.contains(path)) {
            this.jComboBox_CurrentFolder.addItem(path);
        }
        this.jComboBox_CurrentFolder.setSelectedItem(path);

        PythonInteractiveInterpreter interp = this.consoleDock.getInterpreter();
        try {
            path = path.replace("\\", "/");
            interp.exec("mipylib.migl.currentfolder = '" + path + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public MapView getMapView() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LayersLegend getMapDocument() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JMenuBar getMainMenuBar() {
        return this.jMenuBar1;
    }

    @Override
    public JMenu getPluginMenu() {
        return this.jMenu_Apps;
    }

    @Override
    public JPanel getToolBarPanel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JButton getCurrentTool() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCurrentTool(AbstractButton value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JProgressBar getProgressBar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JLabel getProgressBarLabel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void openProjectFile(String fileName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Delete variables
     */
    public void delVariables() {
        PythonInteractiveInterpreter interp = this.consoleDock.getInterpreter();
        PyStringMap locals = (PyStringMap) interp.getLocals();
        PyList items = locals.items();
        String name;
        for (Object a : items) {
            PyTuple at = (PyTuple) a;
            name = at.__getitem__(0).toString();
            if (!this.loadObjects.contains(name)) {
                locals.__delitem__(name);
            }
        }
    }

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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //new FrmMain().setVisible(true);
                FrmMain frame = new FrmMain(null, null);
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private bibliothek.gui.dock.common.CControl cControl1;
    private javax.swing.JButton jButton_CurrentFolder;
    private javax.swing.JButton jButton_NewFile;
    private javax.swing.JButton jButton_OpenFile;
    private javax.swing.JButton jButton_Redo;
    private javax.swing.JButton jButton_RunScript;
    private javax.swing.JButton jButton_SaveAs;
    private javax.swing.JButton jButton_SaveFile;
    private javax.swing.JButton jButton_Undo;
    private javax.swing.JComboBox jComboBox_CurrentFolder;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem_About;
    private javax.swing.JMenuItem jMenuItem_AppsManager;
    private javax.swing.JMenuItem jMenuItem_CloseAllFiles;
    private javax.swing.JMenuItem jMenuItem_ColorDialog;
    private javax.swing.JMenuItem jMenuItem_ColorMaps;
    private javax.swing.JMenuItem jMenuItem_Comment;
    private javax.swing.JMenuItem jMenuItem_Copy;
    private javax.swing.JMenuItem jMenuItem_Cut;
    private javax.swing.JMenuItem jMenuItem_DeleteTab;
    private javax.swing.JMenuItem jMenuItem_Exit;
    private javax.swing.JMenuItem jMenuItem_FindReplace;
    private javax.swing.JMenuItem jMenuItem_Help;
    private javax.swing.JMenuItem jMenuItem_InsertTab;
    private javax.swing.JMenuItem jMenuItem_NewFile;
    private javax.swing.JMenuItem jMenuItem_OpenFile;
    private javax.swing.JMenuItem jMenuItem_Paste;
    private javax.swing.JMenuItem jMenuItem_SaveAs;
    private javax.swing.JMenuItem jMenuItem_SaveFile;
    private javax.swing.JMenuItem jMenuItem_Setting;
    private javax.swing.JMenu jMenu_Apps;
    private javax.swing.JMenu jMenu_Editor;
    private javax.swing.JMenu jMenu_File;
    private javax.swing.JMenu jMenu_Help;
    private javax.swing.JMenu jMenu_Options;
    private javax.swing.JPanel jPanel_Status;
    private javax.swing.JPanel jPanel_Toolbar;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JToolBar jToolBar_CurrentFolder;
    private javax.swing.JToolBar jToolBar_Editor;
    // End of variables declaration//GEN-END:variables

}
