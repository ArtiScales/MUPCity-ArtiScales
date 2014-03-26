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
public class NbNearWhiteEvaluator extends Evaluator{


    public NbNearWhiteEvaluator() {
        super(new DiscreteFunction(new double[] {0.0, 1.0}, new double[] {0.001, 1.0}));
    }

    @Override
    protected double eval(Scenario scenario, Cell cell) {
        List<Cell> cells = cell.getNeighbors();
        int nb = 0;
        for(Cell c : cells)
            if(c.getLayerValue(scenario.getResultLayerName()) == 0)
                nb++;
        if(nb > 0)
            return 1;
        else
            return 0;
    }

    @Override
    public String getShortName() {
        return "NearWhite";
    }
    


//    @Override
//    public Double[] eval(final Scenario anal, final double mean) {
//        int nb = grid.agregate(new AbstractAgregateOperation<Integer>(4, 0) {
//
//            public void perform(Cell cell) {
//                if(!isEvaluated(cell, anal))
//                    return;
//                List<Cell> cells = cell.getNeighbors();
//                int nb = 0;
//                for(Cell c : cells)
//                    if(c.getLayerValue(anal.getResultLayerName()) == 0)
//                        nb++;
//                if(nb > 0)
//                    result++;
//
//            }
//
//        });
//
//        return new Double[] {(double)nb, (double)nb, (double)nb};
//    }

}
