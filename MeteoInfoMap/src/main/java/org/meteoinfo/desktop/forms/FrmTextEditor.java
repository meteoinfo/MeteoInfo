/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.desktop.forms;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGUtils;
import com.l2fprod.common.swing.JFontChooser;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

import org.meteoinfo.console.JConsole;
import org.meteoinfo.console.editor.JTextAreaPrintStream;
import org.meteoinfo.console.editor.JTextAreaWriter;
import org.meteoinfo.console.jython.JIntrospect;
import org.meteoinfo.console.editor.MITextEditorPane;
import org.meteoinfo.console.jython.PythonInteractiveInterpreter;
import org.meteoinfo.desktop.config.EncodingUtil;
import org.meteoinfo.desktop.config.GenericFileFilter;
import org.meteoinfo.console.editor.TextEditor;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;

import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.ui.ButtonTabComponent;
import org.python.util.PythonInterpreter;

/**
 *
 * @author User
 */
public class FrmTextEditor extends javax.swing.JFrame {

    private FrmMain _parent = null;
    private Font _font = new Font("Simsun", Font.PLAIN, 15);
    private String _scriptLanguage = "Jython";
    private Dimension _splitPanelSize;
    private JConsole console;
    private PythonInteractiveInterpreter interp;

    /**
     * Get font
     *
     * @return Font
     */
    public Font getTextFont() {
        return _font;
    }

    /**
     * Set font
     *
     * @param font Font
     */
    public void setTextFont(Font font) {
        _font = font;
        for (Component tab : this.jTabbedPane1.getComponents()) {
            if (tab instanceof TextEditor) {
                ((TextEditor) tab).setTextFont(_font);
            }
        }

        this.console.setFont(_font);
    }

    /**
     * Get script language name - Groovy or Jython
     *
     * @return Script language name
     */
    public String getScriptLanguage() {
        return this._scriptLanguage;
    }

    /**
     * Set script language name - Groovy or Jython
     *
     * @param value Script language name
     */
    public void setScriptLanguage(String value) {
        this._scriptLanguage = value;
        if (_scriptLanguage.equals("Groovy")) {
            this.setTitle("MeteoInfoMap Script - Groovy");
            this.jRadioButtonMenuItem_Groovy.setSelected(true);
            this.jRadioButtonMenuItem_Jython.setSelected(false);
        } else {
            this.setTitle("MeteoInfoMap Script - Jython");
            this.jRadioButtonMenuItem_Groovy.setSelected(false);
            this.jRadioButtonMenuItem_Jython.setSelected(true);
        }
    }

    /**
     * Creates new form FrmTextEditor
     */
    public FrmTextEditor() {
        initComponents();

        //DefaultCaret caret = (DefaultCaret) this.jTextArea_Output.getCaret();
        //caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        this.initializeConsole();

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setIconImages(FlatSVGUtils.createWindowIconImages("/org/meteoinfo/desktop/icons/jython.svg"));
        this.setScriptLanguage(_scriptLanguage);
        addNewTextEditor("New file");
        this._splitPanelSize = this.jSplitPane1.getBounds().getSize();
        this.setSize(600, 600);
        //this.jSplitPane1.setDividerLocation(0.6);
        this.jSplitPane1.setDividerLocation(300);
        //this.jScrollPane1.invalidate();
    }

