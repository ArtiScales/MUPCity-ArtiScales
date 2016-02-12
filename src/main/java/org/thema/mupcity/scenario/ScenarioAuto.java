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

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.*;
import javax.media.jai.Histogram;
import javax.media.jai.ROI;
import javax.media.jai.operator.SubtractFromConstDescriptor;
import javax.swing.ProgressMonitor;
import org.thema.mupcity.AHP;
import org.thema.mupcity.Project;
import org.thema.common.swing.TaskMonitor;
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
 * Base implementation for automatic scenario.
 * 
 * @author Gilles Vuidel
 */
public class ScenarioAuto extends Scenario {

    private boolean monoScale;

    private double startScale;
    private double endScale;
    private boolean useNoBuild;

    // multiscale atributes
    private boolean strict;
    private int coefDecomp;

    // monoscale attributes
    private int nbCell;

    // random number generator for the shuffling (see perform)
    private Random rnd;
    /**
     * Creates a new scenario.
     * 
     * @param name name of the scenario
     * @param ahp the ahp matrix for rule weight
     * @param nMax the max number of cell which can be built between 1 and 9
     * @param mean true for average aggregation, yager agregation otherwise
     */
    private ScenarioAuto(String name, AHP ahp, int nMax, boolean mean, long seed) {
        super(name, ahp, nMax, mean);
        this.rnd = new Random(seed);
    }

    /**
     * @return true if this scenario has no rule evaluation and so is random
     */
    public boolean isRandom() {
        return getAHP().getCoefs().isEmpty();
    }

    /**
     * @return the number of newly built cells
     */
    public int getNbNewBuild() {
        return nbCell;
    }

    /**
     * @return true if the scenario is not multiscale
     */
    public boolean isMonoScale() {
        return monoScale;
    }

    /**
     * @return true if the scenario respect stricty the number of cells (nmax) and so unbuild cells
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * Retourne vrai si la cellule est construite réellement
     * c à d si il y a du bati à l'intérieur
     * Code -1 ou 1
     * @param c the cell
     * @return true if the cell is built initially
     */
    public final boolean isBuild(Cell c) {
        return Math.abs(c.getLayerValue(getResultLayerName())) == 1;
    }

    /**
     * Retourne vrai si la cellule est noire (ou grise)
     * c à d si il y a du bati à l'intérieur ou bien va être construite
     * Code 1 ou 2
     * @param c the cell
     * @return true if the cell is built (initially or newly)
     */
    public final boolean isBlack(Cell c) {
        return c.getLayerValue(getResultLayerName()) > 0;
    }

    public final boolean isRemoved(Cell c) {
        return c.getLayerValue(getResultLayerName()) == -1;
    }
    
    /**
     * Retourne vrai si la cellule n'est pas construite
     * et peut être construite (contrainte des zones non constructible)
     * Code 0
     * @param c the cell
     * @return true if the cell is not built initially and does not intersect "too much" the no build area 
     */
    public final boolean canBeBuild(Cell c) {
        return !isBlack(c) &&
                (!useNoBuild || c.getLayerValue(Project.NOBUILD_DENS)
                    <= (1 - (monoScale ?  0.5 : getNMax() / Math.pow(coefDecomp, 2))));
    }

    /**
     * Computes the scenario result.
     * @param msGrid the multi scale grid
     */
    public void perform(MSGridBuilder msGrid) {
        initLayers(msGrid);
        
        if(monoScale) {
            if(isRandom()) {
                performMonoRandom(msGrid);
            } else {
                performMonoSimOptim(msGrid);
            }
        } else {
            performSim(msGrid);
        }
    }

