import ellemes.gradle.mod.api.task.MinifyJsonTask

plugins {
    id("ellemes.gradle.mod").apply(false)
}

val noConfiguration: (ModuleDependency) -> Unit = {
}

dependencies {
    include(modImplementation("ellemes:${properties["container_library_artifact"]}-forge:${properties["container_library_version"]}", dependencyConfiguration = noConfiguration))
}

tasks.getByName<MinifyJsonTask>("minJar") {
    manifest.attributes(mapOf(
            "Automatic-Module-Name" to "ellemes.expandedstorage"
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
