/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.mupcity;

import java.util.*;
import javax.swing.table.AbstractTableModel;
import org.thema.common.HashMap2D;
import org.thema.common.param.ParamEditor;

/**
 *
 * @author gvuidel
 */
public class AHPEditor extends ParamEditor<AHP> {

    private class SimpleTableModel extends AbstractTableModel {
        List list1, list2;
        Object[][] values;
        public SimpleTableModel(HashMap2D param) {
            list1 = new ArrayList(param.getKeys1());
            list2 = new ArrayList(param.getKeys2());
            values = new Object[list1.size()][list2.size()];
            for(int i = 0; i < list1.size(); i++)
                for(int j = 0; j < list2.size(); j++)
                    values[i][j] = param.getValue(list1.get(i), list2.get(j));
        }
        
        public int getRowCount() {
            return list1.size();
        }

        public int getColumnCount() {
            return list2.size()+1;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if(columnIndex == 0)
                return list1.get(rowIndex);
            else 
                return values[rowIndex][columnIndex-1];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public String getColumnName(int column) {
            if(column == 0)
                return "";
            else
                return list2.get(column-1).toString();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            
            if(columnIndex == 0)
                return false;
            
            return rowIndex != columnIndex-1;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            values[rowIndex][columnIndex-1] = value;
            String inv = "1/" + value;
            if(value.toString().startsWith("1/"))
                inv = value.toString().substring(2);
            
            if(!values[columnIndex-1][rowIndex].equals(inv)) {
                values[columnIndex-1][rowIndex] = inv;
                fireTableCellUpdated(columnIndex-1, rowIndex+1);
            }
        }
        
        public HashMap2D getMap2D() {
            HashMap2D map = new HashMap2D(list1, list2);
            for(int i = 0; i < list1.size(); i++)
                for(int j = 0; j < list2.size(); j++)
                    map.setValue(list1.get(i), list2.get(j), values[i][j]);
            return map;
        }
        
    }
    
    SimpleTableModel model;

    
    /**
     * Creates new form CollectionEditor
     */
    public AHPEditor() {
        super(null);
        initComponents();
    }
    public AHPEditor(AHP ahp) {
        super(ahp);
        initComponents();
        
        model = new SimpleTableModel(ahp.getMatrix());
        table.setModel(model);    
    }
    
    @Override
    public void setParam(AHP param) {
        super.setParam(param);
        model = new SimpleTableModel(param.getMatrix());
        table.setModel(model);
    }

    @Override
    public void validateParam() {
        if(table.isEditing())
            table.getCellEditor().stopCellEditing();
        param.setMatrix(model.getMap2D());
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        table.setModel(new javax.swing.table.DefaultTableModel(
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
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(table);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables

}