    /**
     * Computes the scenario for multiple scales.
     * @param msGrid the multi scale grid
     */
    private void performSim(MSGridBuilder msGrid) {
         AbstractLayerOperation op = new AbstractLayerOperation(4) {
            final int NBCELL = getNMax();

            @Override
            public void perform(Cell cell) {
                String simLayer = getResultLayerName();
                List<MSCell> lstCell = ((MSCell)cell).getChildren();
                if(isRemoved(cell)) {
                   for(MSCell c : lstCell) {
                       if(isBuild(c)) {
                           c.setLayerValue(simLayer, REM_BUILD);
                       }
                    } 
                   return;
                }
                
                if(!isBlack(cell) || lstCell.isEmpty()) {
                    return;
                }
                
                byte nb = 0;
                for(MSCell c : lstCell) {
                    if(isBlack(c)) {
                        nb++;
                    }
                }

                Collections.shuffle(lstCell, rnd);
                TreeMap<Double, List<Cell>> map = new TreeMap<>();
                // on enlève du bati
                if(strict && nb > NBCELL) {
                    for(Cell c : lstCell) {
                        if(isBlack(c)) {
                            double v = c.getLayerValue(getEvalLayerName());
                            if(map.containsKey(v)) {
                                map.get(v).add(c);
                            } else {
                                List<Cell> lst = new ArrayList<>();
                                lst.add(c);
                                map.put(v, lst);
                            }
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
                } else {
                    // on ajoute du bati
                    for(Cell c : lstCell) {
                        if(canBeBuild(c)) {
                            double v = c.getLayerValue(getEvalLayerName());
                            if(map.containsKey(v)) {
                                map.get(v).add(c);
                            } else {
                                List<Cell> lst = new ArrayList<>();
                                lst.add(c);
                                map.put(v, lst);
                            }
                        }
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
        // TODO pass the random generatoor to the execute method
        msGrid.execute(op);
        monitor.close();
        nbCell = ((Number)msGrid.agregate(new SimpleAgregateOperation.COUNT(4, new AcceptableCell() {
                @Override
                public boolean accept(Cell c) {
                    return c.getLayerValue(getResultLayerName()) == NEW_BUILD;
                }
            })).firstEntry().getValue()).intValue();
    }

    /**
     * @return informations of this scenario
     */
    public String getInfo() {
        String info = getName();
        if(monoScale) {
            info += "\nMono échelle : " + startScale;
        } else {
            info += "\nMulti échelle\nNmax : " + getNMax() + "\n" + (strict ? "Strict\n" : "");
        }
        info += "Nb new build cell : " + nbCell;
        info += "\nRègles :\n";
        for(String rule : getAHP().getCoefs().keySet()) {
            info += "- " + rule + " - " + getAHP().getCoefs().get(rule) + "\n";
        }
        if(useNoBuild) {
            info += "Prends en compte les zones non-constructibles.\n";
        }
        return info;
    }

    /**
     * Computes the scenario for mono scale without rules (ie. random scenario)
     * @param msGrid the multi scale grid
     */
    private void performMonoRandom(MSGridBuilder msGrid) {

        final String simLayer = getResultLayerName();

        AbstractGrid grid = (AbstractGrid) msGrid.getGrid(startScale);

        ProgressMonitor monitor = new ProgressMonitor(null, "Random MonoScale Scenario", "", 0, nbCell);
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

    /**
     * Computes the scenario for mono scale
     * @param msGrid the multi scale grid
     */
    private void performMonoSimOptim(MSGridBuilder msGrid) {
        class CellEval implements Comparable<CellEval> {
            int id;
            double eval;

            private CellEval(int id, double eval) {
                this.id = id;
                this.eval = eval;
            }

            @Override
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
                if(!canBeBuild(cell)) {
                    return;
                }
                double eval = cell.getLayerValue(evalLayer);
                if(eval >= min) {
                    result.add(new CellEval(cell.getId(), eval));
                }
            }
        }
            
        ProgressMonitor monitor = new ProgressMonitor(null, "MonoScale Scenario", "", 0, nbCell);

        PriorityQueue<CellEval> queue = new PriorityQueue<>();
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
                while(j >= 0 && nb < nbCell) {
                    nb += bins[j--];
                }
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
                for(Cell c : cells) {
                    if(canBeBuild(c) && c.getDistBorder() >= 4) {
                        double eval = c.getLayerValue(evalLayer);
                        if(eval >= min) {
                            queue.add(new CellEval(c.getId(), eval));
                        }
                    }
                }
            } else {
                i--;
            }
        }
        monitor.close();
    }

    @Override
    protected void createLayers(MSGridBuilder<? extends SquareGrid> msGrid) {
        layers = new DefaultGroupLayer(getName());

        if(monoScale) {
            MSGrid grid = msGrid.getGrid(startScale);
            RasterStyle style = new RasterStyle(new UniqueColorTable(Project.COLOR_MAP));
            style.setDrawGrid(false);
            RasterLayer l = new RasterLayer(String.format("%s-%g", Project.SIMUL, startScale),
                new RasterShape(grid.getRaster(getResultLayerName()),
                    org.geotools.geometry.jts.JTS.getEnvelope2D(grid.getEnvelope(),
                        msGrid.getCrs()).getBounds2D()));
            l.setVisible(false);
            l.setCRS(msGrid.getCrs());
            l.setStyle(style);

            layers.addLayerLast(l);
            if(!isRandom()) {
                l = new RasterLayer(String.format("%s-%g", Project.EVAL, startScale),
                    new RasterShape(grid.getRaster(getEvalLayerName()),
                        org.geotools.geometry.jts.JTS.getEnvelope2D(grid.getEnvelope(),
                            msGrid.getCrs()).getBounds2D()));
                l.setVisible(false);
                l.setStyle(new RasterStyle(ColorRamp.RAMP_INVGRAY));
                l.setCRS(msGrid.getCrs());
                layers.addLayerLast(l);
            }
        } else {
            layers.addLayerFirst(Project.createMultiscaleLayers(getResultLayerName(), new UniqueColorTable(Project.COLOR_MAP),
                    getName() + "-" + java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("scenario"), msGrid));
            layers.addLayerLast(Project.createMultiscaleLayers(getEvalLayerName(), null,  
                    getName() + "-" + java.util.ResourceBundle.getBundle("org/thema/mupcity/Bundle").getString("interest"), msGrid));
        }

    }

    @Override
    public final String getResultLayerName() {
        return getName() + "-" + Project.SIMUL;
    }

    @Override
    public final String getEvalLayerName() {
        return getName() + "-" + Project.EVAL;
    }

    @Override
    public final String getBuildFreeLayerName() {
        return getName() + "-" + Project.MORPHO_RULE;
    }


    /**
     * Creates a new monoscale scenario.
     * The method {@link #perform} must be called after to compute the result
     * @param name the name of the scenario
     * @param scale the scale (cell size)
     * @param nbCell the number cell to build
     * @param ahp the ahp matrix 
     * @param useNoBuild use no build restriction layer ?
     * @param mean if true use average agregation, yager otherwise
     * @return the new scenario
     */
    public static ScenarioAuto createMonoScaleScenario(String name, double scale,
            int nbCell, AHP ahp, boolean useNoBuild, boolean mean, long seed) {
        ScenarioAuto scenario = new ScenarioAuto(name, ahp, 0, mean, seed);

        scenario.monoScale = true;
        scenario.startScale = scenario.endScale = scale;
        scenario.nbCell = nbCell;
        scenario.useNoBuild = useNoBuild;

        return scenario;
    }

    /**
     * Creates a new multiscale scenario.
     * The method {@link #perform} must be called after to compute the result
     * @param name the name of the scenario
     * @param startScale the first scale 
     * @param endScale the last scale
     * @param nMax the max number of cell which can be built between 1 and 9
     * @param strict true if the scenario must respect stricty the number of cells (nmax) and so unbuild some cells
     * @param ahp the ahp matrix 
     * @param useNoBuild use no build restriction layer ?
     * @param mean if true use average agregation, yager otherwise
     * @param coefDecomp factor between scale of the multiscale grid decomposition
     * @return the new scenario
     */
    public static ScenarioAuto createMultiScaleScenario(String name,
            double startScale, double endScale, int nMax, boolean strict, 
            AHP ahp, boolean useNoBuild, boolean mean, int coefDecomp, long seed) {
        ScenarioAuto scenario = new ScenarioAuto(name, ahp, nMax, mean, seed);

        scenario.monoScale = false;
        scenario.startScale = startScale;
        scenario.endScale = endScale;
        scenario.strict = strict;
        scenario.useNoBuild = useNoBuild;
        scenario.nbCell = 0;
        scenario.coefDecomp = coefDecomp;

        return scenario;
    }

   
}
