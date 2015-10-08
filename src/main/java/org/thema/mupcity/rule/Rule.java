
package org.thema.mupcity.rule;

import java.util.List;
import org.thema.mupcity.Project;

/**
 * Main interface for rules.
 * 
 * @author Gilles Vuidel
 */
public interface Rule {
    
    /**
     * @return the short name of the rule
     */
    String getName();
    /**
     * @return the full name of the rule, may be locale dependent
     */
    String getFullName();
    /**
     * Returns true if all layers needed by this rule exist in the project
     * @param project the current project
     * @return true if the rule can be used in this project
     */
    boolean isUsable(Project project);
    
    /**
     * @return the layers needed by this rule
     */
    List<Project.Layers> getUsedLayers();
    
    /**
     * Computes the grid layer for this rule
     * @param project the current project
     */
    void createRule(Project project);
}
