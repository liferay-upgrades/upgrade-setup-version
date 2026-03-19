package com.liferay.upgrades.main;

import com.liferay.upgrades.main.util.StepOptionsUtil;
import com.liferay.upgrades.project.dependency.Step;
import com.liferay.upgrades.project.dependency.gradle.UpdateGradleProperties;
import com.liferay.upgrades.project.dependency.model.VersionOptions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Albert Gomes Cabral
 *
 * Note: It's a temporary main class to test step interface approach
 */
public class StepMain {

    public static void main(String[] args) {
        VersionOptions stepOptions =
            StepOptionsUtil.resolveOptions(args);

        try {
            _executeSteps(stepOptions);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private static void _executeSteps(VersionOptions options)
        throws Exception {

        Set<Map.Entry<String, Supplier<Step>>> entries =
            _STEPS_SUPPLIERS.entrySet();

        for (Map.Entry<String, Supplier<Step>> entry: entries) {
            Supplier<Step> value = entry.getValue();

            Step step = value.get();

            step.run(options);
        }
    }

    private static final Map<String, Supplier<Step>> _STEPS_SUPPLIERS =
        new LinkedHashMap<>();

    static {
        _STEPS_SUPPLIERS.put(
            UpdateGradleProperties.class.getSimpleName(),
            UpdateGradleProperties::new);
    }

}
