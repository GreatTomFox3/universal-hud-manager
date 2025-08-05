package com.greattomfoxsora.universalhudmanager.client;

import com.mojang.logging.LogUtils;
import com.greattomfoxsora.universalhudmanager.core.HUDElement;
import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;
import org.slf4j.Logger;

/**
 * ドラッグ可能なHUD要素ウィジェット
 * FirstAid modのGuiHoldButtonを参考に実装
 * 
 * @author GreatTomFox & Sora
 */
public class DraggableHudElement extends AbstractWidget {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private final HUDElement hudElement;
    private boolean isDragging = false;
    private double dragStartX, dragStartY;
    private double elementStartX, elementStartY;
    private HudEditScreen parentScreen;
    
    // 表示用の色定義
    private static final int BORDER_COLOR_NORMAL = 0xFF00FF00;    // 緑色の枠
    private static final int BORDER_COLOR_HOVER = 0xFF00FFFF;     // シアンの枠（ホバー時）
    private static final int BORDER_COLOR_DRAG = 0xFFFFFF00;      // 黄色の枠（ドラッグ時）
    private static final int BACKGROUND_COLOR = 0x80000000;       // 半透明の背景
    
    public DraggableHudElement(HUDElement hudElement, int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal(hudElement.getDisplayName()));
        this.hudElement = hudElement;
    }
    
    /**
     * 親スクリーンを設定（変更通知用）
     */
    public void setParentScreen(HudEditScreen parentScreen) {
        this.parentScreen = parentScreen;
    }
    
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 枠の色を状態に応じて決定
        int borderColor = isDragging ? BORDER_COLOR_DRAG : 
                         (isHovered ? BORDER_COLOR_HOVER : BORDER_COLOR_NORMAL);
        
        // 背景を描画
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, BACKGROUND_COLOR);
        
        // 枠を描画
        drawBorder(guiGraphics, getX(), getY(), width, height, borderColor);
        
        // HUD要素名を表示
        int textX = getX() + 4;
        int textY = getY() + 4;
        String displayText = hudElement.getDisplayName() + " [" + hudElement.getModId() + "]";
        
        guiGraphics.drawString(
            net.minecraft.client.Minecraft.getInstance().font,
            displayText,
            textX, textY,
            0xFFFFFFFF
        );
        
        // ドラッグ状態を表示
        if (isDragging) {
            guiGraphics.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                "Dragging...",
                textX, textY + 12,
                0xFFFFFF00
            );
        }
    }
    
    private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        // 上辺
        guiGraphics.fill(x, y, x + width, y + 1, color);
        // 下辺
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color);
        // 左辺
        guiGraphics.fill(x, y, x + 1, y + height, color);
        // 右辺
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 詳細なマウス位置とウィジェット領域の情報
        boolean mouseOver = isMouseOver(mouseX, mouseY);
        LOGGER.info("Mouse clicked on {}: mouse=({},{}) button={} isMouseOver={} widget=({},{},{},{}) bounds=({} to {}, {} to {})", 
                   hudElement.getDisplayName(), mouseX, mouseY, button, mouseOver,
                   getX(), getY(), width, height,
                   getX(), getX() + width, getY(), getY() + height);
        
        if (button == 0 && mouseOver) {
            isDragging = true;
            dragStartX = mouseX;
            dragStartY = mouseY;
            elementStartX = this.getX();
            elementStartY = this.getY();
            
            LOGGER.info("Started dragging {}: dragStart=({},{}) elementStart=({},{})", 
                       hudElement.getDisplayName(), dragStartX, dragStartY, elementStartX, elementStartY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        LOGGER.info("Mouse dragged on {}: mouse=({}, {}) button={} isDragging={} dragStart=({},{})", 
                   hudElement.getDisplayName(), mouseX, mouseY, button, isDragging, dragStartX, dragStartY);
        
        if (isDragging && button == 0) {
            // 新しい位置を計算（elementStartX/Yを基準に）
            double deltaMouseX = mouseX - dragStartX;
            double deltaMouseY = mouseY - dragStartY;
            double newX = elementStartX + deltaMouseX;
            double newY = elementStartY + deltaMouseY;
            
            LOGGER.info("Dragging {} calculation: elementStart=({},{}) mouseDelta=({},{}) newPos=({},{})", 
                       hudElement.getDisplayName(), elementStartX, elementStartY, 
                       deltaMouseX, deltaMouseY, newX, newY);
            
            // 画面境界内に制限
            int screenWidth = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenHeight = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight();
            newX = Math.max(0, Math.min(newX, screenWidth - width));
            newY = Math.max(0, Math.min(newY, screenHeight - height));
            
            LOGGER.info("Dragging {} final: bounded to ({},{}) screenSize={}x{} widgetSize={}x{}", 
                       hudElement.getDisplayName(), (int)newX, (int)newY, screenWidth, screenHeight, width, height);
            
            // ウィジェットの位置を更新（HUD要素は更新しない）
            this.setX((int)newX);
            this.setY((int)newY);
            
            // 親スクリーンに変更を通知
            if (parentScreen != null) {
                parentScreen.markChanged();
            }
            
            return true;
        } else {
            LOGGER.info("Mouse dragged on {}: NOT DRAGGING (isDragging={} button={})", 
                       hudElement.getDisplayName(), isDragging, button);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isDragging) {
            isDragging = false;
            
            LOGGER.info("Finished dragging HUD element: {} to ({}, {})", 
                       hudElement.getDisplayName(), getX(), getY());
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, getMessage());
    }
    
    /**
     * HUD要素の新しい位置を保存
     */
    public void savePosition() {
        int newX = getX();
        int newY = getY();
        
        // HUD要素の位置を更新
        hudElement.setX(newX);
        hudElement.setY(newY);
        
        // HUDConfigにも永続保存
        saveToConfig();
        
        LOGGER.info("Saved position for HUD element: {} at ({}, {}) to both runtime and config", 
                   hudElement.getDisplayName(), newX, newY);
    }
    
    /**
     * 位置をHUDConfigに保存
     */
    private void saveToConfig() {
        // 現在の位置からデフォルト位置を引いたオフセットを計算
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        String hudId = hudElement.getId();
        Vector2i currentPos = new Vector2i(getX(), getY());
        Vector2i defaultPos;
        
        // Vanilla HUD要素の場合、対応するconfig設定に保存
        switch (hudId) {
            case "health":
                defaultPos = HUDConfig.getDefaultHealthPosition(screenWidth, screenHeight);
                Vector2i healthOffset = new Vector2i(currentPos.x - defaultPos.x, currentPos.y - defaultPos.y);
                HUDConfig.setHealthPosition(healthOffset);
                break;
            case "food":
                defaultPos = HUDConfig.getDefaultFoodPosition(screenWidth, screenHeight);
                Vector2i foodOffset = new Vector2i(currentPos.x - defaultPos.x, currentPos.y - defaultPos.y);
                HUDConfig.setFoodPosition(foodOffset);
                break;
            case "experience":
                defaultPos = HUDConfig.getDefaultExperiencePosition(screenWidth, screenHeight);
                Vector2i expOffset = new Vector2i(currentPos.x - defaultPos.x, currentPos.y - defaultPos.y);
                HUDConfig.setExperiencePosition(expOffset);
                break;
            case "hotbar":
                defaultPos = HUDConfig.getDefaultHotbarPosition(screenWidth, screenHeight);
                Vector2i hotbarOffset = new Vector2i(currentPos.x - defaultPos.x, currentPos.y - defaultPos.y);
                HUDConfig.setHotbarPosition(hotbarOffset);
                break;
            case "air":
                defaultPos = HUDConfig.getDefaultAirPosition(screenWidth, screenHeight);
                Vector2i airOffset = new Vector2i(currentPos.x - defaultPos.x, currentPos.y - defaultPos.y);
                HUDConfig.setAirPosition(airOffset);
                break;
            case "chat":
                defaultPos = HUDConfig.getDefaultChatPosition(screenWidth, screenHeight);
                Vector2i chatOffset = new Vector2i(currentPos.x - defaultPos.x, currentPos.y - defaultPos.y);
                HUDConfig.setChatPosition(chatOffset);
                break;
            default:
                LOGGER.warn("Unknown HUD element ID for config saving: {}", hudId);
                return;
        }
        
        // Configファイルに実際に保存
        try {
            HUDConfig.SPEC.save();
            LOGGER.info("Saved to config: {} -> offset ({}, {}) from default ({}, {})", 
                       hudId, currentPos.x - defaultPos.x, currentPos.y - defaultPos.y, defaultPos.x, defaultPos.y);
        } catch (Exception e) {
            LOGGER.error("Failed to save config for {}: {}", hudId, e.getMessage());
        }
    }
    
    public HUDElement getHudElement() {
        return hudElement;
    }
}