import dev.lambdaurora.mcdev.api.McVersionLookup
import lambdabettergrass.Constants
import lambdabettergrass.LambdaBetterGrassExt
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
	id("fabric-loom")
	id("dev.lambdaurora.mcdev")
}

// Seriously you should not worry about it, definitely not a hack.
// https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
val libs = the<LibrariesForLibs>()
Constants.finalizeInit(libs)

val lbg = project.extensions
	.create("lbg", LambdaBetterGrassExt::class, project, libs)

version = "${lbg.version()}+${McVersionLookup.getVersionTag(lbg.mcVersion())}"
lambdamcdev.namespace = Constants.NAMESPACE
val javaVersion = Integer.parseInt(project.property("java_version").toString())

if (!(System.getenv("CURSEFORGE_TOKEN") != null
			|| System.getenv("MODRINTH_TOKEN") != null
			|| System.getenv("LBG_MAVEN") != null)
) {
	version = (version as String) + "-local"
}

repositories {
	mavenCentral()
	maven {
		name = "Gegy"
		url = uri("https://maven.gegy.dev")
		content {
			includeGroupAndSubgroups("dev.lambdaurora")
			includeGroup("io.github.queerbric")
		}
	}
}

dependencies {
	minecraft(libs.minecraft)
}

java {
	sourceCompatibility = JavaVersion.toVersion(javaVersion)
	targetCompatibility = JavaVersion.toVersion(javaVersion)

	withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"

	options.release.set(javaVersion)
}

loom {
	@Suppress("UnstableApiUsage")
	mixin {
		useLegacyMixinAp = false
	}
}
