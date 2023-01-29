import dev.mcmeta.thread_plugin.ModVersions

repositories {
    maven {
        url = uri("https://maven.saps.dev/minecraft")
        content {
            includeGroup("dev.ftb.mods")
        }
    }
}

dependencies {
    modCompileOnly("maven.modrinth:inventory-profiles-next:fabric-${ModVersions.IPN_MINECRAFT}-${ModVersions.IPN}") {
        exclude(group = "net.fabricmc")
        exclude(group = "net.fabricmc.fabric-api")
        exclude(group = "com.terraformersmc")
    }

    modCompileOnly("dev.ftb.mods:ftb-chunks:1902.3.13-build.207") {
        exclude(group = "net.fabricmc")
    }
}
