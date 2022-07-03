plugins {
    id("ellemes.gradle.mod").apply(false)
}

val excludeFabric: (ModuleDependency) -> Unit = {
    it.exclude("net.fabricmc")
    it.exclude("net.fabricmc.fabric-api")
}

repositories {
    maven {
        name = "Ladysnake maven"
        url = uri("https://ladysnake.jfrog.io/artifactory/mods")
        content {
            includeGroup("io.github.onyxstudios.Cardinal-Components-API")
        }
    }
    maven {
        name = "Devan maven"
        url = uri("https://raw.githubusercontent.com/Devan-Kerman/Devan-Repo/master/")
        content {
            includeGroup("net.devtech")
        }
    }

    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
    listOf(
        "fabric-api-base",
        "fabric-data-generation-api-v1",
        "fabric-blockrenderlayer-v1",
        "fabric-item-groups-v0",
        "fabric-rendering-v1",
        "fabric-textures-v0",
        "fabric-lifecycle-events-v1",
        "fabric-transfer-api-v1"
    ).forEach {
        modImplementation(mod.fabricApi().module(it))
    }


    modCompileOnly("ellemes:${properties["container_library_artifact"]}-fabric:${properties["container_library_version"]}", dependencyConfiguration = excludeFabric)

    modCompileOnly(group = "curse.maven", name = "statement-340604", version = "3423826", dependencyConfiguration = excludeFabric)
    modCompileOnly(group = "curse.maven", name = "towelette-309338", version = "3398761", dependencyConfiguration = excludeFabric)
    modCompileOnly(group = "curse.maven", name = "carrier-409184", version = "3504375", dependencyConfiguration = excludeFabric)
    modCompileOnly(group = "io.github.onyxstudios.Cardinal-Components-API", name = "cardinal-components-base", version = "${properties["cardinal_version"]}", dependencyConfiguration = excludeFabric)
    modCompileOnly(group = "io.github.onyxstudios.Cardinal-Components-API", name = "cardinal-components-entity", version = "${properties["cardinal_version"]}", dependencyConfiguration = excludeFabric)
    modCompileOnly(group = "curse.maven", name = "htm-462534", version = "3539120", dependencyConfiguration = excludeFabric)
}