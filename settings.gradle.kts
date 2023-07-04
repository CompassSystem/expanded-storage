pluginManagement {
    repositories {
        maven {
            name = "Fabric Maven"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "Cotton Maven"
            url = uri("https://server.bbkr.space/artifactory/libs-release/")
        }
        gradlePluginPortal()
        mavenLocal()
    }
}

rootProject.name = "expandedstorage"

include(":fabric")
