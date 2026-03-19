package com.liferay.upgrades.project.dependency;

import com.liferay.upgrades.project.dependency.git.GitHandler;
import com.liferay.upgrades.project.dependency.model.Context;
import com.liferay.upgrades.project.dependency.model.VersionOptions;

/**
 * @author Albert Gomes
 */
public interface Step {

    Context applyChanges(VersionOptions stepOptions) throws Exception;

    String commitMessage(Context context);

    default Context run(VersionOptions stepOptions) throws Exception {
        Context result = applyChanges(stepOptions);

        if (result != null) {
            GitHandler gitHandler = new GitHandler();

            gitHandler.commit(
                result.directory(), commitMessage(result));
        }

        return result;
    }

}
