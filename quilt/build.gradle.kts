import compasses.gradle.mod.api.publishing.UploadProperties

plugins {
    id("ellemes.gradle.mod").apply(false)
    id("thread-plugin")
}

dependencies {
    modImplementation(mod.fabricApi().full())
}

val upload = UploadProperties(project, "${project.property("repo_base_url")}")

upload.configureCurseForge {
    relations(closureOf<me.hypherionmc.cursegradle.CurseRelation> {
        requiredDependency("qsl")
        optionalDependency("htm")
        optionalDependency("carrier")
        optionalDependency("towelette")
        optionalDependency("roughly-enough-items")
        optionalDependency("modmenu")
        optionalDependency("amecs")
        optionalDependency("inventory-profiles-next")
        optionalDependency("emi")
        optionalDependency("inventory-tabs-updated")
        optionalDependency("jei")
    })
}

upload.configureModrinth {
    dependencies {
        required.project("qvIfYCYJ") // qsl
        optional.project("IEPAK5x6") // htm
//         optional.project("carrier") // carrier (not on Modrinth)
        optional.project("bnesqDoc") // towelette
        optional.project("nfn13YXA") // rei
        optional.project("mOgUt4GM") // modmenu
        optional.project("rcLriA4v") // amecs
        optional.project("O7RBXm3n") // inventory-profiles-next
        optional.project("fRiHVvU7") // emi
        optional.project("F1AqcMCK") // inventory-tabs-updated
        optional.project("u6dRKJwZ") // jei
    }
}
