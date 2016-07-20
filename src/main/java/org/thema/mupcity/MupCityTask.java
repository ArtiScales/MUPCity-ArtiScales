package org.thema.mupcity;

import java.io.File;
import java.io.IOException;

import org.geotools.feature.SchemaException;
import org.thema.drawshape.layer.GroupLayer;

public class MupCityTask {
	public static void run(long seed, File data, File results, File facilityFile) throws IOException, SchemaException {
		String name = "testExplo";
		File dir = results;
		File buildFile = new File(data, "BATI_AU.shp");
		int exp = 3;
		double minSize = 20;
		double maxSize = 25273;
		boolean useNoBuild = true;
		boolean network = true;// true => network distance
		File roadFile = new File(data, "route_sans_chemin.shp");
		//File facilityFile = new File(data, "CS_au_besac_sirene_2012.shp");
		File leisureFile = new File(data, "loisirs.shp");
		File busFile = new File(data, "stations_besac_tram_2015.shp");
		File trainFile = new File(data, "gare_train_ICONE_docs_2015.shp");
		File restrictFile = new File(data, "non_urba.shp");
		// definition de la grille
		double width = 28303;
		double height = 21019;
		double minX = 914760;
		double minY = 6680157;
		int nMax = 5;
		boolean strict = false;
		boolean mean = true;
		int ahpIndex = 2;
		MupCityCLI.run(name, dir, buildFile, exp, minSize, maxSize, nMax, strict, useNoBuild, mean, network, seed, roadFile, facilityFile, leisureFile, busFile, trainFile, restrictFile, minX, minY, width, height, ahpIndex, results);
	}

	public static void main(String[] args) throws IOException, SchemaException {

		/* to test the sensibility of different grids
		long seed = 42;
		for (int i = 0; i <= 8; i++) {
			File data = new File("/media/mcolomb/Data_2/resultTest/changement_projection/reproj/data" + i);
			File results = new File(data, "results");
			results.mkdirs();
			MupCityTask.run(seed, data, results);
		}*/
		long seed = 42;
		File data = new File("/home/mcolomb/informatique/MUP/explo/data");
		File results = new File("/media/mcolomb/Data_2/resultTest/mairie");
		for (int i = 0; i <= 1; i++) {
			File facilityFile = new File(data, "CS_au_besac_sirene_2012.shp");
			if (i == 1) {
				facilityFile = new File(data, "CS_au_besac_sirene_2012-ss-mairie.shp");
				results = new File("/media/mcolomb/Data_2/resultTest/mairie/sans");
				results.mkdirs();
			}
			MupCityTask.run(seed, data, results, facilityFile);
		}

	}
}
