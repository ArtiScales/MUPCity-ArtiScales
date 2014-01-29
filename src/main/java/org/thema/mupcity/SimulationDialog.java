/*
 * SimulationDialog.java
 *
 * Created on 8 juin 2007, 12:15
 */

package org.thema.mupcity;

import org.thema.mupcity.scenario.ScenarioAuto;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

/**
 *
 * @author  Gilles Vuidel
 */
public class SimulationDialog extends javax.swing.JDialog {

    public boolean returnOk = false;
    public ScenarioAuto analyse;
    
    /**
     * Creates new form SimulationDialog
     */
    public SimulationDialog(java.awt.Frame parent, Project project) {
        super(parent, true);
        initComponents();
        setLocationRelativeTo(parent);

        if(!project.isDecomp()) {
            JOptionPane.showMessageDialog(this, "No decomposition !");
            setVisible(false);
        }
        DefaultComboBoxModel lstStart = new DefaultComboBoxModel();
        DefaultComboBoxModel lstEnd = new DefaultComboBoxModel();
        
        for(Double res : project.getResolutions()) {
            lstStart.addElement(res);
            lstEnd.addElement(res);
        }
        startComboBox.setModel(lstStart);
        endComboBox.setModel(lstEnd);

        noBuildCheckBox.setEnabled(project.hasNoBuild());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        startComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        endComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        nMaxTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        strictCheckBox = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        noBuildCheckBox = new javax.swing.JCheckBox();
        ruleSelectionPanel = new org.thema.mupcity.rule.RuleSelectionPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle"); // NOI18N
        setTitle(bundle.getString("SimulationDialog.title")); // NOI18N

        startComboBox.setEnabled(false);

        jLabel1.setText(bundle.getString("SimulationDialog.jLabel1.text")); // NOI18N
        jLabel1.setEnabled(false);

        endComboBox.setEnabled(false);

        jLabel2.setText(bundle.getString("SimulationDialog.jLabel2.text")); // NOI18N
        jLabel2.setEnabled(false);

        okButton.setText(bundle.getString("SimulationDialog.okButton.text")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(bundle.getString("SimulationDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        nMaxTextField.setText(bundle.getString("SimulationDialog.nMaxTextField.text")); // NOI18N

        jLabel3.setText(bundle.getString("SimulationDialog.jLabel3.text")); // NOI18N

        strictCheckBox.setText(bundle.getString("SimulationDialog.strictCheckBox.text")); // NOI18N

        jLabel4.setText(bundle.getString("SimulationDialog.jLabel4.text")); // NOI18N

        nameTextField.setText(bundle.getString("SimulationDialog.nameTextField.text")); // NOI18N

        noBuildCheckBox.setText(bundle.getString("SimulationDialog.noBuildCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameTextField))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ruleSelectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nMaxTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(strictCheckBox))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(endComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(startComboBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(noBuildCheckBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(endComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(nMaxTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(strictCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noBuildCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ruleSelectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed

        int start = startComboBox.getSelectedIndex();
        int end = endComboBox.getSelectedIndex();
        int nMax = Integer.parseInt(nMaxTextField.getText());
        boolean strict = strictCheckBox.isSelected();
        String simName = nameTextField.getText();

        analyse = ScenarioAuto.createMultiScaleAnalysis(simName, start, end, 
                nMax, strict, ruleSelectionPanel.getAHP(), noBuildCheckBox.isSelected(), ruleSelectionPanel.isAgregMean());
        returnOk = true;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox endComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField nMaxTextField;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JCheckBox noBuildCheckBox;
    private javax.swing.JButton okButton;
    private org.thema.mupcity.rule.RuleSelectionPanel ruleSelectionPanel;
    private javax.swing.JComboBox startComboBox;
    private javax.swing.JCheckBox strictCheckBox;
    // End of variables declaration//GEN-END:variables
    
}

