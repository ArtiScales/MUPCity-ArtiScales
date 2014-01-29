/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.mupcity.rule;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import java.awt.image.DataBuffer;
import java.util.Arrays;
import org.thema.mupcity.Project;
import org.thema.mupcity.Project.Layers;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.common.param.XMLParams;
import org.thema.drawshape.feature.DefaultFeature;
import org.thema.drawshape.feature.DefaultFeatureCoverage;
import org.thema.drawshape.feature.Feature;
import org.thema.msca.Cell;
import org.thema.msca.operation.AbstractLayerOperation;

/**
 *
 * @author gvuidel
 */
public class PTRule extends AbstractRule {

    @XMLParams.Name("Distance max for bus")
    double maxDistBus = 400;
    
    @XMLParams.Name("Bus station count function")
    DiscreteFunction stationCount = new DiscreteFunction(new double[]{0.0, 4.0}, new double []{0.0, 1.0});
    
    @XMLParams.Name("Distance train station function")
    DiscreteFunction distanceStation = new DiscreteFunction(new double[]{0.0, 3000.0}, new double []{1.0, 0.0});
    
    public PTRule() {
        super(Arrays.asList(Layers.BUS_STATION, Layers.TRAIN_STATION));
    }
    
    @Override
    public String getName() {
        return "pt";
    }

    @Override
    public void createRule(final Project project) {
        final DefaultFeatureCoverage<DefaultFeature> busCov = project.getCoverage(Layers.BUS_STATION);
        final DefaultFeatureCoverage<DefaultFeature> trainCov = project.getCoverage(Layers.TRAIN_STATION);
        project.getMSGrid().addLayer(getName(), DataBuffer.TYPE_FLOAT, Float.NaN);
        project.getMSGrid().execute(new AbstractLayerOperation(4) {
            @Override
            public void perform(Cell cell) {
                double maxDist = Math.max(maxDistBus, distanceStation.getPoints().lastKey());
                OriginDistance origDistance = project.getDistance(cell.getGeometry(), maxDist);
                Envelope envMax = new Envelope(cell.getGeometry().getEnvelopeInternal());
                envMax.expandBy(maxDistBus);
                int nbStation = 0;
                for(Feature f : busCov.getFeatures(envMax)) 
                    if(origDistance.getDistance((Point)f.getGeometry()) <= maxDistBus)
                        nbStation++;
                            
                envMax = new Envelope(cell.getGeometry().getEnvelopeInternal());
                envMax.expandBy(distanceStation.getPoints().lastKey());
                double distMinTrain = Double.MAX_VALUE;
                for(Feature f : trainCov.getFeatures(envMax)) {
                    double d = origDistance.getDistance((Point)f.getGeometry());
                    if(d < distMinTrain)
                        distMinTrain = d;
                }
                cell.setLayerValue(getName(), Math.max(stationCount.getValue(nbStation), distanceStation.getValue(distMinTrain)));
              
            }
        }, true);
    }
    
}
