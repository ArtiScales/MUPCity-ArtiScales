/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.mupcity.scenario;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.List;
import org.thema.drawshape.feature.FeatureShape;
import org.thema.drawshape.layer.StyledLayer;
import org.thema.drawshape.style.FeatureStyle;
import org.thema.drawshape.style.Style;
import org.thema.drawshape.style.table.ColorRamp;
import org.thema.drawshape.style.table.FeatureAttributeIterator;
import org.thema.drawshape.ui.StylePanel;
import org.thema.msca.MSFeature;

/**
 *
 * @author gvuidel
 */
public class ScenarioStyle<T extends FeatureShape> extends FeatureStyle<T> {

    String scenarioAttr;
    String evalAttr;

    public ScenarioStyle(String scenarioAttr, String evalAttr, List<MSFeature> features) {
        super(evalAttr, new ColorRamp(ColorRamp.reverse(ColorRamp.RAMP_SYM_GREEN_RED),
                new FeatureAttributeIterator<Number>(features, evalAttr)));
        this.scenarioAttr = scenarioAttr;
        this.evalAttr = evalAttr;

    }

    @Override
    public void setStyle(Style style) {
        System.err.println("Setstyle in scenariostyle not implemented !!");
    }

    @Override
    public void draw(Graphics2D g, FeatureShape shape, AffineTransform t) {
        MSFeature f = (MSFeature)shape.getFeature();
        Shape s = shape.getJavaShape(t);
        double eval = ((Number)f.getAttribute(evalAttr)).doubleValue();
        int build = ((Number)f.getAttribute(scenarioAttr)).intValue();
        Color c = getRampFill().getColor(eval);
        if(build == -1)
            g.setColor(Color.GRAY.brighter());
        else if(build == 1)
            g.setColor(Color.GRAY);
        else if(build == 2)
            g.setColor(Color.BLACK);
        else if(f.getParent() != null && ((Number)f.getParent().
                getAttribute(scenarioAttr)).intValue() <= 0)
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 80));
        else
            g.setColor(c);
        //g.setColor(Color.getHSBColor(eval*0.333f, 1, build == 1 ? 0 : 0.75f - build*0.125f));
        g.fill(s);
        g.setColor(new Color(128, 128, 128, 50));
        g.setStroke(new BasicStroke(1));
        g.draw(s);

        if(f.getParent() != null && ((Number)f.getParent().
                getAttribute(scenarioAttr)).intValue() == 2) {
            List<MSFeature> lst = f.getParent().getChildren();
            if(lst.get(lst.size()-1) == f) {
                s = new FeatureShape(f.getParent()).getJavaShape(t);
                g.setStroke(new BasicStroke(3));
                g.setColor(new Color(0, 0, 0, 200));
                g.draw(s);
            }
        }
    }

    @Override
    public StylePanel getPanel(StyledLayer layer) {
        return null;
    }

    


}
