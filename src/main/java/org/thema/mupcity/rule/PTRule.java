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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import java.awt.image.DataBuffer;
import java.util.Arrays;
import org.thema.mupcity.Project;
import org.thema.mupcity.Project.Layers;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.common.param.ReflectObject;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.msca.Cell;
import org.thema.msca.operation.AbstractLayerOperation;

/**
 * Public transport rule.
 * 
 * @author Gilles Vuidel
 */
public class PTRule extends AbstractRule {

    @ReflectObject.Name("Distance max for bus")
    private double maxDistBus = 400;
    
    @ReflectObject.Name("Bus station count function")
    private DiscreteFunction stationCount = new DiscreteFunction(new double[]{0.0, 4.0}, new double []{0.0, 1.0});
    
    @ReflectObject.Name("Distance train station function")
    private DiscreteFunction distanceStation = new DiscreteFunction(new double[]{0.0, 3000.0}, new double []{1.0, 0.0});
    
    /**
     * Creates a new public transport rule with default parameters.
     */
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
                for(Feature f : busCov.getFeatures(envMax))  {
                    if(origDistance.getDistance((Point)f.getGeometry()) <= maxDistBus) {
                        nbStation++;
                    }
                }           
                envMax = new Envelope(cell.getGeometry().getEnvelopeInternal());
                envMax.expandBy(distanceStation.getPoints().lastKey());
                double distMinTrain = Double.MAX_VALUE;
                for(Feature f : trainCov.getFeatures(envMax)) {
                    double d = origDistance.getDistance((Point)f.getGeometry());
                    if(d < distMinTrain) {
                        distMinTrain = d;
                    }
                }
                cell.setLayerValue(getName(), Math.max(stationCount.getValue(nbStation), distanceStation.getValue(distMinTrain)));
              
            }
        }, true);
    }
    
}
