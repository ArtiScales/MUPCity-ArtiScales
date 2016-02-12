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
import org.thema.mupcity.rule.OriginDistance.EuclideanDistance;
import org.thema.mupcity.rule.OriginDistance.NetworkDistance;
import org.thema.mupcity.scenario.ScenarioAuto;
import org.thema.mupcity.scenario.ScenarioManual;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.SchemaException;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.thema.common.JTS;
import org.thema.common.swing.TaskMonitor;
import org.thema.common.swing.tree.AbstractTreeNode;
import org.thema.data.GlobalDataStore;
import org.thema.drawshape.GridModShape;
import org.thema.drawshape.GridShape;
import org.thema.drawshape.RectModShape;
import org.thema.data.feature.*;
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
import org.thema.mupcity.evaluation.*;
import org.thema.mupcity.scenario.Scenario;

/**
 * This class represents a MupCity project.
 * It is serialized for saving project parameters.
 * It implements also TreeNode for viewing the project tree in the MainFrame.
 * 
 * @author Gilles Vuidel
 */
public class Project extends AbstractTreeNode {

    public static final String LEVEL_FIELD = "level"; //NOI18N
    public static final String TYPE_FIELD = "type"; //NOI18N
    public static final String SPEED_FIELD = "speed"; //NOI18N
    
    /**
     * Predefined layers used by some rules
     */
    public enum Layers {
        BUILD, ROAD, TRAIN_STATION, BUS_STATION, FACILITY, LEISURE, RESTRICT
    }
    
