package org.thema.mupcity.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

import org.thema.mupcity.AHP;
import org.thema.mupcity.Project;
import org.thema.mupcity.scenario.ScenarioAuto;

public class SimulTask {

	public static void main(String[] args) throws Exception {

		File projFile = new File("/home/mickael/data/mbrasebin/donnees/Maxime/1m/data0/data/tmp/Project/");
		File projOut = new File("/home/mickael/temp/");
		String name = "Project";

		int nMax = 5;
		boolean strict = true;
		double ahp0 = 0.111;
		double ahp1 = 0.111;
		double ahp2 = 0.111;
		double ahp3 = 0.111;
		double ahp4 = 0.111;
		double ahp5 = 0.111;
		double ahp6 = 0.111;
		double ahp7 = 0.111;
		double ahp8 = 0.111;

		boolean mean = true;

		String titreScenario = "Titre";
		long seed = 42L;

		run(projFile, projOut, name, nMax, strict, ahp0, ahp1, ahp2, ahp3, ahp4, ahp5, ahp6, ahp7, ahp8, mean,
				titreScenario, seed);

	}

	public static File run(File projFile, File projOut, String name, int nMax, boolean strict, double ahp0, double ahp1,
			double ahp2, double ahp3, double ahp4, double ahp5, double ahp6, double ahp7, double ahp8, boolean mean,
			String titreScenario, long seed) throws Exception {

		return run(projFile, projOut, name, nMax, strict,
				prepareAHP(ahp0, ahp1, ahp2, ahp3, ahp4, ahp5, ahp6, ahp7, ahp8), mean, titreScenario, seed);
	}

	private static AHP prepareAHP(double ahp0, double ahp1, double ahp2, double ahp3, double ahp4, double ahp5,
			double ahp6, double ahp7, double ahp8) {
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
		ahpE_Moy.setCoef(items.get(8), ahp8);
		ahpE_Moy.setCoef(items.get(7), ahp7);
		ahpE_Moy.setCoef(items.get(6), ahp6);
		ahpE_Moy.setCoef(items.get(5), ahp5);
		ahpE_Moy.setCoef(items.get(4), ahp4);
		ahpE_Moy.setCoef(items.get(3), ahp3);
		ahpE_Moy.setCoef(items.get(2), ahp2);
		ahpE_Moy.setCoef(items.get(1), ahp1);
		ahpE_Moy.setCoef(items.get(0), ahp0);

		return ahpE_Moy;
	}

	public static File run(File projFile, File projOut, String name, int nMax, boolean strict, AHP ahp, boolean mean,
			String titreScenario, long seed) throws Exception {

		Project project = Project.load(new File(projFile, "/" + name + ".xml"));

		NavigableSet<Double> res = project.getMSGrid().getResolutions();
		ScenarioAuto scenario = ScenarioAuto.createMultiScaleScenario(titreScenario, res.first(), res.last(), nMax,
				strict, ahp, false, mean, 3, seed, false, false);
		project.performScenarioAuto(scenario);
		// save the project
		scenario.save(projOut, project);
		scenario.extractEvalAnal(projOut, project);
		project.getMSGrid().save(projOut);
		System.out.println("layers : " + project.getMSGrid().getLayers());
		project.getMSGrid().saveRaster(titreScenario + "-eval", projOut);

		return projOut;
	}
}
