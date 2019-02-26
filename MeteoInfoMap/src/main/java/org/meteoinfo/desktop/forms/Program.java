/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.desktop.forms;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.codehaus.groovy.control.CompilationFailedException;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.util.FontUtil;
import org.meteoinfo.global.util.GlobalUtil;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.InteractiveConsole;
import org.python.util.PythonInterpreter;

/**
 *
 * @author yaqiang
 */
public class Program {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        //registerFonts();
        if (args.length >= 1){
            if (args[0].equalsIgnoreCase("-r")) {
                String fontPath = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "fonts";
                //fontPath = "D:\\MyProgram\\java\\MeteoInfoDev\\MeteoInfo\\fonts";
                List<String> fontFns = GlobalUtil.getFiles(fontPath, ".ttc");
                for (String fontFn : fontFns){
                    System.out.println("Register: " + fontFn);
                    FontUtil.registerFont(fontFn);
                }
                args = (String[])DataConvert.resizeArray(args, args.length - 1);
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
                //fontPath = "D:\\MyProgram\\java\\MeteoInfoDev\\MeteoInfo\\fonts";
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
        try {
            String ext = GlobalUtil.getFileExtension(fn);
            if (ext.equals("groovy")) {
                System.out.println("Running Groovy script...");
                GroovyShell shell = new GroovyShell();
                //shell.setVariable("miapp", new FrmMainOld());
                Script script = shell.parse(new File(fn));
                script.run();
                System.exit(0);
            } else if (ext.equals("py")) {
                System.out.println("Running Jython script...");
                //PySystemState state = Py.getSystemState();
                //Py.getSystemState().setdefaultencoding("utf-8");
                PySystemState state = new PySystemState();
                //state.setdefaultencoding("utf-8");
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
                //interp.set("mis", mis);
                interp.exec("sys.path.append('" + path + "')");
                //interp.exec("import mipylib");
                //interp.exec("from mipylib.miscript import *");
                //interp.exec("from meteoinfo.numeric.JNumeric import *");
                for (String jarfn : jarfns) {
                    interp.exec("sys.path.append('" + jarfn + "')");
                }
                interp.execfile(fn);
                System.exit(0);
            }
        } catch (CompilationFailedException ex) {
            Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void runInteractive() {
        //String path = GlobalUtil.getAppPath(FrmMain.class) + File.separator + "pylib";
        InteractiveConsole console = new InteractiveConsole();
        try {
            //console.set("mis", mis);
            console.exec("import sys");
            //console.exec("sys.path.append('" + path + "')");
            //console.exec("from milab import *");
            //console.exec("mipylib.miplot.isinteractive = True");
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

    private static void runApplication() {
        runApplication(null);
    }

    private static void runApplication(final String locale) {
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

            //WebLookAndFeel.install();
            //UIManager.setLookAndFeel("com.alee.laf.WebLookAndFeel");
            //com.jtattoo.plaf.acryl.AcrylLookAndFeel.setTheme("Green", "INSERT YOUR LICENSE KEY HERE", "my company");
            //UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
            //UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
            //UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");
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

//        System.setProperty("Quaqua.tabLayoutPolicy", "wrap");
//        try {
//            JFrame.setDefaultLookAndFeelDecorated(true);
//            JDialog.setDefaultLookAndFeelDecorated(true);
//            UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
//        } catch (Exception e) {
//        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
//                new Thread() {
//                    @Override
//                    public void run() {
//                        try {
//                            final SplashScreen splash = SplashScreen.getSplashScreen();
//                            if (splash == null){
//                                System.out.println("SplashScreen.getSplashScreen() returned null");
//                                return;
//                            }
//                            Graphics2D g = splash.createGraphics();
//                            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//                            g.setFont(new Font("Arial", Font.BOLD, 60));
//                            g.setColor(Color.red);
//                            g.drawString("MeteoInfo", 100, 200);
//                            splash.update();
//                            Thread.sleep(1000);
//                            //splash.setImageURL(Program.class.getResource("/meteoinfo/resources/logo.png"));
//                            //splash.update();
//                        } catch (Exception e) {
//                        }
//                    }
//                }.start();

                boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                        getInputArguments().toString().contains("jdwp");
//                if (isDebug) {
//                    Locale.setDefault(Locale.ENGLISH);
//                }

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

                //registerFonts();
                org.meteoinfo.global.util.FontUtil.registerWeatherFont();
                FrmMain frame = new FrmMain();
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                //frame.setLocationRelativeTo(null);
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
        //fn = fn.substring(0, fn.lastIndexOf("/"));
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
                    Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
