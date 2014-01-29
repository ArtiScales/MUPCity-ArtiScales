/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.mupcity.operation;

import java.util.Map;
import org.thema.msca.Cell;
import org.thema.msca.operation.AbstractLayerOperation;
import org.thema.msca.operation.AbstractOperation;

/**
 *
 * @author gvuidel
 */
public class YagerAgregOperation extends AbstractOperation {

    private Map<String, Double> coefLayers;

    public YagerAgregOperation(Map<String, Double> coefLayers) {
        super(false, 4);
        this.coefLayers = coefLayers;
    }
    
    @Override
    public double getValue(Cell cell) {
        double min = Double.MAX_VALUE;
        for(String layer : coefLayers.keySet())
            min = Math.min(min, Math.pow(cell.getLayerValue(layer), coefLayers.get(layer)));
        return min;
    }
    
}
