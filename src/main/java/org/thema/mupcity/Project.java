/*
 * Project.java
 *
 * Created on 8 juin 2007, 09:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.thema.mupcity;

import org.thema.mupcity.rule.Rule;
import org.thema.mupcity.rule.LeisureRule;
import org.thema.mupcity.rule.Facility12Rule;
import org.thema.mupcity.rule.OriginDistance;
import org.thema.mupcity.rule.PTRule;
import org.thema.mupcity.rule.Facility3Rule;
import org.thema.mupcity.rule.MorphoRule;
import org.thema.mupcity.rule.RoadRule;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.JDomDriver;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree.DynamicUtilTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.thema.mupcity.rule.OriginDistance.EuclidianDistance;
import org.thema.mupcity.rule.OriginDistance.NetworkDistance;
import org.thema.mupcity.scenario.ScenarioAuto;
import org.thema.mupcity.scenario.ScenarioManual;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.thema.common.JTS;
import org.thema.common.parallel.TaskMonitor;
import org.thema.common.swing.tree.AbstractTreeNode;
import org.thema.drawshape.GridModShape;
import org.thema.drawshape.GridShape;
import org.thema.drawshape.RectModShape;
import org.thema.drawshape.feature.*;
import org.thema.drawshape.image.RasterShape;
import org.thema.drawshape.layer.Layer;
import org.thema.drawshape.layer.*;
import org.thema.drawshape.style.*;
import org.thema.drawshape.style.table.ColorBuilder;
import org.thema.drawshape.style.table.ColorRamp;
import org.thema.drawshape.style.table.UniqueColorTable;
import org.thema.graph.SpatialGraph;
import org.thema.msca.*;
import org.thema.msca.operation.*;
import org.thema.mupcity.evaluation.DistEnvelopeEvaluator;
import org.thema.mupcity.evaluation.DistMinAmenEvaluator;
import org.thema.mupcity.evaluation.Evaluator;
import org.thema.mupcity.evaluation.MeanWhiteEvaluator;
import org.thema.mupcity.evaluation.NbAmenEvaluator;
import org.thema.mupcity.evaluation.NbCellOnEnvelopeEvaluator;
import org.thema.mupcity.evaluation.NbNearWhiteEvaluator;


/**
 *
 * @author Gilles Vuidel
 */
public class Project extends AbstractTreeNode {

    public static final String LEVEL_FIELD = "level";
    public static final String TYPE_FIELD = "type";
    public static final String SPEED_FIELD = "speed";
    
    public enum Layers {
        BUILD, ROAD, TRAIN_STATION, BUS_STATION, FACILITY, LEISURE, RESTRICT
    }
    
    public static final List<LayerDef> LAYERS = Arrays.asList(
        new LayerDef(Layers.BUILD, "Buildings", new FeatureStyle(Color.gray, Color.black)),
        new LayerDef(Layers.ROAD, "Road network", new LineStyle(Color.black), SPEED_FIELD, Number.class),
        new LayerDef(Layers.BUS_STATION, "Bus stations", new PointStyle(Color.black, Color.red)), 
        new LayerDef(Layers.TRAIN_STATION, "Train stations", new PointStyle(Color.black, Color.red)), 
        new LayerDef(Layers.FACILITY, "Facilities", new PointStyle(Color.yellow.darker()), LEVEL_FIELD, Number.class, TYPE_FIELD, Object.class),
        new LayerDef(Layers.LEISURE, "Leisure", new PointStyle(Color.blue), LEVEL_FIELD, Number.class, TYPE_FIELD, Object.class),
//        new LayerDef(Layers.GREEN, "Green areas", new FeatureStyle(new Color(118, 179, 0), new Color(170, 203, 0))),
//        new LayerDef(Layers.WORK, "Working areas", new FeatureStyle(Color.DARK_GRAY, Color.gray), "job", Number.class),
        new LayerDef(Layers.RESTRICT, "Restricted areas", new FeatureStyle(Color.orange, Color.gray))
//        new LayerDef(Layers.WATER, "Water", new FeatureStyle(new Color(1, 155, 255), new Color(1, 155, 255))),
//        new LayerDef(Layers.AGRICULTURE, "Agriculture", new FeatureStyle(Color.YELLOW.darker(), Color.gray))
        );
    
