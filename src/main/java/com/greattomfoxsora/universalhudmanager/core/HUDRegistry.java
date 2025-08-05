package com.greattomfoxsora.universalhudmanager.core;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all HUD elements discovered and managed by Universal HUD Manager
 * 
 * @author GreatTomFox & Sora
 */
public class HUDRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, HUDElement> registeredHUDs = new ConcurrentHashMap<>();
    
    /**
     * Register a new HUD element
     */
    public static void registerHUD(HUDElement element) {
        registeredHUDs.put(element.getId(), element);
        LOGGER.info("Registered HUD element: {} from mod: {}", 
                   element.getDisplayName(), element.getModId());
    }
    
    /**
     * Get a HUD element by ID
     */
    public static HUDElement getHUD(String id) {
        return registeredHUDs.get(id);
    }
    
    /**
     * Get all registered HUD elements
     */
    public static Collection<HUDElement> getAllHUDs() {
        return Collections.unmodifiableCollection(registeredHUDs.values());
    }
    
    /**
     * Get HUD elements from a specific mod
     */
    public static List<HUDElement> getHUDsFromMod(String modId) {
        return registeredHUDs.values().stream()
                .filter(hud -> hud.getModId().equals(modId))
                .toList();
    }
    
    /**
     * Auto-discover vanilla Minecraft HUD elements
     */
    public static void discoverVanillaHUDs() {
        // 画面サイズを取得
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        // Health bar
        HUDElement health = new HUDElement("health", "Health Bar", "minecraft");
        org.joml.Vector2i healthPos = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getDefaultHealthPosition(screenWidth, screenHeight);
        org.joml.Vector2i healthOffset = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getHealthPosition();
        org.joml.Vector2i healthFinal = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getFinalPosition(healthPos, healthOffset);
        health.setX(healthFinal.x);
        health.setY(healthFinal.y);
        health.setWidth(81);
        health.setHeight(9);
        registerHUD(health);
        
        // Food bar
        HUDElement food = new HUDElement("food", "Food Bar", "minecraft");
        org.joml.Vector2i foodPos = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getDefaultFoodPosition(screenWidth, screenHeight);
        org.joml.Vector2i foodOffset = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getFoodPosition();
        org.joml.Vector2i foodFinal = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getFinalPosition(foodPos, foodOffset);
        food.setX(foodFinal.x);
        food.setY(foodFinal.y);
        food.setWidth(81);
        food.setHeight(9);
        registerHUD(food);
        
        // Experience bar
        HUDElement experience = new HUDElement("experience", "Experience Bar", "minecraft");
        org.joml.Vector2i expPos = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getDefaultExperiencePosition(screenWidth, screenHeight);
        org.joml.Vector2i expOffset = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getExperiencePosition();
        org.joml.Vector2i expFinal = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getFinalPosition(expPos, expOffset);
        experience.setX(expFinal.x);
        experience.setY(expFinal.y);
        experience.setWidth(182);
        experience.setHeight(5);
        registerHUD(experience);
        
        // Hotbar
        HUDElement hotbar = new HUDElement("hotbar", "Hotbar", "minecraft");
        org.joml.Vector2i hotbarPos = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getDefaultHotbarPosition(screenWidth, screenHeight);
        org.joml.Vector2i hotbarOffset = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getHotbarPosition();
        org.joml.Vector2i hotbarFinal = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getFinalPosition(hotbarPos, hotbarOffset);
        hotbar.setX(hotbarFinal.x);
        hotbar.setY(hotbarFinal.y);
        hotbar.setWidth(182);
        hotbar.setHeight(22);
        registerHUD(hotbar);
        
        // Air bar (only add if player needs it)
        HUDElement air = new HUDElement("air", "Air Bar", "minecraft");
        org.joml.Vector2i airPos = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getDefaultAirPosition(screenWidth, screenHeight);
        org.joml.Vector2i airOffset = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getAirPosition();
        org.joml.Vector2i airFinal = com.greattomfoxsora.universalhudmanager.config.HUDConfig.getFinalPosition(airPos, airOffset);
        air.setX(airFinal.x);
        air.setY(airFinal.y);
        air.setWidth(81);
        air.setHeight(9);
        registerHUD(air);
        
        LOGGER.info("Discovered {} vanilla HUD elements with proper positions", 5);
        LOGGER.info("Screen size: {}x{}", screenWidth, screenHeight);
    }
    
    /**
     * Auto-discover HUD elements from loaded mods
     */
    public static void discoverModHUDs() {
        // TerraFirmaCraft HUDs
        if (isModLoaded("tfc")) {
            registerHUD(new HUDElement("tfc:thirst", "Thirst Bar", "tfc"));
            registerHUD(new HUDElement("tfc:temperature", "Temperature", "tfc"));
            LOGGER.info("Discovered TerraFirmaCraft HUD elements");
        }
        
        // JEI HUDs
        if (isModLoaded("jei")) {
            registerHUD(new HUDElement("jei:item_list", "Item List", "jei"));
            registerHUD(new HUDElement("jei:recipe_gui", "Recipe GUI", "jei"));
            LOGGER.info("Discovered JEI HUD elements");
        }
        
        // Jade/WAILA HUDs
        if (isModLoaded("jade")) {
            registerHUD(new HUDElement("jade:tooltip", "Block Info Tooltip", "jade"));
            LOGGER.info("Discovered Jade HUD elements");
        }
    }
    
    /**
     * Check if a mod is loaded
     */
    private static boolean isModLoaded(String modId) {
        try {
            return net.minecraftforge.fml.ModList.get().isLoaded(modId);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Clear all registered HUDs (for reload)
     */
    public static void clear() {
        registeredHUDs.clear();
        LOGGER.info("Cleared all registered HUD elements");
    }
    
    /**
     * Get registry statistics
     */
    public static String getStats() {
        Map<String, Long> modCounts = registeredHUDs.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        HUDElement::getModId, 
                        java.util.stream.Collectors.counting()));
        
        return String.format("Total HUDs: %d, Mods: %s", 
                           registeredHUDs.size(), modCounts.toString());
    }
}