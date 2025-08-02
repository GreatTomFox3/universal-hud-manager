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
    
    /**
     * Cancel vanilla HUD rendering when custom positioning is enabled
     */
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        // Cancel vanilla health bar when custom health is enabled
        if (HUDConfig.HEALTH_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            LOGGER.info("Canceling vanilla health overlay");
            event.setCanceled(true);
            return;
        }
        
        // Cancel vanilla food bar when custom food is enabled
        if (HUDConfig.FOOD_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.FOOD_LEVEL.id())) {
            LOGGER.info("Canceling vanilla food overlay");
            event.setCanceled(true);
            return;
        }
        
        // Cancel vanilla experience bar when custom experience is enabled
        if (HUDConfig.EXPERIENCE_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) {
            LOGGER.info("Canceling vanilla experience overlay");
            event.setCanceled(true);
            return;
        }
        
        // Cancel vanilla hotbar when custom hotbar is enabled
        if (HUDConfig.HOTBAR_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            LOGGER.info("Canceling vanilla hotbar overlay");
            event.setCanceled(true);
            return;
        }
        
        // Cancel vanilla air level when custom air is enabled
        if (HUDConfig.AIR_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.AIR_LEVEL.id())) {
            LOGGER.info("Canceling vanilla air overlay");
            event.setCanceled(true);
            return;
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