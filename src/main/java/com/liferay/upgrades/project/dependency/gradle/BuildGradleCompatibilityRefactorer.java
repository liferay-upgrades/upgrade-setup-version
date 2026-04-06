package com.liferay.upgrades.project.dependency.gradle;

import com.liferay.upgrades.project.dependency.Step;
import com.liferay.upgrades.project.dependency.model.Context;
import com.liferay.upgrades.project.dependency.model.VersionOptions;

import java.io.File;
import java.util.logging.Logger;

public class BuildGradleCompatibilityRefactorer implements Step {

    @Override
    public Context applyChanges(VersionOptions stepOptions) throws Exception {
        String directory = stepOptions.directory;

        if (_hasProperty(directory, "sourceCompatibility") ||
            _hasProperty(directory, "targetCompatibility")) {

            _log.info(
                "Compatibility properties found. Proceeding with removal...");

            _executeShell(directory, _generateCommand("/sourceCompatibility/d"));

            _executeShell(directory, _generateCommand("/targetCompatibility/d"));

            return new Context(
                stepOptions.ticket, null, directory, null, null, null, null,
                null);
        }

        _log.info(
            "No sourceCompatibility or targetCompatibility properties found. " +
                "Skipping step.");

        return null;
    }

    @Override
    public String commitMessage(Context context) {
        StringBuilder sb = new StringBuilder();

        sb.append(context.ticket());
        sb.append(" Remove sourceCompatibility and targetCompatibility from build.gradle files");

        return sb.toString();
    }

    private boolean _hasProperty(String directory, String propertyName)
        throws Exception {

        String checkCommand = String.format(
            "grep -rql --include=\"build.gradle\" \"%s\" .", propertyName);

        ProcessBuilder processBuilder = new ProcessBuilder(
            "sh", "-c", checkCommand);

        processBuilder.directory(new File(directory));

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        return exitCode == 0;
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
        BuildGradleCompatibilityRefactorer.class.getName());

}
