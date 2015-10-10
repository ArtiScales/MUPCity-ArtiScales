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


package org.thema.mupcity.scenario;

import java.awt.image.DataBuffer;
import java.util.HashMap;
import java.util.Map;
import org.thema.mupcity.AHP;
import org.thema.mupcity.Project;
import org.thema.mupcity.operation.YagerAgregOperation;
import org.thema.mupcity.operation.PBuildFreeOperation;
import org.thema.drawshape.layer.DefaultGroupLayer;
import org.thema.msca.Cell;
import org.thema.msca.MSGridBuilder;
import org.thema.msca.SquareGrid;
import org.thema.msca.operation.AbstractLayerOperation;
import org.thema.msca.operation.AbstractOperation;
import org.thema.msca.operation.MeanOperation;

/**
 * Base class for scenario.
 * 
 * @author Gilles Vuidel
 */
public abstract class Scenario {
    
    /** the cell is empty (ie. non built)  */
    public static final int EMPTY = 0;
    /** the cell is initially built */
    public static final int BUILD = 1;
    /** the cell is build in the scenario */
    public static final int NEW_BUILD = 2;
    /** the cell is initially built and has been unbuilt */
    public static final int REM_BUILD = -1;
    
    private String name;

    private AHP ahp;
    private int nMax;
    private boolean mean;

    protected transient DefaultGroupLayer layers;

    /**
     * Creates a new scenario
     * @param name name of the new scenario
     * @param ahp the AHP matrix for weights
     * @param nMax the maximum number of cells which can be built between 1 and 9
     * @param mean true for average aggregation, false for yager
     */
    public Scenario(String name, AHP ahp, int nMax, boolean mean) {
        this.name = name;
        this.ahp = ahp;
        this.nMax = nMax;
        this.mean = mean;
    }
    
    /**
     * @return the AHP matrix
     */
    public AHP getAHP() {
        return ahp;
    }

    /**
     * @return the maximum number of cells which can be built between 1 and 9
     */
    public int getNMax() {
        return nMax;
    }

    /**
     * @return  the name of the scenario
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return the name of the scenario
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * @return the name of the layer in the grid containing the result of the scenario
     */
    public abstract String getResultLayerName();

    /**
     * @return the name of the layer in the grid containing the evaluation of the scenario
     */
    public abstract String getEvalLayerName();

    /**
     * Warning, the layer in the grid does not necessarily exist
     * @return the name of the layer in the grid containing the morpho rule of the scenario
     */
    public abstract String getBuildFreeLayerName();

    /**
     * Creates the group layers if it has not been created yet
     * @return the group of layers for viewing the scenario layers
     */
    public synchronized DefaultGroupLayer getLayers(MSGridBuilder<? extends SquareGrid> msGrid) {
        if(layers == null) {
            createLayers(msGrid);
        }
        return layers;
    }
    
    /**
     * Creates the group layers of the scenario.
     * This method is called by {@link #getLayers() }
     * @param msGrid the multiscale grid
     */
    protected abstract void createLayers(MSGridBuilder<? extends SquareGrid> msGrid);
    
    /**
     * Creates the layers for the scenario in the MSGrid.
     * The result layer, the evaluation layer and the morpho rule if needed.
     * @param msGrid the multi scale grid
     */
    protected void initLayers(MSGridBuilder msGrid) {
        boolean useBuildRule = false;
        Map<String, Double> coefLayers = new HashMap<>();
        for(String rule : ahp.getCoefs().keySet()) { 
            if(rule.equals(Project.MORPHO_RULE)) {
                coefLayers.put(getBuildFreeLayerName(), ahp.getCoefs().get(rule));
                useBuildRule = true;
            } else {
                coefLayers.put(rule, ahp.getCoefs().get(rule));
            }
        }

        // initialise la couche SIMUL avec le bati existant
        final String simLayer = getResultLayerName();

        msGrid.addLayer(simLayer, DataBuffer.TYPE_SHORT, 0);
        msGrid.execute(new AbstractLayerOperation() {
            @Override
            public void perform(Cell cell) {
                cell.setLayerValue(simLayer, cell.getLayerValue(Project.BUILD));
            }
        });
        if(useBuildRule) {
            msGrid.addDynamicLayer(getBuildFreeLayerName(),
                    new PBuildFreeOperation(simLayer));
        }

        if(!coefLayers.isEmpty()) {
            msGrid.addDynamicLayer(getEvalLayerName(), mean ? new MeanOperation(coefLayers, 4, false) : new YagerAgregOperation(coefLayers));
        } else {
            msGrid.addDynamicLayer(getEvalLayerName(), new AbstractOperation(true, 4) {
                @Override
                public final double getValue(Cell cell) { 
                    return 0; 
                }
            });
        }

    }
}
