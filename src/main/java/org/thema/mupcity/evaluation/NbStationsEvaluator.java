/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.mupcity.evaluation;

import java.util.ArrayList;
import java.util.List;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.data.feature.DefaultFeature;
import org.thema.graph.SpatialGraph;
import org.thema.mupcity.scenario.Scenario;
import org.thema.common.param.XMLParams;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.msca.Cell;
import org.thema.mupcity.Project;

/**
 *
 * @author gvuidel
 */
public class NbStationsEvaluator extends Evaluator {

    @XMLParams.NoParam
    private Project project;
    
    @XMLParams.NoParam
    private transient DistAmenities distAmen;
        
    @XMLParams.NoParam
    private transient SpatialGraph graph;
    
    double distMax;
    
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

            // test s'il existe les layers 
            if (project.isLayerExist(Project.Layers.BUS_STATION) && project.isLayerExist(Project.Layers.TRAIN_STATION)) {

                // charge coverage service des stations de bus
                final DefaultFeatureCoverage<DefaultFeature> CovBusStation = project.getCoverage(Project.Layers.BUS_STATION);// extrait l'ensemble des points
                //récupère la liste des features des stations de bus
                List<DefaultFeature> featuresBusStation = new ArrayList<DefaultFeature>(CovBusStation.getFeatures());

                //récupère la liste des features de level 2
                final DefaultFeatureCoverage<DefaultFeature> CovTrainStation = project.getCoverage(Project.Layers.TRAIN_STATION);// extrait l'ensemble des points
                //récupère la liste des features des stations de train
                List<DefaultFeature> featureTrainStation = new ArrayList<DefaultFeature>(CovTrainStation.getFeatures());

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

        }
        return distAmen;
    }
     
     public void setGraph(SpatialGraph graph) {
        this.graph = graph;
        distAmen = null;
    }

    @Override
    public String getShortName() {
        return "NbStations";
    }
}
