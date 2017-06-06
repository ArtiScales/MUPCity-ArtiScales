package org.thema.mupcity.analyse;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import com.google.common.io.Files;
import com.vividsolutions.jts.geom.Geometry;

public class BougeData {

	//TODO cette classe ne marche pas - réparer si l'on veut lancer des tests automatiques et ne pas tout faire à la main
	public static void main() throws IOException, MismatchedDimensionException, TransformException {
		for (int ecart = 1; ecart <= 9; ecart = ecart * 3) {
			System.out.println("start to move it move it");
			File folderGen = new File("/media/mcolomb/Data_2/resultExplo/MouvData");
			File[] aCopier = new File(folderGen, "dataOriginel/").listFiles();
			File copierVers = new File(folderGen, ecart+ "m/data0/data/");
			copierVers.mkdirs();
			for (File aCp : aCopier) {
				File nf = new File(copierVers, aCp.getName());
				if (aCp.isFile()) {
					Files.copy(aCp, nf);
				}
			}
			for (int ii = 1; ii <= 8; ii++) {
				// définition des variables fixes
				File dataFile = new File(folderGen,ecart+ "m/data" + (ii) + "/data");
				dataFile.mkdirs();
				double Xmouve = 0;
				double Ymouve = 0;
				switch (ii) {
				case 1:
					Xmouve = ecart;
					Ymouve = 0;
					break;
				case 2:
					Xmouve = ecart;
					Ymouve = ecart;
					break;
				case 3:
					Xmouve = 0;
					Ymouve = ecart;
					break;
				case 4:
					Xmouve = -ecart;
					Ymouve = ecart;
					break;
				case 5:
					Xmouve = -ecart;
					Ymouve = 0;
					break;
				case 6:
					Xmouve = -ecart;
					Ymouve = -ecart;
					break;
				case 7:
					Xmouve = 0;
					Ymouve = -ecart;
					break;
				case 8:
					Xmouve = ecart;
					Ymouve = -ecart;
					break;
				}
				for (File shFile : aCopier) {
					if (shFile.toString().endsWith("shp")) {
						ShapefileDataStore dataStore = new ShapefileDataStore(shFile.toURI().toURL());

						AffineTransform2D translate = new AffineTransform2D(1, 0, 0, 1, Xmouve, Ymouve); //AffineTransform2D.getTranslateInstance(Xmouve, Ymouve);						//j'aimerais mettre direct une saloperie de shape dans translate.createTransformedShape(shape) mais je ne vois pas comment faire

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
						
				        File newFile = new File(dataFile+"/"+shFile.getName());

				        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

				        Map<String, Serializable> params = new HashMap<>();
				        params.put("url", newFile.toURI().toURL());
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
				    
						
						
						/*
						Transaction transaction = new DefaultTransaction("create");
						String typeName = dataStore.getTypeNames()[0];
						SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
						if (featureSource instanceof SimpleFeatureStore) {
							SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
							featureStore.setTransaction(transaction);
							try {
//								FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
//								Filter filter = ff.like(null, "");
//								featureStore.deleteFeatures();
								featureStore.addFeatures(newFeatures);
								transaction.commit();
							} catch (Exception problem) {
								problem.printStackTrace();
								transaction.rollback();

							} finally {
								transaction.close();
								System.out.println("	done");
							}
						} else {

							System.out.println(typeName + " does not support read/write access");

						}*/
					}
				}
			}
		}
	}
}
