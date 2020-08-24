/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.gui;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.action.CAction;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.meteoinfo.console.JConsole;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import org.meteoinfo.chart.IChartPanel;
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
    private ConsoleColors consoleColors;

    public ConsoleDockable(FrmMain parent, String startupPath, String id, String title, CAction... actions) {
        super(id, title, actions);

        this.parent = parent;
        this.startupPath = startupPath;
        this.consoleColors = new ConsoleColors(this.parent.getOptions().getLookFeel());
        console = new JConsole();
        console.setCommandColor(this.consoleColors.getCommandColor());
        console.setLocale(Locale.getDefault());
        //System.out.println(console.getFont());
        console.setPreferredSize(new Dimension(600, 400));
        console.println(new ImageIcon(this.getClass().getResource("/images/jython_small_c.png")));
        this.initializeConsole(console, parent.getCurrentFolder());
        JIntrospect nameComplete = new JIntrospect(this.interp);
        console.setNameCompletion(nameComplete);

        this.setTitleIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/console.svg"));

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
                        if (ke.isControlDown()) {
                            if (myWorker != null && !myWorker.isCancelled() && !myWorker.isDone()) {
                                myWorker.cancel(true);
                                myWorker = null;
                                //myWorker = new SmallWorker();
                                //myWorker.execute();
                                //enter();
                            }
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
     * Set Look and feel
     * @param laf Look and feel
     */
    public void setLookFeel(String laf) {
        this.consoleColors = new ConsoleColors(laf);
        this.console.setCommandColor(this.consoleColors.getCommandColor());
        this.console.setStyle(this.consoleColors.getCommandColor());
        this.console.repaint();        
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
        interp.setConsoleColors(consoleColors);
        String path = this.startupPath + File.separator + "pylib";
        String toolboxPath = this.startupPath + "/toolbox";
        String miPath = this.startupPath;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows") && miPath.substring(0, 1).equals("/")) {
            miPath = miPath.substring(1);
        }

        //this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(interp).start();
        try {
            interp.set("milapp", parent);
            interp.exec("import sys");
            interp.exec("import os");
            interp.exec("import datetime");
            interp.exec("sys.path.append('" + path + "')");
            interp.execfile_(path + "/milab.py");
            interp.exec("mipylib.plotlib.miplot.isinteractive = True");
            interp.exec("mipylib.migl.milapp = milapp");
            interp.exec("mipylib.migl.mifolder = '" + miPath + "'");
            currentPath = currentPath.replace("\\", "/");
            interp.exec("mipylib.migl.currentfolder = u'" + currentPath + "'");
            interp.exec("sys.path.append('" + toolboxPath + "')");
            if (isDebug) {
                interp.execfile_(path + "/milab_debug.py");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
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
        myWorker = new SwingWorker<String, String>() {

            @Override
            protected String doInBackground() throws Exception {
                interp.console.setStyle(consoleColors.getCommandColor());
                interp.console.println("evaluate selection...");
                interp.console.setStyle(consoleColors.getCodeLinesColor());
                interp.console.println(command);
                interp.console.setStyle(consoleColors.getCommandColor());
                interp.console.setFocusable(true);
                interp.console.requestFocusInWindow();
                try {
                    interp.exec(command);
                } catch (Exception e) {
                    e.printStackTrace();
                    interp.fireConsoleExecEvent();
                }

                return "";
            }

            @Override
            protected void done() {
                interp.console.print(">>> ", consoleColors.getPromptColor());
                interp.console.setStyle(consoleColors.getCommandColor());
                interp.exec("mipylib.plotlib.miplot.isinteractive = True");
            }
        };
        myWorker.execute();
    }

    /**
     * Do Enter key
     */
    public void enter() {
        interp.console.print(">>> ", this.consoleColors.getPromptColor());
        interp.console.setStyle(this.consoleColors.getCommandColor());
        interp.exec("mipylib.plotlib.miplot.isinteractive = True");
    }

    /**
     * Run a command line
     *
     * @param command Command line
     */
    public void exec(String command) {
        interp.console.setStyle(this.consoleColors.getCommandColor());
        this.interp.console.println("run script...");
        //this.interp.console.error(this.interp.err);
        this.interp.exec(command);
        //this.interp.push(command);
        this.interp.console.print(">>> ", this.consoleColors.getPromptColor());
        interp.exec("mipylib.plotlib.miplot.isinteractive = True");
        console.setStyle(consoleColors.getCommandColor());
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
                //interp.setOut(writer);
                //interp.setErr(writer);
                //System.setOut(printStream);
                //System.setErr(printStream);

                interp.console.setStyle(consoleColors.getCommandColor());
                interp.console.println("run script...");
                interp.console.setFocusable(true);
                interp.console.requestFocusInWindow();

                try {
                    interp.exec("mipylib.plotlib.miplot.isinteractive = False");
                    interp.exec("mipylib.plotlib.miplot.clf()");
                    interp.execfile(fn);
                    interp.exec("mipylib.plotlib.miplot.isinteractive = True");                    
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PythonInteractiveInterpreter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    interp.console.print(">>> ", consoleColors.getPromptColor());
                    interp.console.setStyle(consoleColors.getCommandColor());
                    interp.exec("mipylib.plotlib.miplot.isinteractive = True");
                }

                return "";
            }

            @Override
            protected void done() {
                //System.setOut(oout);
                //System.setErr(oerr);
                IChartPanel cp = parent.getFigureDock().getCurrentFigure();
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
            interp.console.setStyle(consoleColors.getCommandColor());
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
            this.interp.console.print(">>> ", consoleColors.getPromptColor());
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
                interp.console.setStyle(consoleColors.getCommandColor());
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
                    interp.exec("mipylib.plotlib.miplot.clf()");
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
                IChartPanel cp = parent.getFigureDock().getCurrentFigure();
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
