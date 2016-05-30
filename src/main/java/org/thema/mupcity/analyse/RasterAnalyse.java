package org.thema.mupcity.analyse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Pattern;

import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.store.ContentFeatureCollection;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.DirectPosition;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

public class RasterAnalyse {

	public static File rootFile = new File("/media/mcolomb/Data_2/resultTest/");
	public static File discreteFile = new File("/home/mcolomb/informatique/MUP/explo/data/admin_typo.shp");
	public static boolean stabilite = false;

	/**
	 * Select files containing a defined String at a defined scale. It could be from the defined rootfile or from a selected array of files
	 * 
	 * @param with: string contained in the wanted file
	 * @param echelle: scale of the file
	 * @param in: array of file to search in (can be null)
	 * @return an ArrayList of File
	 * @throws IOException
	 */
	public static ArrayList<File> selectWith(String with, String echelle, ArrayList<File> in) throws IOException {
		ArrayList<File> listFile = new ArrayList<File>();
		if (in == null) {
			for (File fil : rootFile.listFiles()) {
				Pattern ech = Pattern.compile("eval_anal-");
				String[] list = ech.split(fil.toString());
				if (fil.toString().contains(with) && list[1].equals(echelle + ".0.tif")) {
					listFile.add(fil);
				}
			}
		} else {
			for (File fil : in) {
				Pattern ech = Pattern.compile("eval_anal-");
				String[] list = ech.split(fil.toString());
				if (fil.toString().contains(with) && list[1].equals(echelle + ".0.tif")) {
					listFile.add(fil);
				}
			}
		}
		return listFile;
	}
	/**
	 * tableau final à envoyer avec les statistiques, ou les valeurs seront orgainsé comme ceci : index 0 : moyenne du nombre de cellules index 1
	 * 
	 * @author Maxime Colomb
	 * @return 0 : moyenne du nombre de cellules a urbaniser
	 * @return 1 : ecart type des différentes valeurs totales de cellules a urbaniser.
	 * @return 2 : coefficient de variation du nombre de cellules sélectionné
	 * @return 3 : premier décile du nombre de réplication des cellules sélectionnées
	 * @return 4 : premier quartile du nombre de réplication des cellules sélectionnées
	 * @return 5 : médiane du nombre de réplication des cellules sélectionnées
	 * @return 6: dernier quartile du nombre de réplication des cellules sélectionnées
	 * @return 7 : dernier décile du nombre de réplication des cellules sélectionnées
	 * @return 8 : coefficient de corrélation entre le nombre de sélection des cellules et leurs évaluations
	 * @return 9 à 18 : discrétisation du nombre de cellules répliqué (uniquement pour les tests de réplications sur de petits sets)
	 * @throws IOException
	 * @throws TransformException
	 */
	public static Hashtable<String, double[]> replication(String echelle, boolean isDiscrete) throws IOException, TransformException {
		Hashtable<String, double[]> TableauRepliTotal = new Hashtable<String, double[]>();
		for (int n = 3; n <= 7; n++) {
			String N = new String("N" + n);
			for (int s = 0; s <= 1; s++) {
				String strict = "St";// part of the folder's name
				if (s == 1) {
					strict = "Ba";
				}
				for (int ah = 0; ah <= 2; ah++) {
					String ahp = "ahpS";// part of the folder's name
					if (ah == 1) {
						ahp = "ahpE";
					} else if (ah == 2) {
						ahp = "ahpT";
					}
					for (int agg = 0; agg <= 1; agg++) {
						String aggreg = "Yag";
						if (agg == 1) {
							aggreg = "Moy";
						}
						String eachTest = new String(N + "--" + strict + "--" + ahp + "_" + aggreg);
						ArrayList<File> listRepliFile = selectWith(eachTest, echelle, null);
						System.out.println("pour le scenario " + eachTest);
						double[] TableauRepliScenar;
						if (isDiscrete) { // different function if we need to discretize the area within different urban types

							TableauRepliScenar = mergeRasters(listRepliFile, false);
						} else {
							TableauRepliScenar = mergeRasters(listRepliFile);
						}
						TableauRepliTotal.put(eachTest, TableauRepliScenar);
						for (int i = 0; i <= 7; i++) {
						}
					}
				}
			}
		}
		return TableauRepliTotal;
	}
	
