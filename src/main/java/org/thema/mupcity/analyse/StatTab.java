package org.thema.mupcity.analyse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class StatTab {
	public String name;
	public String nameSc;
	private double[] stats;
	private String[] firstCol;

	public StatTab(String nom, String nameScenar, double[] stat, String[] firstColl) {
		name = nom;
		stats = stat;
		firstCol = firstColl;
		nameSc = nameScenar;
	}

	public double[] getStats() {
		System.out.println("je vais retourner llike : " + stats[1]);
		return stats;
	}

	public String[] getFirstCol() {
		System.out.println("je vais retourner llike : " + firstCol[1]);
		return firstCol;
	}

	public void toCsv(File file, boolean firstLine) throws IOException {
		toCsv(file, firstLine, null);
	}

	public void toCsv(File file, boolean firstLine, String tissus) throws IOException {
		File fileName = new File(file + "/" + name + ".csv");
		boolean addAfter = true; //if the new written lines will erase the doc
		FileWriter writer = new FileWriter(fileName, addAfter);
		if (firstLine == true) {
			writer.append("scenario,");
			if (RasterAnalyse.discrete == true) {
				writer.append("tissus,");
			}
			for (String title : firstCol) {
				writer.append(title + ",");
			}
			writer.append("\n");
		}
		writer.append(nameSc + ",");
		if (RasterAnalyse.discrete == true) {
			writer.append(tissus + ",");
		}
		
		for (double val : stats) {
			writer.append(val + ",");
		}
		writer.append("\n");
		writer.close();
	}

}
