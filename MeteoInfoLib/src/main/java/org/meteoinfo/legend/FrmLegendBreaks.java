/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.legend;

import org.meteoinfo.layer.FrmLayerProperty;
import org.meteoinfo.shape.ShapeTypes;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.meteoinfo.global.colors.ColorMap;
import org.meteoinfo.global.colors.ColorUtil;
import org.meteoinfo.global.util.BigDecimalUtil;
import org.meteoinfo.ui.ColorComboBoxModel;
import org.meteoinfo.ui.ColorListCellRender;

/**
 *
 * @author yaqiang
 */
public class FrmLegendBreaks extends javax.swing.JDialog {

    private final java.awt.Dialog _parent;
    private LegendScheme _legendScheme = null;
    private double startValue, endValue, _interval;
    private boolean _isUniqueValue = false;

    /**
     * Constructor
     *
     * @param parent Parent dialog
     * @param modal Modal
     * @param isUniqueValue If is unique value legend scheme
     */
    public FrmLegendBreaks(java.awt.Dialog parent, boolean modal, boolean isUniqueValue) {
        super(parent, modal);
        initComponents();

        _parent = parent;
        _isUniqueValue = isUniqueValue;
    }

    private void setInterval() {
        int bnum = _legendScheme.getBreakNum();
        if (bnum > 2) {
            ColorBreak aCB = _legendScheme.getLegendBreaks().get(0);
            startValue = Double.parseDouble(aCB.getEndValue().toString());
            if (_legendScheme.getHasNoData()) {
                aCB = _legendScheme.getLegendBreaks().get(bnum - 2);
                endValue = Double.parseDouble(aCB.getStartValue().toString());
                _interval = BigDecimalUtil.div((BigDecimalUtil.sub(endValue, startValue)), (bnum - 3));
            } else {
                aCB = _legendScheme.getLegendBreaks().get(bnum - 1);
                endValue = Double.parseDouble(aCB.getStartValue().toString());
                switch (_legendScheme.getShapeType()) {
                    case Polyline:
                    case PolylineZ:
                        _interval = BigDecimalUtil.div((BigDecimalUtil.sub(endValue, startValue)), (bnum - 1));
                        break;
                    default:
                        _interval = BigDecimalUtil.div((BigDecimalUtil.sub(endValue, startValue)), (bnum - 2));
                        break;
                }
            }

            this.jTextField_StartValue.setText(String.valueOf(startValue));
            this.jTextField_EndValue.setText(String.valueOf(endValue));
            this.jTextField_Interval.setText(String.valueOf(_interval));
        }
    }
    
    private void setInterval_log(){
        double min = this._legendScheme.getMinValue();
        double max = this._legendScheme.getMaxValue();
        int minE = (int)Math.floor(Math.log10(min));
        int maxE = (int)Math.floor(Math.log10(max));
        if (min == 0)
            minE = maxE - 3;
        if (max == 0)
            maxE = minE + 3;
        this.jTextField_StartValue.setText(String.valueOf(minE));
        this.jTextField_EndValue.setText(String.valueOf(maxE));
        this.jTextField_Interval.setText(String.valueOf(1));
    }