	/** 
	 * anaysis of a lot of tests 
	 * @param echelle: scale of the file
	 * @param isDiscrete if the process has to discretise the output cells within a shape file
	 * @return a collection of a scenario name with his analysis array (described in the margeRaster  method)
	 * @throws IOException
	 * @throws TransformException
	 */
	public static Hashtable<String, double[]> replicationStab(String echelle, boolean isDiscrete) throws IOException, TransformException {
		stabilite = true;
		Hashtable<String, double[]> TableauRepliTotal = new Hashtable<String, double[]>();
		ArrayList<File> listRepliFile = selectWith("replication_numero", echelle, null);
		System.out.println(listRepliFile);
		double[] TableauRepliStabl = mergeRasters(listRepliFile);
		TableauRepliTotal.put("stable", TableauRepliStabl);
		System.out.println("done");
		return TableauRepliTotal;
	}
	
	/**
	 * Compares replication by the AHP matrix choice
	 * 
	 * @param echelle: scale of the file
	 * @return a collection of a scenario name with his analysis array (described in the margeRaster  method)
	 * @throws IOException, TransformException
	 */
	
	public static Hashtable<String, double[]> compareAHP(String echelle) throws IOException, TransformException {

		Hashtable<String, double[]> TableauRepliTotal = new Hashtable<String, double[]>();
		ArrayList<File> oneSeed = selectWith("replication_7", echelle, null);
		for (int n = 3; n <= 7; n++) {
			String N = new String("N" + n);
			for (int s = 0; s <= 1; s++) {
				String strict = "St";// part of the folder's name
				if (s == 1) {
					strict = "Ba";
				}
				for (int agg = 0; agg <= 1; agg++) {
					String aggreg = "Yag";
					if (agg == 1) {
						aggreg = "Moy";
					}
					String TestNSt = new String(N + "--" + strict);
					ArrayList<File> tempList = selectWith(TestNSt, echelle, oneSeed);
					ArrayList<File> oneSeedAhp = selectWith(aggreg, echelle, tempList);
					double[] tableauAhp = mergeRasters(oneSeedAhp, false);
					String nameScenar = new String(TestNSt + "--" + aggreg);
					TableauRepliTotal.put(nameScenar, tableauAhp);
				}
			}
		}
		return TableauRepliTotal;
	}

