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
	}
}
