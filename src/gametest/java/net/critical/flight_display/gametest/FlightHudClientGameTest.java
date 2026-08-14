package net.critical.flight_display.gametest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mojang.blaze3d.platform.NativeImage;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;

/**
 * Tier 3: the mod inside a real Minecraft client, asserting on real pixels.
 *
 * <p>This mod's entire observable behaviour is "a red pitch ladder appears while
 * you are Elytra-flying, and does not appear otherwise". {@code FlightHudMath} is
 * unit-tested to 100% line coverage, but pure math cannot tell whether its numbers
 * ever reached a framebuffer, and both classes that would answer that -
 * {@code FlightDisplayClient} (which registers the HUD callback) and
 * {@code FlightHudRenderer} (which decides {@code isFallFlying()} and issues the
 * draw calls) - are excluded from coverage because neither can load headless.
 *
 * <p>So this test flies a real player with a real Elytra and reads the resulting
 * PNG back. Because the HUD is drawn in pure red (0xFFFF0000) and nothing in a
 * vanilla world is exactly that colour, the pixels are a usable signal rather than
 * a vague impression - which is why the screenshots here ARE the assertion, unlike
 * the sibling critical-orientation where they are only a human-legible glance.
 */
public class FlightHudClientGameTest implements FabricClientGameTest {
	private static final String MOD_ID = "flight_display";

	/**
	 * The renderer asks for one colour, 0xFFFF0000, but it arrives on screen as two.
	 *
	 * <p>{@code guiGraphics.fill} lands as exactly 0xFFFF0000: an untextured, fully
	 * opaque quad, so the blend is a straight overwrite. {@code guiGraphics.drawString}
	 * lands as 0xFFFC0000 - the glyphs go through the text render type, and whatever it
	 * does to the channel (it is not alpha blending; the background leaks nothing, green
	 * and blue stay at exactly 0) costs three units of red.
	 *
	 * <p>This matters because the two overlap. The "Speed:" line is drawn on top of a
	 * hash mark, replacing part of that mark with the 0xFC shade - so an exact match on
	 * 0xFFFF0000 measured that row at 82.5% and lost it. Matching a narrow band of
	 * saturated red instead of one literal value costs nothing in precision: across a
	 * whole vanilla frame - sky, terrain, hearts, hotbar, chat - this predicate matched
	 * 0 pixels grounded and 8164 while flying, every one of them inside the ladder box
	 * and in one of those two shades. It is also the driver-independent choice, since
	 * the 0xFC is a shader artifact and software GL under CI need not reproduce it
	 * exactly.
	 */
	private static boolean isHudRed(int argb) {
		return ((argb >>> 24) & 0xFF) == 0xFF
				&& ((argb >> 16) & 0xFF) >= 0xF0
				&& ((argb >> 8) & 0xFF) == 0
				&& (argb & 0xFF) == 0;
	}

	/** Mirrors FlightHudMath.NUMBER_OF_HASHES, restated so the test is not circular. */
	private static final int NUMBER_OF_HASHES = 11;

	/**
	 * Altitude to glide from. The overworld build limit is 320 and teleporting above
	 * terrain leaves plenty of glide time to take two screenshots before anything can
	 * touch the ground - which matters, because a fall-damage tint is red.
	 */
	private static final int GLIDE_ALTITUDE = 300;

	private static final int SETTLE_TICKS = 20;
	private static final int RENDER_TICKS = 5;

	/**
	 * Pitches chosen for what they do to the ladder rather than for being round.
	 * FlightHudMath.pitchOffset is {@code (spacing / 10) * (truncatedPitch % 10)}:
	 * -20 gives offset 0 (hash marks exactly on their unshifted positions, all 12
	 * visible), while -25 gives offset -spacing/2 (every mark shifted up half a
	 * spacing, and the topmost one scrolled out of the ladder, leaving 11). A
	 * renderer that ignored pitchOffset entirely would produce identical images for
	 * both and fail the second.
	 */
	private static final float PITCH_UNSHIFTED = -20.0f;
	private static final float PITCH_HALF_SHIFTED = -25.0f;

	/**
	 * A row/column counts as part of the ladder frame only if its red run covers most
	 * of the ladder's span. This is what separates the full-width hash marks from the
	 * "Pitch:"/"Speed:" text, which is drawn in the same red inside the same box but
	 * spans only a few dozen pixels.
	 */
	private static final double SPAN_FRACTION = 0.9;

