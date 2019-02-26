/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.gui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import org.meteoinfo.layer.FrmLabelSet;
import org.meteoinfo.layer.LayerTypes;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.layout.MapLayout;
import org.meteoinfo.layout.MouseMode;
import org.meteoinfo.legend.LayersLegend;
import org.meteoinfo.map.MapView;
import org.meteoinfo.map.MouseTools;

/**
 *
 * @author Yaqiang Wang
 */
public class MainToolBar extends JToolBar {

    // <editor-fold desc="Variables">
    MapView mapView;
    MapLayout mapLayout;
    JButton jButton_Select;
    JButton jButton_ZoomIn;
    JButton jButton_ZoomOut;
    JButton jButton_Pan;
    JButton jButton_FullExtent;
    JButton jButton_ZoomToLayer;
    JButton jButton_Identifer;
    JButton jButton_Label;
    JLabel jLabel_Status;
    JButton currentTool;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param toc Table of contents
     * @param jLabel_Status Label for selected button
     */
    public MainToolBar(LayersLegend toc, JLabel jLabel_Status){
        this(toc.getActiveMapFrame().getMapView(), toc.getMapLayout(), jLabel_Status);
    }
    
    /**
     * Constructor
     *
     * @param mapView MapView
     * @param mapLayout MapLayout
     * @param jLabel_Status Label for selected button
     */
    public MainToolBar(MapView mapView, MapLayout mapLayout, JLabel jLabel_Status) {
        this.mapView = mapView;
        this.mapLayout = mapLayout;
        this.jLabel_Status = jLabel_Status;
        
        jButton_Select = new JButton();
        jButton_Select.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_None.Image.png"))); // NOI18N
        jButton_Select.setToolTipText("Select");
        jButton_Select.setFocusable(false);
        jButton_Select.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Select.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Select.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SelectActionPerformed(evt);
            }
        });
        this.add(jButton_Select);

        jButton_ZoomIn = new JButton();
        jButton_ZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_ZoomIn.Image.png"))); // NOI18N
        jButton_ZoomIn.setToolTipText("Zoom In");
        jButton_ZoomIn.setFocusable(false);
        jButton_ZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ZoomIn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ZoomInActionPerformed(evt);
            }
        });
        this.add(jButton_ZoomIn);

        jButton_ZoomOut = new JButton();
        jButton_ZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_ZoomOut.Image.png"))); // NOI18N
        jButton_ZoomOut.setToolTipText("Zoom Out");
        jButton_ZoomOut.setFocusable(false);
        jButton_ZoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ZoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ZoomOut.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ZoomOutActionPerformed(evt);
            }
        });
        this.add(jButton_ZoomOut);

        jButton_Pan = new JButton();
        jButton_Pan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_Pan.Image.png"))); // NOI18N
        jButton_Pan.setToolTipText("Pan");
        jButton_Pan.setFocusable(false);
        jButton_Pan.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Pan.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Pan.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_PanActionPerformed(evt);
            }
        });
        this.add(jButton_Pan);

        jButton_FullExtent = new JButton();
        jButton_FullExtent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_FullExent.Image.png"))); // NOI18N
        jButton_FullExtent.setToolTipText("Full Extent");
        jButton_FullExtent.setFocusable(false);
        jButton_FullExtent.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_FullExtent.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_FullExtent.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_FullExtentActionPerformed(evt);
            }
        });
        this.add(jButton_FullExtent);
        
        jButton_ZoomToLayer = new JButton();
        jButton_ZoomToLayer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_ZoomToLayer.Image.png"))); // NOI18N
        jButton_ZoomToLayer.setToolTipText("Zoom to Layer");
        jButton_ZoomToLayer.setFocusable(false);
        jButton_ZoomToLayer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ZoomToLayer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ZoomToLayer.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ZoomToLayerActionPerformed(evt);
            }
        });
        this.add(jButton_ZoomToLayer);

        jButton_Identifer = new JButton();
        jButton_Identifer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/information.png"))); // NOI18N
        jButton_Identifer.setToolTipText("Identifer");
        jButton_Identifer.setFocusable(false);
        jButton_Identifer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Identifer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Identifer.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_IdentiferActionPerformed(evt);
            }
        });
        this.add(jButton_Identifer);
        this.add(new Separator());

        jButton_Label = new JButton();
        jButton_Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_LabelSet.Image.png"))); // NOI18N
        jButton_Label.setToolTipText("Label");
        jButton_Label.setFocusable(false);
        jButton_Label.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Label.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Label.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_LabelActionPerformed(evt);
            }
        });
        this.add(jButton_Label);

        //this.currentTool = this.jButton_Select;
    }

    private void jButton_SelectActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        this.mapView.setMouseTool(MouseTools.SelectElements);
        if (this.mapLayout != null) {
            this.mapLayout.setMouseMode(MouseMode.Select);
        }

        setCurrentTool((JButton) evt.getSource());
    }

    private void jButton_ZoomInActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        this.mapView.setMouseTool(MouseTools.Zoom_In);
        if (this.mapLayout != null) {
            this.mapLayout.setMouseMode(MouseMode.Map_ZoomIn);
        }

        setCurrentTool((JButton) evt.getSource());
    }

    private void jButton_ZoomOutActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        this.mapView.setMouseTool(MouseTools.Zoom_Out);
        if (this.mapLayout != null) {
            this.mapLayout.setMouseMode(MouseMode.Map_ZoomOut);
        }

        setCurrentTool((JButton) evt.getSource());
    }

    private void jButton_PanActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        this.mapView.setMouseTool(MouseTools.Pan);
        if (this.mapLayout != null) {
            this.mapLayout.setMouseMode(MouseMode.Map_Pan);
        }

        setCurrentTool((JButton) evt.getSource());
    }

    private void jButton_FullExtentActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        this.mapView.zoomToExtent(this.mapView.getExtent());
    }
    
    private void jButton_ZoomToLayerActionPerformed(java.awt.event.ActionEvent evt) {
        MapLayer layer = this.mapView.getSelectedLayer();
        if (layer != null){
            this.mapView.zoomToExtent(layer.getExtent());
        }
    }

    private void jButton_IdentiferActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        this.mapView.setMouseTool(MouseTools.Identifer);
        if (this.mapLayout != null) {
            this.mapLayout.setMouseMode(MouseMode.Map_Identifer);
        }

        setCurrentTool((JButton) evt.getSource());
    }

    private void jButton_LabelActionPerformed(java.awt.event.ActionEvent evt) {
        MapLayer aMLayer = this.mapView.getSelectedLayer();
        if (aMLayer.getLayerType() == LayerTypes.VectorLayer) {
            VectorLayer aLayer = (VectorLayer) aMLayer;
            if (aLayer.getShapeNum() > 0) {
                FrmLabelSet aFrmLabel = new FrmLabelSet(null, false, this.mapView);
                aFrmLabel.setLayer(aLayer);
                //aFrmLabel.setLocationRelativeTo(this);
                aFrmLabel.setVisible(true);
            }
        }
    }

    private void setCurrentTool(JButton currentTool) {
        if (!(this.currentTool == null)) {
            this.currentTool.setSelected(false);
        }
        this.currentTool = currentTool;
        this.currentTool.setSelected(true);
        if (this.jLabel_Status != null) {
            jLabel_Status.setText(currentTool.getToolTipText());
        }
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
