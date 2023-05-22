package dev.mcmeta.thread_plugin

import org.gradle.kotlin.dsl.DependencyHandlerScope

private const val COMPILE_CONFIGURATION: String = "modCompileOnly"
private const val COMPILE_API_CONFIGURATION: String = "modCompileOnlyApi"
private const val RUNTIME_CONFIGURATION: String = "modRuntimeOnly"

sealed class Mods {
    open fun addDependenciesToScope(platform: ModPlatform, adder: (String) -> Unit) {}

    open fun applyCompileDependencies(platform: ModPlatform, scope: DependencyHandlerScope) {
        addDependenciesToScope(platform) { scope.add(COMPILE_CONFIGURATION, it) }
    }

    open fun applyRuntimeDependencies(platform: ModPlatform, scope: DependencyHandlerScope) {
        addDependenciesToScope(platform) { scope.add(RUNTIME_CONFIGURATION, it) }
    }

    object Amecs : Mods() {
        override fun applyCompileDependencies(platform: ModPlatform, scope: DependencyHandlerScope) {
            if (platform.isThread()) {
                scope.add(COMPILE_CONFIGURATION, "de.siphalor:amecsapi-1.20:1.4.0+mc1.20-pre1")
            }
        }

        override fun applyRuntimeDependencies(platform: ModPlatform, scope: DependencyHandlerScope) {
            if (platform.isThread()) {
                scope.add(RUNTIME_CONFIGURATION, "de.siphalor:amecs-1.20:1.3.9+mc.1.20-pre2")
            }
        }
    }

    object Carrier : Mods() {
        private const val cardinalComponentsVersion = "5.0.2"

        override fun addDependenciesToScope(platform: ModPlatform, adder: (String) -> Unit) {
            if (platform.isThread()) {
                adder("curse.maven:carrier-409184:3873675")
                adder("dev.onyxstudios.cardinal-components-api:cardinal-components-base:$cardinalComponentsVersion")
                adder("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$cardinalComponentsVersion")
                adder("net.devtech:arrp:0.6.7")
            }
        }
    }

    object EMI : Mods() {
        override fun addDependenciesToScope(platform: ModPlatform, adder: (String) -> Unit) {
            if (platform.isThread()) {
                adder("maven.modrinth:emi:0.7.3+1.19.4")
            }
        }
    }

    object HeyThatsMine : Mods() {
        override fun addDependenciesToScope(platform: ModPlatform, adder: (String) -> Unit) {
            if (platform.isThread()) {
                adder("maven.modrinth:htm:v1.1.8")
            }
        }
    }

    object InventoryProfiles : Mods() {
        private const val libVersion = "3.0.0"
        private const val minecraftVersion = "1.19.4"
        private const val version = "1.10.1"

        override fun addDependenciesToScope(platform: ModPlatform, adder: (String) -> Unit) {
            val target = if (platform == ModPlatform.Common) ModPlatform.Fabric else platform.parent

            adder("maven.modrinth:inventory-profiles-next:$target-$minecraftVersion-$version")
            adder("maven.modrinth:libipn:$target-$minecraftVersion-$libVersion")

            if (platform == ModPlatform.Forge) {
                adder("maven.modrinth:kotlin-for-forge:4.2.0")
            } else if (platform.isThread()) {
                adder("net.fabricmc:fabric-language-kotlin:1.9.4+kotlin.1.8.21")
            }
        }
    }

    object JustEnoughItems : Mods() {
        private const val minecraftVersion = "1.19.2"
        private const val version = "11.5.0.297"

        override fun applyCompileDependencies(platform: ModPlatform, scope: DependencyHandlerScope) {
            scope.add(COMPILE_API_CONFIGURATION, "mezz.jei:jei-$minecraftVersion-common-api:$version")

            if (platform != ModPlatform.Common) {
                scope.add(COMPILE_API_CONFIGURATION, "mezz.jei:jei-$minecraftVersion-${platform.parent}-api:$version")
            }
        }

        override fun applyRuntimeDependencies(platform: ModPlatform, scope: DependencyHandlerScope) {
            if (platform != ModPlatform.Common) {
                scope.add(COMPILE_API_CONFIGURATION, "mezz.jei:jei-$minecraftVersion-${platform.parent}:$version")
            }
        }
    }

    object ModMenu : Mods() {
        override fun addDependenciesToScope(platform: ModPlatform, adder: (String) -> Unit) {
            if (platform.isThread()) {
                adder("com.terraformersmc:modmenu:7.0.0-beta.2")
            }
        }
    }

    object RoughlyEnoughItems : Mods() {
        private const val version = "11.0.599"

        override fun applyCompileDependencies(platform: ModPlatform, scope: DependencyHandlerScope) {
            val target = if (platform == ModPlatform.Common) "" else "-${platform.parent}"

            scope.add(COMPILE_CONFIGURATION, "me.shedaniel:RoughlyEnoughItems-api$target:$version")
        }

        override fun applyRuntimeDependencies(platform: ModPlatform, scope: DependencyHandlerScope) {
            scope.add(RUNTIME_CONFIGURATION, "me.shedaniel:RoughlyEnoughItems-${platform.parent}:$version")
        }
    }

    object Quark : Mods() {
        private const val version = "3.4-400.2879"

        override fun applyCompileDependencies(platform: ModPlatform, scope: DependencyHandlerScope) {
            if (platform == ModPlatform.Forge) {
                scope.add(COMPILE_CONFIGURATION, "vazkii.quark:Quark:$version")
            }
        }
    }

    object Towelette : Mods() {
        override fun addDependenciesToScope(platform: ModPlatform, adder: (String) -> Unit) {
            if (platform.isThread()) {
                adder("maven.modrinth:statement:4.2.5+1.14.4-1.19.3")
                adder("maven.modrinth:towelette:5.0.0+1.14.4-1.19.3")
            }
        }
    }
}
