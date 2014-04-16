/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
 *
 * @author gvuidel
 */
public class DistEnvelopeEvaluator extends Evaluator {

    transient Geometry urbanBorder;
    
    transient SpatialGraph graph;
    
    transient Geometry netGeom;

    transient DistAmenities distAmenities;

    public DistEnvelopeEvaluator() {
        super(new DiscreteFunction(new double[] {0.0, 200}, new double[] {1.0, 0.001}));
    }

    @Override
    public void execute(Scenario scenario, SquareGrid grid, TaskMonitor monitor) {
        Geometry points = netGeom.intersection(urbanBorder);
        ArrayList<Feature> dest = new ArrayList<Feature>();
        for(int i = 0; i < points.getNumGeometries(); i++)
            dest.add(new DefaultFeature("Env_"+i, points.getGeometryN(i), null, null));
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

    public void setUrbanBorder(Geometry urbanBorder) {
        this.urbanBorder = urbanBorder;
    }
    
    public void setGraph(SpatialGraph graph) {
        this.graph = graph;
    }

    public void setNetGeom(Geometry netGeom) {
        this.netGeom = netGeom;
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
//        saveGeom(envelope, anal + "-envelope", "MultiPolygon");
//        Geometry envLine = envelope.getBoundary();
//        //saveGeom(envLine, "envLine", "MultiLineString");
//        
//        Geometry points = netGeom.intersection(envLine);
//        saveGeom(points, anal + "-points", "MultiPoint");
//        ArrayList<Feature> dest = new ArrayList<Feature>();
//        for(int i = 0; i < points.getNumGeometries(); i++)
//            dest.add(new DefaultFeature("Env_"+i, points.getGeometryN(i), null, null));
//
//        List<GridFeature> residBuild = cov.getFeatures(new FeatureFilter() {
//            public boolean accept(Feature f) {
//                return isEvaluated(((GridFeature)f).getCell(), anal);
//            }
//        });
//
//        double [] dist = new double[residBuild.size()];
//        Arrays.fill(dist, Double.MAX_VALUE);
//        for(Feature f : dest) {
//            double [] dc = graph.getCostVector(f, residBuild, DijkstraPathFinder.DIST_WEIGHTER);
//            for(int i = 0; i < dist.length; i++)
//                if(dc[i] < dist[i])
//                    dist[i] = dc[i];
//        }
//
//        double sum = 0;
//        int nb = 0, nbInf = 0;
//        for(int i = 0; i < dist.length; i++)
//            if(dist[i] != Double.MAX_VALUE) {
//                sum += dist[i];
//                nb++;
//                if(dist[i] < mean && isNewBuild(residBuild.get(i).getCell(), anal))
//                    nbInf++;
//            }
//
//        return new Double[] {sum / nb, (double)nb, (double)nbInf};
//    }



//    private void saveGeom(Geometry geom, String name, String geomType) {
//        try {
//            ShapefileDataStore store = new ShapefileDataStore(new File(
//                    Project.getProject().getDirectory().getAbsolutePath() + File.separator + name + ".shp").toURI().toURL());
//            SimpleFeatureType fType = DataUtilities.createType("result", "geom:" + geomType);
//            store.createSchema(fType);
//            FeatureStore fStore = (FeatureStore) store.getFeatureSource();
//            FeatureCollection fCol = FeatureCollections.newCollection();
//            for (int i = 0; i < geom.getNumGeometries(); i++) {
//                SimpleFeature sf = DataUtilities.template(fType);
//                sf.setAttributes(new Object[]{geom.getGeometryN(i)});
//                fCol.add(sf);
//            }
//            fStore.addFeatures(fCol);
//            store.dispose();
//        } catch (Exception ex) {
//            Logger.getLogger(DistEnvelopeEvaluator.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }


}
