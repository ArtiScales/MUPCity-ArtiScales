/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.mupcity.rule;

import java.util.Collections;
import org.thema.mupcity.Project;
import org.thema.mupcity.operation.PBuildFreeOperation;

/**
 *
 * @author gvuidel
 */
public class MorphoRule extends AbstractRule{

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
