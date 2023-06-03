import ellemes.gradle.mod.api.task.AbstractJsonTask

plugins {
    id("ellemes.gradle.mod").apply(false)
    id("thread-plugin")
}

loom {
    forge {
        mixinConfig("expandedstorage-forge.mixins.json")
    }
}

tasks.getByName<AbstractJsonTask>("minJar") {
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
        optionalDependency("roughly-enough-items")
    })
}

u.configureModrinth {
    dependencies {
        optional.project("u6dRKJwZ") // jei
        optional.project("qnQsVE2z") // quark
        optional.project("O7RBXm3n") // inventory profiles next
        optional.project("nfn13YXA") // rei
    }
}
