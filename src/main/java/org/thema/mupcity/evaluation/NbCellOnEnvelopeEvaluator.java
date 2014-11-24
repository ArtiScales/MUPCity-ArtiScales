/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
 *
 * @author gvuidel
 */
public class NbCellOnEnvelopeEvaluator extends Evaluator {
    
    transient FeatureCoverage urbanBorder;
    
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

    public void setUrbanBorder(Geometry urbanBorderGeom) {
        List<Feature> features = new ArrayList<>();
        for(int i = 0; i < urbanBorderGeom.getNumGeometries(); i++) {
            features.add(new DefaultFeature(i, urbanBorderGeom.getGeometryN(i)));
        }
        this.urbanBorder = new DefaultFeatureCoverage(features);
    }
    
    @Override
    protected double eval(Scenario scenario, Cell cell) { // does nothing
        return 0;
    }

    @Override
    public String getShortName() {
        return "NbCellEnv";
    }

}
