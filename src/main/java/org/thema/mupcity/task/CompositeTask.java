package org.thema.mupcity.task;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

public class CompositeTask {
	public static void main(String[] args) throws Exception {

		Map<String, String> dataHT = new Hashtable<String, String>();

		// //Data1.1
		// dataHT.put("name", "Data1");
		// dataHT.put("build", "batimentPro.shp");
		// dataHT.put("road", "routePro.shp");
		// dataHT.put("fac", "servicePro.shp");
		// dataHT.put("lei", "loisirPro.shp");
		// dataHT.put("ptTram", "tramPro.shp");
		// dataHT.put("ptTrain", "trainPro.shp");
		// dataHT.put("nU", "nonUrbaPro.shp");

		// //Data1.1
		// dataHT.put("name", "Data1.1");
		// dataHT.put("build", "batimentPro.shp");
		// dataHT.put("road", "routePro.shp");
		// dataHT.put("fac", "servicePro.shp");
		// dataHT.put("lei", "loisirPro.shp");
		// dataHT.put("ptTram", "tramPro.shp");
		// dataHT.put("ptTrain", "trainPro.shp");
		// dataHT.put("nU", "nonUrbaPhyPro.shp");
		//
		// //Data1.2
		// dataHT.put("name", "Data1.2");
		// dataHT.put("build", "batimentPro.shp");
		// dataHT.put("road", "routePro.shp");
		// dataHT.put("fac", "servicePro.shp");
		// dataHT.put("lei", "loisirPro.shp");
		// dataHT.put("ptTram", "tramPro.shp");
		// dataHT.put("ptTrain", "trainPro.shp");
		//
		// Data2
		dataHT.put("name", "Data2");
		dataHT.put("build", "batimentSys.shp");
		dataHT.put("road", "routeSys.shp");
		dataHT.put("fac", "serviceSys.shp");
		dataHT.put("lei", "loisirSys.shp");
		dataHT.put("ptTram", "tramSys.shp");
		dataHT.put("ptTrain", "trainSys.shp");
		dataHT.put("nU", "nonUrbaSys.shp");

		// //Data2.1
		// dataHT.put("name", "Data2.1");
		// dataHT.put("build", "batimentSys.shp");
		// dataHT.put("road", "routeSys.shp");
		// dataHT.put("fac", "serviceSys.shp");
		// dataHT.put("lei", "loisirSys.shp");
		// dataHT.put("ptTram", "tramSys.shp");
		// dataHT.put("ptTrain", "trainSys.shp");
		// dataHT.put("nU", "nonUrbaPhySys.shp");
		//
		// //Data2.2
		// dataHT.put("name", "Data2.2");
		// dataHT.put("build", "batimentSys.shp");
		// dataHT.put("road", "routeSys.shp");
		// dataHT.put("fac", "serviceSys.shp");
		// dataHT.put("lei", "loisirSys.shp");
		// dataHT.put("ptTram", "tramSys.shp");
		// dataHT.put("ptTrain", "trainSys.shp");
		//
//		 //Data3
//		 dataHT.put("name", "Data3");
//		 dataHT.put("build", "batimentPro.shp");
//		 dataHT.put("road", "routeSys.shp");
//		 dataHT.put("fac", "servicePro.shp");
//		 dataHT.put("lei", "loisirPro.shp");
//		 dataHT.put("ptTram", "tramPro.shp");
//		 dataHT.put("ptTrain", "trainPro.shp");
//		 dataHT.put("nU", "nonUrbaPro.shp");
		//
		// //Data3.1
		// dataHT.put("name", "Data3.1");
		// dataHT.put("build", "batimentPro.shp");
		// dataHT.put("road", "routePro.shp");
		// dataHT.put("fac", "servicePro.shp");
		// dataHT.put("lei", "loisirSys.shp");
		// dataHT.put("ptTram", "tramPro.shp");
		// dataHT.put("ptTrain", "trainPro.shp");
		// dataHT.put("nU", "nonUrbaPro.shp");
		//
		// //Data3.2
		// dataHT.put("name", "Data3.2");
		// dataHT.put("build", "batimentPro.shp");
		// dataHT.put("road", "routePro.shp");
		// dataHT.put("fac", "serviceSys.shp");
		// dataHT.put("lei", "loisirPro.shp");
		// dataHT.put("ptTram", "tramPro.shp");
		// dataHT.put("ptTrain", "trainPro.shp");
		// dataHT.put("nU", "nonUrbaPro.shp");

		String name = "exOct";
		File folderIn = new File("/home/mcolomb/informatique/MUP/explo/data");
		File folderOut = new File("/media/mcolomb/Data_2/resultExplo/testNov");
		File discreteFile = new File("/home/mcolomb/informatique/MUP/explo/dataExtra/admin_typo.shp");
		
		double width = 28303;
		double height = 21019;
		double xmin = 914760;
		double ymin = 6680157;
		double shiftX = 0;
		double shiftY = 0;

		double minSize = 20;
		double maxSize = 5000;
		double seuilDensBuild = 0;

		int nMax = 5;
		boolean strict = true;
		
//		double ahp8 = 0.083;
//		double ahp7 = 0.083;
//		double ahp6 = 0.083;
//		double ahp5 = 0.04;
//		double ahp4 = 0.218;
//		double ahp3 = 0.218;
//		double ahp2 = 0.218;
//		double ahp1 = 0.03;
//		double ahp0 = 0.027;

		double ahp8 = 0.111;
		double ahp7 = 0.111;
		double ahp6 = 0.111;
		double ahp5 = 0.111;
		double ahp4 = 0.111;
		double ahp3 = 0.111;
		double ahp2 = 0.111;
		double ahp1 = 0.111;
		double ahp0 = 0.111;
		
		boolean mean = true;
		boolean useNU = true;
		if (dataHT.get("nU").isEmpty()) {
			System.out.println("no NU zone");
			useNU = false;
		}

		long seed = 42;
		File filout = CompositeTask.run(name, folderIn, folderOut, discreteFile, xmin, ymin, width, height, shiftX, shiftY, minSize, maxSize, seuilDensBuild, nMax, strict, ahp0, ahp1, ahp2,
				ahp3, ahp4, ahp5, ahp6, ahp7, ahp8, mean, seed, useNU, dataHT);
		System.out.println(filout);

	}

	public static File run(String name, File folderIn, File folderOut,File discreteFile, double xmin, double ymin, double width, double height, double shiftX, double shiftY, double minSize,
			double maxSize, double seuilDensBuild, int nMax, boolean strict, double ahp0, double ahp1, double ahp2, double ahp3, double ahp4, double ahp5, double ahp6, double ahp7,
			double ahp8, boolean mean, long seed, boolean useNU, Map<String, String> dataHT) throws Exception {
		System.out.println("----------Project creation----------");
		File projectFile = ProjectCreationTask.run(name, folderIn, folderOut, xmin, ymin, width, height, shiftX, shiftY, useNU, dataHT);
		System.out.println("----------Decomp task----------");
		DecompTask.run(projectFile, name, minSize, maxSize, seuilDensBuild);
		System.out.println("----------Simulation task----------");
		for (seed = 42; seed < 62; seed = seed + 1) {
			SimulTask.run(projectFile, name, nMax, strict, ahp0, ahp1, ahp2, ahp3, ahp4, ahp5, ahp6, ahp7, ahp8, mean, seed, useNU);
		}
		RasterAnalyseTask.run(projectFile,discreteFile, name);
		System.out.println("----------End task----------");
		return projectFile;
	}
}
