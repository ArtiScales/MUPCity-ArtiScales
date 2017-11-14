package org.thema.mupcity.task;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
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
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.feature.simple.SimpleFeature;
import org.thema.common.swing.TaskMonitor;
import org.thema.mupcity.Project;
import org.thema.mupcity.rule.OriginDistance;

import com.google.common.io.Files;
import com.vividsolutions.jts.geom.Geometry;

public class ProjectCreationTask {

	public static void main(String[] args) throws Exception {
		
		Map<String,String> dataHT = new Hashtable<String,String>() ;
		
		//Data1.1
		dataHT.put("name", "Data1");
		dataHT.put("build", "batimentPro.shp");
		dataHT.put("road", "routePro.shp");
		dataHT.put("fac", "servicePro.shp");
		dataHT.put("lei", "loisirsPro.shp");
		dataHT.put("ptTram", "tramPro.shp");
		dataHT.put("ptTrain", "trainPro.shp");
		dataHT.put("nU", "nonUrbaPro.shp");
	
		//Data1.1
		dataHT.put("name", "Data1.1");
		dataHT.put("build", "batimentPro.shp");
		dataHT.put("road", "routePro.shp");
		dataHT.put("fac", "servicePro.shp");
		dataHT.put("lei", "loisirsPro.shp");
		dataHT.put("ptTram", "tramPro.shp");
		dataHT.put("ptTrain", "trainPro.shp");
		dataHT.put("nU", "nonUrbaPhyPro.shp");
		
		//Data1.2
		dataHT.put("name", "Data1.2");
		dataHT.put("build", "batimentPro.shp");
		dataHT.put("road", "routePro.shp");
		dataHT.put("fac", "servicePro.shp");
		dataHT.put("lei", "loisirsPro.shp");
		dataHT.put("ptTram", "tramPro.shp");
		dataHT.put("ptTrain", "trainPro.shp");	
		
		//Data2
		dataHT.put("name", "Data2");
		dataHT.put("build", "batimentSys.shp");
		dataHT.put("road", "routeSys.shp");
		dataHT.put("fac", "serviceSys.shp");
		dataHT.put("lei", "loisirsSys.shp");
		dataHT.put("ptTram", "tramSys.shp");
		dataHT.put("ptTrain", "trainSys.shp");
		dataHT.put("nU", "nonUrbaSys.shp");
	
		//Data2.1
		dataHT.put("name", "Data2.1");
		dataHT.put("build", "batimentSys.shp");
		dataHT.put("road", "routeSys.shp");
		dataHT.put("fac", "serviceSys.shp");
		dataHT.put("lei", "loisirsSys.shp");
		dataHT.put("ptTram", "tramSys.shp");
		dataHT.put("ptTrain", "trainSys.shp");
		dataHT.put("nU", "nonUrbaPhySys.shp");
		
		//Data2.2
		dataHT.put("name", "Data2.2");
		dataHT.put("build", "batimentSys.shp");
		dataHT.put("road", "routeSys.shp");
		dataHT.put("fac", "serviceSys.shp");
		dataHT.put("lei", "loisirsSys.shp");
		dataHT.put("ptTram", "tramSys.shp");
		dataHT.put("ptTrain", "trainSys.shp");
		
		//Data3
		dataHT.put("name", "Data3");
		dataHT.put("build", "batimentPro.shp");
		dataHT.put("road", "routeSys.shp");
		dataHT.put("fac", "servicePro.shp");
		dataHT.put("lei", "loisirsPro.shp");
		dataHT.put("ptTram", "tramPro.shp");
		dataHT.put("ptTrain", "trainPro.shp");
		dataHT.put("nU", "nonUrbaPro.shp");
		
		//Data4
		dataHT.put("name", "Data4");
		dataHT.put("build", "batimentPro.shp");
		dataHT.put("road", "routePro.shp");
		dataHT.put("fac", "servicePro.shp");
		dataHT.put("lei", "loisirsSys.shp");
		dataHT.put("ptTram", "tramPro.shp");
		dataHT.put("ptTrain", "trainPro.shp");
		dataHT.put("nU", "nonUrbaPro.shp");
		
		//Data5
		dataHT.put("name", "Data5");
		dataHT.put("build", "batimentPro.shp");
		dataHT.put("road", "routePro.shp");
		dataHT.put("fac", "serviceSys.shp");
		dataHT.put("lei", "loisirsPro.shp");
		dataHT.put("ptTram", "tramPro.shp");
		dataHT.put("ptTrain", "trainPro.shp");
		dataHT.put("nU", "nonUrbaPro.shp");
		
		String name = "Project";
		File folderIn = new File("./data/");
		File folderOut = new File("./result/");
		double width = 28303;
		double height = 21019;
		double xmin = 914760;
		double ymin = 6680157;
		double shiftX = 50;
		double shiftY = 50;
		
		boolean useNU = true;

		ProjectCreationTask.run(name, folderIn, folderOut, xmin, ymin, width, height, shiftX, shiftY, useNU,dataHT);
	}

