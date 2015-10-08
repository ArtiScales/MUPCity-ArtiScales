
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
