import ellemes.gradle.mod.api.task.MinifyJsonTask

plugins {
    id("ellemes.gradle.mod").apply(false)
}

loom {
    forge {
        mixinConfig("expandedstorage-forge.mixins.json")
        mixinConfig("ellemes-container-library-forge.mixins.json")
    }
}

repositories {
    maven {
        // JEI maven
        name = "Progwml6 maven"
        url = uri("https://dvs1.progwml6.com/files/maven/")
    }
    maven {
        // JEI maven - fallback
        name = "ModMaven"
        url = uri("https://modmaven.k-4u.nl")
    }
    maven {
        name = "Jared"
        url = uri("https://maven.blamejared.com/")
    }
}

val noConfiguration: (ModuleDependency) -> Unit = {
}

dependencies {
    compileOnly("mezz.jei:jei-${properties["jei_minecraft_version"]}-forge-api:${properties["jei_version"]}")
    modCompileOnly("maven.modrinth:inventory-profiles-next:forge-${properties["ipn_minecraft_version"]}-${properties["ipn_version"]}")
    modCompileOnly("vazkii.quark:Quark:3.3-373.2529")
}

tasks.getByName<MinifyJsonTask>("minJar") {
    manifest.attributes(mapOf(
            "Automatic-Module-Name" to "ellemes.expandedstorage"
    ))
}

val u = ellemes.gradle.mod.api.publishing.UploadProperties(project, "${project.property("repo_base_url")}")

u.configureCurseForge {
    relations(closureOf<me.hypherionmc.cursegradle.CurseRelation> {
        optionalDependency("jei")
        optionalDependency("quark")
        optionalDependency("inventory-profiles-next")
    })
}

u.configureModrinth {
    dependencies {
//        optional.project("jei") // jei (not on Modrinth)
//        optional.project("quark") // quark (not on Modrinth)
        optional.project("inventory-profiles-next")
    }
}
