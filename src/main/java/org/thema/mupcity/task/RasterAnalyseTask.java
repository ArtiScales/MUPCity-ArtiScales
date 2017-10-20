package org.thema.mupcity.task;

import java.io.File;
import java.util.ArrayList;

import org.thema.mupcity.analyse.RasterAnalyse;

public class RasterAnalyseTask {

	public static String echelle;

	public static void main(String[] args) throws Exception {
		File file = new File("/media/mcolomb/Data_2/resultExplo/testOct/exOct");
		System.out.println("stats in : " + run(file, "stats"));
	}

	public static File run(File file, String name) throws Exception {
		RasterAnalyse.rootFile = file;

		echelle = "20";
		RasterAnalyse.discrete = false;
		RasterAnalyse.echelle = echelle;
		RasterAnalyse.stabilite = true;

		ArrayList<File> fileToTest = new ArrayList<File>();

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
		return statFile;
	}
}
