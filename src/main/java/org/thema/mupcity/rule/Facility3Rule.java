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

import java.awt.image.DataBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.thema.common.collection.HashMapList;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.common.param.ReflectObject;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.graph.GraphLocation;
import org.thema.msca.Cell;
import org.thema.msca.operation.AbstractLayerOperation;
import org.thema.mupcity.Project;
import org.thema.mupcity.Project.Layers;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * The facility rule for level 3.
 * 
 * @author Gilles Vuidel
 */
public class Facility3Rule extends AbstractRule {

	@ReflectObject.Name("Distance function")
	DiscreteFunction distance = new DiscreteFunction(new double[] { 0.0, 15000.0 }, new double[] { 1.0, 0.001 });

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
		final DefaultFeatureCoverage<DefaultFeature> facCov = project.getCoverageLevel(Layers.FACILITY, 3);
		// only for optimize networkdistance
		final HashMapList<Node, Object> nodeTypes = new HashMapList<>();
		final HashSet<String> types = new HashSet<String>();
		for (Feature fac : facCov.getFeatures()) {
			types.add((String)fac.getAttribute(Project.TYPE_FIELD));
			GraphLocation location = project.getSpatialGraph().getLocation((Point) fac.getGeometry());
			if (location.isSnapToEdge()) {
				nodeTypes.putValue(((Edge) location.getGraphElem()).getNodeA(), fac.getAttribute(Project.TYPE_FIELD));
				nodeTypes.putValue(((Edge) location.getGraphElem()).getNodeB(), fac.getAttribute(Project.TYPE_FIELD));
			} else {
				nodeTypes.putValue((Node) location.getGraphElem(), fac.getAttribute(Project.TYPE_FIELD));
			}
		}
		project.getMSGrid().addLayer(getName(), DataBuffer.TYPE_FLOAT, Float.NaN);
		project.getMSGrid().execute(new AbstractLayerOperation(4) {
			@Override
	        public void perform(Cell cell) {
                double distMax = distance.getPoints().lastKey();
                Polygon cellGeom = cell.getGeometry();
                Envelope envMax = new Envelope(cellGeom.getEnvelopeInternal());
                envMax.expandBy(distMax);
                OriginDistance origDistance = project.getDistance(cellGeom, distMax);
                HashMap<Object, Double> minDist = new HashMap<>();
                for(Feature lei : facCov.getFeatures(envMax)) {
                    double dist = origDistance.getDistance((Point)lei.getGeometry());
                    if(dist > distMax) {
                        continue;
                    }
                    Object type = lei.getAttribute(Project.TYPE_FIELD);
                    if(minDist.containsKey(type)) {
                        double min = minDist.get(type);
                        if(dist < min) {
                            minDist.put(type, dist);
                        }
                    } else {
                        minDist.put(type, dist);
                    }                    
                }
                if(minDist.isEmpty()) {
                    cell.setLayerValue(getName(), 0);
                    return;
                }
                double o;
                if(types.size() == 1) { // pour éviter la division par 0
                    o = 0;
                } else {
                    o = 1 - (minDist.size()-1) / (types.size() - 1);
                }
                // on maximise l'évaluation et donc on minimise la distance car la fonction d'évaluation est monotone décroissante
                double min = Collections.min(minDist.values());
                cell.setLayerValue(getName(), Math.pow(distance.getValue(min), o));
              
            }
		}, true);
	}

}
