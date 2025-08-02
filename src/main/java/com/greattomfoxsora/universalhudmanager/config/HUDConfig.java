package com.greattomfoxsora.universalhudmanager.config;

import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.joml.Vector2i;

import java.util.Arrays;
import java.util.List;

/**
 * HUD Configuration System for Universal HUD Manager
 * Based on Cold-Sweat's Vector2i position control system
 */
public class HUDConfig {
    
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    
    // Position configurations for vanilla HUD elements
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> HEALTH_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> FOOD_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> EXPERIENCE_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> HOTBAR_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> AIR_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> CHAT_POS;
    
    // Enable/disable configurations
    public static final ForgeConfigSpec.BooleanValue HEALTH_ENABLED;
    public static final ForgeConfigSpec.BooleanValue FOOD_ENABLED;
    public static final ForgeConfigSpec.BooleanValue EXPERIENCE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue HOTBAR_ENABLED;
    public static final ForgeConfigSpec.BooleanValue AIR_ENABLED;
    public static final ForgeConfigSpec.BooleanValue CHAT_ENABLED;
    
    // Global settings
    public static final ForgeConfigSpec.BooleanValue HUD_EDIT_MODE;
    public static final ForgeConfigSpec.BooleanValue SHOW_PLACEHOLDERS;
    
    static {
        BUILDER.push("hud_positions");
        
        // Position settings (x, y offsets from default positions)
        HEALTH_POS = BUILDER
                .comment("Health bar position offset [x, y]")
                .defineList("health_position", Arrays.asList(0, 0), 
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        FOOD_POS = BUILDER
                .comment("Food bar position offset [x, y]")
                .defineList("food_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        EXPERIENCE_POS = BUILDER
                .comment("Experience bar position offset [x, y]")
                .defineList("experience_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        HOTBAR_POS = BUILDER
                .comment("Hotbar position offset [x, y]")
                .defineList("hotbar_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        AIR_POS = BUILDER
                .comment("Air/Oxygen bar position offset [x, y]")
                .defineList("air_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        CHAT_POS = BUILDER
                .comment("Chat position offset [x, y]")
                .defineList("chat_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        BUILDER.pop();
        
        BUILDER.push("hud_enabled");
        
        // Enable/disable settings
        HEALTH_ENABLED = BUILDER
                .comment("Enable custom health bar positioning")
                .define("health_enabled", true);
        
        FOOD_ENABLED = BUILDER
                .comment("Enable custom food bar positioning")
                .define("food_enabled", true);
        
        EXPERIENCE_ENABLED = BUILDER
                .comment("Enable custom experience bar positioning")
                .define("experience_enabled", true);
        
        HOTBAR_ENABLED = BUILDER
                .comment("Enable custom hotbar positioning")
                .define("hotbar_enabled", true);
        
        AIR_ENABLED = BUILDER
                .comment("Enable custom air bar positioning")
                .define("air_enabled", true);
        
        CHAT_ENABLED = BUILDER
                .comment("Enable custom chat positioning")
                .define("chat_enabled", true);
        
        BUILDER.pop();
        
        BUILDER.push("general");
        
        // General settings
        HUD_EDIT_MODE = BUILDER
                .comment("Enable HUD edit mode (shows draggable elements)")
                .define("hud_edit_mode", false);
        
        SHOW_PLACEHOLDERS = BUILDER
                .comment("Show placeholder boxes in edit mode")
                .define("show_placeholders", true);
        
        BUILDER.pop();
        
        SPEC = BUILDER.build();
    }
    
    // Utility methods to convert between List<Integer> and Vector2i
    
    /**
     * Get Vector2i position from config value
     */
    public static Vector2i getPosition(ForgeConfigSpec.ConfigValue<List<? extends Integer>> configValue) {
        List<? extends Integer> list = configValue.get();
        if (list.size() >= 2) {
            return new Vector2i(list.get(0), list.get(1));
        }
        return new Vector2i(0, 0);
    }
    
    /**
     * Set Vector2i position to config value
     */
    public static void setPosition(ForgeConfigSpec.ConfigValue<List<? extends Integer>> configValue, Vector2i position) {
        configValue.set(Arrays.asList(position.x, position.y));
    }
    
    // Convenience methods for specific HUD elements
    
    public static Vector2i getHealthPosition() {
        return getPosition(HEALTH_POS);
    }
    
    public static void setHealthPosition(Vector2i position) {
        setPosition(HEALTH_POS, position);
    }
    
    public static Vector2i getFoodPosition() {
        return getPosition(FOOD_POS);
    }
    
    public static void setFoodPosition(Vector2i position) {
        setPosition(FOOD_POS, position);
    }
    
    public static Vector2i getExperiencePosition() {
        return getPosition(EXPERIENCE_POS);
    }
    
    public static void setExperiencePosition(Vector2i position) {
        setPosition(EXPERIENCE_POS, position);
    }
    
    public static Vector2i getHotbarPosition() {
        return getPosition(HOTBAR_POS);
    }
    
    public static void setHotbarPosition(Vector2i position) {
        setPosition(HOTBAR_POS, position);
    }
    
    public static Vector2i getAirPosition() {
        return getPosition(AIR_POS);
    }
    
    public static void setAirPosition(Vector2i position) {
        setPosition(AIR_POS, position);
    }
    
    public static Vector2i getChatPosition() {
        return getPosition(CHAT_POS);
    }
    
    public static void setChatPosition(Vector2i position) {
        setPosition(CHAT_POS, position);
    }
    
    // Default position calculations (based on vanilla Minecraft)
    
    /**
     * Get default health bar position (bottom-left relative)
     */
    public static Vector2i getDefaultHealthPosition(int screenWidth, int screenHeight) {
        return new Vector2i(screenWidth / 2 - 91, screenHeight - 39);
    }
    
    /**
     * Get default food bar position (bottom-right relative)  
     */
    public static Vector2i getDefaultFoodPosition(int screenWidth, int screenHeight) {
        return new Vector2i(screenWidth / 2 + 91, screenHeight - 39);
    }
    
    /**
     * Get default experience bar position (bottom-center relative)
     */
    public static Vector2i getDefaultExperiencePosition(int screenWidth, int screenHeight) {
        return new Vector2i(screenWidth / 2 - 91, screenHeight - 32);
    }
    
    /**
     * Get default hotbar position (bottom-center relative)
     */
    public static Vector2i getDefaultHotbarPosition(int screenWidth, int screenHeight) {
        return new Vector2i(screenWidth / 2 - 91, screenHeight - 22);
    }
    
    /**
     * Get default air bar position (above health bar)
     */
    public static Vector2i getDefaultAirPosition(int screenWidth, int screenHeight) {
        return new Vector2i(screenWidth / 2 + 91, screenHeight - 59);
    }
    
    /**
     * Get default chat position (bottom-left)
     */
    public static Vector2i getDefaultChatPosition(int screenWidth, int screenHeight) {
        return new Vector2i(4, screenHeight - 40);
    }
    
    /**
     * Calculate final position with offset applied
     */
    public static Vector2i getFinalPosition(Vector2i defaultPos, Vector2i offset) {
        return new Vector2i(defaultPos.x + offset.x, defaultPos.y + offset.y);
    }
    
    /**
     * Register config specification
     */
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC, "universalhudmanager-client.toml");
    }
}