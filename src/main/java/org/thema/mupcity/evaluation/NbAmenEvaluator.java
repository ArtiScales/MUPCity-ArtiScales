
package org.thema.mupcity.evaluation;

import org.thema.msca.Cell;
import org.thema.mupcity.Project;
import org.thema.mupcity.scenario.Scenario;

/**
 * Evaluates the number of amenities inside a given distance 
 * 
 * @author Gilles Vuidel
 */
public class NbAmenEvaluator extends AbstractAmenEvaluator {
    
    private double distMax;

    /**
     * Creates a new NbAmenEvaluator.
     * @param project the current project
     * @param layer the layer of amenities
     * @param level the level of amenity or -1 for all levels
     * @param distMax the maximum distance
     * @param x the abscisses of the membership function
     * @param y the ordinates of the membership function
     */
    public NbAmenEvaluator(Project project, Project.Layers layer, int level, double distMax, double[] x, double[] y ) {
        super(project, layer, level, x, y);
        this.distMax = distMax;
    }

    @Override
    protected double eval(Scenario scenario, Cell cell) {
        return getDistAmen().getNbAmen(cell, distMax);
    }

    @Override
    public String getShortName() {
        return "NB_" + getLayer().toString() + "-" + getLevel();
    }

}
