package com.liferay.upgrades.main;

import com.beust.jcommander.ParameterException;
import com.liferay.upgrades.main.util.StepOptionsUtil;
import com.liferay.upgrades.project.dependency.Step;
import com.liferay.upgrades.project.dependency.bnd.BndRefactorer;
import com.liferay.upgrades.project.dependency.docker.UpdateDockerCompose;
import com.liferay.upgrades.project.dependency.gradle.*;
import com.liferay.upgrades.project.dependency.model.VersionOptions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;

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
            if (exception instanceof ParameterException) {
                _log.info(_generateOptionsHelp());
            }
            else throw new RuntimeException(exception);
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

    private static String _generateOptionsHelp() {
        return """
                The available options are:
                \t--ticket or -t to set the Jira ticket ID (Required)
                \t--plugin-version or -p to set the new Liferay workspace plugin version
                \t--gradle-version or -g to Set the new Gradle version
                \t--liferay-version or -l to set the new Liferay upgrade version (Required)
                \t--docker-compose or -d to set the new image liferay version in docker compose
                \t--folder or -f to specify the path for the liferay workspace (Required)
                \t--target-release or -tr to Set the target release for source-formatter
               """;
    }

    private static final Map<String, Supplier<Step>> _STEPS_SUPPLIERS =
        new LinkedHashMap<>();

    static {
        _STEPS_SUPPLIERS.put(
            UpdateGradleProperties.class.getSimpleName(),
            UpdateGradleProperties::new);

        _STEPS_SUPPLIERS.put(
            UpdateDockerCompose.class.getSimpleName(),
            UpdateDockerCompose::new);

        _STEPS_SUPPLIERS.put(
            UpdateSettingsGradle.class.getSimpleName(),
            UpdateSettingsGradle::new);

        _STEPS_SUPPLIERS.put(
            UpdateGradleWrapper.class.getSimpleName(),
            UpdateGradleWrapper::new);

        _STEPS_SUPPLIERS.put(
            BuildGradleTaskRefactorer.class.getSimpleName(),
            BuildGradleTaskRefactorer::new);

        _STEPS_SUPPLIERS.put(
            BuildGradlePortalApiRefactorer.class.getSimpleName(),
            BuildGradlePortalApiRefactorer::new);

        _STEPS_SUPPLIERS.put(
            BuildGradleCompatibilityRefactorer.class.getSimpleName(),
            BuildGradleCompatibilityRefactorer::new);

        _STEPS_SUPPLIERS.put(
            BndRefactorer.class.getSimpleName(),
            BndRefactorer::new);
        }

    private static final Logger _log = Logger.getLogger(StepMain.class.getName());

}
