/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.mupcity.evaluation;

import com.vividsolutions.jts.geom.Geometry;
import java.awt.image.DataBuffer;
import java.io.IOException;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.msca.Cell;
import org.thema.msca.SquareGrid;
import org.thema.msca.operation.SimpleGeomOperation;
import org.thema.mupcity.scenario.Scenario;
import org.thema.common.parallel.TaskMonitor;
import org.thema.mupcity.Project;

/**
 *
 * @author gvuidel
 */
public class NbCellOnEnvelopeEvaluator extends Evaluator {
    
    transient Geometry urbanBorder;
    
    public NbCellOnEnvelopeEvaluator() {
        super(new DiscreteFunction(new double[] {0, 1}, new double[] {0, 1}));
    }

    @Override
    public void execute(Scenario scenario, SquareGrid grid, TaskMonitor monitor) {
        
        // créé une nouvelle couche pour stocker l'évaluation
        Project.getProject().getMSGrid().addLayer(getEvalLayerName(scenario), DataBuffer.TYPE_FLOAT, Float.NaN);

        Project.getProject().getMSGrid().visit(new SimpleGeomOperation(SimpleGeomOperation.ISEMPTY, getEvalLayerName(scenario)), urbanBorder);
    }

    public void setUrbanBorder(Geometry urbanBorder) {
        this.urbanBorder = urbanBorder;
    }
    
    @Override
    protected double eval(Scenario scenario, Cell cell) { // does nothing
        return 0;
    }

    @Override
    public String getShortName() {
        return "NbCellEnv";
    }


//    @Override
//    public Double[] eval(final Scenario anal, final double mean) {
//        final String analLayer = anal.getResultLayerName();
//        FeatureCoverage<GridFeature> newBuild = cov.getCoverage(new FeatureFilter() {
//            public boolean accept(Feature f) {
//                return ((Number)f.getAttribute(analLayer)).intValue() == 2;
//            }
//        });
//
//        ArrayList<Geometry> geoms = new ArrayList<Geometry>();
//        for(Feature f : newBuild.getFeatures())
//            geoms.add(f.getGeometry().getCentroid());
//
//        Geometry buildBuf = BufferTask.threadedBuffer(new GeometryFactory().buildGeometry(geoms), radius + 5);
//        if(buildBuf == null)
//            buildBuf = totBuild;
//        else
//            buildBuf = buildBuf.union(totBuild);
//        Geometry envelope = BufferTask.threadedBuffer(buildBuf, -radius);
//        Geometry envLine = envelope.getBoundary();
//
//        msGrid.visit(new SimpleGeomOperation(SimpleGeomOperation.ISEMPTY, "env"), envLine);
//       
//        double nb = grid.agregate(new AbstractAgregateOperation<Double>(4, 0.0) {
//            public void perform(Cell cell) {
//                if(isEvaluated(cell, anal) && cell.getLayerValue("env") == 1)
//                    result++;
//            }
//        });
//        
//        return new Double[] {nb, nb, nb};
//    }


}
