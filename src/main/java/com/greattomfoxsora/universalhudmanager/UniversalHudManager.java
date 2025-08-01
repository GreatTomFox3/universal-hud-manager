package com.greattomfoxsora.universalhudmanager;

import com.mojang.logging.LogUtils;
import com.greattomfoxsora.universalhudmanager.core.HUDRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * Universal HUD Manager - Main Mod Class
 * 
 * A powerful mod that allows universal positioning of HUD elements from vanilla 
 * Minecraft and other mods, inspired by Xaero's Minimap drag-and-drop system.
 * 
 * Created by GreatTomFox & Sora collaboration project.
 * 
 * @author GreatTomFox
 * @author Sora (AI Assistant)
 * @version 1.0.0
 */
@Mod(UniversalHudManager.MODID)
public class UniversalHudManager {
    
    public static final String MODID = "universalhudmanager";
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public UniversalHudManager() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register client setup event
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::doClientStuff);
        }
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        LOGGER.info("Universal HUD Manager initialized!");
        LOGGER.info("Created by GreatTomFox & Sora - Making HUD management universal!");
    }
    
    private void doClientStuff(final FMLClientSetupEvent event) {
        // Initialize HUD discovery on client
        event.enqueueWork(() -> {
            LOGGER.info("Initializing client-side HUD discovery...");
            HUDRegistry.clear();
            HUDRegistry.discoverVanillaHUDs();
            HUDRegistry.discoverModHUDs();
            LOGGER.info("Client initialization complete. {}", HUDRegistry.getStats());
        });
    }
}