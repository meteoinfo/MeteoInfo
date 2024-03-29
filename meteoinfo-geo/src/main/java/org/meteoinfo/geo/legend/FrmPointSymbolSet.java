/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.geo.legend;

import org.meteoinfo.ui.util.FontUtil;
import org.meteoinfo.common.util.GlobalUtil;
import org.meteoinfo.geometry.legend.MarkerType;
import org.meteoinfo.geometry.legend.PointBreak;
import org.meteoinfo.geometry.legend.PointStyle;
import org.meteoinfo.ui.event.ISelectedCellChangedListener;
import org.meteoinfo.ui.event.SelectedCellChangedEvent;
import org.meteoinfo.geo.layout.MapLayout;
import org.meteoinfo.geo.mapview.MapView;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JColorChooser;

/**
 *
 * @author User
 */
public class FrmPointSymbolSet extends javax.swing.JDialog {

    private Object _parent = null;
    private PointBreak _pointBreak = null;
    private boolean isLoading = false;
    private String[] _imagePaths = null;
    private MarkerType _markerType = MarkerType.SIMPLE;

    /**
     * Creates new form FrmPointSymbolSet
     * @param parent
     * @param modal
     */
    public FrmPointSymbolSet(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        this.setTitle("Point Symbol Set");
    }

    /**
     * Creates new form FrmPointSymbolSet
     * @param parent
     * @param modal
     * @param tparent
     */
    public FrmPointSymbolSet(java.awt.Dialog parent, boolean modal, Object tparent) {
        super(parent, modal);
        initComponents();
        
        this.setTitle("Point Symbol Set");
        if (tparent.getClass() == LegendView.class){
            this.jButton_Apply.setVisible(false);
            this.jButton_OK.setVisible(false);
            this.setPreferredSize(new Dimension(this.getWidth(), this.getHeight() - 40));
        }

        this.symbolControl1.addSelectedCellChangedListener(new ISelectedCellChangedListener() {
            @Override
            public void selectedCellChangedEvent(SelectedCellChangedEvent event) {
                onSelectedCellChanged(event);
            }
        });

        this._parent = tparent;
    }

     /**
     * Creates new form FrmPointSymbolSet
     * @param parent
     * @param modal
     * @param tparent
     */
    public FrmPointSymbolSet(java.awt.Frame parent, boolean modal, Object tparent) {
        super(parent, modal);
        initComponents();
        
        this.setTitle("Point Symbol Set");
        if (tparent.getClass() == LegendView.class){
            this.jButton_Apply.setVisible(false);
            this.jButton_OK.setVisible(false);
            this.setPreferredSize(new Dimension(this.getWidth(), this.getHeight() - 40));
        }

        this.symbolControl1.addSelectedCellChangedListener(new ISelectedCellChangedListener() {
            @Override
            public void selectedCellChangedEvent(SelectedCellChangedEvent event) {
                onSelectedCellChanged(event);
            }
        });

        this._parent = tparent;
    }
    
