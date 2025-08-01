package com.greattomfoxsora.universalhudmanager.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Key bindings for Universal HUD Manager
 * 
 * @author GreatTomFox & Sora
 */
@Mod.EventBusSubscriber(modid = "universalhudmanager", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {
    
    // Key mapping for toggling edit mode
    public static final KeyMapping TOGGLE_EDIT_MODE = new KeyMapping(
            "key.universalhudmanager.toggle_edit_mode",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,  // Default: H key
            "key.categories.universalhudmanager"
    );
    
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_EDIT_MODE);
    }
    
    /**
     * Handle key input events
     */
    @Mod.EventBusSubscriber(modid = "universalhudmanager", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class KeyInputHandler {
        
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (TOGGLE_EDIT_MODE.consumeClick()) {
                HUDPositionHandler.toggleEditMode();
            }
        }
        
        @SubscribeEvent
        public static void onMouseInput(InputEvent.MouseButton.Pre event) {
            if (HUDPositionHandler.isEditMode()) {
                if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    if (event.getAction() == GLFW.GLFW_PRESS) {
                        // Minecraft.getInstance()からマウス座標を取得
                        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                        double mouseX = mc.mouseHandler.xpos() * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getScreenWidth();
                        double mouseY = mc.mouseHandler.ypos() * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getScreenHeight();
                        
                        if (HUDPositionHandler.handleMousePress((int)mouseX, (int)mouseY)) {
                            event.setCanceled(true);
                        }
                    } else if (event.getAction() == GLFW.GLFW_RELEASE) {
                        HUDPositionHandler.handleMouseRelease();
                    }
                }
            }
        }
    }
}