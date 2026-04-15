#!/bin/bash

# Function to ask for a variable
ask_variable() {
    local prompt="$1"
    local required="$2"
    local value=""

    while true; do
        read -p "$prompt: " value
        if [ -z "$value" ] && [ "$required" = "true" ]; then
            echo "Error: This field is required."
        else
            break
        fi
    done
    echo "$value"
}

echo "Liferay Upgrade Setup Version"
echo "-----------------------------------------------"

TICKET=$(ask_variable "Enter Jira Ticket ID (Required)" "true")
LIFERAY_VERSION=$(ask_variable "Enter new Liferay version (Required)" "true")
FOLDER=$(ask_variable "Enter workspace folder path (Required)" "true")

PLUGIN_VERSION=$(ask_variable "Enter new Liferay workspace plugin version (Optional)" "false")
GRADLE_VERSION=$(ask_variable "Enter new Gradle version (Optional)" "false")
DOCKER_COMPOSE=$(ask_variable "Enter new Docker image version (Optional)" "false")
TARGET_RELEASE=$(ask_variable "Enter target release for source-formatter (Optional)" "false")
GITHUB_REPO=$(ask_variable "Enter GitHub repository for PR (Optional)" "false")
BASE_BRANCH=$(ask_variable "Enter the base branch for the PR (Optional)" "false")
CSV_PATH=$(ask_variable "Enter CSV file path for module dependency order (Optional)" "false")

# Build the JAR
echo ""
echo "Building the project..."
./gradlew jar

if [ $? -ne 0 ]; then
    echo "Error: Build failed."
    exit 1
fi

# Construct the command
CMD="java -jar build/libs/upgrade-setup-version.jar -t \"$TICKET\" -l \"$LIFERAY_VERSION\" -f \"$FOLDER\""

if [ -n "$PLUGIN_VERSION" ]; then
    CMD="$CMD -p \"$PLUGIN_VERSION\""
fi

if [ -n "$GRADLE_VERSION" ]; then
    CMD="$CMD -g \"$GRADLE_VERSION\""
fi

if [ -n "$DOCKER_COMPOSE" ]; then
    CMD="$CMD -d \"$DOCKER_COMPOSE\""
fi

if [ -n "$TARGET_RELEASE" ]; then
    CMD="$CMD -tr \"$TARGET_RELEASE\""
fi

if [ -n "$GITHUB_REPO" ]; then
    CMD="$CMD -gr \"$GITHUB_REPO\""
fi

if [ -n "$BASE_BRANCH" ]; then
    CMD="$CMD -bb \"$BASE_BRANCH\""
fi

if [ -n "$CSV_PATH" ]; then
    CMD="$CMD -c \"$CSV_PATH\""
fi

echo ""
echo "Executing: $CMD"
eval $CMD
