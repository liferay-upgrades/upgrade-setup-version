package com.liferay.upgrades.project.dependency.gradle;

import com.liferay.upgrades.project.dependency.Step;
import com.liferay.upgrades.project.dependency.model.Context;
import com.liferay.upgrades.project.dependency.model.VersionOptions;

import java.io.File;
import java.util.logging.Logger;

public class BuildGradleTaskRefactorer implements Step {

    @Override
    public Context applyChanges(VersionOptions stepOptions) throws Exception {
        String directory = stepOptions.directory;

        String[] actions = {
            "s/\\bcompile\\b/compileOnly/g",
            "s/\\btestCompile\\b/testCompileOnly/g",
            "s/\\bruntime\\b/runtimeOnly/g",
            "s/\\btestRuntime\\b/testRuntimeOnly/g"
        };

        for (String action : actions) {
            _executeShell(directory, _generateCommand(action));
        }

        return new Context(
            stepOptions.ticket, null, directory, null, null, null, null, null);
    }

    @Override
    public String commitMessage(Context context) {
        StringBuilder sb = new StringBuilder();

        sb.append(context.ticket());
        sb.append(" Refactor build.gradle: migrate legacy dependency configurations");

        return sb.toString();
    }

    private String _generateCommand(String sedAction) {
        return String.format(
            "find . -name \"build.gradle\" | while read origin; do " +
                "cat \"$origin\" | sed -e '%s' > \"$origin\".new; " +
                "rm \"$origin\"; mv \"$origin\".new \"$origin\"; done",
            sedAction);
    }

    private void _executeShell(String directory, String command)
        throws Exception {

        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);

        processBuilder.directory(new File(directory));

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            _log.warning("Command failed with exit code: " + exitCode);
        }
    }

    private static final Logger _log = Logger.getLogger(
        BuildGradleTaskRefactorer.class.getName());

}
