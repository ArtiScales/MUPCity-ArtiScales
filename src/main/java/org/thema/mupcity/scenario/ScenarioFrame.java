/*
 * Copyright (C) 2015 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 * http://thema.univ-fcomte.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.thema.mupcity.scenario;

import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.thema.drawshape.PanelMap.ShapeSelectionEvent;
import org.thema.drawshape.PanelMap.ShapeSelectionListener;
import org.thema.drawshape.SelectableShape;
import org.thema.drawshape.feature.FeatureShape;
import org.thema.drawshape.layer.DefaultGroupLayer;
import org.thema.drawshape.layer.GroupLayer;
import org.thema.drawshape.layer.Layer;
import org.thema.drawshape.ui.MapViewer;
import org.thema.msca.Cell;
import org.thema.msca.MSFeature;
import org.thema.msca.MSGridBuilder;
import org.thema.msca.SquareGrid;
import org.thema.msca.operation.AbstractAgregateOperation;
import org.thema.mupcity.Project;

/**
 * Frame for manual scenario editing.
 * 
 * @author  Gilles Vuidel
 */
public class ScenarioFrame extends javax.swing.JInternalFrame implements ShapeSelectionListener {
    
    private DefaultGroupLayer layers;
    private ScenarioManual scenario;

    private Project project;
    private MSGridBuilder<? extends SquareGrid> msGrid;
    
    private List<MSFeature> selFeatures;

    /**
     * Creates new form ScenarioFrame 
     * @param scenario the scenario to view/edit
     * @param layers the layers to add in the mapframe
     * @param project the current project
     */
    public ScenarioFrame(ScenarioManual scenario, DefaultGroupLayer layers, Project project) {
        initComponents();
        setTitle(Project.NODE_SCENARIO + " : " + scenario.getName());
        this.project = project;
        this.msGrid = project.getMSGrid();
        this.layers = layers;
        this.scenario = scenario;
        mapViewer.setRootLayer(layers);
        mapViewer.putAddLayerButton();
        mapViewer.putExportButton();
        mapViewer.getMap().setMultipleSelection(true);
        mapViewer.getMap().addShapeSelectionListener(this);
        pack();
    }
    
    /**
     * @return the map viewer of this frame
     */
    public MapViewer getMapViewer() {
        return mapViewer;
    }

    @Override
    public void selectionChanged(ShapeSelectionEvent event) {
        List<? extends SelectableShape> shapes = mapViewer.getMap().getSelection();
        List<MSFeature> features = new ArrayList<>();
        for(SelectableShape s : shapes) {
            if(s instanceof FeatureShape &&
                    ((FeatureShape)s).getFeature() instanceof MSFeature) {
                features.add((MSFeature)((FeatureShape)s).getFeature());
            }
        }

        selFeatures = features;

        buildButton.setEnabled(selFeatures.size() > 0);
        unBuildButton.setEnabled(selFeatures.size() > 0);
        if(!selFeatures.isEmpty()) {
            double eval = ((Number)selFeatures.get(0).getAttribute(scenario.getEvalLayerName())).doubleValue();
            infoLabel.setText(String.format("Eval : %g", eval));
        }

    }

    private boolean canBeBuild(MSFeature f) {
        int build = ((Number)f.getAttribute(scenario.getResultLayerName())).intValue();

        if(build == ScenarioAuto.BUILD || build == ScenarioAuto.REM_BUILD
                || build == ScenarioAuto.NEW_BUILD) {
            return true;
        }

        // finalement build == 0
        MSFeature parent = f.getParent();
        int nb = 0;
        if(parent != null) {
            if(!scenario.isBlack(parent)) {
                JOptionPane.showMessageDialog(null, "La cellule à l'échelle supérieure n'est pas batie.");
                return false;
            }

            for(MSFeature c : parent.getChildren()) {
                if(scenario.isBlack(c)) {
                    nb++;
                }
            }
        }

        if(nb >= scenario.getNMax()) {
            JOptionPane.showMessageDialog(null, "Le nombre de cellules baties dans la maille est déjà au maximum (" + nb + ").");
            return false;
        }

        double res = 0;
        for(Layer l : scenario.getLayers(project.getMSGrid()).getLayers()) {
            if(l.isVisible()) {
                String name = l.getName();
                if(name.contains(",")) {
                    name = name.replace(',', '.');
                }
                res = Double.parseDouble(name);
            }
        }
        
        Raster r = msGrid.getGrid(res).getRaster(scenario.getResultLayerName());

        try {
            int nbGap = getNbClusterGap(r);

            scenario.setBlack(f);
            nbGap = getNbClusterGap(r) - nbGap;
            scenario.setWhite(f);

            if(nbGap > 0) {
                JOptionPane.showMessageDialog(null, "La cellule bloque une coulée verte.");
                return false;
            }
        } catch(Exception e) {
            Logger.getLogger(ScenarioFrame.class.getName()).log(Level.WARNING, "", e);
        }

        return true;

    }

