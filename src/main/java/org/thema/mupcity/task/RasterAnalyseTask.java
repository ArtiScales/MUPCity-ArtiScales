package org.thema.mupcity.task;

import java.io.File;
import java.util.ArrayList;

import org.thema.mupcity.analyse.RasterAnalyse;

public class RasterAnalyseTask {

	public static String echelle;

	public static void main(String[] args) throws Exception {
		File file = new File("/media/mcolomb/Data_2/resultExplo/mairie1/tmp/mairie1/N5_St_Moy_ahpx_seed42");
		run(file,"stats");
	}
	
	public static File run(File file, String name) throws Exception {
		RasterAnalyse.rootFile = file;
		echelle = "20";
		RasterAnalyse.discrete = true;
		RasterAnalyse.echelle = echelle;
		ArrayList<File> fileToTest = new ArrayList<File>();
		fileToTest = RasterAnalyse.selectWith("", null);
		File statFile = RasterAnalyse.mergeRasters(fileToTest, name);
		return statFile;
	}
}
