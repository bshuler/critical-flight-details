package net.critical.flight_display;

//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//?} else if neoforge {
/*
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
*///?} else if forge {
/*
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
*///?}

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//? if fabric {
@Environment(EnvType.CLIENT)
public class FlightDisplayClient implements ClientModInitializer {
//?} else if neoforge {
/*
@Mod(value = FlightDisplayClient.MOD_ID, dist = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class FlightDisplayClient {
*///?} else if forge {
/*
@Mod(FlightDisplayClient.MOD_ID)
@OnlyIn(Dist.CLIENT)
public class FlightDisplayClient {
*///?}

    public static final String MOD_ID = "flight_display";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //? if fabric {
    @Override
    public void onInitializeClient() {
        init();
    }
    //?} else if neoforge {
    /*
    public FlightDisplayClient(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        init();
    }
    *///?} else if forge {
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
        LOGGER.info("Critical Flight Display mod initialized");
    }
}
