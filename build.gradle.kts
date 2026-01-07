@file:Suppress("PropertyName")

plugins {
    id("gg.meza.stonecraft")
}

// Mod metadata
val mod_id: String by project
val mod_name: String by project
val mod_version: String by project
val mod_description: String by project
val mod_license: String by project
val mod_author: String by project
val mod_homepage: String by project
val mod_sources: String by project
val mod_issues: String by project

modSettings {
    // Mod identifiers
    modId = mod_id
    modName = mod_name
    modVersion = mod_version
    modDescription = mod_description
    modLicense = mod_license
    modAuthor = mod_author
    modHomepage = mod_homepage
    modSources = mod_sources
    modIssues = mod_issues

    // This is a client-side only mod
    clientSideOnly = true

    // Mixin configuration
    mixins {
        add("flight_display.mixins.json")
    }
}

// Publishing configuration for CurseForge and Modrinth
publishMods {
    modrinth {
        // Fabric requires Fabric API
        if (isFabric) {
            requires("fabric-api")
        }
    }
    curseforge {
        clientRequired = true
        serverRequired = false
        // Fabric requires Fabric API
        if (isFabric) {
            requires("fabric-api")
        }
    }
}

// Testing configuration
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
