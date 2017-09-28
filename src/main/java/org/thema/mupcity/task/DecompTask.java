package org.thema.mupcity.task;

import java.io.File;

import org.thema.common.swing.TaskMonitor;
import org.thema.mupcity.Project;

public class DecompTask {

	public static void main(String args[]) throws Exception {
		File projFile = new File("/media/mcolomb/Data_2/resultExplo/exDistrib/tmp/test/");
		String name = "test";
		double minSize = 20;
		double maxSize = 25273;
		double seuilDensBuild = 0;
		run(projFile, name, minSize, maxSize, seuilDensBuild);
	}

	public static File run(File projFile, String name, double minSize, double maxSize, double seuilDensBuild) throws Exception {
			TaskMonitor mon = new TaskMonitor.EmptyMonitor();
			System.out.println(projFile);
			System.out.println(new File(projFile, name + ".xml"));
			Project project = Project.load(new File(projFile, name + ".xml"));
			project.decomp(3, maxSize, minSize, seuilDensBuild, mon, false);
			//File decompFile = new File(projFile, String.valueOf(minSize) + "_" + String.valueOf(maxSize) + "_cell_" + String.valueOf(seuilDensBuild));
			project.save();
			return projFile;
	}
}
