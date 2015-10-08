
package org.thema.mupcity.scenario;

import org.thema.mupcity.AHP;
import org.thema.mupcity.Project;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureFilter;
import org.thema.data.feature.WritableFeature;
import org.thema.drawshape.layer.DefaultGroupLayer;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.msca.*;

/**
 * Implementation for handmade scenario.
 * 
 * @author Gilles Vuidel
 */
public class ScenarioManual extends Scenario {
    

    /**
     * Creates a new user made scenario
     * @param name name of the scenario
     * @param ahp the ahp matrix for rule weight
     * @param nMax the max number of cell which can be built between 1 and 9
     * @param msGrid the multiscale grid
     * @param mean true for average aggregation, yager agregation otherwise
     */
    public ScenarioManual(String name, int nMax, AHP ahp, MSGridBuilder msGrid, boolean mean) {
        super(name, ahp, nMax, mean);
        initLayers(msGrid);
    }

    /**
     * Build the cell f
     * @param f the feature representing a cell of the grid
     */
    public void setBlack(MSFeature f) {
        if(!isBlack(f)) {
            int build = ((Number)f.getAttribute(getResultLayerName())).intValue();
            ((WritableFeature)f).setAttribute(getResultLayerName(), build + 2);
        }

    }

    /**
     * Unbuild the cell f
     * @param f the feature representing a cell of the grid
     */
    public void setWhite(MSFeature f) {
        if(isBlack(f)) {
            int build = ((Number)f.getAttribute(getResultLayerName())).intValue();
            ((WritableFeature)f).setAttribute(getResultLayerName(), build - 2);
        }

    }

    /**
     * Retourne vrai si la cellule est construite réellement
     * c à d si il y a du bati à l'intérieur
     * Code 1
     * @param f the feature representing a cell of the grid
     * @return true if the cell is initially built
     */
    public final boolean isBuild(MSFeature f) {
        return ((Number)f.getAttribute(getResultLayerName())).intValue() == 1;
    }

    /**
     * Retourne vrai si la cellule est noire (ou grise)
     * c à d si il y a du bati à l'intérieur ou bien va être construite
     * Code 1 ou 2
     * @param f the feature representing a cell of the grid
     * @return true if the cell is built (initially or in the scenario)
     */
    public final boolean isBlack(MSFeature f) {
        return ((Number)f.getAttribute(getResultLayerName())).intValue() > 0;
    }

    /**
     * Code -1 or 1
     * @param f the feature representing a cell of the grid
     * @return true if the cell is empty or unbuild
     */
    public final boolean isWhite(MSFeature f) {
        return !isBlack(f);
    }
    
    /**
     * Retourne vrai si la cellule n'est pas construite
     * Code 0
     * @param f the feature representing a cell of the grid
     * @return true if the cell is empty
     */
    public final boolean canBeBuild(MSFeature f) {
        return ((Number)f.getAttribute(getResultLayerName())).intValue() == 0;
    }

    
    @Override
    public String getResultLayerName() {
        return getName() + "-" + Project.SCENARIO;
    }

    @Override
    public String getEvalLayerName() {
        return getName() + "-" + Project.SCENARIO + "-" + Project.EVAL;
    }

    @Override
    public String getBuildFreeLayerName() {
        return getName() + "-" + Project.SCENARIO + "-" + Project.MORPHO_RULE;
    }

    @Override
    protected void createLayers(MSGridBuilder<? extends SquareGrid> msGrid) {
        layers = new DefaultGroupLayer(java.util.ResourceBundle.getBundle("org/thema/mupcity/scenario/Bundle").getString("Cell_interest"));
        final String resLayer = getResultLayerName();
        MSGridFeatureCoverage coverages = new MSGridFeatureCoverage(msGrid);
        FeatureLayer l = null;
        for(Double res : msGrid.getResolutions().descendingSet()) {
            GridFeatureCoverage features = coverages.getGridCoverage(res);
            l = new FeatureLayer(String.format("%g", res),
                    features.getFeatures(new FeatureFilter() {
                        @Override
                        public boolean accept(Feature f) {
                            return ((Number)f.getAttribute(Project.ZONE)).intValue() == 1;
                        }
                    }),
                    new ScenarioStyle(resLayer, getEvalLayerName(), 
                    features.getFeatures(new FeatureFilter() {
                        @Override
                        public boolean accept(Feature f) {
                            return ((Number)f.getAttribute(resLayer)).intValue() != 1;
                        }
                    })));
            l.setVisible(false);
            layers.addLayerLast(l);
        }
        l.setVisible(true);
        
    }

}
