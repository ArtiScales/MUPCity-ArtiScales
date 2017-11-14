package org.thema.mupcity.exp;

import java.io.File;
import java.io.IOException;

import org.geotools.feature.SchemaException;
import org.thema.mupcity.AHP;

public class Param {
	public String nameSc;
	int Nmax;
	boolean strict;
	boolean mean;
	AHP ponderation;
	float seuil;
	int replications;

	public Param(int N, boolean strict, AHP ponderation) throws IOException, SchemaException {
		new Param(N, strict, false, ponderation, 0,1);
	}

	public Param(int N, boolean st, boolean moy, AHP pond, float se,int repli) throws IOException, SchemaException {
		
		Nmax = N;
		strict = st;
		mean=moy;
		ponderation=pond;
		seuil=se ;		
		replications=repli;
		
		
		//autogenerate the name of the scenario
		String strSt = new String();
		String strMoy = new String();
		String strAhp = new String();
		String strSeuil = new String();
		if (strict == true) {
			strSt = "St";
		} else {
			strSt = "Ba";
		}
		if (moy == true) {
			strMoy = "Moy";
		} else {
			strMoy = "Yag";
		}
		strAhp = ponderation.toString();
		if(seuil!=0.0){
			strSeuil=new String("--seuil_"+seuil);
		}
		nameSc = new String("N" + Nmax + "--" + strSt + "--" +  strAhp + "--" +  strMoy+strSeuil+"--" +repli);
		
		
	}

	public int getN() {
		return Nmax;
	}
	public String getNameSc(){
		return nameSc;
	}
	
	public int getRepli() {
		return replications;
	}
	
	public boolean isStrict() {
		if (strict == true) {
			return true;
		} else {
			return false;
		}
	}
	
public AHP getAhp(){
	return ponderation;
}


public float getSeuilDens(){
	return seuil;
}
	
	public boolean isMean() {
		if (mean == true) {
			return true;
		} else {
			return false;
		}
	}
}

