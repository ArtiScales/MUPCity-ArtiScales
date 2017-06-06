package org.thema.mupcity.exp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;

import org.geotools.feature.SchemaException;
import org.opengis.referencing.FactoryException;
import org.thema.common.swing.TaskMonitor;
import org.thema.mupcity.AHP;
import org.thema.mupcity.Project;
import org.thema.mupcity.rule.OriginDistance;
import org.thema.mupcity.rule.Rule;
import org.thema.mupcity.scenario.ScenarioAuto;

public class EffetSeuil {

	public static void main(File folderData, File folderOut) throws IOException, SchemaException {

		File dir = folderOut;

		for (int ii = 1; ii <= 5; ii++) {
			// définition des variables fixes


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

			// empty monitor
			TaskMonitor mon = new TaskMonitor.EmptyMonitor();
			// definition de la grille
			double width = 28303;
			double height = 21019;
			double minX = 914760;
			double minY = 6680157;
			
			// variation of the threshold
			double seuilDensBuild = 0.0;// NO PARAMETER FOR THAT

			switch (ii) {
			case 1:
				seuilDensBuild = 0.00001; //equivalent to delete buildings inferior to m² at a 180m scale	
				dir = new File(folderOut, "/results/seuil_10-5");
				break;
			case 2:
				seuilDensBuild = 0.0001;
				dir = new File(folderOut, "/results/seuil_10-4");
				break;
			case 3:
				seuilDensBuild = 0.001;
				dir = new File(folderOut, "/results/seuil_10-3");
				break;
			case 4:
				seuilDensBuild = 0.01;
				dir = new File(folderOut, "/results/seuil_10-2");
				break;
			case 5:
				seuilDensBuild = 0;
				dir = new File(folderOut, "/results/seuil_10-6");//dans un dossier 10^-6 pour faciliter les traitements, mais le seuil est nul
				break;
			}
			
			// create a new project
			dir.mkdirs();
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
			int nMax = 6;
			boolean strict = true;
			AHP ahp = ahpList.get(0);
			boolean mean = true;
			NavigableSet<Double> res = project.getMSGrid().getResolutions();

			for (int zi = 0; zi <=10; zi++){
			long seed = zi;
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
