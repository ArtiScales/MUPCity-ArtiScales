package org.thema.mupcity.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.thema.mupcity.analyse.RasterAnalyse;
import org.thema.mupcity.analyse.RasterMerge;

public class RasterAnalyseTask {

	public static String echelle;

	public static void main(String[] args) throws Exception {
		File file = new File("/media/mcolomb/Data_2/resultExplo/testNov/exOct");
		File discreteFile = new File("/home/mcolomb/informatique/MUP/explo/dataExtra/admin_typo.shp");
		run(file,discreteFile, "stats-dicrete");
	}

	public static File run(File file, File discreteFile, String name) throws Exception {
		RasterAnalyse.rootFile = file;

		echelle = "20";
		RasterAnalyse.discrete = false;
		RasterAnalyse.echelle = echelle;
		RasterAnalyse.stabilite = true;

		List<File> fileToTest = new ArrayList<File>();

		for (File f : file.listFiles()) {
			if (f.isDirectory()) {
				for (File ff : f.listFiles()) {
					if (ff.getName().endsWith("eval_anal-" + echelle + ".0.tif")) {
						fileToTest.add(ff);
					}
				}
			}
		}
		File statFile = RasterAnalyse.mergeRasters(fileToTest, name);
		RasterAnalyse.discrete=true;
		RasterAnalyse.discreteFile = discreteFile;
		RasterAnalyse.mergeRasters(fileToTest, "stat-discrete");
		File rastFile = new File(file, "raster");
		rastFile.mkdir();
		RasterMerge.merge(fileToTest, new File(rastFile, "rasterMerged.tif"), 20);
		
		return statFile;
	}
}
