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
