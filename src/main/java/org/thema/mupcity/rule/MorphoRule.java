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

import java.util.Collections;
import org.thema.mupcity.Project;
import org.thema.mupcity.operation.PBuildFreeOperation;

/**
 * The default morphological rule.
 * This rule is used only for initial view.
 * It is dupplicated for each scenario, cause values change for each built cell.
 * 
 * @author Gilles Vuidel
 */
public class MorphoRule extends AbstractRule {

    /**
     * Creates a new default MorphoRule
     */
    public MorphoRule() {
        super(Collections.EMPTY_LIST);
    }

    @Override
    public String getName() {
        return Project.MORPHO_RULE;
    }

    @Override
    public void createRule(Project project) {
        project.getMSGrid().addDynamicLayer(getName(), new PBuildFreeOperation(Project.BUILD));
    }
    
}
