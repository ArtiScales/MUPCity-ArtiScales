
package org.thema.mupcity.evaluation;

import java.awt.image.DataBuffer;
import org.thema.common.fuzzy.MembershipFunction;
import org.thema.mupcity.scenario.Scenario;
import org.thema.msca.Cell;
import org.thema.common.swing.TaskMonitor;
import org.thema.msca.SquareGrid;
import org.thema.msca.operation.AbstractLayerOperation;

/**
 * Base class for evaluator.
 * 
 * @author Gilles Vuidel, Florian Litot
 */
public abstract class Evaluator {

    public static final String BATI_RESID = "bati_resid";
    
    /** fonction pour remettre l'évaluation entre 0 et 1 */
    protected MembershipFunction function;
    
    /**
     * Creates a new evaluator
     * @param function the membership function to transform evaluation in the range [0-1]
     */
    public Evaluator(MembershipFunction function) {
        this.function = function;
    }

    /**
     * The cell is evaluated if the cell is built and is not on the border
     * @param cell the cell
     * @param anal the scenario
     * @return true if the cell must be evaluated, false otherwise
     */
    protected final boolean isEvaluated(Cell cell, Scenario anal) {
        return cell.getDistBorder() >= 4 && 
                (cell.getLayerValue(anal.getResultLayerName()) == 2 || cell.getLayerValue(BATI_RESID) == 1);
    }

    /**
     * 
     * @param cell the cell
     * @param anal the scenario
     * @return true if the cell is built for the scenario anal and is not on the border
     */
    protected final boolean isNewBuild(Cell cell, Scenario anal) {
        return cell.getDistBorder() >= 4 &&
                cell.getLayerValue(anal.getResultLayerName()) == 2;
    }

    /**
     * Creates a grid layer and calculates the evaluation for each cells.
     * The execution is parallelized
     * @param scenario the scenario
     * @param grid the grid at a given scale (generally the finest)
     * @param monitor UI monitor
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
                } else {
                    eval = Double.NaN;
                }
                cell.setLayerValue(getEvalLayerName(scenario), eval);
            }
        });
        
    }
    
    /**
     * @param scenario the scenario
     * @return the name of the grid layer evaluation for the scenario
     */
    public String getEvalLayerName(Scenario scenario) {
        return scenario.getName() + "_" + getShortName();
    }
    
    /**
     * By default return always true
     * @return true if this evaluation can be computed
     */
    public boolean isUsable() {
        return true;
    }

    /**
     * Calculates the evaluation of one cell for the given scenario
     * @param scenario the scenario
     * @param cell the cell
     * @return the evaluation
     */
    protected abstract double eval(Scenario scenario, Cell cell);
    
    /**
     * @return the short name of this evaluator
     */
    public abstract String getShortName();
    
    /**
     * @return the full name of this evaluator
     */
    @Override
    public String toString() {
        return getFullName();
    }
    
    /**
     * The full name can be locale dependant.
     * The name is stored in org/thema/mupcity/evaluation/Bundle properties file
     * @return the full name of this evaluator
     */
    public String getFullName() {
        return java.util.ResourceBundle.getBundle("org/thema/mupcity/evaluation/Bundle").getString(getShortName());
    }
}
