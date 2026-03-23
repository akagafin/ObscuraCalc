pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ObscuraCalc"

include(
    ":app",
    ":core-calculator",
    ":core-converter",
    ":core-security",
    ":core-vault",
    ":feature-calculator",
    ":feature-converter",
    ":feature-vault",
    ":feature-settings",
    ":feature-legal",
)
