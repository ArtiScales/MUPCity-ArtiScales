
package org.thema.mupcity.rule;

import java.awt.image.DataBuffer;
import java.util.Arrays;
import java.util.TreeMap;
import org.thema.mupcity.Project;
import org.thema.mupcity.Project.Layers;
import org.thema.common.fuzzy.DiscreteFunction;
import org.thema.common.param.ReflectObject;
import org.thema.msca.Cell;
import org.thema.msca.operation.AbstractLayerOperation;
import org.thema.msca.operation.SimpleCoverageOperation;

/**
 * Road proximity rule.
 * 
 * @author Gilles Vuidel
 */
public class RoadRule extends AbstractRule {

    private static final String HAS_ROAD = "has_road";

    @ReflectObject.Name("Cell distance functions")
    private TreeMap<Double, DiscreteFunction>  cellDistance;
    
    /**
     * Creates a new road proximity rule with default parameters
     */
    public RoadRule() {
        super(Arrays.asList(Layers.ROAD));
        cellDistance = new TreeMap<>();
        cellDistance.put(500.0, new DiscreteFunction(new double[]{0.0, 1.0}, new double[]{1.0, 0.0}));
        cellDistance.put(200.0, new DiscreteFunction(new double[]{0.0, 1.0, 2.0}, new double[]{1.0, 0.5, 0.0}));
        cellDistance.put(50.0, new DiscreteFunction(new double[]{0.0, 1.0, 2.0, 3.0}, new double[]{1.0, 0.666, 0.333, 0.0}));
        cellDistance.put(20.0, new DiscreteFunction(new double[]{0.0, 1.0, 2.0, 3.0, 4.0}, new double[]{1.0, 0.75, 0.5, 0.25, 0.0}));
        cellDistance.put(0.0, new DiscreteFunction(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0}, new double[]{1.0, 0.8, 0.6, 0.4, 0.2, 0.0}));
    }
    
    @Override
    public final String getName() {
        return "road";
    }

    @Override
    public void createRule(Project project) {
        project.getMSGrid().addLayer(HAS_ROAD, DataBuffer.TYPE_BYTE, 0);
        project.getMSGrid().execute(new SimpleCoverageOperation(SimpleCoverageOperation.ISEMPTY, HAS_ROAD, project.getCoverage(Layers.ROAD)));

        project.getMSGrid().addLayer(getName(), DataBuffer.TYPE_FLOAT, Float.NaN);
        project.getMSGrid().execute(new AbstractLayerOperation(4) {
            @Override
            public void perform(Cell cell) {
                double size = cell.getGeometry().getEnvelopeInternal().getWidth();
                DiscreteFunction distFunction = cellDistance.lowerEntry(size).getValue();
                if(cell.getLayerValue(HAS_ROAD) == 1) {
                    cell.setLayerValue(getName(), distFunction.getValue(0)); 
                    return;
                }
                double maxDist = distFunction.getPoints().lastKey();
                for(int i = 1; i <= maxDist; i++) {
                    for(Cell c : cell.getNeighbors(i)) {
                        if(c.getLayerValue(HAS_ROAD) == 1) {
                            cell.setLayerValue(getName(), distFunction.getValue(i));
                            return;
                        }
                    }
                }
                // si aucune cellule route n'a été trouvée on met l'évaluation à 0
                cell.setLayerValue(getName(), 0); 
            }
        }, true);
    }
    
}
