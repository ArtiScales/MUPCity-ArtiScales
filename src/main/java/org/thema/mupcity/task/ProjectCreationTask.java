package org.thema.mupcity.task;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.feature.simple.SimpleFeature;
import org.thema.common.swing.TaskMonitor;
import org.thema.mupcity.Project;
import org.thema.mupcity.rule.OriginDistance;

import com.vividsolutions.jts.geom.Geometry;

public class ProjectCreationTask {

	public static String NAME_BUILD_FILE = "BATI_AU.shp";
	public static String NAME_FILE_ROAD = "route_sans_chemin.shp";
	public static String NAME_FILE_FACILITY ="CS_au_besac_sirene_2012.shp";
	public static String NAME_FILE_LEISURE ="loisirs.shp";
	public static String NAME_FILE_BUS_STATION = "stations_besac_tram_2015.shp";
	public static String NAME_FILE_TRAIN = "gare_train_ICONE_docs_2015.shp";
	public static String NAME_FILE_NON_BUILDABLE =  "non_urba.shp";

	
	
	public static void main(String[] args) throws Exception {
		String name = "Project";
		String folderIn = "/home/mickael/data/mbrasebin/donnees/Maxime/1m/data0/data/";
		double width = 28303;
		double height = 21019;
		double xmin = 914760;
		double ymin = 6680157;
		double shiftX = 50;
		double shiftY = 50;
		
	 ProjectCreationTask.run(name, new File(folderIn), xmin, ymin, width, height,shiftX,shiftY);

		

		
	}

	public static File run(String name, File folderIn, double xmin, double ymin, double width, double height, double shiftX, double shiftY)
			throws Exception {
		TaskMonitor mon = new TaskMonitor.EmptyMonitor();
		

		
		//Dossier intermédiaire avec les fichiers transformées
		File folderTemp = new File(folderIn+"/tmp/");
		if(! folderTemp.exists()){
			folderTemp.mkdir();
		}
		
		
		
		File buildFile = new File(folderTemp, NAME_BUILD_FILE);		
		File roadFile = new File(folderTemp, NAME_FILE_ROAD);
		File facilityFile = new File(folderTemp, NAME_FILE_FACILITY);
		File leisureFile = new File(folderTemp, NAME_FILE_LEISURE);
		File busFile = new File(folderTemp, NAME_FILE_BUS_STATION);
		File trainFile = new File(folderTemp, NAME_FILE_TRAIN);
		File restrictFile = new File(folderTemp,NAME_FILE_NON_BUILDABLE);
		
		

		translateSHP( new File(folderIn, NAME_BUILD_FILE), buildFile,shiftX,shiftY);
		translateSHP( new File(folderIn, NAME_FILE_ROAD), roadFile,shiftX,shiftY);
		translateSHP( new File(folderIn, NAME_FILE_FACILITY), facilityFile,shiftX,shiftY);
		translateSHP( new File(folderIn, NAME_FILE_LEISURE), leisureFile,shiftX,shiftY);
		translateSHP( new File(folderIn, NAME_FILE_BUS_STATION), busFile,shiftX,shiftY);
		translateSHP( new File(folderIn, NAME_FILE_TRAIN), trainFile,shiftX,shiftY);
		translateSHP( new File(folderIn, NAME_FILE_NON_BUILDABLE), restrictFile,shiftX,shiftY);
		
		
		
		
		//Transformation des bâtiments		
		Project project = Project.createProject(name, folderTemp, buildFile, xmin, ymin, width, height, mon);
		project.setNetPrecision(0);


		
		
		boolean network = true;
		
		// Le réseau routier apparait peut être un peu moins déformé avec cette contrainte, mais ce n'est pas pour ça qu'il n'y a plus detache =0 dans fac3
		// set layers and attributes for the decomposition
		List<String> roadAttrs = Arrays.asList("Speed");// SPEED(numeric)
		project.setLayer(Project.LAYERS.get(Project.Layers.ROAD.ordinal()), roadFile, roadAttrs);
		List<String> facilityAttrs = Arrays.asList("LEVEL", "TYPE");// LEVEL(numeric),TYPE (any)
		project.setLayer(Project.LAYERS.get(Project.Layers.FACILITY.ordinal()), facilityFile, facilityAttrs);
		List<String> leisureAttrs = Arrays.asList("LEVEL", "TYPE");// LEVEL(numeric),TYPE (any)
		project.setLayer(Project.LAYERS.get(Project.Layers.LEISURE.ordinal()), leisureFile, leisureAttrs);
		List<String> emptyAttrs = Arrays.asList("");
		project.setLayer(Project.LAYERS.get(Project.Layers.BUS_STATION.ordinal()), busFile, emptyAttrs);
		project.setLayer(Project.LAYERS.get(Project.Layers.TRAIN_STATION.ordinal()), trainFile, emptyAttrs);
		project.setLayer(Project.LAYERS.get(Project.Layers.RESTRICT.ordinal()), restrictFile, emptyAttrs); //provoque un GC limit overhead
		project.setDistType((network) ? OriginDistance.NetworkDistance.class : OriginDistance.EuclideanDistance.class);
		
		project.save();


		
		return folderTemp;
	}
	
	
	
	
	
	private static void translateSHP(File fileIn, File fileOut, double shiftX, double shiftY ) throws Exception{
		ShapefileDataStore dataStore = new ShapefileDataStore(fileIn.toURI().toURL());

		AffineTransform2D translate = new AffineTransform2D(1, 0, 0, 1, shiftX, shiftY); //AffineTransform2D.getTranslateInstance(Xmouve, Ymouve);						//j'aimerais mettre direct une saloperie de shape dans translate.createTransformedShape(shape) mais je ne vois pas comment faire

		ContentFeatureCollection shpFeatures = dataStore.getFeatureSource().getFeatures();
		DefaultFeatureCollection newFeatures = new DefaultFeatureCollection();
		Object[] nouveaux = new Object[shpFeatures.size()];
		int cpt = 0;
		SimpleFeatureIterator iterator = shpFeatures.features();
		try {
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				Geometry geom = (Geometry) feature.getDefaultGeometry();
				Geometry geomTransformed = JTS.transform(geom, translate);
				feature.setDefaultGeometry(geomTransformed);
				nouveaux[cpt] = feature;
				newFeatures.add(feature);
				cpt = +1;
			}
		} finally {
			iterator.close();
		}
		
     
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<>();
        params.put("url", fileOut.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

        /*
         * TYPE is used as a template to describe the file contents
         */
        newDataStore.createSchema(dataStore.getSchema());
        
        
        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(newFeatures);
                transaction.commit();

            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();

            } finally {
                transaction.close();
            }
      
        } else {
            System.out.println(typeName + " does not support read/write access");
            System.exit(1);
        }
	}
	
	
	

}
