package com.liferay.upgrades.project.dependency.gradle;

import com.liferay.upgrades.project.dependency.Step;
import com.liferay.upgrades.project.dependency.git.GitHandler;
import com.liferay.upgrades.project.dependency.model.Context;
import com.liferay.upgrades.project.dependency.model.VersionOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BuildRestRefactorer implements Step {

    @Override
    public Context applyChanges(VersionOptions stepOptions) throws Exception {
        return null;
    }

    @Override
    public String commitMessage(Context context) {
        StringBuilder sb = new StringBuilder();

        sb.append(context.ticket());
        sb.append(" buildRest in ");
        sb.append(context.targetRelease());
        sb.append(" module");

        return sb.toString();
    }

    @Override
    public Context run(VersionOptions stepOptions) throws Exception {
        String directory = stepOptions.directory;

        List<File> restModules = _findRestModules(directory);

        GitHandler gitHandler = new GitHandler();

        for (File module : restModules) {
            Context context = _applyChanges(stepOptions, module);

            gitHandler.commit(directory, commitMessage(context));
        }

        return null;
    }

    private Context _applyChanges(VersionOptions stepOptions, File module)
        throws Exception {

        String modulePath = module.getAbsolutePath();

        _log.info("Running buildRest in " + modulePath);

        _executeShell(modulePath, "blade gw buildRest");

        return new Context(
            stepOptions.ticket, null, stepOptions.directory, null, null, null,
            module.getName(), null);
    }

    private List<File> _findRestModules(String directory) {
        List<File> restModules = new ArrayList<>();
        File modulesDir = new File(directory, "modules");

        if (modulesDir.exists() && modulesDir.isDirectory()) {
            _findRestConfigYaml(modulesDir, restModules);
        }

        return restModules;
    }

    private void _findRestConfigYaml(File directory, List<File> restModules) {
        String name = directory.getName();

        if (name.equals("bin") || name.equals("build") ||
            name.equals(".gradle") || name.equals(".idea")) {

            return;
        }

        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                _findRestConfigYaml(file, restModules);
            }
            else if (file.getName().equals("rest-config.yaml")) {
                restModules.add(file.getParentFile());
            }
        }
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
                "Command '" + command + "' failed with exit code: " + exitCode);
        }
    }

    private static final Logger _log = Logger.getLogger(
        BuildRestRefactorer.class.getName());

}
