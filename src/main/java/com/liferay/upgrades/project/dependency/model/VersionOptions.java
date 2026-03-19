package com.liferay.upgrades.project.dependency.model;

import com.beust.jcommander.Parameter;

/**
 * @author Albert Gomes Cabral
 */
public class VersionOptions {

    @Parameter(
        names = {"-t", "--ticket"},
        description = "Jira ticket ID",
        required = true
    )
    public String ticket;

    @Parameter(
        names = {"-p", "--plugin-version"},
        description = "Set the new liferay workspace plugin version",
        required = true
    )
    public String pluginsVersion;

    @Parameter(
        names = {"-g", "--gradle-version"},
        description = "Set the new Gradle version)",
        required = true
    )
    public String gradleVersion;

    @Parameter(
        names = {"-l", "--liferay-version"},
        description = "Set the new Liferay upgrade version",
        required = true
    )
    public String liferayVersion;

    @Parameter(
        names = {"-d", "--docker-compose"},
        description = "Set the new image liferay version in docker compose"
    )
    public String dockerCompose;

    @Parameter(
        names = {"-f", "--folder"},
        description = "Specify the path for the liferay workspace",
        required = true
    )
    public String directory;

    @Parameter(
        names = {"-tr", "--target-release"},
        description = "Set the target release for source-formatter",
        required = true
    )
    public String targetRelease;

}
