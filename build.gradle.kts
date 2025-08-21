import com.modrinth.minotaur.dependencies.ModDependency
import dev.lambdaurora.mcdev.api.McVersionLookup
import dev.lambdaurora.mcdev.api.ModUtils

plugins {
	alias(libs.plugins.loom)
	alias(libs.plugins.lambdamcdev)
	alias(libs.plugins.licenser)
	`java-library`
	`maven-publish`
	id("com.gradleup.shadow").version("8.3.3")
	id("com.modrinth.minotaur").version("2.+")
	id("com.matthewprenger.cursegradle").version("1.4.+")
}

base.archivesName.set(project.property("archives_base_name") as String)

val mcVersion = libs.versions.minecraft.get()
val VERSION = project.property("mod_version") as String
version = "$VERSION+$mcVersion"

// This field defines the Java version your mod target.
val targetJavaVersion = Integer.parseInt(project.property("java_version").toString())

repositories {
	mavenCentral()
	maven {
		name = "Gegy"
		url = uri("https://maven.gegy.dev/releases/")
		content {
			includeGroup("dev.lambdaurora")
		}
	}
	maven {
		name = "TerraformersMC"
		url = uri("https://maven.terraformersmc.com/")
		content {
			includeGroup("com.terraformersmc")
		}
	}
}

loom {
	accessWidenerPath.set(file("src/main/resources/lambdabettergrass.accesswidener"))
}

dependencies {
	minecraft(libs.minecraft)
	@Suppress("UnstableApiUsage")
	mappings(loom.layered {
		officialMojangMappings()
		mappings("dev.lambdaurora:yalmm:${mcVersion}+build.${libs.versions.mappings.yalmm.get()}")
	})
	modImplementation(libs.fabric.loader)
	modImplementation(libs.fabric.api)

	implementation(libs.yumi.commons.core)
	modImplementation(libs.spruceui)

	// Config
	modCompileOnly(libs.modmenu) {
		this.isTransitive = false
	}
	modLocalRuntime(libs.modmenu) {
		this.isTransitive = false
	}
	implementation(libs.nightconfig.core)
	implementation(libs.nightconfig.toml)

	// Bundling
	include(libs.yumi.commons.core)
	include(libs.spruceui)
	shadow(libs.nightconfig.core)
	shadow(libs.nightconfig.toml)
}

java {
	sourceCompatibility = JavaVersion.toVersion(targetJavaVersion)
	targetCompatibility = JavaVersion.toVersion(targetJavaVersion)

	withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.isDeprecation = true
	options.isIncremental = true
	options.release.set(targetJavaVersion)
}

tasks.processResources {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand("version" to inputs.properties["version"])
	}
	filesMatching("quilt.mod.json") {
		expand("version" to inputs.properties["version"])
	}
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${base.archivesName.get()}" }
	}
}

license {
	rule(file("metadata/HEADER"))
}

tasks.shadowJar {
	dependsOn(tasks.jar)
	configurations = listOf(project.configurations["shadow"])
	destinationDirectory.set(file("${project.layout.buildDirectory.get()}/devlibs"))
	archiveClassifier.set("dev")

	relocate("com.electronwill.nightconfig", "dev.lambdaurora.lambdabettergrass.shadow.nightconfig")

	from(rootProject.file("LICENSE")) {
		rename { "${it}_${base.archivesName.get()}" }
	}
}

tasks.remapJar {
	dependsOn(tasks.shadowJar)
}

modrinth {
	projectId.set(project.property("modrinth_id") as String)
	versionName.set("LambdaBetterGrass $VERSION (${McVersionLookup.getVersionTag(mcVersion)})")
	versionType.set(ModUtils.fetchVersionType(VERSION, mcVersion))
	uploadFile.set(tasks.remapJar)
	loaders.set(listOf("fabric", "quilt"))
	gameVersions.set(listOf(mcVersion))
	dependencies.set(listOf(
		ModDependency("P7dR8mSH", "required") // Fabric API
	))
	syncBodyFrom.set(
		ModUtils.parseReadme(
			project, "https://raw.githubusercontent.com/LambdAurora/lovely_snails/1.20/\$2"
		)
	)

	// Changelog fetching
	val changelogContent = ModUtils.fetchChangelog(project, VERSION)

	if (changelogContent != null) {
		changelog.set(changelogContent)
	} else {
		afterEvaluate {
			tasks.modrinth.get().isEnabled = false
		}
	}

	// If we don't have a MODRINTH_TOKEN, don't run the modrinth publish tasks.
	if (System.getenv("MODRINTH_TOKEN") == null) {
		project.logger.debug("MODRINTH_TOKEN is not set! Disabled modrinth and modrinthSyncBody tasks.")
		tasks.modrinth.get().isEnabled = false
		tasks.modrinthSyncBody.get().isEnabled = false
	}
}

/*curseforge {
	if (System.getenv("CURSEFORGE_TOKEN") != null) {
		apiKey = System.getenv("CURSEFORGE_TOKEN")
	} else { // If we don't have a CURSEFORGE_TOKEN, don't run the curseforge publish tasks.
		project.logger.debug("CURSEFORGE_TOKEN is not set! Disabled curseforge task.")
		tasks.curseforge.get().isEnabled = false
	}

	project {
		id = project.curseforge_id
		releaseType = this.getVersionType(mcVersion)
		addGameVersion(mcVersion)
		addGameVersion("Quilt")
		addGameVersion("Java 17")
		addGameVersion("Java 18")

		// Changelog fetching
		val changelogContent = fetchChangelog()

		if (changelogContent) {
			changelogType = "markdown"
			changelog = "Changelog:\n\n${changelogContent}"
		} else {
			afterEvaluate {
				uploadTask.setEnabled(false)
			}
		}

		mainArtifact(remapJar) {
			displayName = "LambdaBetterGrass $VERSION (${mcVersion})"

			relations {
				requiredDependency("qsl")
				optionalDependency("modmenu")
				incompatible("optifabric")
				incompatible("fabric-api")
			}
		}

		afterEvaluate {
			uploadTask.setGroup("publishing")
			uploadTask.dependsOn("remapJar")
		}
	}
}
tasks.curseforge.setGroup("publishing")*/

// Configure the maven publication.
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])

			pom {
				name.set("LambdaBetterGrass")
				description.set("Adds actual better grass to the game.")
			}
		}
	}

	repositories {
		mavenLocal()
		maven {
			name = "BuildDirLocal"
			url = uri("${rootProject.layout.buildDirectory.get()}/repo")
		}
	}
}
