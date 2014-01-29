/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.mupcity.evaluation;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.common.param.XMLParams;
import org.thema.graph.SpatialGraph;
import org.thema.mupcity.scenario.Scenario;
import org.thema.msca.Cell;
import org.thema.msca.SquareGrid;
import org.thema.mupcity.Project;

/**
 *
 * @author gvuidel
 */
public class DistMinAmenEvaluator extends Evaluator {

    @XMLParams.NoParam
    private transient DistAmenities distAmen;
    
    @XMLParams.NoParam
    private Project project;
    @XMLParams.NoParam
    private Project.Layers layer;
    @XMLParams.NoParam
    private int level;
    @XMLParams.NoParam
    private transient SpatialGraph graph;

    public DistMinAmenEvaluator(Project project, Project.Layers layer, int level) {
        super(new DiscreteFunction(new double[] {0, 10000}, new double[] {0, 1}));
        this.project = project;
        this.layer = layer;
        this.level = level;
    }

    @Override
    protected double eval(Scenario scenario, Cell cell) {        
        return getDistAmen().getMinDistance(cell);
    }
    
    @Override
    public boolean isUsable() {
        return project.isLayerExist(layer);
    }

    public void setGraph(SpatialGraph graph) {
        this.graph = graph;
    }
    
    private synchronized DistAmenities getDistAmen() {
        if(distAmen == null)
            try {
                if(graph == null)
                    distAmen = new DistAmenities(project, layer, level);
                else
                    distAmen = new DistAmenities(project, layer, level, graph);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        return distAmen;
    }

    @Override
    public String getShortName() {
        return "DMin_" + layer.toString() + "-" + level;
    }

//    @Override
//    public Double[] eval(final Scenario anal, final double mean) {
//
//        return grid.agregate(new AbstractAgregateOperation<Double[]>(4) {
//            int nb = 0, nbInf = 0;
//            double sum = 0;
//            public void perform(Cell cell) {
//                if(!isEvaluated(cell, anal))
//                    return;
//                double d = distAmen.getMinDistance(cell);
//                if(Double.isNaN(d))
//                    return;
//                sum += d;
//                nb++;
//                if(isNewBuild(cell, anal) && d < mean)
//                    nbInf++;
//            }
//
//            @Override
//            public Double[] getResult() {
//                return new Double[] {sum / nb, (double)nb, (double)nbInf};
//            }
//
//        });
//    }


}
