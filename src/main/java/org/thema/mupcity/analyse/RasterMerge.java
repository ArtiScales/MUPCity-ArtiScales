package org.thema.mupcity.analyse;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.synth.SynthScrollBarUI;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

public class RasterMerge {

	public static void main(String[] args) throws Exception {

		File fileIn = new File("/media/mcolomb/Data_2/resultExplo/testNov/exOct");
		File fileOut = new File("/media/mcolomb/Data_2/resultExplo/testNov/exOct/raster/remerged.tif");
		String nameSimul = "N5_St_Moy_ahpx_seed_";
		String echelle = "20";
		merge(fileIn, fileOut, nameSimul, echelle);
	}

	/** merge multiple raster to get their replication. Overloaded in the case of a raw mupcity output;
	 * /!\ Rasters must be the same size /!\
	 * @param folderIn : file containing files containing raster to merge
	 * @param fileOut : raster out
	 * @param nameSimul : the code-name of your to-merge rasters
	 * @param filter : filter the scale that you want to keep
	 * @return the raster file
	 * @throws Exception
	 */
	public static boolean merge(File folderIn, File fileOut, String nameSimul, final String filter) throws Exception {

		List<File> select = new ArrayList<>();

		for (File f : folderIn.listFiles()) {
			if (f.isDirectory()) {

				for (File ff : f.listFiles()) {
					if (ff.getName().contains(nameSimul) && ff.getName().endsWith("eval_anal-20.0.tif")) {
						select.add(ff);
					}
				}
			}
		}

		int echelle = Integer.parseInt(filter);
		return merge(select, fileOut, echelle);

	}
	/** merge multiple raster to get their replication
	 *  /!\ Rasters must be the same size /!\
	 * @param folderIn file tableau containing the rasterfiles
	 * @param fileOut raster out
	 * @param echelle the granularity of your rasters
	 * @return the raster file
	 * @throws Exception
	 */
	public static boolean merge(List<File> folderIn, File fileOut, int echelle) throws Exception {

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

		double xMinIni = 0, yMinIni = 0, xMaxIni = 0, yMaxIni = 0;

		// set matrice

		GeoTiffReader readerSet = new GeoTiffReader(folderIn.get(0));
		GridCoverage2D coverageSet = readerSet.read(params);
		Envelope2D env = coverageSet.getEnvelope2D();

		float[][] imagePixelData = new float[(int) Math.floor(env.getWidth() / echelle)][(int) Math.floor(env.getHeight() / echelle)];
	
		double xMin = env.getMinX();
		double yMin = env.getMinY();

		for (int fInd = 0; fInd < (folderIn.size()); fInd++) {

			System.out.println("Image n° " + fInd + "  sur " + (folderIn.size() - 1));

			GeoTiffReader reader = new GeoTiffReader(folderIn.get(fInd));
			GridCoverage2D coverage = reader.read(params);

			for (int i = 0; i < imagePixelData.length; ++i) {
				for (int j = 0; j < imagePixelData[0].length; ++j) {
					DirectPosition2D pt = new DirectPosition2D(xMin + (2 * i + 1) * echelle / 2, yMin + (2 * j + 1) * echelle / 2);
					float[] val = (float[]) coverage.evaluate(pt);
					if (val[0] > 0) {
						imagePixelData[i][j] = imagePixelData[i][j]+1;
					}
				}
			}
		}

		float[][] imgpix2 = new float[imagePixelData[0].length][imagePixelData.length];
		float[][] imgpix3 = new float[imagePixelData[0].length][imagePixelData.length];
		for (int i = 0; i < imgpix2.length; ++i) {
			for (int j = 0; j < imgpix2[0].length; ++j) {
				imgpix2[i][j] = imagePixelData[imgpix2[0].length - 1 - j][i];
			}
		}
		for (int i = 0; i < imgpix3.length; ++i) {
			for (int j = 0; j < imgpix3[0].length; ++j) {
				imgpix3[i][j] = imgpix2[imgpix3.length-1-i][imgpix3[0].length-1-j];
			}
		}

		writeGeotiff(fileOut, imgpix3, env);

		return true;
	}

	public static void writeGeotiff(File fileName, float[][] imagePixelData, Envelope2D env) {
		GridCoverage2D coverage = new GridCoverageFactory().create("OTPAnalyst", imagePixelData, env);
		writeGeotiff(fileName, coverage);
	}

	public static void writeGeotiff(File fileName, GridCoverage2D coverage) {
		try {
			GeoTiffWriteParams wp = new GeoTiffWriteParams();
			wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
			wp.setCompressionType("LZW");
			ParameterValueGroup params = new GeoTiffFormat().getWriteParameters();
			params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);
			GeoTiffWriter writer = new GeoTiffWriter(fileName);
			writer.write(coverage, (GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[1]));
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

}