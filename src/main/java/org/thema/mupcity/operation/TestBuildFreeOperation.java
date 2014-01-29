/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.mupcity.operation;

import org.thema.msca.operation.NbNbCellOperation;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.DataBuffer;
import org.thema.msca.Cell;
import org.thema.msca.DefaultCell;
import org.thema.msca.SquareGrid;
import org.thema.msca.operation.AcceptableCell;
import org.thema.msca.operation.AbstractLayerOperation;

/**
 *
 * @author gvuidel
 */


public class TestBuildFreeOperation {

private static class CellBuild implements AcceptableCell {
    @Override
    public boolean accept(Cell c) {
        return c.getLayerValue("layer") == 1;
    }
}
private static class CellFree implements AcceptableCell{
    @Override
    public boolean accept(Cell c) {
        return c.getLayerValue("layer") == 0;
    }
}

    public static void main(String [] args) {
        SquareGrid grid = new SquareGrid(new Envelope(0, 5, 0, 5), 1);
        grid.addLayer("layer", DataBuffer.TYPE_BYTE, 0);
        NbNbCellOperation op = new  NbNbCellOperation(new CellBuild(), new CellFree());
        DefaultCell cell;
        for(int i = 0; i < 256; i++) {
            cell = new DefaultCell(6, grid);
            cell.setLayerValue("layer",i & 1);
            cell = new DefaultCell(7, grid);
            cell.setLayerValue("layer",(i >> 1) & 1);
            cell = new DefaultCell(8, grid);
            cell.setLayerValue("layer",(i >> 2) & 1);
            cell = new DefaultCell(11, grid);
            cell.setLayerValue("layer",(i >> 3) & 1);
            cell = new DefaultCell(13, grid);
            cell.setLayerValue("layer",(i >> 4) & 1);
            cell = new DefaultCell(16, grid);
            cell.setLayerValue("layer",(i >> 5) & 1);
            cell = new DefaultCell(17, grid);
            cell.setLayerValue("layer",(i >> 6) & 1);
            cell = new DefaultCell(18, grid);
            cell.setLayerValue("layer",(i >> 7) & 1);
            int value = (int) op.getValue(new DefaultCell(12, grid));
            System.out.println("" + i + " : " + Integer.bitCount(i) + " : " + value);
            
        }
        
    }
}
