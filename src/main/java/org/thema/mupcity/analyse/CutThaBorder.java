package org.thema.mupcity.analyse;

import java.io.File;

/**
 * this class cut the border of a given grid location depending on how the grid modification 
 * configuration has been set (please report on the scheme)
 * 
 * @author mcolomb
 *
 */

public class CutThaBorder{
	
	static double xMin;
	static double yMin;
	static double xMax;
	static double yMax;
	
	public CutThaBorder(File f,double Xmin,double Xmax,double Ymin,double Ymax){
		//look in which grid's folder the input file is located
		String noom = f.getAbsoluteFile().toString();
		String[] grille = noom.split("/replication");
		int nGrid = Integer.parseInt(grille[0].substring(grille[0].length() - 1));
		int ecart = Integer.parseInt(RasterAnalyse.echelle);
		if (nGrid > 8) { // in this case the 180m-moved grid
			nGrid = nGrid - 8;
		}
		switch (nGrid) {
		case 0:
			Xmin = Xmin + ecart;
			Xmax = Xmax - ecart;
			Ymin = Ymin + ecart;
			Ymax = Ymax - ecart;
			break;
		case 1:
			Xmax = Xmax - 2 * ecart;
			Ymin = Ymin + ecart;
			Ymax = Ymax - ecart;
			break;
		case 2:
			Xmax = Xmax - 2 * ecart;
			Ymax = Ymax - 2 * ecart;
			break;
		case 3:
			Xmin = Xmin + ecart;
			Xmax = Xmax - ecart;
			Ymax = Ymax - 2 * ecart;
			break;
		case 4:
			Xmin = Xmin + 2 * ecart;
			Ymax = Ymax - 2 * ecart;
			break;
		case 5:
			Xmin = Xmin + 2 * ecart;
			Ymin = Ymin + ecart;
			Ymax = Ymax - ecart;
			break;
		case 6:
			Xmin = Xmin + 2 * ecart;
			Ymin = Ymin + 2 * ecart;
			break;
		case 7:
			Xmin = Xmin + ecart;
			Xmax = Xmax - ecart;
			Ymin = Ymin + 2 * ecart;
			break;
		case 8:
			Xmax = Xmax - ecart;
			Ymin = Ymin + 2 * ecart;
			break;
		}
		xMin = Xmin;
		xMax=Xmax;
		yMin=Ymin;
		yMax=Ymax;
	}
	public static double getXmin(){
		return xMin;
		
	}
	public static double getXmax(){
		return xMax;
		
	}
	public static double getYmin(){
		return yMin;
		
	}
	public static double getYmax(){
		return yMax;
		
	}
}
