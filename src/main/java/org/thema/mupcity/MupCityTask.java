package org.thema.mupcity;

import java.io.File;
import java.io.IOException;

import org.geotools.feature.SchemaException;

public class MupCityTask {
	public static void run(long seed, File data, File results) throws IOException, SchemaException {
		String name = "testExplo";
		File dir = results;
		File buildFile = new File(data, "BATI_AU.shp");
		int exp = 3;
		double minSize = 20;
		double maxSize = 25273;
		boolean useNoBuild = true;
		boolean network = true;// true => network distance
		File roadFile = new File(data, "route_sans_chemin.shp");
		File facilityFile = new File(data, "CS_au_besac_sirene_2012.shp");
		File leisureFile = new File(data, "loisirs.shp");
		File busFile = new File(data, "stations_besac_tram_2015.shp");
		File trainFile = new File(data, "gare_train_ICONE_docs_2015.shp");
		File restrictFile = new File(data, "ICONE-zonesNU_AU.shp");
		// definition de la grille
		double width = 32243;
		double height = 33602;
		double minX = 911598;
		double minY = 6670519;
		int nMax = 3;
		boolean strict = true;
		boolean mean = false;
		int ahpIndex = 0;
		MupCityCLI.run(name, dir, buildFile, exp, minSize, maxSize, nMax, strict, useNoBuild, mean, network, seed,
				roadFile, facilityFile, leisureFile, busFile, trainFile, restrictFile, minX, minY, width, height,
				ahpIndex, results);
	}
	public static void main(String[] args) throws IOException, SchemaException {
		long seed = 42L;
		File data = new File("/home/julien/mupcity/data");
		File results = new File(data,"results"+File.separator+seed);
		results.mkdirs();
		MupCityTask.run(seed, data, results);
	}
}
