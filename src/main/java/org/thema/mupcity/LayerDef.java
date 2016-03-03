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


package org.thema.mupcity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.thema.mupcity.Project.Layers;
import org.thema.drawshape.style.FeatureStyle;

/**
 * Layer definition for predefined layers.
 * 
 * @author Gilles Vuidel
 */
public class LayerDef {
    private Layers layer;
    private String desc;
    private FeatureStyle style;
    private List<String> attrNames;
    private List<Class> attrClasses;

    /**
     * Creates a new LayerDef.
     * @param layer the predefined layer
     * @param desc the localized name
     * @param style the layer style
     */
    public LayerDef(Layers layer, String desc, FeatureStyle style) {
        this.layer = layer;
        this.desc = desc;
        this.style = style;
        this.attrNames = Collections.EMPTY_LIST;
    }
    
    /**
     * Creates a new LayerDef.
     * @param layer the predefined layer
     * @param desc the localized name
     * @param style the layer style
     * @param attr the required attribute
     * @param type the type of the required attribute
     */
    public LayerDef(Layers layer, String desc, FeatureStyle style, String attr, Class type) {
        this(layer, desc, style);
        this.attrNames = Arrays.asList(attr);
        this.attrClasses = Arrays.asList(type);
    }
    
    /**
     * Creates a new LayerDef.
     * @param layer the predefined layer
     * @param desc the localized name
     * @param style the layer style
     * @param attr1 the first required attribute
     * @param type1 the type of the first required attribute
     * @param attr2 the second required attribute
     * @param type2 the type of the second required attribute
     */
    public LayerDef(Layers layer, String desc, FeatureStyle style, String attr1, Class type1, String attr2, Class type2) {
        this(layer, desc, style);
        this.attrNames = Arrays.asList(attr1, attr2);
        this.attrClasses = Arrays.asList(type1, type2);
    }

    /**
     * @return the localized name
     */
    @Override
    public String toString() {
        return desc;
    }
    
    /**
     * @return the internal name of the predefined layer
     */
    public String getName() {
        return layer.toString();
    }

    /**
     * @return the predefined layer
     */
    public Layers getLayer() {
        return layer;
    }

    /**
     * @return the required attributes name
     */
    public List<String> getAttrNames() {
        return attrNames;
    }

    /**
     * @return the types of all required attributes name
     */
    public List<Class> getAttrClasses() {
        return attrClasses;
    }

    /**
     * @return the localized name of this layer
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @return the layer style
     */
    public FeatureStyle getStyle() {
        return style;
    }
    
}