	@Override
	public void runTest(ClientGameTestContext context) {
		context.waitForScreen(TitleScreen.class);

		if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) {
			throw new AssertionError("the client booted without the '" + MOD_ID
					+ "' mod loaded - every assertion below would have passed vacuously");
		}

		try (TestSingleplayerContext singleplayer =
					 context.worldBuilder().setUseConsistentSettings(true).create()) {
			// Survival, not creative. In creative a double-tap of the jump key toggles
			// creative flight, and getAbilities().flying blocks Elytra deployment
			// outright - so the one input this test depends on would silently do the
			// wrong thing. Survival has no such ambiguity. Nothing here ever takes
			// damage, so survival's red hurt-tint never appears.
			singleplayer.getServer().runCommand("gamemode survival @p");
			awaitSurvival(context);
			awaitLiveHudFrame(context);

			// The paired negative control, and it runs BEFORE anything else can
			// succeed: standing on the ground, not flying, the ladder must be entirely
			// absent. Without this, every assertion below would also pass for a mod
			// that drew its HUD unconditionally.
			Ladder grounded = measureLadder(context, context.takeScreenshot("flight-hud-absent-grounded"));

			if (grounded.redPixelsInBox() != 0) {
				throw new AssertionError(String.format(Locale.ROOT,
						"the pitch ladder is being drawn while the player is standing on the ground and "
								+ "not Elytra-flying: found %d HUD-red pixels inside the ladder box "
								+ "(%d,%d)-(%d,%d). FlightHudRenderer's isFallFlying() guard is the "
								+ "suspect - every 'HUD is visible' assertion after this point would "
								+ "pass for a HUD that is simply always on.",
						grounded.redPixelsInBox(), grounded.boxLeft(), grounded.boxTop(),
						grounded.boxRight(), grounded.boxBottom()));
			}

			equipElytra(context, singleplayer);
			liftOff(context, singleplayer);
			startGliding(context);

			// Unshifted: all 12 hash marks on their base positions.
			assertLadderRendered(context, PITCH_UNSHIFTED, "flight-hud-visible-flying", NUMBER_OF_HASHES + 1, 0.0);

			// Half-shifted: the ladder has scrolled up by half a hash spacing and the
			// topmost mark has scrolled out of the box.
			assertLadderRendered(context, PITCH_HALF_SHIFTED, "flight-hud-pitch-scrolled", NUMBER_OF_HASHES, -0.5);

			singleplayer.getWorldSave();
		}

