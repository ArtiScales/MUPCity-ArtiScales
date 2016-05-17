package org.thema.mupcity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableSet;

import org.geotools.feature.SchemaException;
import org.thema.common.swing.TaskMonitor;
import org.thema.mupcity.rule.OriginDistance;
import org.thema.mupcity.rule.Rule;
import org.thema.mupcity.scenario.ScenarioAuto;

public class MupCityCLI {
	public static void run(
			String name, File dir, File buildFile, int exp, double minSize, double maxSize, int nMax, boolean strict, 
			boolean useNoBuild, boolean mean, boolean network, long seed, File roadFile, File facilityFile, File leisureFile,
			File busFile, File trainFile, File restrictFile, double minX, double minY, double width, double height,
			int ahpIndex, File results
			) throws IOException, SchemaException {
		double seuilDensBuild = 0.0;// NO PARAMETER FOR THAT
		// empty monitor for CLI
		TaskMonitor mon = new TaskMonitor.EmptyMonitor();
		// new project
		Project project = Project.createProject(name, dir, buildFile, minX, minY, width, height, mon);
		project.setNetPrecision(0.1);
		// set layers and attributes for the decomposition
		List<String> roadAttrs = Arrays.asList("Speed");// SPEED(numeric)
		project.setLayer(Project.LAYERS.get(Project.Layers.ROAD.ordinal()), roadFile, roadAttrs, mon);
		List<String> facilityAttrs = Arrays.asList("LEVEL", "TYPE");// LEVEL (numeric), TYPE (any)
		project.setLayer(Project.LAYERS.get(Project.Layers.FACILITY.ordinal()), facilityFile, facilityAttrs, mon);
		List<String> leisureAttrs = Arrays.asList("LEVEL", "TYPE");// LEVEL (numeric), TYPE (any)
		project.setLayer(Project.LAYERS.get(Project.Layers.LEISURE.ordinal()), leisureFile, leisureAttrs, mon);
		List<String> emptyAttrs = Arrays.asList("");
		project.setLayer(Project.LAYERS.get(Project.Layers.BUS_STATION.ordinal()), busFile, emptyAttrs, mon);
		project.setLayer(Project.LAYERS.get(Project.Layers.TRAIN_STATION.ordinal()), trainFile, emptyAttrs, mon);
		project.setLayer(Project.LAYERS.get(Project.Layers.RESTRICT.ordinal()), restrictFile, emptyAttrs, mon);
		project.setDistType((network) ? OriginDistance.NetworkDistance.class : OriginDistance.EuclideanDistance.class);

		// setting of the six different AHP matrix
		// we first take the names of the different working rules
		List<String> items = new ArrayList<>();
		for (Rule rule : project.getRules()) {
			if (rule.isUsable(project)) {
				items.add(rule.getName());
			}
		}

		// setting on our six ahp objects
		AHP ahpE_Yag = new AHP(items);// creation of the true objects
		ahpE_Yag.realName = "ahpE_Yag";// putting a String name
		AHP ahpT_Yag = new AHP(items);
		ahpT_Yag.realName = "ahpT_Yag";
		AHP ahpS_Yag = new AHP(items);
		ahpS_Yag.realName = "ahpS_Yag";
		AHP ahpE_Moy = new AHP(items);
		ahpE_Moy.realName = "ahpE_Moy";
		AHP ahpT_Moy = new AHP(items);
		ahpT_Moy.realName = "ahpT_Moy";
		AHP ahpS_Moy = new AHP(items);
		ahpS_Moy.realName = "ahpS_Moy";

		HashMap<String, Double> coefsE_Yag = new HashMap<>();// collection of the eigenvector values
		coefsE_Yag.put(items.get(8), 1.0);
		coefsE_Yag.put(items.get(7), 1.0);
		coefsE_Yag.put(items.get(6), 1.0);
		coefsE_Yag.put(items.get(5), 1.0);
		coefsE_Yag.put(items.get(4), 1.0);
		coefsE_Yag.put(items.get(3), 1.0);
		coefsE_Yag.put(items.get(2), 1.0);
		coefsE_Yag.put(items.get(1), 1.0);
		coefsE_Yag.put(items.get(0), 1.0);
		ahpE_Yag.setCoeff(coefsE_Yag);// we force the vector to the ahp objects

		HashMap<String, Double> coefsT_Yag = new HashMap<>();
		coefsE_Yag.put(items.get(8), 0.458);
		coefsE_Yag.put(items.get(7), 0.458);
		coefsE_Yag.put(items.get(6), 0.458);
		coefsT_Yag.put(items.get(5), 3.625);
		coefsT_Yag.put(items.get(4), 1.199);
		coefsT_Yag.put(items.get(3), 1.199);
		coefsT_Yag.put(items.get(2), 1.199);
		coefsT_Yag.put(items.get(1), 0.202);
		coefsT_Yag.put(items.get(0), 0.202);
		ahpT_Yag.setCoeff(coefsT_Yag);

		HashMap<String, Double> coefsS_Yag = new HashMap<>();
		coefsE_Yag.put(items.get(8), 0.745);
		coefsE_Yag.put(items.get(7), 0.745);
		coefsE_Yag.put(items.get(6), 0.745);
		coefsS_Yag.put(items.get(5), 0.359);
		coefsS_Yag.put(items.get(4), 1.965);
		coefsS_Yag.put(items.get(3), 1.965);
		coefsS_Yag.put(items.get(2), 1.965);
		coefsS_Yag.put(items.get(1), 0.269);
		coefsS_Yag.put(items.get(0), 0.243);
		ahpS_Yag.setCoeff(coefsS_Yag);

		HashMap<String, Double> coefsE_Moy = new HashMap<>();
		coefsE_Yag.put(items.get(8), 0.111);
		coefsE_Yag.put(items.get(7), 0.111);
		coefsE_Yag.put(items.get(6), 0.111);
		coefsE_Moy.put(items.get(5), 0.111);
		coefsE_Moy.put(items.get(4), 0.111);
		coefsE_Moy.put(items.get(3), 0.111);
		coefsE_Moy.put(items.get(2), 0.111);
		coefsE_Moy.put(items.get(1), 0.111);
		coefsE_Moy.put(items.get(0), 0.111);
		ahpE_Moy.setCoeff(coefsE_Moy);

		HashMap<String, Double> coefsT_Moy = new HashMap<>();
		coefsE_Yag.put(items.get(8), 0.051);
		coefsE_Yag.put(items.get(7), 0.051);
		coefsE_Yag.put(items.get(6), 0.051);
		coefsT_Moy.put(items.get(5), 0.403);
		coefsT_Moy.put(items.get(4), 0.133);
		coefsT_Moy.put(items.get(3), 0.133);
		coefsT_Moy.put(items.get(2), 0.133);
		coefsT_Moy.put(items.get(1), 0.022);
		coefsT_Moy.put(items.get(0), 0.022);
		ahpT_Moy.setCoeff(coefsT_Moy);

		HashMap<String, Double> coefsS_Moy = new HashMap<>();
		coefsE_Yag.put(items.get(8), 0.083);
		coefsE_Yag.put(items.get(7), 0.083);
		coefsE_Yag.put(items.get(6), 0.083);
		coefsS_Moy.put(items.get(5), 0.04);
		coefsS_Moy.put(items.get(4), 0.218);
		coefsS_Moy.put(items.get(3), 0.218);
		coefsS_Moy.put(items.get(2), 0.218);
		coefsS_Moy.put(items.get(1), 0.03);
		coefsS_Moy.put(items.get(0), 0.027);
		ahpS_Moy.setCoeff(coefsS_Moy);

		// list of AHP to loop in
		List<AHP> ahpList = new ArrayList<AHP>();
		ahpList.add(ahpE_Yag);
		ahpList.add(ahpT_Yag);
		ahpList.add(ahpS_Yag);
		ahpList.add(ahpE_Moy);
		ahpList.add(ahpT_Moy);
		ahpList.add(ahpS_Moy);

		// create new decomp
		project.decomp(exp, maxSize, minSize, seuilDensBuild, mon);
//		project.save();
		
		String nname = "N" + nMax;// part of the folder's name
		String nstrict = strict?"St":"Ba";// part of the folder's name
		AHP ahp = ahpList.get(ahpIndex);
		String nahp = ahp.getName();
		String nameseed = "replication_" + seed;// part of the folder's name
		String titre = nname + "--" + nstrict + "--" + nahp + "--" + nameseed;// part of the folder's name
		NavigableSet<Double> res = project.getMSGrid().getResolutions();
		ScenarioAuto scenario = ScenarioAuto.createMultiScaleScenario(titre, res.first(), res.last(), nMax, strict, ahp, useNoBuild, mean, exp, seed, false, false);
		project.performScenarioAuto(scenario);
		// save the project
		// scenario.save(testFile,project);
		// project.getMSGrid().saveRaster(scenario.getEvalLayerName(), testFile); pas besoin de ces couches
		scenario.extractEvalAnal(results, project);
	}

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
		File leisureFile = new File(args[14]);
		File busFile = new File(args[15]);
		File trainFile = new File(args[16]);
		File restrictFile = new File(args[17]);
		double minX = Double.parseDouble(args[18]);
		double minY = Double.parseDouble(args[19]);
		double width = Double.parseDouble(args[20]);
		double height = Double.parseDouble(args[21]);
		int index = Integer.parseInt(args[22]);
		File results = new File(args[23]);
		run(name, dir, buildFile, exp, minSize, maxSize, nMax, strict, useNoBuild, mean, network, seed, roadFile, facilityFile, leisureFile, busFile, trainFile, restrictFile, minX, minY,width, height, index, results);
	}
}
