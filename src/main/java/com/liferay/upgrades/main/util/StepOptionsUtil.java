package com.liferay.upgrades.main.util;

import com.beust.jcommander.JCommander;
import com.liferay.upgrades.project.dependency.model.VersionOptions;

/**
 * @author Albert Gomes Cabral
 */
public class StepOptionsUtil {

    public static VersionOptions resolveOptions(
        String[] args) {

        VersionOptions stepOptions = new VersionOptions();

        JCommander jCommander = JCommander.newBuilder()
            .addObject(stepOptions)
            .build();

        jCommander.parse(args);

        return stepOptions;
    }

}
