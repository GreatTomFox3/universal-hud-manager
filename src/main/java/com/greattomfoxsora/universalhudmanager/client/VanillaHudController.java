package com.greattomfoxsora.universalhudmanager.client;

import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Controls vanilla HUD overlay visibility
 * Disables vanilla HUD elements when custom positioning is enabled
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VanillaHudController {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Debug control - disable excessive logging
    private static boolean enableDetailedLogging = false;
    
    /**
     * Cancel vanilla HUD rendering when custom positioning is enabled
     */
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        // Cancel vanilla health bar when custom health is enabled
        if (HUDConfig.HEALTH_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            if (enableDetailedLogging) LOGGER.info("Canceling vanilla health overlay");
            event.setCanceled(true);
            return;
        }
        
        // Cancel vanilla food bar when custom food is enabled
        if (HUDConfig.FOOD_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.FOOD_LEVEL.id())) {
            if (enableDetailedLogging) LOGGER.info("Canceling vanilla food overlay");
            event.setCanceled(true);
            return;
        }
        
        // Cancel vanilla experience bar when custom experience is enabled
        if (HUDConfig.EXPERIENCE_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) {
            if (enableDetailedLogging) LOGGER.info("Canceling vanilla experience overlay");
            event.setCanceled(true);
            return;
        }
        
        // Cancel vanilla hotbar when custom hotbar is enabled
        if (HUDConfig.HOTBAR_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            if (enableDetailedLogging) LOGGER.info("Canceling vanilla hotbar overlay");
            event.setCanceled(true);
            return;
        }
        
        // Cancel vanilla air level when custom air is enabled
        if (HUDConfig.AIR_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.AIR_LEVEL.id())) {
            if (enableDetailedLogging) LOGGER.info("Canceling vanilla air overlay");
            event.setCanceled(true);
            return;
        }
        
        // Cancel vanilla armor level when custom armor is enabled
        if (HUDConfig.ARMOR_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.ARMOR_LEVEL.id())) {
            if (enableDetailedLogging) LOGGER.info("Canceling vanilla armor overlay");
            event.setCanceled(true);
            return;
        }
        
        // Enhanced Mount HUD handling - Cancel mount-specific overlays
        // This addresses the horse riding overlay conflicts
        if (event.getOverlay().id().getNamespace().equals("minecraft")) {
            String path = event.getOverlay().id().getPath();
            
            // Cancel mount health display (conflicts with our horse health rendering)
            if ((path.equals("mount_health") || path.contains("horse")) && 
                HUDConfig.FOOD_ENABLED.get()) {
                if (enableDetailedLogging) LOGGER.info("Canceling vanilla mount health overlay: {}", event.getOverlay().id());
                event.setCanceled(true);
                return;
            }
            
            // Cancel jump bar display (conflicts with our horse jump rendering)
            if ((path.equals("jump_bar") || path.contains("jump")) && 
                HUDConfig.EXPERIENCE_ENABLED.get()) {
                if (enableDetailedLogging) LOGGER.info("Canceling vanilla jump bar overlay: {}", event.getOverlay().id());
                event.setCanceled(true);
                return;
            }
        }
        
        // Chat positioning is complex, skip for now
        // TODO: Implement chat positioning in future version
        /*
        if (HUDConfig.CHAT_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.CHAT_PANEL.id())) {
            event.setCanceled(true);
            return;
        }
        */
    }
}