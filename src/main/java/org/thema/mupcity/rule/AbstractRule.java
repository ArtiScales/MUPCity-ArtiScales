
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
