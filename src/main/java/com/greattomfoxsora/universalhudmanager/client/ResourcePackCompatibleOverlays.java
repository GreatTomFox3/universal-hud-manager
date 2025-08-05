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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Resource Pack Compatible HUD Overlays
 * Uses vanilla GUI rendering methods with position adjustment for full Resource Pack support
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ResourcePackCompatibleOverlays {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourcePackCompatibleOverlays.class);
    
    // Performance optimization: Cache positions to avoid recalculation every frame
    private static int lastScreenWidth = -1, lastScreenHeight = -1;
    private static final java.util.Map<String, Vector2i> POSITION_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    
    // デバッグフラグ - 座標測定時のみtrue
    private static boolean DEBUG_POSITIONS = false; // Production build
    private static boolean debugLogPrinted = false;
    
    // Vanilla HUDの実際の位置を取得するヘルパーメソッド（キャッシュ付き）
    public static Vector2i getActualHudPosition(String hudId, int screenWidth, int screenHeight) {
        // Clear cache if screen size changed
        if (screenWidth != lastScreenWidth || screenHeight != lastScreenHeight) {
            POSITION_CACHE.clear();
            lastScreenWidth = screenWidth;
            lastScreenHeight = screenHeight;
        }
        
        // Use cached position if available
        return POSITION_CACHE.computeIfAbsent(hudId, id -> {
            Vector2i offset = getCurrentOffset(id);
            Vector2i basePosition = getDefaultPosition(id, screenWidth, screenHeight);
            return new Vector2i(basePosition.x + offset.x, basePosition.y + offset.y);
        });
    }
    
    private static Vector2i getCurrentOffset(String hudId) {
        switch (hudId) {
            case "health": return HUDConfig.getHealthPosition();
            case "food": return HUDConfig.getFoodPosition();
            case "experience": return HUDConfig.getExperiencePosition();
            case "hotbar": return HUDConfig.getHotbarPosition();
            case "air": return HUDConfig.getAirPosition();
            case "armor": return HUDConfig.getArmorPosition();
            default: return new Vector2i(0, 0);
        }
    }
    
    private static Vector2i getDefaultPosition(String hudId, int screenWidth, int screenHeight) {
        switch (hudId) {
            case "health": return HUDConfig.getDefaultHealthPosition(screenWidth, screenHeight);
            case "food": return HUDConfig.getDefaultFoodPosition(screenWidth, screenHeight);
            case "experience": return HUDConfig.getDefaultExperiencePosition(screenWidth, screenHeight);
            case "hotbar": return HUDConfig.getDefaultHotbarPosition(screenWidth, screenHeight);
            case "air": return HUDConfig.getDefaultAirPosition(screenWidth, screenHeight);
            case "armor": return HUDConfig.getDefaultArmorPosition(screenWidth, screenHeight);
            default: return new Vector2i(0, 0);
        }
    }
    
    /**
     * デバッグ用座標測定メソッド - 実際のVanilla座標と比較用
     */
    public static void debugVanillaPositions(int screenWidth, int screenHeight) {
        if (!DEBUG_POSITIONS || debugLogPrinted) return;
        
        // デバッグ情報をLOGGERで出力
        if (DEBUG_POSITIONS) {
            LOGGER.debug("=== Universal HUD Manager - Vanilla Position Debug ===");
            LOGGER.debug("Screen Size: {}x{}", screenWidth, screenHeight);
            LOGGER.debug("GUI Scale: {}", net.minecraft.client.Minecraft.getInstance().options.guiScale().get());
            
            // 現在の計算値（そらの実装）
            Vector2i health = HUDConfig.getDefaultHealthPosition(screenWidth, screenHeight);
            Vector2i food = HUDConfig.getDefaultFoodPosition(screenWidth, screenHeight);
            Vector2i experience = HUDConfig.getDefaultExperiencePosition(screenWidth, screenHeight);
            Vector2i hotbar = HUDConfig.getDefaultHotbarPosition(screenWidth, screenHeight);
            Vector2i air = HUDConfig.getDefaultAirPosition(screenWidth, screenHeight);
            
            LOGGER.debug("=== Current Implementation (Sora's calculation) ===");
            LOGGER.debug("Health Bar: {}, {}", health.x, health.y);
            LOGGER.debug("Food Bar: {}, {}", food.x, food.y);
            LOGGER.debug("Experience Bar: {}, {}", experience.x, experience.y);
            LOGGER.debug("Hotbar: {}, {}", hotbar.x, hotbar.y);
            LOGGER.debug("Air Bar: {}, {}", air.x, air.y);
            
            // 画面中央・基準点
            LOGGER.debug("=== Reference Points ===");
            LOGGER.debug("Screen Center: {}, {}", screenWidth/2, screenHeight/2);
            LOGGER.debug("Bottom-Left: 0, {}", screenHeight);
            LOGGER.debug("Bottom-Right: {}, {}", screenWidth, screenHeight);
            
            // 推測される正しい座標（左端・右端基準）
            LOGGER.debug("=== Alternative Calculations (Guess) ===");
            Vector2i healthAlt1 = new Vector2i(10, screenHeight - 39);  // 左端から10px
            Vector2i foodAlt1 = new Vector2i(screenWidth - 91, screenHeight - 39);  // 右端から91px
            Vector2i hotbarAlt1 = new Vector2i((screenWidth - 182) / 2, screenHeight - 22);  // ホットバー幅182px
            
            LOGGER.debug("Health (Left-based): {}, {}", healthAlt1.x, healthAlt1.y);
            LOGGER.debug("Food (Right-based): {}, {}", foodAlt1.x, foodAlt1.y);
            LOGGER.debug("Hotbar (Center-182px): {}, {}", hotbarAlt1.x, hotbarAlt1.y);
            
            // Vanilla配置との比較用参考値
            LOGGER.debug("=== Expected Vanilla Positions (from screenshot) ===");
            LOGGER.debug("Health should be: Left-bottom area");
            LOGGER.debug("Food should be: Right-bottom area");
            LOGGER.debug("Hotbar should be: Center-bottom");
            
            LOGGER.debug("========================================================");
            
            debugLogPrinted = true;  // 一度だけ表示
        }
    }
    
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
     * When riding a horse, this is hidden (horse health is shown in food position instead)
     */
    public static final IGuiOverlay HEALTH_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.HEALTH_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderSurvivalElements()) return;
        
        // Player health display logic
        // Note: In Vanilla, player health always shows on left, even when riding
        if (DEBUG_POSITIONS && !debugLogPrinted) {
            boolean ridingMount = isRidingMount(player);
            if (ridingMount) { // Only log when actually riding to reduce spam
                System.out.println("Health Overlay Debug - Player health while riding mount");
            }
        }
        
        // Get position offset and calculate final position (2025-08-04 fix applied)
        Vector2i offset = HUDConfig.getHealthPosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(
            HUDConfig.getDefaultHealthPosition(width, height), offset);
        
        // Use vanilla GUI rendering method for full Resource Pack support
        renderVanillaHealth(gui, graphics, partialTick, width, height);
        // Currently using fallback rendering with direct coordinates
        renderSimpleHealth(graphics, finalPos.x, finalPos.y, player);
    };
    
    /**
     * Food Bar Overlay - Resource Pack compatible  
     * When riding a horse, this displays the horse's health instead of food
     */
    public static final IGuiOverlay FOOD_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.FOOD_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderSurvivalElements()) return;
        
        // 2025-08-05 fix: Always reset Vanilla position manager at FOOD_OVERLAY start
        // FOOD_OVERLAY is typically rendered first, so it handles the reset
        HUDConfig.VanillaPositionManager.resetForFrame();
        
        // Get position offset and calculate final position (2025-08-04 fix applied)
        Vector2i offset = HUDConfig.getFoodPosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(
            HUDConfig.getDefaultFoodPosition(width, height), offset);
        
        // Enhanced mount detection and rendering
        if (isRidingMount(player)) {
            // When riding: Show horse health at food position (Vanilla behavior)
            renderVanillaHorseHealth(gui, graphics, partialTick, width, height);
            renderSimpleHorseHealth(graphics, finalPos.x, finalPos.y, player);
        } else {
            // When not riding: Show normal food bar
            renderVanillaFood(gui, graphics, partialTick, width, height);
            renderSimpleFood(graphics, finalPos.x, finalPos.y, player);
        }
    };
    
    /**
     * Experience Bar Overlay - Resource Pack compatible
     * When riding a horse, this displays the horse's jump bar instead of experience
     */
    public static final IGuiOverlay EXPERIENCE_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.EXPERIENCE_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderSurvivalElements()) return;
        
        Vector2i offset = HUDConfig.getExperiencePosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(
            HUDConfig.getDefaultExperiencePosition(width, height), offset);
        
        // Enhanced experience/jump bar rendering (2025-08-04 fix applied)
        if (isRidingMount(player)) {
            // When riding: Show horse jump bar at experience position
            renderVanillaHorseJump(gui, graphics, partialTick, width, height);
            renderSimpleHorseJump(graphics, finalPos.x, finalPos.y, player);
        } else {
            // When not riding: Show normal experience bar
            renderVanillaExperience(gui, graphics, partialTick, width, height);
            renderSimpleExperience(graphics, finalPos.x, finalPos.y, player);
        }
    };
    
    /**
     * Hotbar Overlay - Resource Pack compatible
     */
    public static final IGuiOverlay HOTBAR_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.HOTBAR_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderHotbar()) return;
        
        Vector2i offset = HUDConfig.getHotbarPosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(
            HUDConfig.getDefaultHotbarPosition(width, height), offset);
        
        renderVanillaHotbar(gui, graphics, partialTick, width, height);
        renderSimpleHotbar(graphics, finalPos.x, finalPos.y, player, partialTick);
    };
    
    /**
     * Air Bar Overlay - Resource Pack compatible
     */
    public static final IGuiOverlay AIR_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.AIR_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderSurvivalElements()) return;
        
        // Check if we should render air bar
        boolean shouldRender = player.isEyeInFluidType(net.minecraftforge.common.ForgeMod.WATER_TYPE.get()) || 
                              player.getAirSupply() < player.getMaxAirSupply() || 
                              HUDConfig.HUD_EDIT_MODE.get();
        
        if (!shouldRender) return;
        
        Vector2i offset = HUDConfig.getAirPosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(
            HUDConfig.getDefaultAirPosition(width, height), offset);
        
        renderVanillaAir(gui, graphics, partialTick, width, height);
        renderSimpleAir(graphics, finalPos.x, finalPos.y, player);
        
        // 2025-08-05 fix: Increment rightHeight after air bar rendering (Vanilla timing)
        if (!HUDConfig.HUD_EDIT_MODE.get() && shouldRender) {
            HUDConfig.VanillaPositionManager.airBarRendered();
        }
        
        // End frame after all overlays processed
        HUDConfig.VanillaPositionManager.endFrame();
    };
    
    /**
     * Armor Bar Overlay - Resource Pack compatible
     */
    public static final IGuiOverlay ARMOR_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (!HUDConfig.ARMOR_ENABLED.get()) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !shouldRenderSurvivalElements()) return;
        
        // Only render when player has armor or in edit mode
        int armorValue = player.getArmorValue();
        if (armorValue <= 0 && !HUDConfig.HUD_EDIT_MODE.get()) return;
        
        Vector2i offset = HUDConfig.getArmorPosition();
        Vector2i finalPos = HUDConfig.getFinalPosition(
            HUDConfig.getDefaultArmorPosition(width, height), offset);
        
        renderVanillaArmor(gui, graphics, partialTick, width, height);
        renderSimpleArmor(graphics, finalPos.x, finalPos.y, player);
    };
    
    /**
     * Register all overlays
     */
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        // Register our custom overlays to replace vanilla ones
        event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "universal_health_rp", HEALTH_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "universal_food_rp", FOOD_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "universal_experience_rp", EXPERIENCE_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "universal_hotbar_rp", HOTBAR_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.AIR_LEVEL.id(), "universal_air_rp", AIR_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.ARMOR_LEVEL.id(), "universal_armor_rp", ARMOR_OVERLAY);
    }
    
    // NOTE: Overlay cancellation is now handled exclusively by VanillaHudController
    
    // Vanilla GUI method calls - Simplified implementation for now
    
    private static void renderVanillaHealth(Gui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        // TODO: Implement actual vanilla GUI method calls via reflection
        // For now, log debug message and use fallback rendering
        LOGGER.debug("renderVanillaHealth not implemented, using fallback");
        // Fallback will be handled by caller
    }
    
    private static void renderVanillaFood(Gui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        LOGGER.debug("renderVanillaFood not implemented, using fallback");
        // Fallback will be handled by caller
    }
    
    private static void renderVanillaExperience(Gui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        LOGGER.debug("renderVanillaExperience not implemented, using fallback");
        // Fallback will be handled by caller
    }
    
    private static void renderVanillaHotbar(Gui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        LOGGER.debug("renderVanillaHotbar not implemented, using fallback");
        // Fallback will be handled by caller
    }
    
    private static void renderVanillaAir(Gui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        LOGGER.debug("renderVanillaAir not implemented, using fallback");
        // Fallback will be handled by caller
    }
    
    private static void renderVanillaArmor(Gui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        LOGGER.debug("renderVanillaArmor not implemented, using fallback");
        // Fallback will be handled by caller
    }
    
    private static void renderVanillaHorseHealth(Gui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        LOGGER.debug("renderVanillaHorseHealth not implemented, using fallback");
        // Fallback will be handled by caller
    }
    
    private static void renderVanillaHorseJump(Gui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        LOGGER.debug("renderVanillaHorseJump not implemented, using fallback");
        // Fallback will be handled by caller
    }
    
    // Fallback simple rendering methods (from original implementation)
    
    private static void renderSimpleHealth(GuiGraphics graphics, int x, int y, LocalPlayer player) {
        // Full health bar implementation - using direct coordinates (2025-08-04 fix)
        // No more position calculation - coordinates are pre-calculated and passed in
        
        net.minecraft.resources.ResourceLocation GUI_ICONS_LOCATION = 
            new net.minecraft.resources.ResourceLocation("textures/gui/icons.png");
        
        // Get player health data
        int health = net.minecraft.util.Mth.ceil(player.getHealth());
        int maxHealth = net.minecraft.util.Mth.ceil(player.getMaxHealth());
        int absorption = net.minecraft.util.Mth.ceil(player.getAbsorptionAmount());
        
        // Check for status effects that change heart appearance
        boolean hasPoison = player.hasEffect(net.minecraft.world.effect.MobEffects.POISON);
        boolean hasWither = player.hasEffect(net.minecraft.world.effect.MobEffects.WITHER);
        
        // Calculate rows for hearts
        int rows = net.minecraft.util.Mth.ceil((maxHealth + absorption) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (rows - 2), 3);
        
        // Render all hearts
        for (int i = 0; i < net.minecraft.util.Mth.ceil((maxHealth + absorption) / 2.0F); ++i) {
            int row = i / 10;
            int col = i % 10;
            int heartX = x + col * 8;
            int heartY = y - row * rowHeight;
            
            // Background heart
            graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 16, 0, 9, 9);
            
            // Heart type and fill with status effect colors
            if (i < maxHealth / 2) {
                // Normal hearts with status effect coloring
                if (i * 2 + 1 < health) {
                    // Full heart - choose sprite based on status effects
                    if (hasWither) {
                        graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 52, 36, 9, 9); // Withered full heart
                    } else if (hasPoison) {
                        graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 88, 0, 9, 9); // Poisoned full heart
                    } else {
                        graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 52, 0, 9, 9); // Normal full heart
                    }
                } else if (i * 2 + 1 == health) {
                    // Half heart - choose sprite based on status effects
                    if (hasWither) {
                        graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 61, 36, 9, 9); // Withered half heart
                    } else if (hasPoison) {
                        graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 97, 0, 9, 9); // Poisoned half heart
                    } else {
                        graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 61, 0, 9, 9); // Normal half heart
                    }
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
    
    private static void renderSimpleFood(GuiGraphics graphics, int x, int y, LocalPlayer player) {
        // Food bar implementation - using direct coordinates (2025-08-04 fix)
        // 2025-08-05 fix: Vanilla-accurate coordinate calculation with proper offset support
        
        net.minecraft.resources.ResourceLocation GUI_ICONS_LOCATION = 
            new net.minecraft.resources.ResourceLocation("textures/gui/icons.png");
        
        // Get player food data
        net.minecraft.world.food.FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();
        
        // Render food icons with Vanilla-accurate coordinate calculation
        // Use provided x,y as base position (includes HUDConfig offset)
        for (int i = 0; i < 10; ++i) {
            // Vanilla-accurate relative calculation: base position + right-to-left offset
            // In vanilla: starts from right edge and goes left with 8px spacing
            int foodX = x - i * 8;  // Right-to-left positioning from base x
            int foodY = y;
            
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
    
    private static void renderSimpleExperience(GuiGraphics graphics, int x, int y, LocalPlayer player) {
        // Experience bar implementation - using direct coordinates (2025-08-04 fix)
        
        // Only render if player has experience levels
        if (player.experienceLevel > 0) {
            // Use icons.png with correct coordinates for Forge 1.20.1
            renderVanillaAccurateExperienceBar(graphics, x, y, player);
        }
    }
    
    /**
     * Vanilla-Accurate Experience Bar rendering for Forge 1.20.1
     * Uses icons.png with correct coordinates and proper text rendering
     */
    private static void renderVanillaAccurateExperienceBar(GuiGraphics graphics, int x, int y, LocalPlayer player) {
        // Use icons.png at correct coordinates for Forge 1.20.1
        // Background bar at (0, 64) - moved 1px left
        graphics.blit(GUI_ICONS_LOCATION, x - 1, y, 0, 64, 182, 5);
        
        // Progress bar at (0, 69) with width proportional to experience progress - moved 1px left
        if (player.experienceProgress > 0) {
            int fillWidth = (int) (player.experienceProgress * 183.0F); // +1 to round up like vanilla
            graphics.blit(GUI_ICONS_LOCATION, x - 1, y, 0, 69, fillWidth, 5);
        }
        
        // Experience level text - Vanilla-accurate positioning with drop shadow
        String levelText = String.valueOf(player.experienceLevel);
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        int centerX = x + 182 / 2 - 1; // Center of the experience bar, adjusted 1px left
        int textY = y - 6; // Perfect distance from bar like vanilla (was -5, now -6)
        
        // Use drawCenteredString for vanilla-accurate appearance
        // Note: Forge 1.20.1 drawCenteredString automatically includes drop shadow
        graphics.drawCenteredString(mc.font, levelText, centerX, textY, 0x80FF20);
    }
    
    private static void renderSimpleHotbar(GuiGraphics graphics, int x, int y, LocalPlayer player, float partialTick) {
        // Hotbar implementation - using direct coordinates (2025-08-04 fix)
        
        // Render hotbar background using WIDGETS_LOCATION
        graphics.blit(WIDGETS_LOCATION, x, y, 0, 0, 182, 22);
        
        // Render hotbar selection highlight
        int selectedSlot = player.getInventory().selected;
        graphics.blit(WIDGETS_LOCATION, x - 1 + selectedSlot * 20, y - 1, 0, 22, 24, 24);
        
        // Render items in hotbar slots
        for (int i = 0; i < 9; ++i) {
            int slotX = x + 3 + i * 20;
            int slotY = y + 3;
            
            net.minecraft.world.item.ItemStack itemstack = player.getInventory().items.get(i);
            if (!itemstack.isEmpty()) {
                graphics.renderItem(itemstack, slotX, slotY);
                graphics.renderItemDecorations(net.minecraft.client.Minecraft.getInstance().font, itemstack, slotX, slotY);
            }
        }
    }
    
    private static void renderSimpleAir(GuiGraphics graphics, int x, int y, LocalPlayer player) {
        // Air bar implementation - using direct coordinates (2025-08-04 fix)
        // 2025-08-05 fix: Unified with food/horse health right-side pattern
        
        net.minecraft.resources.ResourceLocation GUI_ICONS_LOCATION = 
            new net.minecraft.resources.ResourceLocation("textures/gui/icons.png");
        
        // Get air data
        int air = player.getAirSupply();  // Current air (0-300)
        
        // ✅ Vanilla準拠: 水中または空気不足時のみ表示
        boolean shouldRender = player.isEyeInFluidType(net.minecraftforge.common.ForgeMod.WATER_TYPE.get()) || air < 300;
        if (!shouldRender && !HUDConfig.HUD_EDIT_MODE.get()) return;
        
        // ✅ Vanilla準拠の正確な計算式（maxAir=300固定）
        int full = net.minecraft.util.Mth.ceil((double)(air - 2) * 10.0D / 300.0D);  // 満タン泡
        int partial = net.minecraft.util.Mth.ceil((double)air * 10.0D / 300.0D) - full;  // 部分泡
        int totalBubbles = full + partial;
        
        // Render air bubbles with unified right-side pattern (same as food/horse health)
        // Use provided x,y as base position (includes HUDConfig offset)
        for (int i = 0; i < totalBubbles; ++i) {
            // ✅ 統一パターン: 右から左配置（フード・馬ヘルスと同じ）
            int bubbleX = x - i * 8;  // Right-to-left positioning from base x
            int bubbleY = y;
            
            if (i < full) {
                // 満タン泡（テクスチャ座標16,18）
                graphics.blit(GUI_ICONS_LOCATION, bubbleX, bubbleY, 16, 18, 9, 9); // Full bubble
            } else {
                // 部分泡（テクスチャ座標25,18）
                graphics.blit(GUI_ICONS_LOCATION, bubbleX, bubbleY, 25, 18, 9, 9); // Popped bubble
            }
        }
    }
    
    private static void renderSimpleArmor(GuiGraphics graphics, int x, int y, LocalPlayer player) {
        // Armor bar implementation - using direct coordinates (2025-08-04 fix)
        
        net.minecraft.resources.ResourceLocation GUI_ICONS_LOCATION = 
            new net.minecraft.resources.ResourceLocation("textures/gui/icons.png");
        
        // Get armor data
        int armorValue = player.getArmorValue();
        
        // Render armor icons (left to right like vanilla armor positioning)
        for (int i = 0; i < 10; ++i) {
            int col = i; // Left to right like vanilla armor
            int armorX = x + col * 8; // Left alignment like vanilla armor
            int armorY = y;
            
            // Calculate how much armor this icon should show
            int armorForThisIcon = armorValue - i * 2;
            
            if (armorForThisIcon > 0) {
                if (armorForThisIcon >= 2) {
                    // Full armor icon
                    graphics.blit(GUI_ICONS_LOCATION, armorX, armorY, 34, 9, 9, 9);
                } else {
                    // Half armor icon (armorForThisIcon == 1)
                    graphics.blit(GUI_ICONS_LOCATION, armorX, armorY, 25, 9, 9, 9);
                }
            } else if (HUDConfig.ARMOR_VANILLA_STYLE.get() && armorValue > 0) {
                // Vanilla-style: Show empty armor backgrounds when there's some armor
                graphics.blit(GUI_ICONS_LOCATION, armorX, armorY, 16, 9, 9, 9);
            } else if (HUDConfig.HUD_EDIT_MODE.get()) {
                // Edit mode: Always show backgrounds for positioning
                graphics.blit(GUI_ICONS_LOCATION, armorX, armorY, 16, 9, 9, 9);
            }
        }
    }
    
    private static void renderSimpleHorseHealth(GuiGraphics graphics, int x, int y, LocalPlayer player) {
        // Horse health implementation - Vanilla-accurate position and layout
        // 2025-08-05 fix: Correct right-side positioning with proper vanilla coordinates
        
        net.minecraft.resources.ResourceLocation GUI_ICONS_LOCATION = 
            new net.minecraft.resources.ResourceLocation("textures/gui/icons.png");
        
        // Get mount entity
        net.minecraft.world.entity.Entity ridingEntity = player.getVehicle();
        if (!(ridingEntity instanceof net.minecraft.world.entity.LivingEntity)) return;
        
        net.minecraft.world.entity.LivingEntity mount = (net.minecraft.world.entity.LivingEntity) ridingEntity;
        
        // Get horse health data
        int horseHealth = net.minecraft.util.Mth.ceil(mount.getHealth());
        int maxHorseHealth = net.minecraft.util.Mth.ceil(mount.getMaxHealth());
        int totalHearts = net.minecraft.util.Mth.ceil(maxHorseHealth / 2.0F);
        
        // Limit to 30 hearts max like vanilla (3 rows of 10)
        totalHearts = Math.min(totalHearts, 30);
        
        // Calculate screen dimensions for proper positioning
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        
        // ✅ Vanilla準拠の位置計算（右側配置）
        // Use the provided x,y as base, but adjust for right-side alignment
        int baseX = x; // This already includes HUDConfig offset
        
        // Render horse hearts (right to left like food bar)
        for (int i = 0; i < totalHearts; ++i) {
            int row = i / 10; // Row index (0 = bottom row, 1 = middle row, 2 = top row)
            int col = i % 10; // Column within the row
            
            // Calculate positions with vanilla coordinates
            // Right-aligned positioning (like food bar)
            int heartX = baseX - col * 8;  // Right to left with 8px spacing
            int heartY = y - row * 10;      // Stack rows upward with 10px spacing
            
            // Background heart (empty horse heart) - correct texture coordinates
            graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 52, 9, 9, 9);
            
            // Horse heart fill based on current health
            if (i * 2 + 1 < horseHealth) {
                // Full horse heart - correct texture coordinates
                graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 88, 9, 9, 9);
            } else if (i * 2 + 1 == horseHealth) {
                // Half horse heart - correct texture coordinates
                graphics.blit(GUI_ICONS_LOCATION, heartX, heartY, 97, 9, 9, 9);
            }
        }
    }
    
    private static void renderSimpleHorseJump(GuiGraphics graphics, int x, int y, LocalPlayer player) {
        // Horse jump bar implementation - using direct coordinates (2025-08-04 fix)
        // 2025-08-05 major fix: Correct LocalPlayer jumpRidingScale access with space key detection
        
        // Get mount entity
        net.minecraft.world.entity.Entity ridingEntity = player.getVehicle();
        if (!(ridingEntity instanceof net.minecraftforge.common.extensions.IForgeEntity)) return;
        if (!(ridingEntity instanceof net.minecraft.world.entity.PlayerRideableJumping)) return;
        
        Minecraft minecraft = Minecraft.getInstance();
        
        // ✅ 正しいアプローチ: LocalPlayerから充電値とキー状態を取得
        float jumpStrength = 0.0f;
        boolean jumpKeyPressed = false;
        
        try {
            // スペースキー押下状態を確認
            jumpKeyPressed = minecraft.options.keyJump.isDown();
            
            // LocalPlayerの充電値を取得（リアルタイム）
            String[] possibleFields = {
                "jumpRidingScale",  // MCP名
                "f_108608_"        // SRG名
            };
            
            for (String fieldName : possibleFields) {
                try {
                    java.lang.reflect.Field jumpField = LocalPlayer.class.getDeclaredField(fieldName);
                    jumpField.setAccessible(true);
                    jumpStrength = jumpField.getFloat(player);
                    break;
                } catch (NoSuchFieldException e) {
                    continue;
                }
            }
            
        } catch (Exception e) {
            // フォールバック: スペースキー押下時のみ固定値
            jumpKeyPressed = minecraft.options.keyJump.isDown();
            jumpStrength = jumpKeyPressed ? 0.5f : 0.0f;
        }
        
        // デバッグ情報
        if (DEBUG_POSITIONS && !debugLogPrinted) {
            System.out.println("Jump Key Pressed: " + jumpKeyPressed);
            System.out.println("Jump Strength: " + jumpStrength);
            System.out.println("Vehicle: " + (ridingEntity != null ? 
                ridingEntity.getClass().getSimpleName() : "null"));
        }
        
        // ✅ Vanilla準拠: 馬に乗っている間は常に空のゲージを表示
        // Background bar (always shown when riding) - moved 1px left
        graphics.blit(GUI_ICONS_LOCATION, x - 1, y, 0, 84, 182, 5);
        
        // ✅ スペースキー押下中のみプログレスバーを表示 - moved 1px left
        if (jumpKeyPressed && jumpStrength > 0.0F) {
            int fillWidth = (int) (jumpStrength * 183.0F);
            if (fillWidth > 0) {
                graphics.blit(GUI_ICONS_LOCATION, x - 1, y, 0, 89, fillWidth, 5);
            }
        }
    }
    
    /**
     * Check if player is riding a mountable entity (horse, donkey, mule, etc.)
     */
    private static boolean isRidingMount(LocalPlayer player) {
        net.minecraft.world.entity.Entity ridingEntity = player.getVehicle();
        return ridingEntity instanceof net.minecraft.world.entity.animal.horse.AbstractHorse;
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