    private int getNbClusterGap(Raster r) {
        WritableRaster clust = r.createCompatibleWritableRaster(r.getWidth()+2, r.getHeight()+1);
        int k = 0;
        TreeSet<Integer> set = new TreeSet<>();
        ArrayList<Integer> idClust = new ArrayList<>();

        for(int j = 1; j <= r.getHeight(); j++) {
            for(int i = 1; i <= r.getWidth(); i++) {
                if(r.getSample(i-1, j-1, 0) == 0) {
                    set.add(clust.getSample(i-1, j, 0));
                    set.add(clust.getSample(i, j-1, 0));
                    set.add(clust.getSample(i-1, j-1, 0));
                    set.add(clust.getSample(i+1, j-1, 0));
                    set.remove(0);

                    if(set.isEmpty()) {
                        k++;
                        clust.setSample(i, j, 0, k);
                        idClust.add(k);
                    } else if(set.size() == 1) {
                        int id = set.iterator().next();
                        clust.setSample(i, j, 0, id);
                    } else {
                        int minId = Integer.MAX_VALUE;
                        for(Integer id : set) {
                            if(idClust.get(id-1) < minId) {
                                minId = idClust.get(id-1);
                            }
                        }

                        for(Integer id : set) {
                            while(idClust.get(id-1) != minId) {
                                int newId = idClust.get(id-1);
                                idClust.set(id-1, minId);
                                id = newId;
                            }
                        }

                        clust.setSample(i, j, 0, minId);

                    }
                    set.clear();
                }
            }
        }

        for(int i = 0; i < idClust.size(); i++) {
            int m = i+1;
            while(idClust.get(m-1) != m) {
                m = idClust.get(m-1);
            }
            idClust.set(i, m);
        }


        set.clear();
        for(Integer id : idClust) {
            set.add(id);
        }
        return set.size();

    }

