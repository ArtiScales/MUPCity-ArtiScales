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


package org.thema.mupcity;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.thema.common.collection.HashMap2D;

/**
 * AHP matrix.
 * Matrix comparison of items for determining weights by calculating first eigenvector of the matrix.
 * @author Gilles Vuidel
 */
public class AHP {
    
    private HashMap2D<String, String, String> matrix;
    private Map<String, Double> coefs;

    /**
     * Creates a new AHP matrix for the list of items
     * @param items the list of items to compare
     */
    public AHP(List<String> items) {
        matrix = new HashMap2D<>(items, items, "1");
    }
    
    
    /**
     * @return the matrix comparison
     */
    public HashMap2D<String, String, String> getMatrix() {
        return matrix;
    }

    /**
     * Sets the matrix comparison
     * @param matrix the matrix
     */
    public void setMatrix(HashMap2D<String, String, String> matrix) {
        this.matrix = matrix;
    }
    public void setCoef(String var, double coef) {
        if(!getCoefs().containsKey(var)) {
            throw new IllegalArgumentException(var + "is unknown");
        }
        coefs.put(var, coef);
    }
    
    /**
     * Calculates the first eigenvector of the comparison matrix
     * @return the coefficient of each item
     */
    public Map<String, Double> getCoefs() {
        if(coefs != null) {
            return coefs;
        }
        
        int size = matrix.getKeys1().size();
        if(size == 0) {
            return Collections.EMPTY_MAP;
        }
        
        double[][] mat = new double[size][size];
        List<String> items = new ArrayList<>(matrix.getKeys1());
        int i = 0;
        for(String s1 : items) {
            int j = 0;
            for(String s2 : items) {
                String s = matrix.getValue(s1, s2);
                double val;
                if(s.startsWith("1/")) {
                    val = 1.0 / Integer.parseInt(s.substring(2));
                } else {
                    val = Integer.parseInt(s);
                }
                mat[i][j++] = val;
            }
            i++;
        }
        
        EigenvalueDecomposition eig = new Matrix(mat).eig();
        // détermine la valeur propre max
        double[] eigValues = eig.getRealEigenvalues();
        int bestInd = -1;
        double maxEigValue = -Double.MAX_VALUE;
        for(i = 0; i < eigValues.length; i++) {
            if(eigValues[i] > maxEigValue) {
                bestInd = i;
                maxEigValue = eigValues[i];
            }
        }
                
        double [] vector = eig.getV().getMatrix(0, size-1, bestInd, bestInd).getColumnPackedCopy();
        double sum = 0;
        for(i = 0; i < vector.length; i++) {
            sum += vector[i];
        }
        double [] finalVector = new double[size];
        for(i = 0; i < size; i++) {
            finalVector[i] = size * vector[i] / sum;
            if(finalVector[i] < 0) {
                Logger.getLogger(AHP.class.getName()).log(Level.WARNING, "Vecteur nÃ©gatif !!"
                        + "\nMatrice : \n" + new Matrix(mat)
                        + "\nVector : \n" + Arrays.toString(vector)
                        + "\nValues" + Arrays.toString(new Matrix(mat).eig().getRealEigenvalues()));
            }
        }
        
        coefs = new HashMap<>();
        for(i = 0; i < size; i++) {
            coefs.put(items.get(i), finalVector[i]);
            System.out.println("Added in AHP : " + items.get(i) + "  " + finalVector[i]);
        }
        return coefs;        
    }
}
