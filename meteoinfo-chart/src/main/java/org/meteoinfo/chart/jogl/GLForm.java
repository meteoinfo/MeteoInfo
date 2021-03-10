/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import javax.swing.*;

/**
 *
 * @author yaqiang
 */
public class GLForm extends JFrame{
    private Plot3DGL plt;
    private GLChartPanel glcp;
    
    public GLForm(Plot3DGL plt) {
        this.plt = plt;
        final GLProfile gp = GLProfile.get(GLProfile.GL2);
        GLCapabilities cap = new GLCapabilities(gp);

        glcp = new GLChartPanel(cap, plt);
        glcp.setSize(400, 400);
        
        this.getContentPane().add(glcp);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing();
            }
        });
        
        glcp.animator_start();
    }
    
    private void formWindowClosing() {
        glcp.animator_stop();
    }
}
