package org.thema.mupcity.exp;

import java.io.File;
import java.io.IOException;

import javax.media.jai.RasterAccessor;

import org.geotools.feature.SchemaException;
import org.thema.mupcity.analyse.RasterAnalyse;

public class TotalTests {

	public static void main(String[] args) throws Exception {
		File fileData = new File("/home/mcolomb/informatique/MUP/explo/data/");
		
		//TestStabilite.main(fileData,new File("/media/mcolomb/Data_2/resultTest/sensibility/"));
		//ExplorationTest.main(fileData,new File("/media/mcolomb/Data_2/resultTest/tests_param/results/"));
		MouvData.main(fileData,new File("/media/mcolomb/Data_2/resultTest/mouv_data/3m/"));
		//MouvGrid.main(fileData,new File("/media/mcolomb/Data_2/resultTest/changement_grille/"));
		//EffetSeuil.main(fileData,new File( "/media/mcolomb/Data_2/resultTest/test_180"));
		//RasterAnalyse total = new RasterAnalyse (new File("/media/mcolomb/Data_2/resultTest/"),"20");
	}
}
