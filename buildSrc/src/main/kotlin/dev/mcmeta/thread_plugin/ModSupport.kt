package dev.mcmeta.thread_plugin

import org.gradle.api.artifacts.ModuleDependency
import org.gradle.kotlin.dsl.exclude

object ModVersions {
    val AMECS = "1.3.8+mc.1.18.2"
    val CCA = "5.0.2"
    val EMI = "0.4.3+1.18.2"
    val IPN = "1.3.5"
    val IPN_MINECRAFT = "1.18.2"
    val MODMENU = "3.2.3"
    val REI = "7.3.443"
}

val emptyAdditionalDependencyExclusions = fun ModuleDependency.() {

}

// todo: rework to allow different dependencies for compile / runtime (api compile only, full mod runtime only)
enum class ModSupport(
    vararg val dependencies: String,
    val block: ModuleDependency.() -> Unit = emptyAdditionalDependencyExclusions
) {
    AMECS(
        "de.siphalor:amecs-1.18:${ModVersions.AMECS}",
        "de.siphalor:nmuk-1.18:1.0.1+mc1.18-pre1",
        block = {
            exclude(group = "com.github.astei")
            exclude(group = "de.siphalor", module = "nmuk-1.18")
        }),
    CARRIER(
        "curse.maven:carrier-409184:3671184",
        "dev.onyxstudios.cardinal-components-api:cardinal-components-base:${ModVersions.CCA}",
        "dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${ModVersions.CCA}",
        "net.devtech:arrp:0.6.7"
    ),
    EMI("maven.modrinth:emi:${ModVersions.EMI}"),
    HTM("curse.maven:htm-462534:3539120"),
    INVENTORY_PROFILES(
        "maven.modrinth:inventory-profiles-next:fabric-${ModVersions.IPN_MINECRAFT}-${ModVersions.IPN}",
        "maven.modrinth:libipn:fabric-${ModVersions.IPN_MINECRAFT}-1.0.5",
        "net.fabricmc:fabric-language-kotlin:1.7.4+kotlin.1.6.21"
    ),
    INVENTORY_TABS("com.github.Andrew6rant:InventoryTabs:inventorytabs-0.8.1-1.18.x"),
    JEI("curse.maven:jei-238222:4060783"),
    MOD_MENU("com.terraformersmc:modmenu:${ModVersions.MODMENU}"),
    // todo: crashes game at runtime due to obfuscated mixins in jijs?
    REI("me.shedaniel:RoughlyEnoughItems-fabric:${ModVersions.REI}"),
    TOWELETTE(
        "maven.modrinth:statement:4.2.5+1.14.4-1.19.3",
        "maven.modrinth:towelette:5.0.0+1.14.4-1.19.3"
    );
}
