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
     * @param dest the destination point or polygon
     * @return the distance between the origin and dest or Double.MAX_VALUE
     */
    double getDistance(Geometry dest);
    
    /**
     * Return time distance in minutes
     * @param dest the destination point or polygon
     * @return the time distance between the origin and dest or Double.MAX_VALUE
     */
    double getTimeDistance(Geometry dest);
    
    /**
     * OriginDistance implementation for euclidean distance.
     */
    class EuclideanDistance implements OriginDistance {

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
        public double getDistance(Geometry dest) {
            return origin.distance(dest);
        }
        
        @Override
        public double getTimeDistance(Geometry dest) {
            return 60 * ((origin.distance(dest) / 1000) / speed);
        }

    }
    
    /**
     * OriginDistance implementation for network distance.
     */
    class NetworkDistance implements OriginDistance {

        private Geometry origin;
        private double maxCost;
        private DijkstraPathFinder distPathfinder, timePathfinder;
        private SpatialGraph graph;
        private DijkstraPathFinder.CalculateListener dijkstraListener;

        /**
         * Attention le polygone est supposé rectangle -> est égal à son enveloppe
         * @param graph the network graph
         * @param origin the origin as a Point or Polygon, if it is a Polygon, must be rectangular
         * @param maxCost max distance in meter or in minutes for speeding up execution, or NaN to calculate on the whole network
         */
        public NetworkDistance(SpatialGraph graph, Geometry origin, double maxCost) {
            this.graph = graph;
            this.origin = origin;
            this.maxCost = maxCost;
        }
        
        @Override
        public double getDistance(Geometry dest) {
            return getCost(getDistPathFinder(), dest);
        }
        
        @Override
        public double getTimeDistance(Geometry dest) {   
            return getCost(getTimePathFinder(), dest);
        }
        
        private double getCost(DijkstraPathFinder finder, Geometry dest) {
            if(dest instanceof Point) {
                if(origin.getEnvelopeInternal().contains(dest.getCoordinate())) {
                    return 0;
                }
                Double cost = graph.getCost(finder, (Point)dest);
                if(cost == null) {
                    return Double.MAX_VALUE;
                } else {
                    return cost;
                }
            } else if(dest instanceof Polygon) {
                if(dest.getEnvelopeInternal().contains(origin.getCoordinate())) {
                    return 0;
                }
                double minCost = Double.MAX_VALUE;
                for(Point p : getPointsOnNetwork((Polygon)dest)) {
                    Double cost = graph.getCost(finder, p);
                    if(cost != null && cost < minCost) {
                        minCost = cost;
                    }
                }
                return minCost;
            } else { // multipoint
                double minCost = Double.MAX_VALUE;
                for(int i = 0; i < dest.getNumGeometries(); i++) {
                    Double cost = graph.getCost(finder, (Point)dest.getGeometryN(i));
                    if(cost != null && cost < minCost) {
                        minCost = cost;
                    }
                }
                return minCost;
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
                List<Point> points = getPointsOnNetwork((Polygon)origin);
                List<GraphLocation> locs = new ArrayList<>(points.size());
                for(Point p : points) {
                    locs.add(graph.getLocation(p));
                }
                
                pathfinder = new DijkstraPathFinder(graph.getGraph(), locs, weighter);
                
            }
            if(dijkstraListener != null) {
                pathfinder.calculate(dijkstraListener);
            } else {
                pathfinder.calculate(maxCost);
            }
            return pathfinder;
        }
    
        /**
         * @param origin polygone d'origine ou de destination
         * @return la liste des points du réseau intersectant le polygone ou bien le point le plus proche 
         */
        public List<Point> getPointsOnNetwork(Polygon origin) {
            List<Edge> edges = graph.getEdgeSpatialIndex().query(origin.getEnvelopeInternal());
            List<Point> points = new ArrayList<>();
            Geometry envelope = origin.getEnvelope();
            GeometryFactory geomFac = new GeometryFactory();
            for(Edge edge : edges) { 
                if(Util.getGeometry(edge).intersects(envelope)) {
                    Geometry inter = Util.getGeometry(edge).intersection(envelope);
                    for(Coordinate coord : inter.getCoordinates()) {
                        points.add(geomFac.createPoint(coord));
                    }
                }
            }

            if(points.isEmpty()) {
                Edge edge = graph.getNearestEdge(origin);
                Coordinate coord = new DistanceOp(origin, Util.getGeometry(edge)).nearestPoints()[0];
                points.add(geomFac.createPoint(coord));
            } 
            return points;
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
