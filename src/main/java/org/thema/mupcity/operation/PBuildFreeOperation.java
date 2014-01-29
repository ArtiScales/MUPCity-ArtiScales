/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.mupcity.operation;

import org.thema.msca.Cell;
import org.thema.msca.operation.AcceptableCell;
import org.thema.msca.operation.NbNbCellOperation;

/**
 *
 * @author gvuidel
 */

class CellBuild implements AcceptableCell {
    String paramLayer;

    CellBuild(String paramLayer) {
        this.paramLayer = paramLayer;
    }
    @Override
    public final boolean accept(Cell c) {
        return c.getLayerValue(paramLayer) != 0;
    }
}

class CellFree implements AcceptableCell{
    String paramLayer;

    CellFree(String paramLayer) {
        this.paramLayer = paramLayer;
    }
    @Override
    public final boolean accept(Cell c) {
        return c.getLayerValue(paramLayer) == 0;
    }
}

public class PBuildFreeOperation extends NbNbCellOperation {

    public PBuildFreeOperation(String paramLayer) {
        super(new CellBuild(paramLayer), new CellFree(paramLayer));
    }
    
    @Override
    public final double getValue(Cell c) {
        return super.getValue(c) / 34.0;
    }

}