package org.thema.mupcity.analyse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

public class convertRasterToCsv {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File rastFile = new File("/home/mcolomb/informatique/MUP/explo/enveloppes/evalE-moy-NU.tif");
		ParameterValue<OverviewPolicy> policy = AbstractGridFormat.OVERVIEW_POLICY.createValue();
		policy.setValue(OverviewPolicy.IGNORE);
		ParameterValue<String> gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();
		ParameterValue<Boolean> useJaiRead = AbstractGridFormat.USE_JAI_IMAGEREAD.createValue();
		useJaiRead.setValue(false);
		GeneralParameterValue[] params = new GeneralParameterValue[] { policy, gridsize, useJaiRead };

		GridCoverage2DReader reader = new GeoTiffReader(rastFile);
		GridCoverage2D coverage = reader.read(params);
		GridEnvelope dimensions = reader.getOriginalGridRange();
		GridCoordinates maxDimensions = dimensions.getHigh();

		int w = maxDimensions.getCoordinateValue(0) + 1;
		int h = maxDimensions.getCoordinateValue(1) + 1;
		int numBands = reader.getGridCoverageCount();
		double[] vals = new double[numBands];

		// beginning of the all cells loop			
		int debI = 0;
		int debJ = 0;
		Hashtable<String, Double> cells = new Hashtable<String, Double>();

		for (int i = debI; i < w; i++) {
			for (int j = debJ; j < h; j++) {
				GridCoordinates2D coord = new GridCoordinates2D(i, j);
				double[] temp = coverage.evaluate(coord, vals);
				if (temp[0] > 0.001) {
					cells.put(coord.toString(), temp[0]);
				}
			}
		}
	
		//RasterAnalyse.generateCsvFileCol(cells,new File (rastFile.getParent()),);
		File fileName = new File(rastFile + "-tocsv.csv");
		FileWriter writer = new FileWriter(fileName, false);
		writer.append("eval");
		writer.append("\n");
		for (String nomm : cells.keySet()) {
			double tableau = cells.get(nomm);
			for (int i = 0; i < tableau; i++) {
				String in = Double.toString(tableau);
				writer.append(in + "\n");
			}
		}
		writer.close();
	}
}
