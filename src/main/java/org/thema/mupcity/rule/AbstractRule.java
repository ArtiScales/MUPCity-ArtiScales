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


package org.thema.mupcity.rule;

import java.util.List;
import org.thema.mupcity.Project;
import org.thema.mupcity.Project.Layers;
import org.thema.common.param.ReflectObject;

/**
 * Base class for rules.
 * 
 * @author Gilles Vuidel
 */
public abstract class AbstractRule implements Rule {
    
    @ReflectObject.NoParam
    protected List<Layers> usedLayers;

    /**
     * Creates a new abstract rule.
     * @param usedLayers list of layers needed by this rule, may be empty
     */
    public AbstractRule(List<Layers> usedLayers) {
        this.usedLayers = usedLayers;
    }
 
    @Override
    public boolean isUsable(Project project) {
        for(Layers layer : usedLayers) {
            if(!project.isLayerExist(layer)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the full name of the rule
     */
    @Override
    public String toString() {
        return getFullName();
    }
    
    /**
     * {@inheritDoc }
     * Retrieves the full name of the rule stored in org/thema/mupcity/rule/Bundle properties file
     */
    @Override
    public String getFullName() {
        return java.util.ResourceBundle.getBundle("org/thema/mupcity/rule/Bundle").getString(getName());
    }

    @Override
    public List<Layers> getUsedLayers() {
        return usedLayers;
    }
    
}
