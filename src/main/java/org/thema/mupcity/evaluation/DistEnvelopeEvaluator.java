
package org.thema.mupcity.evaluation;

import com.vividsolutions.jts.geom.Geometry;
import java.util.ArrayList;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graph.SpatialGraph;
import org.thema.msca.SquareGrid;
import org.thema.mupcity.scenario.Scenario;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.msca.Cell;

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
