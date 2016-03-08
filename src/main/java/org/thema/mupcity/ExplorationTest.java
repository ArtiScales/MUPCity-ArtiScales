package org.thema.mupcity;


import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableSet;
import java.util.Random;

import org.geotools.feature.SchemaException;
import org.thema.common.swing.TaskMonitor;
import org.thema.mupcity.rule.OriginDistance;
import org.thema.mupcity.rule.Rule;
import org.thema.mupcity.scenario.ScenarioAuto;

/**
 * Create automatic tests and loop on defined values
 * 
 * @author Maxime Colomb
 */

public class ExplorationTest {
		public static void main(String[] args) throws IOException, SchemaException {
		
			//définition des variables fixes
		String name = "testExplo";		
		File dir = new File("/home/mcolomb/informatique/MUP/explo/result");
		File buildFile = new File("/home/mcolomb/informatique/MUP/explo/data/BATI_AU.shp");
		int exp = 3;
		double minSize = 20;
		double maxSize = 25273;// d'ou sors cette valeur? (excépté du gui)
		boolean useNoBuild = true;
		boolean network = true;//true => network distance	
		File roadFile = new File("/home/mcolomb/informatique/MUP/explo/data/ROUTE_AU_ss_autoroutes_et_chemins.shp");
		File facilityFile = new File("/home/mcolomb/informatique/MUP/explo/data/CS_au_besac_sirene_2012.shp");
		//File leisureFile = new File("home/mcolomb/informatique/MUP/explo/data/");
		File busFile = new File("/home/mcolomb/informatique/MUP/explo/data/stations_besac_tram_2015.shp");
		File trainFile = new File("/home/mcolomb/informatique/MUP/explo/data/gare_train_ICONE_docs_2015.shp");
		File restrictFile = new File("/home/mcolomb/informatique/MUP/explo/data/ICONE-zonesNU_AU.shp");
		double seuilDensBuild = 0.0;// NO PARAMETER FOR THAT
			// empty monitor
		TaskMonitor mon = new TaskMonitor.EmptyMonitor();
			
			//definitiojn de la grille
			// /3 pour réduire l'emprise et la durée des tests
		double width = 52246.700000005076/3;
		double height = 50546.80000000168/3;
		double minX = 898590.4999999964;
		double minY = 6661379.800000002;
		String g = "G1";
			//variation de la grille -- 3 valeurs
		for (int a=0; a<=2; a++){
			switch(a){
			case 1:
				minX = minX+100;
				g = "G2";
				System.out.println("première grille calculé");
				break;
			case 2:
				minX = minX-50;
				minY = minY+100;
				g = "G3";
				System.out.println("deuxième grille calculé");
				break;}
			
				//create a new project
			Project project = Project.createProject(name, dir, buildFile, minX, minY, width, height, mon);
	
				// set layers and attributes for the decomposition
			List<String> roadAttrs = Arrays.asList("speed");// SPEED(numeric)
			project.setLayer(Project.LAYERS.get(Project.Layers.ROAD.ordinal()), roadFile, roadAttrs, mon);
			List<String> facilityAttrs = Arrays.asList("LEVEL", "NATURE");// LEVEL (numeric), TYPE (any)
			project.setLayer(Project.LAYERS.get(Project.Layers.FACILITY.ordinal()), facilityFile, facilityAttrs, mon);
			//List<String> leisureAttrs = Arrays.asList("LEVEL", "NATURE");// LEVEL (numeric), TYPE (any)
			//project.setLayer(Project.LAYERS.get(Project.Layers.LEISURE.ordinal()), leisureFile, leisureAttrs, mon);
			List<String> emptyAttrs = Arrays.asList("");
			project.setLayer(Project.LAYERS.get(Project.Layers.BUS_STATION.ordinal()), busFile, emptyAttrs, mon);
			project.setLayer(Project.LAYERS.get(Project.Layers.TRAIN_STATION.ordinal()), trainFile, emptyAttrs, mon);
			project.setLayer(Project.LAYERS.get(Project.Layers.RESTRICT.ordinal()), restrictFile, emptyAttrs, mon);
			project.setDistType((network)?OriginDistance.NetworkDistance.class:OriginDistance.EuclideanDistance.class);
	
			
				//setting of the six different AHP matrix
				//we first take the names of the different working rules
			List<String> items = new ArrayList<>();
				for (Rule rule : project.getRules()){ 
					if (rule.isUsable(project)){ 
						items.add(rule.getName());
					}
				}
				
				//setting on our six ahp objects	
			AHP ahpE_Yag = new AHP(items);//creation of the true objects
			ahpE_Yag.realName = "ahpE_Yag";//putting a String name
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
			
			HashMap<String, Double> coefsE_Yag = new HashMap<>();//collection of the eigenvector values
			coefsE_Yag.put(items.get(5), 1.0);	
			coefsE_Yag.put(items.get(4), 1.0);				        
	        coefsE_Yag.put(items.get(3), 1.0);
	        coefsE_Yag.put(items.get(2), 1.0);			        
	        coefsE_Yag.put(items.get(1), 1.0);
			coefsE_Yag.put(items.get(0), 1.0);
			ahpE_Yag.setCoeff(coefsE_Yag);//we force the vector to the ahp objects
			
			HashMap<String, Double> coefsT_Yag = new HashMap<>();
			coefsT_Yag.put(items.get(5), 3.136);	
			coefsT_Yag.put(items.get(4), 0.448);				        
	        coefsT_Yag.put(items.get(3), 0.448);
	        coefsT_Yag.put(items.get(2), 1.33);			        
	        coefsT_Yag.put(items.get(1), 0.19);
			coefsT_Yag.put(items.get(0), 0.448);
	        ahpT_Yag.setCoeff(coefsT_Yag);
			
			HashMap<String, Double> coefsS_Yag = new HashMap<>();
			coefsS_Yag.put(items.get(5), 1.365);
			coefsS_Yag.put(items.get(4), 0.483);
	        coefsS_Yag.put(items.get(3), 0.483);
	        coefsS_Yag.put(items.get(2), 2.695);			        
	        coefsS_Yag.put(items.get(1), 0.215);
			coefsS_Yag.put(items.get(0), 0.76);
	        ahpS_Yag.setCoeff(coefsS_Yag);
	
			HashMap<String, Double> coefsE_Moy = new HashMap<>();
			coefsE_Moy.put(items.get(5), 0.167);	
			coefsE_Moy.put(items.get(4), 0.167);				        
	        coefsE_Moy.put(items.get(3), 0.167);
	        coefsE_Moy.put(items.get(2), 0.167);			        
	        coefsE_Moy.put(items.get(1), 0.167);
			coefsE_Moy.put(items.get(0), 0.167);
			ahpE_Moy.setCoeff(coefsE_Moy);
			
			HashMap<String, Double> coefsT_Moy = new HashMap<>();
			coefsT_Moy.put(items.get(5), 0.523);	
			coefsT_Moy.put(items.get(4), 0.075);				        
	        coefsT_Moy.put(items.get(3), 0.075);
	        coefsT_Moy.put(items.get(2), 0.222);			        
	        coefsT_Moy.put(items.get(1), 0.032);
			coefsT_Moy.put(items.get(0), 0.075);
	        ahpT_Moy.setCoeff(coefsT_Moy);
	        
			HashMap<String, Double> coefsS_Moy = new HashMap<>();
			coefsS_Moy.put(items.get(5), 0.227);
			coefsS_Moy.put(items.get(4), 0.08);
	        coefsS_Moy.put(items.get(3), 0.08);
	        coefsS_Moy.put(items.get(2), 0.449);			        
	        coefsS_Moy.put(items.get(1), 0.036);
			coefsS_Moy.put(items.get(0), 0.127);
	        ahpS_Moy.setCoeff(coefsS_Moy);
	        
	        	//list of AHP to loop in
	        List<AHP> ahpList = new ArrayList<AHP>();
	        ahpList.add(ahpE_Yag);
	        ahpList.add(ahpT_Yag);
	        ahpList.add(ahpS_Yag);
	        ahpList.add(ahpE_Moy);
	        ahpList.add(ahpT_Moy);
	        ahpList.add(ahpS_Moy);
			
				//create new decomp
	        project.decomp(exp, maxSize, minSize, seuilDensBuild, mon);	
	        project.save();
				
	        	//looping for scenarios
				//loop on Nmax
			for(int nMax = 3 ; nMax <= 7; nMax++ ){
				String nname = "N"+nMax;//part of the folder's name
				
					//loop on strict/basic
				for (int s = 0 ; s <= 1; s++){
					String nstrict;//part of the folder's name
					boolean strict;
					if (s==0) {	
						strict=true; 
						nstrict = "St";}
					else {
						strict=false;
						nstrict = "Ba";}
							
				        	//loop on the AHP
					for (AHP ahp : ahpList ){
				        
						String nahp = ahp.getName();
				        int lgt = nahp.length();
				        boolean mean ; //determination de par le nom de l'ahp si la methode de calcul sera avec mean ou Yager
				        if (nahp.substring(lgt-3).equals("Moy")) {
						        mean = true;}
				        else{mean = false;}
						for (int se=0; se<=2;se++){     	
							Random random=new Random();
							long seed = random.nextLong();//outsourcing the seed
							String titre = g+"--"+nname+"--"+nstrict+"--"+nahp;//part of the folder's name
							int nameseed = se+1;//part of the folder's name
					        File testFile = new File("/home/mcolomb/informatique/MUP/explo/result/testExplo/"+g+"/"+nMax+"/"+titre+"/"+"replication_"+nameseed);
					        NavigableSet<Double> res = project.getMSGrid().getResolutions();
							ScenarioAuto scenario = ScenarioAuto.createMultiScaleScenario(titre, res.first(), res.last(), nMax, strict, ahp, useNoBuild, mean, exp, seed);
							project.performScenarioAuto(scenario);
		
								// save the project
							scenario.save(project, testFile);
							project.getMSGrid().saveRaster(scenario.getEvalLayerName(), testFile); //ne marchait pas dans la methode scenario.save mais marche ici..
														
								// write the seed into a text file
							Charset charset = Charset.forName("US-ASCII");
							String nseed = String.valueOf(seed);
							File testFiletext = new File (testFile+"/nbseed");
							try (BufferedWriter nbseed = Files.newBufferedWriter(testFiletext.toPath(), charset)){ 
							nbseed.write(nseed);}
							catch (IOException e){
								e.printStackTrace();
									}
							
							}
			        	}
					}	
				}
			}		
		}
}

