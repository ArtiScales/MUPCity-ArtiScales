
package org.thema.mupcity.evaluation;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import java.io.IOException;
import java.util.HashSet;
import org.thema.mupcity.Project;
import org.thema.mupcity.rule.OriginDistance;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.graph.GraphLocation;
import org.thema.graph.SpatialGraph;
import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.msca.Cell;
import org.thema.mupcity.rule.OriginDistance.NetworkDistance;

/**
 * Calculates metric distances between cells and a layer of points (amenities) on a network.
 * 
 * @author Gilles Vuidel
 */

public class DistAmenities {

    private DefaultFeatureCoverage<DefaultFeature> amCov;
    private HashSet<Node> nodes;
    private SpatialGraph graph;

    /**
     * Creates a new DistAmenities
     * @param amCov the coverage containing amenities points
     * @param graph the network graph
     * @throws IOException 
     */
    public DistAmenities(DefaultFeatureCoverage<DefaultFeature> amCov, SpatialGraph graph) {
        this.amCov = amCov;
        this.graph = graph;
        nodes = new HashSet();
        for(Feature fac : amCov.getFeatures()) {
            GraphLocation location = graph.getLocation((Point)fac.getGeometry());
            if(location.isSnapToEdge()) {
                nodes.add(((Edge)location.getGraphElem()).getNodeA());
                nodes.add(((Edge)location.getGraphElem()).getNodeB());
            } else {
                nodes.add((Node)location.getGraphElem());
            }
        }
    }
    
    /**
     * Creates a new DistAmenities with the project network
     * @param project the current project
     * @param layer the layer containing amenities points
     * @throws IOException 
     */
    public DistAmenities(Project project, Project.Layers layer) throws IOException {
       this(project, layer, -1);
    }
    
    /**
     * Creates a new DistAmenities with the project network
     * @param project the current project
     * @param layer the layer containing amenities points
     * @param level the level of amenity or -1
     * @throws IOException 
     */
    public DistAmenities(Project project, Project.Layers layer, final int level) throws IOException {
        this(project, layer, level, project.getSpatialGraph());
    }
    
    /**
     * Creates a new DistAmenities
     * @param project the current project
     * @param layer the layer containing amenities points
     * @param level the level of amenity or -1
     * @param graph the network graph
     * @throws IOException 
     */
    public DistAmenities(Project project, Project.Layers layer, final int level, SpatialGraph graph) throws IOException {
        this.graph = graph;
        // récupère le coverage du layer avec un certain niveau
        if (level == -1) {
            amCov = project.getCoverage(layer);
        } else {
            amCov = project.getCoverageLevel(layer, level);
        }

        nodes = new HashSet();
        for(Feature fac : amCov.getFeatures()) {
            GraphLocation location = graph.getLocation((Point)fac.getGeometry());
            if(location.isSnapToEdge()) {
                nodes.add(((Edge)location.getGraphElem()).getNodeA());
                nodes.add(((Edge)location.getGraphElem()).getNodeB());
            } else {
                nodes.add((Node)location.getGraphElem());
            }
        }
    }

    /**
     * Calculates the network distance between the cell and the nearest point of the amenities layer
     * @param cell the cell
     * @return the minimal distance between the cell and the amenities
     */
    public double getMinDistance(Cell cell) {
        NetworkDistance origDistance = new NetworkDistance(graph, cell.getGeometry(), Double.NaN);
        
        origDistance.setDijkstraListener(new DijkstraPathFinder.CalculateListener() {
            @Override
            public boolean currentNode(DijkstraPathFinder.DijkstraNode node) {
                return !nodes.contains(node.node);
            }
        });
        double dist = Double.MAX_VALUE;
        for(Feature fac : amCov.getFeatures())  {
            double d = origDistance.getDistance(fac.getGeometry().getCentroid());
            if(d < dist) {
                dist = d;
            }
        }

        if(dist == Double.MAX_VALUE) {
            return Double.NaN;
        } else {
            return dist;
        }
    }

    /**
     * 
     * @param cell the cell
     * @param distMax the maximum distance
     * @return the number of amenities in a distance less than distMax
     */
    public int getNbAmen(Cell cell, double distMax) {
        OriginDistance distance = new NetworkDistance(graph, cell.getGeometry(), distMax);
        Envelope env = new Envelope(cell.getGeometry().getEnvelopeInternal());
        env.expandBy(distMax);
        int nb = 0;
        for(Feature fac : amCov.getFeatures(env)) { 
            if(distance.getDistance(fac.getGeometry().getCentroid()) <= distMax) {
                nb++;
            }
        }

        return nb;
    }

}