    public static final String BUILD = "build";
    public static final String BUILD_DENS = "build_dens";
    public static final String MORPHO_RULE = "morpho";
    
    public static final String ZONE = "zone";
    public static final String NOBUILD_DENS = "no_build_dens";

    public static final String EVAL = "eval";
    public static final String SIMUL = "analyse";
    public static final String SCENARIO = "scenario";
    
    public static final String NODE_ZONE = java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Zone_etude");
    public static final String NODE_DECOMP = java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Decomposition");
    public static final String NODE_SCENARIO = java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Scenarios");
    public static final String NODE_ANALYSE = java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Analyses");

    public static final TreeMap<Object, Color> colorMap = new TreeMap<Object, Color>();

    static {
        colorMap.put(-1.0, new Color(220, 220, 220)); colorMap.put(0.0, Color.WHITE);
        colorMap.put(1.0, new Color(100, 100, 100)); colorMap.put(2.0, new Color(0, 0, 0));
    }

    public static List<? extends Rule> RULES = Arrays.asList(new MorphoRule(), new RoadRule(), 
            new Facility12Rule(1), new Facility12Rule(2), new Facility3Rule(), new PTRule(),
            new LeisureRule(1), new LeisureRule(2), new LeisureRule(3));
    
    private static Project project;

    private RectModShape bounds;    
    
    private ArrayList<ShapeFileLayer> infoLayers;

    private LinkedHashMap<String, Rule> rules;
    private Class<? extends OriginDistance> distType;
    private double netPrecision;
    
    private int coefDecomp;

    private List<ScenarioAuto> scenarioAutos;
    private List<ScenarioManual> scenarios;
    
    private List<Evaluator> evaluators;
    
    private transient File file;
    
    private transient MSGridBuilder<SquareGridExtent> msGrid;
    private transient DefaultGroupLayer decompLayer;
    private transient DefaultGroupLayer infoLayer;
    
    private transient HashMap<String, DefaultFeatureCoverage<DefaultFeature>> coverages;
            
    private transient CoordinateReferenceSystem crs;

    private transient SpatialGraph spatialGraph;
    
