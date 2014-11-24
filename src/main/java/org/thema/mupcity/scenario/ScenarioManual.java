
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
 *
 * @author Gilles Vuidel
 */
public class ScenarioManual extends Scenario {
    

    public ScenarioManual(String name, int nMax, AHP ahp, MSGridBuilder msGrid, boolean mean) {
        super(name, ahp, nMax, mean);
        initLayers(msGrid);
    }


    public void setBlack(MSFeature f) {
        if(!isBlack(f)) {
            int build = ((Number)f.getAttribute(getResultLayerName())).intValue();
            ((WritableFeature)f).setAttribute(getResultLayerName(), build + 2);
        }

    }

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
     * @param f
     * @return
     */
    public final boolean isBuild(MSFeature f) {
        return ((Number)f.getAttribute(getResultLayerName())).intValue() == 1;
    }

    /**
     * Retourne vrai si la cellule est noire (ou grise)
     * c à d si il y a du bati à l'intérieur ou bien va être construite
     * Code 1 ou 2
     * @param f
     * @return
     */
    public final boolean isBlack(MSFeature f) {
        return ((Number)f.getAttribute(getResultLayerName())).intValue() > 0;
    }

    public final boolean isWhite(MSFeature f) {
        return !isBlack(f);
    }
    
    /**
     * Retourne vrai si la cellule n'est pas construite
     * Code 0
     * @param f
     * @return
     */
    public final boolean canBeBuild(MSFeature f) {
        return ((Number)f.getAttribute(getResultLayerName())).intValue() == 0;
    }

    
    @Override
    public String getResultLayerName() {
        return name + "-" + Project.SCENARIO;
    }

    @Override
    public String getEvalLayerName() {
        return name + "-" + Project.SCENARIO + "-" + Project.EVAL;
    }

    @Override
    public String getBuildFreeLayerName() {
        return name + "-" + Project.SCENARIO + "-" + Project.MORPHO_RULE;
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
