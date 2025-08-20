import com.modrinth.minotaur.dependencies.ModDependency

plugins {
	id("org.quiltmc.loom").version("1.2.+")
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

data class Module(val library: String, val module: String) {}

val qslModules: Set<Module> = setOf(
	Module("core", "crash_info"),
	Module("core", "lifecycle_events"),
	Module("core", "resource_loader"),
	Module("block", "block_extensions"),
	Module("gui", "screen"),
	Module("gui", "tooltip")
)
val fabricModules = setOf(
	"fabric-model-loading-api-v1",
	"fabric-renderer-api-v1",
	"fabric-renderer-indigo"
)
val runtimeModules = setOf(
	"fabric-lifecycle-events-v1",
	"fabric-key-binding-api-v1",
	"fabric-rendering-v1",
	"fabric-resource-loader-v0",
	"fabric-screen-api-v1"
)

// This field defines the Java version your mod target.
val targetJavaVersion = Integer.parseInt(project.property("java_version").toString())

fun isMCVersionNonRelease(mcVersion: String): Boolean {
	return mcVersion.matches(Regex("^\\d\\dw\\d\\d[a-z]$"))
			|| mcVersion.matches(Regex("\\d+\\.\\d+-(pre|rc)(\\d+)"))
}

fun getMCVersionString(mcVersion: String): String {
	if (isMCVersionNonRelease(mcVersion)) {
		return mcVersion
	}
	val version = mcVersion.split(".")
	return version[0] + "." + version[1]
}

fun getVersionType(mcVersion: String): String {
	return if (isMCVersionNonRelease(mcVersion) || mcVersion.contains("-alpha.")) {
		"alpha"
	} else if (mcVersion.contains("-beta.")) {
		"beta"
	} else {
		"release"
	}
}

fun parseReadme(): String {
	return ""
}

fun fetchChangelog(): String? {
	return null
}

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
	modImplementation("org.quiltmc:quilt-loader:${project.property("loader_version")}")

	qslModules.asSequence().map { "org.quiltmc.qsl.${it.library}:${it.module}:${project.property("qsl_version")}+${mcVersion}" }
		.forEach {
			modImplementation(it) {
				exclude(module = "quilt-loader")
			}
		}
	// Fabric API.
	fabricModules.asSequence().map { "org.quiltmc.quilted-fabric-api:${it}:${project.property("fabric_api_version")}-${mcVersion}" }
		.forEach {
			modImplementation(it) {
				exclude(module = "quilt-loader")
			}
		}
	runtimeModules.asSequence().map { "org.quiltmc.quilted-fabric-api:${it}:${project.property("fabric_api_version")}-${mcVersion}" }
		.forEach {
			modImplementation(it) {
				exclude(module = "quilt-loader")
			}
		}
	modLocalRuntime("org.quiltmc.qsl.entity:entity_events:${project.property("qsl_version")}+${mcVersion}") {
		exclude(module = "quilt-loader")
	}

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
	versionName.set("LambdaBetterGrass $VERSION (${getMCVersionString(mcVersion)})")
	versionType.set(if (isMCVersionNonRelease(mcVersion)) "beta" else "release")
	uploadFile.set(tasks.remapJar)
	loaders.set(listOf("quilt"))
	gameVersions.set(listOf(mcVersion))
	dependencies.set(listOf(
		ModDependency("qvIfYCYJ", "required")
	))
	syncBodyFrom.set(parseReadme())

	// Changelog fetching
	val changelogContent = fetchChangelog()

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
