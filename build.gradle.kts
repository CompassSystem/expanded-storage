import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseRelation
import com.modrinth.minotaur.request.VersionType

buildscript {
    dependencies {
        classpath(group = "com.google.guava", name = "guava", version = "30.1.1-jre") {
            because("Required by loom, conflicts with curse gradle.")
        }
        classpath(group = "com.google.code.gson", name = "gson", version = "2.8.8") {
            because("Required by loom, conflicts with curse gradle")
        }
    }
}

plugins {
    id("fabric-loom").version("0.11.29").apply(false)
    id("net.minecraftforge.gradle").version("5.1.26").apply(false)
    id("org.spongepowered.mixin").version("0.7-SNAPSHOT").apply(false)
    id("ninjaphenix.gradle-utils").version("0.1.0-beta.7").apply(false)
    id("ninjaphenix.gradle.mod").version("0.0.12")
    id("com.modrinth.minotaur").version("1.2.1")
    id("com.matthewprenger.cursegradle").version("1.4.0")
}

val forgeProject = findProject(":forge")
val fabricProject = findProject(":fabric")

var modrinthForgeTask : TaskProvider<com.modrinth.minotaur.TaskModrinthUpload>? = null
var modrinthFabricTask : TaskProvider<com.modrinth.minotaur.TaskModrinthUpload>? = null

var curseforgeForgeTask : TaskProvider<com.matthewprenger.cursegradle.CurseUploadTask>? = null
var curseforgeFabricTask : TaskProvider<com.matthewprenger.cursegradle.CurseUploadTask>? = null

val realChangelog = rootDir.resolve("changelog.md").readText(Charsets.UTF_8)
val modrinthToken: String? = System.getenv("MODRINTH_TOKEN")
val curseforgeToken: String? = System.getenv("CURSEFORGE_TOKEN")

val extraGameVersions = (properties["extra_game_versions"] as String).split(",")

// todo: needs reworking as game version is now in gradle plugin
if (modrinthToken != null) {
    if (forgeProject != null) {
        modrinthForgeTask = tasks.register<com.modrinth.minotaur.TaskModrinthUpload>("publishModrinthForge") {
            val releaseJarTask = forgeProject.tasks.getByName("minJar")
            dependsOn(releaseJarTask)

            detectLoaders = false
            changelog = realChangelog
            token = modrinthToken
            projectId = properties["modrinth_project_id"] as String
            versionName = "Forge ${properties["mod_version"]}+${properties["minecraft_version"]}"
            versionNumber = "${properties["mod_version"]}+${properties["minecraft_version"]}-forge"
            versionType = VersionType.RELEASE
            uploadFile = releaseJarTask
            addGameVersion(properties["minecraft_version"] as String)
            extraGameVersions.forEach {
                addGameVersion(it)
            }
            addLoader("forge")
        }
    }

    if (fabricProject != null) {
        modrinthFabricTask = tasks.register<com.modrinth.minotaur.TaskModrinthUpload>("publishModrinthFabric") {
            val releaseJarTask = fabricProject.tasks.getByName("minJar")
            dependsOn(releaseJarTask)
            if (modrinthForgeTask != null) {
                mustRunAfter(modrinthForgeTask)
            }

            detectLoaders = false
            changelog = realChangelog
            token = modrinthToken
            projectId = properties["modrinth_project_id"] as String
            versionName = "Fabric ${properties["mod_version"]}+${properties["minecraft_version"]}"
            versionNumber = "${properties["mod_version"]}+${properties["minecraft_version"]}-fabric"
            versionType = VersionType.RELEASE
            uploadFile = releaseJarTask
            addGameVersion(properties["minecraft_version"] as String)
            extraGameVersions.forEach {
                addGameVersion(it)
            }
            addLoader("fabric")
        }
    }
}

if (curseforgeToken != null) {
    var gameVersion = properties["minecraft_version"] as String
    if ("w" in gameVersion || "rc" in gameVersion) {
        gameVersion = "1.18-Snapshot"
    }

    if (forgeProject != null) {
        curseforgeForgeTask = tasks.register<com.matthewprenger.cursegradle.CurseUploadTask>("publishCurseforgeForge") {
            val releaseJarTask = forgeProject.tasks.getByName("minJar")
            dependsOn(releaseJarTask)

            apiKey = curseforgeToken
            projectId = properties["curseforge_forge_project_id"] as String
            mainArtifact = CurseArtifact().apply {
                artifact = releaseJarTask
                changelogType = "markdown"
                changelog = realChangelog
                displayName = "[${properties["minecraft_version"]}] ${properties["mod_version"]}"
                releaseType = "release"
                gameVersionStrings = listOf(gameVersion, "Forge", "Java ${properties["mod_java_version"]}") + extraGameVersions
                curseRelations = CurseRelation().apply {
                    requiredDependency("ninjaphenixs-container-library")
                }
            }
            additionalArtifacts = listOf()
        }
    }

    if (fabricProject != null) {
        curseforgeFabricTask = tasks.register<com.matthewprenger.cursegradle.CurseUploadTask>("publishCurseforgeFabric") {
            val releaseJarTask = fabricProject.tasks.getByName("minJar")
            dependsOn(releaseJarTask)
            if (curseforgeForgeTask != null) {
                mustRunAfter(curseforgeForgeTask)
            }

            apiKey = curseforgeToken
            projectId = properties["curseforge_fabric_project_id"] as String
            mainArtifact = CurseArtifact().apply {
                artifact = releaseJarTask
                changelogType = "markdown"
                changelog = realChangelog
                displayName = "[${properties["minecraft_version"]}] ${properties["mod_version"]}"
                releaseType = "release"
                gameVersionStrings = listOf(gameVersion, "Fabric", "Java ${properties["mod_java_version"]}") + extraGameVersions
                curseRelations = CurseRelation().apply {
                    requiredDependency("fabric-api")
                    requiredDependency("ninjaphenixs-container-library")
                    optionalDependency("carrier")
                    optionalDependency("towelette")
                    optionalDependency("htm")
                }
            }
            additionalArtifacts = listOf()
        }
    }
}

val publishTask = tasks.create("publish") {
    listOf(modrinthForgeTask, modrinthFabricTask, curseforgeForgeTask, curseforgeFabricTask).forEach {
        if (it != null) {
            this.dependsOn(it)
            this.mustRunAfter(it)
        }
    }
}