    /**
     * Creates new form FrmTextEditor
     *
     * @param parent Parent JFrame
     */
    public FrmTextEditor(JFrame parent) {
        this();
        _parent = (FrmMain) parent;
        this.setScriptLanguage(_parent.getOptions().getScriptLanguage());

        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
        String pluginPath = _parent.getStartupPath() + File.separator + "plugins";
        List<String> jarfns = GlobalUtil.getFiles(pluginPath, ".jar");
        String path = _parent.getStartupPath() + File.separator + "pylib";
        if (isDebug) {
            path = "D:\\MyProgram\\java\\MeteoInfoDev\\MeteoInfo\\MeteoInfoLab\\pylib";
        }

        try {
            interp.exec("import sys");
            interp.exec("sys.path.append('" + path + "')");
            //interp.exec("import mipylib");
            for (String jarfn : jarfns) {
                interp.exec("sys.path.append('" + jarfn + "')");
            }
            interp.set("miapp", _parent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initializeConsole() {
        console = new JConsole();
        console.setLocale(Locale.getDefault());
        console.setPreferredSize(new Dimension(600, 400));
        console.println(new ImageIcon(this.getClass().getResource("/images/jython_small_c.png")));

        this.interp = new PythonInteractiveInterpreter(console);
        new Thread(interp).start();

        JIntrospect nameComplete = new JIntrospect(this.interp);
        console.setNameCompletion(nameComplete);

        this.jSplitPane1.setRightComponent(console);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jButton_NewFile = new javax.swing.JButton();
        jButton_OpenFile = new javax.swing.JButton();
        jButton_CloseFile = new javax.swing.JButton();
        jButton_SaveFile = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton_Undo = new javax.swing.JButton();
        jButton_Redo = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton_RunScript = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu_File = new javax.swing.JMenu();
        jMenuItem_NewFile = new javax.swing.JMenuItem();
        jMenuItem_OpenFile = new javax.swing.JMenuItem();
        jMenuItem_SaveFile = new javax.swing.JMenuItem();
        jMenuItem_SaveAs = new javax.swing.JMenuItem();
        jMenuItem_CloseFile = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_Exit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem_Cut = new javax.swing.JMenuItem();
        jMenuItem_Copy = new javax.swing.JMenuItem();
        jMenuItem_Paste = new javax.swing.JMenuItem();
        jMenu_Options = new javax.swing.JMenu();
        jMenuItem_SetFont = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenu_ScriptLanguage = new javax.swing.JMenu();
        jRadioButtonMenuItem_Groovy = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem_Jython = new javax.swing.JRadioButtonMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jToolBar1.setRollover(true);
        jToolBar1.setPreferredSize(new java.awt.Dimension(74, 25));

        //jButton_NewFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NewFile.Image.png"))); // NOI18N
        jButton_NewFile.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/file-new.svg"));
        jButton_NewFile.setToolTipText("New File");
        jButton_NewFile.setFocusable(false);
        jButton_NewFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_NewFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_NewFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewFileActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_NewFile);

        //jButton_OpenFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Folder_1_16x16x8.png"))); // NOI18N
        jButton_OpenFile.setIcon(new FlatSVGIcon("org/meteoinfo/icons/file-open.svg"));
        jButton_OpenFile.setToolTipText("Open File");
        jButton_OpenFile.setFocusable(false);
        jButton_OpenFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_OpenFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_OpenFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OpenFileActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_OpenFile);

        //jButton_CloseFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/closefile.png"))); // NOI18N
        jButton_CloseFile.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/file-close-all.svg"));
        jButton_CloseFile.setToolTipText("Close File");
        jButton_CloseFile.setFocusable(false);
        jButton_CloseFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_CloseFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_CloseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_CloseFileActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_CloseFile);

        //jButton_SaveFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Disk_1_16x16x8.png"))); // NOI18N
        jButton_SaveFile.setIcon(new FlatSVGIcon("org/meteoinfo/icons/file-save.svg"));
        jButton_SaveFile.setToolTipText("Save File");
        jButton_SaveFile.setFocusable(false);
        jButton_SaveFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_SaveFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_SaveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SaveFileActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_SaveFile);
        jToolBar1.add(jSeparator1);

