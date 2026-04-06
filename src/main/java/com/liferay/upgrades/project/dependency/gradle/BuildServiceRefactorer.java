package com.liferay.upgrades.project.dependency.gradle;

import com.liferay.upgrades.project.dependency.Step;
import com.liferay.upgrades.project.dependency.git.GitHandler;
import com.liferay.upgrades.project.dependency.model.Context;
import com.liferay.upgrades.project.dependency.model.VersionOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BuildServiceRefactorer implements Step {

    @Override
    public Context applyChanges(VersionOptions stepOptions) throws Exception {
        return null;
    }

    @Override
    public String commitMessage(Context context) {
        StringBuilder sb = new StringBuilder();

        sb.append(context.ticket());
        sb.append(" buildService in ");
        sb.append(context.targetRelease());
        sb.append(" module");

        return sb.toString();
    }

    @Override
    public Context run(VersionOptions stepOptions) throws Exception {
        String directory = stepOptions.directory;

        List<File> serviceModules = _findServiceModules(directory);

        GitHandler gitHandler = new GitHandler();

        for (File module : serviceModules) {
            Context context = _applyChanges(stepOptions, module);

            gitHandler.commit(directory, commitMessage(context));
        }

        return null;
    }

    private Context _applyChanges(VersionOptions stepOptions, File module)
        throws Exception {

        String modulePath = module.getAbsolutePath();

        _log.info("Running buildService in " + modulePath);

        _executeShell(modulePath, "blade gw buildService");

        return new Context(
            stepOptions.ticket, null, stepOptions.directory, null, null, null,
            module.getName(), null);
    }

    private List<File> _findServiceModules(String directory) {
        List<File> serviceModules = new ArrayList<>();

        File modulesDir = new File(directory, "modules");

        if (modulesDir.exists() && modulesDir.isDirectory()) {
            _findServiceXml(modulesDir, serviceModules);
        }

        return serviceModules;
    }

    private void _findServiceXml(File directory, List<File> serviceModules) {
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
                _findServiceXml(file, serviceModules);
            }
            else if (file.getName().equals("service.xml")) {
                serviceModules.add(file.getParentFile());
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
        BuildServiceRefactorer.class.getName());

}
