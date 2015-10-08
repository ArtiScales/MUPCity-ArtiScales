
package org.thema.mupcity.evaluation;

import org.thema.mupcity.scenario.Scenario;
import org.thema.msca.Cell;
import org.thema.mupcity.Project;

/**
 * Evaluates the minimum distance to a layer of amenities.
 * 
 * @author Gilles Vuidel
 */
public class DistMinAmenEvaluator extends AbstractAmenEvaluator {
    
    /**
     * Creates a new DistMinAmenEvaluator.
     * @param project the current project
     * @param layer the layer of amenities
     * @param level the level of amenity or -1 for all levels
     * @param x the abscisses of the membership function
     * @param y the ordinates of the membership function
     */
    public DistMinAmenEvaluator(Project project, Project.Layers layer, int level, double[] x, double[] y ) {
        super(project, layer, level, x, y);
    }

    @Override
    protected double eval(Scenario scenario, Cell cell) {        
        return getDistAmen().getMinDistance(cell);
    }

    @Override
    public String getShortName() {
        return "DMin_" + getLayer().toString() + "-" + getLevel();
    }

}
