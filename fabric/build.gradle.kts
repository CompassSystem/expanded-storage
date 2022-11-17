plugins {
    id("ellemes.gradle.mod").apply(false)
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
}

val excludeFabric: (ModuleDependency) -> Unit = {
    it.exclude("net.fabricmc")
    it.exclude("net.fabricmc.fabric-api")
}

dependencies {
    modImplementation(mod.fabricApi().full())

    modCompileOnly("com.terraformersmc:modmenu:${project.properties["modmenu_version"]}") {
        excludeFabric(this)
    }

    include(modImplementation("ellemes:${properties["container_library_artifact"]}-fabric:${properties["container_library_version"]}", dependencyConfiguration = excludeFabric))

    // For chest module
    modCompileOnly(group = "curse.maven", name = "statement-340604", version = "3423826", dependencyConfiguration = excludeFabric)
    modCompileOnly(group = "curse.maven", name = "towelette-309338", version = "3398761", dependencyConfiguration = excludeFabric)
    modCompileOnly(group = "curse.maven", name = "carrier-409184", version = "3504375", dependencyConfiguration = excludeFabric)
    modCompileOnly(group = "io.github.onyxstudios.Cardinal-Components-API", name = "cardinal-components-base", version = "${properties["cardinal_version"]}", dependencyConfiguration = excludeFabric)
    modCompileOnly(group = "io.github.onyxstudios.Cardinal-Components-API", name = "cardinal-components-entity", version = "${properties["cardinal_version"]}", dependencyConfiguration = excludeFabric)
    //modRuntimeOnly(group = "net.devtech", name = "arrp", version = "0.4.2")

    //modRuntimeOnly("me.lucko:fabric-permissions-api:0.1-SNAPSHOT")
    modCompileOnly(group = "curse.maven", name = "htm-462534", version = "3539120", dependencyConfiguration = excludeFabric)
}

//
//configurations {
//    create("dev")
//}
//
//tasks.jar {
//    archiveClassifier.set("dev")
//}
//
//artifacts {
//    add("dev", tasks.jar.get().archiveFile) {
//        builtBy(tasks.jar)
//    }
//}

val u = ellemes.gradle.mod.api.publishing.UploadProperties(project, "https://gitlab.com/Ellemes/expanded-storage")

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
    }
}
