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

public class MouvData {

	public static void main(File folderData, File folderOut, Param param) throws IOException, SchemaException {

		for (int decalage = 1; decalage <= 9; decalage = decalage * 3) {
			System.out.println("decal : " + decalage);
			for (int ii = 0; ii <= 8; ii++) {
				// définition des variables fixes

				File dir = new File(folderOut, decalage + "m/data" + (ii));
				File dirData = new File(dir, "/data");
				int exp = 3;
				double minSize = 20;
				if(decalage==3){
					minSize =60;
				}
				else if (decalage==9){
					minSize=180;
				}
				double maxSize = 25273;
				boolean useNoBuild = true;
				boolean network = true;// true => network distance
				String name = "testExplo";
				File buildFile = new File(dirData, "BATI_AU.shp");
				File roadFile = new File(dirData, "route_sans_chemin.shp");
				File facilityFile = new File(dirData, "CS_au_besac_sirene_2012.shp");
				File leisureFile = new File(dirData, "loisirs.shp");
				File busFile = new File(dirData, "stations_besac_tram_2015.shp");
				File trainFile = new File(dirData, "gare_train_ICONE_docs_2015.shp");
				File restrictFile = new File(dirData, "non_urba.shp");

				double seuilDensBuild = 0.0;
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
				project.setLayer(Project.LAYERS.get(Project.Layers.RESTRICT.ordinal()), restrictFile, emptyAttrs); //provoque un GC limit overhead
				project.setDistType((network) ? OriginDistance.NetworkDistance.class : OriginDistance.EuclideanDistance.class);
				
				project.decomp(exp, maxSize, minSize, seuilDensBuild, mon, false);
				project.save();
				
				NavigableSet<Double> res = project.getMSGrid().getResolutions();
				long seed = 42;

				for (int jj = 0; jj < 2; jj++) {

					String str = "strict";
					boolean strict = true;
					if (jj == 1) {
						str = "basique";
						strict = false;
					}
					String titre = new String("replication_numero-" + str + "_" + seed);
					ScenarioAuto scenario = ScenarioAuto.createMultiScaleScenario(titre, res.first(), res.last(), param.getN(), strict, param.getAhp(), useNoBuild, param.isMean(), exp, seed, false, false);
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
}
