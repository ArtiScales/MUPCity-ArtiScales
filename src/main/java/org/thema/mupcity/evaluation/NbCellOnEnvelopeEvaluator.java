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

import com.vividsolutions.jts.geom.Geometry;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureCoverage;
import org.thema.msca.Cell;
import org.thema.msca.SquareGrid;
import org.thema.msca.operation.SimpleCoverageOperation;
import org.thema.mupcity.Project;
import org.thema.mupcity.scenario.Scenario;

/**
 * Evaluates the number of cell touching the urban border.
 * 
 * @author Gilles Vuidel
 */
public class NbCellOnEnvelopeEvaluator extends Evaluator {
    
    private transient FeatureCoverage urbanBorder;
    
    /**
     * Creates a new NbCellOnEnvelopeEvaluator with default parameters.
     * Before calculating evaluation, the urban border must be set.
     */
    public NbCellOnEnvelopeEvaluator() {
        super(new DiscreteFunction(new double[] {0.0, 1.0}, new double[] {0.001, 1}));
    }

    @Override
    public void execute(Scenario scenario, SquareGrid grid, TaskMonitor monitor) {
        // créé une nouvelle couche pour stocker l'évaluation
        grid.addLayer(getEvalLayerName(scenario), DataBuffer.TYPE_FLOAT, Float.NaN);
        grid.executeThreaded(new SimpleCoverageOperation(SimpleCoverageOperation.ISEMPTY, 
                getEvalLayerName(scenario), urbanBorder));
    }

    /**
     * Sets the urban border.
     * Must be set before evaluation calculation
     * @param urbanBorderGeom a MultiLinestring representing the urban border
     */
    public void setUrbanBorder(Geometry urbanBorderGeom) {
        List<Feature> features = new ArrayList<>();
        for(int i = 0; i < urbanBorderGeom.getNumGeometries(); i++) {
            features.add(new DefaultFeature(i, urbanBorderGeom.getGeometryN(i)));
        }
        this.urbanBorder = new DefaultFeatureCoverage(features);
    }
    
    /**
     * @throws UnsupportedOperationException
     * @param scenario
     * @param cell
     * @return nothing
     */
    @Override
    protected double eval(Scenario scenario, Cell cell) { // does nothing
        throw new UnsupportedOperationException();
    }

    @Override
    public String getShortName() {
        return "NbCellEnv";
    }

}
