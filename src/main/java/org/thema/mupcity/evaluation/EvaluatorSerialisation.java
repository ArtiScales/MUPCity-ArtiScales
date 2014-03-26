/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.mupcity.evaluation;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.thema.mupcity.AHP;

/**
 *
 * @author flitot
 */
public class EvaluatorSerialisation {
    
    public List<Evaluator> evaluators;
    public AHP ahp;
    public Map<String, Double> coefEvaluators;

    public EvaluatorSerialisation(List<Evaluator> evaluators, AHP ahp, Map<String, Double> coefEvaluators) {
        this.evaluators = evaluators;
        this.ahp = ahp;
        this.coefEvaluators = coefEvaluators;
    }

    public void save(File rep) {
          try {   

	    // Instanciation de la classe XStream
	    XStream xstream = new XStream(new DomDriver());
	    
	    // Instanciation d'un fichier Evaluators.xml
	    File fichier = new File(rep, "Evaluators.xml");
            
	    // Instanciation d'un flux de sortie fichier vers
	    FileOutputStream fos = new FileOutputStream(fichier);
	    try {
		// Sérialisation de l'objet Evaluator
		xstream.toXML(this, fos);
	    } finally {
		// On s'assure de fermer le flux quoi qu'il arrive
		fos.close();
	    }
 
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
    
    }
}
