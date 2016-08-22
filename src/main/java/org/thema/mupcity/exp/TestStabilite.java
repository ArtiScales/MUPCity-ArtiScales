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
import org.thema.mupcity.rule.OriginDistance;
import org.thema.mupcity.rule.Rule;
import org.thema.mupcity.scenario.ScenarioAuto;

public class TestStabilite {
	public static void main(File folderData, File folderOut) throws IOException, SchemaException {

		for (int ii = 0; ii < 3; ii++) {

			File dir = new File(folderOut,"/Ba/results/");
			switch (ii) {
			case 1:
				dir = new File(folderOut,"/St/results/");
				break;
			case 2:
				dir = new File(folderOut,"/Yag/results/");
				break;
			}
			dir.mkdirs();
			// définition des variables fixes
			String name = "testExplo";
			File buildFile = new File(folderData, "BATI_AU.shp");
			int exp = 3;
			double minSize = 20;
			double maxSize = 25273;
			boolean useNoBuild = true;
			boolean network = true;// true => network distance
			File roadFile = new File(folderData, "route_sans_chemin.shp");
			File facilityFile = new File(folderData, "CS_au_besac_sirene_2012.shp");
			File leisureFile = new File(folderData, "loisirs.shp");
			File busFile = new File(folderData , "stations_besac_tram_2015.shp");
			File trainFile = new File(folderData , "gare_train_ICONE_docs_2015.shp");
			File restrictFile = new File(folderData , "non_urba.shp");
			double seuilDensBuild = 0.0;// NO PARAMETER FOR THAT
			// empty monitor
			TaskMonitor mon = new TaskMonitor.EmptyMonitor();
			// definition de la grille
			double width = 28303;
			double height = 21019;
			double minX = 914760;
			double minY = 6680157;
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
			project.setLayer(Project.LAYERS.get(Project.Layers.RESTRICT.ordinal()), restrictFile, emptyAttrs);
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
			switch (ii) {
			case 1:
				strict = true;
				break;
			}
			AHP ahp = ahpList.get(0);
			boolean mean = true;
			switch (ii) {
			case 2:
				mean = false;
				break;
			}
			NavigableSet<Double> res = project.getMSGrid().getResolutions();

			for (int i = 0; i < 1000; i++) {
				Random random = new Random();
				long seed = random.nextLong();
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
}
