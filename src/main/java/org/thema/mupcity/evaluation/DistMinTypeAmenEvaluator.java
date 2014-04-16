/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.mupcity.evaluation;

import com.vividsolutions.jts.geom.Point;
import java.util.HashMap;
import java.util.HashSet;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.thema.common.collection.HashMapList;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeature;
import org.thema.graph.SpatialGraph;
import org.thema.mupcity.scenario.Scenario;
import org.thema.common.param.XMLParams;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.graph.GraphLocation;
import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.msca.Cell;
import org.thema.msca.SquareGrid;
import org.thema.mupcity.Project;
import org.thema.mupcity.rule.OriginDistance;

/**
 *
 * @author gvuidel
 */
public class DistMinTypeAmenEvaluator extends Evaluator {

    @XMLParams.NoParam
    private Project project;
    @XMLParams.NoParam
    private Project.Layers layer;
    @XMLParams.NoParam
    private int level;
    
    @XMLParams.NoParam
    private transient HashSet types;
    @XMLParams.NoParam
    private transient HashMapList<Node, Object> nodeTypes ;
    @XMLParams.NoParam
    private transient DefaultFeatureCoverage<DefaultFeature> facCov;
        
    @XMLParams.NoParam
    private transient SpatialGraph graph;
    
     
    public DistMinTypeAmenEvaluator(Project project, Project.Layers layer, int level, double[] x, double[] y ) {
        
        super(new DiscreteFunction(x, y));
        this.project = project;
        this.layer = layer;
        this.level = level;
    }
        
    @Override
    public void execute(Scenario scenario, SquareGrid grid, TaskMonitor monitor) {
        // initialisation du type de graph (walking distance) ou celui du projet
        getGraph();
        //charge le coverage du layer suivant le niveau
        facCov = project.getCoverageLevel(layer, level);
        
        // only for optimize networkdistance
        // hashMapList liste de noeud avec comme object les attributs de Type 
        nodeTypes = new HashMapList<Node, Object>();
        types = new HashSet();
        for(Feature fac : facCov.getFeatures()) {
            types.add(fac.getAttribute(Project.TYPE_FIELD));
            GraphLocation location = graph.getLocation((Point)fac.getGeometry());
            if(location.isSnapToEdge()) {
                nodeTypes.putValue(((Edge)location.getGraphElem()).getNodeA(), fac.getAttribute(Project.TYPE_FIELD));
                nodeTypes.putValue(((Edge)location.getGraphElem()).getNodeB(), fac.getAttribute(Project.TYPE_FIELD));
            } else
                nodeTypes.putValue((Node)location.getGraphElem(), fac.getAttribute(Project.TYPE_FIELD));
        }
        
           
        super.execute(scenario, grid, monitor);
    }
    
    @Override
    protected double eval(Scenario scenario, Cell cell) {        
        HashMap<Object, Double> minDist = new HashMap<Object, Double>();
        final HashSet restTypes = new HashSet(types);
        OriginDistance origDistance = project.getDistance(cell.getGeometry(), Double.NaN);
        if(origDistance instanceof OriginDistance.NetworkDistance)
            ((OriginDistance.NetworkDistance)origDistance).setDijkstraListener(new DijkstraPathFinder.CalculateListener() {
                @Override
                public boolean currentNode(DijkstraPathFinder.DijkstraNode node) {
                    if(nodeTypes.containsKey(node.node))
                        restTypes.removeAll(nodeTypes.get(node.node));
                    return !restTypes.isEmpty();
                }
            });
                              
        for(Feature fac : facCov.getFeatures()) {
            
            // getTimeDistance  Calcul le temps de parcours utiliser pour "the average time-distance"
            //double dist = origDistance.getTimeDistance((Point)fac.getGeometry());
            
            // the average distance 
            double dist = origDistance.getDistance((Point)fac.getGeometry());
            Object type = fac.getAttribute(Project.TYPE_FIELD);
            if(minDist.containsKey(type)) {
                double min = minDist.get(type);
                if(dist < min)
                    minDist.put(type, dist);
            } else
                minDist.put(type, dist);                    
        }
                
                double sum = 0;
                for(Double val : minDist.values())
                    sum += val;
                
                return(sum / minDist.size());
    }
    
      public void getGraph() {
          if (graph == null || level != 1){
              graph = project.getSpatialGraph();
          }
                  
       }
     
     public void setGraph(SpatialGraph graph) {
        this.graph = graph;
    }

    @Override
    public String getShortName() {
        return "DistMinType_"+ layer.toString() + "-" + level;
    }
}
