package com.greattomfoxsora.universalhudmanager.client;

import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector2i;

/**
 * Universal HUD Overlays System
 * Implements ForgeGuiOverlay-based HUD positioning using Cold-Sweat methodology
 */
// DISABLED: Replaced by ResourcePackCompatibleOverlays for Resource Pack support
// @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class UniversalOverlays {
    
    // Vanilla GUI textures
    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    
    /**
     * Health Bar Overlay - Custom positioned health hearts
     */
    public static final IGuiOverlay HEALTH_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.HEALTH_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderSurvivalElements()) return;
        
        // Get custom position
        Vector2i defaultPos = HUDConfig.getDefaultHealthPosition(width, height);
        Vector2i offset = HUDConfig.getHealthPosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(defaultPos, offset);
        
        // Render custom health bar
        renderHealthBar(graphics, finalPos.x, finalPos.y, player);
    };
    
    /**
     * Food Bar Overlay - Custom positioned food/hunger bar
     */
    public static final IGuiOverlay FOOD_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.FOOD_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderSurvivalElements()) return;
        
        // Get custom position
        Vector2i defaultPos = HUDConfig.getDefaultFoodPosition(width, height);
        Vector2i offset = HUDConfig.getFoodPosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(defaultPos, offset);
        
        // Render custom food bar
        renderFoodBar(graphics, finalPos.x, finalPos.y, player);
    };
    
    /**
     * Experience Bar Overlay - Custom positioned experience bar
     */
    public static final IGuiOverlay EXPERIENCE_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.EXPERIENCE_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderSurvivalElements()) return;
        
        // Get custom position
        Vector2i defaultPos = HUDConfig.getDefaultExperiencePosition(width, height);
        Vector2i offset = HUDConfig.getExperiencePosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(defaultPos, offset);
        
        // Render custom experience bar
        renderExperienceBar(graphics, finalPos.x, finalPos.y, player, width);
    };
    
    /**
     * Air Bar Overlay - Custom positioned air/oxygen bar (underwater)
     */
    public static final IGuiOverlay AIR_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.AIR_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderSurvivalElements()) return;
        
        // Only render when underwater or in edit mode
        if (player.getAirSupply() >= player.getMaxAirSupply() && !HUDConfig.HUD_EDIT_MODE.get()) return;
        
        // Get custom position
        Vector2i defaultPos = HUDConfig.getDefaultAirPosition(width, height);
        Vector2i offset = HUDConfig.getAirPosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(defaultPos, offset);
        
        // Render custom air bar
        renderAirBar(graphics, finalPos.x, finalPos.y, player);
    };
    
    /**
     * Hotbar Overlay - Custom positioned hotbar
     */
    public static final IGuiOverlay HOTBAR_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.HOTBAR_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderSurvivalElements()) return;
        
        // Get custom position
        Vector2i defaultPos = HUDConfig.getDefaultHotbarPosition(width, height);
        Vector2i offset = HUDConfig.getHotbarPosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(defaultPos, offset);
        
        // Render custom hotbar
        renderHotbar(graphics, finalPos.x, finalPos.y, player, partialTick);
    };
    
    /**
     * Chat Overlay - Custom positioned chat
     */
    public static final IGuiOverlay CHAT_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.CHAT_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        
        // Get custom position
        Vector2i defaultPos = HUDConfig.getDefaultChatPosition(width, height);
        Vector2i offset = HUDConfig.getChatPosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(defaultPos, offset);
        
        // Note: Chat rendering is complex and handled by ChatComponent
        // For now, we'll register the overlay but chat positioning may need special handling
    };
    
    /**
     * Register all custom overlays with Forge
     */
    // @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        // Register above vanilla overlays to replace them when enabled
        event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "universal_health", HEALTH_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "universal_food", FOOD_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "universal_experience", EXPERIENCE_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.AIR_LEVEL.id(), "universal_air", AIR_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "universal_hotbar", HOTBAR_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.CHAT_PANEL.id(), "universal_chat", CHAT_OVERLAY);
    }
    
    // Rendering methods based on vanilla Minecraft GUI rendering
    
    /**
     * Render health bar with hearts
     */
    private static void renderHealthBar(GuiGraphics graphics, int x, int y, Player player) {
        RenderSystem.enableBlend();
        
        int health = Mth.ceil(player.getHealth());
        int maxHealth = Mth.ceil(player.getMaxHealth());
        int absorption = Mth.ceil(player.getAbsorptionAmount());
        
        int rows = Mth.ceil((maxHealth + absorption) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (rows - 2), 3);
        
        // Render hearts
        for (int i = 0; i < Mth.ceil((maxHealth + absorption) / 2.0F); ++i) {
            int row = i / 10;
            int col = i % 10;
            int heartX = x + col * 8;
            int heartY = y - row * rowHeight;
            
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
        
        RenderSystem.disableBlend();
    }
    
    /**
     * Render food bar with hunger icons
     */
    private static void renderFoodBar(GuiGraphics graphics, int x, int y, Player player) {
        RenderSystem.enableBlend();
        
        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();
        
        // Render food icons (right to left)
        for (int i = 0; i < 10; ++i) {
            int col = 9 - i; // Right to left
            int foodX = x - col * 8 - 9; // Adjust for right alignment
            int foodY = y;
            
            // Background
            graphics.blit(GUI_ICONS_LOCATION, foodX, foodY, 16, 27, 9, 9);
            
            // Food icon
            if (i * 2 + 1 < foodLevel) {
                graphics.blit(GUI_ICONS_LOCATION, foodX, foodY, 52, 27, 9, 9); // Full food
            } else if (i * 2 + 1 == foodLevel) {
                graphics.blit(GUI_ICONS_LOCATION, foodX, foodY, 61, 27, 9, 9); // Half food
            }
        }
        
        RenderSystem.disableBlend();
    }
    
    /**
     * Render experience bar
     */
    private static void renderExperienceBar(GuiGraphics graphics, int x, int y, Player player, int screenWidth) {
        RenderSystem.enableBlend();
        
        if (player.experienceLevel > 0) {
            // Experience bar background
            graphics.blit(GUI_ICONS_LOCATION, x, y, 0, 64, 182, 5);
            
            // Experience bar fill
            if (player.experienceProgress > 0) {
                int width = (int) (player.experienceProgress * 183.0F);
                graphics.blit(GUI_ICONS_LOCATION, x, y, 0, 69, width, 5);
            }
            
            // Experience level text
            String levelText = String.valueOf(player.experienceLevel);
            int textX = (screenWidth - Minecraft.getInstance().font.width(levelText)) / 2;
            int textY = y - 15;
            graphics.drawString(Minecraft.getInstance().font, levelText, textX, textY, 8453920);
        }
        
        RenderSystem.disableBlend();
    }
    
    /**
     * Render air bar (bubbles)
     */
    private static void renderAirBar(GuiGraphics graphics, int x, int y, Player player) {
        RenderSystem.enableBlend();
        
        int air = player.getAirSupply();
        int maxAir = player.getMaxAirSupply();
        int bubbles = Mth.ceil((double)(air - 2) * 10.0D / (double)maxAir);
        int poppedBubbles = Mth.ceil((double)air * 10.0D / (double)maxAir) - bubbles;
        
        // Render bubbles (right to left)
        for (int i = 0; i < bubbles + poppedBubbles; ++i) {
            int col = 9 - i; // Right to left
            int bubbleX = x - col * 8 - 9; // Adjust for right alignment
            int bubbleY = y;
            
            if (i < bubbles) {
                graphics.blit(GUI_ICONS_LOCATION, bubbleX, bubbleY, 16, 18, 9, 9); // Full bubble
            } else {
                graphics.blit(GUI_ICONS_LOCATION, bubbleX, bubbleY, 25, 18, 9, 9); // Popped bubble
            }
        }
        
        RenderSystem.disableBlend();
    }
    
    /**
     * Render hotbar with items and selection
     */
    private static void renderHotbar(GuiGraphics graphics, int x, int y, Player player, float partialTick) {
        RenderSystem.enableBlend();
        
        // Render hotbar background
        graphics.blit(GUI_ICONS_LOCATION, x, y, 0, 0, 182, 22);
        
        // Render hotbar selection
        int selectedSlot = player.getInventory().selected;
        graphics.blit(GUI_ICONS_LOCATION, x - 1 + selectedSlot * 20, y - 1, 0, 22, 24, 24);
        
        // Render items in hotbar
        for (int i = 0; i < 9; ++i) {
            int slotX = x + 3 + i * 20;
            int slotY = y + 3;
            
            net.minecraft.world.item.ItemStack itemstack = player.getInventory().items.get(i);
            if (!itemstack.isEmpty()) {
                graphics.renderItem(itemstack, slotX, slotY);
                graphics.renderItemDecorations(Minecraft.getInstance().font, itemstack, slotX, slotY);
            }
        }
        
        RenderSystem.disableBlend();
    }
    
    /**
     * Check if survival elements should be rendered
     */
    private static boolean shouldRenderSurvivalElements() {
        Minecraft mc = Minecraft.getInstance();
        return !mc.options.hideGui && mc.gameMode != null && mc.gameMode.canHurtPlayer();
    }
}