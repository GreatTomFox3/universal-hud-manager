package com.greattomfoxsora.universalhudmanager.client;

import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.Field;

/**
 * Custom ChatScreen that positions the input field at the same location as the chat display
 */
public class CustomChatScreen extends ChatScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public CustomChatScreen(String initial) {
        super(initial);
    }
    
    @Override
    protected void init() {
        // Let the parent initialize first
        super.init();
        
        if (!HUDConfig.CHAT_ENABLED.get()) {
            return;
        }
        
        try {
            // Get the current input field using reflection
            Field inputField = ChatScreen.class.getDeclaredField("f_95573_");
            inputField.setAccessible(true);
            EditBox currentInput = (EditBox) inputField.get(this);
            
            if (currentInput != null) {
                // Get chat position offset
                Vector2i chatOffset = HUDConfig.getChatPosition();
                
                // Calculate new position for input field
                // Default ChatScreen input position calculation:
                // x = (width - 204) / 2, y = height - 40
                int defaultX = (this.width - 204) / 2;
                int defaultY = this.height - 40;
                
                int newX = defaultX + chatOffset.x;
                int newY = defaultY + chatOffset.y;
                
                // Remove the old input from renderables and children
                this.removeWidget(currentInput);
                
                // Create a new EditBox at the correct position
                EditBox newInput = new EditBox(this.font, newX, newY, 204, 12, 
                    Component.translatable("chat.editBox"));
                
                // Copy properties from old input
                newInput.setMaxLength(256);
                newInput.setBordered(false);
                newInput.setValue(currentInput.getValue());
                newInput.setTextColor(0xE0E0E0);
                newInput.setTextColorUneditable(0x707070);
                // Set focus - use setFocused in 1.20.1
                newInput.setFocused(true);
                
                // Add the new input
                this.addRenderableWidget(newInput);
                this.setInitialFocus(newInput);
                
                // Update the field reference
                inputField.set(this, newInput);
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    LOGGER.debug("CustomChatScreen: Replaced input field");
                    LOGGER.debug("Original position: ({}, {})", defaultX, defaultY);
                    LOGGER.debug("New position: ({}, {})", newX, newY);
                    LOGGER.debug("Chat offset: ({}, {})", chatOffset.x, chatOffset.y);
                }
                
                // Try to adjust CommandSuggestions position if present
                try {
                    Field suggestionsField = ChatScreen.class.getDeclaredField("f_95577_");
                    suggestionsField.setAccessible(true);
                    Object commandSuggestions = suggestionsField.get(this);
                    
                    if (commandSuggestions != null) {
                        // Recreate CommandSuggestions with the new input position
                        // This is complex and might require more reflection work
                        if (HUDConfig.DEBUG_MODE.get()) {
                            LOGGER.debug("CommandSuggestions found, may need position adjustment");
                        }
                    }
                } catch (Exception e) {
                    if (HUDConfig.DEBUG_MODE.get()) {
                        LOGGER.debug("Could not adjust CommandSuggestions: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to customize ChatScreen input position", e);
        }
    }
}