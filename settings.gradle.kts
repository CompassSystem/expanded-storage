pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		maven {
			name = "Architectury"
			url = uri("https://maven.architectury.dev/")
		}
		mavenCentral()
		gradlePluginPortal()
		maven {
			name = "NeoForge"
			url = uri("https://maven.neoforged.net/releases/")
		}
	}
}

rootProject.name = "expanded-storage"

include("fabric")
