/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.mupcity.scenario;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.*;
import javax.media.jai.Histogram;
import javax.media.jai.ROI;
import javax.media.jai.operator.SubtractFromConstDescriptor;
import javax.swing.ProgressMonitor;
import org.thema.mupcity.AHP;
import org.thema.mupcity.Project;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.thema.common.parallel.TaskMonitor;
import org.thema.drawshape.image.RasterShape;
import org.thema.drawshape.layer.DefaultGroupLayer;
import org.thema.drawshape.layer.RasterLayer;
import org.thema.drawshape.style.RasterStyle;
import org.thema.drawshape.style.table.ColorRamp;
import org.thema.drawshape.style.table.UniqueColorTable;
import org.thema.msca.AbstractGrid;
import org.thema.msca.Cell;
import org.thema.msca.DefaultCell;
import org.thema.msca.Grid;
import org.thema.msca.MSCell;
import org.thema.msca.MSGrid;
import org.thema.msca.MSGridBuilder;
import org.thema.msca.operation.AbstractLayerOperation;
import org.thema.msca.*;
import org.thema.msca.operation.*;

/**
 *
 * @author gvuidel
 */
public class ScenarioAuto extends Scenario {


    boolean monoScale;

    double startScale;
    double endScale;
    boolean useNoBuild;

    // multiscale atributes
    boolean strict;

    // monoscale attributes
    int nbCell;

    protected ScenarioAuto(String name, AHP ahp, int nMax, boolean mean) {
        super(name, ahp, nMax, mean);
    }

    public boolean isRandom() {
        return ahp.getCoefs().isEmpty();
    }

    public int getNbNewBuild() {
        return nbCell;
    }

    public boolean isMonoScale() {
        return monoScale;
    }

    public boolean isStrict() {
        return strict;
    }

    /**
     * Retourne vrai si la cellule est construite réellement
     * c à d si il y a du bati à l'intérieur
     * Code -1 ou 1
     * @param c
     * @return
     */
    public final boolean isBuild(Cell c) {
        return Math.abs(c.getLayerValue(getResultLayerName())) == 1;
    }

    /**
     * Retourne vrai si la cellule est noire (ou grise)
     * c à d si il y a du bati à l'intérieur ou bien va être construite
     * Code 1 ou 2
     * @param c
     * @return
     */
    public final boolean isBlack(Cell c) {
        return c.getLayerValue(getResultLayerName()) > 0;
    }

    /**
     * Retourne vrai si la cellule n'est pas construite
     * et peut être construite (contrainte des zones non constructible)
     * Code 0
     * @param c
     * @return
     */
    public final boolean canBeBuild(Cell c) {
        return !isBlack(c) &&
                (!useNoBuild || c.getLayerValue(Project.NOBUILD_DENS)
                    <= (1 - (monoScale ?  0.5 : nMax / Math.pow(Project.getProject().getCoefDecomp(), 2))));
    }

    public void perform(MSGridBuilder msGrid) {
        initLayers(msGrid);
        
        if(monoScale)
            if(isRandom())
                performMonoRandom(msGrid);
            else
                performMonoSimOptim(msGrid);
        else
            performSim(msGrid);
    }

    protected void performSim(MSGridBuilder msGrid) {
         AbstractLayerOperation op = new AbstractLayerOperation(4) {
            final int NBCELL = nMax;

            @Override
            public void perform(Cell cell) {
                String simLayer = getResultLayerName();
                List<MSCell> lstCell = ((MSCell)cell).getChildren();
                if(!isBlack(cell) || lstCell.isEmpty())
                    return;
                byte nb = 0;
                for(MSCell c : lstCell)
                    if(isBlack(c))
                        nb++;

                Collections.shuffle(lstCell);
                TreeMap<Double, List<Cell>> map = new TreeMap<Double, List<Cell>>();
                // on enlève du bati
                if(strict && nb > NBCELL) {
                    for(Cell c : lstCell)
                        if(isBlack(c)) {
                            double v = c.getLayerValue(getEvalLayerName());
                            if(map.containsKey(v))
                                map.get(v).add(c);
                            else {
                                List<Cell> lst = new ArrayList<Cell>();
                                lst.add(c);
                                map.put(v, lst);
                            }
                        }

                    while(nb > NBCELL) {
                        List<Cell> cells = map.pollFirstEntry().getValue();
                        while(nb > NBCELL && !cells.isEmpty()) {
                            cells.get(0).setLayerValue(simLayer, REM_BUILD);
                            cells.remove(0);
                            nb--;
                        }
                    }
                } else // on ajoute du bati
                    for(Cell c : lstCell)
                        if(canBeBuild(c)) {
                            double v = c.getLayerValue(getEvalLayerName());
                            if(map.containsKey(v))
                                map.get(v).add(c);
                            else {
                                List<Cell> lst = new ArrayList<Cell>();
                                lst.add(c);
                                map.put(v, lst);
                            }
                        }

                    while(nb < NBCELL && map.size() > 0) {
                        List<Cell> cells = map.pollLastEntry().getValue();
                        while(nb < NBCELL && !cells.isEmpty()) {
                            cells.get(0).setLayerValue(simLayer, NEW_BUILD);
                            cells.remove(0);
                            nb++;
                        }
                    }
            }
        };

        TaskMonitor monitor = new TaskMonitor(null, "Initialize...", "", 0, 100);
        op.setMonitor(monitor);
        msGrid.execute(op);
        monitor.close();
        nbCell = ((Number)msGrid.agregate(new SimpleAgregateOperation.COUNT(4, new AcceptableCell() {
                @Override
                public boolean accept(Cell c) {
                    return c.getLayerValue(getResultLayerName()) == NEW_BUILD;
                }
            })).firstEntry().getValue()).intValue();
}

