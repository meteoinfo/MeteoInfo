/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.gui;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.action.CAction;
import org.meteoinfo.console.JConsole;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;
import org.meteoinfo.chart.ChartPanel;
import org.meteoinfo.laboratory.codecomplete.JIntrospect;
import org.python.core.Py;

/**
 *
 * @author yaqiang
 */
public class ConsoleDockable extends DefaultSingleCDockable {

    private String startupPath;
    private FrmMain parent;
    private PythonInteractiveInterpreter interp;
    private JConsole console;
    private SwingWorker myWorker;

    public ConsoleDockable(FrmMain parent, String startupPath, String id, String title, CAction... actions) {
        super(id, title, actions);

        this.parent = parent;
        this.startupPath = startupPath;
        console = new JConsole();
        console.setLocale(Locale.getDefault());
        //System.out.println(console.getFont());
        console.setPreferredSize(new Dimension(600, 400));
        console.println(new ImageIcon(this.getClass().getResource("/images/jython_small_c.png")));
        this.initializeConsole(console, parent.getCurrentFolder());
        JIntrospect nameComplete = new JIntrospect(this.interp);
        console.setNameCompletion(nameComplete);

        this.getContentPane().add(console, BorderLayout.CENTER);
        console.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
                switch (ke.getKeyCode()) {
                    // Control-C
                    case (KeyEvent.VK_C):
                        if (myWorker != null && !myWorker.isCancelled() && !myWorker.isDone()) {
                            myWorker.cancel(true);
                            myWorker = null;
                            //myWorker = new SmallWorker();
                            //myWorker.execute();
                            //enter();
                        }
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent ke) {
            }

        });
    }

    /**
     * Initialize console
     *
     * @param console
     */
    private void initializeConsole(JConsole console, String currentPath) {
        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
        //String pluginPath = this.startupPath + File.separator + "plugins";
        //List<String> jarfns = GlobalUtil.getFiles(pluginPath, ".jar");

        //Issue java.lang.IllegalArgumentException: Cannot create PyString with non-byte value
        try {
            Py.getSystemState().setdefaultencoding("utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        interp = new PythonInteractiveInterpreter(console);
        String path = this.startupPath + File.separator + "pylib";
        String toolboxPath = this.startupPath + "/toolbox";
        String mapPath = this.startupPath + "/map";
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows") && mapPath.substring(0, 1).equals("/")) {
            mapPath = mapPath.substring(1);
        }
        if (isDebug) {
            path = "D:/MyProgram/Java/MeteoInfoDev/MeteoInfoLab/pylib";
            toolboxPath = "D:/MyProgram/Java/MeteoInfoDev/toolbox";
            mapPath = "D:/MyProgram/Distribution/Java/MeteoInfo/MeteoInfo/map";
        }
        //console.println(path);
        //console.println(toolboxPath);

        //this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(interp).start();
        try {
            interp.set("milapp", parent);
            interp.exec("import sys");
            interp.exec("import os");
            interp.exec("import datetime");
            //interp.exec("sys.setdefaultencoding('utf-8')");
            interp.exec("sys.path.append('" + path + "')");
            interp.exec("from milab import *");
            interp.exec("sys.path.append('" + toolboxPath + "')");
            //interp.exec("import toolbox");
            //interp.exec("from toolbox import *");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        try {
            interp.exec("mipylib.plotlib.miplot.isinteractive = True");
            interp.exec("mipylib.migl.milapp = milapp");
            interp.exec("mipylib.migl.mapfolder = '" + mapPath + "'");
            currentPath = currentPath.replace("\\", "/");
            interp.exec("mipylib.migl.currentfolder = u'" + currentPath + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //this.setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Get interactive interpreter
     *
     * @return Interactive interpreter
     */
    public PythonInteractiveInterpreter getInterpreter() {
        return this.interp;
    }

    /**
     * Get console
     *
     * @return Console
     */
    public JConsole getConsole() {
        return this.interp.console;
    }

    /**
     * Set startup path
     *
     * @param path Startup path
     */
    public void setStartupPath(String path) {
        this.startupPath = path;
    }

    /**
     * Set parent frame
     *
     * @param parent Parent frame
     */
    public void setParent(FrmMain parent) {
        this.parent = parent;
    }

    /**
     * Run a command line
     *
     * @param command Command line
     */
    public void run(String command) {
        interp.console.println("evaluate selection...");
        interp.console.setStyle(Color.blue);
        this.interp.console.println(command);
        interp.console.setStyle(Color.black);
        try {
            this.interp.exec(command);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            interp.console.print(">>> ", Color.red);
            interp.console.setStyle(Color.black);
            interp.exec("mipylib.plotlib.miplot.isinteractive = True");
        }
    }

    /**
     * Do Enter key
     */
    public void enter() {
        interp.console.print(">>> ", Color.red);
        interp.console.setStyle(Color.black);
        interp.exec("mipylib.plotlib.miplot.isinteractive = True");
    }

    /**
     * Run a command line
     *
     * @param command Command line
     */
    public void exec(String command) {
        this.interp.console.println("run script...");
        //this.interp.console.error(this.interp.err);
        this.interp.exec(command);
        //this.interp.push(command);
        this.interp.console.print(">>> ");
        interp.exec("mipylib.plotlib.miplot.isinteractive = True");
    }

    /**
     * Run a python file
     *
     * @param fn Python file name
     */
    public void execfile(final String fn) {
        myWorker = new SwingWorker<String, String>() {
            //PrintStream oout = System.out;
            //PrintStream oerr = System.err;

            @Override
            protected String doInBackground() throws Exception {
                //JTextPane jTextPane_Output = interp.console.getTextPane();
                //JTextPaneWriter writer = new JTextPaneWriter(jTextPane_Output);
                //JTextPanePrintStream printStream = new JTextPanePrintStream(System.out, jTextPane_Output);

                interp.console.println("run script...");
                interp.console.setFocusable(true);
                interp.console.requestFocusInWindow();
                //interp.setOut(writer);
                //interp.setErr(writer);
                //System.setOut(printStream);
                //System.setErr(printStream);

                try {
                    interp.exec("mipylib.plotlib.miplot.isinteractive = False");
                    interp.exec("clf()");
                    interp.execfile(fn);
                    interp.exec("mipylib.plotlib.miplot.isinteractive = True");
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PythonInteractiveInterpreter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    interp.console.print(">>> ", Color.red);
                    interp.console.setStyle(Color.black);
                    //interp.console.setForeground(Color.black);
                    interp.exec("mipylib.plotlib.miplot.isinteractive = True");
                }

                return "";
            }

            @Override
            protected void done() {
                //System.setOut(oout);
                //System.setErr(oerr);
                ChartPanel cp = parent.getFigureDock().getCurrentFigure();
                if (cp != null) {
                    cp.paintGraphics();
                }
            }
        };
        myWorker.execute();
    }

    /**
     * Run a Jython file text
     *
     * @param code Jython file text
     */
    public void runfile(String code) {
        try {
            this.interp.console.println("run script...");
            this.interp.setOut(this.interp.console.getOut());
            this.interp.setErr(this.interp.console.getErr());
            System.setOut(this.interp.console.getOut());
            System.setErr(this.interp.console.getErr());
            String encoding = EncodingUtil.findEncoding(code);
            if (encoding != null) {
                try {
                    interp.execfile(new ByteArrayInputStream(code.getBytes(encoding)));
                } catch (Exception e) {
                }
            } else {
                try {
                    interp.execfile(new ByteArrayInputStream(code.getBytes()));
                } catch (Exception e) {
                }
            }
            this.interp.console.print(">>> ");
        } catch (IOException ex) {
            Logger.getLogger(ConsoleDockable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Run Jython script
     *
     * @param code
     * @throws java.lang.InterruptedException
     */
    public void runPythonScript(final String code) throws InterruptedException {

        myWorker = new SwingWorker<String, String>() {
            //PrintStream oout = System.out;
            //PrintStream oerr = System.err;

            @Override
            protected String doInBackground() throws Exception {
                //JTextAreaWriter writer = new JTextAreaWriter(jTextArea_Output);
                //JTextAreaPrintStream printStream = new JTextAreaPrintStream(System.out, jTextArea_Output);
                //jTextArea_Output.setText("");

                //JTextPane jTextPane_Output = interp.console.getTextPane();
                //JTextPaneWriter writer = new JTextPaneWriter(jTextPane_Output);
                //JTextPanePrintStream printStream = new JTextPanePrintStream(System.out, jTextPane_Output);
                interp.console.println("run script...");
                interp.console.setFocusable(true);
                interp.console.requestFocusInWindow();
                //interp.setOut(writer);
                //interp.setErr(writer);
                //System.setOut(printStream);
                //System.setErr(printStream);

                String encoding = "utf-8";
                try {
                    interp.exec("mipylib.plotlib.miplot.isinteractive = False");
                    interp.exec("clf()");
                    interp.execfile(new ByteArrayInputStream(code.getBytes(encoding)));
                    interp.exec("mipylib.plotlib.miplot.isinteractive = True");
                } catch (Exception e) {
                    e.printStackTrace();
                    //interp.console.print(">>> ", Color.red);
                    //interp.console.setStyle(Color.black);
                    //interp.console.setForeground(Color.black);
                    interp.exec("mipylib.plotlib.miplot.isinteractive = True");
                    interp.fireConsoleExecEvent();
                }

                //String encoding = EncodingUtil.findEncoding(code);                
//                if (encoding != null) {
//                    try {
//                        interp.execfile(new ByteArrayInputStream(code.getBytes(encoding)));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        interp.console.print(">>> ", Color.red);
//                        //interp.console.setStyle(Color.black);
//                        //interp.console.setForeground(Color.black);
//                    }
//                } else {
//                    try {
//                        interp.execfile(new ByteArrayInputStream(code.getBytes()));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        interp.console.print(">>> ", Color.red);
//                        //interp.console.setStyle(Color.black);
//                        //interp.console.setForeground(Color.black);
//                    }
//                }
                return "";
            }

            @Override
            protected void done() {
                //System.setOut(oout);
                //System.setErr(oerr);
                ChartPanel cp = parent.getFigureDock().getCurrentFigure();
                if (cp != null) {
                    cp.paintGraphics();
                }
            }
        };
        myWorker.execute();
    }
    
    class SmallWorker extends SwingWorker<String, String> {

        @Override
        protected String doInBackground() throws Exception {
            interp.exec("print('Thread cancled!')");
            return "";
        }
        
    }
}
