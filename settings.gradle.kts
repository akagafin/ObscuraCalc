pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
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
