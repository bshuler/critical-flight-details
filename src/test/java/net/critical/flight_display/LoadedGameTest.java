package net.critical.flight_display;

//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tier 1 "loaded game" tests: these run against a real, bootstrapped
 * Minecraft and a real Fabric loader rather than mocks, courtesy of
 * fabric-loader-junit (see the dependency comment in build.gradle.kts).
 *
 * <p>{@code FlightHudMathTest} already covers the arithmetic headless. What
 * only a loaded game can check is whether this cell's <em>packaging</em> and
 * <em>environment assumptions</em> still hold: that {@code fabric.mod.json}
 * parses, that the version ranges it declares are actually satisfiable
 * against the Minecraft and loader versions this cell builds against, and
 * that the pitch range the HUD is drawn for is the pitch range the game can
 * really produce.
 *
 * <p>Fabric cells only: NeoForge's equivalent bootstrap (junit-fml) is only
 * usable from ModDevGradle, not from Architectury Loom - see the junit-fml
 * exclusion comment in build.gradle.kts.
 */
public class LoadedGameTest {

    private static final String MOD_ID = "flight_display";

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void gameDataIsActuallyLoaded() {
        // Guard on the harness itself: if the bootstrap above ever silently
        // no-ops, every other assertion in this class becomes vacuous.
        assertNotNull(Items.DIAMOND, "Items.DIAMOND should be a real loaded game object");
        // The item registry moved from Registry.ITEM to BuiltInRegistries.ITEM
        // in 1.19.3; both expose getKey/keySet identically.
        //? if >=1.19.3 {
        var itemRegistry = net.minecraft.core.registries.BuiltInRegistries.ITEM;
        //?} else
        /*var itemRegistry = net.minecraft.core.Registry.ITEM;*/
        assertEquals("minecraft:diamond", itemRegistry.getKey(Items.DIAMOND).toString());
        assertTrue(itemRegistry.keySet().size() > 500,
                "the real item registry should hold the full vanilla item set");
    }

    @Test
    void modIsDiscoveredByARealFabricLoader() {
        // The processed fabric.mod.json (Stonecraft has already substituted
        // ${id}/${version}/etc. by this point) is on the test classpath, so a
        // real loader discovers this mod exactly as the game would. A
        // malformed or mis-templated metadata file fails here instead of at
        // first launch.
        var self = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(
                () -> new AssertionError("a real Fabric loader did not discover mod id '" + MOD_ID + "'"));
        assertEquals(MOD_ID, self.getMetadata().getId());
        assertFalse(self.getMetadata().getVersion().getFriendlyString().isBlank(),
                "mod version must survive resource templating");
    }

    @Test
    void declaredDependencyRangesAreSatisfiableInThisCell() {
        // The real drift hazard in a Stonecutter matrix: fabric.mod.json
        // declares minecraft/fabricloader version ranges, and a cell can be
        // built against a Minecraft the ranges no longer admit. That produces
        // a jar that builds perfectly and then refuses to load in-game. Here
        // the loader itself resolves the ranges against the actually-loaded
        // versions, per cell.
        var loader = FabricLoader.getInstance();
        var self = loader.getModContainer(MOD_ID).orElseThrow();
        for (ModDependency dependency : self.getMetadata().getDependencies()) {
            if (dependency.getKind() != ModDependency.Kind.DEPENDS) {
                continue;
            }
            var provider = loader.getModContainer(dependency.getModId());
            assertTrue(provider.isPresent(),
                    "fabric.mod.json requires '" + dependency.getModId() + "' but nothing provides it");
            assertTrue(dependency.matches(provider.get().getMetadata().getVersion()),
                    "fabric.mod.json requires " + dependency + " but this cell loads "
                            + dependency.getModId() + " "
                            + provider.get().getMetadata().getVersion().getFriendlyString());
        }
    }

    @Test
    void pitchLadderCoversEveryPitchTheGameCanProduce() {
        // Vanilla clamps camera pitch to [-90, 90]; the ladder is drawn from
        // the truncated pitch, and its scroll offset must stay inside one hash
        // spacing for every pitch in that real range - otherwise the ladder
        // visibly jumps at the extremes. Mth.clamp is the game's own clamp,
        // so the bounds are the game's, not the test's.
        var layout = FlightHudMath.computeLayout(1920, 1080);
        var spacing = layout.distanceBetweenHashes();
        for (float raw = -180f; raw <= 180f; raw += 0.5f) {
            var pitch = Mth.clamp(raw, -90f, 90f);
            var offset = FlightHudMath.pitchOffset(spacing, FlightHudMath.truncatedPitch(pitch));
            assertTrue(Math.abs(offset) < spacing,
                    "ladder offset " + offset + " at pitch " + pitch + " exceeds one hash spacing " + spacing);
        }
    }

    @Test
    void displayedPitchIsTheInverseOfTheGamesOwnPitchSign() {
        // Minecraft's xRot is positive looking down; aviation instruments read
        // positive looking up. The readout inverts, and it must keep doing so
        // across the whole clamped range the game actually delivers.
        assertEquals(90, FlightHudMath.invertedDisplayPitch(Mth.clamp(-120f, -90f, 90f)));
        assertEquals(-90, FlightHudMath.invertedDisplayPitch(Mth.clamp(120f, -90f, 90f)));
        assertEquals(0, FlightHudMath.invertedDisplayPitch(Mth.clamp(0f, -90f, 90f)));
    }
}
//?}
