package com.tomonosora.universalhudmanager.client;

import com.mojang.logging.LogUtils;
import com.tomonosora.universalhudmanager.core.HUDElement;
import com.tomonosora.universalhudmanager.core.HUDRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD編集用スクリーン - ドラッグ&ドロップでHUD要素を配置
 * FirstAid modの実装を参考に作成
 * 
 * @author Tomo & Sora
 */
public class HudEditScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private final List<DraggableHudElement> hudElements = new ArrayList<>();
    private final Screen parentScreen;
    
    public HudEditScreen(Screen parentScreen) {
        super(Component.translatable("screen.universalhudmanager.hud_edit"));
        this.parentScreen = parentScreen;
    }
    
    @Override
    protected void init() {
        super.init();
        
        // 既存のHUD要素を編集可能な状態で追加
        hudElements.clear();
        int yOffset = 40; // 上部の説明文の下から開始
        int index = 0;
        
        for (HUDElement element : HUDRegistry.getAllHUDs()) {
            // 初期位置を設定（デフォルト位置があれば使用、なければ左側に配置）
            int x = element.getX() == 0 ? 20 : element.getX();
            int y = element.getY() == 0 ? yOffset + (index * 30) : element.getY();
            
            DraggableHudElement draggable = new DraggableHudElement(
                element,
                x, y,
                200, // 幅を広く
                25   // 高さを調整
            );
            addRenderableWidget(draggable);
            hudElements.add(draggable);
            
            LOGGER.info("Added draggable HUD element: {} at ({}, {})", element.getDisplayName(), x, y);
            index++;
        }
        
        LOGGER.info("HUD Edit Screen initialized with {} elements", hudElements.size());
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 半透明の背景を描画（編集モードを示す）
        guiGraphics.fill(0, 0, width, height, 0x80000000);
        
        // HUD要素を描画
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        
        // 編集ヒントを表示
        guiGraphics.drawCenteredString(font, 
            "Drag HUD elements to reposition them", 
            width / 2, 10, 0xFFFFFF);
        
        // 操作説明を表示
        guiGraphics.drawCenteredString(font, 
            "H: Exit Edit Mode | ESC: Save and Exit", 
            width / 2, height - 30, 0xAAAAAA);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;  // ゲームプレイを継続
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return true;   // ESCキーで閉じる
    }
    
    @Override
    public void onClose() {
        // 変更された位置を保存
        for (DraggableHudElement draggable : hudElements) {
            draggable.savePosition();
        }
        
        LOGGER.info("HUD Edit Screen closed, positions saved");
        
        // 元の画面に戻る
        if (parentScreen != null) {
            minecraft.setScreen(parentScreen);
        } else {
            super.onClose();
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Hキーでも閉じることができる
        if (keyCode == 72) { // H key
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}