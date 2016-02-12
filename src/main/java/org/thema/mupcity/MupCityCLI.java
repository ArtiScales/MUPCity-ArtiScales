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
		String name = args[0];
		File dir = new File(args[1]);
		File buildFile = new File(args[2]);
		int exp = Integer.parseInt(args[3]);
		double minSize = Double.parseDouble(args[4]);
		double maxSize = Double.parseDouble(args[5]);
		int nMax = 5;// Add PARAM
		boolean strict = true;// ADD
		boolean useNoBuild = false;// ADD
		boolean mean = true;// FALSE=>YAGER
		boolean network = true;//ADD P
		long seed = System.currentTimeMillis();// ADD P

		File roadFile = new File(args[6]);
		File facilityFile = new File(args[7]);
		File leisureFile = new File(args[8]);
		
		double seuilDensBuild = 0.0;// NO PARAMETER FOR THAT
		// ADD RULES
		// ADD SEED
		TaskMonitor mon = new TaskMonitor.EmptyMonitor();
		Project project = Project.createProject(name, dir, buildFile, mon);
		// set layers and attributes for the decomposition
		List<String> roadAttrs = Arrays.asList("PREC_PLANI");// SPEED(numeric)
		project.setLayer(Project.LAYERS.get(Project.Layers.ROAD.ordinal()), roadFile, roadAttrs);
		List<String> facilityAttrs = Arrays.asList("LEVEL", "NATURE");// LEVEL (numeric), TYPE (any)
		project.setLayer(Project.LAYERS.get(Project.Layers.FACILITY.ordinal()), facilityFile, facilityAttrs);
		List<String> leisureAttrs = Arrays.asList("LEVEL", "NATURE");// LEVEL (numeric), TYPE (any)
		project.setLayer(Project.LAYERS.get(Project.Layers.LEISURE.ordinal()), leisureFile, leisureAttrs);
		// dist type
		project.setDistType((network)?OriginDistance.NetworkDistance.class:OriginDistance.EuclideanDistance.class);
		// decomposition
		project.decomp(exp, maxSize, minSize, seuilDensBuild, mon);
		// handle the rules
		List<String> items = new ArrayList<>();
		// get the names of the usable rules
		for (Rule rule : project.getRules()) {
			if (rule.isUsable(project)) {
				items.add(rule.getName());
			}
		}
		NavigableSet<Double> res = project.getMSGrid().getResolutions();
		AHP ahp = new AHP(items);
		// create the scenario
		project.performScenarioAuto(ScenarioAuto.createMultiScaleScenario(name, res.first(), res.last(), nMax, strict,
				ahp, useNoBuild, mean, exp, seed));
		project.save();
	}
}
