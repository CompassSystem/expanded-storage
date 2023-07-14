import compasses.idk_plugin.JsonNormalizerReader
import me.hypherionmc.cursegradle.CurseArtifact
import me.hypherionmc.cursegradle.CurseProject
import me.hypherionmc.cursegradle.CurseRelation
import net.fabricmc.loom.task.RemapJarTask
import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.kotlin.dsl.version
import java.nio.file.Files

plugins {
    `java-library`
    id("dev.architectury.loom") version "1.2-SNAPSHOT"
    id("io.github.juuxel.loom-vineflower") version "1.11.0"
    id("me.hypherionmc.cursegradle") version "2.0.1"
}

group = "compasses"
version = property("mod_version")!!

val minecraftVersion: String = property("minecraft_version") as String
val javaVersion: JavaVersion = (property("java_version") as String).let { JavaVersion.toVersion(it) }

val modId = property("mod_id") as String
val usesDatagen = findProperty("template.usesDataGen") == "true"
val producesReleaseArtifact = findProperty("template.producesReleaseArtifact") == "true"

loom {
    silentMojangMappingsLicense()

    findProperty("access_widener_path")?.let {
        accessWidenerPath = file(it)
    }

    mixin {
        defaultRefmapName = "$modId.refmap.json"
    }

    splitEnvironmentSourceSets()

    mods {
        create(modId) {
            sourceSet("main")
            sourceSet("client")
        }
    }
}

// <editor-fold desc="// Dependencies">
val compileScope = "modCompileOnly"
val compileApiScope = "modCompileOnlyApi"
val runtimeScope = "modRuntimeOnly"

val enabledMods = setOf<String>()
val compileDependencies = mutableMapOf<String, Map<String, List<String>>>()
val runtimeDependencies = mutableMapOf<String, Map<String, List<String>>>()

compileDependencies["amecs"] = mapOf(compileScope to listOf("de.siphalor:amecsapi-1.20:1.4.0+mc1.20-pre1"))
runtimeDependencies["amecs"] = mapOf(runtimeScope to listOf("de.siphalor:amecsapi-1.20:1.4.0+mc1.20-pre1"))

val cardinalComponentsVersion = "5.2.0"

compileDependencies["carrier"] = mapOf(
    compileScope to listOf(
        "curse.maven:carrier-409184:4605884",
        "dev.onyxstudios.cardinal-components-api:cardinal-components-base:$cardinalComponentsVersion",
        "dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$cardinalComponentsVersion",
        "net.devtech:arrp:0.6.7"
    )
)
runtimeDependencies["carrier"] = mapOf(
    runtimeScope to listOf(
        "curse.maven:carrier-409184:3873675",
        "dev.onyxstudios.cardinal-components-api:cardinal-components-base:$cardinalComponentsVersion",
        "dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$cardinalComponentsVersion",
        "net.devtech:arrp:0.6.7"
    )
)

val emiVersion = "1.0.5+1.20.1"

compileDependencies["emi"] = mapOf(
    compileApiScope to listOf(
        "dev.emi:emi-fabric:${emiVersion}:api"
    )
)
runtimeDependencies["emi"] = mapOf(
    runtimeScope to listOf(
        "dev.emi:emi-fabric:${emiVersion}"
    )
)

val htmVersion = "1.1.9"

compileDependencies["hey_thats_mine"] = mapOf(
    compileScope to listOf(
        "maven.modrinth:htm:$htmVersion"
    )
)
runtimeDependencies["hey_thats_mine"] = mapOf(
    runtimeScope to listOf(
        "maven.modrinth:htm:$htmVersion"
    )
)

compileDependencies["inventory_profiles"] = mapOf(
    compileScope to listOf(
        "maven.modrinth:inventory-profiles-next:fabric-1.20-1.10.6",
        "maven.modrinth:libipn:fabric-1.20-3.0.2",
        "net.fabricmc:fabric-language-kotlin:1.9.6+kotlin.1.8.22"
    )
)
runtimeDependencies["inventory_profiles"] = mapOf(
    runtimeScope to listOf(
        "maven.modrinth:inventory-profiles-next:fabric-1.20-1.10.6",
        "maven.modrinth:libipn:fabric-1.20-3.0.2",
        "net.fabricmc:fabric-language-kotlin:1.9.6+kotlin.1.8.22"
    )
)

val jeiMcVersion = "1.20.1"
val jeiVersion = "15.2.0.22"

compileDependencies["jei"] = mapOf(
    compileScope to listOf(
        "mezz.jei:jei-$jeiMcVersion-common-api:$jeiVersion",
        "mezz.jei:jei-$jeiMcVersion-fabric-api:$jeiVersion"
    )
)
runtimeDependencies["jei"] = mapOf(
    runtimeScope to listOf(
        "mezz.jei:jei-$jeiMcVersion-fabric:$jeiVersion"
    )
)

compileDependencies["modmenu"] = mapOf(
    compileScope to listOf(
        "com.terraformersmc:modmenu:7.0.1"
    )
)
runtimeDependencies["modmenu"] = mapOf(
    runtimeScope to listOf(
        "com.terraformersmc:modmenu:7.1.0"
    )
)

val reiVersion = "12.0.626"

compileDependencies["rei"] = mapOf(
    compileScope to listOf(
        "me.shedaniel:RoughlyEnoughItems-fabric:$reiVersion"
    )
)
runtimeDependencies["rei"] = mapOf(
    runtimeScope to listOf(
        "me.shedaniel:RoughlyEnoughItems-fabric:$reiVersion"
    )
)

