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

rootProject.name = "VisaoColor"
include(":app")

val properties = java.util.Properties()
file("gradle.properties").inputStream().use { properties.load(it) }
val opencvsdk = properties.getProperty("opencvsdk")

include(":opencv")
project(":opencv").projectDir = File("$opencvsdk/sdk")