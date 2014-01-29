/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.mupcity.evaluation;

import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.mupcity.scenario.Scenario;
import org.thema.msca.Cell;

/**
 *
 * @author gvuidel
 */
public class NbCellEvaluator  extends Evaluator {


    public NbCellEvaluator() {
        super(new DiscreteFunction(new double[] {0, 1}, new double[] {0, 1}));
    }

    @Override
    protected double eval(Scenario scenario, Cell cell) {
        return 1;
    }

    @Override
    public String getShortName() {
        return "NB";
    }

}