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
package org.meteoinfo.geo.mapdata;

 import com.formdev.flatlaf.extras.FlatSVGUtils;
 import org.meteoinfo.geo.layer.VectorLayer;
 import org.meteoinfo.ndarray.DataType;
 import org.meteoinfo.table.*;

 import javax.swing.*;
 import javax.swing.table.JTableHeader;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;

 /**
  *
  * @author Yaqiang Wang
  */
 public class FrmAttriData extends JFrame {

     private VectorLayer _layer;
     private DataTable _dataTable;
     private boolean _isEditing = false;

     /**
      * Creates new form FrmAttriData
      */
     public FrmAttriData() {
         initComponents();

         this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         this.setIconImages(FlatSVGUtils.createWindowIconImages("/org/meteoinfo/icons/table.svg"));

         //this.jTable1.setColumnSelectionAllowed(true);
         //this.jTable1.setRowSelectionAllowed(false);
         final JTableHeader header = this.jTable1.getTableHeader();
         header.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseReleased(MouseEvent e) {

                 if (!e.isShiftDown()) {
                     jTable1.clearSelection();
                 }

                 //Get column index of the mouse point
                 int pick = header.columnAtPoint(e.getPoint());

                 //set select model
                 //jTable1.addColumnSelectionInterval(pick, pick);
                 jTable1.setColumnSelectionAllowed(true);
                 jTable1.setRowSelectionAllowed(false);
                 jTable1.setColumnSelectionInterval(pick, pick);

                 if (_isEditing) {
                     jMenuItem_RemoveField.setEnabled(true);
                     jMenuItem_RenameField.setEnabled(true);
                 }
             }
         });
         this.jTable1.setDefaultEditor(Object.class, new MyCellEditor());
         this.jTable1.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseReleased(MouseEvent e) {
                 jTable1.clearSelection();
                 int pick = jTable1.rowAtPoint(e.getPoint());
                 jTable1.setColumnSelectionAllowed(false);
                 jTable1.setRowSelectionAllowed(true);
                 jTable1.setRowSelectionInterval(pick, pick);

                 if (jMenuItem_RemoveField.isEnabled()) {
                     jMenuItem_RemoveField.setEnabled(false);
                 }
                 if (jMenuItem_RenameField.isEnabled()) {
                     jMenuItem_RenameField.setEnabled(false);
                 }
             }
         });

         this.jMenuItem_AddField.setEnabled(false);
         this.jMenuItem_RemoveField.setEnabled(false);
         this.jMenuItem_RenameField.setEnabled(false);
         this.jMenuItem_StopEdit.setEnabled(false);
     }

     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {

         jScrollPane1 = new JScrollPane();
         jTable1 = new JTable();
         jMenuBar1 = new JMenuBar();
         jMenu_Edit = new JMenu();
         jMenuItem_StartEdit = new JMenuItem();
         jMenuItem_StopEdit = new JMenuItem();
         jSeparator1 = new JPopupMenu.Separator();
         jMenuItem_AddField = new JMenuItem();
         jMenuItem_RemoveField = new JMenuItem();
         jMenuItem_RenameField = new JMenuItem();

         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

         jTable1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         jScrollPane1.setViewportView(jTable1);

         jMenu_Edit.setText("Edit");

         jMenuItem_StartEdit.setText("Start Edit");
         jMenuItem_StartEdit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem_StartEditActionPerformed(evt);
             }
         });
         jMenu_Edit.add(jMenuItem_StartEdit);

         jMenuItem_StopEdit.setText("Stop Edit");
         jMenuItem_StopEdit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem_StopEditActionPerformed(evt);
             }
         });
         jMenu_Edit.add(jMenuItem_StopEdit);
         jMenu_Edit.add(jSeparator1);

         jMenuItem_AddField.setText("Add Field");
         jMenuItem_AddField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem_AddFieldActionPerformed(evt);
             }
         });
         jMenu_Edit.add(jMenuItem_AddField);

         jMenuItem_RemoveField.setText("Remove Field");
         jMenuItem_RemoveField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem_RemoveFieldActionPerformed(evt);
             }
         });
         jMenu_Edit.add(jMenuItem_RemoveField);

         jMenuItem_RenameField.setText("Rename Field");
         jMenuItem_RenameField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem_RenameFieldActionPerformed(evt);
             }
         });
         jMenu_Edit.add(jMenuItem_RenameField);

         jMenuBar1.add(jMenu_Edit);

         setJMenuBar(jMenuBar1);

         GroupLayout layout = new GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                 .addGap(0, 0, 0))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                 .addGap(0, 0, 0))
         );

         pack();
     }// </editor-fold>//GEN-END:initComponents

     private void jMenuItem_StartEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_StartEditActionPerformed
         // TODO add your handling code here:
         this.jMenuItem_StartEdit.setEnabled(false);
         this.jMenuItem_AddField.setEnabled(true);
         this.jMenuItem_StopEdit.setEnabled(true);
         if (this.jTable1.getSelectedColumnCount() > 0) {
             this.jMenuItem_RemoveField.setEnabled(true);
             this.jMenuItem_RenameField.setEnabled(true);
         }
         this._isEditing = true;

         DataTableModel dataTableModel = new DataTableModel(_dataTable) {
             @Override
             public boolean isCellEditable(int row, int column) {
                 return true;
             }
         };
         this.jTable1.setModel(dataTableModel);
     }//GEN-LAST:event_jMenuItem_StartEditActionPerformed

     private void jMenuItem_StopEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_StopEditActionPerformed
         // TODO add your handling code here:
         this.jMenuItem_StartEdit.setEnabled(true);
         this.jMenuItem_AddField.setEnabled(false);
         this.jMenuItem_RemoveField.setEnabled(false);
         this.jMenuItem_RenameField.setEnabled(false);
         this.jMenuItem_StopEdit.setEnabled(false);
         this._isEditing = false;

         int result = JOptionPane.showConfirmDialog(null, "If save the edition?", "Confirm", JOptionPane.YES_NO_OPTION);
         if (result == JOptionPane.YES_OPTION) {
             this.saveDataTable();
         } else {
             _dataTable = _layer.getAttributeTable().getTable().cloneTable_Field();
         }

         DataTableModel dataTableModel = new DataTableModel(_dataTable) {
             @Override
             public boolean isCellEditable(int row, int column) {
                 return false;
             }
         };
         this.jTable1.setModel(dataTableModel);
     }//GEN-LAST:event_jMenuItem_StopEditActionPerformed

     private void jMenuItem_AddFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_AddFieldActionPerformed
         // TODO add your handling code here:
         FrmAddField frmField = new FrmAddField(null, true);
         frmField.setLocationRelativeTo(this);
         frmField.setVisible(true);
         if (frmField.isOK()) {
             String fieldName = frmField.getFieldName();
             if (fieldName.isEmpty()) {
                 JOptionPane.showMessageDialog(null, "Field name is empty!");
                 return;
             }
             List<String> fieldNames = _layer.getFieldNames();
             if (fieldNames.contains(fieldName)) {
                 JOptionPane.showMessageDialog(null, "Field name has exist in the data table!");
                 return;
             }
             DataType dataType = frmField.getDataType();
             try {
                 _dataTable.addColumn(new Field(fieldName, dataType));
                 //this.jTable1.revalidate();
                 DataTableModel dataTableModel = new DataTableModel(_dataTable) {
                     @Override
                     public boolean isCellEditable(int row, int column) {
                         return true;
                     }
                 };
                 this.jTable1.setModel(dataTableModel);
             } catch (Exception ex) {
                 Logger.getLogger(FrmAttriData.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }//GEN-LAST:event_jMenuItem_AddFieldActionPerformed

     private void jMenuItem_RemoveFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_RemoveFieldActionPerformed
         // TODO add your handling code here:
         if (this.jTable1.getSelectedColumnCount() == 0) {
             JOptionPane.showMessageDialog(null, "Please select one field firstly!");
             return;
         }

         int fieldIdx = this.jTable1.getSelectedColumn();
         String fieldName = this.jTable1.getColumnName(fieldIdx);
         int result = JOptionPane.showConfirmDialog(null, "If remove the field: " + fieldName + "?", "Confirm",
                 JOptionPane.YES_NO_OPTION);
         if (result == JOptionPane.YES_OPTION) {
             _dataTable.removeColumn(_dataTable.getColumns().get(fieldName));
             DataTableModel dataTableModel = new DataTableModel(_dataTable) {
                 @Override
                 public boolean isCellEditable(int row, int column) {
                     return true;
                 }
             };
             this.jTable1.setModel(dataTableModel);
         }
     }//GEN-LAST:event_jMenuItem_RemoveFieldActionPerformed

     private void jMenuItem_RenameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_RenameFieldActionPerformed
         // TODO add your handling code here:
         if (this.jTable1.getSelectedColumnCount() == 0) {
             JOptionPane.showMessageDialog(null, "Please select one field firstly!");
             return;
         }

         int fieldIdx = this.jTable1.getSelectedColumn();
         String fieldName = this.jTable1.getColumnName(fieldIdx);
         String result = JOptionPane.showInputDialog(this, "Please input new field name:", fieldName);
         if (result != null) {
             if (result.isEmpty()){
                 JOptionPane.showMessageDialog(null, "The field name is empty!");
                 return;
             }
             List<String> fieldNames = _dataTable.getColumnNames();
             if (fieldNames.contains(result)){
                 JOptionPane.showMessageDialog(null, "The field name is exist!");
                 return;
             }
             _dataTable.renameColumn(_dataTable.getColumns().get(fieldName), result);
             DataTableModel dataTableModel = new DataTableModel(_dataTable) {
                 @Override
                 public boolean isCellEditable(int row, int column) {
                     return true;
                 }
             };
             this.jTable1.setModel(dataTableModel);
         }
     }//GEN-LAST:event_jMenuItem_RenameFieldActionPerformed

     /**
      * Set vector layer
      *
      * @param aLayer The vector layer
      */
     public void setLayer(VectorLayer aLayer) {
         _layer = aLayer;
         //_dataTable = _layer.getAttributeTable().getTable().cloneTable_Field();
         _dataTable = _layer.getAttributeTable().getTable();
         this.setTitle("Attribute Data - " + _layer.getLayerName());
         DataTableModel dataTableModel = new DataTableModel(_dataTable);
         this.jTable1.setModel(dataTableModel);
         this.jScrollPane1.setRowHeaderView(new RowHeaderTable(this.jTable1, 40));
     }

     private void saveDataTable() {
 //        for (int i = 0; i < _layer.getFieldNumber(); i++){
 //            for (int j = 0; j < _layer.getShapeNum(); j++){
 //                _layer.editCellValue(i, j, this.jTable1.getModel().getValueAt(j, i));
 //            }
 //        }

         _layer.getAttributeTable().setTable(_dataTable.cloneTable_Field());
         if (new File(_layer.getFileName()).exists())
             _layer.getAttributeTable().save();
         else
             _layer.saveFile();
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
             for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             Logger.getLogger(FrmAttriData.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             Logger.getLogger(FrmAttriData.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             Logger.getLogger(FrmAttriData.class.getName()).log(Level.SEVERE, null, ex);
         } catch (UnsupportedLookAndFeelException ex) {
             Logger.getLogger(FrmAttriData.class.getName()).log(Level.SEVERE, null, ex);
         }
         //</editor-fold>

         /* Create and display the dialog */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 FrmAttriData dialog = new FrmAttriData();
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
     private JMenuBar jMenuBar1;
     private JMenuItem jMenuItem_AddField;
     private JMenuItem jMenuItem_RemoveField;
     private JMenuItem jMenuItem_RenameField;
     private JMenuItem jMenuItem_StartEdit;
     private JMenuItem jMenuItem_StopEdit;
     private JMenu jMenu_Edit;
     private JScrollPane jScrollPane1;
     private JPopupMenu.Separator jSeparator1;
     private JTable jTable1;
     // End of variables declaration//GEN-END:variables
 }
