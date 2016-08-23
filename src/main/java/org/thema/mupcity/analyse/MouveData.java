package org.thema.mupcity.analyse;

import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.feature.simple.SimpleFeatureType;

public class MouveData {

	public static void main(String[] args) throws IOException {

		for (int ii = 1; ii <= 8; ii++) {
			// définition des variables fixes
			File folderData = new File("/media/mcolomb/Data_2/resultTest/mouv_data/9m/");
			File dataFile = new File(folderData, "/data" + (ii)+"/data");
			System.out.println(dataFile);
			
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
					  SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
					  SimpleFeatureType LOCATION = builder.buildFeatureType();
					SimpleFeatureBuilder ssh = new SimpleFeatureBuilder (LOCATION);
					System.out.println(ssh);
					AffineTransform2D translate =  new AffineTransform2D(1, 0, 0, 1, Xmouve, Ymouve); //AffineTransform2D.getTranslateInstance(Xmouve, Ymouve);
					System.out.println("Translate:" + translate.toString());
					translate.createTransformedShape((Shape) ssh.buildFeature(null));
					//fuck geotool
					/*for (Object eachFeature : features.toArray() ){
						System.out.println(eachFeature);
					}*/
				}
			}
		}

	}

}
