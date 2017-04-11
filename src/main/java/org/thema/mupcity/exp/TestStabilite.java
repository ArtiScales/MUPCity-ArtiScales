package org.thema.mupcity.exp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.Random;

import org.geotools.feature.SchemaException;
import org.thema.common.swing.TaskMonitor;
import org.thema.mupcity.AHP;
import org.thema.mupcity.Project;
import org.thema.mupcity.Project.Layers;
import org.thema.mupcity.analyse.RasterAnalyse;
import org.thema.mupcity.rule.OriginDistance;
import org.thema.mupcity.rule.Rule;
import org.thema.mupcity.scenario.ScenarioAuto;

public class TestStabilite {
	public static void main(File folderData, File folderOut, Param param) throws Exception {
		String nMean;
		String nStrict;
		if (param.isMean()) {
			nMean = "Moy";
		} else {
			nMean = "Yag";
		}
		if (param.isStrict()) {
			nStrict = "St";
		} else {
			nStrict = "Ba";
		}
		String nameSimu = "N" + String.valueOf(param.getN() + nMean + nStrict);
		File dir = new File(folderOut, nameSimu);
		dir.mkdirs();
		// définition des variables fixes
		String name = "testStabilite";
		File buildFile = new File(folderData, "BATI_AU.shp");
		int exp = 3;
		double minSize = 20;
		double maxSize = 25273;
		boolean useNoBuild = true;
		boolean network = true;// true => network distance
		File roadFile = new File(folderData, "route_sans_chemin.shp");
		File facilityFile = new File(folderData, "CS_au_besac_sirene_2012.shp");
		File leisureFile = new File(folderData, "loisirs.shp");
		File busFile = new File(folderData, "stations_besac_tram_2015.shp");
		File trainFile = new File(folderData, "gare_train_ICONE_docs_2015.shp");
		File restrictFile = new File(folderData, "non_urba.shp");
		double seuilDensBuild = param.getSeuilDens();
		// empty monitor
		TaskMonitor mon = new TaskMonitor.EmptyMonitor();
		// definition de la grille
		boolean isTest = false;

		double width = 28303;
		double height = 21019;
		double minX = 914760;
		double minY = 6680157;
		// create a new project
		if (isTest) {
			width = width / 13;
			height = height / 13;
		}
		Project project = Project.createProject(name, dir, buildFile, minX, minY, width, height, mon);
		project.setNetPrecision(0.1); // Le réseau routier apparait peut être un peu moins déformé avec cette contrainte, mais ce n'est pas pour ça qu'il n'y a plus detache =0 dans fac3
		// set layers and attributes for the decomposition
		List<String> roadAttrs = Arrays.asList("Speed");// SPEED(numeric)
		project.setLayer(Project.LAYERS.get(Project.Layers.ROAD.ordinal()), roadFile, roadAttrs);
		List<String> facilityAttrs = Arrays.asList("LEVEL", "TYPE");// LEVEL(numeric),TYPE (any)
		project.setLayer(Project.LAYERS.get(Project.Layers.FACILITY.ordinal()), facilityFile, facilityAttrs);
		List<String> leisureAttrs = Arrays.asList("LEVEL", "TYPE");// LEVEL(numeric),TYPE (any)
		project.setLayer(Project.LAYERS.get(Project.Layers.LEISURE.ordinal()), leisureFile, leisureAttrs);
		List<String> emptyAttrs = Arrays.asList("");
		project.setLayer(Project.LAYERS.get(Project.Layers.BUS_STATION.ordinal()), busFile, emptyAttrs);
		project.setLayer(Project.LAYERS.get(Project.Layers.TRAIN_STATION.ordinal()), trainFile, emptyAttrs);
		project.setLayer(Project.LAYERS.get(Project.Layers.RESTRICT.ordinal()), restrictFile, emptyAttrs);
		project.setDistType((network) ? OriginDistance.NetworkDistance.class : OriginDistance.EuclideanDistance.class);
		System.out.println("decomp started");
		project.decomp(exp, maxSize, minSize, seuilDensBuild, mon, false);
		project.save();

		int nMax = param.getN();
		boolean strict = param.isStrict();
		boolean mean = param.isMean();
		AHP ahp = param.getAhp();
		NavigableSet<Double> res = project.getMSGrid().getResolutions();
		System.out.println("scenar started");
		for (int i = 0; i < param.getRepli(); i++) {
			Random random = new Random();
			long seed = random.nextLong();
			String titre = new String("replication_numero-" + seed);
			ScenarioAuto scenario = ScenarioAuto.createMultiScaleScenario(titre, res.first(), res.last(), nMax, strict, ahp, useNoBuild, mean, exp, seed, false, false);
			project.performScenarioAuto(scenario);
			scenario.extractEvalAnal(dir, project);
			//enregistrement des info scénar dans un autre dossier (seulement pour la première itération)
			File scenarIni = new File(dir, nameSimu + "ini");
			if (i == 0) {
				project.save(scenarIni);
				scenario.save(scenarIni, project);
				project.saveGridLayer(titre + "-eval");
			}
			// delete of the saved layer to unload the heap space
			project.getMSGrid().removeLayer(titre + "-morpho");
			project.getMSGrid().removeLayer(titre + "-eval_anal");
			project.getMSGrid().removeLayer(titre + "-analyse");
			project.getMSGrid().removeLayer(titre + "-eval");
		}
		//launch the analysis methods 
		for (Integer echl = 20; echl <= 180; echl = echl * 3) {
			RasterAnalyse.rootFile = dir;
			RasterAnalyse.echelle = echl.toString();
			RasterAnalyse.discrete = false;
			RasterAnalyse.replicationStab();
			RasterAnalyse.discrete = true;
			RasterAnalyse.replicationStab();
		}
	}
}