    private void onSelectedCellChanged(SelectedCellChangedEvent event) {
        if (isLoading) {
            return;
        }

        _pointBreak.setMarkerType(_markerType);
        switch (_markerType) {
            case CHARACTER:
                _pointBreak.setCharIndex(this.symbolControl1.getSelectedCell());
                break;
            case IMAGE:
                _pointBreak.setImagePath(_imagePaths[this.symbolControl1.getSelectedCell()]);
                break;
            case SIMPLE:
                _pointBreak.setStyle(PointStyle.values()[this.symbolControl1.getSelectedCell()]);
                break;
        }
        if (_parent.getClass() == LegendView.class) {
            ((LegendView) _parent).setLegendBreak_MarkerType(_markerType);
            if (_pointBreak.getMarkerType() == MarkerType.IMAGE) {
                ((LegendView) _parent).setLegendBreak_Image(_imagePaths[this.symbolControl1.getSelectedCell()]);
            } else {
                ((LegendView) _parent).setLegendBreak_MarkerIndex(this.symbolControl1.getSelectedCell());
            }

        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBox_MarkerType = new javax.swing.JComboBox();
        jLabel_FontFamily = new javax.swing.JLabel();
        jComboBox_FontFamily = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jSpinner_Size = new javax.swing.JSpinner();
        jLabel_FillColor = new javax.swing.JLabel();
        jLabel_Color = new javax.swing.JLabel();
        jCheckBox_DrawShape = new javax.swing.JCheckBox();
        jCheckBox_DrawFill = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jSpinner_Angle = new javax.swing.JSpinner();
        jPanel_Outline = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel_OutlineColor = new javax.swing.JLabel();
        jCheckBox_DrawOutline = new javax.swing.JCheckBox();
        jLabel_OutlineSize = new javax.swing.JLabel();
        jSpinner_OutlineSize = new javax.swing.JSpinner();
        jButton_OK = new javax.swing.JButton();
        jButton_Apply = new javax.swing.JButton();
        symbolControl1 = new SymbolControl();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jLabel1.setText("Marker Type:");

        jComboBox_MarkerType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_MarkerType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_MarkerTypeActionPerformed(evt);
            }
        });

        jLabel_FontFamily.setText("Font Family:");

        jComboBox_FontFamily.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_FontFamily.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_FontFamilyActionPerformed(evt);
            }
        });

        jLabel3.setText("Size:");

        jSpinner_Size.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(100.0f), Float.valueOf(0.5f)));
        jSpinner_Size.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner_SizeStateChanged(evt);
            }
        });

        jLabel_FillColor.setText("Fill Color:");

        jLabel_Color.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel_Color.setOpaque(true);
        jLabel_Color.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_ColorMouseClicked(evt);
            }
        });

        jCheckBox_DrawShape.setText("Draw Shape");
        jCheckBox_DrawShape.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_DrawShapeActionPerformed(evt);
            }
        });

        jCheckBox_DrawFill.setText("Draw Fill");
        jCheckBox_DrawFill.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_DrawFillActionPerformed(evt);
            }
        });

        jLabel5.setText("Angle:");

        jSpinner_Angle.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(360.0f), Float.valueOf(0.5f)));
        jSpinner_Angle.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner_AngleStateChanged(evt);
            }
        });

        jPanel_Outline.setBorder(javax.swing.BorderFactory.createTitledBorder("Outline"));
        jPanel_Outline.setToolTipText("Outline");
        jPanel_Outline.setName("Outline"); // NOI18N

        jLabel7.setText("Color:");

        jLabel_OutlineColor.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel_OutlineColor.setOpaque(true);
        jLabel_OutlineColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_OutlineColorMouseClicked(evt);
            }
        });

        jCheckBox_DrawOutline.setText("Draw");
        jCheckBox_DrawOutline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_DrawOutlineActionPerformed(evt);
            }
        });

        jLabel_OutlineSize.setText("Size:");

        jSpinner_OutlineSize.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(100.0f), Float.valueOf(0.5f)));
        jSpinner_OutlineSize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner_OutlineSizeStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel_OutlineLayout = new javax.swing.GroupLayout(jPanel_Outline);
        jPanel_Outline.setLayout(jPanel_OutlineLayout);
        jPanel_OutlineLayout.setHorizontalGroup(
            jPanel_OutlineLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_OutlineLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox_DrawOutline)
                .addGap(18, 18, 18)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_OutlineColor, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel_OutlineSize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSpinner_OutlineSize, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel_OutlineLayout.setVerticalGroup(
            jPanel_OutlineLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_OutlineLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_OutlineLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel_OutlineLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel_OutlineSize)
                        .addComponent(jSpinner_OutlineSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel_OutlineColor, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel_OutlineLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7)
                        .addComponent(jCheckBox_DrawOutline)))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        jButton_OK.setText("OK");
        jButton_OK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OKActionPerformed(evt);
            }
        });

        jButton_Apply.setText("Apply");
        jButton_Apply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ApplyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel_FontFamily)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jComboBox_MarkerType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox_FontFamily, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel_Outline, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(symbolControl1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner_Size, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner_Angle, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox_DrawShape)
                            .addComponent(jCheckBox_DrawFill)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel_FillColor)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel_Color, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(24, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton_OK, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(58, 58, 58)
                .addComponent(jButton_Apply, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox_MarkerType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_FontFamily)
                    .addComponent(jComboBox_FontFamily, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(symbolControl1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBox_DrawShape)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox_DrawFill)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel_FillColor)
                            .addComponent(jLabel_Color, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jSpinner_Angle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jSpinner_Size, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(4, 4, 4)))
                .addComponent(jPanel_Outline, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_OK)
                    .addComponent(jButton_Apply))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jSpinner_SizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner_SizeStateChanged
        // TODO add your handling code here:
        if (isLoading) {
            return;
        } 
        
        float size = Float.parseFloat(this.jSpinner_Size.getValue().toString());
        _pointBreak.setSize(size);
        if (_parent.getClass() == LegendView.class) {
            ((LegendView) _parent).setLegendBreak_Size(size);
        }
    }//GEN-LAST:event_jSpinner_SizeStateChanged

    private void jLabel_ColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_ColorMouseClicked
        // TODO add your handling code here:
        Color aColor = JColorChooser.showDialog(rootPane, null, this.jLabel_Color.getBackground());
        this.jLabel_Color.setBackground(aColor);
        _pointBreak.setColor(aColor);
        if (_parent.getClass() == LegendView.class) {
            ((LegendView) _parent).setLegendBreak_Color(aColor);
        }
    }//GEN-LAST:event_jLabel_ColorMouseClicked

    private void jCheckBox_DrawShapeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_DrawShapeActionPerformed
        // TODO add your handling code here:
        if (isLoading) {
            return;
        }
        
        _pointBreak.setDrawShape(this.jCheckBox_DrawShape.isSelected());
        if (_parent.getClass() == LegendView.class) {
            ((LegendView) _parent).setLegendBreak_DrawShape(this.jCheckBox_DrawShape.isSelected());
        }
    }//GEN-LAST:event_jCheckBox_DrawShapeActionPerformed

    private void jCheckBox_DrawFillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_DrawFillActionPerformed
        // TODO add your handling code here:
        if (isLoading) {
            return;
        }
        
        _pointBreak.setDrawFill(this.jCheckBox_DrawFill.isSelected());
        if (_parent.getClass() == LegendView.class) {
            ((LegendView) _parent).setLegendBreak_DrawFill(this.jCheckBox_DrawFill.isSelected());
        }
    }//GEN-LAST:event_jCheckBox_DrawFillActionPerformed

    private void jSpinner_AngleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner_AngleStateChanged
        // TODO add your handling code here:
        if (isLoading) {
            return;
        }
        
        float angle = Float.parseFloat(this.jSpinner_Angle.getValue().toString());
        _pointBreak.setAngle(angle);
        if (_parent.getClass() == LegendView.class) {
            ((LegendView) _parent).setLegendBreak_Angle(angle);
        }
    }//GEN-LAST:event_jSpinner_AngleStateChanged

    private void jLabel_OutlineColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_OutlineColorMouseClicked
        // TODO add your handling code here:
        Color aColor = JColorChooser.showDialog(rootPane, null, this.jLabel_OutlineColor.getBackground());
        this.jLabel_OutlineColor.setBackground(aColor);
        _pointBreak.setOutlineColor(aColor);
        if (_parent.getClass() == LegendView.class) {
            ((LegendView) _parent).setLegendBreak_OutlineColor(aColor);
        }
    }//GEN-LAST:event_jLabel_OutlineColorMouseClicked

    private void jCheckBox_DrawOutlineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_DrawOutlineActionPerformed
        // TODO add your handling code here:
        if (isLoading) {
            return;
        }
        
        _pointBreak.setDrawOutline(this.jCheckBox_DrawOutline.isSelected());
        if (_parent.getClass() == LegendView.class) {
            ((LegendView) _parent).setLegendBreak_DrawOutline(this.jCheckBox_DrawOutline.isSelected());
        }
    }//GEN-LAST:event_jCheckBox_DrawOutlineActionPerformed

    private void jComboBox_MarkerTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_MarkerTypeActionPerformed
        // TODO add your handling code here:
        if (this.jComboBox_MarkerType.getItemCount() == 0) {
            return;
        }

        switch (MarkerType.valueOf(this.jComboBox_MarkerType.getSelectedItem().toString())) {
            case SIMPLE:
                updateSimpleTab();
                _markerType = MarkerType.SIMPLE;
                break;
            case CHARACTER:
                updateCharacterTab();
                _markerType = MarkerType.CHARACTER;
                break;
            case IMAGE:
                updateImageTab();
                _markerType = MarkerType.IMAGE;
                break;
        }

        //this.symbolControl1._vScrollBar.Value = 0;
    }//GEN-LAST:event_jComboBox_MarkerTypeActionPerformed

    private void jComboBox_FontFamilyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_FontFamilyActionPerformed
        // TODO add your handling code here:
        if (this.jComboBox_FontFamily.getItemCount() == 0) {
            return;
        }

        Font aFont = new Font(this.jComboBox_FontFamily.getSelectedItem().toString(), Font.PLAIN, 10);
