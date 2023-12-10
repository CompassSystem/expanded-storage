import groovy.lang.MissingPropertyException
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dev.architectury.loom") version "1.4-SNAPSHOT"
    kotlin("jvm") version "1.9.21"
}

version = requiredProp("mod_version")
group = requiredProp("maven_group")
base.archivesName = requiredProp("archives_base_name")
val modIdentifier = requiredProp("mod_id")
val targetJdkVersion = JavaVersion.VERSION_17

loom {
    splitEnvironmentSourceSets()

    mods {
        create(modIdentifier) {
            sourceSet("main")
            sourceSet("client")
        }
    }

    accessWidenerPath = file("src/main/resources/expanded-storage.accesswidener")

    silentMojangMappingsLicense()
}

fabricApi {
    configureDataGeneration {
        modId = modIdentifier
        outputDirectory = file("src/generated/resources")
    }
}

repositories {
    exclusiveContent {
        forRepository {
            maven {
                url = uri("https://cursemaven.com")
            }
        }
        filter {
            includeGroup("curse.maven")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${requiredProp("minecraft_version")}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${requiredProp("fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${requiredProp("fabric_api")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${requiredProp("fabric_kotlin")}")

    modRuntimeOnly("curse.maven:variantbarrels-576766:4762229")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = targetJdkVersion.toInt()
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = targetJdkVersion.toInt().toString()
        }
    }

    jar {
        from("license")
    }

    processResources {
        inputs.properties(mutableMapOf(
            "version" to project.version
        ))

        filesMatching("fabric.mod.json") {
            expand(inputs.properties)
        }
    }

    afterEvaluate {
        all {
            if (name == "genSources" || name.startsWith("gen") && name.contains("Sources") && !name.contains("With")) {
                println("$name : ${name}WithVineflower")
                setDependsOn(setOf(named("${name}WithVineflower")))
            }
        }
    }
}

java {
    sourceCompatibility = targetJdkVersion
    targetCompatibility = targetJdkVersion
}

//#region // Utilities
fun requiredProp(name: String): String {
    return project.findProperty(name) as String? ?: throw MissingPropertyException(name)
}

fun JavaVersion.toInt() = ordinal + 1
//#endregion