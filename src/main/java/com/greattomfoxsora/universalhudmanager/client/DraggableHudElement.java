package com.greattomfoxsora.universalhudmanager.client;

import com.mojang.logging.LogUtils;
import com.greattomfoxsora.universalhudmanager.core.HUDElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
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
    
    // 表示用の色定義
    private static final int BORDER_COLOR_NORMAL = 0xFF00FF00;    // 緑色の枠
    private static final int BORDER_COLOR_HOVER = 0xFF00FFFF;     // シアンの枠（ホバー時）
    private static final int BORDER_COLOR_DRAG = 0xFFFFFF00;      // 黄色の枠（ドラッグ時）
    private static final int BACKGROUND_COLOR = 0x80000000;       // 半透明の背景
    
    public DraggableHudElement(HUDElement hudElement, int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal(hudElement.getDisplayName()));
        this.hudElement = hudElement;
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
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            isDragging = true;
            dragStartX = mouseX;
            dragStartY = mouseY;
            elementStartX = this.getX();
            elementStartY = this.getY();
            
            LOGGER.info("Started dragging HUD element: {} at ({}, {})", 
                       hudElement.getDisplayName(), mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging && button == 0) {
            // 新しい位置を計算
            double newX = elementStartX + (mouseX - dragStartX);
            double newY = elementStartY + (mouseY - dragStartY);
            
            // 画面境界内に制限
            newX = Math.max(0, Math.min(newX, net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth() - width));
            newY = Math.max(0, Math.min(newY, net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight() - height));
            
            // 位置を更新
            this.setX((int)newX);
            this.setY((int)newY);
            
            return true;
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
        hudElement.setX(getX());
        hudElement.setY(getY());
        
        LOGGER.info("Saved position for HUD element: {} at ({}, {})", 
                   hudElement.getDisplayName(), getX(), getY());
    }
    
    public HUDElement getHudElement() {
        return hudElement;
    }
}