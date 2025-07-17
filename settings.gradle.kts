pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://repo1.maven.org/maven2/")
            isAllowInsecureProtocol = false
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add JitPack as backup
        maven { url = uri("https://jitpack.io") }
        // Add Gradle Plugin Portal
        gradlePluginPortal()
    }
}

rootProject.name = "Clear Sky"

// App module
include(":app")

// Core modules
include(":core:common")
include(":core:network")

// Feature modules
include(":feature:weather")
include(":feature:main")
include(":feature:settings")
include(":feature:alerts")
 