    public String getInfo() {
        String info = name;
        if(monoScale)
            info += "\nMono échelle : " + startScale;
        else
            info += "\nMulti échelle\nNmax : " + nMax + "\n" + (strict ? "Strict\n" : "");
        info += "Nb new build cell : " + nbCell;
        info += "\nRègles :\n";
        for(String rule : ahp.getCoefs().keySet())
            info += "- " + rule + " - " + ahp.getCoefs().get(rule) + "\n";
        if(useNoBuild)
            info += "Prends en compte les zones non-constructibles.\n";
        return info;
    }

    private void performMonoRandom(MSGridBuilder msGrid) {

        final String simLayer = getResultLayerName();

        AbstractGrid grid = (AbstractGrid) msGrid.getGrid(startScale);

        ProgressMonitor monitor = new ProgressMonitor(null, "Random MonoScale Analysis", "", 0, nbCell);
        int size = grid.getLayer(simLayer).getSampleModel().getWidth()
                    * grid.getLayer(simLayer).getSampleModel().getHeight();
        int nb = 0;
        while(nb < nbCell) {
            monitor.setNote(nb + "/" + nbCell);
            monitor.setProgress(nb);

            DefaultCell cell = new DefaultCell((int)(Math.random() * size), grid);
            if(canBeBuild(cell) && cell.getDistBorder() >= 4) {
                cell.setLayerValue(simLayer, NEW_BUILD);
                nb++;
            }

        }

        monitor.close();
    }

    private void performMonoSimOptim(MSGridBuilder msGrid) {
        class CellEval implements Comparable<CellEval> {
            int id;
            double eval;

            private CellEval(int id, double eval) {
                this.id = id;
                this.eval = eval;
            }

            public int compareTo(CellEval c) {
                return eval == c.eval ? 0 : (eval < c.eval ? 1 : -1);
            }
                    
        }

        // initialise la couche SIMUL
        final String simLayer = getResultLayerName();
        final String evalLayer = getEvalLayerName();
       
        Grid grid = msGrid.getGrid(startScale);
       
        class BestCellQueue extends AbstractAgregateOperation<PriorityQueue<CellEval>> {
            double min;

            BestCellQueue(int nb, double min) {
                super(4, new PriorityQueue<CellEval>(nb));
                this.min = min;
            }

            @Override
            public final void perform(Cell cell) {
                // cellule déjà batie ?
                if(!canBeBuild(cell))
                    return;
                double eval = cell.getLayerValue(evalLayer);
                if(eval >= min)
                    result.add(new CellEval(cell.getId(), eval));
            }
        }
            
        ProgressMonitor monitor = new ProgressMonitor(null, "MonoScale Analysis", "", 0, nbCell);

        PriorityQueue<CellEval> queue = new PriorityQueue<CellEval>();
        double min = Double.NaN;
        for(int i = 0; i < nbCell; i++) {
            monitor.setNote(i + "/" + nbCell);
            monitor.setProgress(i+1);
            while(queue.isEmpty()) {
                monitor.setNote("Queue empty -> report");
                Raster r = grid.getRaster(evalLayer);

                RenderedImage mask = SubtractFromConstDescriptor.create(grid.getLayer(simLayer).getImage(), new double[] {1}, null);
// solution propre pour gérer nobuild mais plante putain de JAI !!
//                if(useNoBuild)
//                    mask = SubtractDescriptor.create(mask,
//                            BinarizeDescriptor.create(grid.getLayer(Project.NOBUILD).getImage(), 0.5, null), null);
                
                ROI roi = new ROI(mask, 1);
                Histogram histo = new Histogram(101, 0.0, 1.01, 1);
                
                histo.countPixels(r, roi, 0, 0, 1, 1);
                int [] bins = histo.getBins(0);
                int nb = 0;
                int j = 100;
                while(j >= 0 && nb < nbCell)
                    nb += bins[j--];
                // solution à la con pour le nobuild
                min = ((j+1) / 100.0 >= min) ? min-0.1 : ((j+1) / 100.0);
                
                BestCellQueue rOp = new BestCellQueue(nb, min);
                queue = grid.agregate(rOp);
                System.out.println("Queue size : " + queue.size());
            }


            CellEval cEval = queue.poll();
            Cell cell = new DefaultCell(cEval.id, (AbstractGrid)grid);
            if(cell.getLayerValue(evalLayer) == cEval.eval && canBeBuild(cell)) {
                cell.setLayerValue(simLayer, NEW_BUILD);
                //System.out.println(i + " - " + cell.getId() + " : " + cEval.eval);
                // on réinsère les cellules modifiées
                List<Cell> cells = cell.getNeighbors(2);
                for(Cell c : cells)
                    if(canBeBuild(c) && c.getDistBorder() >= 4) {
                        double eval = c.getLayerValue(evalLayer);
                        if(eval >= min)
                            queue.add(new CellEval(c.getId(), eval));
                    }
            } else
                i--;
        }
        monitor.close();
    }

