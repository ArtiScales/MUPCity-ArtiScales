package org.thema.mupcity.analyse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class RasterAnalyse {

	/**
	 * This class contains several methods used for the analysis of the MUP-City outputs during the sensibility and stability tests raster outputs must contains the selected to
	 * urbanize cells mixed with the evaluation layer (output of the extract-eval-anal method) The raster selected with the selectWith method are compared within the mergeRaster
	 * method There is two ways to compare rasters : if they are composed of the exact same grid, we will use the relative position of the cells within this grid. The "discrete"
	 * variable will be "false" and CreateStats method will be used to calculate statistics if the rasters to compare are different, we use the DirectPosition object to locate the
	 * cells. The "discrete" variable will be "true" and SplitMergedTypo method will be used to calculate statistics
	 * 
	 */

	public static File rootFile = new File("/media/mcolomb/Data_2/resultExplo/");
	public static File discreteFile = new File("/home/mcolomb/informatique/MUP/explo/data/admin_typo.shp");
	public static boolean discrete = false;
	public static boolean stabilite = false;
	public static boolean sensibility = false;
	public static boolean cutBorder = false;
	public static String echelle;
	public static boolean firstline = true;
	public static boolean compareAHP = false;
	public static boolean compareBaSt = false;
	public static boolean compare20_180 = false;
	public static boolean compare20_60 = false;
	public static Hashtable<DirectPosition2D, Float> SvgCellEval20;
	public static Hashtable<DirectPosition2D, Integer> SvgCellRepet20;

	/**
	 * Select files containing the defined strings at a defined scale. It could be from the defined rootfile or from a previous selection in an array of files
	 * 
	 * @author Maxime Colomb
	 * @param with:
	 *            string contained in the wanted file
	 * @param echelle:
	 *            scale of the file
	 * @param in:
	 *            array of file to search in (can be null)
	 * @return an ArrayList of File
	 * @throws Exception
	 * @throws IOException
	 */

	public RasterAnalyse(File fileRoot, String echelle) throws Exception {
		RasterAnalyse.rootFile = fileRoot;
		RasterAnalyse.echelle = echelle;
		replication();
		replicationStab();
		discrete = true;
		replication();
		replicationCompareScale();
		discrete = false;
		compareAHP();
		compareBaSt();
		gridSensibility();
		gridChange();
	}

	/**
	 * Select a list of file with the argument "with" in its name from the rootFile
	 *
	 * @param with
	 *            : String that is contained into the selection's name
	 * @param in:
	 *            optional , if the search is needed to be in a specific list
	 * @return an arrayList of files
	 * @author Maxime Colomb
	 * @throws Exception
	 * 
	 */
	public static ArrayList<File> selectWith(String with, ArrayList<File> in) throws IOException {
		ArrayList<File> listFile = new ArrayList<File>();
		if (in == null) {
			for (File fil : rootFile.listFiles()) {
				Pattern ech = Pattern.compile("eval_anal-");
				String[] list = ech.split(fil.toString());
				if (fil.toString().contains(with) && list.length > 1 && list[1].equals(echelle + ".0.tif")) {
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
	 * method which analyse the small replication of a lot of parameters described into the experimental tests about the sensibility of MUP-City All the arguments are taken in the
	 * class variable it creates a tab sheet on the /stats folder from the rootFile and merged rasters
	 * 
	 * @author Maxime Colomb
	 * @throws Exception
	 * 
	 */
	public static void replication() throws Exception {
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
					for (int rer = 0; rer <= 1; rer++) {
						String aggreg = "Moy";
						if (rer == 1) {
							aggreg = "Yag";
						}
						String eachTest = new String(N + "--" + strict + "--" + ahp + "_" + aggreg);
						ArrayList<File> listRepliFile = selectWith(eachTest, null);
						System.out.println("pour le scenario " + eachTest);
						mergeRasters(listRepliFile, eachTest);
					}
				}
			}
		}
	}

	// test de réplications discrètisé
	public static void replicationCompareScale() throws Exception {

		compare20_60 = true;
		compare20_180 = true;
		discrete = true;
		rootFile = new File(rootFile, "tests_param/results/G0");

		ArrayList<String> echelles = new ArrayList<String>();
		for (Integer i = 20; i <= 180; i = i * 3) {
			String nombre = i.toString();
			echelles.add(nombre);
		}

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
					System.out.println("pour le scenario " + eachTest);
					for (String ech : echelles) {
						echelle = ech;
						ArrayList<File> listRepliFile = selectWith(eachTest, null);
						System.out.println("pour une echelle: " + ech);
						System.out.println(listRepliFile);
						mergeRasters(listRepliFile, eachTest);
					}
					firstline = false;
				}
			}
		}
	}

	/**
	 * method which analyse the stability of a big amount of replication of simulation Directly create a statistic file
	 * 
	 * @param echelle:
	 *            scale of the file
	 * @param isDiscrete
	 *            if the process has to discretise the output cells within a shape file
	 * @return a collection of a scenario name with his analysis array (described in the margeRaster method)
	 * @throws Exception
	 */
	public static void replicationStab() throws Exception {

		ArrayList<File> listRepliFiles = selectWith("replication", null);
		stabilite = true;
		mergeRasters(listRepliFiles, "stability");
	}

	/**
	 * Compares replication by the AHP matrix choice
	 * 
	 * @param echelle:
	 *            scale of the file
	 * @return a collection of a scenario name with his analysis array (described in the margeRaster method)
	 * @throws Exception
	 */

	public static void compareAHP() throws Exception {
		compareAHP = true;
		ArrayList<File> oneSeed = selectWith("replication_7", null);
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
					ArrayList<File> tempList = selectWith(TestNSt, oneSeed);
					ArrayList<File> oneSeedAhp = selectWith(aggreg, tempList);
					System.out.println("one seed ahp : " + oneSeedAhp);
					String nameScenar = new String(TestNSt + "--" + aggreg);
					mergeRasters(oneSeedAhp, nameScenar);

				}
			}
		}
	}

	public static void compareBaSt() throws Exception {

		ArrayList<String> echelles = new ArrayList<String>();

		for (Integer i = 20; i <= 180; i = i * 3) {
			String nombre = i.toString();
			echelles.add(nombre);
		}
		for (String scale : echelles) {
			echelle = scale;
			compareBaSt = true;
			System.out.println("echelle " + echelle);
			ArrayList<File> oneSeed = selectWith("replication_7", null);
			System.out.println(oneSeed);
			for (int n = 3; n <= 7; n++) {
				String N = new String("N" + n);
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
						String aggregahp = new String(ahp + "_" + aggreg);
						ArrayList<File> tempList = selectWith(N, oneSeed);
						System.out.println(tempList);
						ArrayList<File> oneSeedSt = selectWith(aggregahp, tempList);
						System.out.println("one seed st : " + oneSeedSt);
						String nameScenar = new String(N + "--" + aggregahp);
						mergeRasters(oneSeedSt, nameScenar);

					}
				}
			}
		}
	}

	/**
	 * Count how many cells of 20m are included in cells of 180m
	 * 
	 * @author Maxime Colomb
	 * @param cellRepetCentroid:
	 * @param echelle:
	 *            scale of the file
	 * @param in:
	 *            array of file to search in (can be null)
	 * @return an ArrayList of File
	 * @throws Exception
	 * @throws IOException
	 */
	public static void compare180(Hashtable<DirectPosition2D, Integer> cellRepetCentroid, Hashtable<DirectPosition2D, Float> cellEvalCentroid, String namescenar)
			throws IOException {
		Hashtable<DirectPosition2D, Integer> cellRepet180 = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Float> cellEval180 = new Hashtable<DirectPosition2D, Float>();

		if (echelle.equals("20")) {
			SvgCellRepet20 = cellRepetCentroid;
			SvgCellEval20 = cellEvalCentroid;
		}

		if (echelle.equals("180")) {
			float sumCellEval = 0;

			for (DirectPosition2D coord20 : SvgCellEval20.keySet()) {
				sumCellEval = sumCellEval + SvgCellEval20.get(coord20);
			}
			float avCellEval = sumCellEval / SvgCellEval20.size();

			int cellIn = 0;
			int cellTotal = SvgCellRepet20.size();
			System.out.println("cell totales : " + cellTotal);
			ArrayList<Float> cellInEval = new ArrayList<Float>();
			cellRepet180 = cellRepetCentroid;
			for (DirectPosition2D coord180 : cellRepetCentroid.keySet()) {
				double emp180Xmin = coord180.getX() - 180 / 2;
				double emp180Xmax = coord180.getX() + 180 / 2;
				double emp180Ymin = coord180.getY() - 180 / 2;
				double emp180Ymax = coord180.getY() + 180 / 2;
				for (DirectPosition2D coord20 : SvgCellRepet20.keySet()) {
					if (coord20.getX() > emp180Xmin && coord20.getX() < emp180Xmax && coord20.getY() > emp180Ymin && coord20.getY() < emp180Ymax) {
						cellIn = cellIn + 1;
						cellInEval.add(SvgCellEval20.get(coord20));
					}
				}
			}

			float sumVal = 0;
			for (float val : cellInEval) {
				sumVal = sumVal + val;
			}
			float averageValIn = sumVal / cellInEval.size();
			int cellOut = cellTotal - cellIn;

			double[] resultStats = new double[6];
			String[] firstLine = new String[6];

			firstLine[0] = "nombre totale de cellules";
			firstLine[1] = "évaluation moyenne de toutes les cellules";
			firstLine[2] = "cellules de 20m non inclues dans les cellules de 180m";
			firstLine[3] = "évaluation moyenne des cellules de 20m non inclues dans les cellules de 180m";
			firstLine[4] = "cellules de 20m inclues dans les cellules de 180m";
			firstLine[5] = "évaluation moyenne des cellules de 20m incluses dans les cellules de 180m";

			resultStats[0] = cellTotal;
			resultStats[1] = averageValIn;
			resultStats[2] = cellOut;
			resultStats[4] = cellIn;
			resultStats[5] = avCellEval;
			resultStats[3] = (resultStats[0] * resultStats[1] - resultStats[4] * resultStats[5]) / resultStats[2];

			StatTab result = new StatTab("compare_20to180", (namescenar + "compare-180-20"), resultStats, firstLine);
			File statFile = new File(rootFile, "/stats");
			result.toCsv(statFile, true);
		}
	}

	public static void compare60(Hashtable<DirectPosition2D, Integer> cellRepetCentroid, Hashtable<DirectPosition2D, Float> cellEvalCentroid, String namescenar)
			throws IOException {
		Hashtable<DirectPosition2D, Integer> cellRepet60 = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Float> cellEval60 = new Hashtable<DirectPosition2D, Float>();

		if (echelle.equals("20")) {
			SvgCellRepet20 = cellRepetCentroid;
			SvgCellEval20 = cellEvalCentroid;
		}

		if (echelle.equals("60")) {
			float sumCellEval = 0;

			for (DirectPosition2D coord20 : SvgCellEval20.keySet()) {
				sumCellEval = sumCellEval + SvgCellEval20.get(coord20);
			}
			float avCellEval = sumCellEval / SvgCellEval20.size();

			int cellIn = 0;
			int cellTotal = SvgCellRepet20.size();
			System.out.println("cell totales : " + cellTotal);
			ArrayList<Float> cellInEval = new ArrayList<Float>();
			cellRepet60 = cellRepetCentroid;
			for (DirectPosition2D coord60 : cellRepetCentroid.keySet()) {
				double emp60Xmin = coord60.getX() - 60 / 2;
				double emp60Xmax = coord60.getX() + 60 / 2;
				double emp60Ymin = coord60.getY() - 60 / 2;
				double emp60Ymax = coord60.getY() + 60 / 2;
				for (DirectPosition2D coord20 : SvgCellRepet20.keySet()) {
					if (coord20.getX() > emp60Xmin && coord20.getX() < emp60Xmax && coord20.getY() > emp60Ymin && coord20.getY() < emp60Ymax) {
						cellIn = cellIn + 1;
						cellInEval.add(SvgCellEval20.get(coord20));
					}
				}
			}

			float sumVal = 0;
			for (float val : cellInEval) {
				sumVal = sumVal + val;
			}
			float averageValIn = sumVal / cellInEval.size();

			int cellOut = cellTotal - cellIn;

			double[] resultStats = new double[6];
			String[] firstLine = new String[6];

			firstLine[0] = "nombre totale de cellules";
			firstLine[1] = "évaluation moyenne de toutes les cellules";
			firstLine[2] = "cellules de 20m non inclues dans les cellules de 60m";
			firstLine[3] = "évaluation moyenne des cellules de 20m non inclues dans les cellules de 60m";
			firstLine[4] = "cellules de 20m inclues dans les cellules de 60m";
			firstLine[5] = "évaluation moyenne des cellules de 20m incluses dans les cellules de 60m";

			resultStats[0] = cellTotal;
			resultStats[1] = averageValIn;
			resultStats[2] = cellOut;
			resultStats[3] = (cellTotal * averageValIn - cellIn * avCellEval) / cellOut;
			resultStats[4] = cellIn;
			resultStats[5] = avCellEval;

			StatTab result = new StatTab("compare_20to60", (namescenar + "compare-60-20"), resultStats, firstLine);
			File statFile = new File(rootFile, "/stats");
			result.toCsv(statFile, true);
		}
	}

	/**
	 * explo method to analyse an output when the data have been moved a little in order to impact the sensibility of the simulation to the grid.
	 * 
	 * @param echelle
	 *            : scale in which the analyse should take place
	 * @param nbTest
	 *            : Number of different simulation runned for the sensibility test
	 * @return void, but creates a statistic file
	 * @throws Exception
	 */
	public static void gridSensibility() throws Exception {
		ArrayList<File> listRepliFile = new ArrayList<File>();

		for (int i = 0; i <= 8; i++) {
			File file = new File(rootFile + "/data" + i + "/replication_numero-42-eval_anal-" + echelle + ".0.tif");
			listRepliFile.add(file);
		}
		mergeRasters(listRepliFile, "gridSensibility");
		discrete = true;
		for (int i = 0; i <= 8; i++) {
			ArrayList<File> singleCity = new ArrayList<File>();
			File file = new File(rootFile + "/data" + i + "/replication_numero-42-eval_anal-" + echelle + ".0.tif");
			singleCity.add(file);
			mergeRasters(singleCity, "cityGen" + i);
			listRepliFile = new ArrayList<File>();
		}
	}

	/**
	 * method to analyse an output when the grid have had a move
	 * 
	 * @param echelle
	 *            : scale in which the analyse should take place
	 * @param nbTest
	 *            : Number of different simulation runned for the sensibility test
	 * @return void, but creates a statistic file
	 * @throws Exception
	 */
	public static void gridChange() throws Exception {

		ArrayList<File> listRepliGen = new ArrayList<File>();
		for (int i = 0; i <= 8; i++) {
			ArrayList<File> listEachCity = new ArrayList<File>();
			File file = new File(rootFile + "/G" + i + "/replication_numero-42-eval_anal-" + echelle + ".0.tif");
			listEachCity.add(file);
			mergeRasters(listEachCity, "cityGen" + i);
		}
		// mergeRasters(listRepliGen, "gridCompare");

	}

	/**
	 * mergeRaster Merge the given Array of Files. Return an array of statistic values. Will also return a merged tif (in construction) ======= Merge the given Array of Files.
	 * Return an array of statistic values. Will also return a merged tif (in construction) >>>>>>> f7654ed maj workflow et comm rasteranalyse
	 * 
	 * @param listRepliFile
	 *            : ArrayList of File pointing to the raster layer to merge
	 * @return array of statistics results
	 * @throws Exception
	 */
	public static File mergeRasters(List<File> listRepliFile, String nameScenar) throws Exception {


		// variables to create statistics

		DescriptiveStatistics statNb = new DescriptiveStatistics();
		Hashtable<GridCoordinates2D, Integer> cellRepet = new Hashtable<GridCoordinates2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetCentroid = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Float> cellEvalCentroid = new Hashtable<DirectPosition2D, Float>();
		Hashtable<GridCoordinates2D, ArrayList<Float>> cellEval = new Hashtable<GridCoordinates2D, ArrayList<Float>>();
		Hashtable<GridCoordinates2D, ArrayList<Float>> cellEvalSt = new Hashtable<GridCoordinates2D, ArrayList<Float>>();

		double nbScSt = 0;// nb de cellules dans une simulation stricte lors de la comparaison

		int nbDeScenar = 0; // le nombre total de scénarios analysés dans la fonction

		double[] histo = new double[listRepliFile.size()];
		int iter = 0;

		// variables for merged raster
		// not cool coz i cannot know the number of column and lines of the enveloppe yet and the type need it
		// change the type to a collection or an arraylist?

		Envelope2D env = null;

		// loop on the different cells
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
			if (env == null) {
				env = coverage.getEnvelope2D();
			}
			int compteurNombre = 0;
			nbDeScenar = nbDeScenar + 1;

			// beginning of the all cells loop
			int debI = 0;
			int debJ = 0;

			// in case of a move of the grid, we have to delete the border cells because they will be moved

			// double Xmin = 914760;
			// double Xmax = 943200;
			// double Ymin = 6680157;
			// double Ymax = 6701217;
			double Xmin = env.getMinX();
			double Xmax = env.getMaxX();
			double Ymin = env.getMinY();
			double Ymax = env.getMaxY();
			if (cutBorder == true) {
				int ecart = Integer.parseInt(echelle);
				Xmin = Xmin + ecart;
				Xmax = Xmax - ecart;
				Ymin = Ymin + ecart;
				Ymax = Ymax - ecart;
			}

			// developpement pour les cas ou l'on veut une analyse discrétisée ou si les bordures doivent être coupées
			if (((discrete == true && Double.parseDouble(echelle) <= 180)) || cutBorder == true) {
				for (double r = Xmin + Double.parseDouble(echelle)/2; r <= Xmax; r = r + Double.parseDouble(echelle)) {
					// those values are the bounds from project (and upped to correspond to a multiple of 180 to	analyse all the cells in the project)
					for (double t = Ymin+ Double.parseDouble(echelle)/2; t <= Ymax; t = t + Double.parseDouble(echelle)) {
						DirectPosition2D coordCentre = new DirectPosition2D(r, t);
						float[] yo = (float[]) coverage.evaluate(coordCentre);
						if (yo[0] > 0) {
							compteurNombre = compteurNombre + 1;
							if (cellRepetCentroid.containsKey(coordCentre)) { // si la cellule a déja été sélectionné lors de réplications
								cellRepetCentroid.put(coordCentre, cellRepetCentroid.get(coordCentre) + 1);
								// on mets les valeurs d'évaluation dans un tableau

							} else { // si la cellule est sélectionné pour la première fois
								cellRepetCentroid.put(coordCentre, 1);
							}
							cellEvalCentroid.put(coordCentre, yo[0]);
						}
					}
				}
			}
			// analyse normale de la réplication des cellules
			else {
				for (int i = debI; i < w; i++) {
					for (int j = debJ; j < h; j++) {
						GridCoordinates2D coord = new GridCoordinates2D(i, j);
						if (coverage.evaluate(coord, vals)[0] > 0) {
							compteurNombre = compteurNombre + 1;
							if (cellRepet.containsKey(coord)) { // si la cellule a déja été sélectionné lors de réplications
								cellRepet.put(coord, cellRepet.get(coord) + 1);
								ArrayList<Float> temp = cellEval.get(coord); // on mets les valeurs d'évaluation dans un tableau
								temp.add((float) coverage.evaluate(coord, vals)[0]);
								cellEval.put(coord, temp);
							} else {// si la cellule est sélectionné pour la première fois
								cellRepet.put(coord, 1);
								ArrayList<Float> firstList = new ArrayList<Float>();
								firstList.add((float) coverage.evaluate(coord, vals)[0]);
								cellEval.put(coord, firstList);
								// bricolage pour avoir l'eval des cellules qui sont présente dans les simulations St et non dans Ba
								if (compareBaSt == true && f.toString().contains("--St--")) {
									cellEvalSt.put(coord, firstList);
								}
							}
						}
					}
				}
			}

			System.out.println("il y a " + compteurNombre + " cellules dans " + nameScenar + " de réplication " + nbDeScenar);

			// Historique de l'évolution du nombre de cellules sélectionnées dans toutes les simulations
			statNb.addValue(compteurNombre);
			histo[iter] = (double) cellRepet.size();
			iter = iter + 1;
			System.out.println(cellRepet.size());

			// lors de la comparaison des scénarios strictes et basiques, inscrit le nombre de cellule dans le scénario stricte
			if (compareBaSt == true && f.toString().contains("--St--")) {
				nbScSt = (double) compteurNombre;
			}
		}

		// compare different scales of cells
		if (compare20_180 == true) {
			compare180(cellRepetCentroid, cellEvalCentroid, nameScenar);

		}
		if (compare20_60 == true) {
			compare60(cellRepetCentroid, cellEvalCentroid, nameScenar);

		}

		// truandage pour faire passer dans la methode createStat le nombre de cellule dans une simulation stricte et leurs évaluations
		if (compareBaSt == true) {
			histo = new double[1];
			histo[0] = nbScSt;
			cellEval = new Hashtable<GridCoordinates2D, ArrayList<Float>>();
			for (GridCoordinates2D coord : cellEvalSt.keySet()) {
				if (cellRepet.get(coord) == 1) {
					cellEval.put(coord, cellEvalSt.get(coord));
				}
			}
		}
		File statFile = new File("");
		// création de statistiques pour une analyse discrétisé
		if ((discrete == true || cutBorder == true) && (compare20_180 == false || compare20_60 == false)) {
			statFile = splitMergedTypo(nameScenar, cellRepetCentroid, cellEvalCentroid);
		}
		// création de statistiques pour une analyse normale
		else if (discrete == false && (compare20_180 == false || compare20_60 == false)) {
			statFile = createStats(nameScenar, histo, statNb, cellRepet, cellEval);
		}
		return statFile;
	}

	/**
	 * create the statistics for a discretized study
	 * 
	 * @param nameScenar
	 *            : name given to the study
	 * @param cellRepet
	 *            : Collection of the cell's replication
	 * @param cellEval
	 *            : Collection of the cell's evaluation
	 * @throws IOException
	 */
	private static File splitMergedTypo(String nameScenar, Hashtable<DirectPosition2D, Integer> cellRepet, Hashtable<DirectPosition2D, Float> cellEval) throws IOException {
		Hashtable<DirectPosition2D, Integer> cellRepetPeriCentre = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetBanlieue = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetPeriUrbain = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetRural = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetHyperCentre = new Hashtable<DirectPosition2D, Integer>();

		int repl = 10;
		if (sensibility = true) {
			repl = 9;
		} else if (stabilite == true) {
			repl = 1000;
		}

		ArrayList<Hashtable<DirectPosition2D, Integer>> listCellByTypo = new ArrayList<Hashtable<DirectPosition2D, Integer>>();

		Hashtable<String, double[]> cellByCity = new Hashtable<String, double[]>();
		Hashtable<String, ArrayList<Float>> evalByCity = new Hashtable<String, ArrayList<Float>>();

		File statFile = new File(rootFile + "/stats-discrete");
		statFile.mkdirs();

		String[] premiereCol = new String[12];

		ShapefileDataStore typo = new ShapefileDataStore(discreteFile.toURI().toURL());

		ContentFeatureCollection features = typo.getFeatureSource().getFeatures();

		for (DirectPosition2D coord : cellRepet.keySet()) {
			for (Object eachFeature : features.toArray()) {
				// geotool way of create feature
				SimpleFeatureImpl feature = (SimpleFeatureImpl) eachFeature;
				Geometry geom = (Geometry) feature.getDefaultGeometry();
				String city = feature.getAttribute("NOM_COM").toString();
				String typopo = feature.getAttribute("typo").toString();
				Coordinate coordo = new Coordinate(coord.getX(), coord.getY());
				// point representing the tested cell
				GeometryFactory geometryFactory = new GeometryFactory();
				Geometry pt = geometryFactory.createPoint(coordo);

				// creating the city statistics and typology
				if (geom.contains(pt)) {
					double[] nbByCity = new double[5];
					if (cellByCity.containsKey(city)) {
						nbByCity[0] = cellByCity.get(city)[0] + 1;
						// class if cells are varaible or not
						if (cellRepet.get(coord) == repl) {
							nbByCity[1] = cellByCity.get(city)[1] + 1;
							nbByCity[2] = cellByCity.get(city)[2];
						} else if (1 <= cellRepet.get(coord) && cellRepet.get(coord) < repl) {
							nbByCity[2] = cellByCity.get(city)[2] + 1;
							nbByCity[1] = cellByCity.get(city)[1];
						}
						// aggregation of the different evaluations
						ArrayList<Float> temp = evalByCity.get(city);
						temp.add(cellEval.get(coord));
						evalByCity.put(city, temp);
						float valeval = 0;
						for (float tymp : evalByCity.get(city)) {
							valeval = valeval + tymp;
						}
						nbByCity[3] = (double) valeval / evalByCity.get(city).size();
						cellByCity.put(city, nbByCity);
					} else {
						// new cell in the city game
						nbByCity[0] = (double) 1;
						ArrayList<Float> temp = new ArrayList<Float>();
						temp.add(cellEval.get(coord));
						// evaluation and average of the evaluation by city
						nbByCity[3] = cellEval.get(coord);
						evalByCity.put(city, temp);
						if (cellRepet.get(coord) == repl) {
							nbByCity[1] = (double) 1;
						} else if (1 <= cellRepet.get(coord) && cellRepet.get(coord) < 10) {
							nbByCity[2] = (double) 1;
						}
					}

					switch (typopo) {
					case "rural":
						cellRepetRural.put(coord, cellRepet.get(coord));
						nbByCity[4] = 4;
						break;
					case "peri-urbain":
						cellRepetPeriUrbain.put(coord, cellRepet.get(coord));
						nbByCity[4] = 3;
						break;
					case "peri-centre":
						cellRepetPeriCentre.put(coord, cellRepet.get(coord));
						nbByCity[4] = 1;
						break;
					case "banlieue":
						cellRepetBanlieue.put(coord, cellRepet.get(coord));
						nbByCity[4] = 2;
						break;
					case "hypercentre":
						cellRepetHyperCentre.put(coord, cellRepet.get(coord));
						nbByCity[4] = 1;
						break;
					}
					cellByCity.put(city, nbByCity);

				}
			}
		}

		String[] nameLine = new String[6];

		nameLine[0] = (nameScenar + " -- City");
		nameLine[1] = "Total Cells";
		nameLine[2] = "Stable cells";
		nameLine[3] = "Unstable cells";
		nameLine[4] = "average evaluation";
		nameLine[5] = "typology of the city";

		generateCsvFile(cellByCity, statFile, ("cellByCity" + nameScenar), nameLine);

		if (nameScenar.contains("cityGen")) {
			generateCsvFileCol(cellByCity, statFile, "cityInEachGrid");
		}

		listCellByTypo.add(cellRepetPeriCentre);
		listCellByTypo.add(cellRepetBanlieue);
		listCellByTypo.add(cellRepetPeriUrbain);
		listCellByTypo.add(cellRepetRural);
		listCellByTypo.add(cellRepetHyperCentre);

		String[] listNom = new String[5];
		listNom[0] = "peri_centre";
		listNom[1] = "banlieue";
		listNom[2] = "peri-urbain";
		listNom[3] = "rural";
		listNom[4] = "hypercentre";

		premiereCol[0] = "echelle";
		for (int y = 1; y <= 10; y++) {
			premiereCol[y] = ("repet " + y);
			if (stabilite) {
				premiereCol[11] = "toutes cellules selec";
			}
		}
		int compteur = 0;
		for (Hashtable<DirectPosition2D, Integer> tab : listCellByTypo) {
			double[] tableauFinal = new double[11];
			if (stabilite) {
				tableauFinal = new double[12];
			}
			tableauFinal[0] = Double.parseDouble(echelle);

			for (DirectPosition2D key : tab.keySet()) {
				if (stabilite == false) {
					switch (tab.get(key)) {
					case 1:
						tableauFinal[1]++;
						break;
					case 2:
						tableauFinal[2]++;
						break;
					case 3:
						tableauFinal[3]++;
						break;
					case 4:
						tableauFinal[4]++;
						break;
					case 5:
						tableauFinal[5]++;
						break;
					case 6:
						tableauFinal[6]++;
						break;
					case 7:
						tableauFinal[7]++;
						break;
					case 8:
						tableauFinal[8]++;
						break;
					case 9:
						tableauFinal[9]++;
						break;
					case 10:
						tableauFinal[10]++;
						break;
					}
				} else {
					if (0 < tab.get(key) && tab.get(key) <= 100) {
						tableauFinal[1]++;
					}
					if (100 < tab.get(key) && tab.get(key) <= 200) {
						tableauFinal[2]++;
					}
					if (200 < tab.get(key) && tab.get(key) <= 300) {
						tableauFinal[3]++;
					}
					if (300 < tab.get(key) && tab.get(key) <= 400) {
						tableauFinal[4]++;
					}
					if (400 < tab.get(key) && tab.get(key) <= 500) {
						tableauFinal[5]++;
					}
					if (500 < tab.get(key) && tab.get(key) <= 600) {
						tableauFinal[6]++;
					}
					if (600 < tab.get(key) && tab.get(key) <= 700) {
						tableauFinal[7]++;
					}
					if (700 < tab.get(key) && tab.get(key) <= 800) {
						tableauFinal[8]++;
					}
					if (800 < tab.get(key) && tab.get(key) <= 900) {
						tableauFinal[9]++;
					}
					if (900 < tab.get(key) && tab.get(key) <= 999) {
						tableauFinal[10]++;
					}
					if (tab.get(key) == 1000) {
						tableauFinal[11]++;
					}

				}
			}

			StatTab tabDiscret = new StatTab("stats-discret-type", nameScenar, tableauFinal, premiereCol);
			tabDiscret.toCsv(statFile, firstline, listNom[compteur]);
			firstline = false;
			compteur++;

		}
		return statFile;
	}

	private static File createStats(String nameScenar, double[] histo, DescriptiveStatistics statNb, Hashtable<GridCoordinates2D, Integer> cellRepet,
			Hashtable<GridCoordinates2D, ArrayList<Float>> cellEval) throws IOException {

		File statFile = new File(rootFile + "/stats");
		if (compareAHP == true) {
			statFile = new File(rootFile + "/stat_compare-AHP");
		}
		if (compareBaSt == true) {
			statFile = new File(rootFile + "/stat_compare-Ba-St");

		}
		statFile.mkdirs();

		double[] tableauFinal = new double[22];
		String[] premiereCol = new String[22];

		DescriptiveStatistics statInstable = new DescriptiveStatistics();
		DescriptiveStatistics statStable = new DescriptiveStatistics();

		// des statistiques du merge des rasters
		Hashtable<GridCoordinates2D, Float> cellEvalFinal = new Hashtable<GridCoordinates2D, Float>();

		// historique du nombre de cellules sélectionné par scénarios
		Hashtable<String, double[]> enForme = new Hashtable<String, double[]>();
		enForme.put("histo", histo);
		generateCsvFileCol(enForme, statFile, "selected_cells_all_simu");

		// statistiques du nombre de cellules par scénario
		tableauFinal[0] = Double.parseDouble(echelle);
		premiereCol[0] = "echelle";
		tableauFinal[1] = statNb.getMean();
		premiereCol[1] = "nombre moyen de cellules sélectionnées par simulations";
		tableauFinal[2] = statNb.getStandardDeviation();
		premiereCol[2] = "ecart-type du nombre des cellules sélectionnées par simulations";
		tableauFinal[3] = tableauFinal[2] / tableauFinal[1];
		premiereCol[3] = "coeff de variation du nombre de cellules sélectionnées par simulations";

		if (compareBaSt == true) {
			tableauFinal[1] = histo[0];
			premiereCol[1] = "nombre de cellules sélectionné dans la simulation stricte";
		}
		// tableaux servant à calculer les coefficients de correlations
		double[] tableauMoy = new double[cellEval.size()];
		double[] tableauRepl = new double[cellRepet.size()];

		int j = 0;

		// calcul de la moyenne des evaluations et de la corrélation avec la réqurence de réplication
		for (GridCoordinates2D temp : cellEval.keySet()) {
			float moyenne = 0;
			float somme = 0;
			ArrayList<Float> tablTemp = new ArrayList<Float>();
			tablTemp.addAll(cellEval.get(temp));
			for (float nombre : tablTemp) {
				somme = somme + nombre;
			}
			moyenne = somme / tablTemp.size();
			cellEvalFinal.put(temp, moyenne);

			tableauMoy[j] = moyenne;
			j = j + 1;
		}

		// extract the distribution of eval
		Hashtable<String, double[]> deuForme = new Hashtable<String, double[]>();
		double[] distrib = new double[cellEvalFinal.size()];
		int cpt = 0;
		DescriptiveStatistics distrEval = new DescriptiveStatistics();
		;
		for (GridCoordinates2D it : cellEvalFinal.keySet()) {
			distrib[cpt] = cellEvalFinal.get(it);
			distrEval.addValue(distrib[cpt]);
			cpt++;
		}
		double ecartTypeEval = distrEval.getStandardDeviation();
		deuForme.put("distribution des evaluation", distrib);
		System.out.println("distrib_size: " + distrib.length);
		generateCsvFileCol(deuForme, statFile, "evaluation_comportment");

		int i = 0;
		for (int repli : cellRepet.values()) {
			tableauRepl[i] = repli;
			i = i + 1;
		}
		// cet indicateur ne sert pas à grand chose
		// if (tableauMoy.length > 1 && stabilite == false && compareBaSt == false) { // si il n'y a pas de cellules, la covariance fait planter
		// double correlationCoefficient = new Covariance().covariance(tableauMoy, tableauRepl);
		// tableauFinal[14] = correlationCoefficient;
		// premiereCol[14] = ("coefficient de correlation entre le nombre de réplication et les évaluations des cellules");
		// }
		// if (tableauMoy.length > 1 && stabilite == true && compareBaSt == false) { // si il n'y a pas de cellules, la covariance fait planter
		// double covariance = new Covariance().covariance(tableauMoy, tableauRepl);
		// double correlationCoefficient = covariance /(statStable.getStandardDeviation()*ecartTypeEval);
		// tableauFinal[21] = correlationCoefficient;
		// System.out.println("correlationCoefficient iz : "+correlationCoefficient + " and covariance " + covariance + "and produit des ecart types " + ecartTypeEval +" et " +
		// tableauFinal[2] );
		// premiereCol[21] = ("coefficient de correlation entre le nombre de réplication et les évaluations des cellules");
		// }
		premiereCol[15] = ("moyenne evaluation des cellules instables");
		premiereCol[16] = ("ecart type des cellules instables");
		premiereCol[17] = ("coefficient de variation des cellules instables");
		premiereCol[18] = ("moyenne evaluation des cellules stables");
		premiereCol[19] = ("ecart type des cellules stables");
		premiereCol[20] = ("coefficient de variation des cellules stables");

		// distribution
		if (stabilite == false) {
			premiereCol[4] = ("repet 1");
			premiereCol[5] = ("repet 2");
			premiereCol[6] = ("repet 3");
			premiereCol[7] = ("repet 4");
			premiereCol[8] = ("repet 5");
			premiereCol[9] = ("repet 6");
			premiereCol[10] = ("repet 7");
			premiereCol[11] = ("repet 8");
			premiereCol[12] = ("repet 9");
			premiereCol[13] = ("repet 10");

			for (GridCoordinates2D key : cellRepet.keySet()) {
				switch (cellRepet.get(key)) {
				case 1:
					tableauFinal[4]++;
					break;
				case 2:
					tableauFinal[5]++;
					break;
				case 3:
					tableauFinal[6]++;
					break;
				case 4:
					tableauFinal[7]++;
					break;
				case 5:
					tableauFinal[8]++;
					break;
				case 6:
					tableauFinal[9]++;
					break;
				case 7:
					tableauFinal[10]++;
					break;
				case 8:
					tableauFinal[11]++;
					break;
				case 9:
					tableauFinal[12]++;
					break;
				case 10:
					tableauFinal[13]++;
					break;
				}
				if (cellRepet.get(key) < 10 && compareBaSt == false) {
					statInstable.addValue(cellEvalFinal.get(key));
				}

				if (cellRepet.get(key) == 10 && compareBaSt == false) {
					statStable.addValue(cellEvalFinal.get(key));
				}
			}

			if (compareBaSt == true) {
				tableauFinal[2] = histo[0] - tableauFinal[5];
				premiereCol[2] = "nb de cellules sélectionnées uniquement dans les simulations strictes";
			}

			tableauFinal[15] = statInstable.getMean();
			tableauFinal[16] = statInstable.getStandardDeviation();
			tableauFinal[17] = tableauFinal[16] / tableauFinal[15];
			tableauFinal[18] = statStable.getMean();
			tableauFinal[19] = statStable.getStandardDeviation();
			tableauFinal[20] = tableauFinal[19] / tableauFinal[18];

			if (compareBaSt == true) {
				double moyenne = 0;
				double temp = 0;
				double compteur = 0;
				for (GridCoordinates2D coord : cellEvalFinal.keySet()) {
					temp = temp + cellEvalFinal.get(coord);
					compteur = compteur + 1;
				}
				moyenne = temp / compteur;
				premiereCol[15] = "moyenne des évaluations dans les simulations St et pas Ba";
				tableauFinal[15] = moyenne;
			}

		} else if (stabilite == true) {
			premiereCol[4] = ("repet de 0 a 100");
			premiereCol[5] = ("repet de 100 a 200");
			premiereCol[6] = ("de 200 a 300");
			premiereCol[7] = ("de 300 a 400");
			premiereCol[8] = ("de 400 a 500");
			premiereCol[9] = ("de 500 a 600");
			premiereCol[10] = ("de 600 a 700");
			premiereCol[11] = ("de 700 a 800");
			premiereCol[12] = ("de 800 a 900");
			premiereCol[13] = ("de 900 a 999");
			premiereCol[14] = ("1000 repet (allstar)");
			for (GridCoordinates2D key : cellRepet.keySet()) {
				if (0 < cellRepet.get(key) && cellRepet.get(key) <= 100) {
					tableauFinal[4]++;
				} else if (100 < cellRepet.get(key) && cellRepet.get(key) <= 200) {
					tableauFinal[5]++;
				} else if (200 < cellRepet.get(key) && cellRepet.get(key) <= 300) {
					tableauFinal[6]++;
				} else if (300 < cellRepet.get(key) && cellRepet.get(key) <= 400) {
					tableauFinal[7]++;
				} else if (400 < cellRepet.get(key) && cellRepet.get(key) <= 500) {
					tableauFinal[8]++;
				} else if (500 < cellRepet.get(key) && cellRepet.get(key) <= 600) {
					tableauFinal[9]++;
				} else if (600 < cellRepet.get(key) && cellRepet.get(key) <= 700) {
					tableauFinal[10]++;
				} else if (700 < cellRepet.get(key) && cellRepet.get(key) <= 800) {
					tableauFinal[11]++;
				} else if (800 < cellRepet.get(key) && cellRepet.get(key) <= 900) {
					tableauFinal[12]++;
				} else if (900 < cellRepet.get(key) && cellRepet.get(key) < 1000) {
					tableauFinal[13]++;
				} else if (cellRepet.get(key) == 1000) {
					tableauFinal[14]++;
				}
				if (cellRepet.get(key) < 1000) {
					statInstable.addValue(cellEvalFinal.get(key));
				}
				if (cellRepet.get(key) == 1000) {
					statStable.addValue(cellEvalFinal.get(key));
				}
			}

			tableauFinal[15] = statInstable.getMean();
			tableauFinal[16] = statInstable.getStandardDeviation();
			tableauFinal[17] = tableauFinal[16] / tableauFinal[15];
			tableauFinal[18] = statStable.getMean();
			tableauFinal[19] = statStable.getStandardDeviation();
			tableauFinal[20] = tableauFinal[19] / tableauFinal[18];

		}

		// moyenne des évaluations pour les cellules instables

		StatTab tableauStat = new StatTab("descriptive_statistics", nameScenar, tableauFinal, premiereCol);
		tableauStat.toCsv(statFile, firstline);
		firstline = false;

		return statFile;
	}

	private static void generateCsvFile(Hashtable<String, Double> cellRepet, File file, String name) throws IOException {
		Hashtable<String, double[]> newCellRepet = new Hashtable<String, double[]>();
		System.out.println("size:" + cellRepet.size());
		for (String element : cellRepet.keySet()) {
			Double[] transvaser = new Double[1];
			transvaser[0] = cellRepet.get(element);
		}
		String[] fisrtCol = null;
		generateCsvFile(newCellRepet, file, name, fisrtCol);
	}

	public static void generateCsvFile(Hashtable<String, double[]> cellRepet, File file, String name, String[] premiereColonne) throws IOException {
		File fileName = new File(file + "/" + name + ".csv");

		boolean addAfter = true;
		FileWriter writer = new FileWriter(fileName, addAfter);
		if (premiereColonne != null) {
			for (String title : premiereColonne) {
				writer.append(title + ",");
			}
			writer.append("\n");
		}

		for (String nomScenar : cellRepet.keySet()) {
			writer.append(nomScenar + ",");
			for (double val : cellRepet.get(nomScenar)) {
				writer.append(val + ",");
			}
			writer.append("\n");
		}
		writer.close();
	}

	public static void generateCsvFileCol(Hashtable<String, double[]> cellRepet, File file, String name) throws IOException {
		File fileName = new File(file + "/" + name + ".csv");
		FileWriter writer = new FileWriter(fileName, false);
		for (String nomm : cellRepet.keySet()) {
			double[] tableau = cellRepet.get(nomm);
			for (int i = 0; i < tableau.length; i++) {
				String in = Double.toString(tableau[i]);
				writer.append(in + "\n");
			}

		}
		writer.close();
	}

	public static void main(String[] args) throws Exception {
		/*
		 * //oneSIm echelle = "20"; ArrayList<File> atest = new ArrayList<File>(); File dir1 = new
		 * File("/media/mcolomb/Data_2/resultTest/OneTest/LAEA/N5--St--org.thema.mupcity.AHP@610455d6--Moy--1-analyse-20.0.tif"); atest.add(dir1); File dir2 = new
		 * File("/media/mcolomb/Data_2/resultTest/OneTest/Lambert/N5--St--org.thema.mupcity.AHP@610455d6--Moy--1-analyse-20.0.tif"); atest.add(dir2);
		 * 
		 * mergeRasters(atest, "analyseProjection");
		 */

		// changement de la grille
		// sensibility=true;
		// for (int yo = 0; yo <= 2; yo++) {
		//
		// if (yo == 0) {
		// rootFile = new File("/media/mcolomb/Data_2/resultExplo/MouvGrid/decal-20/");
		// } else if ( yo == 1){
		// rootFile = new File("/media/mcolomb/Data_2/resultExplo/MouvGrid/decal-60/");
		// }
		// else {
		// rootFile = new File("/media/mcolomb/Data_2/resultExplo/MouvGrid/decal-180/");
		// }
		//
		//// ArrayList<String> echelles = new ArrayList<String>();
		//// for (Integer i = 20; i <= 180; i = i * 3) {
		//// String nombre = i.toString();
		//// echelles.add(nombre);
		//// }
		//// for (String scale : echelles) {
		// echelle = "20";
		// discrete = true;
		// cutBorder = true;
		// sensibility = true;
		// gridChange();
		// }
		//
		// }
		discrete = true;
		compare20_180 = true;
		compare20_60 = true;
		// for(Integer tc=22;tc<=22;tc=tc+1){
		rootFile = new File("/media/mcolomb/Data_2/resultExplo/Stability/N5YagBa");
		// echelle = tc.toString();
		for (Integer ech = 20; ech <= 180; ech = ech * 3) {
			echelle = ech.toString();
			String echStr = echelle + "m";
			System.out.println("echelle :" + echStr);
			// rootFile = new File(rootFile, echStr);
			List<File> fileToTest = new ArrayList<File>();
			fileToTest = selectWith("", null);
			mergeRasters(fileToTest, echStr + "analyse");
		}

		// }

		/*
		 * //test_seuil pour la diff entre les tests avec et sans seuils
		 * 
		 * rootFile = new File("/media/mcolomb/Data_2/resultTest/test_seuil/St/N6/results/"); ArrayList<String> echelles = new ArrayList<String>();
		 * 
		 * for (Integer i = 20; i <= 180; i = i * 3) { String nombre = i.toString(); echelles.add(nombre); } for (String scale : echelles) { echelle = scale;
		 * 
		 * for (int i = 2; i <= 6; i++) { ArrayList<File> listFile = new ArrayList<File>(); for (int j = 0; j < 10; j++) { File fileCool = new File(rootFile, ("seuil_10-" + i +
		 * "/replication_numero-" + j + "-eval_anal-" + echelle + ".0.tif")); listFile.add(fileCool); } System.out.println(listFile); mergeRasters(listFile, "test-seuil_10-" + i);
		 * } }
		 * 
		 * 
		 */
		// sensibilité de la grille

		// File root = rootFile;
		// discrete = false;
		// for (int decalage = 1; decalage <= 9; decalage = decalage * 3) {
		// rootFile = root;
		// rootFile = new File(rootFile + "/MouvData/" + decalage + "m");
		// sensibility = true;
		// switch (decalage) {
		// case 1:
		// echelle = "20";
		// break;
		// case 3:
		// echelle = "60";
		// break;
		// case 9:
		// echelle = "180";
		// break;
		// }
		//
		// gridSensibility();
		//
		//
		// }
		/*
		 * //test de réplications discrètisé
		 * 
		 * rootFile = new File(rootFile, "/tests_param/results/G0/"); compareBaSt();
		 */

		// etude des stabilités

		// discrete = false;
		//
		// echelle = "20";
		// for (int i = 0 ; i<=2 ; i++){
		//
		// rootFile = new File(rootFile, "/Stability/N6MoySt");
		// rootFile = new File("/media/mcolomb/Data_2/resultTest/sensibility/5St/results");
		//
		// switch(i){
		// case 1 :
		// rootFile = new File("/media/mcolomb/Data_2/resultExplo/Stability/N5MoySt");
		// break;
		// case 2:
		// rootFile = new File("/media/mcolomb/Data_2/resultExplo/Stability/N5MoyBa");
		// break;
		// }
		// replicationStab();
		//
		// }
		// discrete = false;
		// echelle = "20";
		// for (int i = 0; i <= 3; i++) {
		//
		// rootFile = new File("/media/mcolomb/Data_2/resultExplo/Stability/N6MoySt");
		// switch (i) {
		// case 1:
		// rootFile = new File("/media/mcolomb/Data_2/resultExplo/Stability/N5MoySt");
		// break;
		// case 2:
		// rootFile = new File("/media/mcolomb/Data_2/resultExplo/Stability/N5MoyBa");
		// break;
		// case 3:
		// rootFile = new File("/media/mcolomb/Data_2/resultExplo/Stability/N5YagBa");
		// break;
		// }
		// replicationStab();
		// }

		/*
		 * ArrayList<File> listFil = new ArrayList<File>(); for (int i = 0; i <= 8; i++) { File file = new
		 * File("/media/mcolomb/Data_2/resultTest/changement_grille/decal-180/stats-discrete/cellByCitydiscreteSensibility_case-" + i + ".csv"); listFil.add(file); }
		 * 
		 * File statest = new File("/media/mcolomb/Data_2/resultTest/changement_grille/decal-180/stats-discrete/"); compareCities(listFil, statest);
		 */
	}
}
