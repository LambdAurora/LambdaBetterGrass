rootProject.name = "lambdabettergrass"

pluginManagement {
	repositories {
		gradlePluginPortal()
		maven {
			name = "Quilt"
			url = uri("https://maven.quiltmc.org/repository/release")
		}
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		maven {
			name = "Gegy"
			url = uri("https://maven.gegy.dev/releases/")
		}
	}
}
