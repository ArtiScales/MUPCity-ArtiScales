package org.thema.mupcity.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
			Project project = Project.load(new File(projFile, name + ".xml"));
			project.decomp(3, maxSize, minSize, seuilDensBuild, mon, false);
			//File decompFile = new File(projFile, String.valueOf(minSize) + "_" + String.valueOf(maxSize) + "_cell_" + String.valueOf(seuilDensBuild));
			project.save();
			cleanProject(project);
			return projFile;
	}
	
	private static void cleanProject(Project project) throws IOException {
		// TODO mettre propre :  vraiment pas beau mais je jette l'éponge sur les milliers de types d'objets différents pour pouvoir retrouver le shapefile des layers
		for (File f : project.getDirectory().listFiles()) {
			if (f.getName().endsWith(".shp") || f.getName().endsWith(".dbf") || f.getName().endsWith(".fix") || f.getName().endsWith(".prj") || f.getName().endsWith(".shx")) {
				Files.delete(f.toPath());
			}
		}

	}
}
