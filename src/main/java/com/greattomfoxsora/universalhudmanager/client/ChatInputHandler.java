package com.greattomfoxsora.universalhudmanager.client;

import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector2i;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.Field;
import java.util.WeakHashMap;

/**
 * Handles chat input field positioning to match chat display position
 * Uses Reflection approach as recommended in ChatInputField_Implementation_Guide.md
 * 
 * NOTE: Disabled to prevent floating text issue. Chat display movement works perfectly,
 * but input field positioning causes visual issues. Keeping input field at original position.
 */
@OnlyIn(Dist.CLIENT)
// @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = "universalhudmanager")
public class ChatInputHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    
    // Track which ChatScreen instances we've already modified to avoid repeated adjustments
    private static final WeakHashMap<ChatScreen, Boolean> modifiedScreens = new WeakHashMap<>();
    
    /**
     * Adjusts the chat input field position using Reflection
     * Called every client tick but only processes when ChatScreen is open
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!HUDConfig.CHAT_ENABLED.get()) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof ChatScreen chatScreen) {
            // Check if we've already modified this ChatScreen instance
            if (modifiedScreens.containsKey(chatScreen)) {
                return;
            }
            
            try {
                // Use SRG name for the EditBox field (f_95573_ in 1.20.1)
                Field inputField = ChatScreen.class.getDeclaredField("f_95573_");
                inputField.setAccessible(true);
                EditBox input = (EditBox) inputField.get(chatScreen);
                
                if (input != null) {
                    Vector2i chatOffset = HUDConfig.getChatPosition();
                    Vector2i defaultPos = HUDConfig.getDefaultChatPosition(mc.getWindow().getGuiScaledWidth(), 
                                                                           mc.getWindow().getGuiScaledHeight());
                    
                    // Calculate the actual offset from default position
                    int offsetX = chatOffset.x;
                    int offsetY = chatOffset.y;
                    
                    // Store original position for debug logging
                    int originalX = input.getX();
                    int originalY = input.getY();
                    
                    // Apply the same offset as the chat display
                    int newX = originalX + offsetX;
                    int newY = originalY + offsetY;
                    
                    // Try different approaches to move the EditBox
                    try {
                        // Method 1: Try setPosition if it exists
                        input.setPosition(newX, newY);
                        if (HUDConfig.DEBUG_MODE.get()) {
                            LOGGER.debug("Used setPosition method successfully");
                        }
                    } catch (Exception e1) {
                        try {
                            // Method 2: Use setX and setY separately
                            input.setX(newX);
                            input.setY(newY);
                            if (HUDConfig.DEBUG_MODE.get()) {
                                LOGGER.debug("Used setX/setY methods successfully");
                            }
                        } catch (Exception e2) {
                            // Method 3: Direct field manipulation
                            try {
                                Field xField = input.getClass().getDeclaredField("x");
                                Field yField = input.getClass().getDeclaredField("y");
                                xField.setAccessible(true);
                                yField.setAccessible(true);
                                xField.setInt(input, newX);
                                yField.setInt(input, newY);
                                if (HUDConfig.DEBUG_MODE.get()) {
                                    LOGGER.debug("Used direct field manipulation successfully");
                                }
                            } catch (Exception e3) {
                                LOGGER.warn("All positioning methods failed for EditBox");
                            }
                        }
                    }
                    
                    // Mark this screen as modified
                    modifiedScreens.put(chatScreen, true);
                    
                    if (HUDConfig.DEBUG_MODE.get()) {
                        LOGGER.debug("ChatScreen opened, modified input position");
                        LOGGER.debug("Original position: x={}, y={}", originalX, originalY);
                        LOGGER.debug("New position: x={}, y={}", newX, newY);
                        LOGGER.debug("Chat offset: x={}, y={}", offsetX, offsetY);
                    }
                    
                    // Also try to adjust CommandSuggestions if present (f_95577_)
                    try {
                        Field suggestionsField = ChatScreen.class.getDeclaredField("f_95577_");
                        suggestionsField.setAccessible(true);
                        Object commandSuggestions = suggestionsField.get(chatScreen);
                        
                        if (commandSuggestions != null) {
                            // Try to move CommandSuggestions using reflection
                            try {
                                Class<?> suggestionsClass = commandSuggestions.getClass();
                                
                                // Try to find and modify position-related fields
                                // CommandSuggestions likely has x, y fields or similar
                                Field[] suggestionsFields = suggestionsClass.getDeclaredFields();
                                for (Field field : suggestionsFields) {
                                    if (field.getType() == int.class && 
                                        (field.getName().contains("x") || field.getName().contains("X"))) {
                                        field.setAccessible(true);
                                        int currentValue = field.getInt(commandSuggestions);
                                        field.setInt(commandSuggestions, currentValue + offsetX);
                                        if (HUDConfig.DEBUG_MODE.get()) {
                                            LOGGER.debug("Adjusted CommandSuggestions X field: {} from {} to {}", 
                                                field.getName(), currentValue, currentValue + offsetX);
                                        }
                                    } else if (field.getType() == int.class && 
                                              (field.getName().contains("y") || field.getName().contains("Y"))) {
                                        field.setAccessible(true);
                                        int currentValue = field.getInt(commandSuggestions);
                                        field.setInt(commandSuggestions, currentValue + offsetY);
                                        if (HUDConfig.DEBUG_MODE.get()) {
                                            LOGGER.debug("Adjusted CommandSuggestions Y field: {} from {} to {}", 
                                                field.getName(), currentValue, currentValue + offsetY);
                                        }
                                    }
                                }
                            } catch (Exception innerE) {
                                if (HUDConfig.DEBUG_MODE.get()) {
                                    LOGGER.debug("Could not adjust CommandSuggestions position: {}", innerE.getMessage());
                                }
                            }
                        }
                    } catch (NoSuchFieldException e) {
                        // CommandSuggestions field not found, this is okay
                        if (HUDConfig.DEBUG_MODE.get()) {
                            LOGGER.debug("CommandSuggestions field not found (normal if not using commands)");
                        }
                    }
                }
            } catch (NoSuchFieldException e) {
                LOGGER.error("Failed to find ChatScreen input field (f_95573_). The SRG name might have changed.", e);
            } catch (Exception e) {
                LOGGER.error("Failed to modify ChatScreen input position", e);
            }
        }
    }
    
    /**
     * Clear the modified screens cache periodically to prevent memory leaks
     * WeakHashMap should handle this automatically, but we can be extra safe
     */
    public static void clearCache() {
        modifiedScreens.clear();
    }
}