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


package org.thema.mupcity.operation;

import java.util.Map;
import org.thema.msca.Cell;
import org.thema.msca.operation.AbstractOperation;

/**
 * Yager agregate operator.
 * 
 * @author Gilles Vuidel
 */
public class YagerAgregOperation extends AbstractOperation {

    private Map<String, Double> coefLayers;

    /**
     * Creates a new Yager operator with border of 4 cells.
     * @param coefLayers exponent of each layer 
     */
    public YagerAgregOperation(Map<String, Double> coefLayers) {
        super(false, 4);
        this.coefLayers = coefLayers;
    }
    
    @Override
    public double getValue(Cell cell) {
        double min = Double.MAX_VALUE;
        for(String layer : coefLayers.keySet()) {
            min = Math.min(min, Math.pow(cell.getLayerValue(layer), coefLayers.get(layer)));
        }
        return min;
    }
    
}
