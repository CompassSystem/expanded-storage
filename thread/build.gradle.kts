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
    // For Mod Menu
    exclusiveContent {
        forRepository {
            maven {
                name = "TerraformersMC"
                url = uri("https://maven.terraformersmc.com/")
            }
        }
        filter {
            includeGroup("com.terraformersmc")
        }
    }

    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    // For REI
    maven {
        name = "Shedaniel"
        url = uri("https://maven.shedaniel.me/")
    }
    // For Amecs
    maven {
        name = "Siphalor's Maven"
        url = uri("https://maven.siphalor.de/")
    }
    maven {
        name = "Flemmli97"
        url = uri("https://gitlab.com/api/v4/projects/21830712/packages/maven")
    }
}

dependencies {
    //listOf(
    //    "fabric-api-base",
    //    "fabric-data-generation-api-v1",
    //    "fabric-blockrenderlayer-v1",
    //    "fabric-item-groups-v0",
    //    "fabric-rendering-v1",
    //    "fabric-textures-v0",
    //    "fabric-lifecycle-events-v1",
    //    "fabric-transfer-api-v1"
    //).forEach {
    //    modImplementation(mod.fabricApi().module(it))
    //}

    modImplementation(mod.fabricApi().full())

    modCompileOnly("com.terraformersmc:modmenu:${project.properties["modmenu_version"]}") {
        excludeFabric(this)
    }

    modCompileOnly(group = "curse.maven", name = "statement-340604", version = "3423826", dependencyConfiguration = excludeFabric)
    modCompileOnly(group = "curse.maven", name = "towelette-309338", version = "3398761", dependencyConfiguration = excludeFabric)
    modCompileOnly(group = "curse.maven", name = "carrier-409184", version = "3504375", dependencyConfiguration = excludeFabric)
    modCompileOnly(group = "io.github.onyxstudios.Cardinal-Components-API", name = "cardinal-components-base", version = "${properties["cardinal_version"]}", dependencyConfiguration = excludeFabric)
    modCompileOnly(group = "io.github.onyxstudios.Cardinal-Components-API", name = "cardinal-components-entity", version = "${properties["cardinal_version"]}", dependencyConfiguration = excludeFabric)
    modCompileOnly(group = "curse.maven", name = "htm-462534", version = "3539120", dependencyConfiguration = excludeFabric)

    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:${project.properties["rei_version"]}") {
        excludeFabric(this)
    }

    modCompileOnly("com.terraformersmc:modmenu:${project.properties["modmenu_version"]}") {
        excludeFabric(this)
    }

    modCompileOnly("de.siphalor:amecsapi-1.18:${project.properties["amecs_version"]}") {
        excludeFabric(this)
        exclude(group = "com.github.astei")
    }

    modCompileOnly("io.github.flemmli97:flan:1.18.2-${project.properties["flan_version"]}:fabric-api") {
        excludeFabric(this)
        exclude(group = "curse.maven")
    }

    modCompileOnly("maven.modrinth:inventory-profiles-next:fabric-${rootProject.properties["ipn_minecraft_version"]}-${rootProject.properties["ipn_version"]}") {
        excludeFabric(this)
    }
}
