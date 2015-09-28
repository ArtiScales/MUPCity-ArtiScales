/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author gvuidel
 */
public abstract class Scenario {
    
    public static final int EMPTY = 0;
    public static final int BUILD = 1;
    public static final int NEW_BUILD = 2;
    public static final int REM_BUILD = -1;
    
    private String name;

    private AHP ahp;
    private int nMax;
    private boolean mean;

    protected transient DefaultGroupLayer layers;

    public Scenario(String name, AHP ahp, int nMax, boolean mean) {
        this.name = name;
        this.ahp = ahp;
        this.nMax = nMax;
        this.mean = mean;
    }
    
    public AHP getAHP() {
        return ahp;
    }

    public int getNMax() {
        return nMax;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public abstract String getResultLayerName();

    public abstract String getEvalLayerName();

    public abstract String getBuildFreeLayerName();

    public DefaultGroupLayer getLayers() {
        if(layers == null) {
            createLayers(Project.getProject().getMSGrid());
        }
        return layers;
    }
    
    protected abstract void createLayers(MSGridBuilder<? extends SquareGrid> msGrid);
    
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
