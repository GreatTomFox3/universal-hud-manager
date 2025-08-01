package com.greattomfoxsora.universalhudmanager.core;

/**
 * Represents a HUD element that can be positioned and managed
 * 
 * @author GreatTomFox & Sora
 */
public class HUDElement {
    private final String id;
    private final String displayName;
    private final String modId;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean enabled;
    private boolean draggable;
    
    public HUDElement(String id, String displayName, String modId) {
        this.id = id;
        this.displayName = displayName;
        this.modId = modId;
        this.x = 0;
        this.y = 0;
        this.width = 100;
        this.height = 20;
        this.enabled = true;
        this.draggable = true;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getModId() { return modId; }
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public boolean isDraggable() { return draggable; }
    public void setDraggable(boolean draggable) { this.draggable = draggable; }
    
    /**
     * Check if a point is within this HUD element's bounds
     */
    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    @Override
    public String toString() {
        return String.format("HUDElement{id='%s', mod='%s', pos=(%d,%d), size=(%dx%d)}", 
                           id, modId, x, y, width, height);
    }
}