        //jButton_Undo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_Undo.Image.png"))); // NOI18N
        jButton_Undo.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/undo.svg"));
        jButton_Undo.setToolTipText("Undo");
        jButton_Undo.setFocusable(false);
        jButton_Undo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Undo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Undo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_UndoActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_Undo);

        //jButton_Redo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_Redo.Image.png"))); // NOI18N
        jButton_Redo.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/redo.svg"));
        jButton_Redo.setToolTipText("Redo");
        jButton_Redo.setFocusable(false);
        jButton_Redo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Redo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Redo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_RedoActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_Redo);
        jToolBar1.add(jSeparator2);

        //jButton_RunScript.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_RunScript.Image.png"))); // NOI18N
        jButton_RunScript.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/execute.svg"));
        jButton_RunScript.setToolTipText("Run Script");
        jButton_RunScript.setFocusable(false);
        jButton_RunScript.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_RunScript.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_RunScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_RunScriptActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton_RunScript);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.NORTH);

        //jSplitPane1.setDividerLocation(0.6);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jSplitPane1ComponentResized(evt);
            }
        });

        jSplitPane1.setLeftComponent(jTabbedPane1);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jMenu_File.setMnemonic('F');
        jMenu_File.setText("File");

        jMenuItem_NewFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_NewFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_NewFile.Image.png"))); // NOI18N
        jMenuItem_NewFile.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/file-new.svg"));
        jMenuItem_NewFile.setText("New");
        jMenuItem_NewFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_NewFileActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_NewFile);

        jMenuItem_OpenFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_OpenFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Folder_1_16x16x8.png"))); // NOI18N
        jMenuItem_OpenFile.setIcon(new FlatSVGIcon("org/meteoinfo/icons/file-open.svg"));
        jMenuItem_OpenFile.setText("Open ...");
        jMenuItem_OpenFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_OpenFileActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_OpenFile);

        jMenuItem_SaveFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_SaveFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Disk_1_16x16x8.png"))); // NOI18N
        jMenuItem_SaveFile.setIcon(new FlatSVGIcon("org/meteoinfo/icons/file-save.svg"));
        jMenuItem_SaveFile.setText("Save");
        jMenuItem_SaveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SaveFileActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_SaveFile);

        jMenuItem_SaveAs.setText("Save As ...");
        jMenuItem_SaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SaveAsActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_SaveAs);

        jMenuItem_CloseFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_CloseFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/closefile.png"))); // NOI18N
        jMenuItem_CloseFile.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/file-close-all.svg"));
        jMenuItem_CloseFile.setText("Close");
        jMenuItem_CloseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_CloseFileActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_CloseFile);
        jMenu_File.add(jSeparator3);

        jMenuItem_Exit.setText("Exit");
        jMenuItem_Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ExitActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenuItem_Exit);

        jMenuBar1.add(jMenu_File);

        jMenu2.setMnemonic('E');
        jMenu2.setText("Edit");

        jMenuItem_Cut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_Cut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSMI_EditCut.Image.png"))); // NOI18N
        jMenuItem_Cut.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/menu-cut.svg"));
        jMenuItem_Cut.setText("Cut");
        jMenuItem_Cut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_CutActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem_Cut);

        jMenuItem_Copy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_Copy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/menuEditCopy.Image.png"))); // NOI18N
        jMenuItem_Copy.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/copy.svg"));
        jMenuItem_Copy.setText("Copy");
        jMenuItem_Copy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_CopyActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem_Copy);

        jMenuItem_Paste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        //jMenuItem_Paste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pasteToolStripButton.Image.png"))); // NOI18N
        jMenuItem_Paste.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/menu-paste.svg"));
        jMenuItem_Paste.setText("Paste");
        jMenuItem_Paste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_PasteActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem_Paste);

        jMenuBar1.add(jMenu2);

        jMenu_Options.setMnemonic('O');
        jMenu_Options.setText("Options");

        //jMenuItem_SetFont.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/miSetFont.Image.png"))); // NOI18N
        jMenuItem_SetFont.setIcon(new FlatSVGIcon("org/meteoinfo/desktop/icons/font.svg"));
        jMenuItem_SetFont.setText("Set Font");
        jMenuItem_SetFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_SetFontActionPerformed(evt);
            }
        });
        jMenu_Options.add(jMenuItem_SetFont);
        //jMenu_Options.add(jSeparator5);

        jMenu_ScriptLanguage.setText("Script Language");

        jRadioButtonMenuItem_Groovy.setSelected(true);
        jRadioButtonMenuItem_Groovy.setText("Groovy");
        jRadioButtonMenuItem_Groovy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItem_GroovyActionPerformed(evt);
            }
        });
        jMenu_ScriptLanguage.add(jRadioButtonMenuItem_Groovy);

        jRadioButtonMenuItem_Jython.setSelected(true);
        jRadioButtonMenuItem_Jython.setText("Jython");
        jRadioButtonMenuItem_Jython.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItem_JythonActionPerformed(evt);
            }
        });
        jMenu_ScriptLanguage.add(jRadioButtonMenuItem_Jython);

        //jMenu_Options.add(jMenu_ScriptLanguage);

        jMenuBar1.add(jMenu_Options);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_NewFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewFileActionPerformed
        // TODO add your handling code here:
        addNewTextEditor("New file");
    }//GEN-LAST:event_jButton_NewFileActionPerformed

    private void jButton_OpenFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OpenFileActionPerformed
        // TODO add your handling code here:          
        if (this._scriptLanguage.equals("Groovy")) {
            this.doOpen_Groovy();
        } else {
            this.doOpen_Jython();
        }
    }//GEN-LAST:event_jButton_OpenFileActionPerformed

    private void jButton_CloseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_CloseFileActionPerformed
        // TODO add your handling code here:
        this.closeFile();
    }//GEN-LAST:event_jButton_CloseFileActionPerformed

    private void jButton_SaveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_SaveFileActionPerformed
        // TODO add your handling code here:
        TextEditor textEditor = getActiveTextEditor();
        if (textEditor != null) {
            this.doSave(textEditor);
        }
    }//GEN-LAST:event_jButton_SaveFileActionPerformed

    private void jButton_UndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_UndoActionPerformed
        // TODO add your handling code here:
        TextEditor textEditor = getActiveTextEditor();
        textEditor.getTextArea().undoLastAction();
    }//GEN-LAST:event_jButton_UndoActionPerformed

    private void jButton_RedoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_RedoActionPerformed
        // TODO add your handling code here:
        TextEditor textEditor = getActiveTextEditor();
        textEditor.getTextArea().redoLastAction();
    }//GEN-LAST:event_jButton_RedoActionPerformed

    private void jButton_RunScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_RunScriptActionPerformed
        TextEditor editor = getActiveTextEditor();
        if (!editor.getFileName().isEmpty()) {
            editor.saveFile(editor.getFile());
        }

        runPythonScript();
    }//GEN-LAST:event_jButton_RunScriptActionPerformed

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
        TextEditor editor = this.getActiveTextEditor();
        if (editor != null) {
            if (this._scriptLanguage.equals("Groovy")) {
                this.doSaveAs_Groovy(editor);
            } else {
                this.doSaveAs_Jython(editor);
            }
        }
    }//GEN-LAST:event_jMenuItem_SaveAsActionPerformed

    private void jMenuItem_CloseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_CloseFileActionPerformed
        // TODO add your handling code here:
        this.jButton_CloseFile.doClick();
    }//GEN-LAST:event_jMenuItem_CloseFileActionPerformed

    private void jMenuItem_ExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_ExitActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jMenuItem_ExitActionPerformed

    private void jMenuItem_CutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_CutActionPerformed
        // TODO add your handling code here:
        TextEditor textEditor = getActiveTextEditor();
        textEditor.getTextArea().cut();
    }//GEN-LAST:event_jMenuItem_CutActionPerformed

    private void jMenuItem_CopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_CopyActionPerformed
        // TODO add your handling code here:
        TextEditor textEditor = getActiveTextEditor();
        textEditor.getTextArea().copy();
    }//GEN-LAST:event_jMenuItem_CopyActionPerformed

    private void jMenuItem_PasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_PasteActionPerformed
        // TODO add your handling code here:
        TextEditor textEditor = getActiveTextEditor();
        textEditor.getTextArea().paste();
    }//GEN-LAST:event_jMenuItem_PasteActionPerformed

    private void jMenuItem_SetFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_SetFontActionPerformed
        // TODO add your handling code here:
        TextEditor textEditor = getActiveTextEditor();
        Font tFont = JFontChooser.showDialog(this, null, textEditor.getTextArea().getFont());
        if (tFont != null) {
            this.setTextFont(tFont);
            ((FrmMain) _parent).getOptions().setTextFont(tFont);
        }
    }//GEN-LAST:event_jMenuItem_SetFontActionPerformed

    private void jRadioButtonMenuItem_GroovyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem_GroovyActionPerformed
        // TODO add your handling code here:
        this.setScriptLanguage("Groovy");
        //this.jRadioButtonMenuItem_Groovy.setSelected(true);
        //this.jRadioButtonMenuItem_Jython.setSelected(false);
    }//GEN-LAST:event_jRadioButtonMenuItem_GroovyActionPerformed

    private void jRadioButtonMenuItem_JythonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem_JythonActionPerformed
        // TODO add your handling code here:
        this.setScriptLanguage("Jython");
        //this.jRadioButtonMenuItem_Groovy.setSelected(false);
        //this.jRadioButtonMenuItem_Jython.setSelected(true);
    }//GEN-LAST:event_jRadioButtonMenuItem_JythonActionPerformed

    private void jSplitPane1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jSplitPane1ComponentResized
        // TODO add your handling code here:
        /*Dimension size = this.jSplitPane1.getBounds().getSize();
        int heightdelta = size.height - this._splitPanelSize.height;
        this.jSplitPane1.setDividerLocation(this.jSplitPane1.getDividerLocation() + heightdelta);
        this._splitPanelSize = this.jSplitPane1.getBounds().getSize();*/
    }//GEN-LAST:event_jSplitPane1ComponentResized

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        boolean isDispose = true;
        for (int i = 0; i < this.jTabbedPane1.getTabCount(); i++) {
            TextEditor textEditor = (TextEditor) this.jTabbedPane1.getComponentAt(i);
            if (textEditor != null) {
                boolean ifClose = true;
                if (textEditor.getTextArea().isDirty()) {
                    String fName = textEditor.getFileName();
                    if (fName.isEmpty()) {
                        fName = "New file";
                    }
                    int result = JOptionPane.showConfirmDialog(null, MessageFormat.format("Save changes to \"{0}\"", fName), "Save?", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        if (!doSave(textEditor)) {
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
        if (isDispose)
            this.dispose();
    }//GEN-LAST:event_formWindowClosing

    private void runPythonScript() {

        SwingWorker worker = new SwingWorker<String, String>() {
            PrintStream oout = System.out;
            PrintStream oerr = System.err;

            @Override
            protected String doInBackground() throws Exception {
                interp.console.println("run script...");
                interp.console.setFocusable(true);
                interp.console.requestFocusInWindow();

                TextEditorPane textArea = getActiveTextArea();
                //textArea.setEncoding(fn);
                String code = textArea.getText();
//                if (code.contains("coding=utf-8")){
//                    code = code.replace("coding=utf-8", "coding = utf-8");
//                }
                String encoding = EncodingUtil.findEncoding(code);
                if (encoding != null) {
                    try {
                        interp.execfile(new ByteArrayInputStream(code.getBytes(encoding)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        interp.execfile(new ByteArrayInputStream(code.getBytes()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                try {
//                    //interp.exec(code);
//                    interp.execfile(new ByteArrayInputStream(code.getBytes()));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                return "";
            }

            @Override
            protected void done() {
                System.setOut(oout);
                System.setErr(oerr);
            }
        };
        worker.execute();
    }

    private TextEditor addNewTextEditor(String title) {
        final TextEditor tab = new TextEditor(this.jTabbedPane1, title);
        this.jTabbedPane1.add(tab, title);
        this.jTabbedPane1.setSelectedComponent(tab);
        tab.setTextFont(_font);
        final MITextEditorPane textArea = (MITextEditorPane) tab.getTextArea();
        if (this._scriptLanguage.equals("Groovy")) {
            tab.getTextArea().setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        } else {
            tab.getTextArea().setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        }
        tab.getTextArea().discardAllEdits();
        tab.getTextArea().setDirty(false);
        tab.setTitle(title);

        //Set name completion
        JIntrospect nameComplete = new JIntrospect(this.interp);
        textArea.setNameCompletion(nameComplete);

        ButtonTabComponent btc = new ButtonTabComponent(this.jTabbedPane1);
        JButton button = btc.getTabButton();
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FrmTextEditor.this.closeFile(tab);
            }
        });
        this.jTabbedPane1.setTabComponentAt(this.jTabbedPane1.indexOfComponent(tab), btc);

        return tab;
    }

    private void doOpen_Groovy() {
        // TODO add your handling code here:          
        JFileChooser aDlg = new JFileChooser();
        aDlg.setMultiSelectionEnabled(true);
        aDlg.setAcceptAllFileFilterUsed(false);
        //File dir = new File(this._parent.getCurrentDataFolder());
        File dir = new File(System.getProperty("user.dir"));
        if (dir.isDirectory()) {
            aDlg.setCurrentDirectory(dir);
        }
        String[] fileExts = new String[]{"groovy"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "Groovy File (*.groovy)");
        aDlg.setFileFilter(mapFileFilter);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File[] files = aDlg.getSelectedFiles();
            //this._parent.setCurrentDataFolder(files[0].getParent());
            System.setProperty("user.dir", files[0].getParent());
            this.openFiles(files);
        }
    }

    private void doOpen_Jython() {
        // TODO add your handling code here:          
        JFileChooser aDlg = new JFileChooser();
        aDlg.setMultiSelectionEnabled(true);
        aDlg.setAcceptAllFileFilterUsed(false);
        //File dir = new File(this._parent.getCurrentDataFolder());
        File dir = new File(System.getProperty("user.dir"));
        if (dir.isDirectory()) {
            aDlg.setCurrentDirectory(dir);
        }
        String[] fileExts = new String[]{"py"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "Jython File (*.py)");
        aDlg.setFileFilter(mapFileFilter);
        if (JFileChooser.APPROVE_OPTION == aDlg.showOpenDialog(this)) {
            File[] files = aDlg.getSelectedFiles();
            //this._parent.setCurrentDataFolder(files[0].getParent());
            System.setProperty("user.dir", files[0].getParent());
            this.openFiles(files);
        }
    }

    /**
     * Open script files
     *
     * @param files The files
     */
    public void openFiles(File[] files) {
        // Close default untitled document if it is still empty
        if (this.jTabbedPane1.getTabCount() == 1) {
            TextEditor textEditor = getActiveTextEditor();
            if (textEditor.getTextArea().getDocument().getLength() == 0 && textEditor.getFileName().isEmpty()) {
                this.removeTextEditor(textEditor);
            }
        }

        // Open file(s)
        for (File file : files) {
            TextEditor editor = addNewTextEditor(file.getName());
            editor.openFile(file);
        }
    }

    private void closeFile() {
        closeFile(this.getActiveTextEditor());
    }

    private void closeFile(TextEditor textEditor) {
        //TextEditor textEditor = getActiveTextEditor();
        if (textEditor != null) {
            boolean ifClose = true;
            if (textEditor.getTextArea().isDirty()) {
                String fName = textEditor.getFileName();
                if (fName.isEmpty()) {
                    fName = "New file";
                }
                int result = JOptionPane.showConfirmDialog(null, MessageFormat.format("Save changes to \"{0}\"", fName), "Save?", JOptionPane.YES_NO_CANCEL_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    if (!doSave(textEditor)) {
                        ifClose = false;
                    }
                } else if (result == JOptionPane.CANCEL_OPTION) {
                    ifClose = false;
                }
            }

            if (ifClose) {
                removeTextEditor(textEditor);
            }
        }
    }

    private boolean doSave(TextEditor editor) {
        if (editor.getFileName().isEmpty()) {
            if (this._scriptLanguage.equals("Groovy")) {
                return doSaveAs_Groovy(editor);
            } else {
                return doSaveAs_Jython(editor);
            }
        } else {
            editor.saveFile(editor.getFile());
            return true;
        }
    }

    private boolean doSaveAs_Groovy(TextEditor editor) {
        JFileChooser aDlg = new JFileChooser();
        String[] fileExts = new String[]{"groovy"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "Groovy File (*.groovy)");
        aDlg.setFileFilter(mapFileFilter);
        if (editor.getFile() != null) {
            aDlg.setSelectedFile(editor.getFile());
        } else {
            File dir = new File(System.getProperty("user.dir"));
            if (dir.isDirectory()) {
                aDlg.setCurrentDirectory(dir);
            }
        }
        aDlg.setAcceptAllFileFilterUsed(false);
        if (aDlg.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = aDlg.getSelectedFile();
            //this._parent.setCurrentDataFolder(file.getParent());
            System.setProperty("user.dir", file.getParent());
            String extent = ((GenericFileFilter) aDlg.getFileFilter()).getFileExtent();
            String fileName = file.getAbsolutePath();
            if (!fileName.substring(fileName.length() - extent.length()).equals(extent)) {
                fileName = fileName + "." + extent;
            }
            file = new File(fileName);
            editor.saveFile(file);
            return true;
        }
        return false;
    }

    private boolean doSaveAs_Jython(TextEditor editor) {
        JFileChooser aDlg = new JFileChooser();
        String[] fileExts = new String[]{"py"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "Python File (*.py)");
        aDlg.setFileFilter(mapFileFilter);
        if (editor.getFile() != null) {
            aDlg.setSelectedFile(editor.getFile());
        } else {
            File dir = new File(System.getProperty("user.dir"));
            if (dir.isDirectory()) {
                aDlg.setCurrentDirectory(dir);
            }
        }
        if (aDlg.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = aDlg.getSelectedFile();
            System.setProperty("user.dir", file.getParent());
            String extent = ((GenericFileFilter) aDlg.getFileFilter()).getFileExtent();
            String fileName = file.getAbsolutePath();
            if (!fileName.substring(fileName.length() - extent.length()).equals(extent)) {
                fileName = fileName + "." + extent;
            }
            file = new File(fileName);
            editor.saveFile(file);
            return true;
        }
        return false;
    }

    private void removeTextEditor(TextEditor editor) {
        this.jTabbedPane1.remove(editor);
    }

    private TextEditorPane getActiveTextArea() {
        TextEditor textEditor = getActiveTextEditor();
        if (textEditor != null) {
            return textEditor.getTextArea();
        } else {
            return null;
        }
    }

    private TextEditor getTextEditor(ButtonTabComponent btc) {
        int idx = this.jTabbedPane1.indexOfTabComponent(btc);
        return (TextEditor) this.jTabbedPane1.getComponentAt(idx);
    }

    private TextEditor getActiveTextEditor() {
        if (this.jTabbedPane1.getTabCount() == 0) {
            return null;
        } else {
            return (TextEditor) this.jTabbedPane1.getSelectedComponent();
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
            public void run() {
                new FrmTextEditor().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_CloseFile;
    private javax.swing.JButton jButton_NewFile;
    private javax.swing.JButton jButton_OpenFile;
    private javax.swing.JButton jButton_Redo;
    private javax.swing.JButton jButton_RunScript;
    private javax.swing.JButton jButton_SaveFile;
    private javax.swing.JButton jButton_Undo;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem_CloseFile;
    private javax.swing.JMenuItem jMenuItem_Copy;
    private javax.swing.JMenuItem jMenuItem_Cut;
    private javax.swing.JMenuItem jMenuItem_Exit;
    private javax.swing.JMenuItem jMenuItem_NewFile;
    private javax.swing.JMenuItem jMenuItem_OpenFile;
    private javax.swing.JMenuItem jMenuItem_Paste;
    private javax.swing.JMenuItem jMenuItem_SaveAs;
    private javax.swing.JMenuItem jMenuItem_SaveFile;
    private javax.swing.JMenuItem jMenuItem_SetFont;
    private javax.swing.JMenu jMenu_File;
    private javax.swing.JMenu jMenu_Options;
    private javax.swing.JMenu jMenu_ScriptLanguage;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem_Groovy;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem_Jython;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    //private javax.swing.JTextArea jTextArea_Output;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
}
