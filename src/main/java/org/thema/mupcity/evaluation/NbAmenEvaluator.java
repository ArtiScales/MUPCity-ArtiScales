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

import org.thema.msca.Cell;
import org.thema.mupcity.Project;
import org.thema.mupcity.scenario.Scenario;

/**
 * Evaluates the number of amenities inside a given distance 
 * 
 * @author Gilles Vuidel
 */
public class NbAmenEvaluator extends AbstractAmenEvaluator {
    
    private double distMax;

    /**
     * Creates a new NbAmenEvaluator.
     * @param project the current project
     * @param layer the layer of amenities
     * @param level the level of amenity or -1 for all levels
     * @param distMax the maximum distance
     * @param x the abscisses of the membership function
     * @param y the ordinates of the membership function
     */
    public NbAmenEvaluator(Project project, Project.Layers layer, int level, double distMax, double[] x, double[] y ) {
        super(project, layer, level, x, y);
        this.distMax = distMax;
    }

    @Override
    protected double eval(Scenario scenario, Cell cell) {
        return getDistAmen().getNbAmen(cell, distMax);
    }

    @Override
    public String getShortName() {
        return "NB_" + getLayer().toString() + "-" + getLevel();
    }

}
