
package org.thema.mupcity.evaluation;

import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.mupcity.scenario.Scenario;
import org.thema.msca.Cell;

/**
 * Evaluates the immediate proximity of empty cells.
 * 
 * @author Gilles Vuidel
 */
public class NbNearWhiteEvaluator extends Evaluator{

    /**
     * Creates a new NbNearWhiteEvaluator with default parameters
     */
    public NbNearWhiteEvaluator() {
        super(new DiscreteFunction(new double[] {0.0, 1.0}, new double[] {0.001, 1.0}));
    }

    @Override
    protected double eval(Scenario scenario, Cell cell) {
        for(Cell c : cell.getNeighbors()) {
            if(c.getLayerValue(scenario.getResultLayerName()) == 0) {
                return 1;
            }
        }
       
        return 0;
    }

    @Override
    public String getShortName() {
        return "NearWhite";
    }
    
}
