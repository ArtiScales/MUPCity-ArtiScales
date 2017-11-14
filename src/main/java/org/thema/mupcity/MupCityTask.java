package org.thema.mupcity;

import java.io.File;
import java.io.IOException;

import org.geotools.feature.SchemaException;
import org.thema.drawshape.layer.GroupLayer;

public class MupCityTask {
	public static void run(long seed, File data, File results) throws IOException, SchemaException {

		int nMax = 5;
		boolean strict = false;
		boolean mean = true;
		int ahpIndex = 5;
MupCityTask.run(seed, data, results, nMax, strict, mean, ahpIndex);
			}

	private static void run(long seed, File data, File results, int nMax, boolean strict, boolean mean, int ahpIndex) throws IOException, SchemaException {
		// TODO Auto-generated method stub
		
		String name = "testExplo";
		File dir = results;
		File buildFile = new File(data, "BATI_AU.shp");
		int exp = 3;
		double minSize = 20;
		double maxSize = 25273;
		boolean useNoBuild = true;
		boolean network = false;// true => network distance
		File roadFile = new File(data, "route_sans_chemin.shp");
		File facilityFile = new File(data, "CS_au_besac_sirene_2012.shp");
		File leisureFile = new File(data, "loisirs.shp");
		File busFile = new File(data, "stations_besac_tram_2015.shp");
		File trainFile = new File(data, "gare_train_ICONE_docs_2015.shp");
		File restrictFile = new File(data, "non_urba.shp");
		// definition de la grille
		double width = 28303;
		double height = 21019;
		double minX = 914760;
		double minY = 6680157;
		MupCityCLI.run(name, dir, buildFile, exp, minSize, maxSize, nMax, strict, useNoBuild, mean, network, seed, roadFile, facilityFile, leisureFile, busFile, trainFile, restrictFile, minX, minY, width, height, ahpIndex, results);
//      MupCityCLI.createProject(name, dir, buildFile, exp, minSize, maxSize, network, roadFile, facilityFile, leisureFile, busFile, trainFile, restrictFile, minX, minY, width, height);
//      MupCityCLI.performScenario(name, dir, exp, nMax, useNoBuild, strict, mean, seed, ahpIndex, results);

	}
	
	public static void main(String[] args) throws IOException, SchemaException {
		long seed = 42L;
		File data = new File("./data");
		File results = new File(data,"results"+File.separator+seed);
		results.mkdirs();
		for (int nMax = 3; nMax <= 7; nMax++) {
			for (int s = 0; s <= 1; s++) {
				boolean strict;
				if (s == 0) {
					strict = true;
				} else {
					strict = false;
				}
				for (int ahpIndex = 0; ahpIndex < 6; ahpIndex++) {
					boolean mean;
					if (ahpIndex < 3) {
						mean = false;
					} else {
						mean = true;
					}
					for (seed = 1L; seed <= 10L; seed++) {
						MupCityTask.run(seed, data, results, nMax, strict, mean, ahpIndex);
					}
				}
			}
		}
		
		//to test stability analysis
		data = new File("/home/mcolomb/informatique/MUP/explo/data");
		results = new File("/media/mcolomb/Data_2/resultTest/sensibility/Ba/results/");
		results.mkdir();
		for (int i = 0; i <= 1000000; i++) {
			MupCityTask.run(i, data, results);
		}
		data = new File("/home/mcolomb/informatique/MUP/explo/data");
		results = new File("/media/mcolomb/Data_2/resultTest/sensibility/St/results/");
		results.mkdir();
		for (int i = 0; i <= 1000000; i++) {
			MupCityTask.run(i, data, results,5,true,true,5);
		}
		data = new File("/home/mcolomb/informatique/MUP/explo/data");
		results = new File("/media/mcolomb/Data_2/resultTest/sensibility/Yag/results/");
		results.mkdir();
		for (int i = 0; i <= 1000000; i++) {
			MupCityTask.run(i, data, results,5,false,false,5);
		}
		
		//to test the sensibility of different grids
		for (int i = 0; i <= 8; i++) {

			data = new File("/media/mcolomb/Data_2/resultTest/mouv_data/data" + i);
			results = new File(data, "results");
			results.mkdirs();
			MupCityTask.run(seed, data, results);
		}
		/*
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
		}*/
	}
}