    /**
     * Predefined layers definition
     */
    public static final List<LayerDef> LAYERS = Arrays.asList(
        new LayerDef(Layers.BUILD, java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("BUILDINGS"), new FeatureStyle(Color.gray, Color.black)),
        new LayerDef(Layers.ROAD, java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("ROAD NETWORK"), new LineStyle(Color.black), SPEED_FIELD, Number.class),
        new LayerDef(Layers.BUS_STATION, java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("BUS STATIONS"), new PointStyle(Color.black, Color.red)), 
        new LayerDef(Layers.TRAIN_STATION, java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("TRAIN STATIONS"), new PointStyle(Color.black, Color.red)), 
        new LayerDef(Layers.FACILITY, java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Facilities"), new PointStyle(Color.yellow.darker()), LEVEL_FIELD, Number.class, TYPE_FIELD, Object.class),
        new LayerDef(Layers.LEISURE, java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("LEISURE"), new PointStyle(Color.blue), LEVEL_FIELD, Number.class, TYPE_FIELD, Object.class),
        new LayerDef(Layers.RESTRICT, java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("non-developable area"), new FeatureStyle(Color.orange, Color.gray))
        );
    
    /** Grid layer name for initial build */
    public static final String BUILD = "build"; //NOI18N
    /** Grid layer name for initial build density */
    public static final String BUILD_DENS = "build_dens"; //NOI18N
    /** Grid layer name for morphological rule */
    public static final String MORPHO_RULE = "morpho"; //NOI18N
    /** Grid layer name for zone */
    public static final String ZONE = "zone"; //NOI18N
    /** Grid layer name for restricted area density */
    public static final String NOBUILD_DENS = "no_build_dens"; //NOI18N

    public static final String EVAL = "eval"; //NOI18N
    public static final String SIMUL = "analyse"; //NOI18N
    public static final String SCENARIO = "scenario"; //NOI18N
    
    public static final String NODE_ZONE = java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Zone_etude");
    public static final String NODE_DECOMP = java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Decomposition");
    public static final String NODE_SCENARIO = java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Scenarios");
    public static final String NODE_ANALYSE = java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Analyses");

    public static final TreeMap<Object, Color> COLOR_MAP = new TreeMap<>();
    static {
        COLOR_MAP.put(-1.0, new Color(220, 220, 220)); COLOR_MAP.put(0.0, Color.WHITE);
        COLOR_MAP.put(1.0, new Color(100, 100, 100)); COLOR_MAP.put(2.0, new Color(0, 0, 0));
    }

    public static final List<? extends Rule> RULES = Arrays.asList(new MorphoRule(), new RoadRule(), 
            new Facility12Rule(1), new Facility12Rule(2), new Facility3Rule(), new PTRule(),
            new LeisureRule(1), new LeisureRule(2), new LeisureRule(3));

    /*
     *   Projects parameters stored in xml file
     */
    private RectModShape bounds;// bounds of the decomposition    
    private ArrayList<ShapeFileLayer> infoLayers;// used layers
    private LinkedHashMap<String, Rule> rules;// rules: to be defined before decomposition
    private Class<? extends OriginDistance> distType;// euclidian, network distance, etc.
    private double netPrecision;// for the topological construction of the network
    private int coefDecomp;// decomposition coefficient
    private List<ScenarioAuto> scenarioAutos;
    private List<ScenarioManual> scenarios;
    private List<Evaluator> evaluators;
    
    
    /*
     * Projects data that are not serialized in xml file.
     */
    private transient File file;
    
    private transient MSGridBuilder<SquareGridExtent> msGrid;
    private transient DefaultGroupLayer decompLayer;
    private transient DefaultGroupLayer infoLayer;
    
    private transient HashMap<String, DefaultFeatureCoverage<DefaultFeature>> coverages;

    private transient SpatialGraph spatialGraph;
    
    /** 
     * Creates a new instance of Project.
     * @param name project's name
     * @param dir directory where creating the project
     */
    private Project(String name, File dir)  {
    	this(name, dir, 0, 0, 0, 0);
    	// TODO clean that up
    	Envelope env = getCoverage(Layers.BUILD).getEnvelope();
    	this.bounds = new GridModShape(new Rectangle2D.Double(-0.5, -0.5, 1, 1), new AffineTransform(env.getWidth(), 0, 0, env.getHeight(), env.centre().x, env.centre().y), 1);
    }
    /** 
     * Creates a new instance of Project.
     * @param name project's name
     * @param dir directory where creating the project
     */
    private Project(String name, File dir, Envelope env)  {
    	this(name, dir, env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight());
    }
    /** 
     * Creates a new instance of Project.
     * The envelope is expanded by a given distance in all directions.
     * @param name project's name
     * @param dir directory where creating the project
     * @param minX
     * @param minY
     * @param width
     * @param height
     */
    private Project(String name, File dir, double minX, double minY, double width, double height) {
        file = new File(dir, name + ".xml");
        Envelope env = new Envelope(minX, minX + width, minY, minY + height);
        bounds = new GridModShape(new Rectangle2D.Double(-0.5, -0.5, 1, 1), new AffineTransform(env.getWidth(), 0, 0, env.getHeight(), env.centre().x, env.centre().y), 1);
        rules = new LinkedHashMap<>();
        for(Rule rule : RULES) {
            rules.put(rule.getName(), rule);
        }        
        scenarioAutos = new ArrayList<>();
        scenarios = new ArrayList<>();
        distType = OriginDistance.EuclideanDistance.class;
        infoLayers = new ArrayList<>();
    }

    /**
     * Sets the bounds from another project.
     * @param zonePrj another project
     * @throws IllegalStateException if the project has already decomposition
     */
    public void setBounds(Project zonePrj) {
        if(isDecomp()) {
            throw new IllegalStateException("Cannot change bounds when the decomposition is already done.");
        }
        bounds = zonePrj.bounds;
    }
    
    /**
     * Creates the multiscale grid and calculates the rules.
     * @param exp the factor between scale
     * @param maxSize the max size of cells
     * @param minSize the min size of cells
     * @param seuilDensBuild the minimum of build density for a cell to be of state built
     * @throws IOException 
     */
    public void decomp(int exp, double maxSize, double minSize, final double seuilDensBuild) throws IOException {
        int nbRule = 0;
        for(Rule rule : rules.values()) {
            if(rule.isUsable(this)) {
                nbRule++;
            }
        }
    	this.decomp(exp, maxSize, minSize, seuilDensBuild, new TaskMonitor(null, "Decomposition", "initialisation...", 0, nbRule+3));
    }
    /**
     * Creates the multiscale grid and calculates the rules.
     * @param exp the factor between scale
     * @param maxSize the max size of cells
     * @param minSize the min size of cells
     * @param seuilDensBuild the minimum of build density for a cell to be of state built
     * @throws IOException 
     */
    public void decomp(int exp, double maxSize, double minSize, final double seuilDensBuild, TaskMonitor monitor) throws IOException {
        monitor.setMillisToPopup(0);
        monitor.setMillisToDecideToPopup(0);

        AffineTransform trans = bounds.getTransform();
        double width = XAffineTransform.getScaleX0(trans);
        double height = XAffineTransform.getScaleY0(trans);

        msGrid = new MSGridBuilder(JTS.geomFromRect(new Rectangle2D.Double(
                    trans.getTranslateX()-width/2,
                    trans.getTranslateY()-height/2, width, height)),
                minSize, maxSize, exp, 4, new SquareGridFactory());
        msGrid.setCrs(getCRS());
        monitor.setNote("Create grid...");
        coefDecomp = exp;
        Envelope env = ((GridExtent)msGrid.getGrid(msGrid.getResolutions().first())).getInternalEnvelope();
        bounds = new RectModShape(new Rectangle2D.Double(-0.5, -0.5, 1, 1), new AffineTransform(env.getWidth(), 0, 0, env.getHeight(), env.centre().x, env.centre().y));
        monitor.incProgress(1);
        msGrid.addDynamicLayer(ZONE, new DistBorderOperation(4));
        monitor.setNote("Create grid... build");
        msGrid.addLayer(BUILD_DENS, DataBuffer.TYPE_FLOAT, Float.NaN);
        msGrid.execute(new SimpleCoverageOperation(SimpleCoverageOperation.DENSITY, BUILD_DENS, getCoverage(Layers.BUILD)), true);
        msGrid.addLayer(BUILD, DataBuffer.TYPE_SHORT, 0.0);
        msGrid.execute(new AbstractLayerOperation() {
            @Override
            public void perform(Cell cell) {
                double dens = cell.getLayerValue(BUILD_DENS);
                if(dens > 0 && dens <= seuilDensBuild) {
                    cell.setLayerValue(BUILD, -1);
                }
                if(dens > seuilDensBuild) {
                    Cell parent = ((MSCell)cell).getParent();
                    if(parent == null || parent.getLayerValue(BUILD) == 1) {
                        cell.setLayerValue(BUILD, 1);
                    } else {
                        cell.setLayerValue(BUILD, -1);
                    }
                }
            }
        }, true);
        if(isLayerExist(Layers.RESTRICT)) {
        	monitor.incProgress(1);
        	monitor.setNote("Create grid... restrict");
            getMSGrid().addLayer(NOBUILD_DENS, DataBuffer.TYPE_FLOAT, Float.NaN);
            getMSGrid().execute(new SimpleCoverageOperation(SimpleCoverageOperation.DENSITY, NOBUILD_DENS, getCoverage(Layers.RESTRICT)), true);
        }
        long t = System.currentTimeMillis();
        for(Rule rule : rules.values()) {
            if(rule.isUsable(this)) {
            	monitor.incProgress(1);
            	monitor.setNote("Create grid... rule " + rule.getFullName());
                rule.createRule(this);
                System.out.println(rule.getFullName() + " : " + (System.currentTimeMillis() - t) / 60000.0 + " minutes");
                t = System.currentTimeMillis();
            }
        }
        createDecompLayer();
        monitor.close();
    }
    
    /**
     * 
     * @param origin the starting polygon 
     * @param maxCost the maximum distance or Double.NaN for no limit
     * @return a NetworkDistance or an EuclideanDistance depending on the projects parameters
     */
    public OriginDistance getDistance(Polygon origin, double maxCost) {
        if(distType.equals(EuclideanDistance.class)) {
            return new EuclideanDistance(origin);
        } else {
            return new NetworkDistance(getSpatialGraph(), origin, maxCost);
        }
    }
    
    /**
     * Creates the graph network from the ROAD layer if it's not been already created and returns it.
     * @return the graph network
     * @throws IllegalStateException if no road layer is present in this project
     */
    public synchronized SpatialGraph getSpatialGraph() {
        if(!isLayerExist(Layers.ROAD)) {
            throw new IllegalStateException("No road network layer is present");
        }
        if(spatialGraph == null) {
            spatialGraph = new SpatialGraph(getCoverage(Layers.ROAD).getFeatures(), 
                    netPrecision == 0 ? null : new GeometryPrecisionReducer(new PrecisionModel(1/netPrecision)));
        }
        return spatialGraph;
    }

    /**
     * @return the implementation class for distance calculation : NetworkDistance or EuclideanDistance
     */
    public Class<? extends OriginDistance> getDistType() {
        return distType;
    }

    /**
     * Sets the implementation class for distance calculation.
     * @param distType the new implementation of distance class : NetworkDistance or EuclideanDistance
     */
    public void setDistType(Class<? extends OriginDistance> distType) {
        this.distType = distType;
    }

    /**
     * @return the spatial precision for building the network graph, 0 for exact precision
     */
    public double getNetPrecision() {
        return netPrecision;
    }

    /**
     * Set the spatial precision for building the network graph
     * @param netPrecision the spatial precision, 0 for exact precision
     */
    public void setNetPrecision(double netPrecision) {
        this.netPrecision = netPrecision;
        spatialGraph = null;
    }

    /**
     * @return the factor between 2 scales of the grid
     */
    public int getCoefDecomp() {
        return coefDecomp;
    }

    /**
     * Removes previous decomposition.
     * Removes all scenarios and the multiscale grid.
     */
    public void removeDecomp() {
        for(ScenarioManual sce : new ArrayList<>(scenarios)) {
            removeScenario(sce);
        }
        for(ScenarioAuto anal : new ArrayList<>(scenarioAutos)) {
            removeScenario(anal);
        }
        coefDecomp = 0;
        msGrid = null;
    }
    
    /**
     * Adds a shepfile layer to the project.
     * @param shapeFileLayer the shapefile layer to add
     */
    public void addInfoLayer(ShapeFileLayer shapeFileLayer) {
        getInfoLayer();
        infoLayer.addLayerFirst(shapeFileLayer);
        infoLayers.add(shapeFileLayer);
    }

    /**
     * @return all automatic scenarios of the project, may be empty.
     */
    public List<ScenarioAuto> getScenarioAutos() {
        return scenarioAutos;
    }
    
    /**
     * @return all manual scenarios of the project, may be empty.
     */
    public List<ScenarioManual> getScenarios() {
        return scenarios;
    }

    /**
     * @param name the name of the scenario
     * @return the automatic scenario or null if it does not exist
     */
    public ScenarioAuto getScenarioAuto(String name) {
        for(ScenarioAuto anal : getScenarioAutos()) {
            if(anal.toString().equals(name)) {
                return anal;
            }
        }
        return null;
    }

    /**
     * @param name the name of the scenario
     * @return the manual scenario or null if it does not exist
     */
    public ScenarioManual getScenario(String name) {
        for(ScenarioManual sce : scenarios) {
            if(sce.toString().equals(name)) {
                return sce;
            }
        }
        return null;
    }
    
    /**
     * @return the projects rules
     */
    public Collection<Rule> getRules() {
        return rules.values();
    }

    /**
     * Calculates the automatic scenario and add it to the project.
     * @param analyse the scenario
     */
    public void performScenarioAuto(final ScenarioAuto analyse) {
        analyse.perform(msGrid);
        scenarioAutos.add(analyse);
    }

    /**
     * Creates the manual scenario and add it to the project
     * @param name the name
     * @param nMax the number of cells which can be built, between 1 and 9
     * @param ahp the ahp matrix for weighting rules
     * @param mean true for agregating rules by average, false for yager agregation
     */
    public void createManualScenario(String name, int nMax, AHP ahp, boolean mean) {
        ScenarioManual sce = new ScenarioManual(name, nMax, ahp, msGrid, mean);
        scenarios.add(sce);
    }
    
    /**
     * @return the set of grid resolution in descending order
     * @throws IllegalStateException if the project has no decomposition
     */
    public NavigableSet<Double> getResolutions() {
        if(!isDecomp()) {
            throw new IllegalStateException("Project has no decomposition");
        }
            
        return msGrid.getResolutions();
    }
    
    /**
     * @return the group layer of decomposition layers
     */
    public GroupLayer getDecompLayer() {
        return decompLayer;
    }
    
    /**
     * Creates a grouplayer containing grid shapes for each resolution (scale) of the multiscale grid.
     * @return a grouplayer of grid shapes
     */
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
            for(int j = 0; j < 3; j++) {
                col[j] = (int) (col[j] + dcol[j]);
            }
        }