    /** Creates a new instance of Project */
    private Project(String name, File dir) throws Exception {
        
        file = new File(dir, name + ".xml");
        Envelope env = getCoverage(Layers.BUILD).getEnvelope();
        bounds = new GridModShape(new Rectangle2D.Double(-0.5, -0.5, 1, 1), new AffineTransform(env.getWidth(), 0, 0, env.getHeight(), env.centre().x, env.centre().y), 1);

        rules = new LinkedHashMap<String, Rule>();
        for(Rule rule : RULES)
            rules.put(rule.getName(), rule);
        
        scenarioAutos = new ArrayList<ScenarioAuto>();
        scenarios = new ArrayList<ScenarioManual>();
        distType = OriginDistance.EuclidianDistance.class;
        
        try {
            ShapefileDataStore dataStore = new ShapefileDataStore(new File(dir, Layers.BUILD.toString() + ".shp").toURI().toURL());
            crs = dataStore.getSchema().getCoordinateReferenceSystem();
            dataStore.dispose();
        } catch (IOException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        infoLayers = new ArrayList<ShapeFileLayer>();
    }

    public void defineZone(Project zonePrj) {
        bounds = zonePrj.bounds;
    }
    
    public void decomp(int exp, double maxSize, double minSize, 
            final double seuilDensBuild) throws IOException {
        int nbRule = 0;
        for(Rule rule : rules.values())
            if(rule.isUsable())
                nbRule++;
        
        TaskMonitor monitor = new TaskMonitor(null, "Decomposition", "initialisation...", 0, nbRule+3);
        monitor.setMillisToPopup(0);
        monitor.setMillisToDecideToPopup(0);

        AffineTransform trans = bounds.getTransform();
        double width = XAffineTransform.getScaleX0(trans);
        double height = XAffineTransform.getScaleY0(trans);

        msGrid = new MSGridBuilder(JTS.geomFromRect(new Rectangle2D.Double(
                    trans.getTranslateX()-width/2,
                    trans.getTranslateY()-height/2, width, height)),
                minSize, maxSize, exp, 4, new SquareGridFactory());
        
        monitor.setNote("Create grid...");
        coefDecomp = exp;
        Envelope env = ((GridExtent)msGrid.getGrid(msGrid.getResolutions().first())).getInternalEnvelope();
        bounds = new RectModShape(new Rectangle2D.Double(-0.5, -0.5, 1, 1), new AffineTransform(env.getWidth(), 0, 0, env.getHeight(), env.centre().x, env.centre().y));

        monitor.incProgress(1);
        msGrid.addDynamicLayer(ZONE, new DistBorderOperation(4));
        monitor.setNote("Create grid... build");
        msGrid.addLayer(BUILD_DENS, DataBuffer.TYPE_FLOAT, Float.NaN);
        msGrid.execute(new SimpleCoverageOperation(SimpleGeomOperation.DENSITY, BUILD_DENS, getCoverage(Layers.BUILD)), true);
        msGrid.addLayer(BUILD, DataBuffer.TYPE_SHORT, 0.0);
        msGrid.execute(new AbstractLayerOperation() {
            public void perform(Cell cell) {
                double dens = cell.getLayerValue(BUILD_DENS);
                if(dens > 0 && dens <= seuilDensBuild)
                    cell.setLayerValue(BUILD, -1);

                if(dens > seuilDensBuild) {
                    Cell parent = ((MSCell)cell).getParent();
                    if(parent == null || parent.getLayerValue(BUILD) == 1)
                        cell.setLayerValue(BUILD, 1);
                    else
                        cell.setLayerValue(BUILD, -1);
                }
            }
        }, true);
        
        if(isLayerExist(Layers.RESTRICT)) {
            monitor.incProgress(1);
            monitor.setNote("Create grid... restrict");
            project.getMSGrid().addLayer(NOBUILD_DENS, DataBuffer.TYPE_FLOAT, Float.NaN);
            project.getMSGrid().execute(new SimpleCoverageOperation(SimpleGeomOperation.DENSITY, NOBUILD_DENS, project.getCoverage(Layers.RESTRICT)), true);
        }

        long t = System.currentTimeMillis();
        for(Rule rule : rules.values())
            if(rule.isUsable()) {
                monitor.incProgress(1);
                monitor.setNote("Create grid... rule " + rule.getFullName());
                rule.createRule(this);
                System.out.println(rule.getFullName() + " : " + (System.currentTimeMillis() - t) / 60000.0 + " minutes");
                t = System.currentTimeMillis();
            }
        
        createGridLayer();
        monitor.close();
    }
    
    public OriginDistance getDistance(Polygon origin, double maxCost) {
        if(distType.equals(EuclidianDistance.class))
            return new EuclidianDistance(origin);
        else
            return new NetworkDistance(getSpatialGraph(), origin, maxCost);
    }
    
    public synchronized SpatialGraph getSpatialGraph() {
        if(spatialGraph == null) {
            spatialGraph = new SpatialGraph(getCoverage(Layers.ROAD).getFeatures(), 
                    netPrecision == 0 ? null : new GeometryPrecisionReducer(new PrecisionModel(1/netPrecision)));
        }
        return spatialGraph;
    }

    public Class<? extends OriginDistance> getDistType() {
        return distType;
    }

    public void setDistType(Class<? extends OriginDistance> distType) {
        this.distType = distType;
    }

    public double getNetPrecision() {
        return netPrecision;
    }

    public void setNetPrecision(double netPrecision) {
        this.netPrecision = netPrecision;
    }

    public int getCoefDecomp() {
        return coefDecomp;
    }

    public void removeDecomp() {
        for(ScenarioManual sce : new ArrayList<ScenarioManual>(scenarios))
            removeScenario(sce);
        for(ScenarioAuto anal : new ArrayList<ScenarioAuto>(scenarioAutos))
            removeAnalysis(anal);
        coefDecomp = 0;
        msGrid = null;
    }
    
    public void addInfoLayer(ShapeFileLayer shapeFileLayer) {
        getInfoLayer();
        infoLayer.addLayerFirst(shapeFileLayer);
        infoLayers.add(shapeFileLayer);
    }

    public List<ScenarioAuto> getScenarioAutos() {
        return scenarioAutos;
    }
    
    public List<ScenarioManual> getScenarios() {
        return scenarios;
    }

    public ScenarioAuto getScenarioAuto(String name) {
        for(ScenarioAuto anal : getScenarioAutos())
            if(anal.toString().equals(name))
                return anal;
        return null;
    }

    public ScenarioManual getScenario(String name) {
        for(ScenarioManual sce : scenarios)
            if(sce.toString().equals(name))
                return sce;
        return null;
    }
    
    public Collection<Rule> getRules() {
        return rules.values();
    }

    public void performScenarioAuto(final ScenarioAuto analyse) {
        analyse.perform(msGrid);
        scenarioAutos.add(analyse);
    }

    public void createManualScenario(String name, int nMax, AHP ahp, boolean mean) {
        ScenarioManual sce = new ScenarioManual(name, nMax, ahp, msGrid, mean);
        scenarios.add(sce);
    }
    
    public NavigableSet<Double> getResolutions() {
        if(isDecomp())
            return msGrid.getResolutions();
        else
            return null;
    }
    
    public GroupLayer getDecompLayer() {
        return decompLayer;
    }
    
    public GroupLayer getGridLayer() {
        DefaultGroupLayer gridLayers = new DefaultGroupLayer(java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Grid"));
        int i = msGrid.getResolutions().size();
        int [] col = {Color.ORANGE.getRed(), Color.ORANGE.getGreen(), Color.ORANGE.getBlue()};
        double dcol [] = {(50.0-col[0])/(i-1), (50.0-col[1])/(i-1), (50.0-col[2])/(i-1)};
        SimpleStyle style;
        for(Double res : msGrid.getResolutions()) {
            style = new SimpleStyle(new Color(col[0], col[1], col[2]), -0.2f+i*0.7f);
            Layer l = new DefaultLayer(String.format("%g", res),
                new GridShape(getBounds(), res), style);
            l.setVisible(false);
            gridLayers.addLayerLast(l);
            i--;
            for(int j = 0; j < 3; j++)
                col[j] = (int) (col[j] + dcol[j]);
        }

        return gridLayers;
    }
    
    public boolean isDecomp() {
        return msGrid != null;
    }

    public MSGridBuilder<SquareGridExtent> getMSGrid() {
        return msGrid;
    }
    
    public Rectangle2D getBounds() {
        return bounds.getBounds();
    }
    
    public RectModShape getRectShape() {
        return bounds;
    }
    
    public boolean isLayerExist(Layers name) {
        return new File(getDirectory(), name.toString()+".shp").exists();
    }

    public boolean hasNoBuild() {
        return msGrid.getLayers().contains(NOBUILD_DENS);
    }
    
    public CoordinateReferenceSystem getCRS() {
        return crs;
    }
    
    public synchronized DefaultFeatureCoverage<DefaultFeature> getCoverage(Layers name) {
        if(coverages == null)
            coverages = new HashMap<String, DefaultFeatureCoverage<DefaultFeature>>();
        if(!coverages.containsKey(name.toString()))
            try {
                coverages.put(name.toString(), new DefaultFeatureCoverage<DefaultFeature>(DefaultFeature.loadFeatures(new File(getDirectory(), name.toString()+".shp"), false)));
            } catch (IOException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        return coverages.get(name.toString());
    }
    
    public synchronized DefaultFeatureCoverage<DefaultFeature> getCoverageLevel(Layers layer, final int level) {
        if(coverages == null)
            coverages = new HashMap<String, DefaultFeatureCoverage<DefaultFeature>>();
        String levelName = layer.toString() + level;
        if(!coverages.containsKey(levelName)) {
            coverages.put(levelName, new DefaultFeatureCoverage<DefaultFeature>(getCoverage(layer).getFeatures(new FeatureFilter() {
                            public boolean accept(Feature f) {
                                return ((Number)f.getAttribute(Project.LEVEL_FIELD)).intValue() == level;
                            }
                        })));
        }
        
        return coverages.get(levelName);
    }
    
    public FeatureGetter<DefaultFeature> getLayerFeatures(final Layers name) {
        return new FeatureGetter<DefaultFeature>() {
            public Collection<DefaultFeature> getFeatures() {
                    return getCoverage(name).getFeatures();
            }
        };
    }
    
    public void setLayer(LayerDef layer, File file, List<String> attrs) throws Exception {
        TaskMonitor mon = new TaskMonitor(null, "Create layer", "", 0, 2);
        mon.setMillisToDecideToPopup(0);
        if(coverages != null) // remove from cache if exists
            coverages.remove(layer.getName());

        mon.setProgress(0);
        mon.setNote("Loading...");
        List<DefaultFeature> features = DefaultFeature.loadFeatures(file, false);
        if(!layer.attrNames.isEmpty()) {
            for(String attr : layer.attrNames)
                if(!features.get(0).getAttributeNames().contains(attr))
                    DefaultFeature.addAttribute(attr, features, null);
            for(DefaultFeature f : features)
                for(int i = 0; i < layer.attrNames.size(); i++)
                    f.setAttribute(layer.attrNames.get(i), f.getAttribute(attrs.get(i)));
        }
        mon.incProgress(1);
        
        mon.setNote("Saving...");
        DefaultFeature.saveFeatures(features, new File(getDirectory(), layer.getName() + ".shp"));
        mon.close();
        
    }

    public void removeAnalysis(ScenarioAuto anal) {
        for(Layer l : anal.getLayers().getLayers())
            msGrid.removeLayer(l.getName());

        scenarioAutos.remove(anal);
    }

    public void removeScenario(ScenarioManual sce) {
        for(Layer l : sce.getLayers().getLayers())
            msGrid.removeLayer(l.getName());

        scenarios.remove(sce);
    }
    
    public List<Evaluator> getEvaluators() {
        if(evaluators == null)
            evaluators = Arrays.asList((Evaluator)new MeanWhiteEvaluator(), new NbNearWhiteEvaluator(), 
                    new NbCellOnEnvelopeEvaluator(), new DistEnvelopeEvaluator(),
                    new DistMinAmenEvaluator(this, Layers.FACILITY, 1), new DistMinAmenEvaluator(this, Layers.FACILITY, 2),
                    new DistMinAmenEvaluator(this, Layers.LEISURE, 1), new DistMinAmenEvaluator(this, Layers.LEISURE, 2), new DistMinAmenEvaluator(this, Layers.LEISURE, 3),
                    new NbAmenEvaluator(this, Layers.FACILITY, 1, 400), new NbAmenEvaluator(this, Layers.FACILITY, 2, 2000),
                    new NbAmenEvaluator(this, Layers.LEISURE, 1, 400), new NbAmenEvaluator(this, Layers.LEISURE, 2, 2000));
        return evaluators;
    }

    public synchronized DefaultGroupLayer getInfoLayer() {
        if(infoLayer == null) {
            infoLayer = new DefaultGroupLayer(java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Infos"));
            for(ShapeFileLayer l : infoLayers)
                infoLayer.addLayerFirst(l);
        }
        return infoLayer;
    }
    
    
    private void createGridLayer() {
        decompLayer = new DefaultGroupLayer(java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Decomposition"));
        
        decompLayer.addLayerFirst(createLayer(BUILD, new UniqueColorTable(colorMap), java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Built-up")));

        for(Rule rule : rules.values())
            if(rule.isUsable())
                decompLayer.addLayerLast(createLayer(rule.getName(), null, rule.getFullName()));
        
//        decompLayer.addLayerLast(createLayer(NB_BUILD_FREE, null, java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Accessibility_open_space")));
//        if(hasNetworkLayer()) {
//            decompLayer.addLayerLast(createLayer(NET_RULE, null, java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Proximity_road")));
//            if(hasServiceLayer()) {
//                decompLayer.addLayerLast(createLayer(N1_ATTR, null, java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Accessibility_N1")));
//                decompLayer.addLayerLast(createLayer(N2_ATTR, null, java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Accessibility_N2")));
//            }
//        }
//        if(hasStationLayer())
//            decompLayer.addLayerLast(createLayer(STATION_RULE, null, java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Proximity_station")));
//           
//        if(hasNetworkLayer() && hasServiceLayer() && msGrid.getLayers().contains(DISTMIN_N1)) {
//            decompLayer.addLayerLast(createLayer(DISTMIN_N1, null, java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Min_dist_N1")));
//            decompLayer.addLayerLast(createLayer(DISTMIN_N2, null, java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Min_dist_N2")));
//        }

    }
    
    public GroupLayer createLayer(String layerName, ColorBuilder colors, String viewLayerName) {
        DefaultGroupLayer gl = new DefaultGroupLayer(viewLayerName);

        //style.setAlpha(0.33f);
//        style.setDrawGrid(false);
//        style.getGridStyle().setContourColor(Color.ORANGE);
        int i = msGrid.getResolutions().size();
        int [] col = {Color.ORANGE.getRed(), Color.ORANGE.getGreen(), Color.ORANGE.getBlue()};
        double dcol [] = {(200.0-col[0])/i, (10.0-col[1])/i, (10.0-col[2])/i};
        
        for(Double res : msGrid.getResolutions()) {
            if(msGrid.getGrid(res).getLayer(layerName) == null)
                continue;
//            style.getGridStyle().setStroke(new BasicStroke(0.5f+i/4f));
//            style.getGridStyle().setContourColor(new Color(col[0], col[1], col[2]));
            RasterStyle style = colors == null ? new RasterStyle(ColorRamp.RAMP_INVGRAY)
                : new RasterStyle(colors, false);
            RasterLayer l = new RasterLayer(String.format("%g", res),
                new RasterShape(msGrid.getGrid(res).getLayer(layerName).getImage().getData(),
                    org.geotools.geometry.jts.JTS.getEnvelope2D(msGrid.getGrid(res).getEnvelope(), 
                        DefaultGeographicCRS.WGS84).getBounds2D(), style));
            l.setCRS(crs);
            l.setVisible(false);
            l.setDrawLegend(false);
            gl.addLayerFirst(l);
            i--;
            for(int j = 0; j < 3; j++)
                col[j] = (int) (col[j] + dcol[j]);
        }
        return gl;
    }
     

    public void saveLayer(String layerName) throws IOException {
        msGrid.saveLayer(getSubDir(), layerName);
    }

    public void reloadLayer(String layerName) throws IOException {
        msGrid.reloadLayer(getSubDir(), layerName);
    }

    public void save() throws IOException {
        XStream xml = new XStream(new JDomDriver());
        if(isDecomp()) {
            getSubDir().mkdir();
            msGrid.save(getSubDir());
        }
        file = file.getAbsoluteFile();
        for(ShapeFileLayer l : infoLayers) 
            if(file.getParentFile().equals(l.getShapeFile().getParentFile()))
                l.setShapeFile(new File(l.getShapeFile().getName()));

        FileWriter fw = new FileWriter(file);
        xml.toXML(this, fw);
        fw.close();
    }


    public static Project load(File file) throws IOException {
        XStream xml = new XStream(new DomDriver());
        FileReader fr = new FileReader(file);
        Project prj = (Project)xml.fromXML(fr);
        fr.close();

        project = prj;

        project.file = file;
        if(project.coefDecomp > 0) {
            project.msGrid = MSGridBuilder.load(project.getSubDir());
            project.createGridLayer();
        }
        
        // add new rules if not already exist in this project
        for(Rule rule : RULES)
            if(!project.rules.containsKey(rule.getName()))
                project.rules.put(rule.getName(), rule);

        return project; 
    }
    
    private File getSubDir() {
        return new File(file.getParent(), "grid");
    }

    @Override
    public String toString() {
        return file != null ? file.getName() : java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Nouveau_projet");
    }

    public String getStatDecomp() {
        msGrid.addLayer("bati", DataBuffer.TYPE_BYTE, 0);
        msGrid.execute(new AbstractLayerOperation(4) {
            public void perform(Cell cell) {
                if(cell.getLayerValue(Layers.BUILD.toString()) == 1)
                    cell.setLayerValue("bati", 1);
            }
        });
        TreeMap<Double, Double> nbCell = msGrid.agregate(new SimpleAgregateOperation.COUNT(4));
        TreeMap<Double, Double> nbBati = msGrid.agregate(new SimpleAgregateOperation.SUM("bati", 4));
        msGrid.removeLayer("bati");

        StringBuilder res = new StringBuilder("Scale\tnb cell\tnb cell bati\n");
        for(Double scale : nbCell.keySet())
            res.append(String.format("%g\t%d\t%d\n", scale, nbCell.get(scale).intValue(), nbBati.get(scale).intValue()));

        return res.toString();
    }

    @Override
    public TreeNode getParent() {
        return null;
    }

    @Override
    protected List<TreeNode> getChildren() {
        List<TreeNode> vChildren = new ArrayList<TreeNode>();
        vChildren.add(new DefaultMutableTreeNode(NODE_ZONE));
        if(isDecomp()) {
            vChildren.add(new DefaultMutableTreeNode(NODE_DECOMP));
            vChildren.add(new DynamicUtilTreeNode(NODE_ANALYSE, scenarioAutos.toArray()));
            vChildren.add(new DynamicUtilTreeNode(NODE_SCENARIO, scenarios.toArray()));
        }
        
        return vChildren;
    }

    public File getDirectory() {
        return file.getParentFile();
    }

    public static Project getProject() {
        return project;
    }
    
    public static Project createProject(String name, File dir, File buildFile, TaskMonitor mon) throws Throwable {
        File directory = new File(dir, name);
        directory.mkdir();
        mon.setProgress(1);
        mon.setNote("Loading data...");
        List<DefaultFeature> buildFeatures = DefaultFeature.loadFeatures(buildFile, false);
              
        CoordinateReferenceSystem crs = new ShapefileDataStore(buildFile.toURI().toURL()).getSchema().getCoordinateReferenceSystem();
        
        mon.setNote("Saving data...");
        
        DefaultFeature.saveFeatures(buildFeatures, new File(directory, Layers.BUILD+".shp"), crs);
        Project prj = new Project(name, directory);
        prj.save();
        project = prj;
        return prj;
    }
    
    private static class DistBorderOperation extends  AbstractOperation{
        int distBorder;

        public DistBorderOperation(int distBorder) {
            super(true, 0);
            this.distBorder = distBorder;
        }

        public double getValue(Cell cell) {
            return cell.getDistBorder() < distBorder ? 0 : 1;
        }
    }

}