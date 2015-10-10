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
