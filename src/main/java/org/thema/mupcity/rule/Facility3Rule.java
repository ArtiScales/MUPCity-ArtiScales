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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.awt.image.DataBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.thema.mupcity.Project;
import org.thema.mupcity.Project.Layers;
import org.thema.mupcity.rule.OriginDistance.NetworkDistance;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.common.param.ReflectObject;
import org.thema.data.feature.Feature;
import org.thema.msca.Cell;
import org.thema.msca.operation.AbstractLayerOperation;

/**
 * The facility rule for level 3.
 * 
 * @author Gilles Vuidel
 */
public class Facility3Rule extends AbstractRule {
    
    @ReflectObject.Name("Distance function")
    DiscreteFunction distance = new DiscreteFunction(new double[]{15.0, 30.0}, new double []{1.0, 0.0});
    
    /**
     * Creates new facility rule level 3 with default parameters.
     */
    public Facility3Rule() {
        super(Arrays.asList(Layers.FACILITY));
    }

    @Override
    public String getName() {
        return "fac3";
    }

    @Override
    public void createRule(final Project project) {
        final Map<Feature, OriginDistance> facDistance = new HashMap<>();
        for(Feature f : project.getCoverageLevel(Layers.FACILITY, 3).getFeatures()) {
            facDistance.put(f, project.getDistance(f.getGeometry(), Double.NaN));
        }
        
        project.getMSGrid().addLayer(getName(), DataBuffer.TYPE_FLOAT, Float.NaN);
        project.getMSGrid().execute(new AbstractLayerOperation(4) {
            @Override
            public void perform(Cell cell) {
                HashMap<Object, Double> minDist = new HashMap<>();
                
                Geometry destGeom = cell.getGeometry();
                OriginDistance origDistance = project.getDistance(destGeom, Double.NaN);
                if(origDistance instanceof NetworkDistance) {
                    List<Point> points = ((NetworkDistance)origDistance).getPointsOnNetwork(cell.getGeometry());
                    destGeom = destGeom.getFactory().buildGeometry(points);
                }
                for(Feature fac : facDistance.keySet()) {
                    double dist = 0;
                    if(!cell.getGeometry().contains(fac.getGeometry())) {
                        dist = facDistance.get(fac).getTimeDistance(destGeom);
                    }
                    Object type = fac.getAttribute(Project.TYPE_FIELD);
                    if(minDist.containsKey(type)) {
                        double min = minDist.get(type);
                        if(dist < min) {
                            minDist.put(type, dist);
                        }
                    } else {
                        minDist.put(type, dist);
                    }                    
                }
                
                double sum = 0;
                for(Double val : minDist.values()) {
                    sum += val;
                }
                
                cell.setLayerValue(getName(), distance.getValue(sum / minDist.size()));
              
            }
        }, true);
        
    }
    
}
