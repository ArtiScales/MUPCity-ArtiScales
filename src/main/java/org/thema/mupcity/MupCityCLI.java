package org.thema.mupcity;

import java.io.File;
import java.io.IOException;

import org.geotools.feature.SchemaException;
import org.thema.common.swing.TaskMonitor;

public class MupCityCLI {
	public static void main(String[] args) throws IOException, SchemaException {
		String name = args[0];
		File dir = new File(args[1]);
		File buildFile = new File(args[2]);
		int exp = Integer.parseInt(args[3]);
		double minSize = Double.parseDouble(args[4]);
		double maxSize = Double.parseDouble(args[5]);
		double seuilDensBuild = 0.0;
		TaskMonitor mon = null;//new TaskMonitor.EmptyMonitor();
		Project project = Project.createProject(name, dir, buildFile, mon);
		project.decomp(exp, maxSize, minSize, seuilDensBuild, false);
		project.save();
	}
}