    @Override
    protected void createLayers(MSGridBuilder<? extends SquareGrid> msGrid) {
        layers = new DefaultGroupLayer(name);

        if(monoScale) {
            MSGrid grid = msGrid.getGrid(startScale);
            RasterStyle style = new RasterStyle(new UniqueColorTable(Project.colorMap));
            style.setDrawGrid(false);
            RasterLayer l = new RasterLayer(String.format("%s-%g", Project.SIMUL, startScale),
                new RasterShape(grid.getRaster(getResultLayerName()),
                    org.geotools.geometry.jts.JTS.getEnvelope2D(grid.getEnvelope(),
                        DefaultGeographicCRS.WGS84).getBounds2D()));
            l.setVisible(false);
            l.setCRS(Project.getProject().getCRS());
            l.setStyle(style);

            layers.addLayerLast(l);
            if(!isRandom()) {
                l = new RasterLayer(String.format("%s-%g", Project.EVAL, startScale),
                    new RasterShape(grid.getRaster(getEvalLayerName()),
                        org.geotools.geometry.jts.JTS.getEnvelope2D(grid.getEnvelope(),
                            DefaultGeographicCRS.WGS84).getBounds2D()));
                l.setVisible(false);
                l.setStyle(new RasterStyle(ColorRamp.RAMP_INVGRAY));
                l.setCRS(Project.getProject().getCRS());
                layers.addLayerLast(l);
            }
        } else {
            layers.addLayerFirst(Project.getProject().createLayer(getResultLayerName(), new UniqueColorTable(Project.colorMap),
                    name + "-" + java.util.ResourceBundle.getBundle("mupcity/Bundle").getString("scenario")));
            layers.addLayerLast(Project.getProject().createLayer(getEvalLayerName(), null,  name + "-" + java.util.ResourceBundle.getBundle("mupcity/Bundle").getString("interest")));
        }

    }

    public final String getResultLayerName() {
        return name + "-" + Project.SIMUL;
    }

    public final String getEvalLayerName() {
        return name + "-" + Project.EVAL;
    }

    public final String getBuildFreeLayerName() {
        return name + "-" + Project.MORPHO_RULE;
    }


    public static ScenarioAuto createMonoScaleAnalysis(String name, double scale,
            int nbCell, AHP ahp, boolean useNoBuild, boolean mean) {
        ScenarioAuto anal = new ScenarioAuto(name, ahp, 0, mean);

        anal.monoScale = true;
        anal.startScale = anal.endScale = scale;
        anal.nbCell = nbCell;
        anal.useNoBuild = useNoBuild;

        return anal;
    }

    public static ScenarioAuto createMultiScaleAnalysis(String name,
            double startScale, double endScale, int nMax, boolean strict, 
            AHP ahp, boolean useNoBuild, boolean mean) {
        ScenarioAuto anal = new ScenarioAuto(name, ahp, nMax, mean);

        anal.monoScale = false;
        anal.startScale = startScale;
        anal.endScale = endScale;
        anal.strict = strict;
        anal.useNoBuild = useNoBuild;
        anal.nbCell = 0;

        return anal;
    }

   
}
