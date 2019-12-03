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

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.graph.SpatialGraph;
import org.thema.msca.Cell;
import org.thema.msca.SquareGrid;
import org.thema.mupcity.scenario.Scenario;

/**
 * Evaluates the network distance to the urban border.
 * 
 * @author Gilles Vuidel
 */
public class DistEnvelopeEvaluator extends Evaluator {

    private transient Geometry urbanBorder;
    private transient SpatialGraph graph;
    private transient Geometry netGeom;
    private transient DistAmenities distAmenities;

    /**
     * Creates a new DistEnvelopeEvaluator with default parameters.
     * Before calculating evaluation, the network graph and geometry and the urban border must be set.
     */
    public DistEnvelopeEvaluator() {
        super(new DiscreteFunction(new double[] {0.0, 200}, new double[] {1.0, 0.001}));
    }

    @Override
    public void execute(Scenario scenario, SquareGrid grid, TaskMonitor monitor) {
        Geometry points = netGeom.intersection(urbanBorder);
        ArrayList<Feature> dest = new ArrayList<>();
        for(int i = 0; i < points.getNumGeometries(); i++) {
            dest.add(new DefaultFeature("Env_"+i, points.getGeometryN(i), null, null));
        }
        DefaultFeatureCoverage destCov = new DefaultFeatureCoverage(dest);
        distAmenities = new DistAmenities(destCov, graph);
        
        super.execute(scenario, grid, monitor);
    }
    
    @Override
    protected double eval(Scenario scenario, Cell cell) {        
        return distAmenities.getMinDistance(cell);
    }

    @Override
    public String getShortName() {
        return "DistEnv";
    }

    /**
     * Sets the urban border.
     * Must be set before evaluation calculation
     * @param urbanBorder a MultiLinestring representing the urban border
     */
    public void setUrbanBorder(Geometry urbanBorder) {
        this.urbanBorder = urbanBorder;
    }
    
    /**
     * Sets the network graph.
     * Must be set before evaluation calculation
     * @param graph 
     */
    public void setGraph(SpatialGraph graph) {
        this.graph = graph;
    }

    /**
     * Sets the network geometry.
     * Must be set before evaluation calculation
     * @param netGeom 
     */
    public void setNetGeom(Geometry netGeom) {
        this.netGeom = netGeom;
    }    

}
