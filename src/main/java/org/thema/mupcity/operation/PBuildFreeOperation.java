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

import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.DataBuffer;
import org.thema.msca.Cell;
import org.thema.msca.DefaultCell;
import org.thema.msca.SquareGrid;
import org.thema.msca.operation.AcceptableCell;
import org.thema.msca.operation.NbNbCellOperation;

/**
 * Operation used for calculating the morphological rule.
 * Counts the number of white cells (free) touching a black cell (build) located in the 8 neighbors of the current cell
 * @author Gilles Vuidel
 */
public class PBuildFreeOperation extends NbNbCellOperation {

    /**
     * Creates a new PBuildFreeOperation
     * @param paramLayer the build grid layer name 
     */
    public PBuildFreeOperation(String paramLayer) {
        super(new CellBuild(paramLayer), new CellFree(paramLayer));
    }
    
    @Override
    public final double getValue(Cell c) {
        return super.getValue(c) / 34.0;
    }

    private static class CellBuild implements AcceptableCell {
        private String paramLayer;

        CellBuild(String paramLayer) {
            this.paramLayer = paramLayer;
        }
        @Override
        public final boolean accept(Cell c) {
            return c.getLayerValue(paramLayer) != 0;
        }
    }

    private static class CellFree implements AcceptableCell {
        private String paramLayer;

        CellFree(String paramLayer) {
            this.paramLayer = paramLayer;
        }
        @Override
        public final boolean accept(Cell c) {
            return c.getLayerValue(paramLayer) == 0;
        }
    }

    /**
     * Tests all combinations of black and white neighborhoods to find the max value of the operator : 34
     * 
     * @param args no use
     */
    public static void main(String [] args) {
        SquareGrid grid = new SquareGrid(new Envelope(0, 5, 0, 5), 1);
        grid.addLayer("layer", DataBuffer.TYPE_BYTE, 0);
        NbNbCellOperation op = new  NbNbCellOperation(new CellBuild("layer"), new CellFree("layer"));
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