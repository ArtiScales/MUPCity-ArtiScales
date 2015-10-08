
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