		context.waitForScreen(TitleScreen.class);
	}

	private static void awaitSurvival(ClientGameTestContext context) {
		// runCommand swallows command failures (it does not throw on a failed dispatch),
		// so the gamemode is confirmed by its effect on the client rather than by the
		// command having been issued.
		try {
			context.waitFor(client -> client.player != null && client.player.isAlive()
					&& !client.player.isCreative() && !client.player.isSpectator());
		} catch (RuntimeException e) {
			throw new AssertionError("the player never entered survival mode after 'gamemode survival @p'. "
					+ "runCommand does not report command failures, so a rejected or misspelled command "
					+ "looks exactly like this.", e);
		}
	}

	/**
	 * Blocks until the client is actually rendering the in-game HUD, standing still on
	 * the ground.
	 *
	 * <p>This exists because of a defect this test shipped with and a negative control
	 * caught. Entering a freshly created world puts a {@code ReceivingLevelScreen}
	 * ("Loading terrain...") in front of the world for a second or so, and Minecraft
	 * does not render the in-game HUD behind a screen. The grounded "the ladder must be
	 * absent" control was being shot during exactly that window, so it was measuring a
	 * frame in which NO mod could have drawn a HUD.
	 *
	 * <p>It passed, and it was worthless: deleting the {@code isFallFlying()} guard from
	 * FlightHudRenderer - making the HUD render permanently, grounded or not - still
	 * produced a green run. A negative control that cannot fail is worse than no
	 * control, because the suite looks stronger than it is. The re-run of that same
	 * control after this fix is what actually establishes the check works.
	 *
	 * <p>{@code screen == null} is the load-bearing condition; onGround and a settling
	 * delay make "grounded" mean what the assertion says it means.
	 */
	private static void awaitLiveHudFrame(ClientGameTestContext context) {
		try {
			context.waitFor(client -> client.screen == null
					&& client.level != null
					&& client.player != null
					&& client.player.onGround());
		} catch (RuntimeException e) {
			throw new AssertionError("the client never reached a live in-game HUD frame: it stayed on a "
					+ "screen, or the player never landed. Screenshots taken in that state show no HUD "
					+ "for any mod, which would make the grounded negative control below pass vacuously.",
					e);
		}

		context.waitTicks(SETTLE_TICKS);
	}

	private static void equipElytra(ClientGameTestContext context, TestSingleplayerContext singleplayer) {
		singleplayer.getServer().runCommand("item replace entity @p armor.chest with minecraft:elytra");

		// Confirmed on the CLIENT, not the server: the client is what renders the HUD,
		// and equipment reaches it by tracked-data sync rather than instantly. Waiting
		// on the client also disposes of that race.
		try {
			context.waitFor(client -> client.player != null
					&& client.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA));
		} catch (RuntimeException e) {
			throw new AssertionError("no Elytra ever appeared in the player's chest slot. Without one the "
					+ "player cannot glide and the HUD would correctly never render, so the rest of this "
					+ "test would be meaningless rather than failing honestly.", e);
		}
	}

	private static void liftOff(ClientGameTestContext context, TestSingleplayerContext singleplayer) {
		// Dispatched from the world spawn rather than the player, hence 'execute as @p
		// at @s' - a bare relative teleport would move the player relative to spawn.
		singleplayer.getServer().runCommand("execute as @p at @s run tp @s ~ " + GLIDE_ALTITUDE + " ~");

		try {
			context.waitFor(client -> client.player != null
					&& client.player.getY() > GLIDE_ALTITUDE - 20
					&& !client.player.onGround());
		} catch (RuntimeException e) {
			throw new AssertionError("the player never reached gliding altitude after the teleport.", e);
		}
	}

	private static void startGliding(ClientGameTestContext context) {
		// Elytra flight starts the way a player starts it: the jump key, pressed while
		// airborne and falling. Vanilla LocalPlayer only sends START_FALL_FLYING on a
		// fresh press, so this holds and releases rather than holding continuously, and
		// retries - the very first attempt can land on a tick where the player is still
		// registered as on the ground.
		for (int attempt = 0; attempt < 10; attempt++) {
			context.getInput().holdKeyFor(options -> options.keyJump, 2);
			context.waitTicks(3);

			if (context.computeOnClient(client -> client.player != null && client.player.isFallFlying())) {
				return;
			}
		}

		double altitude = context.computeOnClient(client -> client.player == null ? -1.0 : client.player.getY());
		boolean hasElytra = context.computeOnClient(client -> client.player != null
				&& client.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA));
		throw new AssertionError(String.format(Locale.ROOT,
				"the player never started Elytra-gliding after 10 jump-key presses (altitude %.1f, "
						+ "elytra equipped: %s). This is the test harness failing to set up its own "
						+ "precondition, not the mod failing - do not read it as a HUD regression.",
				altitude, hasElytra));
	}

	private static void assertLadderRendered(
			ClientGameTestContext context, float pitch, String screenshotName,
			int expectedHashRows, double expectedOffsetInSpacings) {
		context.runOnClient(client -> client.player.setXRot(pitch));
		context.waitTicks(RENDER_TICKS);

		Ladder ladder = measureLadder(context, context.takeScreenshot(screenshotName));

		if (ladder.redPixelsInBox() == 0) {
			throw new AssertionError(String.format(Locale.ROOT,
					"no part of the pitch ladder was drawn while the player was genuinely Elytra-gliding "
							+ "at pitch %.1f. Not one HUD-red pixel appeared inside the ladder box. "
							+ "The HUD callback registration in FlightDisplayClient is the first suspect: "
							+ "if HudRenderCallback.EVENT.register is never reached, FlightHudRenderer is "
							+ "never called and everything else in this repo still passes.",
					pitch));
		}

		// Two vertical rails, at the 1/3 and 2/3 x-marks. Computed here from the gui
		// dimensions directly rather than by calling FlightHudMath, so that a wrong
		// computeLayout cannot agree with itself and pass.
		List<Run> rails = ladder.railColumns();

		if (rails.size() != 2) {
			throw new AssertionError(String.format(Locale.ROOT,
					"expected exactly 2 vertical rails at pitch %.1f, found %d at %s. A rail is a column "
							+ "of HUD red spanning at least %.0f%% of the ladder's height; the ladder box "
							+ "is (%d,%d)-(%d,%d) in a %dx%d screenshot.",
					pitch, rails.size(), rails, SPAN_FRACTION * 100,
					ladder.boxLeft(), ladder.boxTop(), ladder.boxRight(), ladder.boxBottom(),
					ladder.imageWidth(), ladder.imageHeight()));
		}

		assertNear("left rail", pitch, rails.get(0).centre(), ladder.boxLeft(), ladder.tolerance());
		assertNear("right rail", pitch, rails.get(1).centre(), ladder.boxRight(), ladder.tolerance());

		// The hash marks, and where the pitch offset has scrolled them to.
		List<Run> hashes = ladder.hashRows();

		if (hashes.size() != expectedHashRows) {
			throw new AssertionError(String.format(Locale.ROOT,
					"expected %d hash marks at pitch %.1f, found %d at %s. FlightHudMath.hashLineYs drops "
							+ "any mark scrolled outside [top, bottom], so a wrong count usually means the "
							+ "pitch offset is wrong rather than the marks being mis-drawn. Ladder box "
							+ "(%d,%d)-(%d,%d), spacing %.2f px.",
					expectedHashRows, pitch, hashes.size(), hashes,
					ladder.boxLeft(), ladder.boxTop(), ladder.boxRight(), ladder.boxBottom(),
					ladder.hashSpacing()));
		}

		// Every mark must sit on the base grid plus the expected scroll offset. This is
		// the assertion that a renderer ignoring pitchOffset fails: at PITCH_HALF_SHIFTED
		// it would draw the unshifted grid, which is half a spacing away from here.
		double offsetPx = expectedOffsetInSpacings * ladder.hashSpacing();

		for (int i = 0; i < hashes.size(); i++) {
			// At a negative offset the topmost base mark scrolls out of the box, so the
			// first surviving mark is the (i+1)th of the base grid.
			int gridIndex = expectedOffsetInSpacings < 0 ? i + 1 : i;
			double expectedY = ladder.boxTop() + gridIndex * ladder.hashSpacing() + offsetPx;
			assertNear("hash mark " + i + " of " + hashes.size(), pitch,
					hashes.get(i).centre(), expectedY, ladder.tolerance());
		}
	}

	private static void assertNear(String what, float pitch, double actual, double expected, double tolerance) {
		if (Math.abs(actual - expected) > tolerance) {
			throw new AssertionError(String.format(Locale.ROOT,
					"%s is at %.1f px but should be at %.1f px (tolerance %.1f) at pitch %.1f. The ladder "
							+ "is being drawn, so this is FlightHudMath's layout disagreeing with the "
							+ "screen it was computed for - not a missing HUD registration.",
					what, actual, expected, tolerance, pitch));
		}
	}

	/**
	 * Reads a screenshot back and measures the red structure inside the region the
	 * ladder should occupy.
	 *
	 * <p>Deliberately uses Minecraft's own {@link NativeImage} rather than
	 * {@code javax.imageio}: initialising AWT inside a live LWJGL client is unsafe on
	 * macOS, where GLFW owns the main thread, and this test is run locally on macOS
	 * as well as under xvfb in CI.
	 */
	private static Ladder measureLadder(ClientGameTestContext context, Path screenshot) {
		// Gui-scaled dimensions are what the renderer lays out against; the screenshot
		// is in framebuffer pixels. The ratio is read back from the image rather than
		// assumed from the gui-scale option, so a HiDPI framebuffer cannot skew it.
		int guiWidth = context.computeOnClient(client -> client.getWindow().getGuiScaledWidth());
		int guiHeight = context.computeOnClient(client -> client.getWindow().getGuiScaledHeight());

		if (!Files.isRegularFile(screenshot)) {
			throw new AssertionError("the harness reported taking a screenshot at " + screenshot
					+ " but no file is there, so nothing below could have been measured");
		}

		try (InputStream in = Files.newInputStream(screenshot); NativeImage image = NativeImage.read(in)) {
			int width = image.getWidth();
			int height = image.getHeight();
			double scale = width / (double) guiWidth;

			// The same thirds FlightHudMath.computeLayout derives, recomputed here from
			// the client's own gui dimensions rather than obtained by calling it - so a
			// wrong computeLayout is caught instead of being agreed with.
			//
			// These are exact thirds. The renderer's are too (FACTOR is a double), but it
			// truncates them to int at each fill() call: at guiWidth 427 the right rail
			// is asked for at 284.67 and drawn at 284. That is up to 1 gui px of
			// legitimate disagreement, absorbed by tolerance() rather than replicated.
			double left = (guiWidth / 3.0) * scale;
			double right = (guiWidth / 3.0) * 2.0 * scale;
			double top = (guiHeight / 3.0) * scale;
			double bottom = (guiHeight / 3.0) * 2.0 * scale;
			double spacing = (bottom - top) / NUMBER_OF_HASHES;

			int x0 = clamp((int) Math.floor(left) - 2, 0, width - 1);
			int x1 = clamp((int) Math.ceil(right) + 2, 0, width - 1);
			int y0 = clamp((int) Math.floor(top) - 2, 0, height - 1);
			int y1 = clamp((int) Math.ceil(bottom) + 2, 0, height - 1);

			int[] perColumn = new int[width];
			int[] perRow = new int[height];
			int total = 0;

			for (int y = y0; y <= y1; y++) {
				for (int x = x0; x <= x1; x++) {
					// getPixel returns ARGB on this version - verified by bytecode, it
					// calls ARGB.fromABGR on the raw buffer. Do not swap the channels.
					if (isHudRed(image.getPixel(x, y))) {
						perColumn[x]++;
						perRow[y]++;
						total++;
					}
				}
			}

			int minRun = (int) Math.round(scale) + 1;
			List<Run> rails = findRuns(perColumn, (int) ((bottom - top) * SPAN_FRACTION), minRun);
			List<Run> hashes = findRuns(perRow, (int) ((right - left) * SPAN_FRACTION), minRun);

			return new Ladder(width, height, (int) left, (int) top, (int) right, (int) bottom,
					spacing, scale, total, rails, hashes);
		} catch (IOException e) {
			throw new AssertionError("could not read back the screenshot at " + screenshot, e);
		}
	}

	/**
	 * Groups adjacent indices whose count clears {@code threshold} into runs. A 1px
	 * gui-space line is {@code guiScale} framebuffer pixels wide, so the lines arrive
	 * as short runs rather than single indices; collapsing them keeps the counts
	 * independent of the gui scale in force.
	 */
	private static List<Run> findRuns(int[] counts, int threshold, int maxGap) {
		List<Run> runs = new ArrayList<>();
		int start = -1;
		int lastHit = -1;

		for (int i = 0; i < counts.length; i++) {
			if (counts[i] < threshold) {
				continue;
			}

			if (start < 0) {
				start = i;
			} else if (i - lastHit > maxGap) {
				runs.add(new Run(start, lastHit));
				start = i;
			}

			lastHit = i;
		}

		if (start >= 0) {
			runs.add(new Run(start, lastHit));
		}

		return runs;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	/** A contiguous band of framebuffer indices that all cleared the span threshold. */
	private record Run(int from, int to) {
		double centre() {
			return (from + to) / 2.0;
		}

		@Override
		public String toString() {
			return from == to ? String.valueOf(from) : from + "-" + to;
		}
	}

	private record Ladder(
			int imageWidth, int imageHeight,
			int boxLeft, int boxTop, int boxRight, int boxBottom,
			double hashSpacing, double scale,
			int redPixelsInBox,
			List<Run> railColumns, List<Run> hashRows
	) {
		/**
		 * Two fifths of a hash spacing, with a floor for very small windows.
		 *
		 * <p>Sized against both error budgets rather than picked. What a correct
		 * renderer can legitimately be out by: the renderer truncates its gui-space y to
		 * an int (up to 1 gui px, so {@code scale} framebuffer px) and a run's centre
		 * sits half a run-width in, for about 2.5 px at gui scale 2 - measured
		 * deviations were under 1. What it must not be allowed to pass with: a ladder
		 * scrolled by a whole or a half spacing, 14.5 and 7.3 px respectively. 0.4
		 * spacings is 5.8 px, clearing both by better than 2x.
		 */
		double tolerance() {
			return Math.max(3.0, hashSpacing * 0.4);
		}
	}
}
