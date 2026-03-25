package com.greattomfoxsora.universalhudmanager.client;

import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * シンプルなキーバインドシステム
 * H キーでHUD編集画面を開く
 * 
 * @author GreatTomFox & Sora
 */
@Mod.EventBusSubscriber(modid = "universalhudmanager", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyBindings {
    
    public static final String CATEGORY = "key.categories.universalhudmanager";
    
    public static KeyMapping HUD_EDIT_KEY;
    
    /**
     * Register key mappings
     */
    public static void register(RegisterKeyMappingsEvent event) {
        HUD_EDIT_KEY = new KeyMapping(
            "key.universalhudmanager.hud_edit", 
            GLFW.GLFW_KEY_H, 
            CATEGORY
        );
        event.register(HUD_EDIT_KEY);
    }
    
    /**
     * Handle key input events
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        
        // Check if we're in-game and not in any GUI
        if (mc.screen != null || mc.player == null) {
            return;
        }
        
        // H key pressed - toggle HUD edit mode
        if (HUD_EDIT_KEY.consumeClick()) {
            if (HUDConfig.DEBUG_MODE.get()) {
                System.out.println("🔑 H key pressed - opening HUD edit screen");
            }
            
            // Open HUD edit screen
            mc.setScreen(new HudEditScreen());
        }
    }
}