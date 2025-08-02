package com.greattomfoxsora.universalhudmanager.client;

import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector2i;

import java.lang.reflect.Method;

/**
 * Resource Pack Compatible HUD Overlays
 * Uses vanilla GUI rendering methods with position adjustment for full Resource Pack support
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ResourcePackCompatibleOverlays {
    
    // Vanilla GUI textures
    private static final net.minecraft.resources.ResourceLocation GUI_ICONS_LOCATION = 
        new net.minecraft.resources.ResourceLocation("textures/gui/icons.png");
    private static final net.minecraft.resources.ResourceLocation WIDGETS_LOCATION = 
        new net.minecraft.resources.ResourceLocation("textures/gui/widgets.png");
    
    // Modern 1.20.1 Experience Bar Sprites
    private static final net.minecraft.resources.ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = 
        new net.minecraft.resources.ResourceLocation("hud/experience_bar_background");
    private static final net.minecraft.resources.ResourceLocation EXPERIENCE_BAR_PROGRESS_SPRITE = 
        new net.minecraft.resources.ResourceLocation("hud/experience_bar_progress");
    
    // Cached reflection methods for performance
    private static Method renderPlayerHealthMethod;
    private static Method renderFoodMethod;
    private static Method renderExperienceBarMethod;
    private static Method renderHotbarMethod;
    private static Method renderAirLevelMethod;
    
    /**
     * Health Bar Overlay - Resource Pack compatible
     */
    public static final IGuiOverlay HEALTH_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.HEALTH_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderSurvivalElements()) return;
        
        // Get position offset
        Vector2i offset = HUDConfig.getHealthPosition();
        // Always render when enabled, even with zero offset
        
        // Apply position transformation and render using vanilla method
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, 0);
        
        try {
            // Use vanilla GUI rendering method for full Resource Pack support
            renderVanillaHealth(gui, graphics, partialTick, width, height);
        } catch (Exception e) {
            // Fallback to simple rendering if reflection fails
            renderSimpleHealth(graphics, width, height, player);
        }
        
        poseStack.popPose();
    };
    
    /**
     * Food Bar Overlay - Resource Pack compatible
     */
    public static final IGuiOverlay FOOD_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.FOOD_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderSurvivalElements()) return;
        
        // Get position offset
        Vector2i offset = HUDConfig.getFoodPosition();
        // Always render when enabled, even with zero offset
        
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, 0);
        
        try {
            renderVanillaFood(gui, graphics, partialTick, width, height);
        } catch (Exception e) {
            renderSimpleFood(graphics, width, height, player);
        }
        
        poseStack.popPose();
    };
    
    /**
     * Experience Bar Overlay - Resource Pack compatible
     */
    public static final IGuiOverlay EXPERIENCE_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.EXPERIENCE_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderSurvivalElements()) return;
        
        Vector2i offset = HUDConfig.getExperiencePosition();
        // Always render when enabled, even with zero offset
        
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, 0);
        
        try {
            renderVanillaExperience(gui, graphics, partialTick, width, height);
        } catch (Exception e) {
            renderSimpleExperience(graphics, width, height, player);
        }
        
        poseStack.popPose();
    };
    
    /**
     * Hotbar Overlay - Resource Pack compatible
     */
    public static final IGuiOverlay HOTBAR_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.HOTBAR_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderHotbar()) return;
        
        Vector2i offset = HUDConfig.getHotbarPosition();
        // Always render when enabled, even with zero offset
        
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, 0);
        
        try {
            renderVanillaHotbar(gui, graphics, partialTick, width, height);
        } catch (Exception e) {
            renderSimpleHotbar(graphics, width, height, player, partialTick);
        }
        
        poseStack.popPose();
    };
    
    /**
     * Air Bar Overlay - Resource Pack compatible
     */
    public static final IGuiOverlay AIR_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.AIR_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderSurvivalElements()) return;
        
        // Only render when underwater or in edit mode
        if (player.getAirSupply() >= player.getMaxAirSupply() && !HUDConfig.HUD_EDIT_MODE.get()) return;
        
        Vector2i offset = HUDConfig.getAirPosition();
        // Always render when enabled, even with zero offset
        
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, 0);
        
        try {
            renderVanillaAir(gui, graphics, partialTick, width, height);
        } catch (Exception e) {
            renderSimpleAir(graphics, width, height, player);
        }
        
        poseStack.popPose();
    };
    
    /**
     * Register all overlays
     */
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "universal_health_rp", HEALTH_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "universal_food_rp", FOOD_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "universal_experience_rp", EXPERIENCE_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "universal_hotbar_rp", HOTBAR_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.AIR_LEVEL.id(), "universal_air_rp", AIR_OVERLAY);
    }
    
    // Vanilla GUI method calls - Simplified implementation for now
    
    private static void renderVanillaHealth(Gui gui, GuiGraphics graphics, float partialTick, int width, int height) throws Exception {
        // For now, throw exception to use fallback
        // TODO: Implement actual vanilla GUI method calls
        throw new Exception("Vanilla method not implemented yet");
    }
    
    private static void renderVanillaFood(Gui gui, GuiGraphics graphics, float partialTick, int width, int height) throws Exception {
        throw new Exception("Vanilla method not implemented yet");
    }
    
    private static void renderVanillaExperience(Gui gui, GuiGraphics graphics, float partialTick, int width, int height) throws Exception {
        throw new Exception("Vanilla method not implemented yet");
    }
    
    private static void renderVanillaHotbar(Gui gui, GuiGraphics graphics, float partialTick, int width, int height) throws Exception {
        throw new Exception("Vanilla method not implemented yet");
    }
    
    private static void renderVanillaAir(Gui gui, GuiGraphics graphics, float partialTick, int width, int height) throws Exception {
        throw new Exception("Vanilla method not implemented yet");
    }
    
    // Fallback simple rendering methods (from original implementation)
    
    private static void renderSimpleHealth(GuiGraphics graphics, int width, int height, LocalPlayer player) {
        // Full health bar implementation
        Vector2i defaultPos = HUDConfig.getDefaultHealthPosition(width, height);
        Vector2i offset = HUDConfig.getHealthPosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(defaultPos, offset);
        
        net.minecraft.resources.ResourceLocation GUI_ICONS_LOCATION = 
            new net.minecraft.resources.ResourceLocation("textures/gui/icons.png");
        
        // Get player health data
        int health = net.minecraft.util.Mth.ceil(player.getHealth());
        int maxHealth = net.minecraft.util.Mth.ceil(player.getMaxHealth());
        int absorption = net.minecraft.util.Mth.ceil(player.getAbsorptionAmount());
        
        // Calculate rows for hearts
        int rows = net.minecraft.util.Mth.ceil((maxHealth + absorption) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (rows - 2), 3);
        
        // Render all hearts
        for (int i = 0; i < net.minecraft.util.Mth.ceil((maxHealth + absorption) / 2.0F); ++i) {
            int row = i / 10;
            int col = i % 10;
            int heartX = finalPos.x + col * 8;
            int heartY = finalPos.y - row * rowHeight;
            
            // Background heart
            graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 16, 0, 9, 9);
            
            // Heart type and fill
            if (i < maxHealth / 2) {
                // Normal hearts
                if (i * 2 + 1 < health) {
                    graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 52, 0, 9, 9); // Full heart
                } else if (i * 2 + 1 == health) {
                    graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 61, 0, 9, 9); // Half heart
                }
            } else {
                // Absorption hearts
                int absIndex = i - maxHealth / 2;
                if (absIndex * 2 + 1 < absorption) {
                    graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 160, 0, 9, 9); // Full absorption
                } else if (absIndex * 2 + 1 == absorption) {
                    graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 169, 0, 9, 9); // Half absorption
                }
            }
        }
    }
    
    private static void renderSimpleFood(GuiGraphics graphics, int width, int height, LocalPlayer player) {
        Vector2i defaultPos = HUDConfig.getDefaultFoodPosition(width, height);
        Vector2i offset = HUDConfig.getFoodPosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(defaultPos, offset);
        
        net.minecraft.resources.ResourceLocation GUI_ICONS_LOCATION = 
            new net.minecraft.resources.ResourceLocation("textures/gui/icons.png");
        
        // Get player food data
        net.minecraft.world.food.FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();
        
        // Render food icons (right to left like vanilla)
        for (int i = 0; i < 10; ++i) {
            int col = 9 - i; // Right to left
            int foodX = finalPos.x - col * 8 - 9; // Adjust for right alignment
            int foodY = finalPos.y;
            
            // Background
            graphics.blit(GUI_ICONS_LOCATION, foodX, foodY, 16, 27, 9, 9);
            
            // Food icon based on food level
            if (i * 2 + 1 < foodLevel) {
                graphics.blit(GUI_ICONS_LOCATION, foodX, foodY, 52, 27, 9, 9); // Full food
            } else if (i * 2 + 1 == foodLevel) {
                graphics.blit(GUI_ICONS_LOCATION, foodX, foodY, 61, 27, 9, 9); // Half food
            }
        }
    }
    
    private static void renderSimpleExperience(GuiGraphics graphics, int width, int height, LocalPlayer player) {
        Vector2i defaultPos = HUDConfig.getDefaultExperiencePosition(width, height);
        Vector2i offset = HUDConfig.getExperiencePosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(defaultPos, offset);
        
        // Only render if player has experience levels
        if (player.experienceLevel > 0) {
            // Use icons.png with correct coordinates for Forge 1.20.1
            renderVanillaAccurateExperienceBar(graphics, finalPos.x, finalPos.y, player);
        }
    }
    
    /**
     * Vanilla-Accurate Experience Bar rendering for Forge 1.20.1
     * Uses icons.png with correct coordinates and proper text rendering
     */
    private static void renderVanillaAccurateExperienceBar(GuiGraphics graphics, int x, int y, LocalPlayer player) {
        // Use icons.png at correct coordinates for Forge 1.20.1
        // Background bar at (0, 64)
        graphics.blit(GUI_ICONS_LOCATION, x, y, 0, 64, 182, 5);
        
        // Progress bar at (0, 69) with width proportional to experience progress
        if (player.experienceProgress > 0) {
            int fillWidth = (int) (player.experienceProgress * 183.0F); // +1 to round up like vanilla
            graphics.blit(GUI_ICONS_LOCATION, x, y, 0, 69, fillWidth, 5);
        }
        
        // Experience level text - Vanilla-accurate positioning with drop shadow
        String levelText = String.valueOf(player.experienceLevel);
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        int centerX = x + 182 / 2; // Center of the experience bar
        int textY = y - 6; // Perfect distance from bar like vanilla (was -5, now -6)
        
        // Use drawCenteredString for vanilla-accurate appearance
        // Note: Forge 1.20.1 drawCenteredString automatically includes drop shadow
        graphics.drawCenteredString(mc.font, levelText, centerX, textY, 0x80FF20);
    }
    graphics.drawString
    private static void renderSimpleHotbar(GuiGraphics graphics, int width, int height, LocalPlayer player, float partialTick) {
        Vector2i defaultPos = HUDConfig.getDefaultHotbarPosition(width, height);
        Vector2i offset = HUDConfig.getHotbarPosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(defaultPos, offset);
        
        // Render hotbar background using WIDGETS_LOCATION
        graphics.blit(WIDGETS_LOCATION, finalPos.x, finalPos.y, 0, 0, 182, 22);
        
        // Render hotbar selection highlight
        int selectedSlot = player.getInventory().selected;
        graphics.blit(WIDGETS_LOCATION, finalPos.x - 1 + selectedSlot * 20, finalPos.y - 1, 0, 22, 24, 24);
        
        // Render items in hotbar slots
        for (int i = 0; i < 9; ++i) {
            int slotX = finalPos.x + 3 + i * 20;
            int slotY = finalPos.y + 3;
            
            net.minecraft.world.item.ItemStack itemstack = player.getInventory().items.get(i);
            if (!itemstack.isEmpty()) {
                graphics.renderItem(itemstack, slotX, slotY);
                graphics.renderItemDecorations(net.minecraft.client.Minecraft.getInstance().font, itemstack, slotX, slotY);
            }
        }
    }
    
    private static void renderSimpleAir(GuiGraphics graphics, int width, int height, LocalPlayer player) {
        Vector2i defaultPos = HUDConfig.getDefaultAirPosition(width, height);
        Vector2i offset = HUDConfig.getAirPosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(defaultPos, offset);
        
        net.minecraft.resources.ResourceLocation GUI_ICONS_LOCATION = 
            new net.minecraft.resources.ResourceLocation("textures/gui/icons.png");
        
        // Get air data
        int air = player.getAirSupply();
        int maxAir = player.getMaxAirSupply();
        int bubbles = net.minecraft.util.Mth.ceil((double)(air - 2) * 10.0D / (double)maxAir);
        int poppedBubbles = net.minecraft.util.Mth.ceil((double)air * 10.0D / (double)maxAir) - bubbles;
        
        // Render air bubbles (right to left like vanilla)
        for (int i = 0; i < bubbles + poppedBubbles; ++i) {
            int col = 9 - i; // Right to left
            int bubbleX = finalPos.x - col * 8 - 9; // Adjust for right alignment
            int bubbleY = finalPos.y;
            
            if (i < bubbles) {
                graphics.blit(GUI_ICONS_LOCATION, bubbleX, bubbleY, 16, 18, 9, 9); // Full bubble
            } else {
                graphics.blit(GUI_ICONS_LOCATION, bubbleX, bubbleY, 25, 18, 9, 9); // Popped bubble
            }
        }
    }
    
    /**
     * Check if survival elements should be rendered
     */
    private static boolean shouldRenderSurvivalElements() {
        Minecraft mc = Minecraft.getInstance();
        return !mc.options.hideGui && mc.gameMode != null && mc.gameMode.canHurtPlayer();
    }
    
    /**
     * Check if hotbar should be rendered (including creative mode)
     */
    private static boolean shouldRenderHotbar() {
        Minecraft mc = Minecraft.getInstance();
        return !mc.options.hideGui;  // Hotbar shows in both survival and creative
    }
}