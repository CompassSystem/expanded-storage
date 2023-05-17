import dev.mcmeta.thread_plugin.Mods

repositories {
    maven { // Cardinal Components
        name = "Ladysnake maven"
        url = uri("https://ladysnake.jfrog.io/artifactory/mods")
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

// Note: when changing this you will likely need to stop any gradle deamons and delete the root .gradle folder.
val enabledMods = setOf<Mods>()

dependencies {
    Mods::class.nestedClasses.forEach {
        val mod = it.objectInstance as Mods

        mod.applyCompileDependencies(this)
        if (mod in enabledMods) {
            mod.applyRuntimeDependencies(this)
        }
    }
}
