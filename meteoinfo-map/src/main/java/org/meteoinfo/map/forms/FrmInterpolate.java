/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.map.forms;

import java.util.ArrayList;
import java.util.List;

import org.meteoinfo.data.GridDataSetting;
import org.meteoinfo.geo.analysis.InterpolationMethods;
import org.meteoinfo.geo.analysis.InterpolationSetting;

/**
 *
 * @author yaqiang
 */
public class FrmInterpolate extends javax.swing.JDialog {
    
    private InterpolationSetting _interpSetting = new InterpolationSetting();
    private boolean _isOK = false;

    /**
     * Creates new form FrmInterpolate
     */
    public FrmInterpolate(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel_MinX = new javax.swing.JLabel();
        jTextField_MinX = new javax.swing.JTextField();
        jLabel_MaxX = new javax.swing.JLabel();
        jTextField_MaxX = new javax.swing.JTextField();
        jLabel_MinY = new javax.swing.JLabel();
        jTextField_MinY = new javax.swing.JTextField();
        jLabel_MaxY = new javax.swing.JLabel();
        jTextField_MaxY = new javax.swing.JTextField();
        jLabel_XSize = new javax.swing.JLabel();
        jTextField_XSize = new javax.swing.JTextField();
        jLabel_YSize = new javax.swing.JLabel();
        jTextField_YSize = new javax.swing.JTextField();
        jLabel_XNum = new javax.swing.JLabel();
        jTextField_XNum = new javax.swing.JTextField();
        jLabel_YNum = new javax.swing.JLabel();
        jTextField_YNum = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel_Method = new javax.swing.JLabel();
        jComboBox_Methods = new javax.swing.JComboBox();
        jLabel_Radius = new javax.swing.JLabel();
        jTextField_Radius = new javax.swing.JTextField();
        jLabel_MinNum = new javax.swing.JLabel();
        jTextField_MinNum = new javax.swing.JTextField();
        jLabel_MissingValue = new javax.swing.JLabel();
        jTextField_MissingValue = new javax.swing.JTextField();
        jButton_OK = new javax.swing.JButton();
        jButton_Cancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Interpolate");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Output Grid Set"));

        jLabel_MinX.setText("minX:");

        jTextField_MinX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_MinXActionPerformed(evt);
            }
        });

        jLabel_MaxX.setText("maxX:");

        jTextField_MaxX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_MaxXActionPerformed(evt);
            }
        });

        jLabel_MinY.setText("minY:");

        jTextField_MinY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_MinYActionPerformed(evt);
            }
        });

        jLabel_MaxY.setText("maxY:");

        jTextField_MaxY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_MaxYActionPerformed(evt);
            }
        });

        jLabel_XSize.setText("XSize:");

        jTextField_XSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_XSizeActionPerformed(evt);
            }
        });

        jLabel_YSize.setText("YSize:");

        jTextField_YSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_YSizeActionPerformed(evt);
            }
        });

        jLabel_XNum.setText("XNum:");

        jTextField_XNum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_XNumActionPerformed(evt);
            }
        });

        jLabel_YNum.setText("YNum:");

        jTextField_YNum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_YNumActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_MinX, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_MinY, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_XSize, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_XNum, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField_MinX, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField_MinY, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField_XSize, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField_XNum, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_MaxX, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_MaxY, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_YSize, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_YNum, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField_MaxX, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField_MaxY, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField_YSize, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField_YNum, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_MinX)
                    .addComponent(jTextField_MinX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_MaxX)
                    .addComponent(jTextField_MaxX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_MinY)
                    .addComponent(jTextField_MinY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_MaxY)
                    .addComponent(jTextField_MaxY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_XSize)
                    .addComponent(jTextField_XSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_YSize)
                    .addComponent(jTextField_YSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_XNum)
                    .addComponent(jTextField_XNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_YNum)
                    .addComponent(jTextField_YNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Method Set"));

        jLabel_Method.setText("Method:");

        jComboBox_Methods.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_Methods.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_MethodsActionPerformed(evt);
            }
        });

        jLabel_Radius.setText("Radius:");

        jLabel_MinNum.setText("MinNum:");

        jLabel_MissingValue.setText("Missing Value:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel_Method)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox_Methods, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel_MinNum)
                            .addComponent(jLabel_Radius))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jTextField_MinNum, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel_MissingValue)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField_MissingValue))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jTextField_Radius, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_Method)
                    .addComponent(jComboBox_Methods, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_Radius)
                    .addComponent(jTextField_Radius, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_MinNum)
                    .addComponent(jTextField_MinNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_MissingValue)
                    .addComponent(jTextField_MissingValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton_OK.setText("OK");
        jButton_OK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OKActionPerformed(evt);
            }
        });

        jButton_Cancel.setText("Cancel");
        jButton_Cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_CancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(67, 67, 67)
                        .addComponent(jButton_OK, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addComponent(jButton_Cancel)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_OK)
                    .addComponent(jButton_Cancel))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField_XSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_XSizeActionPerformed
        // TODO add your handling code here:
        this.setXYNum();
    }//GEN-LAST:event_jTextField_XSizeActionPerformed
    
    private void jTextField_YSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_YSizeActionPerformed
        // TODO add your handling code here:
        this.setXYNum();
    }//GEN-LAST:event_jTextField_YSizeActionPerformed
    
    private void jTextField_MinXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_MinXActionPerformed
        // TODO add your handling code here:
        this.setXYSize();
    }//GEN-LAST:event_jTextField_MinXActionPerformed
    
    private void jTextField_MaxXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_MaxXActionPerformed
        // TODO add your handling code here:
        this.setXYSize();
    }//GEN-LAST:event_jTextField_MaxXActionPerformed
    
    private void jTextField_MinYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_MinYActionPerformed
        // TODO add your handling code here:
        this.setXYSize();
    }//GEN-LAST:event_jTextField_MinYActionPerformed
    
    private void jTextField_MaxYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_MaxYActionPerformed
        // TODO add your handling code here:
        this.setXYSize();
    }//GEN-LAST:event_jTextField_MaxYActionPerformed
    
    private void jTextField_XNumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_XNumActionPerformed
        // TODO add your handling code here:
        this.setXYSize();
    }//GEN-LAST:event_jTextField_XNumActionPerformed
    
    private void jTextField_YNumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_YNumActionPerformed
        // TODO add your handling code here:
        this.setXYSize();
    }//GEN-LAST:event_jTextField_YNumActionPerformed
    
    private void jComboBox_MethodsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_MethodsActionPerformed
        // TODO add your handling code here:
        if (jComboBox_Methods.getItemCount() == 0) {
            return;
        }
        
        InterpolationMethods method = InterpolationMethods.valueOf(this.jComboBox_Methods.getSelectedItem().toString());
        switch (method) {
            case IDW_RADIUS:
                this.jTextField_Radius.setEnabled(true);
                this.jTextField_MinNum.setEnabled(true);
                this.jTextField_MinNum.setText(String.valueOf(this._interpSetting.getMinPointNum()));
                this.jLabel_MinNum.setText("MinNum:");
                this.jTextField_Radius.setText(String.valueOf(this._interpSetting.getRadius()));                
                break;
            case IDW_NEIGHBORS:
                this.jTextField_Radius.setEnabled(false);
                this.jTextField_MinNum.setEnabled(true);
                this.jTextField_MinNum.setText(String.valueOf(this._interpSetting.getMinPointNum()));
                this.jLabel_MinNum.setText("MinNum:");
                this.jTextField_Radius.setText(String.valueOf(this._interpSetting.getRadius()));                
                break;
            case CRESSMAN:
            case BARNES:
                this.jTextField_Radius.setEnabled(true);
                this.jTextField_MinNum.setEnabled(false);
                this.jTextField_MinNum.setText(String.valueOf(this._interpSetting.getMinPointNum()));
                this.jLabel_MinNum.setText("MinNum:");
                String radStr = "";
                for (int i = 0; i < this._interpSetting.getRadiusList().size(); i++) {
                    radStr = radStr + String.valueOf(this._interpSetting.getRadiusList().get(i)) + ";";
                }
                
                radStr = radStr.substring(0, radStr.length() - 1);
                this.jTextField_Radius.setText(radStr);
                break;
            case KRIGING:
                this.jTextField_Radius.setEnabled(false);
                this.jTextField_MinNum.setEnabled(true);
                this.jLabel_MinNum.setText("Beta:");
                this.jTextField_MinNum.setText(String.valueOf(this._interpSetting.getBeta()));
                break;
        }
    }//GEN-LAST:event_jComboBox_MethodsActionPerformed

    private void jButton_OKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OKActionPerformed
        // TODO add your handling code here:
        this._isOK = true;
        this.dispose();
    }//GEN-LAST:event_jButton_OKActionPerformed

    private void jButton_CancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_CancelActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jButton_CancelActionPerformed
    
    public boolean isOK(){
        return this._isOK;
    }
    
    public void setParameters(InterpolationSetting interpSetting) {
        this._interpSetting = interpSetting;
        
        this.jTextField_MinX.setText(String.valueOf(interpSetting.getGridDataSetting().dataExtent.minX));
        this.jTextField_MaxX.setText(String.valueOf(interpSetting.getGridDataSetting().dataExtent.maxX));
        this.jTextField_MinY.setText(String.valueOf(interpSetting.getGridDataSetting().dataExtent.minY));
        this.jTextField_MaxY.setText(String.valueOf(interpSetting.getGridDataSetting().dataExtent.maxY));
        this.jTextField_XNum.setText(String.valueOf(interpSetting.getGridDataSetting().xNum));
        this.jTextField_YNum.setText(String.valueOf(interpSetting.getGridDataSetting().yNum));
        setXYSize();
        
        this.jComboBox_Methods.removeAllItems();
        this.jComboBox_Methods.addItem(InterpolationMethods.IDW_RADIUS);
        this.jComboBox_Methods.addItem(InterpolationMethods.IDW_NEIGHBORS);
        this.jComboBox_Methods.addItem(InterpolationMethods.CRESSMAN);
        this.jComboBox_Methods.addItem(InterpolationMethods.BARNES);
        this.jComboBox_Methods.addItem(InterpolationMethods.KRIGING);
        this.jComboBox_Methods.addItem(InterpolationMethods.ASSIGN_POINT_GRID);
        this.jComboBox_Methods.setSelectedItem(this._interpSetting.getInterpolationMethod());
        switch (this._interpSetting.getInterpolationMethod()) {
            case CRESSMAN:
            case BARNES:
                String radStr = "";
                for (int i = 0; i < this._interpSetting.getRadiusList().size(); i++) {
                    radStr = radStr + String.valueOf(this._interpSetting.getRadiusList().get(i)) + ";";
                }
                
                radStr = radStr.substring(0, radStr.length() - 1);
                this.jTextField_Radius.setText(radStr);
                this.jTextField_MinNum.setText(String.valueOf(this._interpSetting.getMinPointNum()));
                this.jTextField_MissingValue.setText(String.valueOf(this._interpSetting.getMissingValue()));
                this.jTextField_MissingValue.setEnabled(false);
                break;
            case IDW_NEIGHBORS:
            case IDW_RADIUS:
                this.jTextField_Radius.setText(((Double) this._interpSetting.getRadius()).toString());
                this.jTextField_MinNum.setText(String.valueOf(this._interpSetting.getMinPointNum()));
                this.jTextField_MissingValue.setText(String.valueOf(this._interpSetting.getMissingValue()));
                this.jTextField_MissingValue.setEnabled(false);
                break;
            case KRIGING:
                this.jTextField_MinNum.setText(String.valueOf(this._interpSetting.getBeta()));
                break;
            case ASSIGN_POINT_GRID:
                
                break;
        }
    }
    
    public InterpolationSetting getParameters() {
        float minX, maxX, minY, maxY;
        minX = Float.parseFloat(this.jTextField_MinX.getText());
        maxX = Float.parseFloat(this.jTextField_MaxX.getText());
        minY = Float.parseFloat(this.jTextField_MinY.getText());
        maxY = Float.parseFloat(this.jTextField_MaxY.getText());
        GridDataSetting aGDP = new GridDataSetting();
        aGDP.dataExtent.minX = minX;
        aGDP.dataExtent.maxX = maxX;
        aGDP.dataExtent.minY = minY;
        aGDP.dataExtent.maxY = maxY;
        aGDP.xNum = Integer.parseInt(this.jTextField_XNum.getText());
        aGDP.yNum = Integer.parseInt(this.jTextField_YNum.getText());
        
        InterpolationSetting interpSetting = new InterpolationSetting();
        interpSetting.setGridDataSetting(aGDP);
        
        interpSetting.setInterpolationMethod((InterpolationMethods) this.jComboBox_Methods.getSelectedItem());
        switch (interpSetting.getInterpolationMethod()) {
            case CRESSMAN:
            case BARNES:
                if (!this.jTextField_Radius.getText().isEmpty()) {
                    String[] radStrs = this.jTextField_Radius.getText().split(";");
                    List<Double> radList = new ArrayList<Double>();
                    for (int i = 0; i < radStrs.length; i++) {
                        radList.add(Double.parseDouble(radStrs[i]));
                    }
                    interpSetting.setRadiusList(radList);
                } else {
                    interpSetting.setRadiusList(new ArrayList<Double>());
                }
                
                interpSetting.setMinPointNum(Integer.parseInt(this.jTextField_MinNum.getText()));
                break;
            case IDW_NEIGHBORS:
            case IDW_RADIUS:
                interpSetting.setRadius(Double.parseDouble(this.jTextField_Radius.getText()));
                interpSetting.setMinPointNum(Integer.parseInt(this.jTextField_MinNum.getText()));
                break;
            case KRIGING:
                interpSetting.setBeta(Double.parseDouble(this.jTextField_MinNum.getText()));
                break;
        }
        
        return interpSetting;
    }
    
    private void setXYSize() {
        float minX, maxX, minY, maxY;
        float XSize, YSize;
        int XNum, YNum;
        minX = Float.parseFloat(this.jTextField_MinX.getText());
        maxX = Float.parseFloat(this.jTextField_MaxX.getText());
        minY = Float.parseFloat(this.jTextField_MinY.getText());
        maxY = Float.parseFloat(this.jTextField_MaxY.getText());
        XNum = Integer.parseInt(this.jTextField_XNum.getText());
        YNum = Integer.parseInt(this.jTextField_YNum.getText());
        
        XSize = (maxX - minX) / (XNum - 1);
        YSize = (maxY - minY) / (YNum - 1);
        this.jTextField_XSize.setText(String.valueOf(XSize));
        this.jTextField_YSize.setText(String.valueOf(YSize));
    }
    
    private void setXYNum() {
        float minX, maxX, minY, maxY;
        float XSize, YSize;
        int XNum, YNum;
        minX = Float.parseFloat(this.jTextField_MinX.getText());
        maxX = Float.parseFloat(this.jTextField_MaxX.getText());
        minY = Float.parseFloat(this.jTextField_MinY.getText());
        maxY = Float.parseFloat(this.jTextField_MaxY.getText());
        XSize = Float.parseFloat(this.jTextField_XSize.getText());
        YSize = Float.parseFloat(this.jTextField_YSize.getText());
        
        XNum = (int) ((maxX - minX) / XSize);
        YNum = (int) ((maxY - minY) / YSize);
        
        maxX = minX + XNum * XSize;
        maxY = minY + YNum * YSize;
        
        XNum += 1;
        YNum += 1;
        this.jTextField_XNum.setText(String.valueOf(XNum));
        this.jTextField_YNum.setText(String.valueOf(YNum));
        
        this.jTextField_MaxX.setText(String.valueOf(maxX));
        this.jTextField_MaxY.setText(String.valueOf(maxY));
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
            java.util.logging.Logger.getLogger(FrmInterpolate.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmInterpolate.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmInterpolate.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmInterpolate.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmInterpolate dialog = new FrmInterpolate(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton jButton_Cancel;
    private javax.swing.JButton jButton_OK;
    private javax.swing.JComboBox jComboBox_Methods;
    private javax.swing.JLabel jLabel_MaxX;
    private javax.swing.JLabel jLabel_MaxY;
    private javax.swing.JLabel jLabel_Method;
    private javax.swing.JLabel jLabel_MinNum;
    private javax.swing.JLabel jLabel_MinX;
    private javax.swing.JLabel jLabel_MinY;
    private javax.swing.JLabel jLabel_MissingValue;
    private javax.swing.JLabel jLabel_Radius;
    private javax.swing.JLabel jLabel_XNum;
    private javax.swing.JLabel jLabel_XSize;
    private javax.swing.JLabel jLabel_YNum;
    private javax.swing.JLabel jLabel_YSize;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField jTextField_MaxX;
    private javax.swing.JTextField jTextField_MaxY;
    private javax.swing.JTextField jTextField_MinNum;
    private javax.swing.JTextField jTextField_MinX;
    private javax.swing.JTextField jTextField_MinY;
    private javax.swing.JTextField jTextField_MissingValue;
    private javax.swing.JTextField jTextField_Radius;
    private javax.swing.JTextField jTextField_XNum;
    private javax.swing.JTextField jTextField_XSize;
    private javax.swing.JTextField jTextField_YNum;
    private javax.swing.JTextField jTextField_YSize;
    // End of variables declaration//GEN-END:variables
}
