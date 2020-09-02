/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.gui;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.meteoinfo.console.editor.TextEditor;
import org.meteoinfo.ui.ButtonTabComponent;

/**
 *
 * @author yaqiang
 */
public class GuiUtil {
    /**
     * Create dockable
     * @param title
     * @param color
     * @return 
     */
    public static SingleCDockable createDockable(String title, Color color) {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(color);
        DefaultSingleCDockable dockable = new DefaultSingleCDockable(title, title, panel);
        dockable.setCloseable(true);
        return dockable;
    }
    
    /**
     * Create editor dockable
     * @param title Title
     * @return Editor dockable
     */
    public static SingleCDockable createEditorDockable(String title) {
        JTabbedPane tabbedPanel = new JTabbedPane(); 
        addNewTextEditor("New", tabbedPanel);
        DefaultSingleCDockable dockable = new DefaultSingleCDockable(title, title, tabbedPanel);
        dockable.setCloseable(true);
        return dockable;
    }
    
    private static TextEditor addNewTextEditor(String title, JTabbedPane tabbedPanel) {
        final TextEditor tab = new TextEditor(tabbedPanel, title);
        tabbedPanel.add(tab, title);
        tabbedPanel.setSelectedComponent(tab);
        //tab.setTextFont(_font);
        tab.getTextArea().setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        tab.getTextArea().discardAllEdits();
        tab.getTextArea().setDirty(false);
        tab.setTitle(title);
        ButtonTabComponent btc = new ButtonTabComponent(tabbedPanel);
        JButton button = btc.getTabButton();
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //FrmTextEditor.this.closeFile(tab);
            }
        });
        tabbedPanel.setTabComponentAt(tabbedPanel.indexOfComponent(tab), btc);

        return tab;
    }
}
