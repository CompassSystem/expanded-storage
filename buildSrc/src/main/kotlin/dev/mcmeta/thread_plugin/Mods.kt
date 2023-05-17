package dev.mcmeta.thread_plugin

import org.gradle.kotlin.dsl.DependencyHandlerScope

private const val COMPILE_CONFIGURATION: String = "modCompileOnly"
private const val RUNTIME_CONFIGURATION: String = "modRuntimeOnly"

sealed class Mods {
    open fun addDependenciesToScope(adder: (String) -> Unit) {

    }

    open fun applyCompileDependencies(scope: DependencyHandlerScope) {
        addDependenciesToScope { scope.add(COMPILE_CONFIGURATION, it) }
    }

    open fun applyRuntimeDependencies(scope: DependencyHandlerScope) {
        addDependenciesToScope { scope.add(RUNTIME_CONFIGURATION, it) }
    }

    object Amecs : Mods() {
        override fun applyCompileDependencies(scope: DependencyHandlerScope) {
            scope.add("modCompileOnly", "de.siphalor:amecsapi-1.20:1.4.0+mc1.20-pre1")
        }

        override fun applyRuntimeDependencies(scope: DependencyHandlerScope) {

        }
    }

    object Carrier : Mods() {
        private const val cardinalComponentsVersion = "5.0.2"

        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("curse.maven:carrier-409184:3873675")
            adder("dev.onyxstudios.cardinal-components-api:cardinal-components-base:$cardinalComponentsVersion")
            adder("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$cardinalComponentsVersion")
            adder("net.devtech:arrp:0.6.7")
        }
    }

    object EMI : Mods() {
        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("maven.modrinth:emi:0.4.3+1.19")
        }
    }

    object HeyThatsMine : Mods() {
        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("curse.maven:htm-462534:3539120")
        }
    }

    object InventoryProfiles : Mods() {
        const val minecraftVersion = "1.19.2"
        const val version = "1.8.5"

        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("maven.modrinth:inventory-profiles-next:fabric-$minecraftVersion-$version")
            adder("maven.modrinth:libipn:fabric-$minecraftVersion-1.0.5")
            adder("net.fabricmc:fabric-language-kotlin:1.7.4+kotlin.1.6.21")
        }
    }

    object JustEnoughItems : Mods() {
        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("curse.maven:jei-238222:4371828")
        }
    }

    object ModMenu : Mods() {
        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("com.terraformersmc:modmenu:6.2.2")
        }
    }

    object RoughlyEnoughItems : Mods() {
        const val version = "9.1.587"

        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("me.shedaniel:RoughlyEnoughItems-fabric:$version")
        }
    }

    object Towelette : Mods() {
        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("maven.modrinth:statement:4.2.5+1.14.4-1.19.3")
            adder("maven.modrinth:towelette:5.0.0+1.14.4-1.19.3")
        }
    }
}
