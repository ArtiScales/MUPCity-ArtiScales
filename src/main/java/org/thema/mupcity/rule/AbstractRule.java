/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.mupcity.rule;

import java.util.List;
import org.thema.mupcity.Project;
import org.thema.mupcity.Project.Layers;
import org.thema.common.param.XMLParams;

/**
 *
 * @author gvuidel
 */
public abstract class AbstractRule implements Rule {
    
    @XMLParams.NoParam
    protected List<Layers> usedLayers;

    public AbstractRule(List<Layers> usedLayers) {
        this.usedLayers = usedLayers;
    }
 
    @Override
    public boolean isUsable() {
        for(Layers layer : usedLayers)
            if(!Project.getProject().isLayerExist(layer))
                return false;
        return true;
    }

    @Override
    public String toString() {
        return getFullName();
    }
    
    @Override
    public String getFullName() {
        return java.util.ResourceBundle.getBundle("org/thema/mupcity/rule/Bundle").getString(getName());
    }

    public List<Layers> getUsedLayers() {
        return usedLayers;
    }
    
    
}
