/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.gui;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.ScreenDockStation;
import bibliothek.gui.dock.station.screen.ScreenDockWindow;
import bibliothek.gui.dock.station.screen.window.ScreenDockFrame;
import bibliothek.gui.dock.station.screen.window.WindowConfiguration;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGUtils;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

/**
 *
 * @author wyq
 */
public class FrmCustom extends ScreenDockFrame implements ScreenDockWindow {

    public FrmCustom(ScreenDockStation station, WindowConfiguration configuration) {
        super(station, configuration, false);
    }
    
    @Override
    public void setDockable(Dockable dockable){                
        //init(dockable);
        super.setDockable(dockable);
        this.getFrame().setIconImages(FlatSVGUtils.createWindowIconImages("/org/meteoinfo/laboratory/icons/move.svg"));
        this.getFrame().setTitle("");
    }
    
    private void init(Dockable dockable){
        if (dockable != null){
            String text = dockable.getTitleText();
            switch(text){
                case "Figures":
                    this.init_Figure();
                    break;
                case "Editor":
                    this.init_Editor();
                    break;
            }            
        }
    }

    private void init_Figure() {
        JFrame window = getFrame();

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(new JMenu("File"));
        menuBar.add(new JMenu("Figure"));

        window.setJMenuBar(menuBar);
        
        JToolBar toolBar = new JToolBar();
        JButton jButton_ZoomIn = new JButton();
        jButton_ZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_ZoomIn.Image.png"))); // NOI18N
        jButton_ZoomIn.setToolTipText("Zoom In"); // NOI18N
        jButton_ZoomIn.setFocusable(false);
        jButton_ZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ZoomIn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //jButton_ZoomInActionPerformed(evt);
            }
        });
        toolBar.add(jButton_ZoomIn);
        
        JButton jButton_ZoomOut = new JButton();
        JButton jButton_Pan = new JButton();
        JButton jButton_FullExtent = new JButton();
                
        //window.getContentPane().setLayout(new GridLayout(2, 1));
        //window.add(toolBar);
        window.getContentPane().add(toolBar, BorderLayout.PAGE_START);
    }
    
    private void init_Editor() {
        JFrame window = getFrame();

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(new JMenu("File"));
        menuBar.add(new JMenu("Editor"));

        window.setJMenuBar(menuBar);
    }

}
