package com.liferay.upgrades.project.dependency.git;

import com.liferay.upgrades.project.dependency.Step;
import com.liferay.upgrades.project.dependency.model.Context;
import com.liferay.upgrades.project.dependency.model.VersionOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class PullRequestStep implements Step {

    @Override
    public Context applyChanges(VersionOptions stepOptions) throws Exception {
        if (stepOptions.githubRepo == null || stepOptions.githubRepo.isEmpty()) {
            return null;
        }

        GitHandler gitHandler = new GitHandler();

        gitHandler.push(stepOptions.directory, "origin", stepOptions.ticket);

        String ghToken = _getGhToken(stepOptions.directory);

        if (ghToken != null) {
            gitHandler.setGhToken(ghToken);
        }

        String title = stepOptions.ticket;
        String body = "Ticket: https://liferay.atlassian.net/browse/" + stepOptions.ticket;
        String baseBranch = stepOptions.baseBranch;

        if (baseBranch == null || baseBranch.isEmpty()) {
            baseBranch = "master";
        }

        gitHandler.createPullRequest(
            stepOptions.directory, stepOptions.githubRepo, stepOptions.ticket,
            baseBranch, title, body);

        return null;
    }

    @Override
    public String commitMessage(Context context) {
        return "";
    }

    private String _getGhToken(String directory) {
        String ghToken = System.getenv("GH_TOKEN");

        if (ghToken == null) {
            ghToken = _loadGhTokenFromFile(new File(".env"));
        }

        if (ghToken == null) {
            ghToken = _loadGhTokenFromFile(new File(directory, ".env"));
        }

        return ghToken;
    }

    private String _loadGhTokenFromFile(File dotEnvFile) {
        if (!dotEnvFile.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(dotEnvFile)))) {

            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("GH_TOKEN=")) {
                    String value = line.substring(9).trim();

                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }

                    return value;
                }
            }
        }
        catch (Exception exception) {
        }

        return null;
    }

}
