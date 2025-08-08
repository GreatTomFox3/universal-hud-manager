package com.greattomfoxsora.universalhudmanager.client;

import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector2i;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles chat position control using CustomizeGuiOverlayEvent.Chat
 * This approach preserves all vanilla chat functionality including automatic fade behavior
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = "universalhudmanager")
public class ChatPositionHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Modify chat position using CustomizeGuiOverlayEvent.Chat
     * This preserves vanilla chat behavior (tick, fade, etc.) while allowing position control
     */
    @SubscribeEvent
    public static void onChatRender(CustomizeGuiOverlayEvent.Chat event) {
        if (!HUDConfig.CHAT_ENABLED.get()) return;
        
        // 🌟 編集モード中のチャット表示はClientTickEventで処理
        
        // Get chat position offset from config
        Vector2i chatOffset = HUDConfig.getChatPosition();
        
        // Calculate final position
        Vector2i defaultPos = HUDConfig.getDefaultChatPosition(
            event.getWindow().getGuiScaledWidth(), 
            event.getWindow().getGuiScaledHeight()
        );
        Vector2i finalPos = HUDConfig.getFinalPosition(defaultPos, chatOffset);
        
        // Apply position to vanilla chat overlay
        // This preserves all vanilla functionality (tick, fade, input handling, etc.)
        event.setPosX(finalPos.x);
        event.setPosY(finalPos.y);
        
        if (HUDConfig.DEBUG_MODE.get()) {
            LOGGER.debug("Applied chat position: x={}, y={} (offset: x={}, y={}) edit_mode={}", 
                finalPos.x, finalPos.y, chatOffset.x, chatOffset.y, HUDConfig.HUD_EDIT_MODE.get());
        }
    }
}