/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.mupcity.rule;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.image.DataBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import org.thema.mupcity.Project;
import org.thema.mupcity.Project.Layers;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.common.param.XMLParams;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.msca.Cell;
import org.thema.msca.operation.AbstractLayerOperation;

/**
 *
 * @author gvuidel
 */
public class LeisureRule extends AbstractRule {

    @XMLParams.NoParam
    int level;
    
    @XMLParams.Name("Distance function")
    @XMLParams.Comment("Last Y value must be 0 and funcion must be monotone decrease")
    DiscreteFunction distance;
    
    public LeisureRule(int level) {
        super(Arrays.asList(Project.Layers.LEISURE));
        this.level = level;
        if(level == 1) {
            distance = new DiscreteFunction(new double[]{0.0, 300.0}, new double []{1.0, 0.0});
        } else if(level == 2) { // level 
            distance = new DiscreteFunction(new double[]{0.0, 2000.0}, new double []{1.0, 0.0});
        } else {
            distance = new DiscreteFunction(new double[]{0.0, 5000.0}, new double []{1.0, 0.0});
        }
    }
    @Override
    public String getName() {
        return "lei" + level;
    }

    @Override
    public void createRule(final Project project) {
        final DefaultFeatureCoverage<DefaultFeature> leiCov = project.getCoverageLevel(Layers.LEISURE, level);
        final HashSet types = new HashSet();
        for(Feature f : leiCov.getFeatures())
            types.add(f.getAttribute(Project.TYPE_FIELD));
        
        project.getMSGrid().addLayer(getName(), DataBuffer.TYPE_FLOAT, Float.NaN);
        project.getMSGrid().execute(new AbstractLayerOperation(4) {
            @Override
            public void perform(Cell cell) {
                double distMax = distance.getPoints().lastKey();
                Polygon cellGeom = cell.getGeometry();
                Envelope envMax = new Envelope(cellGeom.getEnvelopeInternal());
                envMax.expandBy(distMax);
                OriginDistance origDistance = project.getDistance(cellGeom, distMax);
                HashMap<Object, Double> minDist = new HashMap<Object, Double>();
                for(Feature lei : leiCov.getFeatures(envMax)) {
                    double dist = origDistance.getDistance((Point)lei.getGeometry());
                    if(dist > distMax)
                        continue;
                    Object type = lei.getAttribute(Project.TYPE_FIELD);
                    if(minDist.containsKey(type)) {
                        double min = minDist.get(type);
                        if(dist < min)
                            minDist.put(type, dist);
                    } else
                        minDist.put(type, dist);                    
                }
                if(minDist.isEmpty()) {
                    cell.setLayerValue(getName(), 0);
                    return;
                }
                double o;
                if(types.size() == 1) // pour éviter la division par 0
                    o = 0;
                else
                    o = 1 - (minDist.size()-1) / (types.size() - 1);
                // on maximise l'évaluation et donc on minimise la distance car la fonction d'évaluation est monotone décroissante
                double min = Collections.min(minDist.values());
                cell.setLayerValue(getName(), Math.pow(distance.getValue(min), o));
              
            }
        }, true);
    }
    
}
