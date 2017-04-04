package org.thema.mupcity.analyse;

import java.io.File;
import java.io.IOException;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import com.google.common.io.Files;
import com.vividsolutions.jts.geom.Geometry;

public class BougeData {

	//TODO cette classe ne marche pas - réparer si l'on veut lancer des tests automatiques et ne pas tout faire à la main
	public static void main(String[] args) throws IOException, MismatchedDimensionException, TransformException {

		for (int ii = 1; ii <= 8; ii++) {
			// définition des variables fixes
			File folderGen = new File("/media/mcolomb/Data_2/resultExplo/MouvData/");
			File folderData = new File("/media/mcolomb/Data_2/resultExplo/MouvData/data0/data");
			File dataFile = new File(folderGen, "/data" + (ii) + "/data");
			dataFile.mkdirs();
			File[] aCopier = folderData.listFiles();
			for (File aCp : aCopier) {
				File nf = new File(dataFile, aCp.getName());
				Files.copy(aCp, nf);
			}
			double ecart = 9;
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
			for (File shFile : dataFile.listFiles()) {
				if (shFile.toString().endsWith("shp")) {
					System.out.println(shFile);
					//					SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
					//					SimpleFeatureType LOCATION = builder.buildFeatureType();
					//					SimpleFeatureBuilder ssh = new SimpleFeatureBuilder (LOCATION);
					//					System.out.println(ssh);
					ShapefileDataStore dataStore = new ShapefileDataStore(shFile.toURI().toURL());

					AffineTransform2D translate = new AffineTransform2D(1, 0, 0, 1, Xmouve, Ymouve); //AffineTransform2D.getTranslateInstance(Xmouve, Ymouve);						//j'aimerais mettre direct une saloperie de shape dans translate.createTransformedShape(shape) mais je ne vois pas comment faire

					ContentFeatureCollection shpFeatures = dataStore.getFeatureSource().getFeatures();
					DefaultFeatureCollection newFeatures = new DefaultFeatureCollection();
					SimpleFeatureType TYPE = shpFeatures.getSchema();
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
							//	System.out.println("objhect ici : "+nouveaux[cpt]);
						}
					} finally {
						iterator.close();
					}
					//					 ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
					//					SimpleFeatureBuilder myBuilder = new SimpleFeatureBuilder( TYPE,(org.opengis.feature.FeatureFactory) dataStoreFactory ) ;
					//newFeatures.add(myBuilder.bui);

					//newFeatures.add(SimpleFeatureBuilder.build( TYPE, nouveaux, null) );

					Transaction transaction = new DefaultTransaction("create");

					String typeName = dataStore.getTypeNames()[0];
					//					SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
					SimpleFeatureSource source = DataUtilities.source(newFeatures);
					if (source instanceof SimpleFeatureStore) {
						SimpleFeatureStore featureStore = (SimpleFeatureStore) source;
						featureStore.setTransaction(transaction);
						try {
							featureStore.addFeatures(newFeatures);
							transaction.commit();
						} catch (Exception problem) {
							problem.printStackTrace();
							transaction.rollback();

						} finally {
							transaction.close();
							System.out.println("cé fé");
						}
						System.exit(0); // success!
					} else {
						System.out.println(typeName + " does not support read/write access");
						System.exit(1);
					}
				}
			}
		}
	}
}
