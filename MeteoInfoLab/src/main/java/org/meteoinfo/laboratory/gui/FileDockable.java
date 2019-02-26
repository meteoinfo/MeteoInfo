/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.gui;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.action.CButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 *
 * @author wyq
 */
public class FileDockable extends DefaultSingleCDockable {
    
    private final FileExplorer fileExplorer;
    
    public FileDockable(String id, String title, CAction... actions) {
        super(id, title, actions);
        
        fileExplorer = new FileExplorer(null);
        this.getContentPane().add(fileExplorer);
        //this.setCloseable(false);
        
        //Add actions     
        //Up action
        CButton button = new CButton();        
        button.setText("Up");
        button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/up_arrow.png")));
        button.setTooltip("Up folder");
        button.addActionListener( new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){
                File path = FileDockable.this.fileExplorer.getPath().getParentFile();
                if (path != null){
                    FileDockable.this.fileExplorer.listFiles(path);
                    FileDockable.this.fileExplorer.fireCurrentPathChangedEvent();
                }
            }
        });
        this.addAction(button);
        
        //Update action        
        button = new CButton();
        button.setText("Update");
        button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/update.png")));
        button.setTooltip("Update");
        button.addActionListener( new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){
                FileDockable.this.fileExplorer.listFiles();
            }
        });
        this.addAction(button);
        
        this.addSeparator();
    }
    
    /**
     * Set path
     * @param path Path
     */
    public void setPath(File path){
        this.fileExplorer.setPath(path);
    }
    
    /**
     * Get FileExplorer
     * @return FileExplorer
     */
    public FileExplorer getFileExplorer(){
        return this.fileExplorer;
    }
}
