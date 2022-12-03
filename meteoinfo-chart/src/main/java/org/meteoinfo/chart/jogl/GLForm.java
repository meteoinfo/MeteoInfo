/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import org.meteoinfo.chart.GLChartPanel;
import org.meteoinfo.chart.MouseMode;

import javax.swing.*;

/**
 *
 * @author yaqiang
 */
public class GLForm extends JFrame{
    private GLPlot plt;
    private GLChartPanel chartPanel;
    
    public GLForm(GLPlot plt) {
        this.plt = plt;

        chartPanel = new GLChartPanel();
        chartPanel.setSize(400, 400);
        chartPanel.setMouseMode(MouseMode.ROTATE);
        chartPanel.getChart().addPlot(this.plt);
        
        this.getContentPane().add(chartPanel);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing();
            }
        });
        
        //glcp.animator_start();
    }
    
    private void formWindowClosing() {
        //glcp.animator_stop();
    }
}
