package dev.mcmeta.thread_plugin

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
        private val cardinalComponentsVersion = "5.2.0"

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
        private val version = "1.0.1+1.19.4"

        override fun applyCompileDependencies() {
            when (platform) {
                ModPlatform.Common -> helper.add(COMPILE_API_CONFIGURATION, "dev.emi:emi-xplat-intermediary:${version}:api")
                ModPlatform.Thread, ModPlatform.Fabric, ModPlatform.Quilt -> helper.add(COMPILE_API_CONFIGURATION, "dev.emi:emi-fabric:${version}:api")
                ModPlatform.Forge -> helper.add(COMPILE_API_CONFIGURATION, "dev.emi:emi-forge:${version}:api")
            }
        }

        override fun applyRuntimeDependencies() {
            if (platform.isThread()) {
                helper.add(RUNTIME_CONFIGURATION, "dev.emi:emi-fabric:${version}")
            } else if (platform == ModPlatform.Forge) {
                helper.add(RUNTIME_CONFIGURATION, "dev.emi:emi-forge:${version}")
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

    class InventoryTabs(platform: ModPlatform, helper: DependencyHelper) : Mods(platform, helper) {
        override fun addDependenciesToScope(adder: (String) -> Unit) {
            adder("com.github.Andrew6rant:InventoryTabs:inventorytabs-0.8.1-1.19.x")
        }
    }

    class InventoryProfiles(platform: ModPlatform, helper: DependencyHelper) : Mods(platform, helper) {
        private val libVersion = "3.0.1"
        private val minecraftVersion = "1.20-pre4"
        private val version = "1.10.2"

        override fun addDependenciesToScope(adder: (String) -> Unit) {
            val target = if (platform == ModPlatform.Common) ModPlatform.Fabric else platform.parent

            adder("maven.modrinth:inventory-profiles-next:$target-$minecraftVersion-$version")
//            adder("maven.modrinth:libipn:$target-$minecraftVersion-$libVersion")
            adder("maven.modrinth:libipn:$target-1.20-pre2-$libVersion")

            if (platform == ModPlatform.Forge) {
                adder("maven.modrinth:kotlin-for-forge:4.2.0")
            } else if (platform == ModPlatform.Thread || platform == ModPlatform.Fabric) {
                adder("net.fabricmc:fabric-language-kotlin:1.9.4+kotlin.1.8.21")
            } else if (platform == ModPlatform.Quilt) {
                adder("org.quiltmc.quilt-kotlin-libraries:quilt-kotlin-libraries:2.0.2+kt.1.8.20+flk.1.9.3")
            }
        }
    }

    class JustEnoughItems(platform: ModPlatform, helper: DependencyHelper) : Mods(platform, helper) {
        private val minecraftVersion = "1.19.4"
        private val version = "13.1.0.11"

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
        private val version = "11.0.617"

        override fun applyCompileDependencies() {
            if (platform == ModPlatform.Common) {
                helper.add(COMPILE_CONFIGURATION, "me.shedaniel:RoughlyEnoughItems-api:$version") {
                    isTransitive = true
                    exclude(mapOf("group" to "net.fabricmc"))
                    exclude(mapOf("group" to "net.fabricmc.fabric-api"))
                }
            } else {
                helper.add(COMPILE_CONFIGURATION, "me.shedaniel:RoughlyEnoughItems-${platform.parent}:$version") {
                    isTransitive = true
                    exclude(mapOf("group" to "net.fabricmc"))
                    exclude(mapOf("group" to "net.fabricmc.fabric-api"))
                }
            }
        }

        override fun applyRuntimeDependencies() {
            if (platform != ModPlatform.Common) {
                helper.add(RUNTIME_CONFIGURATION, "me.shedaniel:RoughlyEnoughItems-${platform.parent}:$version") {
                    isTransitive = true
                    exclude(mapOf("group" to "net.fabricmc"))
                    exclude(mapOf("group" to "net.fabricmc.fabric-api"))
                }
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
