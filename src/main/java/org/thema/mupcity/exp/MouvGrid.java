package org.thema.mupcity.exp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;

import org.geotools.feature.SchemaException;
import org.thema.common.swing.TaskMonitor;
import org.thema.mupcity.AHP;
import org.thema.mupcity.Project;
import org.thema.mupcity.rule.OriginDistance;
import org.thema.mupcity.rule.Rule;
import org.thema.mupcity.scenario.ScenarioAuto;

import com.google.common.io.Files;

public class MouvGrid {

	public static void main(File folderData, File folderOut) throws IOException, SchemaException {

		// définition des variables fixes

		File dir = folderOut;

		int exp = 3;
		double minSize = 20;
		double maxSize = 25273;
		boolean useNoBuild = true;
		boolean network = true;// true => network distance
		String name = "testExplo";
		File buildFile = new File(folderData, "BATI_AU.shp");

		File roadFile = new File(folderData, "route_sans_chemin.shp");
		File facilityFile = new File(folderData, "CS_au_besac_sirene_2012.shp");
		File leisureFile = new File(folderData, "loisirs.shp");
		File busFile = new File(folderData, "stations_besac_tram_2015.shp");
		File trainFile = new File(folderData, "gare_train_ICONE_docs_2015.shp");
		File restrictFile = new File(folderData, "non_urba.shp");

		double seuilDensBuild = 0.0;// NO PARAMETER FOR THAT
		// empty monitor
		TaskMonitor mon = new TaskMonitor.EmptyMonitor();

		for (int ii = 0; ii <= 17; ii++) {

			// definition de la grille
			int scale = 0;
			String partName = "";

			//variables to make it happened on different grid moves
			int petitPlus = 0;
			int jj = ii;

			if (ii <= 8) {
				scale = 20;
				partName = "decal-20";
			} else if (ii > 8) {
				scale = 180;
				partName = "decal-180";
				jj = ii - 8;
				petitPlus = 1;
			}

			double width = 28303;
			double height = 21019;
			double minX = 914760;
			double minY = 6680157;

			switch (jj) {
			case 1:
				minX = minX + scale;
				dir = new File(folderOut, partName + "/G" + (jj + petitPlus));
				break;
			case 2:
				minX = minX + scale;
				minY = minY + scale;
				dir = new File(folderOut, partName + "/G" + (jj + petitPlus));
				break;
			case 3:
				minY = minY + scale;
				dir = new File(folderOut, partName + "/G" + (jj + petitPlus));
				break;
			case 4:
				minX = minX - scale;
				minY = minY + scale;
				dir = new File(folderOut, partName + "/G" + (jj + petitPlus));
				break;
			case 5:
				minX = minX - scale;
				dir = new File(folderOut, partName + "/G" + (jj + petitPlus));
				break;
			case 6:
				minX = minX - scale;
				minY = minY - scale;
				dir = new File(folderOut, partName + "/G" + (jj + petitPlus));
				break;
			case 7:
				minY = minY - scale;
				dir = new File(folderOut, partName + "/G" + (jj + petitPlus));
				break;
			case 8:
				minX = minX + scale;
				minY = minY - scale;
				dir = new File(folderOut, partName + "/G" + (ii + petitPlus));
				break;
			}
			dir.mkdirs();
			if (ii == 9) {
				File[] aCopier = new File(folderOut, "decal-20/G0/").listFiles();
				System.out.println(aCopier);
				File copierVers = new File(folderOut, "decal-180/G0/");
				copierVers.mkdirs();
				for (File aCp : aCopier) {
					Files.copy(aCp, copierVers);
				}
			}
			// create a new project

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
			project.setLayer(Project.LAYERS.get(Project.Layers.RESTRICT.ordinal()), restrictFile, emptyAttrs); //provoque un GC limit overhead
			project.setDistType((network) ? OriginDistance.NetworkDistance.class : OriginDistance.EuclideanDistance.class);
			// setting of AHP matrix
			List<String> items = new ArrayList<>();
			for (Rule rule : project.getRules()) {
				if (rule.isUsable(project)) {
					items.add(rule.getName());
				}
			}
			// setting on our six ahp objects
			AHP ahpS_Moy = new AHP(items);
			ahpS_Moy.setCoef(items.get(8), 0.083);
			ahpS_Moy.setCoef(items.get(7), 0.083);
			ahpS_Moy.setCoef(items.get(6), 0.083);
			ahpS_Moy.setCoef(items.get(5), 0.04);
			ahpS_Moy.setCoef(items.get(4), 0.218);
			ahpS_Moy.setCoef(items.get(3), 0.218);
			ahpS_Moy.setCoef(items.get(2), 0.218);
			ahpS_Moy.setCoef(items.get(1), 0.03);
			ahpS_Moy.setCoef(items.get(0), 0.027);

			// list of AHP to loop in
			List<AHP> ahpList = new ArrayList<AHP>();
			ahpList.add(ahpS_Moy);
			project.decomp(exp, maxSize, minSize, seuilDensBuild, mon, false);
			project.save();

			//réplication du scénario N5-Ba-ahpS-Moy
			int nMax = 5;
			boolean strict = false;
			AHP ahp = ahpList.get(0);
			boolean mean = true;
			NavigableSet<Double> res = project.getMSGrid().getResolutions();

			long seed = 42;
			String titre = new String("replication_numero-" + seed);
			ScenarioAuto scenario = ScenarioAuto.createMultiScaleScenario(titre, res.first(), res.last(), nMax, strict, ahp, useNoBuild, mean, exp, seed, false, false);
			project.performScenarioAuto(scenario);
			scenario.extractEvalAnal(dir, project);
			// delete of the saved layer to unload the heap space
			project.getMSGrid().removeLayer(titre + "-morpho");
			project.getMSGrid().removeLayer(titre + "-eval_anal");
			project.getMSGrid().removeLayer(titre + "-analyse");
			project.getMSGrid().removeLayer(titre + "-eval");

		}
	}
}
