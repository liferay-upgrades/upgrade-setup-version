package com.liferay.upgrades.project.dependency.sourceformatter;

import com.liferay.upgrades.project.dependency.Step;
import com.liferay.upgrades.project.dependency.git.GitHandler;
import com.liferay.upgrades.project.dependency.model.Context;
import com.liferay.upgrades.project.dependency.model.VersionOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SourceFormatterCSVRunner implements Step {

    @Override
    public Context applyChanges(VersionOptions stepOptions) throws Exception {
        if (stepOptions.csv == null || stepOptions.csv.isEmpty()) {
            return null;
        }

        File csvFile = new File(stepOptions.csv);

        if (!csvFile.exists()) {
            _log.warning("CSV file not found at: " + stepOptions.csv);
            return null;
        }

        _log.info("Reading modules from CSV: " + stepOptions.csv);

        List<ModuleEntry> modules = _parseCSV(csvFile);

        _log.info("Found " + modules.size() + " modules to process.");

        String directory = stepOptions.directory;
        GitHandler gitHandler = new GitHandler();

        for (ModuleEntry module : modules) {
            _log.info("Processing module: " + module.bundleName + " (Level " + module.level + ")");

            String gradlePath = _resolveGradlePath(directory, module.bundleName);

            if (gradlePath == null) {
                _log.warning("Could not resolve gradle path for: " + module.bundleName);
                continue;
            }

            String command =
                "blade gw " + gradlePath + ":formatSource -Pjava.parser.enabled=false " +
                    "-Psource.check.category.names=Upgrade";

            boolean success = _executeShell(directory, command);

            if (!success) {
                _log.warning("Skipping commit for module " + module.bundleName + " due to timeout or failure.");
                continue;
            }

            String commitMessage = stepOptions.ticket + " SF Automation in " + module.bundleName + " module";

            gitHandler.commit(directory, commitMessage);
        }

        _log.info("SF automation completed for all modules from CSV.");

        return null;
    }

    private String _resolveGradlePath(String directory, String bundleName) {
        if (bundleName.startsWith(":")) {
            return bundleName;
        }

        File root = new File(directory);

        String[] searchFolders = {"modules", "themes", "wars"};

        for (String folder : searchFolders) {
            File searchDir = new File(root, folder);

            if (searchDir.exists() && searchDir.isDirectory()) {
                String relativePath = _findModulePath(searchDir, bundleName);

                if (relativePath != null) {
                    return ":" + folder + ":" + relativePath.replace(File.separator, ":");
                }
            }
        }

        return null;
    }

    private String _findModulePath(File dir, String bundleName) {
        File[] files = dir.listFiles();

        if (files == null) {
            return null;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getName().equals(bundleName)) {

                    if (new File(file, "build.gradle").exists() || new File(file, "bnd.bnd").exists()) {
                        return file.getName();
                    }
                }

                String subPath = _findModulePath(file, bundleName);

                if (subPath != null) {
                    return file.getName() + File.separator + subPath;
                }
            }
        }

        return null;
    }

    @Override
    public String commitMessage(Context context) {
        return "";
    }

    private List<ModuleEntry> _parseCSV(File csvFile) throws Exception {
        List<ModuleEntry> modules = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\t");

                if (parts.length < 2) {
                    parts = line.split(",");
                }

                if (parts.length >= 2) {
                    try {
                        int level = Integer.parseInt(parts[0].trim());
                        String bundleName = parts[1].trim();

                        modules.add(new ModuleEntry(level, bundleName));
                    } catch (NumberFormatException e) {
                        _log.warning("Skipping invalid line: " + line);
                    }
                }
            }
        }

        modules.sort(Comparator.comparingInt(m -> m.level));

        return modules;
    }

    private boolean _executeShell(String directory, String command)
        throws Exception {

        _log.info("Executing: " + command);

        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);

        processBuilder.directory(new File(directory));

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    _log.fine(line);
                }
            } catch (Exception e) {
                _log.warning("Error reading process output: " + e.getMessage());
            }
        });

        outputThread.start();

        boolean finished = process.waitFor(1, TimeUnit.MINUTES);

        if (!finished) {
            _log.warning("Command timed out after 1 minute: " + command);

            process.destroyForcibly();

            return false;
        }

        int exitCode = process.exitValue();

        if (exitCode != 0) {
            _log.warning(
                "Command failed with exit code: " + exitCode);

            return false;
        }

        return true;
    }

    private static class ModuleEntry {
        int level;
        String bundleName;

        ModuleEntry(int level, String bundleName) {
            this.level = level;
            this.bundleName = bundleName;
        }
    }

    private static final Logger _log = Logger.getLogger(
        SourceFormatterCSVRunner.class.getName());

}