    private void initialize() {
        if (!this._isUniqueValue) {
            this.jLabel_Min.setEnabled(true);
            this.jLabel_Max.setEnabled(true);
            this.jLabel_From.setEnabled(true);
            this.jTextField_StartValue.setEnabled(true);
            this.jLabel_To.setEnabled(true);
            this.jTextField_EndValue.setEnabled(true);
            this.jLabel_Interval.setEnabled(true);
            this.jTextField_Interval.setEnabled(true);
            this.jButton_NewLegend.setEnabled(true);

            this.jLabel_Min.setText("Min: " + String.format("%1$E", _legendScheme.getMinValue()));
            this.jLabel_Max.setText("Max: " + String.format("%1$E", _legendScheme.getMaxValue()));
            this.setInterval();
        } else {
            this.jLabel_Min.setEnabled(false);
            this.jLabel_Max.setEnabled(false);
            this.jLabel_From.setEnabled(false);
            this.jTextField_StartValue.setEnabled(false);
            this.jLabel_To.setEnabled(false);
            this.jTextField_EndValue.setEnabled(false);
            this.jLabel_Interval.setEnabled(false);
            this.jTextField_Interval.setEnabled(false);
            this.jButton_NewLegend.setEnabled(false);
        }

        ColorMap[] colorTables;
        try {
            colorTables = ColorUtil.getColorTables();
            ColorListCellRender render = new ColorListCellRender();
            render.setPreferredSize(new Dimension(62, 21));
            this.jComboBox_ColorTable.setModel(new ColorComboBoxModel(colorTables));
            this.jComboBox_ColorTable.setRenderer(render);
            ColorMap ct = ColorUtil.findColorTable(colorTables, "grads_rainbow");
            if (ct != null) {
                this.jComboBox_ColorTable.setSelectedItem(ct);
            } else {
                this.jComboBox_ColorTable.setSelectedIndex(0);
            }
        } catch (IOException ex) {
            Logger.getLogger(FrmLegendBreaks.class.getName()).log(Level.SEVERE, null, ex);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel_Min = new javax.swing.JLabel();
        jLabel_Max = new javax.swing.JLabel();
        jButton_NewLegend = new javax.swing.JButton();
        jButton_NewColors = new javax.swing.JButton();
        jComboBox_ColorTable = new javax.swing.JComboBox();
        jLabel_ColorTable = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel_Interval = new javax.swing.JPanel();
        jLabel_From = new javax.swing.JLabel();
        jTextField_StartValue = new javax.swing.JTextField();
        jLabel_To = new javax.swing.JLabel();
        jTextField_EndValue = new javax.swing.JTextField();
        jLabel_Interval = new javax.swing.JLabel();
        jTextField_Interval = new javax.swing.JTextField();
        jCheckBox_Log = new javax.swing.JCheckBox();
        jTextField_logFactor = new javax.swing.JTextField();
        jPanel_Values = new javax.swing.JPanel();
        jTextField_Values = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jLabel_Min.setText("Min:");

        jLabel_Max.setText("Max:");

        jButton_NewLegend.setText("New Legend");
        jButton_NewLegend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewLegendActionPerformed(evt);
            }
        });

        jButton_NewColors.setText("New Colors");
        jButton_NewColors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_NewColorsActionPerformed(evt);
            }
        });

        jComboBox_ColorTable.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel_ColorTable.setText("Color table:");

        jLabel_From.setText("from:");

        jTextField_StartValue.setPreferredSize(new java.awt.Dimension(89, 24));

        jLabel_To.setText("to:");

        jTextField_EndValue.setPreferredSize(new java.awt.Dimension(89, 24));

        jLabel_Interval.setText("Interval:");

        jTextField_Interval.setPreferredSize(new java.awt.Dimension(89, 24));

        jCheckBox_Log.setText("Log");
        jCheckBox_Log.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_LogActionPerformed(evt);
            }
        });

        jTextField_logFactor.setText("1");
        jTextField_logFactor.setEnabled(false);

        javax.swing.GroupLayout jPanel_IntervalLayout = new javax.swing.GroupLayout(jPanel_Interval);
        jPanel_Interval.setLayout(jPanel_IntervalLayout);
        jPanel_IntervalLayout.setHorizontalGroup(
            jPanel_IntervalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_IntervalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_IntervalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_Interval)
                    .addComponent(jLabel_From))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_IntervalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField_StartValue, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField_Interval, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel_IntervalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel_IntervalLayout.createSequentialGroup()
                        .addComponent(jLabel_To)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_EndValue, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE))
                    .addGroup(jPanel_IntervalLayout.createSequentialGroup()
                        .addComponent(jCheckBox_Log)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_logFactor)))
                .addContainerGap())
        );
        jPanel_IntervalLayout.setVerticalGroup(
            jPanel_IntervalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_IntervalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_IntervalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_From)
                    .addComponent(jTextField_StartValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_To)
                    .addComponent(jTextField_EndValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_IntervalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_Interval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_Interval)
                    .addComponent(jCheckBox_Log)
                    .addComponent(jTextField_logFactor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Interval", jPanel_Interval);

        jLabel1.setText("Delimiter is \";\"");

        javax.swing.GroupLayout jPanel_ValuesLayout = new javax.swing.GroupLayout(jPanel_Values);
        jPanel_Values.setLayout(jPanel_ValuesLayout);
        jPanel_ValuesLayout.setHorizontalGroup(
            jPanel_ValuesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_ValuesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_ValuesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel_ValuesLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel1)
                        .addContainerGap(263, Short.MAX_VALUE))
                    .addGroup(jPanel_ValuesLayout.createSequentialGroup()
                        .addComponent(jTextField_Values)
                        .addContainerGap())))
        );
        jPanel_ValuesLayout.setVerticalGroup(
            jPanel_ValuesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_ValuesLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jTextField_Values, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Values", jPanel_Values);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel_Min)
                        .addGap(101, 101, 101)
                        .addComponent(jLabel_Max))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(jLabel_ColorTable)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jComboBox_ColorTable, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 437, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addComponent(jButton_NewLegend)
                .addGap(56, 56, 56)
                .addComponent(jButton_NewColors)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_Max)
                    .addComponent(jLabel_Min))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_ColorTable)
                    .addComponent(jComboBox_ColorTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_NewLegend)
                    .addComponent(jButton_NewColors))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        this.initialize();
    }//GEN-LAST:event_formWindowOpened

    private void jButton_NewLegendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewLegendActionPerformed
        // TODO add your handling code here:
        double[] cValues;
        if (this.jTabbedPane1.getSelectedIndex() == 0) {
            _interval = Double.parseDouble(this.jTextField_Interval.getText());
            startValue = Double.parseDouble(this.jTextField_StartValue.getText());
            endValue = Double.parseDouble(this.jTextField_EndValue.getText());

            if ((int) ((endValue - startValue) / _interval) < 2) {
                JOptionPane.showMessageDialog(null, "Please reset the data!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (this.jCheckBox_Log.isSelected()) {
                int sv = Integer.parseInt(this.jTextField_StartValue.getText());
                int ev = Integer.parseInt(this.jTextField_EndValue.getText());
                int step = Integer.parseInt(this.jTextField_Interval.getText());
                double factor = Double.parseDouble(this.jTextField_logFactor.getText());
                List<Integer> vs = new ArrayList<>();
                while (sv < ev) {
                    vs.add(sv);
                    sv += step;
                }
                cValues = new double[vs.size()];
                for (int i = 0; i < vs.size(); i++) {
                    cValues[i] = Math.pow(10, vs.get(i)) * factor;
                }
            } else {
                cValues = LegendManage.createContourValuesInterval(startValue, endValue,
                        _interval);
            }
        } else {
            String vstr = this.jTextField_Values.getText();
            String[] vstrs = vstr.split(";");
            cValues = new double[vstrs.length];
            for (int i = 0; i < vstrs.length; i++) {
                cValues[i] = Double.parseDouble(vstrs[i].trim());
            }
        }

        Color[] colors = createColors(cValues.length + 1);

        LegendScheme aLS;
        if (_isUniqueValue) {
            aLS = LegendManage.createUniqValueLegendScheme(cValues, colors, _legendScheme.getShapeType(),
                    _legendScheme.getMinValue(), _legendScheme.getMaxValue(), _legendScheme.getHasNoData(), _legendScheme.getUndefValue());
        } else {
            aLS = LegendManage.createGraduatedLegendScheme(cValues, colors, _legendScheme.getShapeType(),
                    _legendScheme.getMinValue(), _legendScheme.getMaxValue(), _legendScheme.getHasNoData(),
                    _legendScheme.getUndefValue());
        }
        aLS.setFieldName(_legendScheme.getFieldName());
        //setLegendScheme(aLS);
        this._legendScheme = aLS;

        if (_parent.getClass() == FrmLegendSet.class) {
            ((FrmLegendSet) _parent).setLegendScheme(aLS);
        } else if (_parent.getClass() == FrmLayerProperty.class) {
            ((FrmLayerProperty) _parent).setLegendScheme(aLS);
        }
    }//GEN-LAST:event_jButton_NewLegendActionPerformed

    private void jButton_NewColorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_NewColorsActionPerformed
        // TODO add your handling code here:
        int colorNum = _legendScheme.getBreakNum();

        if (_legendScheme.getShapeType() == ShapeTypes.Polyline) {
            colorNum += 1;
        }

        Color[] colors = createColors(colorNum);

        int i;
        for (i = 0; i < _legendScheme.getBreakNum(); i++) {
            _legendScheme.getLegendBreaks().get(i).setColor(colors[i]);
        }

        if (_parent.getClass() == FrmLegendSet.class) {
            ((FrmLegendSet) _parent).setLegendScheme(_legendScheme);
        } else if (_parent.getClass() == FrmLayerProperty.class) {
            ((FrmLayerProperty) _parent).setLegendScheme(_legendScheme);
        }
    }//GEN-LAST:event_jButton_NewColorsActionPerformed

    private void jCheckBox_LogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_LogActionPerformed
        // TODO add your handling code here:
        this.jTextField_logFactor.setEnabled(this.jCheckBox_Log.isSelected());
        if (this.jCheckBox_Log.isSelected()){
            this.setInterval_log();
        } else {
            this.setInterval();
        }
    }//GEN-LAST:event_jCheckBox_LogActionPerformed

    /**
     * Set legend scheme
     *
     * @param aLS Legend scheme
     */
    public void setLegendScheme(LegendScheme aLS) {
        _legendScheme = (LegendScheme) aLS.clone();
        this.initialize();
    }

    private Color[] createColors(int colorNum) {
        ColorComboBoxModel model = (ColorComboBoxModel) this.jComboBox_ColorTable.getModel();
        ColorMap ct = (ColorMap) model.getSelectedItem();
        return ct.getColors(colorNum);
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
            java.util.logging.Logger.getLogger(FrmLegendBreaks.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmLegendBreaks.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmLegendBreaks.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmLegendBreaks.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                FrmLegendBreaks dialog = new FrmLegendBreaks(new javax.swing.JDialog(), true, false);
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
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton_NewColors;
    private javax.swing.JButton jButton_NewLegend;
    private javax.swing.JCheckBox jCheckBox_Log;
    private javax.swing.JComboBox jComboBox_ColorTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel_ColorTable;
    private javax.swing.JLabel jLabel_From;
    private javax.swing.JLabel jLabel_Interval;
    private javax.swing.JLabel jLabel_Max;
    private javax.swing.JLabel jLabel_Min;
    private javax.swing.JLabel jLabel_To;
    private javax.swing.JPanel jPanel_Interval;
    private javax.swing.JPanel jPanel_Values;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField_EndValue;
    private javax.swing.JTextField jTextField_Interval;
    private javax.swing.JTextField jTextField_StartValue;
    private javax.swing.JTextField jTextField_Values;
    private javax.swing.JTextField jTextField_logFactor;
    // End of variables declaration//GEN-END:variables
}
