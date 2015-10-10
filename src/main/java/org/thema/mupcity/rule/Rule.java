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
