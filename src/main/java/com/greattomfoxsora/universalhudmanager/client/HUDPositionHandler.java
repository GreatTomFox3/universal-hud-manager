package com.greattomfoxsora.universalhudmanager.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.greattomfoxsora.universalhudmanager.core.HUDElement;
import com.greattomfoxsora.universalhudmanager.core.HUDRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.greattomfoxsora.universalhudmanager.config.HUDConfig;

/**
 * Handles HUD positioning and rendering modifications
 * 
 * This class intercepts HUD rendering events and applies custom positioning
 * based on the Universal HUD Manager configuration.
 * 
 * @author GreatTomFox & Sora
 */
@Mod.EventBusSubscriber(modid = "universalhudmanager", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HUDPositionHandler {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean isEditMode = false;
    
    /**
     * デバッグログが有効かどうかチェック
     */
    private static boolean isDebugEnabled() {
        return HUDConfig.DEBUG_MODE.get();
    }
    private static HUDElement draggedElement = null;
    private static int dragOffsetX = 0;
    private static int dragOffsetY = 0;
    
    /**
     * Pre-render event - called before GUI elements are rendered
     * This is where we can modify positions before rendering
     * 
     * Note: Edit mode rendering is now handled by HudEditScreen
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        // Edit mode rendering is now handled by HudEditScreen
        // This method is reserved for future position modification logic
    }
    
    /**
     * Post-render event - called after GUI elements are rendered
     * Here we can draw additional UI elements like drag handles
     * 
     * Note: Edit mode rendering is now handled by HudEditScreen
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        // Edit mode rendering is now handled by HudEditScreen
        // This method is reserved for future overlay logic
    }
    
    /**
     * Draw edit mode overlay
     */
    private static void drawEditModeOverlay(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        // Draw semi-transparent overlay
        guiGraphics.fill(0, 0, screenWidth, screenHeight, 0x80000000);
        
        // Draw title
        String title = "Universal HUD Manager - Edit Mode";
        int titleWidth = mc.font.width(title);
        guiGraphics.drawString(mc.font, title, 
                             (screenWidth - titleWidth) / 2, 10, 
                             0xFFFFFF, true);
        
        // Draw instructions
        String instructions = "Press H to exit edit mode";
        int instrWidth = mc.font.width(instructions);
        guiGraphics.drawString(mc.font, instructions, 
                             (screenWidth - instrWidth) / 2, 25, 
                             0xAAAAAA, true);
    }
    
    /**
     * Draw outlines around HUD elements for editing
     */
    private static void drawHUDElementOutlines(GuiGraphics guiGraphics) {
        for (HUDElement element : HUDRegistry.getAllHUDs()) {
            if (!element.isEnabled()) continue;
            
            int x = element.getX();
            int y = element.getY();
            int width = element.getWidth();
            int height = element.getHeight();
            
            // Draw outline
            int outlineColor = element.isDraggable() ? 0xFF00FF00 : 0xFFFF0000;
            drawOutline(guiGraphics, x, y, width, height, outlineColor);
            
            // Draw label
            String label = element.getDisplayName();
            guiGraphics.drawString(Minecraft.getInstance().font, label, 
                                 x, y - 10, 0xFFFFFF, true);
        }
    }
    
    /**
     * Draw an outline rectangle
     */
    private static void drawOutline(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        // Top
        guiGraphics.fill(x, y, x + width, y + 1, color);
        // Bottom  
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color);
        // Left
        guiGraphics.fill(x, y, x + 1, y + height, color);
        // Right
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color);
    }
    
    /**
     * Toggle edit mode
     */
    public static void toggleEditMode() {
        Minecraft mc = Minecraft.getInstance();
        
        LOGGER.info("toggleEditMode called! Current mode: {}", isEditMode ? "ON" : "OFF");
        
        if (!isEditMode) {
            // 編集モードON - HUD編集スクリーンを開く
            isEditMode = true;
            // 🌟 HUDConfig.HUD_EDIT_MODE も同期して有効化（Air Bar/Chat表示用）
            HUDConfig.HUD_EDIT_MODE.set(true);
            LOGGER.info("Opening HUD Edit Screen... (HUD_EDIT_MODE enabled for Air Bar/Chat visibility)");
            
            // HUD要素を再発見
            HUDRegistry.discoverVanillaHUDs();
            HUDRegistry.discoverModHUDs();
            if (isDebugEnabled()) {
                LOGGER.debug("HUD Registry: {}", HUDRegistry.getStats());
            }
            
            // HUD編集スクリーンを表示
            HudEditScreen editScreen = new HudEditScreen(mc.screen);
            mc.setScreen(editScreen);
            LOGGER.info("HudEditScreen created and set as current screen");
            
        } else {
            // 編集モードOFF - スクリーンを閉じる
            setEditMode(false);
            if (mc.screen instanceof HudEditScreen) {
                mc.setScreen(null);
            }
        }
        
        if (isDebugEnabled()) {
            LOGGER.debug("Edit mode: {}", isEditMode ? "ON" : "OFF");
        }
    }
    
    /**
     * Set edit mode state (for external control)
     */
    public static void setEditMode(boolean editMode) {
        isEditMode = editMode;
        // 🌟 HUDConfig.HUD_EDIT_MODE も同期（Air Bar/Chat表示制御）
        HUDConfig.HUD_EDIT_MODE.set(editMode);
        if (isDebugEnabled()) {
            LOGGER.debug("Edit mode set to: {} (HUDConfig.HUD_EDIT_MODE synchronized)", isEditMode ? "ON" : "OFF");
        }
    }
    
    /**
     * Check if edit mode is active
     */
    public static boolean isEditMode() {
        return isEditMode;
    }
    
    /**
     * Handle mouse drag for HUD elements
     */
    public static void handleMouseDrag(int mouseX, int mouseY) {
        if (!isEditMode || draggedElement == null) return;
        
        int newX = mouseX - dragOffsetX;
        int newY = mouseY - dragOffsetY;
        
        // Clamp to screen bounds
        Minecraft mc = Minecraft.getInstance();
        newX = Math.max(0, Math.min(newX, mc.getWindow().getGuiScaledWidth() - draggedElement.getWidth()));
        newY = Math.max(0, Math.min(newY, mc.getWindow().getGuiScaledHeight() - draggedElement.getHeight()));
        
        draggedElement.setX(newX);
        draggedElement.setY(newY);
    }
    
    /**
     * Handle mouse press for starting drag
     */
    public static boolean handleMousePress(int mouseX, int mouseY) {
        if (!isEditMode) return false;
        
        for (HUDElement element : HUDRegistry.getAllHUDs()) {
            if (element.isDraggable() && element.contains(mouseX, mouseY)) {
                draggedElement = element;
                dragOffsetX = mouseX - element.getX();
                dragOffsetY = mouseY - element.getY();
                if (isDebugEnabled()) {
                    LOGGER.debug("Started dragging: {}", element.getDisplayName());
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Handle mouse release for ending drag
     */
    public static void handleMouseRelease() {
        if (draggedElement != null) {
            if (isDebugEnabled()) {
                LOGGER.debug("Stopped dragging: {} at ({}, {})", 
                           draggedElement.getDisplayName(), 
                           draggedElement.getX(), 
                           draggedElement.getY());
            }
            draggedElement = null;
        }
    }
}