        return gridLayers;
    }
    
    /**
     * @return true if the project has already a decomposition (ie. a multiscale grid)
     */
    public boolean isDecomp() {
        return msGrid != null;
    }

    /**
     * @return the multiscale grid, may be null if no decomposition has been calculated.
     */
    public MSGridBuilder<SquareGridExtent> getMSGrid() {
        return msGrid;
    }
    
    /**
     * @return the rectangular bounds of the zone for the grid
     */
    public Rectangle2D getBounds() {
        return bounds.getBounds();
    }
    
    /**
     * @return the rectangular shape representing the bounds of the zone for the grid
     */
    public RectModShape getRectShape() {
        return bounds;
    }
    
    /**
     * @param name the predefined layer
     * @return true if the predefined layer exists in this project
     */
    public boolean isLayerExist(Layers name) {
        return new File(getDirectory(), name.toString()+".shp").exists();
    }

    /**
     * @return true if the project has a layer for restricted area and the decomposition has been already calculated
     */
    public boolean hasNoBuild() {
        return msGrid != null && msGrid.getLayers().contains(NOBUILD_DENS);
    }
    
    /**
     * The CRS of the project is defined by the build layer set at project creation.
     * @return the CRS associated with this project
     */
    public CoordinateReferenceSystem getCRS() throws IOException {
        return GlobalDataStore.getCRS(new File(getDirectory(), Layers.BUILD.toString() + ".shp"));
    }
    
    /**
     * Loads data from a predefined layer.
     * When the data are loaded, they are kept in cache for future retrieval.
     * @param name the predefined layer
     * @return the coverage data layer
     */
    public synchronized DefaultFeatureCoverage<DefaultFeature> getCoverage(Layers name) {
        if(coverages == null) {
            coverages = new HashMap<>();
        }
        if(!coverages.containsKey(name.toString())) {
            try {
                coverages.put(name.toString(), new DefaultFeatureCoverage<>(DefaultFeature.loadFeatures(new File(getDirectory(), name.toString()+".shp"), false)));
            } catch (IOException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return coverages.get(name.toString());
    }
    
    /**
     * Loads data from a predefined layer and filtering by the level field.
     * When the data are loaded, they are kept in cache for future retrieval.
     * @param layer the predefined layer
     * @param level the level filter
     * @return the coverage data layer
     */
    public synchronized DefaultFeatureCoverage<DefaultFeature> getCoverageLevel(Layers layer, final int level) {
        if(coverages == null) {
            coverages = new HashMap<>();
        }
        String levelName = layer.toString() + level;
        if(!coverages.containsKey(levelName)) {
            coverages.put(levelName, new DefaultFeatureCoverage<>(getCoverage(layer).getFeatures(new FeatureFilter() {
                            @Override
                            public boolean accept(Feature f) {
                                return ((Number)f.getAttribute(Project.LEVEL_FIELD)).intValue() == level;
                            }
                        })));
        }
        
        return coverages.get(levelName);
    }
    
    /**
     * Creates a FeatureGetter from a predefined layer.
     * Does not load the data, until the method {@link FeatureGetter.getFeatures} is called
     * @param name the predefined layer
     * @return a FeatureGetter for the predefined layer
     */
    public FeatureGetter<DefaultFeature> getLayerFeatures(final Layers name) {
        return new FeatureGetter<DefaultFeature>() {
            @Override
            public Collection<DefaultFeature> getFeatures() {
                    return getCoverage(name).getFeatures();
            }
        };
    }

    /**
     * Sets the shapefile data associated with a predefined layer.
     * @param layer the predefined layer
     * @param file the shapefile containing the data
     * @param attrs some shapefile fields name if needed for the layer, may be empty
     * @throws IOException
     * @throws SchemaException 
     */
    public void setLayer(LayerDef layer, File file, List<String> attrs) throws IOException, SchemaException {
    	this.setLayer(layer, file, attrs, new TaskMonitor(null, "Create layer", "", 0, 2));
    }

    /**
     * Sets the shapefile data associated with a predefined layer.
     * @param layer the predefined layer
     * @param file the shapefile containing the data
     * @param attrs some shapefile fields name if needed for the layer, may be empty
     * @throws IOException
     * @throws SchemaException 
     */
    public void setLayer(LayerDef layer, File file, List<String> attrs, TaskMonitor mon) throws IOException, SchemaException {
        mon.setMillisToDecideToPopup(0);
        if(coverages != null) { // remove from cache if exists
            coverages.remove(layer.getName());
        }
        mon.setProgress(0);
        mon.setNote("Loading...");
        List<DefaultFeature> features = DefaultFeature.loadFeatures(file, false);
        if(!layer.getAttrNames().isEmpty()) {
            for(String attr : layer.getAttrNames()) {
                if(!features.get(0).getAttributeNames().contains(attr)) {
                    DefaultFeature.addAttribute(attr, features, null);
                }
            }
            for(DefaultFeature f : features) {
                for(int i = 0; i < layer.getAttrNames().size(); i++) {
                    f.setAttribute(layer.getAttrNames().get(i), f.getAttribute(attrs.get(i)));
                }
            }
        }
        mon.incProgress(1);
        mon.setNote("Saving...");
        DefaultFeature.saveFeatures(features, new File(getDirectory(), layer.getName() + ".shp"));
        mon.close();
    }

    /**
     * Removes a scenario from the project.
     * Removes grid layers associated with this scenario.
     * @param scenario the scenario to remove
     */
    public void removeScenario(Scenario scenario) {
        for(Layer l : scenario.getLayers(getMSGrid()).getLayers()) {
            msGrid.removeLayer(l.getName());
        }
        if(scenario instanceof ScenarioAuto) {
            scenarioAutos.remove((ScenarioAuto)scenario);
        } else {
            scenarios.remove((ScenarioManual)scenario);
        }
    }

    /**
     * @return all available evaluators
     */
    public List<Evaluator> getEvaluators() {
        if(evaluators == null) {
            evaluators = Arrays.asList((Evaluator)new MeanWhiteEvaluator(),
                    new NbCellOnEnvelopeEvaluator(), 
                    new DistEnvelopeEvaluator(),
                    
                    new DistMinAmenEvaluator(this, Layers.FACILITY, 1, new double[] {0.0, 1000.0}, new double[] {1.0, 0.001}),
                    new DistMinAmenEvaluator(this, Layers.FACILITY, 2, new double[] {0.0, 2000.0}, new double[] {1.0, 0.001}), 
                    
                    new DistMinAmenEvaluator(this, Layers.LEISURE, 1, new double[] {0.0, 1000.0}, new double[] {1.0, 0.001}), 
                    new DistMinAmenEvaluator(this, Layers.LEISURE, 2, new double[] {0.0, 2000.0}, new double[] {1.0, 0.001}),
                    new DistMinAmenEvaluator(this, Layers.LEISURE, 3, new double[] {0.0, 5000.0}, new double[] {1.0, 0.001}),
                    
                    new NbAmenEvaluator(this, Layers.FACILITY, 1, 1000, new double[] {0.0, 5}, new double[] {0.001, 1.0}),
                    new NbAmenEvaluator(this, Layers.FACILITY, 2, 2000, new double[] {0.0, 10}, new double[] {0.001, 1.0}),
                    
                    new NbAmenEvaluator(this, Layers.LEISURE, 1, 1000, new double[] {0.0, 2}, new double[] {0.001, 1.0}),
                    new NbAmenEvaluator(this, Layers.LEISURE, 2, 2000, new double[] {0.0, 5}, new double[] {0.001, 1.0}),
                    
                    new NbAmenEvaluator(this, Layers.BUS_STATION, -1, 2000, new double[] {0.0, 5}, new double[] {0.001, 1.0}),
                    new DistMinAmenEvaluator(this, Layers.TRAIN_STATION, -1, new double[] {0.0, 2000.0}, new double[] {1.0, 0.001}),
                    
                    new DistMinTypeAmenEvaluator(this, Layers.FACILITY, 1, new double[] {0.0, 1000.0}, new double[] {1.0, 0.001}),
                    new DistMinTypeAmenEvaluator(this, Layers.FACILITY, 2, new double[] {0.0, 2000.0}, new double[] {1.0, 0.001}),
                    new DistMinTypeAmenEvaluator(this, Layers.FACILITY, 3, new double[] {0.0, 5000.0}, new double[] {1.0, 0.001}),
                    
                    new DistMinTypeAmenEvaluator(this, Layers.LEISURE, 1, new double[] {0.0, 1000.0}, new double[] {1.0, 0.001}),
                    new DistMinTypeAmenEvaluator(this, Layers.LEISURE, 2, new double[] {0.0, 2000.0}, new double[] {1.0, 0.001})
            );
        }
        return evaluators;
    }

    /**
     * Creates if not already created the group layers for user layers 
     * @return the group layer for adding user layer
     */
    public synchronized DefaultGroupLayer getInfoLayer() {
        if(infoLayer == null) {
            infoLayer = new DefaultGroupLayer(java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Infos"));
            for(ShapeFileLayer l : infoLayers) {
                infoLayer.addLayerFirst(l);
            }
        }
        return infoLayer;
    }
    
    /**
     * Create the multiscale grid layers for viewing in map.
     */
    private void createDecompLayer() {
        decompLayer = new DefaultGroupLayer(java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Decomposition"));
        
        decompLayer.addLayerFirst(createMultiscaleLayers(BUILD, new UniqueColorTable(COLOR_MAP), 
                java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Built-up"), getMSGrid()));

        for(Rule rule : rules.values()) {
            if(rule.isUsable(this)) {
                decompLayer.addLayerLast(createMultiscaleLayers(rule.getName(), null, rule.getFullName(), getMSGrid()));
            }
        }
    }
    
    /**
     * Creates a group layer for one multscale grid layer.
     * This group layer contains one layer for each scale present in the multiscale grid.
     * @param layerName the layer name in the multiscale grid
     * @param colors the color map
     * @param viewLayerName the name shown for this layer
     * @param msGrid the multiscale grid
     * @return the new group layers
     */
    public static GroupLayer createMultiscaleLayers(String layerName, ColorBuilder colors, String viewLayerName, MSGridBuilder<? extends MSGrid> msGrid) {
        DefaultGroupLayer gl = new DefaultGroupLayer(viewLayerName);

        int i = msGrid.getResolutions().size();
        int [] col = {Color.ORANGE.getRed(), Color.ORANGE.getGreen(), Color.ORANGE.getBlue()};
        double dcol [] = {(200.0-col[0])/i, (10.0-col[1])/i, (10.0-col[2])/i};
        
        for(Double res : msGrid.getResolutions()) {
            if(msGrid.getGrid(res).getLayer(layerName) == null) {
                continue;
            }
            RasterStyle style = colors == null ? new RasterStyle(ColorRamp.RAMP_INVGRAY)
                : new RasterStyle(colors, false);
            RasterLayer l = new RasterLayer(String.format("%g", res),
                new RasterShape(msGrid.getGrid(res).getLayer(layerName).getImage().getData(),
                    org.geotools.geometry.jts.JTS.getEnvelope2D(msGrid.getGrid(res).getEnvelope(), 
                        msGrid.getCrs()).getBounds2D(), style));
            l.setCRS(msGrid.getCrs());
            l.setVisible(false);
            l.setDrawLegend(false);
            gl.addLayerFirst(l);
            i--;
            for(int j = 0; j < 3; j++) {
                col[j] = (int) (col[j] + dcol[j]);
            }
        }
        return gl;
    }
    
    /**
     * Save a layer of the grid.
     * By default, the project saves all grid layers. This method is used for manual scenario to avoid to save all layers.
     * 
     * @param layerName the layer name in the multiscale grid
     * @throws IOException 
     */
    public void saveGridLayer(String layerName) throws IOException {
        msGrid.saveLayer(getGridDir(), layerName);
    }

    /**
     * Reload a layer of the grid.
     * This method is used for cancelling modification of a manual scenario.
     * @param layerName the layer name in the multiscale grid
     * @throws IOException 
     */
    public void reloadGridLayer(String layerName) throws IOException {
        msGrid.reloadLayer(getGridDir(), layerName);
    }

    /**
     * Saves the project.
     * Saves the xml project file and the multiscale grid if the decomposition is already done.
     * @throws IOException 
     */
    public void save() throws IOException {
        XStream xml = new XStream(new JDomDriver());
        if(isDecomp()) {
            getGridDir().mkdir();
            msGrid.save(getGridDir());
        }
        file = file.getAbsoluteFile();
        for(ShapeFileLayer l : infoLayers) { 
            if(file.getParentFile().equals(l.getShapeFile().getParentFile())) {
                l.setShapeFile(new File(l.getShapeFile().getName()));
            }
        }

        try (FileWriter fw = new FileWriter(file)) {
            xml.toXML(this, fw);
        }
    }

    /**
     * Loads a project.
     * @param file the xml project file
     * @return the loaded project
     * @throws IOException 
     */
    public static Project load(File file) throws IOException {
        XStream xml = new XStream(new DomDriver());
        Project project;
        try (FileReader fr = new FileReader(file)) {
            project = (Project)xml.fromXML(fr);
        }

        project.file = file;
        if(project.coefDecomp > 0) {
            project.msGrid = MSGridBuilder.load(project.getGridDir());
            project.createDecompLayer();
        }
        
        // add new rules if not already exist in this project
        for(Rule rule : RULES) {
            if(!project.rules.containsKey(rule.getName())) {
                project.rules.put(rule.getName(), rule);
            }
        }

        return project; 
    }
    
    private File getGridDir() {
        return new File(file.getParent(), "grid");
    }

    @Override
    public String toString() {
        return file != null ? file.getName() : java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("Nouveau_projet");
    }

    /**
     * @return human readable stats on the decomposition
     */
    public String getStatDecomp() {
        msGrid.addLayer("bati", DataBuffer.TYPE_BYTE, 0);
        msGrid.execute(new AbstractLayerOperation(4) {
            @Override
            public void perform(Cell cell) {
                if(cell.getLayerValue(Layers.BUILD.toString()) == 1) {
                    cell.setLayerValue("bati", 1);
                }
            }
        });
        TreeMap<Double, Double> nbCell = msGrid.agregate(new SimpleAgregateOperation.COUNT(4));
        TreeMap<Double, Double> nbBati = msGrid.agregate(new SimpleAgregateOperation.SUM("bati", 4));
        msGrid.removeLayer("bati");

        StringBuilder res = new StringBuilder("Scale\tnb cell\tnb cell bati\n");
        for(Double scale : nbCell.keySet()) {
            res.append(String.format("%g\t%d\t%d\n", scale, nbCell.get(scale).intValue(), nbBati.get(scale).intValue()));
        }

        return res.toString();
    }

    /**
     * Project tree has no parent.
     * @return null
     */
    @Override
    public TreeNode getParent() {
        return null;
    }

    /**
     * @return the children nodes of the tree project
     */
    @Override
    protected List<TreeNode> getChildren() {
        List<TreeNode> vChildren = new ArrayList<>();
        vChildren.add(new DefaultMutableTreeNode(NODE_ZONE));
        if(isDecomp()) {
            vChildren.add(new DefaultMutableTreeNode(NODE_DECOMP));
            vChildren.add(new DynamicUtilTreeNode(NODE_ANALYSE, scenarioAutos.toArray()));
            vChildren.add(new DynamicUtilTreeNode(NODE_SCENARIO, scenarios.toArray()));
        }
        
        return vChildren;
    }

    /**
     * @return project directory
     */
    public File getDirectory() {
        return file.getParentFile();
    }

    /**
     * Creates a new project and saves it.
     * @param name the name of the project
     * @param dir the parent directory of the project
     * @param buildFile the shapefile containing the buildings
     * @param mon UI monitor
     * @return the new project
     * @throws IOException
     * @throws SchemaException 
     */
    public static Project createProject(String name, File dir, File buildFile, double origX, double origY, double width, double height, TaskMonitor mon) throws IOException, SchemaException {
        File directory = new File(dir, name);
        directory.mkdir();
        if (mon != null) {
        	mon.setProgress(1);
        	mon.setNote("Loading data...");
        }
        List<DefaultFeature> buildFeatures = DefaultFeature.loadFeatures(buildFile, false);
              
        CoordinateReferenceSystem crs = new ShapefileDataStore(buildFile.toURI().toURL()).getSchema().getCoordinateReferenceSystem();
        
        if (mon != null) {
        	mon.setNote("Saving data...");
        }
        
        DefaultFeature.saveFeatures(buildFeatures, new File(directory, Layers.BUILD+".shp"), crs);
        Project prj = new Project(name, directory, origX, origY, width, height);
        prj.save();

        return prj;
    }
    
    /**
     * Binary operation returning 0 if the cell distance to the border is less than a given distance, 1 otherwise
     */
    private static class DistBorderOperation extends AbstractOperation {
        private int distBorder;

        /**
         * Creates a new DistBorderOperation with given distance from the border
         * @param distBorder the distance in number of cell
         */
        public DistBorderOperation(int distBorder) {
            super(true, 0);
            this.distBorder = distBorder;
        }

        @Override
        public double getValue(Cell cell) {
            return cell.getDistBorder() < distBorder ? 0 : 1;
        }
    }

}