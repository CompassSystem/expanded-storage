import com.gitlab.ninjaphenix.gradle.api.task.MinifyJsonTask
import com.gitlab.ninjaphenix.gradle.api.task.ParamLocalObfuscatorTask
import org.gradle.jvm.tasks.Jar

plugins {
    alias(libs.plugins.gradleUtils)
    alias(libs.plugins.forgeGradle)
}

minecraft {
    mappings("official", "1.17.1")

    accessTransformer(file("src/common/resources/META-INF/accesstransformer.cfg")) // Currently, this location cannot be changed from the default.

    runs {
        create("client") {
            workingDirectory(rootProject.file("run"))
            mods {
                create("expandedstorage") {
                    source(sourceSets.main.get())
                }
            }
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            //property("forge.logging.markers", "REGISTRIES")

            // Recommended logging level for the console
            // You can set various levels here.
            // Please read: https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
            //property("forge.logging.console.level", "debug")
        }

        create("server") {
            workingDirectory(rootProject.file("run"))
            mods {
                create("expandedstorage") {
                    source(sourceSets.main.get())
                }
            }
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            //property("forge.logging.markers", "REGISTRIES")

            // Recommended logging level for the console
            // You can set various levels here.
            // Please read: https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
            //property("forge.logging.console.level", "debug")
        }
    }
}

repositories {
    //maven {
    //    // JEI maven
    //    name = "Progwml6 maven"
    //    url = uri("https://dvs1.progwml6.com/files/maven/")
    //}
    //maven {
    //    // JEI maven - fallback
    //    name = "ModMaven"
    //    url = uri("https://modmaven.k-4u.nl")
    //}
    mavenCentral()
    mavenLocal()
}

dependencies {
    minecraft(libs.minecraft.forge)
    val jei = (libs.jei.api as Provider<MinimalExternalModuleDependency>).get()
    compileOnly(fg.deobf("${jei.module.group}:${jei.module.name}:${jei.versionConstraint.displayName}"))
    implementation(libs.jetbrainAnnotations)
}

tasks.withType<ProcessResources> {
    val props = mutableMapOf("version" to properties["mod_version"]) // Needs to be mutable
    inputs.properties(props)
    filesMatching("META-INF/mods.toml") {
        expand(props)
    }
}

val jarTask = tasks.getByName<Jar>("jar") {
    archiveFileName.set("${properties["archivesBaseName"]}-${properties["mod_version"]}+${properties["minecraft_version"]}-fat.jar")
}

jarTask.finalizedBy("reobfJar")

val minifyJarTask = tasks.register<MinifyJsonTask>("minJar") {
    input.set(jarTask.outputs.files.singleFile)
    archiveFileName.set("${properties["archivesBaseName"]}-${properties["mod_version"]}+${properties["minecraft_version"]}-min.jar")
    dependsOn(jarTask)
}

val releaseJarTask = tasks.register<ParamLocalObfuscatorTask>("releaseJar") {
    input.set(minifyJarTask.get().outputs.files.singleFile)
    archiveFileName.set("${properties["archivesBaseName"]}-${properties["mod_version"]}+${properties["minecraft_version"]}.jar")
    from(rootDir.resolve("LICENSE"))
    dependsOn(minifyJarTask)
}

tasks.getByName("build") {
    dependsOn(releaseJarTask)
}