    private boolean canBeUnBuild(MSFeature f) {
        return true;
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mapViewer = new org.thema.drawshape.ui.MapViewer();
        toolBar = new javax.swing.JToolBar();
        editToggleButton = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        downButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        infoLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jLabel1 = new javax.swing.JLabel();
        buildButton = new javax.swing.JButton();
        unBuildButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        nbNewBuildLabel = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        getContentPane().add(mapViewer, java.awt.BorderLayout.CENTER);

        toolBar.setOrientation(javax.swing.SwingConstants.VERTICAL);
        toolBar.setRollover(true);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/mupcity/scenario/Bundle"); // NOI18N
        editToggleButton.setText(bundle.getString("ScenarioFrame.editToggleButton.text")); // NOI18N
        editToggleButton.setToolTipText(bundle.getString("ScenarioFrame.editToggleButton.toolTipText")); // NOI18N
        editToggleButton.setFocusable(false);
        editToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editToggleButtonActionPerformed(evt);
            }
        });
        toolBar.add(editToggleButton);

        downButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/thema/mupcity/image/down.png"))); // NOI18N
        downButton.setToolTipText(bundle.getString("ScenarioFrame.downButton.toolTipText")); // NOI18N
        downButton.setFocusable(false);
        downButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        downButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });
        jPanel1.add(downButton);

        upButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/thema/mupcity/image/up.png"))); // NOI18N
        upButton.setToolTipText(bundle.getString("ScenarioFrame.upButton.toolTipText")); // NOI18N
        upButton.setFocusable(false);
        upButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        upButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });
        jPanel1.add(upButton);

        toolBar.add(jPanel1);

        infoLabel.setText(bundle.getString("ScenarioFrame.infoLabel.text")); // NOI18N
        toolBar.add(infoLabel);
        toolBar.add(jSeparator1);

        jLabel1.setText(bundle.getString("ScenarioFrame.jLabel1.text")); // NOI18N
        toolBar.add(jLabel1);

        buildButton.setText(bundle.getString("ScenarioFrame.buildButton.text")); // NOI18N
        buildButton.setFocusable(false);
        buildButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buildButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buildButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildButtonActionPerformed(evt);
            }
        });
        toolBar.add(buildButton);

        unBuildButton.setText(bundle.getString("ScenarioFrame.unBuildButton.text")); // NOI18N
        unBuildButton.setFocusable(false);
        unBuildButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        unBuildButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        unBuildButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unBuildButtonActionPerformed(evt);
            }
        });
        toolBar.add(unBuildButton);
        toolBar.add(jSeparator2);

        nbNewBuildLabel.setText(bundle.getString("ScenarioFrame.nbNewBuildLabel.text")); // NOI18N
        toolBar.add(nbNewBuildLabel);

        getContentPane().add(toolBar, java.awt.BorderLayout.LINE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void editToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editToggleButtonActionPerformed
        if(!editToggleButton.isSelected()) {
            int res = JOptionPane.showConfirmDialog(this, "Voulez-vous enregistrer les modifications ?",
                    "Suppression...", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(res == JOptionPane.YES_OPTION) {
                save();
            } else {
                try {
                    undo();
                } catch(Exception ex) {
                    Logger.getLogger(ScenarioFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }    
        }

    }//GEN-LAST:event_editToggleButtonActionPerformed

    private void undo() {
        try {
            project.reloadGridLayer(scenario.getResultLayerName());
            refresh();
        } catch (IOException ex) {
            Logger.getLogger(ScenarioFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Une erreur est survenue pendant le chargement de la version précédente :\n" + ex.getLocalizedMessage());
        }
    }
    
    private void save() {
        try {
            project.saveGridLayer(scenario.getResultLayerName());
        } catch (IOException ex) {
            Logger.getLogger(ScenarioFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Une erreur est survenue pendant l'enregistrement :\n" + ex.getLocalizedMessage());
        }
    }

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        mapViewer.getMap().clearSelection();
        List<Layer> sceLayers = scenario.getLayers(project.getMSGrid()).getLayers();
        List<Layer> gridLayers = ((GroupLayer)layers.getLayer(java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Grid"))).getLayers();
        int n = gridLayers.size();
        for(int i = 1; i < sceLayers.size(); i++) {
             if(sceLayers.get(i).isVisible()) {
                 sceLayers.get(i).setVisible(false);
                 sceLayers.get(i-1).setVisible(true);
                 gridLayers.get(n-i-1).setVisible(false);
                 updateNbNewBuild();
                 return ;
             }
        }
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        mapViewer.getMap().clearSelection();
        List<Layer> sceLayers = scenario.getLayers(project.getMSGrid()).getLayers();
        List<Layer> gridLayers = ((GroupLayer)layers.getLayer(java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Grid"))).getLayers();
        int n = gridLayers.size();
        for(int i = 0; i < sceLayers.size()-1; i++) {
             if(sceLayers.get(i).isVisible()) {
                 sceLayers.get(i).setVisible(false);
                 sceLayers.get(i+1).setVisible(true);
                 gridLayers.get(n-i-1).setVisible(true);
                 updateNbNewBuild();
                 return ;
             }
        }
    }//GEN-LAST:event_downButtonActionPerformed

    private void buildButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildButtonActionPerformed
        if(!editToggleButton.isSelected()) {
            JOptionPane.showMessageDialog(this, "Not in edit mode");
            return;
        }
        for(MSFeature f : selFeatures) {
            if(canBeBuild(f)) {
                if(scenario.isWhite(f)) {
                    scenario.setBlack(f);
                }
            } else {
                break;
            }
        }
        refresh();
    }//GEN-LAST:event_buildButtonActionPerformed

    private void unBuildButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unBuildButtonActionPerformed
        if(!editToggleButton.isSelected()) {
            JOptionPane.showMessageDialog(this, "Not in edit mode");
            return;
        }
        for(MSFeature f : selFeatures) {
            if(canBeUnBuild(f)) {
                if(scenario.isBlack(f)) {
                    scenario.setWhite(f);
                }
            } else {
                break;
            }
        }

        refresh();
    }//GEN-LAST:event_unBuildButtonActionPerformed


    private void updateNbNewBuild() {
        double res = 0;
        for(Layer l : scenario.getLayers(project.getMSGrid()).getLayers()) {
            if(l.isVisible()) {
                res = Double.parseDouble(l.getName().replace(",", "."));
            }
        }
        int nb = msGrid.getGrid(res).agregate(new AbstractAgregateOperation<Integer>(0, 0) {
            @Override
            public void perform(Cell cell) {
                if(cell.getLayerValue(scenario.getResultLayerName()) == ScenarioAuto.NEW_BUILD) {
                    result++;
                }
            }
        });
        nbNewBuildLabel.setText("Nb build : "+nb);
    }

    private void refresh() {
        mapViewer.getMap().fullRepaint();

        updateNbNewBuild();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buildButton;
    private javax.swing.JButton downButton;
    private javax.swing.JToggleButton editToggleButton;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private org.thema.drawshape.ui.MapViewer mapViewer;
    private javax.swing.JLabel nbNewBuildLabel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JButton unBuildButton;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    
}
