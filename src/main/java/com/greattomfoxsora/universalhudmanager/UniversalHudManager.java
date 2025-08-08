package com.greattomfoxsora.universalhudmanager;

import com.mojang.logging.LogUtils;
import com.greattomfoxsora.universalhudmanager.core.HUDRegistry;
import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import com.greattomfoxsora.universalhudmanager.client.ResourcePackCompatibleOverlays;
import com.greattomfoxsora.universalhudmanager.client.HUDPositionHandler;
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
    
    /**
     * デバッグログが有効かどうかチェック
     */
    private static boolean isDebugEnabled() {
        return HUDConfig.DEBUG_MODE.get();
    }
    
    public UniversalHudManager() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register configuration
        HUDConfig.register();
        
        // Register client setup event
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::doClientStuff);
            // NOTE: VanillaHudController handles overlay cancellation automatically
            // Removed duplicate ResourcePackCompatibleOverlays::onGuiOverlayEvent registration
        }
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        // Register HUD position handler for edit mode
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(HUDPositionHandler.class);
        }
        
        LOGGER.info("Universal HUD Manager initialized!");
        if (isDebugEnabled()) {
            LOGGER.debug("Config system registered - Vector2i positioning enabled!");
            LOGGER.debug("Created by GreatTomFox & Sora - Making HUD management universal!");
        }
    }
    
    private void doClientStuff(final FMLClientSetupEvent event) {
        // Initialize HUD discovery on client
        event.enqueueWork(() -> {
            if (isDebugEnabled()) {
                LOGGER.debug("Initializing client-side HUD discovery...");
            }
            HUDRegistry.clear();
            HUDRegistry.discoverVanillaHUDs();
            HUDRegistry.discoverModHUDs();
            if (isDebugEnabled()) {
                LOGGER.debug("Client initialization complete. {}", HUDRegistry.getStats());
            }
        });
    }
}