compileDependencies["towelette"] = mapOf(
    compileScope to listOf(
        "maven.modrinth:statement:4.2.5+1.14.4-1.19.3",
        "maven.modrinth:towelette:5.0.0+1.14.4-1.19.3"
    )
)
runtimeDependencies["modmenu"] = mapOf(
    runtimeScope to listOf(
        "maven.modrinth:statement:4.2.5+1.14.4-1.19.3",
        "maven.modrinth:towelette:5.0.0+1.14.4-1.19.3"
    )
)
// </editor-fold>

repositories {
    maven { // Cardinal Components
        name = "Ladysnake maven"
        url = uri("https://maven.ladysnake.org/releases")
        content {
            includeGroup("dev.onyxstudios.cardinal-components-api")
        }
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "ARRP"
                url = uri("https://ueaj.dev/maven")
            }
        }
        filter {
            includeGroup("net.devtech")
        }
    }
    exclusiveContent { // Mod Menu
        forRepository {
            maven {
                name = "TerraformersMC"
                url = uri("https://maven.terraformersmc.com/")
            }
        }
        filter {
            includeGroup("com.terraformersmc")
            includeGroup("dev.emi")
        }
    }
//    exclusiveContent {// Inventory Tabs
//        forRepository {
//            maven {
//                name = "Jitpack Maven"
//                url = uri("https://jitpack.io")
//            }
//        }
//        filter {
//            includeGroup("com.github.Andrew6rant")
//        }
//    }
    maven { // Quark, JEI
        name = "Jared"
        url = uri("https://maven.blamejared.com/")
    }
    maven { // Roughly Enough Items
        name = "Shedaniel"
        url = uri("https://maven.shedaniel.me/")
    }
    maven { // Amecs
        name = "Siphalor's Maven"
        url = uri("https://maven.siphalor.de/")
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "Unnofficial Curseforge Maven"
                url = uri("https://cursemaven.com")
            }
        }

        filter {
            includeGroup("curse.maven")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth Maven"
                url = uri("https://api.modrinth.com/maven")
            }
        }

        filter {
            includeGroup("maven.modrinth")
        }
    }

    maven {
        name = "ParchmentMC Maven"
        url = uri("https://maven.parchmentmc.org")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")

    mappings(loom.layered {
        officialMojangMappings()

        findProperty("parchment_version")?.let {
            parchment("org.parchmentmc.data:parchment-${it}@zip")
        }
    })

    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    compileDependencies.forEach { (_, dependencies) ->
        dependencies.forEach {
            for (dependency in it.value) {
                add(it.key, dependency)
            }
        }
    }

    runtimeDependencies.forEach { (name, dependencies) ->
        if (name in enabledMods) {
            dependencies.forEach {
                for (dependency in it.value) {
                    add(it.key, dependency)
                }
            }
        }
    }

    compileOnly("org.jetbrains:annotations:24.0.1")
}

sourceSets {
    named("main") {
        if (usesDatagen) {
            resources.srcDir("src/generated/resources")
        }
    }
}

tasks {
    processResources {
        inputs.properties(mutableMapOf("version" to version))

        filesMatching("fabric.mod.json") {
            expand(inputs.properties)
        }

        exclude(".cache/*")
    }

    withType(JavaCompile::class.java).configureEach {
        options.encoding = "UTF-8"
        options.release = javaVersion.ordinal + 1
    }

    getByName<Jar>("jar") {
        from("LICENSE")

        if (usesDatagen) {
            exclude("**/datagen")
        }

        if (producesReleaseArtifact) {
            archiveClassifier = "dev"
        }
    }

    getByName<RemapJarTask>("remapJar") {
        if (producesReleaseArtifact) {
            injectAccessWidener = true

            archiveClassifier = "fat"
        }
    }

    create("minJar", Jar::class.java) {
        inputs.files(getByName("remapJar").outputs.files)

        duplicatesStrategy = DuplicatesStrategy.FAIL

        inputs.files.forEach {
            if (it.extension == "jar") {
                this.from(zipTree(it)) {
                    exclude("**/MANIFEST.MF")
                }
            }
        }

        filesMatching(listOf("**/*.json", "**/*.mcmeta")) {
            filter(JsonNormalizerReader::class.java)
        }

        dependsOn("remapJar")
    }

    build.get().dependsOn("minJar")
}

val modVersion = version as String

fun getGitCommit(): String {
    return ProcessGroovyMethods.getText(ProcessGroovyMethods.execute("git rev-parse HEAD"))
}

fun getFileContents(file: java.nio.file.Path): String {
    return Files.readString(file).replace("\r\n", "\n")
}

val modChangelog = buildString {
    append(getFileContents(rootDir.toPath().resolve("changelog.md")))
    append("\nCommit: ${property("repo_base_url")!!}/commit/${getGitCommit()}")
}

curseforge {
    curseGradleOptions.apply {
        debug = System.getProperty("MOD_UPLOAD_DEBUG", "false") == "true"
        javaVersionAutoDetect = false
        javaIntegration = false
        forgeGradleIntegration = false
        fabricIntegration = false
        detectFabricApi = false
    }

    project(closureOf<CurseProject> {
        apiKey = System.getenv("CURSEFORGE_TOKEN")
        id  = project.property("curseforge_project_id")
        releaseType = if (modVersion.contains("alpha")) "alpha" else if (modVersion.contains("beta")) "beta" else "release"

        val minJar = project.tasks.getByName("minJar")

        mainArtifact(minJar, closureOf<CurseArtifact> {
            displayName = "Fabric/Quilt $modVersion"
            artifact = minJar
        })

        changelogType = "markdown"
        changelog = modChangelog

        gameVersionStrings = buildList {
            add("Fabric")
            add("Quilt")
            add("Java " + project.extensions.getByType(JavaPluginExtension::class.java).targetCompatibility.majorVersion)
            add(minecraftVersion)
        }

        relations(closureOf<CurseRelation> {
            requiredDependency("fabric-api")
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
    })
}
