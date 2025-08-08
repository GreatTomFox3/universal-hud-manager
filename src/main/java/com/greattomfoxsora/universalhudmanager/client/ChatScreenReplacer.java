package com.greattomfoxsora.universalhudmanager.client;

import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.Field;

/**
 * Replaces ChatScreen with CustomChatScreen when chat positioning is enabled
 * 
 * NOTE: Temporarily disabled - reverting to Reflection approach
 */
@OnlyIn(Dist.CLIENT)
// @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = "universalhudmanager")
public class ChatScreenReplacer {
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Replace ChatScreen with CustomChatScreen when opening
     */
    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (!HUDConfig.CHAT_ENABLED.get()) {
            return;
        }
        
        // Check if this is a vanilla ChatScreen (not our custom one)
        if (event.getScreen() instanceof ChatScreen && 
            !(event.getScreen() instanceof CustomChatScreen)) {
            
            ChatScreen originalScreen = (ChatScreen) event.getScreen();
            
            try {
                // Get the initial text from the original ChatScreen
                String initialText = getInitialText(originalScreen);
                
                // Create our custom ChatScreen with the same initial text
                CustomChatScreen customScreen = new CustomChatScreen(initialText);
                
                // Replace the screen
                event.setNewScreen(customScreen);
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    LOGGER.debug("Replaced ChatScreen with CustomChatScreen");
                    LOGGER.debug("Initial text: '{}'", initialText);
                }
                
            } catch (Exception e) {
                LOGGER.error("Failed to replace ChatScreen", e);
            }
        }
    }
    
    /**
     * Extract the initial text from the original ChatScreen
     */
    private static String getInitialText(ChatScreen chatScreen) {
        try {
            // Try to get the initial text field (f_95574_ - historyBuffer)
            Field historyBufferField = ChatScreen.class.getDeclaredField("f_95574_");
            historyBufferField.setAccessible(true);
            String historyBuffer = (String) historyBufferField.get(chatScreen);
            
            if (historyBuffer != null && !historyBuffer.isEmpty()) {
                return historyBuffer;
            }
            
            // If no history buffer, try to get current input text
            Field inputField = ChatScreen.class.getDeclaredField("f_95573_");
            inputField.setAccessible(true);
            Object input = inputField.get(chatScreen);
            
            if (input != null) {
                // This might be null if the screen hasn't been initialized yet
                // In that case, we'll return empty string
                return "";
            }
            
        } catch (Exception e) {
            if (HUDConfig.DEBUG_MODE.get()) {
                LOGGER.debug("Could not extract initial text from ChatScreen: {}", e.getMessage());
            }
        }
        
        return "";
    }
}