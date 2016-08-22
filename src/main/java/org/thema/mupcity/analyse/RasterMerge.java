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
import org.geotools.geometry.Envelope2D;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

public class RasterMerge {

	public static void main(String[] args) throws Exception {

		String strFileIn = "/media/mcolomb/Data_2/resultTest/testMerge/";
		String strFolderOut = "/media/mcolomb/Data_2/resultTest/testMerge/folderout/out.tif";
		String filter = "20";
		merge(new File(strFileIn), new File(strFolderOut), filter);
	}

	public static boolean merge(File folderIn, File fileOut, final String filter) throws Exception {
		File[] yo = folderIn.listFiles();
		for (File f : yo) {
			System.out.println(f);
		}
		File[] select = folderIn.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				String path = pathname.getAbsolutePath();

				String[] splitPoint = path.split("\\.");

				// System.out.println(splitPoint.length);
				// System.out.println(path);

				if (splitPoint.length == 0) {
					return false;
				}

				String extension = splitPoint[splitPoint.length - 1];

				// System.out.println(extension);

				if (!(extension.equalsIgnoreCase("tif"))) {

					return false;
				}

				String val = splitPoint[splitPoint.length - 3];

				String[] valSpli = val.split("\\-");

				// System.out.println(valSpli[valSpli.length-1]);

				if (!(filter.equalsIgnoreCase(valSpli[valSpli.length - 1]))) {

					return false;
				}

				return true;
			}
		});

		for (int i = 0; i < select.length; i++) {
			System.out.println(select[i].getAbsolutePath());
		}

		return merge(select, fileOut);

	}

	public static boolean merge(File[] folderIn, File fileOut) throws Exception {

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

		/*float[][]imagePixelData;
		for (int i =0; i<preImagePixelData[i].length;i++){
			for (int j =0; j<preImagePixelData[i].length;j++){
				imagePixelData
			}
				
		}
		*/

		double xMinIni = 0, yMinIni = 0, xMaxIni = 0, yMaxIni = 0;

		float[][] imagePixelData = null;
		float [][] imagePD=null;
		Envelope2D env = null;

		for (int fInd = 0; fInd < (folderIn.length ); fInd++) {

			System.out.println("Image n° " + fInd + "  sur " + (folderIn.length - 1));

			GeoTiffReader reader = new GeoTiffReader(folderIn[fInd]);
			GridCoverage2D coverage = reader.read(params);

			if (env == null) {
				env = coverage.getEnvelope2D();
			}

			double xMni = env.getMinX();
			double yMni = env.getMinY();
			double xMax = env.getMaxX();
			double yMax = env.getMaxX();

			if (xMinIni == 0) {
				xMinIni = env.getMinX();
				yMinIni = env.getMinY();
				xMaxIni = env.getMaxX();
				yMaxIni = env.getMaxX();
			}

			GridEnvelope dimensions = reader.getOriginalGridRange();
			GridCoordinates maxDimensions = dimensions.getHigh();

			int w = maxDimensions.getCoordinateValue(0) + 1;
			int h = maxDimensions.getCoordinateValue(1) + 1;
			int numBands = reader.getGridCoverageCount();
			double[] vals = new double[numBands];

			if (imagePixelData == null) {

				imagePixelData = new float[w][h];
				for (int i = 0; i < w; i++) {
					for (int j = 0; j < h; j++) {
						imagePixelData[i][j] = 0;

					}
				}

			}
			
			if (imagePD == null) {

				imagePD = new float[w][h];
				for (int i = 0; i < w; i++) {
					for (int j = 0; j < h; j++) {
						imagePD[i][j] = 0;
					}
				}
			}

			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					GridCoordinates2D coord = new GridCoordinates2D(i, j);

					if (coverage.evaluate(coord, vals)[0] > 0) {
						imagePixelData[i][j]++;

					}
				}
			}

			int jzz = imagePixelData.length;
			
			for (int i =1; i< jzz;i++ ){
				System.out.println(i);
				for (int j =1; j< jzz;j++ ){
					imagePD[i][j]= imagePixelData[j][i];
				}
								}
			
			
			//Just to check the envelope

			if (Math.abs(xMni - xMinIni) > 0.01) {
				System.out.println("xMni change");
			}

			if (Math.abs(yMni - yMinIni) > 0.01) {
				System.out.println("yMni change");
			}

			if (Math.abs(xMax - xMaxIni) > 0.01) {
				System.out.println("xMax change");
			}

			if (Math.abs(yMax - yMaxIni) > 0.01) {
				System.out.println("yMax change");
			}

		}

		writeGeotiff(fileOut.getAbsolutePath(), imagePD, env);

		return true;
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

}