package com.liferay.upgrades.project.dependency.model;

/**
 * @author Albert Gomes Cabral
 */
public record Context(
    String ticket, String dockerVersion, String directory,
    String gradleVersion, String liferayVersion, String oldVersion,
    String targetRelease) {

}
