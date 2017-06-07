package org.thema.mupcity.task;

import java.io.File;

public class CompositeTask {
	public static void main(String[] args) {
		String name = "test";
		File folderIn = new File("/media/mcolomb/Data_2/resultExplo/exDistrib");
		double width = 28303;
		double height = 21019;
		double xmin = 914760;
		double ymin = 6680157;
		double shiftX = 0;
		double shiftY = 0;
		File projectFile = ProjectCreationTask.run(name, folderIn, xmin, ymin, width, height, shiftX, shiftY);

		double minSize = 20;
		double maxSize = 5000;
		double seuilDensBuild = 0;
		File decompFile = DecompTask.run(projectFile, name, minSize, maxSize, seuilDensBuild);

		int nMax = 6;
		boolean strict = true;
		double ahp0 = 0.111;
		double ahp1 = 0.111;
		double ahp2 = 0.111;
		double ahp3 = 0.111;
		double ahp4 = 0.111;
		double ahp5 = 0.111;
		double ahp6 = 0.111;
		double ahp7 = 0.111;
		double ahp8 = 0.111;

		boolean mean = true;

		long seed = 42L;

		File scenarFile = SimulTask.run(decompFile, name, nMax, strict, ahp0, ahp1, ahp2, ahp3, ahp4, ahp5, ahp6, ahp7, ahp8, mean, seed);
		System.out.println(scenarFile);
	}
}
