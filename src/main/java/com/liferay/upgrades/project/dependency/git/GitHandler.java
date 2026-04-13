package com.liferay.upgrades.project.dependency.git;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Logger;

public class GitHandler {

    public void commit(String directory, String message) throws Exception {

        _executeCommand(directory, "git", "add", ".");

        if (!_hasStagedChanges(directory)) {
            _log.info("No changes to commit for: " + message);
            return;
        }

        _executeCommand(directory, "git", "commit", "-m", message);

        _log.info("Git commit successful: " + message);

    }

    public void createBranch(String directory, String branchName) throws Exception {
        _executeCommand(directory, "git", "checkout", "-B", branchName);
        _log.info("Created and switched to branch: " + branchName);
    }

    public void push(String directory, String remote, String branch) throws Exception {
        _executeCommand(directory, "git", "push", "-u", remote, branch);
        _log.info("Git push successful to " + remote + " " + branch);
    }

    public void createPullRequest(
            String directory, String repo, String head, String base,
            String title, String body)
        throws Exception {

        _executeCommand(
            directory, "gh", "pr", "create", "--repo", repo, "--head", head,
            "--base", base, "--title", title, "--body", body);

        _log.info("Pull Request created successfully for " + repo);
    }

    public void setGhToken(String ghToken) {
        _ghToken = ghToken;
    }

    private boolean _hasStagedChanges(String directory) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "--cached", "--quiet");

        processBuilder.directory(Paths.get(directory).toFile());

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        return exitCode == 1;
    }

    private void _executeCommand(String directory, String... command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.directory(Paths.get(directory).toFile());

        Map<String, String> environment = processBuilder.environment();

        if (_ghToken != null) {
            environment.put("GH_TOKEN", _ghToken);
        }

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                _log.fine("GIT: " + line);
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Git command failed with exit code " + exitCode);
        }

    }

    private String _ghToken;

    private static final Logger _log = Logger.getLogger(GitHandler.class.getName());
}
