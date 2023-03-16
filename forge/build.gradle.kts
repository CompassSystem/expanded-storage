import dev.mcmeta.thread_plugin.ModVersions
import ellemes.gradle.mod.api.task.AbstractJsonTask

plugins {
    id("ellemes.gradle.mod").apply(false)
}

loom {
    forge {
        mixinConfig("expandedstorage-forge.mixins.json")
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
    maven { // Roughly Enough Items
        name = "Shedaniel"
        url = uri("https://maven.shedaniel.me/")
    }
}

dependencies {
    compileOnly("mezz.jei:jei-${properties["jei_minecraft_version"]}-forge-api:${properties["jei_version"]}")
    modCompileOnly("maven.modrinth:inventory-profiles-next:forge-${ModVersions.IPN_MINECRAFT}-${ModVersions.IPN}")
    modCompileOnly("vazkii.quark:Quark:3.3-373.2529")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-forge:${ModVersions.REI}")
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
