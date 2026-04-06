package com.liferay.upgrades.project.dependency.gradle;

import com.liferay.upgrades.project.dependency.Step;
import com.liferay.upgrades.project.dependency.model.Context;
import com.liferay.upgrades.project.dependency.model.VersionOptions;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateSettingsGradle implements Step {

    @Override
    public Context applyChanges(VersionOptions stepOptions) throws Exception {
        String newVersion = stepOptions.pluginsVersion;
        String directory = stepOptions.directory;

        Path path = Paths.get(directory, "settings.gradle");

        if (!Files.exists(path)) {
            throw new FileNotFoundException(
                "settings.gradle not found at: " + path.toAbsolutePath());
        }

        List<String> lines = Files.readAllLines(path);

        List<String> updatedLines = new ArrayList<>();

        String oldVersion = "legacy";

        boolean found = false;
        boolean changed = false;

        Pattern pattern = Pattern.compile(
            "name:\\s*[\"']" + _pluginId + "[\"'].*version:\\s*[\"'](.*?)[\"']");

        for (String line: lines) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                oldVersion = matcher.group(1);

                if (oldVersion.equals(newVersion)) {
                    _log.info(
                        String.format(
                            "%s is already %s. Skipping update.", _pluginId,
                            newVersion));

                    return null;
                }

                String updatedLine = line.replace(oldVersion, newVersion);
                updatedLines.add(updatedLine);

                found = true;
                changed = true;
                _log.info(
                    "Updated " + _pluginId + " from " + oldVersion + " to " +
                        newVersion);
            } else {
                updatedLines.add(line);
            }
        }

        if (changed) {
            Files.write(path, updatedLines);

            return new Context(
                stepOptions.ticket, null, directory, null, null, oldVersion,
                null, newVersion);
        } else if (!found) {
            _log.warning(
                "Could not find the workspace plugin definition in " +
                    "settings.gradle.");
        }

        return null;
    }

    @Override
    public String commitMessage(Context context) {
        StringBuilder sb = new StringBuilder();

        sb.append(context.ticket());
        sb.append(" Update com.liferay.gradle.plugins.workspace from ");
        sb.append(context.oldVersion());
        sb.append(" to ");
        sb.append(context.pluginsVersion());
        sb.append(" in settings.gradle");

        return sb.toString();
    }

    private static final Logger _log = Logger.getLogger(
        UpdateSettingsGradle.class.getName());

    private static final String _pluginId =
        "com.liferay.gradle.plugins.workspace";

}
