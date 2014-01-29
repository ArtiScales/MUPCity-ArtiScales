/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.mupcity.evaluation;

import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.thema.mupcity.AHP;
import org.thema.mupcity.Project;
import org.thema.mupcity.scenario.Scenario;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.thema.common.io.IOImage;
import org.thema.drawshape.image.RasterShape;
import org.thema.drawshape.layer.DefaultGroupLayer;
import org.thema.drawshape.layer.RasterLayer;
import org.thema.drawshape.style.RasterStyle;
import org.thema.drawshape.style.table.UniqueColorTable;
import org.thema.msca.MSGridBuilder;
import org.thema.msca.SquareGrid;
import org.thema.msca.SquareGridExtent;

/**
 *
 * @author gvuidel
 */
public class ExternalScenario extends Scenario {


    public ExternalScenario(File rasterFile) throws IOException {
        super(rasterFile.getName(), new AHP(Collections.EMPTY_LIST), 0, false);
        GridCoverage2D cov = IOImage.loadTiff(rasterFile);
        WritableRaster raster = (WritableRaster) cov.getRenderedImage().getData();
        MSGridBuilder<SquareGridExtent> msGrid = Project.getProject().getMSGrid();
        SquareGridExtent grid = msGrid.getGrid(msGrid.getResolutions().last());
        MSGridBuilder.invertRaster(raster);
        grid.addLayer(getResultLayerName(), raster);
    }
    
    
    @Override
    public String getResultLayerName() {
        return "ext-" + getName();
    }

    @Override
    public String getEvalLayerName() {
        return getName() + "-eval";
    }

    @Override
    public String getBuildFreeLayerName() {
        return getName() + "-buildfree";
    }

    @Override
    protected void createLayers(MSGridBuilder<? extends SquareGrid> msGrid) {
        layers = new DefaultGroupLayer(getName());
        SquareGrid grid = msGrid.getGrid(msGrid.getResolutions().last());
        RasterStyle style = new RasterStyle(new UniqueColorTable(Project.colorMap));
        style.setDrawGrid(false);
        RasterLayer l = new RasterLayer(String.format("%s", Project.SIMUL),
            new RasterShape(grid.getRaster(getResultLayerName()),
                org.geotools.geometry.jts.JTS.getEnvelope2D(grid.getEnvelope(),
                    DefaultGeographicCRS.WGS84).getBounds2D()));
        l.setVisible(false);
        l.setCRS(Project.getProject().getCRS());
        l.setStyle(style);

        layers.addLayerLast(l);
    }
    
}
