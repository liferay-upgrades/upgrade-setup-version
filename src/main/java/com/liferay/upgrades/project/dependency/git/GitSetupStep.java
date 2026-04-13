package com.liferay.upgrades.project.dependency.git;

import com.liferay.upgrades.project.dependency.Step;
import com.liferay.upgrades.project.dependency.model.Context;
import com.liferay.upgrades.project.dependency.model.VersionOptions;

public class GitSetupStep implements Step {

    @Override
    public Context applyChanges(VersionOptions stepOptions) throws Exception {
        if (stepOptions.githubRepo == null || stepOptions.githubRepo.isEmpty()) {
            return null;
        }

        GitHandler gitHandler = new GitHandler();

        gitHandler.createBranch(stepOptions.directory, stepOptions.ticket);

        return null;
    }

    @Override
    public String commitMessage(Context context) {
        return "";
    }

}
