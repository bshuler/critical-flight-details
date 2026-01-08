package net.critical.flight_display;

//? if fabric || quilt {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
//?} elif neoforge {
/*
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
*///?} elif forge {
/*
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
*///?}

import net.critical.flight_display.config.FlightDisplayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//? if fabric || quilt {
@Environment(EnvType.CLIENT)
public class FlightDisplayClient implements ClientModInitializer {
//?} elif neoforge {
/*
@Mod(value = FlightDisplayClient.MOD_ID, dist = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class FlightDisplayClient {
*///?} elif forge {
/*
@Mod(FlightDisplayClient.MOD_ID)
@OnlyIn(Dist.CLIENT)
public class FlightDisplayClient {
*///?}

    public static final String MOD_ID = "flight_display";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //? if fabric || quilt {
    @Override
    public void onInitializeClient() {
        init();
    }
    //?} elif neoforge {
    /*
    public FlightDisplayClient(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        NeoForgeHudRenderer.register();
        init();
    }
    *///?} elif forge {
    /*
    public FlightDisplayClient() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        ForgeHudRenderer.register();
        init();
    }
    *///?}

    public static void init() {
        // Initialize configuration
        //? if fabric || quilt {
        FlightDisplayConfig.setConfigPath(FabricLoader.getInstance().getConfigDir());
        //?} else {
        /*FlightDisplayConfig.setConfigPath(FMLPaths.CONFIGDIR.get());
        *///?}

        // Load config
        FlightDisplayConfig.getInstance();

        LOGGER.info("Critical Flight Display mod initialized");
    }
}
