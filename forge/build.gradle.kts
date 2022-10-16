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
    maven {
        name = "Jared"
        url = uri("https://maven.blamejared.com/")
    }
}

dependencies {
    compileOnly("mezz.jei:jei-${properties["jei_minecraft_version"]}:${properties["jei_version"]}:api")
    modCompileOnly("maven.modrinth:inventory-profiles-next:forge-${properties["ipn_minecraft_version"]}-${properties["ipn_version"]}")
    modCompileOnly("vazkii.quark:Quark:3.3-test-367.2454")// todo: change version
}

tasks.getByName<MinifyJsonTask>("minJar") {
    manifest.attributes(mapOf(
            "Automatic-Module-Name" to "ellemes.expandedstorage",
            "MixinConfigs" to "expandedstorage-common.mixin.json,ellemes-container-library-forge.mixins.json"
    ))
}

val u = ellemes.gradle.mod.api.publishing.UploadProperties(project, "https://github.com/Ellemes/ExpandedStorage")

u.configureCurseForge {
}
