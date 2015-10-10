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
import java.util.List;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.data.feature.DefaultFeature;
import org.thema.graph.SpatialGraph;
import org.thema.mupcity.scenario.Scenario;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.msca.Cell;
import org.thema.mupcity.Project;

/**
 * Evaluates the number of public transport stations inside a given distance.
 * 
 * @author Gilles Vuidel
 */
public class NbStationsEvaluator extends Evaluator {

    private transient Project project;
    private transient DistAmenities distAmen;
    private transient SpatialGraph graph;
    
    private double distMax;
    
    /**
     * Creates a new NbStationsEvaluator
     * @param project the current project
     * @param distMax the maximum distance for counting stations
     */
    public NbStationsEvaluator(Project project, double distMax) {
        super(new DiscreteFunction(new double[] {0, 5}, new double[] {0, 1}));
        this.distMax = distMax;
        this.project = project;
    }
        
    @Override
    public boolean isUsable() {
        return project.isLayerExist(Project.Layers.BUS_STATION)&& project.isLayerExist(Project.Layers.TRAIN_STATION);
    }
       
    @Override
    protected double eval(Scenario scenario, Cell cell) {
        return getDistAmen().getNbAmen(cell, distMax);
    }
    
    private synchronized DistAmenities getDistAmen() {
        if (distAmen == null) {
            // charge coverage service des stations de bus
            final DefaultFeatureCoverage<DefaultFeature> CovBusStation = project.getCoverage(Project.Layers.BUS_STATION);// extrait l'ensemble des points
            //récupère la liste des features des stations de bus
            List<DefaultFeature> featuresBusStation = new ArrayList<>(CovBusStation.getFeatures());

            //récupère la liste des features de level 2
            final DefaultFeatureCoverage<DefaultFeature> CovTrainStation = project.getCoverage(Project.Layers.TRAIN_STATION);// extrait l'ensemble des points
            //récupère la liste des features des stations de train
            List<DefaultFeature> featureTrainStation = new ArrayList<>(CovTrainStation.getFeatures());

            // Concaténation des deux listes
            featuresBusStation.addAll(featureTrainStation);

            // créer le nouveau coverage avec l'esnemble des features des staions de bus et de train
            DefaultFeatureCoverage<DefaultFeature> facCov = new DefaultFeatureCoverage(featuresBusStation);

            if (graph == null) {
                distAmen = new DistAmenities(facCov, project.getSpatialGraph());
            } else {
                distAmen = new DistAmenities(facCov, graph);
            }
        }
        return distAmen;
    }
     
    /**
     * Sets the network graph.
     * @param graph may be null for using the project's network graph
     */
    public void setGraph(SpatialGraph graph) {
        this.graph = graph;
        distAmen = null;
    }

    @Override
    public String getShortName() {
        return "NbStations";
    }
}
