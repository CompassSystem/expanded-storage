pluginManagement {
    repositories {
        maven {
            name = "Fabric Maven"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "Architectury Maven"
            url = uri("https://maven.architectury.dev/")
        }
        maven {
            name = "MinecraftForge Maven"
            url = uri("https://maven.minecraftforge.net/")
        }
        maven {
            name = "Quilt Release Maven"
            url = uri("https://maven.quiltmc.org/repository/release/")
        }
        maven {
            name = "Quilt Snapshot Maven"
            url = uri("https://maven.quiltmc.org/repository/snapshot/")
        }
        maven {
            name = "Cotton Maven"
            url = uri("https://server.bbkr.space/artifactory/libs-release/")
        }
        gradlePluginPortal()
        mavenLocal()
    }
    includeBuild("plugins/mod/")
}

rootProject.name = "expandedstorage"

include(":common")
include(":common:thread")
project(":common:thread").projectDir = file("thread")
include(":common:thread:fabric")
project(":common:thread:fabric").projectDir = file("fabric")
include(":common:thread:quilt")
project(":common:thread:quilt").projectDir = file("quilt")
include(":common:forge")
project(":common:forge").projectDir = file("forge")
