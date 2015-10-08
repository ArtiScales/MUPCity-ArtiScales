
package org.thema.mupcity.evaluation;

import java.io.IOException;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.common.param.ReflectObject;
import org.thema.graph.SpatialGraph;
import org.thema.mupcity.Project;

/**
 * Base class for evaluator using DistAmenities
 * 
 * @author Gilles Vuidel
 */
public abstract class AbstractAmenEvaluator extends Evaluator {
    
    @ReflectObject.NoParam
    private Project.Layers layer;
    @ReflectObject.NoParam
    private int level;

    private transient DistAmenities distAmen;
    private transient Project project;
    private transient SpatialGraph graph;
    
    /**
     * Creates a new AbstractAmenEvaluator.
     * @param project the current project
     * @param layer the layer of amenities
     * @param level the level of amenity or -1 for all levels
     * @param x the abscisses of the membership function
     * @param y the ordinates of the membership function
     */
    public AbstractAmenEvaluator(Project project, Project.Layers layer, int level, double[] x, double[] y ) {
        super(new DiscreteFunction(x, y));
        this.project = project;
        this.layer = layer;
        this.level = level;
    }

    public Project.Layers getLayer() {
        return layer;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public boolean isUsable() {
        return project.isLayerExist(layer);
    }

    /**
     * Sets the network graph.
     * It will be used only if level == 1, else the project's network will be used
     * @param graph may be null for using the project's network graph
     */
    public void setGraph(SpatialGraph graph) {
        if(level == 1) {
            this.graph = graph;
            distAmen = null;
        }
    }
    
    /**
     * Creates DistAmenities if it does not already exist and return it.
     * @return the distamenities
     */
    protected synchronized DistAmenities getDistAmen() {
        if(distAmen == null) {
            try {
                distAmen = new DistAmenities(project, layer, level, graph == null ? project.getSpatialGraph() : graph);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return distAmen;
    }

}
