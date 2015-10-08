
package org.thema.mupcity.rule;

import com.vividsolutions.jts.geom.Point;
import java.awt.image.DataBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.thema.mupcity.Project;
import org.thema.mupcity.Project.Layers;
import org.thema.mupcity.rule.OriginDistance.NetworkDistance;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.thema.common.collection.HashMapList;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.common.param.ReflectObject;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.graph.GraphLocation;
import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.graph.pathfinder.DijkstraPathFinder.DijkstraNode;
import org.thema.msca.Cell;
import org.thema.msca.operation.AbstractLayerOperation;

/**
 * The facility rule for level 3.
 * 
 * @author Gilles Vuidel
 */
public class Facility3Rule extends AbstractRule {
    
    @ReflectObject.Name("Distance function")
    DiscreteFunction distance = new DiscreteFunction(new double[]{15.0, 30.0}, new double []{1.0, 0.0});
    
    /**
     * Creates new facility rule level 3 with default parameters.
     */
    public Facility3Rule() {
        super(Arrays.asList(Layers.FACILITY));
    }

    @Override
    public String getName() {
        return "fac3";
    }

    @Override
    public void createRule(final Project project) {
        final DefaultFeatureCoverage<DefaultFeature> facCov = project.getCoverageLevel(Layers.FACILITY, 3);
        // only for optimize networkdistance
        final HashMapList<Node, Object> nodeTypes = new HashMapList<>();
        final HashSet types = new HashSet();
        for(Feature fac : facCov.getFeatures()) {
            types.add(fac.getAttribute(Project.TYPE_FIELD));
            GraphLocation location = project.getSpatialGraph().getLocation((Point)fac.getGeometry());
            if(location.isSnapToEdge()) {
                nodeTypes.putValue(((Edge)location.getGraphElem()).getNodeA(), fac.getAttribute(Project.TYPE_FIELD));
                nodeTypes.putValue(((Edge)location.getGraphElem()).getNodeB(), fac.getAttribute(Project.TYPE_FIELD));
            } else {
                nodeTypes.putValue((Node)location.getGraphElem(), fac.getAttribute(Project.TYPE_FIELD));
            }
        }
        
        project.getMSGrid().addLayer(getName(), DataBuffer.TYPE_FLOAT, Float.NaN);
        project.getMSGrid().execute(new AbstractLayerOperation(4) {
            @Override
            public void perform(Cell cell) {
                HashMap<Object, Double> minDist = new HashMap<>();
                final HashSet restTypes = new HashSet(types);
                OriginDistance origDistance = project.getDistance(cell.getGeometry(), Double.NaN);
                if(origDistance instanceof NetworkDistance) {
                    ((NetworkDistance)origDistance).setDijkstraListener(new DijkstraPathFinder.CalculateListener() {
                        @Override
                        public boolean currentNode(DijkstraNode node) {
                            if(nodeTypes.containsKey(node.node)) {
                                restTypes.removeAll(nodeTypes.get(node.node));
                            }
                            return !restTypes.isEmpty();
                        }
                    });
                }
                
                
                for(Feature fac : facCov.getFeatures()) {
                    double dist = origDistance.getTimeDistance((Point)fac.getGeometry());
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
                
                cell.setLayerValue(getName(), distance.getValue(sum / minDist.size()));
              
            }
        }, true);
        
    }
    
}
