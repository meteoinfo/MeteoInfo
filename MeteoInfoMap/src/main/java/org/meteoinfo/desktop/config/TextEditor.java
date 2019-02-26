 /* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.desktop.config;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 *
 * @author Yaqiang Wang
 */
public class TextEditor extends JPanel {
    // <editor-fold desc="Variables">

    private TextEditorPane _textArea;
    private String _title;
    private File _file = null;
    private JTabbedPane _parent;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param parent Parent
     * @param title Title text
     */
    public TextEditor(JTabbedPane parent, String title) {
        super();
        this.setLayout(new BorderLayout());
        _title = title;
        _parent = parent;
        _textArea = new TextEditorPane();
        _textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkUpdate();
            }
        });
        //_textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        //_textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        _textArea.setCodeFoldingEnabled(true);
        _textArea.setAntiAliasingEnabled(true);
        //_textArea.setEncoding("GB2312");
        _textArea.setEncoding("utf-8");
        _textArea.setTabSize(4);
        _textArea.setTabsEmulated(true);
        
        RTextScrollPane sp = new RTextScrollPane(_textArea);
        sp.setFoldIndicatorEnabled(true);
        this.add(sp);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get text area
     *
     * @return The RSyntaxTextArea
     */
    public TextEditorPane getTextArea() {
        return _textArea;
    }

    /**
     * Get title text
     *
     * @return Title text
     */
    public String getTitle() {
        return _title;
    }

    /**
     * Set title text
     *
     * @param value Title text
     */
    public void setTitle(String value) {
        int idx = _parent.indexOfTab(_title);
        _title = value;        
        _parent.setTitleAt(idx, _title);
    }
    
    /**
     * Get file
     * 
     * @return File
     */
    public File getFile(){
        return _file;
    }
    
    public void setFile(File file){
        _file = file;
        setTitle(_file.getName());
    }

    /**
     * Get file name
     *
     * @return File name
     */
    public String getFileName() {
        if (_file == null) {
            return "";
        } else {
            return _file.getAbsolutePath();
        }
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Open file
     *
     * @param file The file
     */
    public void openFile(File file) {
        if (file.isDirectory()) { // Clicking on a space character
            JOptionPane.showMessageDialog(this, file.getAbsolutePath()
                    + " is a directory", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } else if (!file.isFile()) {
            JOptionPane.showMessageDialog(this, "No such file: "
                    + file.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            //BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
             _textArea.read(r, null);
            r.close();       
            //String estr = _textArea.getEncoding();
            //_textArea.setEncoding("UTF-8");
            //_textArea.setEncoding("GB2312");      
            //_textArea.save();

//            FileLocation fl = FileLocation.create(file);
//            this._textArea.load(fl, null);
//            String estr = _textArea.getEncoding();
//            //_textArea.setEncoding("UTF-8");
//            //_textArea.setEncoding("GB2312");      
//            _textArea.save();

            setFile(file);
            _textArea.discardAllEdits();
            _textArea.setDirty(false);
        } catch (IOException ioe) {
            UIManager.getLookAndFeel().provideErrorFeedback(_textArea);
        }
    }

    /**
     * Save file
     *
     * @param file The file
     */
    public void saveFile(File file) {
        OutputStreamWriter w = null;
        try {
            w = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()), "UTF-8");
            _textArea.write(w);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TextEditor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TextEditor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                w.close();
                setFile(file);
                _textArea.setDirty(false);
                checkUpdate();
            } catch (IOException ex) {
                Logger.getLogger(TextEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void checkUpdate() {
        String title = _title;
        if (_title.substring(_title.length() - 1).equals("*")) {
            _title = _title.substring(0, _title.length() - 1);
        }

        if (_textArea.isDirty()) {
            _title = _title + '*';
        }

        if (!title.equals(_title)) {
            int idx = _parent.indexOfTab(title);
            _parent.setTitleAt(idx, _title);
        }
    }
    
    /**
     * Set the font for all token types.
     *
     * @param font The font to use.
     */
    public void setTextFont(Font font) {
        if (font != null) {
            SyntaxScheme ss = _textArea.getSyntaxScheme();
            ss = (SyntaxScheme) ss.clone();
            for (int i = 0; i < ss.getStyleCount(); i++) {
                if (ss.getStyle(i) != null) {
                    ss.getStyle(i).font = font;
                }
            }
            _textArea.setSyntaxScheme(ss);
            _textArea.setFont(font);
        }
    }
    // </editor-fold>
}
