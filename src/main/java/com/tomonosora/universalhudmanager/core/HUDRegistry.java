package com.tomonosora.universalhudmanager.core;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all HUD elements discovered and managed by Universal HUD Manager
 * 
 * @author Tomo & Sora
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
        // Health bar
        registerHUD(new HUDElement("minecraft:health", "Health Bar", "minecraft"));
        
        // Food bar
        registerHUD(new HUDElement("minecraft:food", "Food Bar", "minecraft"));
        
        // Experience bar
        registerHUD(new HUDElement("minecraft:experience", "Experience Bar", "minecraft"));
        
        // Hotbar
        registerHUD(new HUDElement("minecraft:hotbar", "Hotbar", "minecraft"));
        
        // Crosshair
        registerHUD(new HUDElement("minecraft:crosshair", "Crosshair", "minecraft"));
        
        // Chat
        registerHUD(new HUDElement("minecraft:chat", "Chat", "minecraft"));
        
        // Debug info (F3)
        registerHUD(new HUDElement("minecraft:debug", "Debug Info", "minecraft"));
        
        LOGGER.info("Discovered {} vanilla HUD elements", 7);
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