	/**
	 * Merge the given Array of Files. Return an array of statistic values. Will also return a merged tif (in construction) 
	 * @param listRepliFile : ArrayList of File pointing to the raster layer to merge
	 * @return array of statistics results
	 * @throws IOException
	 * @throws TransformException
	 */
	public static double[] mergeRasters(ArrayList<File> listRepliFile) throws IOException, TransformException {

		DescriptiveStatistics statNb = new DescriptiveStatistics();
		Hashtable<GridCoordinates2D, Integer> cellRepet = new Hashtable<GridCoordinates2D, Integer>();
		Hashtable<GridCoordinates2D, ArrayList<Float>> cellEval = new Hashtable<GridCoordinates2D, ArrayList<Float>>();
		double[] tableauFinal = new double[19];
		if (stabilite) {
			tableauFinal = new double[9];
		}
		
		//creation of the merged raster, not working for now (I need to dig more into geotool classes)
		String nameMerged = new String(listRepliFile.get(1).toString()); 
		File pathmerged = new File( nameMerged+ "_merge");
		GeoTiffWriter rasterMerged = new GeoTiffWriter(pathmerged);
		rasterMerged.dispose();

		
		for (File f : listRepliFile) {
			// setting of useless parameters
			ParameterValue<OverviewPolicy> policy = AbstractGridFormat.OVERVIEW_POLICY.createValue();
			policy.setValue(OverviewPolicy.IGNORE);
			// this will basically read 4 tiles worth of data at once from the
			// disk...
			ParameterValue<String> gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();
			// Setting read type: use JAI ImageRead (true) or ImageReaders read
			// methods (false)
			ParameterValue<Boolean> useJaiRead = AbstractGridFormat.USE_JAI_IMAGEREAD.createValue();
			useJaiRead.setValue(false);
			GeneralParameterValue[] params = new GeneralParameterValue[] { policy, gridsize, useJaiRead };

			GridCoverage2DReader reader = new GeoTiffReader(f);
			GridCoverage2D coverage = reader.read(params);
			GridEnvelope dimensions = reader.getOriginalGridRange();
			GridCoordinates maxDimensions = dimensions.getHigh();

			int w = maxDimensions.getCoordinateValue(0) + 1;
			int h = maxDimensions.getCoordinateValue(1) + 1;
			int numBands = reader.getGridCoverageCount();
			double[] vals = new double[numBands];

			int compteurNombre = 0;

			
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					GridCoordinates2D coord = new GridCoordinates2D(i, j);
					if (coverage.evaluate(coord, vals)[0] > 0) {
						compteurNombre = compteurNombre + 1;
						if (cellRepet.containsKey(coord)) { // si la cellule a déja été sélectionné lors de réplications
							cellRepet.put(coord, cellRepet.get(coord) + 1);
							ArrayList<Float> temp = cellEval.get(coord); //on mets les valeurs d'évaluation dans un tableau
							temp.add((float) coverage.evaluate(coord, vals)[0]);
							cellEval.put(coord, temp);

						} else {// si la cellule est sélectionné pour la première fois
							cellRepet.put(coord, 1);
							ArrayList<Float> firstList = new ArrayList<Float>();
							firstList.add((float) coverage.evaluate(coord, vals)[0]);
							cellEval.put(coord, firstList);
						}
					}
				}
			}
			System.out.println("il y a " + compteurNombre + " cellules dans ce scenar");
			statNb.addValue(compteurNombre);
			System.out.println(cellRepet.size());
		}

		// des statistiques du merge des rasters

		Hashtable<GridCoordinates2D, Float> cellEvalFinal = new Hashtable<GridCoordinates2D, Float>();
		DescriptiveStatistics statRepli = new DescriptiveStatistics();

		// statistiques du nombre de cellules

		tableauFinal[0] = statNb.getMean();
		tableauFinal[1] = statNb.getStandardDeviation();
		tableauFinal[2] = tableauFinal[1] / tableauFinal[0];

		float moyenne = 0;
		// tableaux servant à calculer les coefficients de correlations
		double[] tableauMoy = new double[cellEval.size()];
		double[] tableauRepl = new double[cellRepet.size()];

		int j = 0;

		// calcul de la moyenne des evaluations
		for (GridCoordinates2D temp : cellEval.keySet()) {
			ArrayList<Float> tablTemp = cellEval.get(temp);
			for (float nombre : tablTemp) {
				moyenne = moyenne + nombre;
			}
			moyenne = moyenne / tablTemp.size();
			cellEvalFinal.put(temp, moyenne);
			tableauMoy[j] = moyenne;
			j = j + 1;
		}
		int i = 0;
		for (int repli : cellRepet.values()) {
			tableauRepl[i] = repli;
			i = i + 1;
			statRepli.addValue(repli);
		}
		tableauFinal[3] = statRepli.getPercentile(10);
		tableauFinal[4] = statRepli.getPercentile(25);
		tableauFinal[5] = statRepli.getPercentile(50);
		tableauFinal[6] = statRepli.getPercentile(75);
		tableauFinal[7] = statRepli.getPercentile(90);

		if (tableauMoy.length > 1) { // si il n'y a pas de cellules, la covrariance fait planter
			double correlationCoefficient = new Covariance().covariance(tableauMoy, tableauRepl);
			tableauFinal[8] = correlationCoefficient;
		}
		if (!stabilite) {
			for (GridCoordinates2D key : cellRepet.keySet()) {
				
				switch (cellRepet.get(key)) {
				case 1:
					tableauFinal[9] = tableauFinal[9] + 1;
					break;
				case 2:
					tableauFinal[10] = tableauFinal[10] + 1;
					break;
				case 3:
					tableauFinal[11] = tableauFinal[11] + 1;
					break;
				case 4:
					tableauFinal[12] = tableauFinal[12] + 1;
					break;
				case 5:
					tableauFinal[13] = tableauFinal[13] + 1;
					break;
				case 6:
					tableauFinal[14] = tableauFinal[14] + 1;
					break;
				case 7:
					tableauFinal[15] = tableauFinal[15] + 1;
					break;
				case 8:
					tableauFinal[16] = tableauFinal[16] + 1;
					break;
				case 9:
					tableauFinal[17] = tableauFinal[17] + 1;
					break;
				case 10:
					tableauFinal[18] = tableauFinal[18] + 1;
					break;
				}
			}
		}
		return tableauFinal;
	}

	/**
	 * overload of the merging method to split every cells within a given shape. Not finished yet.
	 * @param listRepliFile : ArrayList of File pointing to the raster layer to merge
	 * @param isDiscrete : if true, the layer will be splited (will change to contain the targeted shape)
	 * @return array of the statistics results (will also change
	 * @throws IOException
	 * @throws TransformException
	 */
	
	public static double[] mergeRasters(ArrayList<File> listRepliFile, boolean isDiscrete) throws IOException, TransformException {
		double[] tableauFinal = new double[18];
		DescriptiveStatistics statNb = new DescriptiveStatistics();
		Hashtable<DirectPosition2D, Integer> cellRepet = new Hashtable<DirectPosition2D, Integer>();

		ArrayList<Hashtable<DirectPosition2D, Integer>> cellbyTypo = new ArrayList<Hashtable<DirectPosition2D, Integer>>();

		for (File f : listRepliFile) {
			// setting of useless parameters
			ParameterValue<OverviewPolicy> policy = AbstractGridFormat.OVERVIEW_POLICY.createValue();
			policy.setValue(OverviewPolicy.IGNORE);
			// this will basically read 4 tiles worth of data at once from the disk...
			ParameterValue<String> gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();
			// Setting read type: use JAI ImageRead (true) or ImageReaders read methods (false)
			ParameterValue<Boolean> useJaiRead = AbstractGridFormat.USE_JAI_IMAGEREAD.createValue();
			useJaiRead.setValue(true);
			GeneralParameterValue[] params = new GeneralParameterValue[] { policy, gridsize, useJaiRead };

			GridCoverage2DReader reader = new GeoTiffReader(f);
			GridCoverage2D coverage = reader.read(params);

			double w = coverage.getEnvelope2D().width;
			double h = coverage.getEnvelope2D().height;

			double largeur = w + coverage.getEnvelope2D().x;
			double longueur = h + coverage.getEnvelope2D().y;

			double Xmin = coverage.getEnvelope2D().x + 100;
			double Ymin = coverage.getEnvelope2D().y + 100;

			int compteurNombre = 0;
			File pathmerged = new File(f + "_merge");
			GeoTiffWriter rasterMerged = new GeoTiffWriter(pathmerged);
			rasterMerged.write(coverage, params);

			for (double i = Xmin; i < largeur; i = i + 20) {
				for (double j = Ymin; j < longueur; j = j + 20) {
					DirectPosition2D coord = new DirectPosition2D((int) i, (int) j);
					float[] yo = (float[]) coverage.evaluate(coord);
					if (yo[0] > 0) {
						compteurNombre = compteurNombre + 1;
						if (cellRepet.containsKey(coord)) { // si la cellule a déja été sélectionné lors de réplications
							cellRepet.put(coord, cellRepet.get(coord) + 1);
						} else { // si la cellule est sélectionné pour la première fois
							cellRepet.put(coord, 1);
						}
					}
				}
			}
			System.out.println("il y a " + compteurNombre + " cellules dans ce scenar");
			//double[] compteurNombreDiscrete = splitCountTypo(compteurNombre);
			//statNb.addValue(compteurNombreDiscrete);

			System.out.println(cellRepet.size());
			cellbyTypo = splitMergedTypo(cellRepet, coverage);
		}

		// discretisation des réplications de cellules des statistiques du merge des rasters
		for (Hashtable<DirectPosition2D, Integer> discretedMergedRepli : cellbyTypo) {

			DescriptiveStatistics statRepli = new DescriptiveStatistics();

			// statistiques du nombre de cellules - reste a discretiser

			tableauFinal[0] = statNb.getMean();
			tableauFinal[1] = statNb.getStandardDeviation();

			double[] tableauRepl = new double[cellRepet.size()];

			// calcul de la moyenne des evaluations

			int i = 0;
			for (int repli : discretedMergedRepli.values()) {
				tableauRepl[i] = repli;
				i = i + 1;
				statRepli.addValue(repli);
			}
			tableauFinal[2] = statRepli.getPercentile(10);
			tableauFinal[3] = statRepli.getPercentile(25);
			tableauFinal[4] = statRepli.getPercentile(50);
			tableauFinal[5] = statRepli.getPercentile(75);
			tableauFinal[6] = statRepli.getPercentile(90);

			for (DirectPosition2D key : cellRepet.keySet()) {
				switch (cellRepet.get(key)) {
				case 1:
					tableauFinal[7] = tableauFinal[7] + 1;
					break;
				case 2:
					tableauFinal[8] = tableauFinal[8] + 1;
					break;
				case 3:
					tableauFinal[9] = tableauFinal[9] + 1;
					break;
				case 4:
					tableauFinal[10] = tableauFinal[10] + 1;
					break;
				case 5:
					tableauFinal[11] = tableauFinal[11] + 1;
					break;
				case 6:
					tableauFinal[12] = tableauFinal[12] + 1;
					break;
				case 7:
					tableauFinal[13] = tableauFinal[13] + 1;
					break;
				case 8:
					tableauFinal[14] = tableauFinal[14] + 1;
					break;
				case 9:
					tableauFinal[15] = tableauFinal[15] + 1;
					break;
				case 10:
					tableauFinal[16] = tableauFinal[16] + 1;
					break;
				}
			}

		}
		return tableauFinal;
	}

	/*	private static ArrayList<> splitCountTypo(Hashtable<DirectPosition2D, Integer> tablIn, GridCoverage2D projectedCoord) throws IOException {
	
			Hashtable<DirectPosition2D, Integer> cellRepetPeriCentre = new Hashtable<DirectPosition2D, Integer>();
			Hashtable<DirectPosition2D, Integer> cellRepetBanlieue = new Hashtable<DirectPosition2D, Integer>();
			Hashtable<DirectPosition2D, Integer> cellRepetPeriUrbain = new Hashtable<DirectPosition2D, Integer>();
			Hashtable<DirectPosition2D, Integer> cellRepetRural = new Hashtable<DirectPosition2D, Integer>();
			ArrayList<Hashtable<DirectPosition2D, Integer>> listRepli = new ArrayList<Hashtable<DirectPosition2D, Integer>>();
	
			ShapefileDataStore typo = new ShapefileDataStore(discreteFile.toURL());
	
			ContentFeatureCollection features = typo.getFeatureSource().getFeatures();
			// trouver un moyen de ne convertir à la même projection
	
			for (DirectPosition2D coord : tablIn.keySet()) {
	
				/// coord.setLocation(x, y);
				for (Object eachFeature : features.toArray()) {
					SimpleFeature feature = (SimpleFeature) eachFeature;
	
					if (feature.getBounds().contains(coord.getX(), coord.getY())) {
						String typopo = feature.getAttribute("typo").toString();
						switch (typopo) {
						case "rural":
							cellRepetRural.put(coord, tablIn.get(coord));
							break;
						case "peri-urbain":
							cellRepetPeriUrbain.put(coord, tablIn.get(coord));
							break;
	
						case "peri-centre":
							cellRepetPeriCentre.put(coord, tablIn.get(coord));
							break;
	
						case "banlieue":
							cellRepetBanlieue.put(coord, tablIn.get(coord));
							break;
						}
					}
				}
			}
			listRepli.add(cellRepetPeriCentre);
			listRepli.add(cellRepetBanlieue);
			listRepli.add(cellRepetPeriUrbain);
			listRepli.add(cellRepetRural);
	
			return listRepli;
	
		}
		*/
	private static ArrayList<Hashtable<DirectPosition2D, Integer>> splitMergedTypo(Hashtable<DirectPosition2D, Integer> tablIn, GridCoverage2D projectedCoord) throws IOException {
		//classe en construction
		Hashtable<DirectPosition2D, Integer> cellRepetPeriCentre = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetBanlieue = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetPeriUrbain = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetRural = new Hashtable<DirectPosition2D, Integer>();
		ArrayList<Hashtable<DirectPosition2D, Integer>> listRepli = new ArrayList<Hashtable<DirectPosition2D, Integer>>();

		ShapefileDataStore typo = new ShapefileDataStore(discreteFile.toURL());

		ContentFeatureCollection features = typo.getFeatureSource().getFeatures();
		// trouver un moyen de ne convertir à la même projection

		for (DirectPosition2D coord : tablIn.keySet()) {

			/// coord.setLocation(x, y);
			for (Object eachFeature : features.toArray()) {
				SimpleFeature feature = (SimpleFeature) eachFeature;

				if (feature.getBounds().contains(coord.getX(), coord.getY())) {
					String typopo = feature.getAttribute("typo").toString();
					switch (typopo) {
					case "rural":
						cellRepetRural.put(coord, tablIn.get(coord));
						break;
					case "peri-urbain":
						cellRepetPeriUrbain.put(coord, tablIn.get(coord));
						break;

					case "peri-centre":
						cellRepetPeriCentre.put(coord, tablIn.get(coord));
						break;

					case "banlieue":
						cellRepetBanlieue.put(coord, tablIn.get(coord));
						break;
					}
				}
			}
		}
		listRepli.add(cellRepetPeriCentre);
		listRepli.add(cellRepetBanlieue);
		listRepli.add(cellRepetPeriUrbain);
		listRepli.add(cellRepetRural);

		return listRepli;

	}

	private static void generateCsvFile(Hashtable<String, double[]> tablIn, File file, String name) throws IOException {
		File fileName = new File(file + "/" + name + ".csv");
		FileWriter writer = new FileWriter(fileName);
		for (String nomScenar : tablIn.keySet()) { // nulpointerexception pour compareAHP
			writer.append(nomScenar + ",");
			for (double val : tablIn.get(nomScenar)) {
				writer.append(val + ",");
			}
			writer.append("\n");
		}
		writer.close();

	}

	public static void main(String[] args) throws IOException, TransformException {
		/*
		rootFile = new File("/media/mcolomb/Data_2/resultTest/result_stabilite/test_de_stabilite/stable/");
		Hashtable<String, double[]> lol = replicationStab("20",true);
		File statyFile= new File(rootFile+"yo");
		statyFile.mkdir();
		generateCsvFile(lol,statyFile,"discretize");
		System.out.println(lol);
		
		//test de réplications discrètisé
		rootFile = new File(rootFile + "/G1");
		Hashtable<String, double[]> lol = replication("20",true);
		File statyFile= new File(rootFile+"yo");
		statyFile.mkdir();
		generateCsvFile(lol,statyFile,"discretize");
		System.out.println(lol);
		*/
		//test systématique de toutes les grilles
		for (int grid = 0; grid <= 5; grid++) {
			rootFile = new File(rootFile + "/G" + grid);
			File fileResult = new File(rootFile + "/resultStats");
			fileResult.mkdir();
			for (int gr = 0; gr < 3; gr++) {
				String echelle = "20";
				switch (gr) {
				case 1:
					echelle = "60";
					break;
				case 2:
					echelle = "180";
					break;
				}
				Hashtable<String, double[]> repli = replication(echelle, false);
				String name = new String("repli-G" + grid + "-echelle_" + echelle);
				generateCsvFile(repli, fileResult, name);
/*
				Hashtable<String, double[]> ahp = compareAHP(echelle);
				name = new String("ahp-G" + gr + "-echelle_" + echelle);
				generateCsvFile(ahp, fileResult, name);
*/
			}
			rootFile = new File(rootFile.getParent());
		}

		// rootFile = new File(rootFile + "/G1");

		/*
		 * generation des statistiques lors de grande réplications 
		 * Hashtable<String, double[]> repli = replicationStab("20", false); String name = new String("repli-echelle_20_yo"); File fileResult = new File(rootFile + "/resultStats/"); generateCsvFile(repli, fileResult, name);
		 */

		/*Hashtable<GridCoordinates2D, Integer> lol = new Hashtable<GridCoordinates2D, Integer>();
		GridCoordinates2D coord = new GridCoordinates2D(39, 215);
		GridCoordinates2D coord2 = new GridCoordinates2D(40, 972);
		lol.put(coord, 3);
		lol.put(coord2, 4);
		splitTypo(lol);
		*/
	}
}
