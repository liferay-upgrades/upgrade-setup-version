package com.liferay.upgrades.project.dependency.jakarta;

import com.liferay.upgrades.project.dependency.Step;
import com.liferay.upgrades.project.dependency.model.Context;
import com.liferay.upgrades.project.dependency.model.VersionOptions;

import java.io.File;
import java.util.logging.Logger;

public class JakartaUpgradeRunner implements Step {

    @Override
    public Context applyChanges(VersionOptions stepOptions) throws Exception {
        String directory = stepOptions.directory;

        _log.info("Running Jakarta EE upgrade automation...");

        String command = "blade gw upgradeJakarta";

        _executeShell(directory, command);

        return new Context(
            stepOptions.ticket, null, directory, null, null, null, null, null);
    }

    @Override
    public String commitMessage(Context context) {
        StringBuilder sb = new StringBuilder();

        sb.append(context.ticket());
        sb.append(" Upgrade to Jakarta EE");

        return sb.toString();
    }

    private void _executeShell(String directory, String command)
        throws Exception {

        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);

        processBuilder.directory(new File(directory));

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            _log.warning(
                "Jakarta upgrade command failed with exit code: " + exitCode);
        }
        else {
            _log.info("Successfully ran Jakarta upgrade tool.");
        }
    }

    private static final Logger _log = Logger.getLogger(
        JakartaUpgradeRunner.class.getName());

}
