val modId: String by project
val minecraftVersion: String = libs.versions.minecraft.get()

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

repositories {
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    "neoForge"(libs.neoforge.platform)
    modImplementation(libs.neoforge.architectury)
}

tasks {
    processResources {
        val properties = mapOf(
            "version" to project.version,
            "minecraftVersion" to minecraftVersion,
            "neoforgeVersion" to libs.versions.neoforge.platform.get(),
            "loaderVersion" to libs.versions.neoforge.loader.get(),
            "architecturyVersion" to libs.versions.architectury.get()
        )
        inputs.properties(properties)
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(properties)
        }
    }

    remapJar {
        atAccessWideners.add("${modId}.accesswidener")
    }
}
