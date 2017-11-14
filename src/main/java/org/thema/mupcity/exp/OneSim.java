package org.thema.mupcity.exp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import org.geotools.feature.SchemaException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.thema.common.swing.TaskMonitor;
import org.thema.mupcity.AHP;
import org.thema.mupcity.Project;
import org.thema.mupcity.Project.Layers;
import org.thema.mupcity.rule.OriginDistance;
import org.thema.mupcity.rule.Rule;
import org.thema.mupcity.scenario.ScenarioAuto;

public class OneSim {

	public static void main(File folderData, File folderOut, Param param) throws IOException, SchemaException, NoSuchAuthorityCodeException, FactoryException {
		// TODO Auto-generated method stub

		/**
		 * Create automatic tests and loop on defined values
		 * 
		 * @author Maxime Colomb
		 */

		File dir = new File(folderOut, "Yag2");
		String name = "Yag2";
		//double minSize = 18;
		
//		for (int i = 0; i <= 3; i++) {
//			switch (i) {
//			case 1:
//				name = "19m";
//				dir = new File(folderOut, "19m");
//				minSize = 19;
//				break;
//			case 2:
//				name = "21m";
//				dir = new File(folderOut, "21m");
//				minSize = 21;
//				break;
//			case 3:
//				name = "22m";
//				dir = new File(folderOut, "22m");
//				minSize = 22;
//				break;
//			}

			dir.mkdirs();
			File buildFile = new File(folderData, "BATI_AU.shp");
			int exp = 3;
			double minSize = 20;
			double maxSize = 1620;
			boolean useNoBuild = true;
			boolean network = true;// true => network distance
			File roadFile = new File(folderData, "route_sans_chemin.shp");
			File facilityFile = new File(folderData, "CS_au_besac_sirene_2012.shp");
			File leisureFile = new File(folderData, "loisirs.shp");
			File busFile = new File(folderData, "stations_besac_tram_2015.shp");
			File trainFile = new File(folderData, "gare_train_ICONE_docs_2015.shp");
			File restrictFile = new File(folderData, "non_urba.shp");
			double seuilDensBuild = param.getSeuilDens();
			boolean isTest = false; // si l'on veut tester le programme, quelques shortcuts pour que ça aille plus vite

			// empty monitor
			TaskMonitor mon = new TaskMonitor.EmptyMonitor();

			double width = 28303;
			double height = 21019;
			double minX = 914760;
			double minY = 6680157;
			if (isTest) {
				width = width / 6;
				height = height / 6;
			}

			// create a new project
			Project project = Project.createProject(name, dir, buildFile, minX, minY, width, height, mon);
			project.setNetPrecision(0); // Le réseau routier apparait peut être un peu moins déformé avec cette contrainte, mais ce n'est pas pour ça qu'il n'y a plus de tache =0 dans fac3
			// set layers and attributes for the decomposition
			List<String> roadAttrs = Arrays.asList("Speed");// SPEED(numeric)
			project.setLayer(Project.LAYERS.get(Project.Layers.ROAD.ordinal()), roadFile, roadAttrs);
			List<String> facilityAttrs = Arrays.asList("LEVEL", "TYPE");// LEVEL (numeric), TYPE (any)
			project.setLayer(Project.LAYERS.get(Project.Layers.FACILITY.ordinal()), facilityFile, facilityAttrs);
			List<String> leisureAttrs = Arrays.asList("LEVEL", "TYPE");// LEVEL (numeric), TYPE (any)
			project.setLayer(Project.LAYERS.get(Project.Layers.LEISURE.ordinal()), leisureFile, leisureAttrs);
			List<String> emptyAttrs = Arrays.asList("");
			project.setLayer(Project.LAYERS.get(Project.Layers.BUS_STATION.ordinal()), busFile, emptyAttrs);
			project.setLayer(Project.LAYERS.get(Project.Layers.TRAIN_STATION.ordinal()), trainFile, emptyAttrs);
			project.setLayer(Project.LAYERS.get(Project.Layers.RESTRICT.ordinal()), restrictFile, emptyAttrs);
			project.setDistType((network) ? OriginDistance.NetworkDistance.class : OriginDistance.EuclideanDistance.class);

			// setting of the six different AHP matrix
			// we first take the names of the different working rules
			List<String> items = new ArrayList<>();
			for (Rule rule : project.getRules()) {
				if (rule.isUsable(project)) {
					items.add(rule.getName());
				}
			}

			// create new decomp
			project.decomp(exp, maxSize, minSize, seuilDensBuild, mon, false);
			project.save();
			// looping for scenarios
			// loop on Nmax
			int nMax = param.getN();
			boolean strict = param.isStrict();
			AHP ahp = param.getAhp();
			boolean mean = param.isMean();
			String titre = param.getNameSc();
			long seed = 42;
			NavigableSet<Double> res = project.getMSGrid().getResolutions();
			ScenarioAuto scenario = ScenarioAuto.createMultiScaleScenario(titre, res.first(), res.last(), nMax, strict, ahp, useNoBuild, mean, exp, seed, false, false);
			project.performScenarioAuto(scenario);
			// save the project
			scenario.save(dir, project);
			scenario.extractEvalAnal(dir, project);
			project.getMSGrid().save(dir);
			System.out.println("layers : " + project.getMSGrid().getLayers());
			project.getMSGrid().saveRaster(titre + "-eval", dir);
			// delete of the saved layer to unload the heap space
			project.getMSGrid().removeLayer(titre + "-morpho");
			project.getMSGrid().removeLayer(titre + "-eval_anal");
			project.getMSGrid().removeLayer(titre + "-analyse");
			project.getMSGrid().removeLayer(titre + "-eval");
			// write the seed into a text file, uselesse now as the seeds are fixed
			Charset charset = Charset.forName("US-ASCII");
			String nseed = String.valueOf(seed);
			File testFiletext = new File(dir + "/nbseed");
			String crS = project.getCRS().toString();
			try (BufferedWriter nbseed = Files.newBufferedWriter(testFiletext.toPath(), charset)) {
				nbseed.write(nseed);
				nbseed.write(crS);
				nbseed.write(project.getCRS().toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	//}
}
