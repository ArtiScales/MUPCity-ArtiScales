package org.thema.mupcity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;

import org.geotools.feature.SchemaException;
import org.thema.common.swing.TaskMonitor;
import org.thema.mupcity.rule.OriginDistance;
import org.thema.mupcity.rule.Rule;
import org.thema.mupcity.scenario.ScenarioAuto;

public class MupCityCLI {
	public static void main(String[] args) throws IOException, SchemaException {
		// get the attributes from the command line
		String name = args[0];		
		File dir = new File(args[1]);
		File buildFile = new File(args[2]);
		int exp = Integer.parseInt(args[3]);
		double minSize = Double.parseDouble(args[4]);
		double maxSize = Double.parseDouble(args[5]);
		int nMax = Integer.parseInt(args[6]);
		boolean strict = Boolean.parseBoolean(args[7]);
		boolean useNoBuild = Boolean.parseBoolean(args[8]);
		boolean mean = Boolean.parseBoolean(args[9]);//true => mean, false=>yager
		boolean network = Boolean.parseBoolean(args[10]);//true => network distance, false => euclidian
		long seed = Long.parseLong(args[11]);
		File roadFile = new File(args[12]);
		File facilityFile = new File(args[13]);
		//File leisureFile = new File(args[14]);
		double minX = Double.parseDouble(args[15]);
		double minY = Double.parseDouble(args[16]);
		double width = Double.parseDouble(args[17]);
		double height = Double.parseDouble(args[18]);
		double seuilDensBuild = 0.0;// NO PARAMETER FOR THAT
		// empty monitor for CLI
		TaskMonitor mon = new TaskMonitor.EmptyMonitor();
		// new project
		Project project = Project.createProject(name, dir, buildFile, minX, minY, width, height, mon);
		// set layers and attributes for the decomposition
		List<String> roadAttrs = Arrays.asList("PREC_PLANI");// SPEED(numeric)
		project.setLayer(Project.LAYERS.get(Project.Layers.ROAD.ordinal()), roadFile, roadAttrs, mon);
		List<String> facilityAttrs = Arrays.asList("LEVEL", "NATURE");// LEVEL (numeric), TYPE (any)
		project.setLayer(Project.LAYERS.get(Project.Layers.FACILITY.ordinal()), facilityFile, facilityAttrs, mon);
		//List<String> leisureAttrs = Arrays.asList("LEVEL", "NATURE");// LEVEL (numeric), TYPE (any)
		//project.setLayer(Project.LAYERS.get(Project.Layers.LEISURE.ordinal()), leisureFile, leisureAttrs, mon);
		// dist type
		project.setDistType((network)?OriginDistance.NetworkDistance.class:OriginDistance.EuclideanDistance.class);
		// decomposition
		project.decomp(exp, maxSize, minSize, seuilDensBuild, mon);
		// handle the rules
		List<String> items = new ArrayList<>();

		// get the names of the usable rules
		for (Rule rule : project.getRules()) if (rule.isUsable(project)) items.add(rule.getName());
		System.out.println(items);
		// get the resolutions from the grid to pass them to the scenario
		NavigableSet<Double> res = project.getMSGrid().getResolutions();
		// create empty AHP
		AHP ahp = new AHP(items);
		// create the scenario
		ScenarioAuto scenario = ScenarioAuto.createMultiScaleScenario(name, res.first(), res.last(), nMax, strict, ahp, useNoBuild, mean, exp, seed);
		project.performScenarioAuto(scenario);
		// save the project
		project.save();
		// save the evaluation grid
		project.getMSGrid().saveRaster(scenario.getEvalLayerName(), dir);
	}
}
