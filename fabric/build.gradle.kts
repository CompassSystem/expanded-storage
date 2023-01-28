plugins {
    id("ellemes.gradle.mod").apply(false)
    id("thread-plugin")
}

dependencies {
    modImplementation(mod.fabricApi().full())
}

val u = ellemes.gradle.mod.api.publishing.UploadProperties(project, "${project.property("repo_base_url")}")

u.configureCurseForge {
    relations(closureOf<me.hypherionmc.cursegradle.CurseRelation> {
        requiredDependency("fabric-api")
        optionalDependency("htm")
        optionalDependency("carrier")
        optionalDependency("towelette")
        optionalDependency("roughly-enough-items")
        optionalDependency("modmenu")
        optionalDependency("amecs")
        optionalDependency("flan")
        optionalDependency("inventory-profiles-next")
        optionalDependency("emi")
    })
}

u.configureModrinth {
    dependencies {
        required.project("fabric-api") // P7dR8mSH
        optional.project("htm") // IEPAK5x6
//         optional.project("carrier") // carrier (not on Modrinth)
        optional.project("towelette") // bnesqDoc
        optional.project("roughly-enough-items") // nfn13YXA
        optional.project("modmenu") // mOgUt4GM
        optional.project("amecs") // rcLriA4v
//        optional.project("flan") flan (not on Modrinth)
        optional.project("inventory-profiles-next") // O7RBXm3n
        optional.project("emi") // fRiHVvU7
    }
}
