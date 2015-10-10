/*
 * Copyright (C) 2015 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 * http://thema.univ-fcomte.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.thema.mupcity.evaluation;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.thema.mupcity.AHP;

/**
 * Class used for saving evaluation parameters in a xml file.
 * 
 * @author Florian Litot, Gilles Vuidel
 */
public class EvaluatorSerialization {
    
    private List<Evaluator> evaluators;
    private AHP ahp;
    private Map<String, Double> coefEvaluators;

    /**
     * Creates a new EvaluatorSerialization for storing evaluation parameters.
     * @param evaluators the used evaluators
     * @param ahp the ahp matrix for weights
     * @param coefEvaluators the evaluator weights
     */
    public EvaluatorSerialization(List<Evaluator> evaluators, AHP ahp, Map<String, Double> coefEvaluators) {
        this.evaluators = evaluators;
        this.ahp = ahp;
        this.coefEvaluators = coefEvaluators;
    }

    /**
     * Saves the evaluation parameters to an xml file
     * @param rep the directory where the file will be written
     * @throws IOException 
     */
    public void save(File rep) throws IOException {
        // Instanciation de la classe XStream
        XStream xstream = new XStream(new DomDriver());
        // Instanciation d'un fichier Evaluators.xml
        File fichier = new File(rep, "evaluators.xml");
        try (FileOutputStream fos = new FileOutputStream(fichier)) {
            // Sérialisation de l'objet Evaluator
            xstream.toXML(this, fos);
        }
    }
}
