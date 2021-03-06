/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.geo.legend;

import org.meteoinfo.geometry.legend.ColorBreak;

import java.awt.Color;
import javax.swing.JColorChooser;

/**
 *
 * @author Yaqiang Wang
 */
public class FrmColorSymbolSet extends javax.swing.JDialog {

    private Object _parent = null;
    private ColorBreak _colorBreak = null;
    
    /**
     * Creates new form FrmColorSymbolSet
     */
    public FrmColorSymbolSet(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }
    
    /**
     * Creates new form FrmColorSymbolSet
     */
    public FrmColorSymbolSet(java.awt.Dialog parent, boolean modal, Object tparent) {
        super(parent, modal);
        initComponents();
        
        this._parent = tparent;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        jSpinner_TransParency = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel_FillColor = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel5.setText("TransParency:");

        jSpinner_TransParency.setModel(new javax.swing.SpinnerNumberModel(0, 0, 100, 10));
        jSpinner_TransParency.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner_TransParencyStateChanged(evt);
            }
        });

        jLabel1.setText("Fill Color:");

        jLabel_FillColor.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel_FillColor.setOpaque(true);
        jLabel_FillColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_FillColorMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel_FillColor, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner_TransParency, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_FillColor, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jSpinner_TransParency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jSpinner_TransParencyStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner_TransParencyStateChanged
        // TODO add your handling code here:
        int alpha = Integer.parseInt(this.jSpinner_TransParency.getValue().toString());
        alpha = (int)((1 - alpha / 100.0) * 255);
        Color c = _colorBreak.getColor();
        _colorBreak.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
        if (_parent.getClass() == LegendView.class) {
            ((LegendView) _parent).setLegendBreak_Color_Transparency(alpha);
        }
    }//GEN-LAST:event_jSpinner_TransParencyStateChanged

    private void jLabel_FillColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_FillColorMouseClicked
        // TODO add your handling code here:
        Color c = JColorChooser.showDialog(rootPane, null, this.jLabel_FillColor.getBackground());
        int trans = Integer.parseInt(this.jSpinner_TransParency.getValue().toString());
        trans = (int)((1 - trans / 100.0) * 255);
        Color aColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), trans);
        this.jLabel_FillColor.setBackground(aColor);
        _colorBreak.setColor(aColor);
        if (_parent.getClass() == LegendView.class) {
            ((LegendView) _parent).setLegendBreak_Color(aColor);
        }
    }//GEN-LAST:event_jLabel_FillColorMouseClicked

    public void setColorBreak(ColorBreak cb){
        _colorBreak = cb;
        updateProperties();
    }
    
    private void updateProperties() {
        this.jLabel_FillColor.setBackground(_colorBreak.getColor());
        int trans = (int)((1 - (double)_colorBreak.getColor().getAlpha() / 255) * 100);
        this.jSpinner_TransParency.setValue(trans);
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
            java.util.logging.Logger.getLogger(FrmColorSymbolSet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmColorSymbolSet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmColorSymbolSet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmColorSymbolSet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmColorSymbolSet dialog = new FrmColorSymbolSet(new javax.swing.JFrame(), true);
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel_FillColor;
    private javax.swing.JSpinner jSpinner_TransParency;
    // End of variables declaration//GEN-END:variables
}