	public static File run(String name, File folderIn, File folderOut, double xmin, double ymin, double width,
			double height, double shiftX, double shiftY, boolean useNU, Map<String, String> dataHT) throws Exception {
		TaskMonitor mon = new TaskMonitor.EmptyMonitor();
		// Dossier intermédiaire avec les fichiers transformées
		// File folderTemp = new File(folderIn + "/tmp/");
		folderOut.mkdirs();
		File buildFile = new File(folderOut, dataHT.get("build"));
		File roadFile = new File(folderOut, dataHT.get("road"));
		File facilityFile = new File(folderOut, dataHT.get("fac"));
		File leisureFile = new File(folderOut, dataHT.get("lei"));
		File busFile = new File(folderOut, dataHT.get("ptTram"));
		File trainFile = new File(folderOut, dataHT.get("ptTrain"));
		File restrictFile = new File(folderOut, dataHT.get("nU"));
		
		//put in line for the massacre
		File[] listMassacre = {buildFile,roadFile,facilityFile,leisureFile,busFile,trainFile,restrictFile};
		
		// Translation des différentes couches

		translateSHP(new File(folderIn, dataHT.get("build")), buildFile, shiftX, shiftY);
		translateSHP(new File(folderIn, dataHT.get("road")), roadFile, shiftX, shiftY);
		translateSHP(new File(folderIn, dataHT.get("fac")), facilityFile, shiftX, shiftY);
		translateSHP(new File(folderIn, dataHT.get("lei")), leisureFile, shiftX, shiftY);
		translateSHP(new File(folderIn, dataHT.get("ptTram")), busFile, shiftX, shiftY);
		translateSHP(new File(folderIn, dataHT.get("ptTrain")), trainFile, shiftX, shiftY);
		translateSHP(new File(folderIn, dataHT.get("nU")), restrictFile, shiftX, shiftY);

		// Creation du projet dans le dossier de données translaté
		Project project = Project.createProject(name, folderOut, buildFile, xmin, ymin, width, height, mon);
		project.setNetPrecision(0.1);
		// Définition des layers du projet
		boolean network = true;
		List<String> roadAttrs = Arrays.asList("Speed");// SPEED(numeric)
		project.setLayer(Project.LAYERS.get(Project.Layers.ROAD.ordinal()), roadFile, roadAttrs);
		List<String> facilityAttrs = Arrays.asList("LEVEL", "TYPE");// LEVEL(numeric),TYPE(any)
		project.setLayer(Project.LAYERS.get(Project.Layers.FACILITY.ordinal()), facilityFile, facilityAttrs);
		List<String> leisureAttrs = Arrays.asList("LEVEL", "TYPE");// LEVEL(numeric),TYPE(any)
		project.setLayer(Project.LAYERS.get(Project.Layers.LEISURE.ordinal()), leisureFile, leisureAttrs);
		List<String> emptyAttrs = Arrays.asList("");
		project.setLayer(Project.LAYERS.get(Project.Layers.BUS_STATION.ordinal()), busFile, emptyAttrs);
		project.setLayer(Project.LAYERS.get(Project.Layers.TRAIN_STATION.ordinal()), trainFile, emptyAttrs);
		if (useNU) {
			project.setLayer(Project.LAYERS.get(Project.Layers.RESTRICT.ordinal()), restrictFile, emptyAttrs);
		}
		project.setDistType((network) ? OriginDistance.NetworkDistance.class : OriginDistance.EuclideanDistance.class);
		project.save();
		
		//MASSACRE
		for (File f : listMassacre){
			CharSequence target = f.getName().subSequence(0, f.getName().length()-4);
			for (File fDelete : f.getParentFile().listFiles()){
				if (fDelete.toString().contains(target)){
				fDelete.delete();
				}
			}

		}
		return new File(folderOut, name);
	}

	private static void translateSHP(File fileIn, File fileOut, double shiftX, double shiftY) throws Exception {
		ShapefileDataStore dataStore = new ShapefileDataStore(fileIn.toURI().toURL());
		AffineTransform2D translate = new AffineTransform2D(1, 0, 0, 1, shiftX, shiftY);
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
				newDataStore.dispose();
			}
		} else {
			System.out.println(typeName + " does not support read/write access");
			System.exit(1);
		}
		
	}
}
