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

import java.util.HashMap;
import java.util.HashSet;

import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.locationtech.jts.geom.Point;
import org.thema.common.collection.HashMapList;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.common.param.ReflectObject;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.graph.GraphLocation;
import org.thema.graph.SpatialGraph;
import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.msca.Cell;
import org.thema.msca.SquareGrid;
import org.thema.mupcity.Project;
import org.thema.mupcity.rule.OriginDistance;
import org.thema.mupcity.scenario.Scenario;

/**
 * Evaluates the average minimum distance to each type of amenities.
 * 
 * @author Gilles Vuidel
 */
public class DistMinTypeAmenEvaluator extends Evaluator {

    @ReflectObject.NoParam
    private Project.Layers layer;
    @ReflectObject.NoParam
    private int level;
    
    private transient HashSet types;
    private transient HashMapList<Node, Object> nodeTypes ;
    private transient DefaultFeatureCoverage<DefaultFeature> facCov;
    private transient Project project;
    private transient SpatialGraph graph;
    
    /**
     * Creates a new DistMinTypeAmenEvaluator.
     * @param project the current project
     * @param layer the layer of amenities
     * @param level the level of amenity or -1 for all levels
     * @param x the abscisses of the membership function
     * @param y the ordinates of the membership function
     */
    public DistMinTypeAmenEvaluator(Project project, Project.Layers layer, int level, double[] x, double[] y ) {
        super(new DiscreteFunction(x, y));
        this.project = project;
        this.layer = layer;
        this.level = level;
    }
        
    @Override
    public void execute(Scenario scenario, SquareGrid grid, TaskMonitor monitor) {
        // initialisation du type de graph (walking distance) ou celui du projet
        if (graph == null) {
            graph = project.getSpatialGraph();
        }
        //charge le coverage du layer suivant le niveau
        facCov = project.getCoverageLevel(layer, level);
        
        // only for optimize networkdistance
        // hashMapList liste de noeud avec comme object les attributs de Type 
        nodeTypes = new HashMapList<>();
        types = new HashSet();
        for(Feature fac : facCov.getFeatures()) {
            types.add(fac.getAttribute(Project.TYPE_FIELD));
            GraphLocation location = graph.getLocation((Point)fac.getGeometry());
            if(location.isSnapToEdge()) {
                nodeTypes.putValue(((Edge)location.getGraphElem()).getNodeA(), fac.getAttribute(Project.TYPE_FIELD));
                nodeTypes.putValue(((Edge)location.getGraphElem()).getNodeB(), fac.getAttribute(Project.TYPE_FIELD));
            } else {
                nodeTypes.putValue((Node)location.getGraphElem(), fac.getAttribute(Project.TYPE_FIELD));
            }
        }
           
        super.execute(scenario, grid, monitor);
    }
    
    @Override
    protected double eval(Scenario scenario, Cell cell) {        
        HashMap<Object, Double> minDist = new HashMap<>();
        final HashSet restTypes = new HashSet(types);
        OriginDistance origDistance = project.getDistance(cell.getGeometry(), Double.NaN);
        if(origDistance instanceof OriginDistance.NetworkDistance) {
            ((OriginDistance.NetworkDistance)origDistance).setDijkstraListener(new DijkstraPathFinder.CalculateListener() {
                @Override
                public boolean currentNode(DijkstraPathFinder.DijkstraNode node) {
                    if(nodeTypes.containsKey(node.node)) {
                        restTypes.removeAll(nodeTypes.get(node.node));
                    }
                    return !restTypes.isEmpty();
                }
            });
        }
                              
        for(Feature fac : facCov.getFeatures()) {         
            // the average distance 
            double dist = origDistance.getDistance((Point)fac.getGeometry());
            Object type = fac.getAttribute(Project.TYPE_FIELD);
            if(minDist.containsKey(type)) {
                double min = minDist.get(type);
                if(dist < min) {
                    minDist.put(type, dist);
                }
            } else {
                minDist.put(type, dist);
            }                    
        }
                
        double sum = 0;
        for(Double val : minDist.values()) {
            sum += val;
        }
                
        return sum / minDist.size();
    }

    /**
     * Sets the network graph.
     * It will be used only if level == 1, else the project's network will be used
     * @param graph the network to use for level 1 or null for using the project's network graph
     */
    public void setGraph(SpatialGraph graph) {
        if(level == 1) {
            this.graph = graph;
        }
    }

    @Override
    public String getShortName() {
        return "DistMinType_"+ layer.toString() + "-" + level;
    }
}
