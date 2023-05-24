package dev.mcmeta.thread_plugin

import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.exclude

private const val COMPILE_CONFIGURATION: String = "modCompileOnly"
private const val COMPILE_API_CONFIGURATION: String = "modCompileOnlyApi"
private const val RUNTIME_CONFIGURATION: String = "modRuntimeOnly"

sealed class Mods(val platform: ModPlatform, val helper: DependencyHelper) {
    open fun addDependenciesToScope(adder: (String) -> Unit) {}

    open fun applyCompileDependencies() {
        addDependenciesToScope { helper.add(COMPILE_CONFIGURATION, it) }
    }

    open fun applyRuntimeDependencies() {
        addDependenciesToScope { helper.add(RUNTIME_CONFIGURATION, it) }
    }

    class Amecs(platform: ModPlatform, helper: DependencyHelper) : Mods(platform, helper) {
        override fun applyCompileDependencies() {
            if (platform.isThread()) {
                helper.add(COMPILE_CONFIGURATION, "de.siphalor:amecsapi-1.20:1.4.0+mc1.20-pre1")
            }
        }

        override fun applyRuntimeDependencies() {
            if (platform.isThread()) {
                helper.add(RUNTIME_CONFIGURATION, "de.siphalor:amecs-1.20:1.3.9+mc.1.20-pre2")
            }
        }
    }

    class Carrier(platform: ModPlatform, helper: DependencyHelper) : Mods(platform, helper) {
        private val cardinalComponentsVersion = "5.0.2"

        override fun addDependenciesToScope(adder: (String) -> Unit) {
            if (platform.isThread()) {
                adder("curse.maven:carrier-409184:3873675")
                adder("dev.onyxstudios.cardinal-components-api:cardinal-components-base:$cardinalComponentsVersion")
                adder("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$cardinalComponentsVersion")
                adder("net.devtech:arrp:0.6.7")
            }
        }
    }

    class EMI(platform: ModPlatform, helper: DependencyHelper) : Mods(platform, helper) {
        override fun addDependenciesToScope(adder: (String) -> Unit) {
            if (platform.isThread()) {
                adder("maven.modrinth:emi:0.7.3+1.19.4")
            }
        }
    }

    class HeyThatsMine(platform: ModPlatform, helper: DependencyHelper) : Mods(platform, helper) {
        override fun addDependenciesToScope(adder: (String) -> Unit) {
            if (platform.isThread()) {
                adder("maven.modrinth:htm:v1.1.8")
            }
        }
    }

    class InventoryProfiles(platform: ModPlatform, helper: DependencyHelper) : Mods(platform, helper) {
        private val libVersion = "3.0.0"
        private val minecraftVersion = "1.19.4"
        private val version = "1.10.1"

        override fun addDependenciesToScope(adder: (String) -> Unit) {
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

    class JustEnoughItems(platform: ModPlatform, helper: DependencyHelper) : Mods(platform, helper) {
        private val minecraftVersion = "1.19.2"
        private val version = "11.5.0.297"

        override fun applyCompileDependencies() {
            helper.add(COMPILE_API_CONFIGURATION, "mezz.jei:jei-$minecraftVersion-common-api:$version")

            if (platform != ModPlatform.Common) {
                helper.add(COMPILE_API_CONFIGURATION, "mezz.jei:jei-$minecraftVersion-${platform.parent}-api:$version")
            }
        }

        override fun applyRuntimeDependencies() {
            if (platform != ModPlatform.Common) {
                helper.add(COMPILE_API_CONFIGURATION, "mezz.jei:jei-$minecraftVersion-${platform.parent}:$version")
            }
        }
    }

    class ModMenu(platform: ModPlatform, helper: DependencyHelper) : Mods(platform, helper) {
        override fun addDependenciesToScope(adder: (String) -> Unit) {
            if (platform.isThread()) {
                adder("com.terraformersmc:modmenu:7.0.0-beta.2")
            }
        }
    }

    class RoughlyEnoughItems(platform: ModPlatform, helper: DependencyHelper) : Mods(platform, helper) {
        private val version = "11.0.599"

        override fun applyCompileDependencies() {
            val target = if (platform == ModPlatform.Common) "" else "-${platform.parent}"

            helper.add(COMPILE_CONFIGURATION, "me.shedaniel:RoughlyEnoughItems-api$target:$version") {
                isTransitive = true
            }
        }

        override fun applyRuntimeDependencies() {
            helper.add(RUNTIME_CONFIGURATION, "me.shedaniel:RoughlyEnoughItems-${platform.parent}:$version") {
                isTransitive = true
            }
        }
    }

    class Quark(platform: ModPlatform, helper: DependencyHelper) : Mods(platform, helper) {
        private val version = "3.4-400.2879"

        override fun applyCompileDependencies() {
            if (platform == ModPlatform.Forge) {
                helper.add(COMPILE_CONFIGURATION, "vazkii.quark:Quark:$version")
            }
        }
    }

    class Towelette(platform: ModPlatform, helper: DependencyHelper) : Mods(platform, helper) {
        override fun addDependenciesToScope(adder: (String) -> Unit) {
            if (platform.isThread()) {
                adder("maven.modrinth:statement:4.2.5+1.14.4-1.19.3")
                adder("maven.modrinth:towelette:5.0.0+1.14.4-1.19.3")
            }
        }
    }
}
