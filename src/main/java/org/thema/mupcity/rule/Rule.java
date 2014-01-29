/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.mupcity.rule;

import java.util.List;
import org.thema.mupcity.Project;

/**
 *
 * @author gvuidel
 */
public interface Rule {
    
    public String getName();
    public String getFullName();
    public boolean isUsable();
    public List<Project.Layers> getUsedLayers();
    public void createRule(Project project);
}
