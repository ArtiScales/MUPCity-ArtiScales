
package org.thema.mupcity.evaluation;

import java.util.List;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.mupcity.scenario.Scenario;
import org.thema.msca.Cell;

/**
 * Evaluates the number of empty cells in the neighborhood.
 * 
 * @author Gilles Vuidel
 */
public class MeanWhiteEvaluator extends Evaluator {

    /**
     * Creates a new MeanWhiteEvaluator with default parameters
     */
    public MeanWhiteEvaluator() {
        super(new DiscreteFunction(new double[] {0.0, 8.0}, new double[] {0.001, 1.0}));
    }

    @Override
    protected double eval(Scenario scenario, Cell cell) {
        List<Cell> cells = cell.getNeighbors();
        int nb = 0;
        for(Cell c : cells) {
            if(c.getLayerValue(scenario.getResultLayerName()) == 0) {
                nb++;
            }
        }
        return nb;
    }

    @Override
    public String getShortName() {
        return "MeanWhite";
    }

}
