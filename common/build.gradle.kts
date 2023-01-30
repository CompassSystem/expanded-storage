import dev.mcmeta.thread_plugin.ModVersions

dependencies {
    modCompileOnly("maven.modrinth:inventory-profiles-next:fabric-${ModVersions.IPN_MINECRAFT}-${ModVersions.IPN}") {
        exclude(group = "net.fabricmc")
        exclude(group = "net.fabricmc.fabric-api")
        exclude(group = "com.terraformersmc")
    }
}
