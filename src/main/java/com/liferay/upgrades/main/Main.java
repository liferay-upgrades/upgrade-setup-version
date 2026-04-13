package com.liferay.upgrades.main;

import com.beust.jcommander.ParameterException;
import com.liferay.upgrades.main.util.StepOptionsUtil;
import com.liferay.upgrades.project.dependency.Step;
import com.liferay.upgrades.project.dependency.bnd.BndRefactorer;
import com.liferay.upgrades.project.dependency.docker.UpdateDockerCompose;
import com.liferay.upgrades.project.dependency.git.GitSetupStep;
import com.liferay.upgrades.project.dependency.git.PullRequestStep;
import com.liferay.upgrades.project.dependency.gradle.*;
import com.liferay.upgrades.project.dependency.jakarta.JakartaUpgradeRunner;
import com.liferay.upgrades.project.dependency.model.VersionOptions;
import com.liferay.upgrades.project.dependency.sourceformatter.SourceFormatterConfigurator;
import com.liferay.upgrades.project.dependency.sourceformatter.SourceFormatterRunner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        try {
            VersionOptions stepOptions =
                StepOptionsUtil.resolveOptions(args);

            if (stepOptions.targetRelease == null ||
                stepOptions.targetRelease.isEmpty()) {

                stepOptions.targetRelease = _deriveVersion(
                    stepOptions.liferayVersion);

                _log.info(
                    "Derived target-release: " + stepOptions.targetRelease);
            }

            if (stepOptions.targetRelease.endsWith("-lts")) {
                stepOptions.targetRelease = stepOptions.targetRelease.substring(
                    0, stepOptions.targetRelease.length() - 4);

                _log.info(
                    "Removed -lts from target-release: " +
                        stepOptions.targetRelease);
            }

            if (stepOptions.dockerCompose == null ||
                stepOptions.dockerCompose.isEmpty()) {

                stepOptions.dockerCompose = _deriveVersion(
                    stepOptions.liferayVersion);

                _log.info(
                    "Derived docker-compose: " + stepOptions.dockerCompose);
            }

            _executeSteps(stepOptions);
        }
        catch (Exception exception) {
            if (exception instanceof ParameterException) {
                _log.info(_generateOptionsHelp());
            }
            else {
                throw new RuntimeException(exception);
            }
        }
    }

    private static String _deriveVersion(String liferayVersion) {
        if (liferayVersion == null || liferayVersion.isEmpty()) {
            return "";
        }

        if (liferayVersion.startsWith("dxp-")) {
            return liferayVersion.substring(4);
        }
        else if (liferayVersion.startsWith("portal-")) {
            return liferayVersion.substring(7);
        }

        return liferayVersion;
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
                \t--github-repo or -gr to specify the GitHub repository to send a PR
               """;
    }

    private static final Map<String, Supplier<Step>> _STEPS_SUPPLIERS =
        new LinkedHashMap<>();

    static {
        _STEPS_SUPPLIERS.put(
            GitSetupStep.class.getSimpleName(),
            GitSetupStep::new);

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

        _STEPS_SUPPLIERS.put(
            SourceFormatterConfigurator.class.getSimpleName(),
            SourceFormatterConfigurator::new);

        _STEPS_SUPPLIERS.put(
            SourceFormatterRunner.class.getSimpleName(),
            SourceFormatterRunner::new);

        _STEPS_SUPPLIERS.put(
            BuildServiceRefactorer.class.getSimpleName(),
            BuildServiceRefactorer::new);

        _STEPS_SUPPLIERS.put(
            BuildRestRefactorer.class.getSimpleName(),
            BuildRestRefactorer::new);

        _STEPS_SUPPLIERS.put(
            JakartaUpgradeRunner.class.getSimpleName(),
            JakartaUpgradeRunner::new);

        _STEPS_SUPPLIERS.put(
            PullRequestStep.class.getSimpleName(),
            PullRequestStep::new);
    }

    private static final Logger _log = Logger.getLogger(
        Main.class.getName());

}
