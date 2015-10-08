
package org.thema.mupcity.rule;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import java.util.ArrayList;
import java.util.List;
import org.thema.mupcity.Project;
import org.geotools.graph.structure.Edge;
import org.thema.data.feature.Feature;
import org.thema.graph.GraphLocation;
import org.thema.graph.SpatialGraph;
import org.thema.graph.Util;
import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.graph.pathfinder.DijkstraPathFinder.CalculateListener;
import org.thema.graph.pathfinder.EdgeWeighter;

/**
 * Interface for calculating distances from an origin point.
 * 
 * @author Gilles Vuidel
 */
public interface OriginDistance {
    
    /**
     * Return distance in meter
     * @param dest the destination point
     * @return the distance between the origin and dest or Double.MAX_VALUE
     */
    public double getDistance(Point dest);
    /**
     * Return time distance in minutes
     * @param dest the destination point
     * @return the time distance between the origin and dest or Double.MAX_VALUE
     */
    public double getTimeDistance(Point dest);
    
    
    /**
     * OriginDistance implementation for euclidean distance.
     */
    public class EuclideanDistance implements OriginDistance {

        /**
         * The speed in km.h-1
         */
        public static double speed = 50; //km.h-1
        private Geometry origin;

        /**
         * Creates a new EuclideanDistance.
         * 
         * @param origin the origin point or polygon
         */
        public EuclideanDistance(Geometry origin) {
            this.origin = origin;
        }

        @Override
        public double getDistance(Point dest) {
            return origin.distance(dest);
        }
        
        @Override
        public double getTimeDistance(Point dest) {
            return 60 * ((origin.distance(dest) / 1000) / speed);
        }

    }
    
    /**
     * OriginDistance implementation for network distance.
     */
    public class NetworkDistance implements OriginDistance {

        private Geometry origin;
        private double maxCost;
        private DijkstraPathFinder distPathfinder, timePathfinder;
        private SpatialGraph graph;
        private DijkstraPathFinder.CalculateListener dijkstraListener;

        /**
         * Attention le polygone est supposé rectangle -> est égal à son enveloppe
         * @param graph the network graph
         * @param origin the origin as a Polygon, must be rectangular
         * @param maxCost max distance in meter or in minutes for speeding up execution, or NaN to calculate on the whole network
         */
        public NetworkDistance(SpatialGraph graph, Polygon origin, double maxCost) {
            this.graph = graph;
            this.origin = origin;
            this.maxCost = maxCost;
        }
        
        /**
         * Creates a new NetworkDistance.
         * @param graph the network graph
         * @param origin the point origin
         * @param maxCost max distance in meter or in minutes for speeding up execution, or NaN to calculate on the whole network
         */
        public NetworkDistance(SpatialGraph graph, Point origin, double maxCost) {
            this.graph = graph;
            this.origin = origin;
            this.maxCost = maxCost;
        }

        @Override
        public double getDistance(Point dest) {
            if(origin.getEnvelopeInternal().contains(dest.getCoordinate())) {
                return 0;
            }
            Double cost = graph.getCost(getDistPathFinder(), dest);
            if(cost == null) {
                return Double.MAX_VALUE;
            } else {
                return cost;
            }
        }
        
        @Override
        public double getTimeDistance(Point dest) {
            if(origin.getEnvelopeInternal().contains(dest.getCoordinate())) {
                return 0;
            }
            Double cost = graph.getCost(getTimePathFinder(), dest);
            if(cost == null) {
                return Double.MAX_VALUE;
            } else {
                return cost;
            }
        }

        /**
         * Sets dijkstra listener for stoping calculation at any time.
         * If a listener is set the maxCost parameter is not used.
         * This method must be called before {@link #getDistance} or {@link #getTimeDistance}
         * @param dijkstraListener the listener or null
         */
        public void setDijkstraListener(CalculateListener dijkstraListener) {
            this.dijkstraListener = dijkstraListener;
        }

        private synchronized DijkstraPathFinder getDistPathFinder() {
            if(distPathfinder == null) {
                distPathfinder = calcPathFinder(DijkstraPathFinder.DIST_WEIGHTER);
            }
            
            return distPathfinder;
        }
        
        private synchronized DijkstraPathFinder getTimePathFinder() {
            if(timePathfinder == null) {
                timePathfinder = calcPathFinder(new TimeEdgeWeighter());
            }
           
            return timePathfinder;
        }
        
        private DijkstraPathFinder calcPathFinder(EdgeWeighter weighter) {
            DijkstraPathFinder pathfinder;
            if(origin instanceof Point) {
                pathfinder = new DijkstraPathFinder(graph.getGraph(), graph.getLocation((Point)origin), weighter);
            } else {
                // on ne tient compte que des arcs qui intersectent le rectangle 
                // tout ce qui est à l'intérieur est sans intérêt
                //  si il n'y a pas d'arc qui intersecte l'origine on prend l'arc le plus proche
                List<Edge> edges = graph.getEdgeSpatialIndex().query(origin.getEnvelopeInternal());
                List<GraphLocation> edgeLoc = new ArrayList<>();
                Geometry envelope = origin.getEnvelope();
                GeometryFactory geomFac = new GeometryFactory();
                for(Edge edge : edges) { 
                    if(Util.getGeometry(edge).intersects(envelope)) {
                        Geometry inter = Util.getGeometry(edge).intersection(envelope);
                        for(Coordinate coord : inter.getCoordinates()) {
                            edgeLoc.add(new GraphLocation(coord, geomFac.createPoint(coord), edge));
                        }
                    }
                }
                
                if(edgeLoc.isEmpty()) {
                    Edge edge = graph.getNearestEdge(origin);
                    Coordinate coord = new DistanceOp(origin, Util.getGeometry(edge)).nearestPoints()[0];
                    Point p = geomFac.createPoint(coord);
                    pathfinder = new DijkstraPathFinder(graph.getGraph(), graph.getLocation(p), weighter);
                } else {            
                    pathfinder = new DijkstraPathFinder(graph.getGraph(), edgeLoc, weighter);
                }
            }
            if(dijkstraListener != null) {
                pathfinder.calculate(dijkstraListener);
            } else {
                pathfinder.calculate(maxCost);
            }
            return pathfinder;
        }
    
    
        /**
         * Time network weighter
         */
        private static class TimeEdgeWeighter implements EdgeWeighter {

            @Override
            public double getWeight(Edge e) {
                return 60 * ((Util.getGeometry(e).getLength() / 1000) / ((Number)((Feature)e.getObject()).getAttribute(Project.SPEED_FIELD)).doubleValue());
            }

            @Override
            public double getToGraphWeight(double dist) {
                return 60 * ((dist / 1000) / EuclideanDistance.speed);
            }

        }
    
    }
}