//        if (this.jComboBox_FontFamily.getSelectedItem().toString().equals("Weather"))
//            aFont = FontUtil.getWeatherFont();
//        else
//            aFont = new Font(this.jComboBox_FontFamily.getSelectedItem().toString(), Font.PLAIN, 10);
        this.symbolControl1.setFont(aFont);
        this.symbolControl1.setSelectedCell(_pointBreak.getCharIndex());
        this.symbolControl1.repaint();

        _pointBreak.setFontName(aFont.getFontName());
        if (_parent.getClass() == LegendView.class) {
            ((LegendView) _parent).setLegendBreak_FontName(aFont.getFontName());
        }
    }//GEN-LAST:event_jComboBox_FontFamilyActionPerformed

    private void jButton_ApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ApplyActionPerformed
        // TODO add your handling code here:
        if (_parent.getClass() == MapView.class) {
            ((MapView) _parent).paintLayers();
        } else if (_parent.getClass() == MapLayout.class) {
            ((MapLayout) _parent).paintGraphics();
        }
    }//GEN-LAST:event_jButton_ApplyActionPerformed

    private void jButton_OKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OKActionPerformed
        // TODO add your handling code here:
        if (_parent.getClass() == MapView.class) {
            ((MapView) _parent).setDefPointBreak(_pointBreak);
            ((MapView) _parent).paintLayers();
        } else if (_parent.getClass() == MapLayout.class) {
            ((MapLayout) _parent).setDefPointBreak(_pointBreak);
            ((MapLayout) _parent).paintGraphics();
        }

        this.dispose();
    }//GEN-LAST:event_jButton_OKActionPerformed

    private void jSpinner_OutlineSizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner_OutlineSizeStateChanged
        // TODO add your handling code here:
        if (isLoading) {
            return;
        } 
        
        float size = Float.parseFloat(this.jSpinner_OutlineSize.getValue().toString());
        _pointBreak.setOutlineSize(size);
        if (_parent.getClass() == LegendView.class) {
            ((LegendView) _parent).setLegendBreak_OutlineSize(size);
        }
    }//GEN-LAST:event_jSpinner_OutlineSizeStateChanged

    public void setPointBreak(PointBreak pb) {
        _pointBreak = pb;

        updateProperties();
    }

    private void updateProperties() {
        isLoading = true;
        
        this.jLabel_Color.setBackground(_pointBreak.getColor());
        this.jSpinner_Size.setValue(_pointBreak.getSize());
        this.jCheckBox_DrawOutline.setSelected(_pointBreak.isDrawOutline());
        this.jLabel_OutlineColor.setBackground(_pointBreak.getOutlineColor());
        this.jSpinner_OutlineSize.setValue(_pointBreak.getOutlineSize());
        this.jCheckBox_DrawShape.setSelected(_pointBreak.isDrawShape());
        this.jCheckBox_DrawFill.setSelected(_pointBreak.isDrawFill());
        this.jSpinner_Angle.setValue(_pointBreak.getAngle());
        this.jComboBox_MarkerType.removeAllItems();
        for (MarkerType t : MarkerType.values()) {
            this.jComboBox_MarkerType.addItem(t.toString());
        }
        this.jComboBox_MarkerType.setSelectedItem(_pointBreak.getMarkerType().toString());

        isLoading = false;
    }

    private void updateSimpleTab() {
        this.symbolControl1.setMarkerType(MarkerType.SIMPLE);
        this.symbolControl1.setSymbolNumber(PointStyle.values().length);
        this.jLabel_FontFamily.setEnabled(false);
        this.jComboBox_FontFamily.removeAllItems();
        this.jComboBox_FontFamily.setEditable(false);
        this.jPanel_Outline.setEnabled(true);
        this.jSpinner_OutlineSize.setEnabled(true);
        this.jLabel_FillColor.setEnabled(true);
        this.jLabel_Color.setEnabled(true);
        this.jCheckBox_DrawFill.setEnabled(true);
    }

    private void updateCharacterTab() {
        this.symbolControl1.setMarkerType(MarkerType.CHARACTER);
        this.symbolControl1.setSymbolNumber(256);
        this.jLabel_FontFamily.setEnabled(true);
        this.jComboBox_FontFamily.setEditable(true);
        this.jPanel_Outline.setEnabled(false);
        this.jSpinner_OutlineSize.setEnabled(false);
        this.jLabel_FillColor.setEnabled(true);
        this.jLabel_Color.setEnabled(true);
        this.jCheckBox_DrawFill.setEnabled(false);

        this.jComboBox_FontFamily.removeAllItems();
        //FontUtil.registerWeatherFont();
        GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = gEnv.getAvailableFontFamilyNames();
        Font weatherFont = FontUtil.getWeatherFont();
        if (weatherFont != null)
            this.jComboBox_FontFamily.addItem(weatherFont.getFontName());
        for (String ff : fonts) {
            this.jComboBox_FontFamily.addItem(ff);
        }
        this.jComboBox_FontFamily.setSelectedItem(_pointBreak.getFontName());
    }

    private void updateImageTab() {
        this.symbolControl1.setMarkerType(MarkerType.IMAGE);
        this.jLabel_FontFamily.setEnabled(false);
        this.jComboBox_FontFamily.removeAllItems();
        this.jComboBox_FontFamily.setEditable(false);
        this.jPanel_Outline.setEnabled(false);
        this.jSpinner_OutlineSize.setEnabled(false);
        this.jLabel_FillColor.setEnabled(false);
        this.jLabel_Color.setEnabled(false);
        this.jCheckBox_DrawFill.setEnabled(false);

        if (_imagePaths == null) {
            String fn = GlobalUtil.getAppPath(MapFrame.class);
            fn = fn.substring(0, fn.lastIndexOf("/"));
            String path = fn + File.separator + "image";            
            File pathDir = new File(path);
            if (!pathDir.isDirectory()){
                return;
            }

            File[] files = pathDir.listFiles();
            _imagePaths = new String[files.length];
            List<Image> imageList = new ArrayList<>();
            int i = 0;
            for (File aFile : files) {
                _imagePaths[i] = aFile.getAbsolutePath();
                try {
                    imageList.add(ImageIO.read(aFile));
                } catch (IOException ex) {
                    Logger.getLogger(FrmPointSymbolSet.class.getName()).log(Level.SEVERE, null, ex);
                }
                i += 1;
            }

            this.symbolControl1.setIamgeList(imageList);
        } else {
            this.symbolControl1.setSymbolNumber(_imagePaths.length);
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
            Logger.getLogger(FrmPointSymbolSet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(FrmPointSymbolSet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(FrmPointSymbolSet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(FrmPointSymbolSet.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmPointSymbolSet dialog = new FrmPointSymbolSet(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_Apply;
    private javax.swing.JButton jButton_OK;
    private javax.swing.JCheckBox jCheckBox_DrawFill;
    private javax.swing.JCheckBox jCheckBox_DrawOutline;
    private javax.swing.JCheckBox jCheckBox_DrawShape;
    private javax.swing.JComboBox jComboBox_FontFamily;
    private javax.swing.JComboBox jComboBox_MarkerType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel_Color;
    private javax.swing.JLabel jLabel_FillColor;
    private javax.swing.JLabel jLabel_FontFamily;
    private javax.swing.JLabel jLabel_OutlineColor;
    private javax.swing.JLabel jLabel_OutlineSize;
    private javax.swing.JPanel jPanel_Outline;
    private javax.swing.JSpinner jSpinner_Angle;
    private javax.swing.JSpinner jSpinner_OutlineSize;
    private javax.swing.JSpinner jSpinner_Size;
    private SymbolControl symbolControl1;
    // End of variables declaration//GEN-END:variables
}
