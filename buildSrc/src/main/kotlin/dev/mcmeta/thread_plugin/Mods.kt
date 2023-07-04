package dev.mcmeta.thread_plugin

private const val COMPILE_CONFIGURATION: String = "modCompileOnly"
private const val COMPILE_API_CONFIGURATION: String = "modCompileOnlyApi"
private const val RUNTIME_CONFIGURATION: String = "modRuntimeOnly"

sealed class Mods(val helper: DependencyHelper) {
    open fun addDependenciesToScope(adder: (String) -> Unit) {}

    open fun applyCompileDependencies() {
        addDependenciesToScope { helper.add(COMPILE_CONFIGURATION, it) }
    }

    open fun applyRuntimeDependencies() {
        addDependenciesToScope { helper.add(RUNTIME_CONFIGURATION, it) }
    }

    class Amecs(helper: DependencyHelper) : Mods(helper) {
        override fun applyCompileDependencies() {
            helper.add(COMPILE_CONFIGURATION, "de.siphalor:amecsapi-1.20:1.4.0+mc1.20-pre1")
        }

        override fun applyRuntimeDependencies() {
            helper.add(RUNTIME_CONFIGURATION, "de.siphalor:amecs-1.20:1.3.9+mc.1.20-pre2")
        }
    }

    class Carrier(helper: DependencyHelper) : Mods(helper) {
        private val cardinalComponentsVersion = "5.2.0"

        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("curse.maven:carrier-409184:3873675")
            adder("dev.onyxstudios.cardinal-components-api:cardinal-components-base:$cardinalComponentsVersion")
            adder("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$cardinalComponentsVersion")
            adder("net.devtech:arrp:0.6.7")
        }
    }

    class EMI(helper: DependencyHelper) : Mods(helper) {
        private val version = "1.0.1+1.19.4"

        override fun applyCompileDependencies() {
            helper.add(COMPILE_API_CONFIGURATION, "dev.emi:emi-fabric:${version}:api")
        }

        override fun applyRuntimeDependencies() {
            helper.add(RUNTIME_CONFIGURATION, "dev.emi:emi-fabric:${version}")
        }
    }

    class HeyThatsMine(helper: DependencyHelper) : Mods(helper) {
        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("maven.modrinth:htm:v1.1.8")
        }
    }

    class InventoryTabs(helper: DependencyHelper) : Mods(helper) {
        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("com.github.Andrew6rant:InventoryTabs:inventorytabs-0.8.1-1.19.x")
        }
    }

    class InventoryProfiles(helper: DependencyHelper) : Mods(helper) {
        private val libVersion = "3.0.1"
        private val minecraftVersion = "1.20-pre4"
        private val version = "1.10.2"

        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("maven.modrinth:inventory-profiles-next:fabric-$minecraftVersion-$version")
            adder("maven.modrinth:libipn:fabric-1.20-pre2-$libVersion")

            adder("net.fabricmc:fabric-language-kotlin:1.9.4+kotlin.1.8.21")
        }
    }

    class JustEnoughItems(helper: DependencyHelper) : Mods(helper) {
        private val minecraftVersion = "1.19.4"
        private val version = "13.1.0.11"

        override fun applyCompileDependencies() {
            helper.add(COMPILE_API_CONFIGURATION, "mezz.jei:jei-$minecraftVersion-common-api:$version")

            helper.add(COMPILE_API_CONFIGURATION, "mezz.jei:jei-$minecraftVersion-fabric-api:$version")
        }

        override fun applyRuntimeDependencies() {
            helper.add(COMPILE_API_CONFIGURATION, "mezz.jei:jei-$minecraftVersion-fabric:$version")
        }
    }

    class ModMenu(helper: DependencyHelper) : Mods(helper) {
        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("com.terraformersmc:modmenu:7.0.1")
        }
    }

    class RoughlyEnoughItems(helper: DependencyHelper) : Mods(helper) {
        private val version = "11.0.617"

        override fun applyCompileDependencies() {
            helper.add(COMPILE_CONFIGURATION, "me.shedaniel:RoughlyEnoughItems-fabric:$version") {
                isTransitive = true
                exclude(mapOf("group" to "net.fabricmc"))
                exclude(mapOf("group" to "net.fabricmc.fabric-api"))
            }
        }

        override fun applyRuntimeDependencies() {
            helper.add(RUNTIME_CONFIGURATION, "me.shedaniel:RoughlyEnoughItems-fabric:$version") {
                isTransitive = true
                exclude(mapOf("group" to "net.fabricmc"))
                exclude(mapOf("group" to "net.fabricmc.fabric-api"))
            }
        }
    }

    class Towelette(helper: DependencyHelper) : Mods(helper) {
        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("maven.modrinth:statement:4.2.5+1.14.4-1.19.3")
            adder("maven.modrinth:towelette:5.0.0+1.14.4-1.19.3")
        }
    }
}
