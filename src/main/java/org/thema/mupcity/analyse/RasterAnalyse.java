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
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.TransformException;
import org.thema.mupcity.Project;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class RasterAnalyse {

	public static File rootFile = new File("/media/mcolomb/Data_2/resultTest/");
	public static File discreteFile = new File("/home/mcolomb/informatique/MUP/explo/data/admin_typo.shp");
	public static boolean discrete = false;
	public static boolean stabilite = false;
	public static boolean cutBorder = false;
	// TODO faire bien cette fonction (dans une autre classe?)
	public static String echelle;
	public static boolean firstline = true;
	public static boolean compareAHP = false;
	public static boolean compareBaSt = false;
	public static boolean compare20_180 = false;
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
		discrete = true;
		replication();
		replicationStab();
		gridSensibility();
		gridChange();
	}

	public static ArrayList<File> selectWith(String with, ArrayList<File> in) throws IOException {
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
	 * method which analyse the small replication of a lot of parameters it creates a tab sheet on the /stats folder from the rootFile
	 * 
	 * @author Maxime Colomb
	 * @param isDiscrete
	 *            boolean si la décomposition est discrétisé selon un shapefile
	 * @param echelle
	 *            l'échelle de décomposition étudié
	 * @throws Exception
	 * 
	 */
	public static void replication() throws Exception {
		//rootFile = new File(rootFile,"tests_param");
		for (int n = 5; n <= 7; n++) {
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
		ArrayList<File> listRepliFiles = selectWith("replication_numero", null);
		/*//to test
		ArrayList<File> listRepliFiles = new ArrayList<File>();
		for (int y = 0; y < 15; y++) {
			listRepliFiles.add(listRepliFiless.get(y));
		}*/
		/*	if (isDiscrete) {
				mergeRasters(listRepliFiles, "staby", true);
			} else {
			*/

		stabilite = true;
		mergeRasters(listRepliFiles, "staby");

		//}
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
		compareBaSt = true;
		ArrayList<File> oneSeed = selectWith("replication_7", null);
		
	}

	public static void compare180(Hashtable<DirectPosition2D, Integer> cellRepetCentroid, Hashtable<DirectPosition2D, Float> cellEvalCentroid, String namescenar) throws IOException {
		Hashtable<DirectPosition2D, Integer> cellRepet180 = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Float> cellEval180 = new Hashtable<DirectPosition2D, Float>();

		if (echelle.equals("20")) {
			SvgCellRepet20 = cellRepetCentroid;
			SvgCellEval20 = cellEvalCentroid;
		}

		if (echelle.equals("180")) {
			float sumCellEval = 0;

			for (DirectPosition2D coord20 : SvgCellEval20.keySet()) {
				sumCellEval = sumCellEval+ SvgCellEval20.get(coord20);
			}
			float avCellEval = sumCellEval / SvgCellEval20.size();

			int cellIn = 0;
			int cellTotal = SvgCellRepet20.size();
			System.out.println("cell totales : "+cellTotal);
			ArrayList<Float> cellInEval = new ArrayList<Float>();
			cellRepet180 = cellRepetCentroid;
			for (DirectPosition2D coord180 : cellRepetCentroid.keySet()) {
				double emp180Xmin = coord180.getX() - 180 / 2;
				double emp180Xmax = coord180.getX() + 180 / 2;
				double emp180Ymin = coord180.getY() - 180 / 2;
				double emp180Ymax = coord180.getY() + 180 / 2;
				for (DirectPosition2D coord20 : SvgCellRepet20.keySet()) {
					if (coord20.getX() > emp180Xmin && coord20.getX() < emp180Xmax && coord20.getY() > emp180Ymin && coord20.getY() < emp180Ymax) {
						cellIn = cellIn+ 1;
						cellInEval.add(SvgCellEval20.get(coord20));
					}
				}
			}

			float sumVal = 0;
			for (float val : cellInEval) {
				sumVal = sumVal+ val;
			}
			float averageValIn = sumVal / cellInEval.size();
			int cellOut = cellTotal - cellIn;

			double[] resultStats = new double[5];
			String[] firstLine = new String[5];

			firstLine[0] = "différences 20-180";
			firstLine[1] = "cell dans 20 et non dans 180";
			firstLine[2] = "cell dans 20 et dans 180";
			firstLine[3] = "eval moyenne de ces cellules In";
			firstLine[4] = "eval moyenne de toutes les cellules";

			resultStats[1] = cellOut;
			resultStats[2] = cellIn;
			resultStats[3] = averageValIn;
			resultStats[4] = avCellEval;

			StatTab result = new StatTab("compare_20to180", (namescenar+"compare-180-20"), resultStats, firstLine);
			File statFile = new File(rootFile, "/stats");
			result.toCsv(statFile, true);
		}
	}

	/**
	 * <<<<<<< explo method to analyse an output when the data have been moved a little in order to impact the sensibility of the simulation to the grid.
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
		ArrayList<File> yo = new ArrayList<File>();
		for (int i = 0; i <= 8; i++) {
			File file = new File(rootFile + "/data" + i + "/replication_numero-42-eval_anal-" + echelle + ".0.tif");
			file.listFiles();
			listRepliFile.add(file);

			yo = selectWith("42", listRepliFile);
		}
		System.out.println(yo);

		mergeRasters(yo, "gridSensibility");
	}

	/**
	 * method to analyse an output when the data have been moved a little in order to impact the sensibility of the simulation to the grid.
	 * 
	 * @param echelle
	 *            : scale in which the analyse should take place
	 * @param nbTest
	 *            : Number of different simulation runned for the sensibility test
	 * @return void, but creates a statistic file
	 * @throws Exception
	 */
	public static void gridChange() throws Exception {
		ArrayList<File> listRepliFile = new ArrayList<File>();

		for (int i = 0; i <= 8; i++) {
			File file = new File(rootFile + "/G" + i + "/replication_numero-42-eval_anal-" + echelle + ".0.tif");
			listRepliFile.add(file);
		}
		System.out.println(listRepliFile);
		mergeRasters(listRepliFile, "gridCompare");
	}

	/**
	 * mergeRaster Merge the given Array of Files. Return an array of statistic values. Will also return a merged tif (in construction) ======= Merge the given Array of Files. Return an array of statistic values. Will also return a merged tif (in construction) >>>>>>> f7654ed maj
	 * workflow et comm rasteranalyse
	 * 
	 * @param listRepliFile
	 *            : ArrayList of File pointing to the raster layer to merge
	 * @return array of statistics results
	 * @throws Exception
	 */
	public static void mergeRasters(ArrayList<File> listRepliFile, String nameScenar) throws Exception {

		//creating different folders
		File rasterFile = new File(rootFile + "/raster/");
		File raster = new File(rasterFile + "/" + nameScenar + "_rasterMerged_ech_" + echelle + ".tif");
		File rasterStable = new File(rasterFile + "/" + nameScenar + "_rasterMerged-Stable-_ech_" + echelle + ".tif");
		rasterFile.mkdirs();

		//variables to create statistics

		DescriptiveStatistics statNb = new DescriptiveStatistics();
		Hashtable<GridCoordinates2D, Integer> cellRepet = new Hashtable<GridCoordinates2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetCentroid = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Float> cellEvalCentroid = new Hashtable<DirectPosition2D, Float>();
		Hashtable<GridCoordinates2D, ArrayList<Float>> cellEval = new Hashtable<GridCoordinates2D, ArrayList<Float>>();

		int nbDeScenar = 0;

		double[] histo = new double[listRepliFile.size()];
		int iter = 0;

		//variables for merged raster - not cool coz i cannot know the number of column and lines of the enveloppe yet and the type need it -- change the type to a collection or an arraylist? 

		float[][] imagePixelData = new float[1467][1467];
		float[][] imagePixelDataStable = new float[1467][1467];

		Envelope2D env = null;

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

			// in case of a move of the grid, we may have to delete the border cells because they will be moved

			if ((discrete == true && Double.parseDouble(echelle) <= 180) || cutBorder == true) {

				double Xmin = 914760;
				double Xmax = 943200;
				double Ymin = 6680157;
				double Ymax = 6701217;

				if (cutBorder == true) {
					new CutThaBorder(f, Xmin, Xmax, Ymin, Ymax);
					Xmin = CutThaBorder.getXmin();
					Xmax = CutThaBorder.getXmax();
					Ymin = CutThaBorder.getYmin();
					Ymax = CutThaBorder.getYmax();
				}

				for (double r = Xmin; r <= Xmax; r = r + Double.parseDouble(echelle)) {//those values are the bounds from project (and upped to correspond to a multiple of 180 to analyse all the cells in the project) 
					for (double t = Ymin; t <= Ymax; t = t + Double.parseDouble(echelle)) {
						DirectPosition2D coordCentre = new DirectPosition2D(r, t);
						float[] yo = (float[]) coverage.evaluate(coordCentre);
						if (yo[0] > 0) {
							compteurNombre = compteurNombre + 1;
							if (cellRepetCentroid.containsKey(coordCentre)) { // si la cellule a déja été sélectionné lors de réplications
								cellRepetCentroid.put(coordCentre, cellRepetCentroid.get(coordCentre) + 1);
								//on mets les valeurs d'évaluation dans un tableau

							} else { // si la cellule est sélectionné pour la première fois
								cellRepetCentroid.put(coordCentre, 1);
							}
							cellEvalCentroid.put(coordCentre, yo[0]);
						}
					}
				}
			} else {
				for (int i = debI; i < w; i++) {
					for (int j = debJ; j < h; j++) {
						GridCoordinates2D coord = new GridCoordinates2D(i, j);
						if (coverage.evaluate(coord, vals)[0] > 0) {
							compteurNombre = compteurNombre + 1;
							if (cellRepet.containsKey(coord)) { // si la cellule a déja été sélectionné lors de réplications
								cellRepet.put(coord, cellRepet.get(coord) + 1);
								ArrayList<Float> temp = cellEval.get(coord); //on mets les valeurs d'évaluation dans un tableau
								temp.add((float) coverage.evaluate(coord, vals)[0]);
								cellEval.put(coord, temp);
								imagePixelData[j][i]=imagePixelData[j][i]+1;
							} else {// si la cellule est sélectionné pour la première fois
								cellRepet.put(coord, 1);
								ArrayList<Float> firstList = new ArrayList<Float>();
								firstList.add((float) coverage.evaluate(coord, vals)[0]);
								cellEval.put(coord, firstList);
								imagePixelData[j][i]=imagePixelData[j][i]+1;
							}
						}
					}
				}
			}

			System.out.println("il y a " + compteurNombre + " cellules dans " + nameScenar + " de réplication " + nbDeScenar);

			statNb.addValue(compteurNombre);
			histo[iter] = (double) cellRepet.size();
			iter = iter + 1;
			System.out.println(cellRepet.size());
		}
		//create a merged raster and a stable merged raster
		if (discrete != true && compareAHP != true && cutBorder != true && nbDeScenar != 0) {
			int lgt = imagePixelData.length;
			System.out.println("lgt: "+lgt);
			//reverse the pixels coz the raster was turned bad
			for (int p = 1; p < lgt; p++) {
				for (int q = 1; q < lgt; q++) {
					if (imagePixelData[p][q] == nbDeScenar){ 
						imagePixelDataStable[p][q] = imagePixelData[p][q];
					}
				}
			}
			writeGeotiff(raster.getAbsolutePath(), imagePixelData, env);

			writeGeotiff(rasterStable.getAbsolutePath(), imagePixelDataStable, env);

		}

		//compare different size of cells
		if (compare20_180 == true && discrete == true) {
			compare180(cellRepetCentroid, cellEvalCentroid, nameScenar);

		}

		if ((discrete == true || cutBorder == true)&& compare20_180 == false ) {
			splitMergedTypo(nameScenar, cellRepetCentroid, cellEvalCentroid);
		}

		else if (discrete == false && compare20_180 == false ) {
			createStats(nameScenar, histo, statNb, cellRepet, cellEval);
		}
	}

	private static GeometryFactory geomFact = new GeometryFactory();

	private static void splitMergedTypo(String nameScenar, Hashtable<DirectPosition2D, Integer> cellRepet, Hashtable<DirectPosition2D, Float> cellEval) throws IOException {
		Hashtable<DirectPosition2D, Integer> cellRepetPeriCentre = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetBanlieue = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetPeriUrbain = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetRural = new Hashtable<DirectPosition2D, Integer>();
		Hashtable<DirectPosition2D, Integer> cellRepetHyperCentre = new Hashtable<DirectPosition2D, Integer>();

		ArrayList<Hashtable<DirectPosition2D, Integer>> listCellByTypo = new ArrayList<Hashtable<DirectPosition2D, Integer>>();

		Hashtable<String, double[]> cellByCity = new Hashtable<String, double[]>();
		Hashtable<String, ArrayList<Float>> evalByCity = new Hashtable<String, ArrayList<Float>>();

		File statFile = new File(rootFile + "/stats-discrete");
		statFile.mkdirs();

		String[] premiereCol = new String[12];

		ShapefileDataStore typo = new ShapefileDataStore(discreteFile.toURL());

		ContentFeatureCollection features = typo.getFeatureSource().getFeatures();

		for (DirectPosition2D coord : cellRepet.keySet()) {

			for (Object eachFeature : features.toArray()) {
				SimpleFeatureImpl feature = (SimpleFeatureImpl) eachFeature;
				Geometry geom = (Geometry) feature.getDefaultGeometry();
				String city = feature.getAttribute("NOM_COM").toString();
				String typopo = feature.getAttribute("typo").toString();
				Point pt = geomFact.createPoint(new Coordinate(coord.getX(), coord.getY()));

				//creating the city statistics (only for test-param for now) and typology
				if (geom.contains((Geometry) pt)) {
					double[] nbByCity = new double[5];
					if (cellByCity.containsKey(city)) {
						nbByCity[0] = cellByCity.get(city)[0] + 1;
						//class if cells are varaible or not
						if (cellRepet.get(coord) == 10) {
							nbByCity[1] = cellByCity.get(city)[1] + 1;
							nbByCity[2] = cellByCity.get(city)[2];
						} else if (1 <= cellRepet.get(coord) && cellRepet.get(coord) < 10) {
							nbByCity[2] = cellByCity.get(city)[2] + 1;
							nbByCity[1] = cellByCity.get(city)[1];
						}
						//aggregation of evaluations
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
						nbByCity[0] = (double) 1;
						nbByCity[3] = cellEval.get(coord);
						ArrayList<Float> temp = new ArrayList<Float>();
						temp.add(cellEval.get(coord));
						evalByCity.put(city, temp);
						if (cellRepet.get(coord) == 10) {
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
						nbByCity[4] = 0;
						break;

					}

					cellByCity.put(city, nbByCity);

				}
			}
		}

		listCellByTypo.add(cellRepetPeriCentre);
		listCellByTypo.add(cellRepetBanlieue);
		listCellByTypo.add(cellRepetPeriUrbain);
		listCellByTypo.add(cellRepetRural);
		listCellByTypo.add(cellRepetHyperCentre);

		String[] nameLine = new String[6];

		nameLine[0] = (nameScenar + " -- City");
		nameLine[1] = "Total Cells";
		nameLine[2] = "Stable cells";
		nameLine[3] = "Unstable cells";
		nameLine[4] = "average evaluation";
		nameLine[5] = "typology of the city";

		generateCsvFile(cellByCity, statFile, ("cellByCity" + nameScenar), nameLine);

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
	}

	private static void createStats(String nameScenar, double[] histo, DescriptiveStatistics statNb, Hashtable<GridCoordinates2D, Integer> cellRepet, Hashtable<GridCoordinates2D, ArrayList<Float>> cellEval) throws IOException {

		File statFile = new File(rootFile + "/stats");
		if (compareAHP == true) {
			statFile = new File(rootFile + "/stat_compare-AHP");
		}
		statFile.mkdirs();

		double[] tableauFinal = new double[21];
		String[] premiereCol = new String[21];

		DescriptiveStatistics statInstable = new DescriptiveStatistics();
		DescriptiveStatistics statStable = new DescriptiveStatistics();

		// des statistiques du merge des rasters

		Hashtable<GridCoordinates2D, Float> cellEvalFinal = new Hashtable<GridCoordinates2D, Float>();

		//historique du nombre de cellules sélectionné par scénarios
		Hashtable<String, double[]> enForme = new Hashtable<String, double[]>();
		enForme.put("histo", histo);
		String[] noFirstCol = null;
		generateCsvFile(enForme, statFile, "selected_cells_all_simu", noFirstCol);

		// statistiques du nombre de cellules par scénario
		tableauFinal[0] = Double.parseDouble(echelle);
		premiereCol[0] = "echelle";
		tableauFinal[1] = statNb.getMean();
		premiereCol[1] = "nombre moyen de cellules sélectionnées par simulations";
		tableauFinal[2] = statNb.getStandardDeviation();
		premiereCol[2] = "ecart-type du nombre des cellules sélectionnées par simulations";
		tableauFinal[3] = tableauFinal[2] / tableauFinal[1];
		premiereCol[3] = "coeff de variation du nombre de cellules sélectionnées par simulations";

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
		int i = 0;
		for (int repli : cellRepet.values()) {
			tableauRepl[i] = repli;
			i = i + 1;
		}
		if (tableauMoy.length > 1 && stabilite == false) { // si il n'y a pas de cellules, la covariance fait planter
			double correlationCoefficient = new Covariance().covariance(tableauMoy, tableauRepl);
			tableauFinal[14] = correlationCoefficient;
			premiereCol[14] = ("coefficient de correlation entre le nombre de réplication et les évaluations des cellules");
		}

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
				if (cellRepet.get(key) < 10) {
					statInstable.addValue(cellEvalFinal.get(key));
				}

				if (cellRepet.get(key) == 10) {
					statStable.addValue(cellEvalFinal.get(key));
				}
			}
			tableauFinal[15] = statInstable.getMean();
			tableauFinal[16] = statInstable.getStandardDeviation();
			tableauFinal[17] = tableauFinal[16] / tableauFinal[15];
			tableauFinal[18] = statStable.getMean();
			tableauFinal[19] = statStable.getStandardDeviation();
			tableauFinal[20] = tableauFinal[19] / tableauFinal[18];

		} else {
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
	}

	private static void generateCsvFile(Hashtable<String, double[]> cellRepet, File file, String name, String[] premiereColonne) throws IOException {
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

	public static void writeGeotiff(String fileName, float[][] imagePixelData, Envelope2D env) {

		GridCoverage2D coverage = new GridCoverageFactory().create("OTPAnalyst", imagePixelData, env);
		try {
			GeoTiffWriteParams wp = new GeoTiffWriteParams();
			wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
			wp.setCompressionType("LZW");
			ParameterValueGroup params = new GeoTiffFormat().getWriteParameters();
			params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);
			GeoTiffWriter writer = new GeoTiffWriter(new File(fileName));
			writer.write(coverage, (GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[1]));
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public static void main(String[] args) throws Exception {
		/*
				//oneSIm
				echelle="180";
			File dir = new File (rootFile, "OneTest/result");
				ArrayList<File> atest = new ArrayList<File> ();
			for (File f : dir.listFiles()){
				atest.add(f);		
			}
		mergeRasters(atest, "analyseYag");
				
				//changement de la grille 
				/*
				
				for (int yo = 1; yo <= 2; yo++) {
				
					if (yo == 1) {
						rootFile = new File("/media/mcolomb/Data_2/resultTest/changement_grille/decal-20/");
					} else {
						rootFile = new File("/media/mcolomb/Data_2/resultTest/changement_grille/decal-180/");
					}
				
					ArrayList<String> echelles = new ArrayList<String>();
					for (Integer i = 20; i <= 180; i = i * 3) {
						String nombre = i.toString();
						echelles.add(nombre);
					}
					for (String scale : echelles) {
						echelle = scale;
						cutBorder = true;
						gridChange();
					}
				
				}
				
				//test_seuil pour la diff entre les tests avec et sans seuils
				
				rootFile = new File("/media/mcolomb/Data_2/resultTest/test_seuil/St/results/");
				ArrayList<String> echelles = new ArrayList<String>();
				
				for (Integer i = 20; i <= 180; i = i * 3) {
					String nombre = i.toString();
					echelles.add(nombre);
				}
				for (String scale : echelles) {
					echelle = scale;
				
					for (int i = 2; i <= 6; i++) {
						ArrayList<File> listFile = new ArrayList<File>();
						for (int j = 42; j < 142; j = j + 10) {
							File fileCool = new File(rootFile, ("seuil_10-" + i + "/replication_numero-" + j + "-eval_anal-" + echelle + ".0.tif"));
							listFile.add(fileCool);
						}
						mergeRasters(listFile, "test-seuil_10-" + i);
					}
				}
				
				
		
		//sensibilité de la grille
		
		ArrayList<String> echelles = new ArrayList<String>();
		rootFile = new File(rootFile + "/mouv_data/3m");
		for (Integer i = 20; i <= 180; i = i * 3) {
			String nombre = i.toString();
			echelles.add(nombre);
		}
		for (String ech : echelles) {
			echelle = ech;
			compare20_180 = true;
			gridSensibility();
		}
		*/
		//test de réplications discrètisé
		ArrayList<String> echelles = new ArrayList<String>();
		rootFile = new File(rootFile, "tests_param/results/G0");
		for (Integer i = 20; i <= 180; i = i * 3) {
			String nombre = i.toString();
			echelles.add(nombre);
		}
		for (String ech : echelles) {
			System.out.println(ech);
			echelle = ech;
			replication();
		}

		/*
				// etude des trois grandes stabilités 
		
				ArrayList<String> echelles = new ArrayList<String>();
				for (Integer i = 20; i <= 180; i = i * 3) {
					String nombre = i.toString();
					echelles.add(nombre);
				}
		
				discrete = true;
		
				for (String ech : echelles) {
					echelle = ech;
		
					for (int i = 0; i < 3; i++) {
						switch (i) {
						case 0:
							rootFile = new File("/media/mcolomb/Data_2/resultTest/sensibility/Ba/results");
							break;
						case 1:
							rootFile = new File("/media/mcolomb/Data_2/resultTest/sensibility/Yag/results");
							break;
						case 2:
							rootFile = new File("/media/mcolomb/Data_2/resultTest/sensibility/St/results");
							break;
						}
						replicationStab();
		
					}
				}
		*/
	}
}
