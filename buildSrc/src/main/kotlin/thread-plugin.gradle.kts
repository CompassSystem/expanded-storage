import org.gradle.api.artifacts.ModuleDependency

repositories {
    maven { // Cardinal Components
        name = "Ladysnake maven"
        url = uri("https://ladysnake.jfrog.io/artifactory/mods")
        content {
            includeGroup("dev.onyxstudios.cardinal-components-api")
        }
    }
//    maven { // Advance Runtime Resource Pack
//        name = "Devan maven"
//        url = uri("https://raw.githubusercontent.com/Devan-Kerman/Devan-Repo/master/")
//        content {
//            includeGroup("net.devtech")
//        }
//    }
    exclusiveContent { // Mod Menu
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
    exclusiveContent {// Inventory Tabs
        forRepository {
            maven {
                name = "Jitpack Maven"
                url = uri("https://jitpack.io")
            }
        }
        filter {
            includeGroup("com.github.Andrew6rant")
        }
    }
//    maven { // Not Sure
//        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
//    }
    maven { // Roughly Enough Items
        name = "Shedaniel"
        url = uri("https://maven.shedaniel.me/")
    }
    maven { // Amecs
        name = "Siphalor's Maven"
        url = uri("https://maven.siphalor.de/")
    }
    maven { // FLAN
        name = "Flemmli97"
        url = uri("https://gitlab.com/api/v4/projects/21830712/packages/maven")
    }
}

val excludeFabric: (ModuleDependency) -> Unit = {
    it.exclude("net.fabricmc")
    it.exclude("net.fabricmc.fabric-api")
}

dependencies {
    "modCompileOnly"("com.terraformersmc:modmenu:${project.properties["modmenu_version"]}") {
        excludeFabric(this)
    }

    "modCompileOnly"(group = "curse.maven", name = "statement-340604", version = "3872814", dependencyConfiguration = excludeFabric)
    "modCompileOnly"(group = "curse.maven", name = "towelette-309338", version = "3398761", dependencyConfiguration = excludeFabric)
    "modCompileOnly"(group = "curse.maven", name = "carrier-409184", version = "3873675", dependencyConfiguration = excludeFabric)
    "modCompileOnly"(group = "dev.onyxstudios.cardinal-components-api", name = "cardinal-components-base", version = "${properties["cardinal_version"]}", dependencyConfiguration = excludeFabric)
    "modCompileOnly"(group = "dev.onyxstudios.cardinal-components-api", name = "cardinal-components-entity", version = "${properties["cardinal_version"]}", dependencyConfiguration = excludeFabric)
    "modCompileOnly"(group = "curse.maven", name = "htm-462534", version = "3539120", dependencyConfiguration = excludeFabric)

    "modCompileOnly"("me.shedaniel:RoughlyEnoughItems-api-fabric:${project.properties["rei_version"]}") {
        excludeFabric(this)
    }

    "modCompileOnly"("de.siphalor:amecs-1.19:${project.properties["amecs_version"]}") {
        excludeFabric(this)
        exclude(group = "com.github.astei")
    }

    "modCompileOnly"("io.github.flemmli97:flan:1.18.2-${project.properties["flan_version"]}:fabric-api") {
        excludeFabric(this)
        exclude(group = "curse.maven")
    }

    "modCompileOnly"("maven.modrinth:inventory-profiles-next:fabric-${project.properties["ipn_minecraft_version"]}-${project.properties["ipn_version"]}") {
        excludeFabric(this)
    }

    "modCompileOnly"("maven.modrinth:emi:${project.properties["emi_version"]}") {
        excludeFabric(this)
    }

    "modImplementation"("com.github.Andrew6rant:InventoryTabs:inventorytabs-0.8.1-1.19.x") {
        excludeFabric(this)
    }
}
