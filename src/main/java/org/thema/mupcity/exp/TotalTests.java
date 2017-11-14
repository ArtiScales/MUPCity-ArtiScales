package org.thema.mupcity.exp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.RasterAccessor;

import org.geotools.feature.SchemaException;
import org.thema.mupcity.AHP;
import org.thema.mupcity.analyse.RasterAnalyse;
import org.thema.mupcity.rule.Rule;

public class TotalTests {

	public static void main(String[] args) throws Exception {
		File fileData = new File("/home/mcolomb/informatique/MUP/explo/data/");
		File rootResult = new File("/media/mcolomb/Data_2/resultExplo/");
		//Definiton des parametres
		List<String> items = new ArrayList<>();
		items.add("morpho");
		items.add("road");
		items.add("fac1");
		items.add("fac2");
		items.add("fac3");
		items.add("pt");
		items.add("lei1");
		items.add("lei2");
		items.add("lei3");
		AHP ahpE_Moy = new AHP(items);
		ahpE_Moy.setCoef(items.get(8), 0.111);
		ahpE_Moy.setCoef(items.get(7), 0.111);
		ahpE_Moy.setCoef(items.get(6), 0.111);
		ahpE_Moy.setCoef(items.get(5), 0.111);
		ahpE_Moy.setCoef(items.get(4), 0.111);
		ahpE_Moy.setCoef(items.get(3), 0.111);
		ahpE_Moy.setCoef(items.get(2), 0.111);
		ahpE_Moy.setCoef(items.get(1), 0.111);
		ahpE_Moy.setCoef(items.get(0), 0.111);

		AHP ahpE_Yag = new AHP(items);
		ahpE_Yag.setCoef(items.get(8), 1);
		ahpE_Yag.setCoef(items.get(7), 1);
		ahpE_Yag.setCoef(items.get(6), 1);
		ahpE_Yag.setCoef(items.get(5), 1);
		ahpE_Yag.setCoef(items.get(4), 1);
		ahpE_Yag.setCoef(items.get(3), 1);
		ahpE_Yag.setCoef(items.get(2), 1);
		ahpE_Yag.setCoef(items.get(1), 1);
		ahpE_Yag.setCoef(items.get(0), 1);

		//		int Nmax = 5;
		//		boolean moy=true;
		//		boolean st=false;
		//		AHP ahp = ahpE_Moy;
		//		for (int i = 0; i < 4; i++) {
		//			switch (i) {
		//			case 1:
		//				st = true;
		//				break;
		//			case 2:
		//				Nmax = 6;
		//				st = true;
		//				break;
		//			case 3:
		//				Nmax=5;
		//				moy = false;
		//				st = false;
		//				ahp = ahpE_Yag;
		//				break;
		//			}
		//			Param param = new Param(Nmax, st, moy, ahp, 0, 1000);
		//			File fileStab = new File(rootResult, "Stability");
		//			fileStab.mkdir();
		//			TestStabilite.main(fileData, fileStab, param);
		//		}
		//		
		//		int Nmax = 6;
		//		boolean moy = false;
		//		boolean st = false;
		//		AHP ahp = ahpE_Yag;
		//		Param param = new Param(Nmax, st, moy, ahp, 0, 1000);
		//		File fileStab = new File(rootResult, "Stability");
		//		fileStab.mkdir();

		//		TestStabilite.main(fileData, fileStab, param);

		// File fileGrid = new File(rootResult, "MouvGrid");
		File fileProj = new File(rootResult, "MouvProj");
		File fileMvData = new File(rootResult, "MouvData");
		File file19 = new File(rootResult, "tailleCell-ex");

		int Nmax = 5;
		boolean moy = false;
		boolean st = false;
		//		for (int i = 0; i <= 1; i++) {
		//			if (i == 1) {
		//				st = true;
		//				Nmax = 6;
		//				fileMvData = new File(rootResult, "MouvDataSt");
		//			}
		//			fileMvData .mkdir();
		AHP ahp = ahpE_Yag;
		//					for (int i = 0; i <= 2; i++) {
		//						switch (i) {
		//						case 1:
		//							st = true;
		//							break;
		//						case 2:
		//							Nmax = 6;
		//							st = true;
		//							break;
		//						}

		Param param = new Param(Nmax, st, moy, ahp, 0, 1);
		MouvData.main(fileData, fileMvData, param);
		//OneSim.main(fileData, file19, param);
		//			MouvGrid.main(fileData, fileGrid, param);

		//EffetSeuil.main(fileData,new File( "/media/mcolomb/Data_2/resultTest/test_seuil/St/N6"));
		//RasterAnalyse total = new RasterAnalyse (new File("/media/mcolomb/Data_2/resultTest/"),"20");
		//OneSim.main(fileData,new File("/media/mcolomb/Data_2/resultTest/mairie/"),param);
	}
}
//}
