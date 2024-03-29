/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.map;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Font;
import java.awt.FontFormatException;
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
import javax.xml.parsers.ParserConfigurationException;

import org.meteoinfo.common.DataConvert;
import org.meteoinfo.ui.util.FontUtil;
import org.meteoinfo.common.util.GlobalUtil;
import org.meteoinfo.map.config.Options;
import org.meteoinfo.map.forms.FrmMain;
import org.meteoinfo.map.forms.FrmTextEditor;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.InteractiveConsole;
import org.python.util.PythonInterpreter;
import org.xml.sax.SAXException;

/**
 *
 * @author yaqiang
 */
public class MeteoInfoMap {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        //registerFonts();
        if (args.length >= 1){
            if (args[0].equalsIgnoreCase("-r")) {
                String fontPath = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "fonts";
                List<String> fontFns = GlobalUtil.getFiles(fontPath, ".ttc");
                for (String fontFn : fontFns){
                    System.out.println("Register: " + fontFn);
                    FontUtil.registerFont(fontFn);
                }
                args = (String[]) DataConvert.resizeArray(args, args.length - 1);
            }
        }
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("-e")) {
                runTextEditor(args);
            } else if (args[0].equalsIgnoreCase("-i")) {
                runInteractive();
            } else if (args[0].equalsIgnoreCase("-b")) {
                if (args.length == 1) {
                    System.out.println("Script file name is needed!");
                    System.exit(0);
                } else {
                    String fn = args[1];
                    if (new File(fn).isFile()) {
                        System.setProperty("java.awt.headless", "true");
                        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                        System.out.println("Headless mode: " + ge.isHeadless());
                        runScript(args, fn, 1);
                    } else {
                        System.out.println("The script file does not exist!");
                        System.exit(0);
                    }
                }
            } else if (args[0].equalsIgnoreCase("-eng") || args[0].equalsIgnoreCase("-cn")) {
                runApplication(args[0].substring(1));
            } else if (args[0].equalsIgnoreCase("-r")) {
                String fontPath = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "fonts";
                List<String> fontFns = GlobalUtil.getFiles(fontPath, ".ttc");
                for (String fontFn : fontFns){
                    System.out.println("Register: " + fontFn);
                    FontUtil.registerFont(fontFn);
                }
                runApplication();
            } else {
                String fn = args[0];
                if (new File(fn).isFile()) {
                    runScript(args, fn, 0);
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
        String ext = GlobalUtil.getFileExtension(fn);
        System.out.println("Running Jython script...");
        PySystemState state = new PySystemState();
        if (args.length > idx + 1) {
            for (int i = idx + 1; i < args.length; i++) {
                state.argv.append(new PyString(args[i]));
            }
        }

        PythonInterpreter interp = new PythonInterpreter(null, state);
        String pluginPath = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "plugins";
        List<String> jarfns = GlobalUtil.getFiles(pluginPath, ".jar");
        String path = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "pylib";
        interp.exec("import sys");
        interp.exec("sys.path.append('" + path + "')");
        for (String jarfn : jarfns) {
            interp.exec("sys.path.append('" + jarfn + "')");
        }
        interp.execfile(fn);
        System.exit(0);
    }

    private static void runInteractive() {
        String path = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "pylib";
        InteractiveConsole console = new InteractiveConsole();
        try {
            console.exec("import sys");
            console.exec("sys.path.append('" + path + "')");
        } catch (Exception e) {
            e.printStackTrace();
        }
        console.interact();
    }

    private static void runTextEditor(final String args[]) {
        /* Set look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            //UIManager.setLookAndFeel("javax.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmTextEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmTextEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmTextEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmTextEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                FrmTextEditor frmTE = new FrmTextEditor();
                frmTE.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frmTE.setLocationRelativeTo(null);
                frmTE.setVisible(true);
                if (args.length > 1) {
                    String fn = args[1];
                    if (new File(fn).isFile()) {
                        frmTE.openFiles(new File[]{new File(fn)});
                    }
                }
            }
        });
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
        //System.out.println(startupPath);
        return startupPath;
    }

    /**
     * Load configure file
     *
     * @return Configure file
     */
    private static Options loadConfigureFile(String startupPath) {
        String fn = startupPath + File.separator + "config.xml";
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

    private static void runApplication() {
        runApplication(null);
    }

    private static void runApplication(final String locale) {
        String startupPath = getStartupPath();
        Options options = loadConfigureFile(startupPath);

        /* Set look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
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
                Logger.getLogger(MeteoInfoMap.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //</editor-fold>

        //Enable window decorations
        if (laf.startsWith("Flat")) {
            if (options.isLafDecorated()) {
                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);
            }
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                        getInputArguments().toString().contains("jdwp");

                if (locale != null){
                    switch (locale.toLowerCase()){
                        case "eng":
                            Locale.setDefault(Locale.ENGLISH);
                            break;
                        case "cn":
                            Locale.setDefault(Locale.CHINA);
                            break;
                    }
                }

                StackWindow sw = null;
                if (!isDebug) {
                    sw = new StackWindow("Show Exception Stack", 600, 400);
                    Thread.UncaughtExceptionHandler handler = sw;
                    Thread.setDefaultUncaughtExceptionHandler(handler);
                    System.setOut(sw.printStream);
                    System.setErr(sw.printStream);
                }

                FontUtil.registerWeatherFont();
                FrmMain frame = new FrmMain(startupPath, options);
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                frame.setVisible(true);
                if (sw != null) {
                    sw.setLocationRelativeTo(frame);
                }
            }
        });
    }

    private static void registerFonts() {
        FontUtil.registerWeatherFont();
        String fn = GlobalUtil.getAppPath(FrmMain.class);
        String path = fn + File.separator + "font";
        File pathDir = new File(path);
        if (pathDir.isDirectory()) {
            File[] files = pathDir.listFiles();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            for (File file : files) {
                try {
                    Font font = Font.createFont(Font.TRUETYPE_FONT, file);
                    ge.registerFont(font);
                } catch (FontFormatException ex) {
                    Logger.getLogger(MeteoInfoMap.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(MeteoInfoMap.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
