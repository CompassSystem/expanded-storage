import ellemes.gradle.mod.api.task.MinifyJsonTask

plugins {
    id("ellemes.gradle.mod").apply(false)
}

loom {
    forge {
        convertAccessWideners.set(true)
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
        mixinConfig("expandedstorage-common.mixin.json")
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
}

val noConfiguration: (ModuleDependency) -> Unit = {
}

dependencies {
    include(modImplementation("ellemes:${properties["container_library_artifact"]}-forge:${properties["container_library_version"]}", dependencyConfiguration = noConfiguration))
}

tasks.getByName<MinifyJsonTask>("minJar") {
    manifest.attributes(mapOf(
            "Automatic-Module-Name" to "ellemes.expandedstorage",
            "MixinConfigs" to "expandedstorage-common.mixin.json"
    ))
}

val u = ellemes.gradle.mod.api.publishing.UploadProperties(project, "https://codeberg.org/Ellemes/expanded-storage")

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
