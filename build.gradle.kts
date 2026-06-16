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

lambdamcdev.namespace.set(project.property("mod_namespace") as String)
base.archivesName.set(lambdamcdev.namespace)

val mcVersion = libs.versions.minecraft.get()
val compatibleMcVersions: Set<String> = setOf()
val VERSION = project.property("mod_version") as String
version = "$VERSION+${McVersionLookup.getVersionTag(mcVersion)}"

// This field defines the Java version your mod target.
val targetJavaVersion = Integer.parseInt(project.property("java_version").toString())

val fabricApiModules = listOf(
	fabricApi.module("fabric-model-loading-api-v1", libs.versions.fabric.api.get()),
	fabricApi.module("fabric-renderer-api-v1", libs.versions.fabric.api.get()),
	fabricApi.module("fabric-resource-loader-v1", libs.versions.fabric.api.get()),
)

lambdamcdev {
	manifests {
		fmj {
			val sourcesLink = "https://github.com/LambdAurora/LambdaBetterGrass"

			withDescription(project.property("mod_description") as String)
			withAuthors("LambdAurora")
			withContact {
				it.withHomepage("https://lambdaurora.dev/projects/lambdabettergrass")
					.withSources("$sourcesLink.git")
					.withIssues("$sourcesLink/issues")
			}
			withLicense("Lambda License")
			withIcon("assets/${namespace.get()}/icon.png")
			withEnvironment("client")
			withEntrypoints("yumi:client_init", "dev.lambdaurora.lambdabettergrass.LambdaBetterGrass::INSTANCE")
			withEntrypoints("modmenu", "dev.lambdaurora.lambdabettergrass.LambdaBetterGrassModMenu")
			withEntrypoints("sodium:config_api_user", "dev.lambdaurora.lambdabettergrass.LambdaBetterGrassSodiumConfig")
			withEntrypoints("fabric-datagen", "dev.lambdaurora.lambdabettergrass.resource.LBGDataGen")
			withAccessWidener("${namespace.get()}.accesswidener")
			withMixins("${namespace.get()}.mixins.json")
			withDepend("fabricloader", ">=${libs.versions.fabric.loader.get()}")
			withDepend("minecraft", project.property("fabric_mc_constraints").toString())
			withDepend("java", ">=$targetJavaVersion")
			withDepend("spruceui", ">=${libs.versions.spruceui.get()}")
			withDepend("yumi_mc_core", ">=${libs.versions.yumi.mc.foundation.get()}")
			fabricApiModules.forEach { module -> withDepend(module.name, ">=${module.version}") }
			withRecommend("modmenu", ">=${libs.versions.modmenu.get()}")
			withBreak("optifabric", "*")
			withModMenu {
				it.withCurseForge("https://www.curseforge.com/minecraft/mc-mods/lambdabettergrass")
					.withDiscord("https://discord.lambdaurora.dev/")
					.withGitHubReleases("$sourcesLink/releases")
					.withModrinth("https://modrinth.com/mod/lambdabettergrass")
					.withLink("modmenu.bluesky", "https://bsky.app/profile/lambdaurora.dev")
					.withLink("modmenu.donate", "https://donate.lambdaurora.dev/")
			}
		}
	}

	setupActionsRefCheck()
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
	exclusiveContent {
		filter {
			includeGroupAndSubgroups("net.caffeinemc")
		}

		forRepository {
			maven {
				url = uri("https://maven.caffeinemc.net/releases/")
			}
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
	implementation(libs.fabric.loader)
	fabricApiModules.forEach { implementation(it) }
	implementation(fabricApi.module("fabric-data-generation-api-v1", libs.versions.fabric.api.get()))
	implementation(fabricApi.module("fabric-renderer-indigo", libs.versions.fabric.api.get()))

	implementation(libs.yumi.mc.foundation)
	implementation(libs.spruceui)

	// Config
	compileOnly(libs.modmenu) {
		this.isTransitive = false
	}
	compileOnly(libs.sodium.api)
	/*modLocalRuntime(libs.modmenu) {
		this.isTransitive = false
	}*/
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
	exclude(".cache/**")
}

tasks.jar {
	inputs.property("archivesName", base.archivesName)
	archiveClassifier = "dev"

	from("LICENSE") {
		rename { "${it}_${inputs.properties["archivesName"]}" }
	}
}

license {
	rule(file("metadata/HEADER"))

	include("**/*.java")
}

tasks.shadowJar {
	dependsOn(tasks.jar)
	inputs.property("archivesName", base.archivesName)

	configurations = listOf(project.configurations["shadow"])
	destinationDirectory.set(file("${project.layout.buildDirectory.get()}/libs"))
	archiveClassifier = ""

	relocate("com.electronwill.nightconfig", "dev.lambdaurora.lambdabettergrass.shadow.nightconfig")

	from(rootProject.file("LICENSE")) {
		rename { "${it}_${inputs.properties["archivesName"]}" }
	}
}

loom.nestJars(
	tasks.shadowJar,
	fileTree(tasks.processIncludeJars.get().outputDirectory)
)

tasks.assemble.configure {
	dependsOn(tasks.shadowJar)
}

val README = ModUtils.parseReadme(
	project, "https://raw.githubusercontent.com/LambdAurora/LambdaBetterGrass/26.1/\$2"
)
val CHANGELOG_CONTENT = ModUtils.fetchChangelog(project, VERSION)

val packageModrinth by tasks.registering(PackageModrinthTask::class) {
	this.group = "publishing"
	this.versionType.set(ModUtils.getVersionType(VERSION, mcVersion))
	this.versionName.set("${project.name} $VERSION (${McVersionLookup.getVersionTag(mcVersion)})")
	this.gameVersions.set(listOf(mcVersion) + compatibleMcVersions)
	this.loaders.set(listOf("fabric", "quilt"))
	this.dependencies.set(
		listOf(
			ModVersionDependency("P7dR8mSH", ModVersionDependency.Type.REQUIRED), // Fabric API
		)
	)
	this.changelog.set(CHANGELOG_CONTENT)
	this.readme.set(README)
	this.files.setFrom(tasks.shadowJar.get())
}

modrinth {
	projectId.set(project.property("modrinth_id") as String)
	versionName.set("${project.name} $VERSION (${McVersionLookup.getVersionTag(mcVersion)})")
	versionType.set(ModUtils.fetchVersionType(VERSION, mcVersion))
	uploadFile.set(tasks.shadowJar)
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

	val mainFile = upload(project.property("curseforge_id"), tasks.shadowJar.get())
	mainFile.releaseType = ModUtils.fetchVersionType(VERSION, mcVersion)
	mainFile.addGameVersion(McVersionLookup.getCurseForgeEquivalent(mcVersion))
	compatibleMcVersions.stream()
		.map { McVersionLookup.getCurseForgeEquivalent(it) }
		.forEach { mainFile.addGameVersion(it) }
	mainFile.addModLoader("Fabric", "Quilt")
	mainFile.addJavaVersion("Java 21", "Java 22")

	mainFile.displayName = "${project.name} $VERSION (${McVersionLookup.getVersionTag(mcVersion)})"
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
				name.set(project.name)
				description.set(project.property("mod_description") as String)
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
