/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.lab.gui;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.action.CAction;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.meteoinfo.console.ConsoleColors;
import org.meteoinfo.console.JConsole;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.meteoinfo.chart.IChartPanel;
import org.meteoinfo.console.jython.JIntrospect;
import org.meteoinfo.console.jython.PythonInteractiveInterpreter;
import org.python.core.Py;
import org.python.core.PyException;

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
    private Thread myThread;
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
        System.out.println("Initialize console...");
        this.initializeConsole(console, parent.getCurrentFolder());
        JIntrospect nameComplete = new JIntrospect(this.interp);
        console.setNameCompletion(nameComplete);

        System.out.println("Set title icon...");
        this.setTitleIcon(new FlatSVGIcon("org/meteoinfo/lab/icons/console.svg"));

        this.getContentPane().add(console, BorderLayout.CENTER);
        // Global Ctrl+C dispatcher replacing the broken console KeyListener.
        // Console KeyListeners only fire when JConsole has focus. This dispatcher
        // intercepts Ctrl+C regardless of which component (editor, variable panel, etc.)
        // currently holds focus within the application window.
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new KeyEventDispatcher() {
                    @Override
                    public boolean dispatchKeyEvent(KeyEvent e) {
                        if (e.getID() == KeyEvent.KEY_PRESSED
                                && e.getKeyCode() == KeyEvent.VK_C
                                && e.isControlDown()) {

                            Thread workerThread = myThread;
                            SwingWorker<?, ?> sw = myWorker;
                            boolean interrupted = false;

                            // [FIX-2] Cooperative interrupt instead of Thread.stop()
                            if (workerThread != null && workerThread.isAlive()) {
                                workerThread.interrupt();
                                interrupted = true;
                            }
                            if (sw != null && !sw.isDone() && !sw.isCancelled()) {
                                sw.cancel(true);
                                interrupted = true;
                            }

                            if (interrupted) {
                                // Visual feedback on EDT
                                SwingUtilities.invokeLater(() -> {
                                    console.print("^C\n", Color.RED);
                                    parent.getProgressBar().setVisible(false);
                                });
                            }

                            e.consume();
                            return true;
                        }
                        return false;
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
        //System.out.println("New Jython interpreter...");
        interp = new PythonInteractiveInterpreter(console);
        interp.setConsoleColors(consoleColors);
        String pyPath = this.startupPath + File.separator + "pylib";
        pyPath = pyPath.replace("\\", "/");
        String toolboxPath = this.startupPath + "/toolbox";
        if (this.startupPath.endsWith("meteoinfo-lab")) {
            Path path = new File(this.startupPath).toPath();
            path = path.getParent();
            path = path.resolve(Paths.get("auxdata", "toolbox"));
            toolboxPath = path.toFile().getAbsolutePath();
        }
        toolboxPath = toolboxPath.replace("\\", "/");
        String miPath = this.startupPath;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows") && miPath.substring(0, 1).equals("/")) {
            miPath = miPath.substring(1);
        }
        miPath = miPath.replace("\\", "/");

        //System.out.println("New interpreter thread...");
        //this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(interp).start();
        try {
            interp.set("milapp", parent);
            interp.exec("import sys");
            interp.exec("import os");
            interp.exec("import datetime");
            System.out.println("Append path: " + pyPath);
            interp.exec("sys.path.append(u'" + pyPath + "')");
            System.out.println("Run milab.py ...");
            interp.execfile_(pyPath + "/milab.py");
            System.out.println("Set isinteractive...");
            interp.exec("mipylib.migl.interactive = True");
            System.out.println("Set milapp...");
            interp.exec("mipylib.migl.milapp = milapp");
            System.out.println("Set mifolder: " + miPath);
            interp.exec("mipylib.migl.mifolder = u'" + miPath + "'");
            currentPath = currentPath.replace("\\", "/");
            System.out.println("Set currentfolder: " + currentPath);
            interp.exec("mipylib.migl.currentfolder = u'" + currentPath + "'");
            System.out.println("Append path: " + toolboxPath);
            interp.exec("sys.path.append(u'" + toolboxPath + "')");
            if (isDebug) {
                System.out.println("Run milab_debug.py ...");
                interp.execfile_(pyPath + "/milab_debug.py");
            }
            System.out.println("Interpreter done...");
        } catch (Exception e) {
            System.out.println(e);
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
     * Get SwingWorker
     *
     * @return The SwingWorker
     */
    public SwingWorker getSwingWorker() {
        return this.myWorker;
    }

    public Thread getMyThread() {
        return this.myThread;
    }

    /**
     * Safe Jython execution wrapper that respects Thread.interrupt().
     *
     * Jython does NOT automatically throw InterruptedException on interrupt().
     * It raises KeyboardInterrupt as a PyException between bytecode instructions.
     * All exec/execfile calls MUST go through this method for Ctrl+C to work.
     */
    private void safeExec(String command) {
        if (Thread.interrupted()) {
            SwingUtilities.invokeLater(() ->
                    console.print("^C Execution cancelled before start.\n", consoleColors.getPromptColor()));
            return;
        }
        try {
            interp.exec(command);
        } catch (PyException pyEx) {
            String typeName = pyEx.type != null ? pyEx.type.getType().getName() : "";
            if ("KeyboardInterrupt".equals(typeName)) {
                SwingUtilities.invokeLater(() ->
                        console.print("\nKeyboardInterrupt\n", consoleColors.getPromptColor()));
            } else {
                throw pyEx;
            }
        }
        if (Thread.interrupted()) {
            SwingUtilities.invokeLater(() ->
                    console.print("\nKeyboardInterrupt (post-exec)\n", consoleColors.getPromptColor()));
        }
    }

    /**
     * Safe execfile wrapper with encoding support
     */
    private void safeExecFile(String fn) {
        if (Thread.interrupted()) {
            SwingUtilities.invokeLater(() ->
                    console.print("^C Execution cancelled before start.\n", consoleColors.getPromptColor()));
            return;
        }
        try {
            interp.execfile(fn);
        } catch (PyException pyEx) {
            String typeName = pyEx.type != null ? pyEx.type.getType().getName() : "";
            if ("KeyboardInterrupt".equals(typeName)) {
                SwingUtilities.invokeLater(() ->
                        console.print("\nKeyboardInterrupt\n", consoleColors.getPromptColor()));
            } else {
                throw pyEx;
            }
        }
        if (Thread.interrupted()) {
            SwingUtilities.invokeLater(() ->
                    console.print("\nKeyboardInterrupt (post-exec)\n", consoleColors.getPromptColor()));
        }
    }

    /**
     * Safe execfile from InputStream
     */
    private void safeExecFile(java.io.InputStream is) {
        if (Thread.interrupted()) {
            SwingUtilities.invokeLater(() ->
                    console.print("^C Execution cancelled before start.\n", consoleColors.getPromptColor()));
            return;
        }
        try {
            interp.execfile(is);
        } catch (PyException pyEx) {
            String typeName = pyEx.type != null ? pyEx.type.getType().getName() : "";
            if ("KeyboardInterrupt".equals(typeName)) {
                SwingUtilities.invokeLater(() ->
                        console.print("\nKeyboardInterrupt\n", consoleColors.getPromptColor()));
            } else {
                throw pyEx;
            }
        }
        if (Thread.interrupted()) {
            SwingUtilities.invokeLater(() ->
                    console.print("\nKeyboardInterrupt (post-exec)\n", consoleColors.getPromptColor()));
        }
    }

    /**
     * Run a command line with safe interruption support.
     * Uses safeExec to handle KeyboardInterrupt properly.
     * Sets myThread for Ctrl+C dispatcher visibility.
     * All UI operations are strictly on EDT via invokeLater.
     */
    public void run(String command) {
        // Use a dedicated thread instead of SwingWorker for consistency
        // with execJythonFile/runJythonScript, ensuring interrupt() works reliably.
        // SwingWorker.cancel(true) only sets a flag; Jython won't see it without
        // explicit Thread.interrupt() on the actual execution thread.
        myThread = new Thread(() -> {
            final boolean[] interrupted = {false};

            SwingUtilities.invokeLater(() -> {
                parent.getProgressBar().setVisible(true);
                console.print("evaluate selection...\n", consoleColors.getCommandColor());
                console.print(command + "\n", consoleColors.getCodeLinesColor());
                console.setFocusable(true);
                console.requestFocusInWindow();
            });

            try {
                safeExec(command);
            } catch (Exception e) {
                interrupted[0] = true;
                if (!(e instanceof PyException &&
                        "KeyboardInterrupt".equals(
                                ((PyException) e).type != null ? ((PyException) e).type.getType().getName() : ""))) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    final String errorText = sw.toString();

                    SwingUtilities.invokeLater(() ->
                            console.print(errorText, consoleColors.getErrorColor())
                    );
                    interp.fireConsoleExecEvent();
                }
            } finally {
                SwingUtilities.invokeLater(() -> {
                    // Only print prompt on normal completion.
                    // When interrupted, safeExec already printed "^C" / "KeyboardInterrupt".
                    if (!interrupted[0]) {
                        console.print(">>> ", consoleColors.getPromptColor());
                    }

                    // Always reset input attributes regardless of interrupt status
                    MutableAttributeSet cmdAttr = new SimpleAttributeSet();
                    StyleConstants.setForeground(cmdAttr, consoleColors.getCommandColor());
                    Font f = console.getTextPane().getFont();
                    StyleConstants.setFontFamily(cmdAttr, f.getFamily());
                    StyleConstants.setFontSize(cmdAttr, f.getSize());
                    console.getTextPane().setCharacterAttributes(cmdAttr, true);

                    interp.exec("mipylib.plotlib.miplot.isinteractive = True");
                    parent.getProgressBar().setVisible(false);
                });

                // Clear thread reference to prevent stale interrupts
                myThread = null;
            }
        }, "Jython-RunCommand");

        myThread.start();
    }

    /**
     * Run a Jython file
     *
     * @param fn Jython file name
     */
    public void runJythonFile(final String fn) {
        myThread = new Thread(new Runnable() {
            final boolean[] interrupted = {false};

            @Override
            public void run() {
                // UI updates on EDT only
                SwingUtilities.invokeLater(() -> {
                    parent.getProgressBar().setVisible(true);
                    console.print("run script...\n", consoleColors.getCommandColor());
                    console.setFocusable(true);
                    console.requestFocusInWindow();
                });

                try {
                    safeExec("mipylib.plotlib.miplot.set_interactive(False)");
                    safeExec("mipylib.plotlib.miplot.clf()");
                    safeExecFile(fn);
                    safeExec("mipylib.plotlib.miplot.set_interactive(True)");
                } catch (Exception e) {
                    interrupted[0] = true;
                    if (!(e instanceof PyException &&
                            "KeyboardInterrupt".equals(((PyException)e).type != null ?
                                    ((PyException)e).type.getType().getName() : ""))) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        final String errorText = sw.toString();

                        SwingUtilities.invokeLater(() ->
                                console.print(errorText, consoleColors.getErrorColor())
                        );
                    }
                } finally {
                    // ALWAYS restore state and clear thread reference
                    try {
                        safeExec("mipylib.plotlib.miplot.set_interactive(True)");
                    } catch (Exception ignored) {}

                    SwingUtilities.invokeLater(() -> {
                        if (interrupted[0]) {
                            console.print(">>> ", consoleColors.getPromptColor());
                        }
                        MutableAttributeSet cmdAttr = new SimpleAttributeSet();
                        StyleConstants.setForeground(cmdAttr, consoleColors.getCommandColor());
                        Font f = console.getTextPane().getFont();
                        StyleConstants.setFontFamily(cmdAttr, f.getFamily());
                        StyleConstants.setFontSize(cmdAttr, f.getSize());
                        console.getTextPane().setCharacterAttributes(cmdAttr, true);

                        IChartPanel cp = parent.getFigureDock().getCurrentFigure();
                        if (cp != null) {
                            cp.paintGraphics();
                        }
                        parent.getProgressBar().setVisible(false);
                    });

                    myThread = null;  // Clear reference to prevent stale interrupts
                }
            }
        }, "Jython-ExecFile");

        myThread.start();
    }

    /**
     * Run Jython script with safe interruption support.
     * Same pattern as execJythonFile: EDT-safe UI + safeExec + proper cleanup.
     */
    public void runJythonScript(final String code) throws InterruptedException {
        myThread = new Thread(new Runnable() {
            final boolean[] interrupted = {false};

            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    parent.getProgressBar().setVisible(true);
                    console.print("run script...\n", consoleColors.getCommandColor());
                    console.setFocusable(true);
                    console.requestFocusInWindow();
                });

                String encoding = "utf-8";
                try {
                    safeExec("mipylib.plotlib.miplot.set_interactive(False)");
                    safeExec("mipylib.plotlib.miplot.clf()");
                    safeExecFile(new ByteArrayInputStream(code.getBytes(encoding)));
                    safeExec("mipylib.plotlib.miplot.set_interactive(True)");
                } catch (Exception e) {
                    interrupted[0] = true;
                    if (!(e instanceof PyException &&
                            "KeyboardInterrupt".equals(((PyException)e).type != null ?
                                    ((PyException)e).type.getType().getName() : ""))) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        final String errorText = sw.toString();

                        SwingUtilities.invokeLater(() ->
                                console.print(errorText, consoleColors.getErrorColor())
                        );
                    }
                } finally {
                    try {
                        safeExec("mipylib.plotlib.miplot.set_interactive(True)");
                    } catch (Exception ignored) {}

                    SwingUtilities.invokeLater(() -> {
                        IChartPanel cp = parent.getFigureDock().getCurrentFigure();
                        if (cp != null) {
                            cp.paintGraphics();
                        }
                        if (interrupted[0]) {
                            console.print(">>> ", consoleColors.getPromptColor());
                        }
                        MutableAttributeSet cmdAttr = new SimpleAttributeSet();
                        StyleConstants.setForeground(cmdAttr, consoleColors.getCommandColor());
                        Font f = console.getTextPane().getFont();
                        StyleConstants.setFontFamily(cmdAttr, f.getFamily());
                        StyleConstants.setFontSize(cmdAttr, f.getSize());
                        console.getTextPane().setCharacterAttributes(cmdAttr, true);
                        parent.getProgressBar().setVisible(false);
                    });

                    myThread = null;
                }
            }
        }, "Jython-RunScript");

        myThread.start();
    }
}
