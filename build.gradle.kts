plugins {
    id("gg.meza.stonecraft")
}

modSettings {

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
