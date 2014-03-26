/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.mupcity.evaluation;

import java.awt.image.DataBuffer;
import org.thema.common.fuzzy.MembershipFunction;
import org.thema.mupcity.scenario.Scenario;
import org.thema.msca.Cell;
import org.thema.common.parallel.TaskMonitor;
import org.thema.msca.SquareGrid;
import org.thema.msca.operation.AbstractLayerOperation;

/**
 *
 * @author gvuidel
 */
public abstract class Evaluator {

    public static final String BATI_RESID = "bati_resid";
    
    // fonction pour remettre l'évaluation entre 0 et 1
    protected MembershipFunction function;
    
   
//    protected Double[] results;

    
    public Evaluator(MembershipFunction function) {
        this.function = function;
    }

    protected final boolean isEvaluated(Cell cell, Scenario anal) {
        return cell.getDistBorder() >= 4 && 
                (cell.getLayerValue(anal.getResultLayerName()) == 2 || cell.getLayerValue(BATI_RESID) == 1);
    }

    protected final boolean isNewBuild(Cell cell, Scenario anal) {
        return cell.getDistBorder() >= 4 &&
                cell.getLayerValue(anal.getResultLayerName()) == 2;
    }

    /**
     * 
     * @param scenario
     * @param grid grille à la résolution la plus fine
     * @param monitor 
     */
    public void execute(final Scenario scenario, SquareGrid grid, TaskMonitor monitor) {

        // créé une nouvelle couche pour stocker l'évaluation
        grid.addLayer(getEvalLayerName(scenario), DataBuffer.TYPE_FLOAT, Float.NaN);
        // lance le calcul d'évaluation sur chaque cellule
        //executethreaded permet de paralleliser
        grid.executeThreaded(new AbstractLayerOperation(4) {
            @Override
            public void perform(Cell cell) {
                double eval;
                if(isEvaluated(cell, scenario)) {
                    eval = eval(scenario, cell);
                    eval = function.getValue(eval);
                } else
                    eval = Double.NaN;
                cell.setLayerValue(getEvalLayerName(scenario), eval);
            }
        });
        
    }
    
    public String getEvalLayerName(Scenario scenario) {
        return scenario.getName() + "_" + getShortName();
    }
    
    public boolean isUsable() {
        return true;
    }

    protected abstract double eval(Scenario scenario, Cell cell);
    
//    public abstract Double[] eval(Scenario anal, double mean);
//
//    public Double[] getResult() {
//        return results;
//    }
    
    public abstract String getShortName();
    
    @Override
    public String toString() {
        return getFullName();
    }
    
    public String getFullName() {
        return java.util.ResourceBundle.getBundle("org/thema/mupcity/evaluation/Bundle").getString(getShortName());
    }
}
