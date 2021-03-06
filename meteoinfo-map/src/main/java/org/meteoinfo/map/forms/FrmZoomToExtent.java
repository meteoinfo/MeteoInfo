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
package org.meteoinfo.map.forms;

import org.meteoinfo.common.Extent;
import org.meteoinfo.geo.mapview.MapView;
import javax.swing.JOptionPane;

/**
 *
 * @author User
 */
public class FrmZoomToExtent extends javax.swing.JDialog {

    private final FrmMain parent;

    /**
     * Creates new form FrmZoomToExtent
     * @param parent
     * @param modal
     */
    public FrmZoomToExtent(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        this.parent = (FrmMain) parent;

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
        jLabel1 = new javax.swing.JLabel();
        jTextField_MinLon = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField_MaxLon = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField_MinLat = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField_MaxLat = new javax.swing.JTextField();
        jButton_Zoom = new javax.swing.JButton();
        jButton_Close = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Lon/Lat Extent"));

        jLabel1.setText("Min Lon:");

        jTextField_MinLon.setText("jTextField1");

        jLabel2.setText("Max Lon:");

        jTextField_MaxLon.setText("jTextField1");

        jLabel3.setText("Min Lat:");

        jTextField_MinLat.setText("jTextField1");

        jLabel4.setText("Max Lat:");

        jTextField_MaxLat.setText("jTextField1");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_MinLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_MinLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_MaxLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_MaxLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField_MinLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField_MaxLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(jTextField_MaxLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(jTextField_MinLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton_Zoom.setText("Zoom");
        jButton_Zoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ZoomActionPerformed(evt);
            }
        });

        jButton_Close.setText("Close");
        jButton_Close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_CloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton_Zoom)
                .addGap(65, 65, 65)
                .addComponent(jButton_Close)
                .addGap(69, 69, 69))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_Zoom)
                    .addComponent(jButton_Close))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        double minLon, maxLon, minLat, maxLat;
        MapView mapView = ((FrmMain) parent).getMapDocument().getActiveMapFrame().getMapView();
        if (mapView.getProjection().isLonLatMap()) {
            minLon = mapView.getViewExtent().minX;
            maxLon = mapView.getViewExtent().maxX;
            minLat = mapView.getViewExtent().minY;
            maxLat = mapView.getViewExtent().maxY;
        } else {
            minLon = 0;
            maxLon = 0;
            minLat = 0;
            maxLat = 0;
        }

        this.jTextField_MinLon.setText(String.format("%.2f", minLon));
        this.jTextField_MaxLon.setText(String.format("%.2f", maxLon));
        this.jTextField_MinLat.setText(String.format("%.2f", minLat));
        this.jTextField_MaxLat.setText(String.format("%.2f", maxLat));
    }//GEN-LAST:event_formWindowOpened

    private void jButton_ZoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ZoomActionPerformed
        // TODO add your handling code here:
        //Check lon/lat set
        double minLon, maxLon, minLat, maxLat;
        minLon = Double.parseDouble(this.jTextField_MinLon.getText());
        maxLon = Double.parseDouble(this.jTextField_MaxLon.getText());
        minLat = Double.parseDouble(this.jTextField_MinLat.getText());
        maxLat = Double.parseDouble(this.jTextField_MaxLat.getText());

        if (minLon >= maxLon || minLat >= maxLat) {
            JOptionPane.showMessageDialog(this, "Lon/Lat set error!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Zoom to lon/lat extent
        Extent aExtent = new Extent();
        aExtent.minX = minLon;
        aExtent.maxX = maxLon;
        aExtent.minY = minLat;
        aExtent.maxY = maxLat;

        FrmMain frmMain = (FrmMain) parent;
        if (frmMain.getMainTab().getSelectedIndex() == 0) {
            frmMain.getMapDocument().getActiveMapFrame().getMapView().zoomToExtentLonLatEx(aExtent);
        } else if (frmMain.getMainTab().getSelectedIndex() == 1) {
            frmMain.getMapDocument().getMapLayout().getActiveLayoutMap().zoomToExtentLonLatEx(aExtent);
            frmMain.getMapDocument().getMapLayout().paintGraphics();
        }
    }//GEN-LAST:event_jButton_ZoomActionPerformed

    private void jButton_CloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_CloseActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jButton_CloseActionPerformed

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
            java.util.logging.Logger.getLogger(FrmZoomToExtent.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmZoomToExtent.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmZoomToExtent.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmZoomToExtent.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmZoomToExtent dialog = new FrmZoomToExtent(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton jButton_Close;
    private javax.swing.JButton jButton_Zoom;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField_MaxLat;
    private javax.swing.JTextField jTextField_MaxLon;
    private javax.swing.JTextField jTextField_MinLat;
    private javax.swing.JTextField jTextField_MinLon;
    // End of variables declaration//GEN-END:variables
}
