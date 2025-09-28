import com.modrinth.minotaur.dependencies.ModDependency
import dev.lambdaurora.mcdev.api.McVersionLookup
import dev.lambdaurora.mcdev.api.ModUtils
import dev.lambdaurora.mcdev.api.ModVersionDependency
import dev.lambdaurora.mcdev.task.packaging.PackageModrinthTask
import net.darkhax.curseforgegradle.TaskPublishCurseForge

plugins {
	alias(libs.plugins.loom)
	alias(libs.plugins.lambdamcdev)
	alias(libs.plugins.licenser)
	`java-library`
	`maven-publish`
	id("com.gradleup.shadow").version("9.1.0")
	id("com.modrinth.minotaur").version("2.+")
	id("net.darkhax.curseforgegradle").version("1.1.+")
}

base.archivesName.set(project.property("archives_base_name") as String)

val mcVersion = libs.versions.minecraft.get()
val compatibleMcVersions: Set<String> = setOf("1.21")
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

fabricApi {
	configureDataGeneration() {
		client = true
	}
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

	modImplementation(libs.yumi.mc.foundation)
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
	include(libs.yumi.mc.foundation)
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
		expand("version" to (inputs.properties["version"] as String))
	}
	filesMatching("quilt.mod.json") {
		expand("version" to (inputs.properties["version"] as String))
	}

	exclude(".cache/**")
}

tasks.jar {
	inputs.property("archivesName", base.archivesName)

	from("LICENSE") {
		rename { "${it}_${inputs.properties["archivesName"]}" }
	}
}

license {
	rule(file("metadata/HEADER"))
}

tasks.shadowJar {
	dependsOn(tasks.jar)
	inputs.property("archivesName", base.archivesName)

	configurations = listOf(project.configurations["shadow"])
	destinationDirectory.set(file("${project.layout.buildDirectory.get()}/devlibs"))
	archiveClassifier.set("dev")

	relocate("com.electronwill.nightconfig", "dev.lambdaurora.lambdabettergrass.shadow.nightconfig")

	from(rootProject.file("LICENSE")) {
		rename { "${it}_${inputs.properties["archivesName"]}" }
	}
}

tasks.remapJar {
	dependsOn(tasks.shadowJar)
}

val README = ModUtils.parseReadme(
	project, "https://raw.githubusercontent.com/LambdAurora/LambdaBetterGrass/1.21/\$2"
)
val CHANGELOG_CONTENT = ModUtils.fetchChangelog(project, VERSION)

val packageModrinth by tasks.registering(PackageModrinthTask::class) {
	this.group = "publishing"
	this.versionType.set(ModUtils.getVersionType(VERSION, mcVersion))
	this.versionName.set("LambdaBetterGrass $VERSION (${McVersionLookup.getVersionTag(mcVersion)})")
	this.gameVersions.set(listOf(mcVersion) + compatibleMcVersions)
	this.loaders.set(listOf("fabric", "quilt"))
	this.dependencies.set(
		listOf(
			ModVersionDependency("P7dR8mSH", ModVersionDependency.Type.REQUIRED), // Fabric API
		)
	)
	this.changelog.set(CHANGELOG_CONTENT)
	this.readme.set(README)
	this.files.setFrom(tasks.remapJar.get())
}

modrinth {
	projectId.set(project.property("modrinth_id") as String)
	versionName.set("LambdaBetterGrass $VERSION (${McVersionLookup.getVersionTag(mcVersion)})")
	versionType.set(ModUtils.fetchVersionType(VERSION, mcVersion))
	uploadFile.set(tasks.remapJar)
	loaders.set(listOf("fabric", "quilt"))
	gameVersions.set(listOf(mcVersion) + compatibleMcVersions)
	dependencies.set(
		listOf(
			ModDependency("P7dR8mSH", "required") // Fabric API
		)
	)
	syncBodyFrom.set(README)

	// Changelog fetching
	if (CHANGELOG_CONTENT != null) {
		changelog.set(CHANGELOG_CONTENT)
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

tasks.register<TaskPublishCurseForge>("curseforge") {
	this.group = "publishing"

	val token = System.getenv("CURSEFORGE_TOKEN")
	if (token != null) {
		this.apiToken = token
	} else {
		this.isEnabled = false
		return@register
	}

	// Changelog fetching
	var changelogContent = CHANGELOG_CONTENT

	if (changelogContent != null) {
		changelogContent = "Changelog:\n\n${changelogContent}"
	} else {
		this.isEnabled = false
		return@register
	}

	val mainFile = upload(project.property("curseforge_id"), tasks.remapJar.get())
	mainFile.releaseType = ModUtils.fetchVersionType(VERSION, mcVersion)
	mainFile.addGameVersion(McVersionLookup.getCurseForgeEquivalent(mcVersion))
	compatibleMcVersions.stream()
		.map { McVersionLookup.getCurseForgeEquivalent(it) }
		.forEach { mainFile.addGameVersion(it) }
	mainFile.addModLoader("Fabric", "Quilt")
	mainFile.addJavaVersion("Java 21", "Java 22")

	mainFile.displayName = "LambdaBetterGrass $VERSION (${McVersionLookup.getVersionTag(mcVersion)})"
	mainFile.addRequirement("fabric-api")
	mainFile.addOptional("modmenu")
	mainFile.addIncompatibility("optifabric")

	mainFile.changelogType = "markdown"
	mainFile.changelog = changelogContent
}

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
