import dev.mcmeta.thread_plugin.Mods

dependencies {
    modCompileOnly("maven.modrinth:inventory-profiles-next:fabric-${Mods.InventoryProfiles.minecraftVersion}-${Mods.InventoryProfiles.version}") {
        exclude(group = "net.fabricmc")
        exclude(group = "net.fabricmc.fabric-api")
        exclude(group = "com.terraformersmc")
    }

    Mods.JustEnoughItems.applyCompileDependencies(this)
}
