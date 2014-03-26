/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.mupcity.evaluation;

import java.util.List;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.mupcity.scenario.Scenario;
import org.thema.msca.Cell;

/**
 *
 * @author gvuidel
 */
public class MeanWhiteEvaluator extends Evaluator {


    public MeanWhiteEvaluator() {
        super(new DiscreteFunction(new double[] {0.0, 8.0}, new double[] {0.001, 1.0}));

    }

    @Override
    protected double eval(Scenario scenario, Cell cell) {
        List<Cell> cells = cell.getNeighbors();
        int nb = 0;
        for(Cell c : cells)
            if(c.getLayerValue(scenario.getResultLayerName()) == 0)
                nb++;
        return nb;
    }

    @Override
    public String getShortName() {
        return "MeanWhite";
    }


//    @Override
//    public Double[] eval(final Scenario anal, final double mean) {
//        return grid.agregate(new AbstractAgregateOperation<Double[]>(4) {
//            int n = 0, nbInf = 0;
//            double sum = 0;
//            public void perform(Cell cell) {
//                if(!isEvaluated(cell, anal))
//                    return;
//                List<Cell> cells = cell.getNeighbors();
//                int nb = 0;
//                for(Cell c : cells)
//                    if(c.getLayerValue(anal.getResultLayerName()) == 0)
//                        nb++;
//                sum += nb;
//                n++;
//                if(nb < mean && isNewBuild(cell, anal))
//                    nbInf++;
//            }
//
//            @Override
//            public Double[] getResult() {
//                return new Double[] {sum / n, (double)n, (double)nbInf};
//            }
//
//        });
//    }

}
