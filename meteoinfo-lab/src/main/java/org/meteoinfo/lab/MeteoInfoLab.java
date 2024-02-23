/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.lab;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.xml.parsers.ParserConfigurationException;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import org.meteoinfo.common.DataConvert;
import org.meteoinfo.ui.util.FontUtil;
import org.meteoinfo.common.util.GlobalUtil;
import org.meteoinfo.lab.gui.FrmMain;
import org.meteoinfo.console.jython.MyPythonInterpreter;
import org.python.core.Py;
import org.python.core.PySystemState;
import org.python.util.InteractiveConsole;
import org.xml.sax.SAXException;

/**
 *
 * @author Yaqiang Wang
 */
public class MeteoInfoLab {

    /**
     * Disable illegal access warning message
     */
    @SuppressWarnings("unchecked")
    public static void disableAccessWarnings() {
        try {
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object unsafe = field.get(null);

            Method putObjectVolatile = unsafeClass.getDeclaredMethod("putObjectVolatile", Object.class, long.class, Object.class);
            Method staticFieldOffset = unsafeClass.getDeclaredMethod("staticFieldOffset", Field.class);

            Class loggerClass = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field loggerField = loggerClass.getDeclaredField("logger");
            Long offset = (Long) staticFieldOffset.invoke(unsafe, loggerField);
            putObjectVolatile.invoke(unsafe, loggerClass, offset, null);
        } catch (Exception ignored) {
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // Disable illegal access warning message  
        disableAccessWarnings();

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("-r")) {
                String fontPath = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "fonts";
                //fontPath = "D:\\MyProgram\\java\\MeteoInfoDev\\MeteoInfo\\fonts";
                List<String> fontFns = GlobalUtil.getFiles(fontPath, ".ttc");
                for (String fontFn : fontFns) {
                    System.out.println("Register: " + fontFn);
                    FontUtil.registerFont(fontFn);
                }
                args = (String[]) DataConvert.resizeArray(args, args.length - 1);
            }
        }
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("-i")) {
                runInteractive();
            } else if (args[0].equalsIgnoreCase("-b")) {
                if (args.length == 1) {
                    System.out.println("Script file name is needed!");
                    System.exit(0);
                } else {
                    String fn = args[1];
                    File file = new File(fn);
                    if (file.isFile()) {
                        System.setProperty("java.awt.headless", "true");
                        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                        System.out.println("Headless mode: " + ge.isHeadless());
                        runScript(args, file.getCanonicalPath(), 1);
                    } else {
                        System.out.println("The script file does not exist!");
                        System.exit(0);
                    }
                }
            } else if (args[0].equalsIgnoreCase("-eng")) {
                runApplication(true);
            } else {
                String fn = args[0];
                File file = new File(fn);
                if (file.isFile()) {
                    runScript(args, file.getCanonicalPath(), 0);
                } else {
                    System.out.println("The script file does not exist!");
                    System.exit(0);
                }
            }
        } else {
            runApplication();
        }
    }

    private static void runScript(String args[], String fn, int idx) {
        FontUtil.registerWeatherFont();

        System.out.println("Running Jython script: " + fn);
        PySystemState state = new PySystemState();
        if (args.length > idx + 1) {
            for (int i = idx + 1; i < args.length; i++) {
                state.argv.append(Py.newStringOrUnicode(args[i]));
                //state.argv.append(new PyString(args[i]));
            }
        }
        //state.setdefaultencoding("utf-8");
        //PythonInterpreter interp = new PythonInterpreter(null, state);
        MyPythonInterpreter interp = new MyPythonInterpreter(null, state);

        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
        String pyPath, toolboxPath, miPath;
        miPath = GlobalUtil.getAppPath(FrmMain.class);
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows") && miPath.substring(0, 1).equals("/")) {
            miPath = miPath.substring(1);
        }
        if (miPath.endsWith("classes")) {
            Path path = new File(miPath).toPath();
            path = path.getParent().getParent();
            miPath = path.toString();
        }
        pyPath = miPath + File.separator + "pylib";
        toolboxPath = miPath + File.separator + "toolbox";
        if (miPath.endsWith("meteoinfo-lab")) {
            Path path = new File(miPath).toPath();
            path = path.getParent();
            path = path.resolve(Paths.get("auxdata", "toolbox"));
            toolboxPath = path.toFile().getAbsolutePath();
        }
        pyPath = pyPath.replace("\\", "/");
        miPath = miPath.replace("\\", "/");
        toolboxPath = toolboxPath.replace("\\", "/");

        /*if (isDebug) {
            pyPath = "D:/MyProgram/java/MeteoInfoDev/MeteoInfo/meteoinfo-lab/pylib";
            toolboxPath = "D:/MyProgram/java/MeteoInfoDev/toolbox";
            miPath = "D:/MyProgram/Distribution/Java/MeteoInfo/MeteoInfo";
        } else {
            //String pluginPath = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "plugins";
            //List<String> jarfns = GlobalUtil.getFiles(pluginPath, ".jar");
            miPath = GlobalUtil.getAppPath(FrmMain.class);
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows") && miPath.substring(0, 1).equals("/")) {
                miPath = miPath.substring(1);
            }
            pyPath = miPath + File.separator + "pylib";
            toolboxPath = miPath + "/toolbox";
        }*/

        try {
            interp.exec("import sys");
            interp.exec("import os");
            interp.exec("import datetime");
            interp.exec("sys.path.append(u'" + pyPath + "')");
            //interp.exec("from milab import *");
            interp.execfile(pyPath + "/milab.py");
            if (!isDebug) {
                interp.exec("sys.path.append(u'" + toolboxPath + "')");
                //interp.exec("from toolbox import *");
            }
            interp.exec("mipylib.plotlib.miplot.batchmode = True");
            interp.exec("mipylib.migl.interactive = False");
            interp.exec("mipylib.migl.mifolder = u'" + miPath + "'");
            System.out.println("mipylib is loaded...");
            interp.execfile(fn);
            System.out.println("End of run!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    private static void runInteractive() {
        String startPath = System.getProperty("user.dir");
        String miPath = GlobalUtil.getAppPath(FrmMain.class);
        String path = miPath + File.separator + "pylib";
        String toolboxPath = miPath + "/toolbox";
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows") && miPath.substring(0, 1).equals("/")) {
            miPath = miPath.substring(1);
        }
        InteractiveConsole console = new InteractiveConsole();
        try {
            console.exec("import sys");
            console.exec("import os");
            console.exec("import datetime");
            console.exec("sys.path.append('" + path + "')");
            //console.exec("from milab import *");
            console.execfile(path + "/milab.py");
            console.exec("sys.path.append('" + toolboxPath + "')");
            //console.exec("from toolbox import *");
            console.exec("mipylib.plotlib.miplot.isinteractive = True");
            console.exec("mipylib.migl.mifolder = '" + miPath + "'");
            console.exec("mipylib.migl.currentfolder = '" + startPath + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
        console.interact();
    }

    private static void runApplication() {
        runApplication(false);
        //runApplication(true);
    }

    /**
     * Get startup path.
     *
     * @return Startup path.
     */
    private static String getStartupPath() {
        String startupPath = GlobalUtil.getAppPath(FrmMain.class);
        if (startupPath.endsWith("classes")) {
            Path path = new File(startupPath).toPath();
            path = path.getParent().getParent();
            startupPath = path.toString();
        }
        return startupPath;
    }

    /**
     * Load configure file
     *
     * @return Configure file
     */
    private static Options loadConfigureFile(String startupPath) {
        String fn = startupPath + File.separator + "milconfig.xml";
        Options options = new Options();
        if (new File(fn).exists()) {
            try {
                options.loadConfigFile(fn);
            } catch (SAXException | IOException | ParserConfigurationException ex) {
                Logger.getLogger(FrmMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return options;
    }

    private static void runApplication(final boolean isEng) {
        String startupPath = getStartupPath();
        Options options = loadConfigureFile(startupPath);
        String laf = options.getLookFeel();
        if (laf.equals("FlatLightLaf")) {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (laf.equals("FlatDarculaLaf")) {
            try {
                UIManager.setLookAndFeel(new FlatDarculaLaf());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (laf.equals("FlatDarkLaf")) {
            try {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (laf.equals("FlatIntelliJLaf")) {
            try {
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (laf.equals("FlatMacLightLaf")) {
            try {
                UIManager.setLookAndFeel(new FlatMacLightLaf());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (laf.equals("FlatMacDarkLaf")) {
            try {
                UIManager.setLookAndFeel(new FlatMacDarkLaf());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                String lafName;
                switch (laf) {
                    case "CDE/Motif":
                        lafName = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
                        break;
                    case "Metal":
                        lafName = "javax.swing.plaf.metal.MetalLookAndFeel";
                        break;
                    case "Windows":
                        lafName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
                        break;
                    case "Windows Classic":
                        lafName = "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel";
                        break;
                    case "Nimbus":  
                        lafName = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
                        break;
                    case "Mac":
                        lafName = "com.sun.java.swing.plaf.mac.MacLookAndFeel";
                        break;
                    case "GTK":
                        lafName = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
                        break;
                    default:
                        lafName = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
                        break;
                }
                
                UIManager.setLookAndFeel(lafName);

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                Logger.getLogger(MeteoInfoLab.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //Enable window decorations
        if (laf.startsWith("Flat")) {
            if (options.isLafDecorated()) {
                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);
            }
        }
        
        //</editor-fold>
        //System.setProperty("-Dsun.java2d.dpiaware", "false");

        /* Create and display the form */
        SwingUtilities.invokeLater(new Runnable() {
            //java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (isEng) {
                    Locale.setDefault(Locale.ENGLISH);
                }

                System.out.println("Register weather font...");
                FontUtil.registerWeatherFont();
                System.out.println("Open main form...");
                FrmMain frame = new FrmMain(startupPath, options);
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }
}
