package lambdabettergrass

import org.gradle.accessors.dm.LibrariesForLibs

object Constants {
	const val NAME = "lambdabettergrass"
	const val NAMESPACE = "lambdabettergrass"
	const val PRETTY_NAME = "LambdaBetterGrass"

	const val DESCRIPTION = "Adds actual better grass and better snow while being customizable."
	const val RUNTIME_DESCRIPTION = "The runtime of LambdaBetterGrass."

	const val API_ARTIFACT = "$NAME-api"

	@JvmField
	val AUTHORS = listOf("LambdAurora")

	@JvmField
	val CONTRIBUTORS = listOf<String>()

	const val PROJECT_LINK = "https://lambdaurora.dev/projects/lambdabettergrass"
	const val SOURCES_LINK = "https://github.com/LambdAurora/LambdaBetterGrass"
	const val LICENSE = "Lambda License"

	private var minecraftVersion: String? = null

	fun finalizeInit(libs: LibrariesForLibs) {
		this.minecraftVersion = libs.versions.minecraft.get()
	}
}
