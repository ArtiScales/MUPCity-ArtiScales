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
	 * @param with:
	 *            string contained in the wanted file
	 * @param echelle:
	 *            scale of the file
	 * @param in:
	 *            array of file to search in (can be null)
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
	 * methode sélectionnant les noms des couches a comparer grace aux methodes mergeRaster
	 * 
	 * @author Maxime Colomb
	 * @param isDiscrete
	 *            boolean si la décomposition est discrétisé selon un shapefile
	 * @param echelle
	 *            l'échelle de décomposition étudié
	 * @return
	 * @return un Hashtable<String, double[]> avec : String le nom du scénario étudié - double[] les statistiques renvoyés par la méthode mergeRaster employé
	 * @throws IOException
	 * @throws TransformException
	 */
	public static void replication(String echelle, boolean isDiscrete) throws IOException, TransformException {
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
					String aggreg = "Moy";

					String eachTest = new String(N + "--" + strict + "--" + ahp + "_" + aggreg);
					ArrayList<File> listRepliFile = selectWith(eachTest, echelle, null);
					System.out.println("pour le scenario " + eachTest);
					double[] TableauRepliScenar;
					if (isDiscrete) { // different function if we need to discretize the area within different urban types

						mergeRasters(listRepliFile, eachTest, false);

					} else {
						TableauRepliScenar = mergeRasters(listRepliFile, eachTest);
					}

					for (int i = 0; i <= 7; i++) {
					}
				}
			}
		}
	}

	/**
	 * anaysis of a lot of tests
	 * 
	 * @param echelle:
	 *            scale of the file
	 * @param isDiscrete
	 *            if the process has to discretise the output cells within a shape file
	 * @return a collection of a scenario name with his analysis array (described in the margeRaster method)
	 * @throws IOException
	 * @throws TransformException
	 */
	public static Hashtable<String, Double> replicationStab(String echelle, boolean isDiscrete) throws IOException, TransformException {
		ArrayList<File> listRepliFiles = selectWith("replication_numero", echelle, null);
		ArrayList<File> listRepliFile = new ArrayList<File>();
		for (int i = 0; i < 600; i++) {

			listRepliFile.add(listRepliFiles.get(i));
		}
		DescriptiveStatistics statNb = new DescriptiveStatistics();
		double[] histo = new double[listRepliFile.size()];
		Hashtable<GridCoordinates2D, Integer> cellRepet = new Hashtable<GridCoordinates2D, Integer>();

		Hashtable<String, Double> tableauFinal = new Hashtable<String, Double>();

		int iter = 0;
		for (File f : listRepliFile) {
			// setting of useless parameters
			ParameterValue<OverviewPolicy> policy = AbstractGridFormat.OVERVIEW_POLICY.createValue();
			policy.setValue(OverviewPolicy.IGNORE);
			// this will basically read 4 tiles worth of data at once from the disk...
			ParameterValue<String> gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();
			// Setting read type: use JAI ImageRead (true) or ImageReaders read methods (false)
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
						} else {// si la cellule est sélectionné pour la première fois
							cellRepet.put(coord, 1);
						}
					}
				}
			}
			System.out.println("il y a " + compteurNombre + " cellules dans ce scenar de réplication " + iter + " et " + cellRepet.size() + " tout scénarios confondus");
			statNb.addValue(compteurNombre);
			histo[iter] = (double) cellRepet.size();
			iter = iter + 1;
		}

		Hashtable<String, double[]> enForme = new Hashtable<String, double[]>();
		enForme.put("histo", histo);
		File statyFile = new File(rootFile + "/stats");

		generateCsvFile(enForme, statyFile, "selec_cellules_totales", true);
		// statistiques du nombre de cellules

		tableauFinal.put("moyenne_cellules_replication", statNb.getMean());
		tableauFinal.put("ecart_type_cellules_replication", statNb.getStandardDeviation());
		tableauFinal.put("coeff de variation du nombre de cellules", tableauFinal.get("ecart_type_cellules_replication") / tableauFinal.get("moyenne_cellules_replication"));

		for (GridCoordinates2D key : cellRepet.keySet()) {
			if (tableauFinal.containsKey(cellRepet.get(key).toString())) {
				tableauFinal.put(cellRepet.get(key).toString(), (double) tableauFinal.get(cellRepet.get(key).toString()) + 1);
			} else {
				tableauFinal.put(cellRepet.get(key).toString(), (double) 1);
			}

		}

		return tableauFinal;
	}

	/**
	 * Compares replication by the AHP matrix choice
	 * 
	 * @param echelle:
	 *            scale of the file
	 * @return a collection of a scenario name with his analysis array (described in the margeRaster method)
	 * @throws IOException,
	 *             TransformException
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
					double[] tableauAhp = mergeRasters(oneSeedAhp, TestNSt);
					String nameScenar = new String(TestNSt + "--" + aggreg);
					TableauRepliTotal.put(nameScenar, tableauAhp);
				}
			}
		}
		return TableauRepliTotal;
	}

	/**
	 * Merge the given Array of Files. Return an array of statistic values. Will also return a merged tif (in construction)
	 * 
	 * @param listRepliFile
	 *            : ArrayList of File pointing to the raster layer to merge
	 * @return array of statistics results
	 * @throws IOException
	 * @throws TransformException
	 */
	public static double[] mergeRasters(ArrayList<File> listRepliFile, String nameScenar) throws IOException, TransformException {
		File statFile = new File(rootFile + "/stats");
		statFile.mkdir();
		File fileName = new File(statFile + "/" + "result.csv");
		FileWriter writer = new FileWriter(fileName, true);
		DescriptiveStatistics statNb = new DescriptiveStatistics();
		Hashtable<GridCoordinates2D, Integer> cellRepet = new Hashtable<GridCoordinates2D, Integer>();
		Hashtable<GridCoordinates2D, ArrayList<Float>> cellEval = new Hashtable<GridCoordinates2D, ArrayList<Float>>();
		double[] tableauFinal = new double[19];
		if (stabilite) {
			tableauFinal = new double[9];
		}

			//creation of the merged raster, not working for now (I need to dig more into geotool classes)
		
		String nameMerged = new String(listRepliFile.get(1).toString()); 
		File pathmerged = new File(rootFile + "/"+ nameMerged+ "_merge");
		GeoTiffWriter rasterMerged = new GeoTiffWriter(pathmerged);
		rasterMerged.dispose();
		
		
		
		
		int youhou = 0;
		for (File f : listRepliFile) {
			// setting of useless parameters
			ParameterValue<OverviewPolicy> policy = AbstractGridFormat.OVERVIEW_POLICY.createValue();
			policy.setValue(OverviewPolicy.IGNORE);
			ParameterValue<String> gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();
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
			youhou = youhou + 1;

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
			System.out.println("il y a " + compteurNombre + " cellules dans ce scenar de réplication " + youhou);
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

		if (tableauMoy.length > 1) { // si il n'y a pas de cellules, la covariance fait planter
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
		//écriture dans un .csv
		writer.append(nameScenar + ",");
		for (double db : tableauFinal) {
			writer.append(db + ",");
		}
		writer.append("\n");
		writer.close();
		return tableauFinal;
	}

	/**
	 * overload of the merging method to split every cells within a given shape. Not finished yet.
	 * 
	 * @param listRepliFile
	 *            : ArrayList of File pointing to the raster layer to merge
	 * @param isDiscrete
	 *            : if true, the layer will be splited (will change to contain the targeted shape)
	 * @return array of the statistics results (will also change
	 * @throws IOException
	 * @throws TransformException
	 */

	public static double[] mergeRasters(ArrayList<File> listRepliFile, String nameScenar, boolean isDiscrete) throws IOException, TransformException {
		File statFile = new File(rootFile + "/stats");
		statFile.mkdir();
		File fileName = new File(statFile + "/result.csv");
		FileWriter writer = new FileWriter(fileName, true);
		Hashtable<DirectPosition2D, Integer> cellRepet = new Hashtable<DirectPosition2D, Integer>();
		ArrayList<Hashtable<DirectPosition2D, Integer>> listCellByTypo = new ArrayList<Hashtable<DirectPosition2D, Integer>>();
		double[] tableauFinal = new double[40];

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

			double Xmin = coverage.getEnvelope2D().x + 1;
			double Ymin = coverage.getEnvelope2D().y + 1;

			double largeur = coverage.getEnvelope2D().width + Xmin;
			double longueur = coverage.getEnvelope2D().height + Ymin;

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
			System.out.println("il y a " + compteurNombre + " cellules dans ce scenar et " + cellRepet.size() + " dans tous");
			//double[] compteurNombreDiscrete = splitCountTypo(compteurNombre);
			//statNb.addValue(compteurNombreDiscrete);

		}

		// discretisation des réplications de cellules des statistiques du merge des rasters
		int i = 0;
		listCellByTypo = splitMergedTypo(cellRepet);
		for (Hashtable<DirectPosition2D, Integer> discretedMergedRepli : listCellByTypo) {
			for (DirectPosition2D key : discretedMergedRepli.keySet()) {
				switch (cellRepet.get(key)) {
				case 1:
					tableauFinal[0 + i * 10] = tableauFinal[0 + i * 10] + 1;
					break;
				case 2:
					tableauFinal[1 + i * 10] = tableauFinal[1 + i * 10] + 1;
					break;
				case 3:
					tableauFinal[2 + i * 10] = tableauFinal[2 + i * 10] + 1;
					break;
				case 4:
					tableauFinal[3 + i * 10] = tableauFinal[3 + i * 10] + 1;
					break;
				case 5:
					tableauFinal[4 + i * 10] = tableauFinal[4 + i * 10] + 1;
					break;
				case 6:
					tableauFinal[5 + i * 10] = tableauFinal[5 + i * 10] + 1;
					break;
				case 7:
					tableauFinal[6 + i * 10] = tableauFinal[6 + i * 10] + 1;
					break;
				case 8:
					tableauFinal[7 + i * 10] = tableauFinal[7 + i * 10] + 1;
					break;
				case 9:
					tableauFinal[8 + i * 10] = tableauFinal[8 + i * 10] + 1;
					break;
				case 10:
					tableauFinal[9 + i * 10] = tableauFinal[9 + i * 10] + 1;
					break;
				}

			}
			i = i + 1;
		}
		writer.append(nameScenar + ",");
		for (double db : tableauFinal) {
			System.out.println(db);
			writer.append(db + ",");
		}
		writer.append("\n");
		writer.close();
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
	private static ArrayList<Hashtable<DirectPosition2D, Integer>> splitMergedTypo(Hashtable<DirectPosition2D, Integer> tablIn) throws IOException {
		Hashtable<DirectPosition2D, Integer> cellRepetPeriCentre = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetBanlieue = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetPeriUrbain = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetRural = new Hashtable<DirectPosition2D, Integer>();
		ArrayList<Hashtable<DirectPosition2D, Integer>> listCellByTypo = new ArrayList<Hashtable<DirectPosition2D, Integer>>();

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

		listCellByTypo.add(cellRepetPeriCentre);
		listCellByTypo.add(cellRepetBanlieue);
		listCellByTypo.add(cellRepetPeriUrbain);
		listCellByTypo.add(cellRepetRural);
		return listCellByTypo;
	}

	private static void generateCsvFile(Hashtable<String, double[]> tablIn, File file, String name, boolean addAfter) throws IOException {
		File fileName = new File(file + "/" + name + ".csv");
		FileWriter writer = new FileWriter(fileName, addAfter);
		for (String nomScenar : tablIn.keySet()) { // nulpointerexception pour compareAHP
			writer.append(nomScenar + ",");
			for (double val : tablIn.get(nomScenar)) {
				writer.append(val + ",");
			}
			writer.append("\n");
		}
		writer.close();
	}

	public static void gridSensibility(String echelle, int nbTest) throws IOException, TransformException {
		ArrayList<File> listRepliFile = new ArrayList<File>();
		ArrayList<File> yo = new ArrayList<File>();
		for (int i = 0; i <= nbTest; i++) {
			File file = new File(rootFile + "/data" + i + "/results/N5--Ba--ahpS_Moy--replication_42-eval_anal-" + echelle + ".0.tif");
			file.listFiles();
			listRepliFile.add(file);

			yo = selectWith("Moy", echelle, listRepliFile);

		}
		System.out.println(yo);
		mergeRasters(yo, "gridCompare");
	}

	public static void main(String[] args) throws IOException, TransformException {

		//sensibilité de la grille
		ArrayList<String> echelles = new ArrayList<String>();
		rootFile = new File(rootFile + "/mouv_data/");
		for (Integer i = 20; i <= 14580; i = i * 3) {
			String nombre = i.toString();
			echelles.add(nombre);
		}
		for (String echelle : echelles) {

			gridSensibility(echelle, 8);
		}
		/*
		ArrayList<String> echelles = new ArrayList<String>();
		for (Integer i = 20; i <= 14580; i = i * 3) {
			String nombre = i.toString();
			echelles.add(nombre);
		}
		System.out.println(echelles);
		for (String echelle : echelles) {
			rootFile = new File("/media/mcolomb/Data_2/resultTest/result_stabilite/test_de_stabilite/strict/");
			File statyFile = new File(rootFile + "/stats");
			statyFile.mkdir();
			Hashtable<String, Double> lol = replicationStab(echelle, false);
			Hashtable<String, double[]> enforme = new Hashtable<String, double[]>();
			for (String temp : lol.keySet()) {
				double[] valTabl = new double[1];
				valTabl[0] = lol.get(temp);
				enforme.put(temp, valTabl);
			}
			generateCsvFile(enforme, statyFile, "discretize", true);
		}
		
		//test de réplications discrètisé
		/*
		rootFile = new File(rootFile + "/G1");
		replication("60", true);
		
		
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
				}
		}
		/*
				Hashtable<String, double[]> ahp = compareAHP(echelle);
				name = new String("ahp-G" + gr + "-echelle_" + echelle);
				generateCsvFile(ahp, fileResult, name);
		
			}
			rootFile = new File(rootFile.getParent());
		}
		
		// rootFile = new File(rootFile + "/G1");
		
		/*
		 * generation des statistiques lors de grande réplications 
		 * Hashtable<String, double[]> repli = replicationStab("20", false); String name = new String("repli-echelle_20_yo"); File fileResult = new File(rootFile + "/resultStats/"); generateCsvFile(repli, fileResult, name);
		 */
		/*
				Hashtable<GridCoordinates2D, Integer> lol = new Hashtable<GridCoordinates2D, Integer>();
				GridCoordinates2D coord = new GridCoordinates2D(39, 215);
				GridCoordinates2D coord2 = new GridCoordinates2D(40, 972);
				lol.put(coord, 3);
				lol.put(coord2, 4);
				splitMergedTypo(lol);
			*/
	}
}
