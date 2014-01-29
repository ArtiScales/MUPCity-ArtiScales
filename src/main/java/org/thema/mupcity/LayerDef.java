/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.mupcity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.thema.mupcity.Project.Layers;
import org.thema.drawshape.style.FeatureStyle;

/**
 *
 * @author gvuidel
 */
public class LayerDef {
    Layers layer;
    String desc;
    FeatureStyle style;
    List<String> attrNames;
    List<Class> attrClasses;

    public LayerDef(Layers layer, String desc, FeatureStyle style) {
        this.layer = layer;
        this.desc = desc;
        this.style = style;
        this.attrNames = Collections.EMPTY_LIST;
    }
    
    public LayerDef(Layers layer, String desc, FeatureStyle style, String attr, Class type) {
        this(layer, desc, style);
        this.attrNames = Arrays.asList(attr);
        this.attrClasses = Arrays.asList(type);
    }
    
    public LayerDef(Layers layer, String desc, FeatureStyle style, String attr1, Class type1, String attr2, Class type2) {
        this(layer, desc, style);
        this.attrNames = Arrays.asList(attr1, attr2);
        this.attrClasses = Arrays.asList(type1, type2);
    }

    @Override
    public String toString() {
        return desc;
    }
    
    public String getName() {
        return layer.toString();
    }
    
    
}
