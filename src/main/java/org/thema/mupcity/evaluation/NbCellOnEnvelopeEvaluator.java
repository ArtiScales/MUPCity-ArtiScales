
package org.thema.mupcity.evaluation;

import com.vividsolutions.jts.geom.Geometry;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureCoverage;
import org.thema.msca.Cell;
import org.thema.msca.SquareGrid;
import org.thema.msca.operation.SimpleCoverageOperation;
import org.thema.mupcity.Project;
import org.thema.mupcity.scenario.Scenario;

/**
 * Evaluates the number of cell touching the urban border.
 * 
 * @author Gilles Vuidel
 */
public class NbCellOnEnvelopeEvaluator extends Evaluator {
    
    private transient FeatureCoverage urbanBorder;
    
    /**
     * Creates a new NbCellOnEnvelopeEvaluator with default parameters.
     * Before calculating evaluation, the urban border must be set.
     */
    public NbCellOnEnvelopeEvaluator() {
        super(new DiscreteFunction(new double[] {0.0, 1.0}, new double[] {0.001, 1}));
    }

    @Override
    public void execute(Scenario scenario, SquareGrid grid, TaskMonitor monitor) {
        // créé une nouvelle couche pour stocker l'évaluation
        Project.getProject().getMSGrid().addLayer(getEvalLayerName(scenario), DataBuffer.TYPE_FLOAT, Float.NaN);
        Project.getProject().getMSGrid().execute(new SimpleCoverageOperation(SimpleCoverageOperation.ISEMPTY, 
                getEvalLayerName(scenario), urbanBorder), true);
    }

    /**
     * Sets the urban border.
     * Must be set before evaluation calculation
     * @param urbanBorder a MultiLinestring representing the urban border
     */
    public void setUrbanBorder(Geometry urbanBorderGeom) {
        List<Feature> features = new ArrayList<>();
        for(int i = 0; i < urbanBorderGeom.getNumGeometries(); i++) {
            features.add(new DefaultFeature(i, urbanBorderGeom.getGeometryN(i)));
        }
        this.urbanBorder = new DefaultFeatureCoverage(features);
    }
    
    /**
     * @throws UnsupportedOperationException
     * @param scenario
     * @param cell
     * @return nothing
     */
    @Override
    protected double eval(Scenario scenario, Cell cell) { // does nothing
        throw new UnsupportedOperationException();
    }

    @Override
    public String getShortName() {
        return "NbCellEnv";
    }

}
