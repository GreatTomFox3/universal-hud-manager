package com.greattomfoxsora.universalhudmanager.client;

import com.mojang.logging.LogUtils;
import com.greattomfoxsora.universalhudmanager.core.HUDElement;
import com.greattomfoxsora.universalhudmanager.core.HUDRegistry;
import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD編集用スクリーン - ドラッグ&ドロップでHUD要素を配置
 * FirstAid modの実装を参考に作成
 * 
 * @author GreatTomFox & Sora
 */
public class HudEditScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private final List<DraggableHudElement> hudElements = new ArrayList<>();
    private final Screen parentScreen;
    private boolean hasUnsavedChanges = false;
    private boolean buttonsVisible = true; // ボタンパネルの表示状態
    
    // Button references
    private Button saveButton;
    private Button resetButton;
    private Button exitButton;
    
    // 新方式：Vanilla HUD オーバーレイ管理
    private final List<VanillaHudOverlay> vanillaOverlays = new ArrayList<>();
    private VanillaHudOverlay draggedOverlay = null;
    private double dragStartX, dragStartY;
    private Vector2i dragStartOffset;
    
    public HudEditScreen(Screen parentScreen) {
        super(Component.translatable("screen.universalhudmanager.hud_edit"));
        this.parentScreen = parentScreen;
    }
    
    @Override
    protected void init() {
        super.init();
        
        // 新方式：Vanilla HUD + オーバーレイ方式（智の革命的設計）
        hudElements.clear();
        
        LOGGER.info("HudEditScreen initialized with Vanilla HUD overlay system (智's perfect design)");
        
        // Vanilla HUDの描画位置を検出して管理
        initializeVanillaHudOverlays();
        
        // 制御ボタンを追加（画面右上に配置してVanilla HUDと完全に重ならないように）
        int buttonWidth = 60;
        int buttonHeight = 18;
        int buttonY = 5; // 画面最上部
        int spacing = 65;
        int startX = width - (spacing * 3) - 10; // 右上に配置
        
        saveButton = Button.builder(Component.literal("Save"), button -> saveChanges())
                .bounds(startX, buttonY, buttonWidth, buttonHeight)
                .build();
        
        resetButton = Button.builder(Component.literal("Reset"), button -> resetToDefaults())
                .bounds(startX + spacing, buttonY, buttonWidth, buttonHeight)
                .build();
        
        exitButton = Button.builder(Component.literal("Exit"), button -> attemptClose())
                .bounds(startX + spacing * 2, buttonY, buttonWidth, buttonHeight)
                .build();
        
        // ボタンの初期表示状態を設定
        saveButton.visible = buttonsVisible;
        resetButton.visible = buttonsVisible;
        exitButton.visible = buttonsVisible;
        
        // ボタンを最後に追加（最優先で表示される）
        addRenderableWidget(saveButton);
        addRenderableWidget(resetButton);
        addRenderableWidget(exitButton);
        
        LOGGER.info("HUD Edit Screen initialized with {} elements and control buttons", hudElements.size());
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 半透明の背景を描画（編集モードを示す）
        guiGraphics.fill(0, 0, width, height, 0x80000000);
        
        // 新方式：Vanilla HUD上に緑枠オーバーレイを描画（智の完璧な設計）
        renderVanillaHudOverlays(guiGraphics, mouseX, mouseY);
        
        // ボタンを描画
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        
        // 編集ヒントを左下に表示（HUD要素と重ならない位置）
        guiGraphics.drawString(font, "Drag HUD elements to reposition them", 10, height - 60, 0xFFFFFF, false);
        
        // 操作説明を左下に表示
        String instructions = hasUnsavedChanges ? "Unsaved changes detected!" : "H: Exit | T: Toggle Buttons";
        int instructionColor = hasUnsavedChanges ? 0xFFFF5555 : 0xAAAAAA;
        guiGraphics.drawString(font, instructions, 10, height - 45, instructionColor, false);
        
        // ボタン表示状態の説明
        if (!buttonsVisible) {
            String hiddenMessage = "Control buttons hidden - Press T to show";
            guiGraphics.drawString(font, hiddenMessage, 10, height - 30, 0xFFFFAA, false);
        }
    }
    
    /**
     * Vanilla HUD上に緑枠オーバーレイを描画（智の革命的アイデア）
     */
    private void renderVanillaHudOverlays(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        for (VanillaHudOverlay overlay : vanillaOverlays) {
            Vector2i position = overlay.getActualPosition(screenWidth, screenHeight);
            int x = position.x;
            int y = position.y;
            int w = overlay.getWidth();
            int h = overlay.getHeight();
            
            // マウスオーバー検知
            boolean isMouseOver = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
            boolean isDragging = draggedOverlay == overlay;
            
            // デバッグ情報を追加
            if (isMouseOver) {
                LOGGER.info("Mouse over {}: mouse=({},{}) overlay=({},{},{},{}) isOver={}", 
                           overlay.getDisplayName(), mouseX, mouseY, x, y, w, h, isMouseOver);
            }
            
            // 枠の色を決定
            int borderColor;
            if (isDragging) {
                borderColor = 0xFFFFFF00; // 黄色（ドラッグ中）
            } else if (isMouseOver) {
                borderColor = 0xFF00FFFF; // シアン（ホバー時）
            } else {
                borderColor = 0xFF00FF00; // 緑色（通常）
            }
            
            // 緑枠を描画（Vanilla HUDの各辺ギリギリインライン）
            drawHudOverlayInline(guiGraphics, x, y, w, h, borderColor);
            
            // ドラッグ中の表示
            if (isDragging) {
                guiGraphics.drawCenteredString(font, "Dragging " + overlay.getDisplayName(), 
                                             x + w/2, y - 10, 0xFFFFFF00);
            }
            
            // デバッグ情報（一時的）
            Vector2i offset = overlay.getCurrentOffset();
            String debugInfo = String.format("%s: offset(%d,%d)", overlay.getId(), offset.x, offset.y);
            guiGraphics.drawString(font, debugInfo, x, y + h + 2, 0xFFAAAAA, false);
        }
    }
    
    /**
     * HUD要素の枠をインライン描画（智の要望：各辺ギリギリに緑線）
     */
    private void drawHudOverlayInline(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        // 上辺（内側）
        guiGraphics.fill(x, y, x + width, y + 1, color);
        // 下辺（内側）
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color);
        // 左辺（内側）
        guiGraphics.fill(x, y, x + 1, y + height, color);
        // 右辺（内側）
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;  // ゲームプレイを継続
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        LOGGER.info("Mouse clicked: ({},{}) button={}", mouseX, mouseY, button);
        
        if (button == 0) { // 左クリック
            // ボタンエリアクリックチェック（デバッグ用）
            if (buttonsVisible && mouseY >= 5 && mouseY <= 25) {
                LOGGER.info("Click detected in button area Y=5-25, allowing button handling");
                // ボタンクリックを先に処理
                return super.mouseClicked(mouseX, mouseY, button);
            }
            
            // Vanilla HUDオーバーレイのクリック検知（ボタンエリア外優先）
            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();
            
            LOGGER.info("Screen size: {}x{} - Checking {} overlays", screenWidth, screenHeight, vanillaOverlays.size());
            
            for (VanillaHudOverlay overlay : vanillaOverlays) {
                Vector2i position = overlay.getActualPosition(screenWidth, screenHeight);
                int x = position.x;
                int y = position.y;
                int w = overlay.getWidth();
                int h = overlay.getHeight();
                
                LOGGER.info("Checking click on {}: mouse=({},{}) overlay=({},{},{},{})", 
                           overlay.getDisplayName(), mouseX, mouseY, x, y, w, h);
                
                if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                    // ドラッグ開始
                    draggedOverlay = overlay;
                    dragStartX = mouseX;
                    dragStartY = mouseY;
                    dragStartOffset = overlay.getCurrentOffset();
                    
                    LOGGER.info("Started dragging {}: mouse=({},{}) currentOffset=({},{})", 
                               overlay.getDisplayName(), mouseX, mouseY, 
                               dragStartOffset.x, dragStartOffset.y);
                    return true; // HUD要素のクリックは最優先
                } else {
                    LOGGER.info("Click missed {}: outside bounds", overlay.getDisplayName());
                }
            }
        }
        
        // HUD要素に当たらなかった場合のみボタンクリックを処理
        boolean buttonResult = super.mouseClicked(mouseX, mouseY, button);
        LOGGER.info("Button click result: {}", buttonResult);
        return buttonResult;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && draggedOverlay != null) {
            // マウス移動量を計算
            double deltaMouseX = mouseX - dragStartX;
            double deltaMouseY = mouseY - dragStartY;
            
            // 新しいオフセットを計算
            Vector2i newOffset = new Vector2i(
                (int)(dragStartOffset.x + deltaMouseX),
                (int)(dragStartOffset.y + deltaMouseY)
            );
            
            // オフセットを更新（リアルタイムでVanilla HUDに反映）
            draggedOverlay.saveOffset(newOffset);
            hasUnsavedChanges = true;
            
            LOGGER.info("Dragging {}: mouseDelta=({},{}) newOffset=({},{})", 
                       draggedOverlay.getDisplayName(), deltaMouseX, deltaMouseY, 
                       newOffset.x, newOffset.y);
            
            return true;
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggedOverlay != null) {
            // ドラッグ終了時にConfigファイルに保存（クラッシュ防止）
            try {
                HUDConfig.SPEC.save();
                LOGGER.info("Finished dragging {}: final offset=({},{}) - Config saved", 
                           draggedOverlay.getDisplayName(), 
                           draggedOverlay.getCurrentOffset().x, draggedOverlay.getCurrentOffset().y);
            } catch (Exception e) {
                LOGGER.error("Failed to save config after drag: {}", e.getMessage());
            }
            
            draggedOverlay = null;
            return true;
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        attemptClose();
        return false;   // 直接閉じずに、確認処理を通す
    }
    
    /**
     * 閉じる前に未保存変更の確認
     */
    private void attemptClose() {
        if (hasUnsavedChanges) {
            ConfirmScreen confirmScreen = new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        saveChanges();
                    }
                    // 保存するかどうかに関わらず閉じる
                    closeScreen();
                },
                Component.literal("Unsaved Changes"),
                Component.literal("You have unsaved changes. Save before closing?")
            );
            minecraft.setScreen(confirmScreen);
        } else {
            closeScreen();
        }
    }
    
    /**
     * 変更を保存（新方式：オフセットのみ保存）
     */
    private void saveChanges() {
        try {
            HUDConfig.SPEC.save();
            hasUnsavedChanges = false;
            LOGGER.info("All HUD offsets saved to config (智's perfect system)");
        } catch (Exception e) {
            LOGGER.error("Failed to save HUD config: {}", e.getMessage());
        }
    }
    
    /**
     * デフォルト位置にリセット
     */
    private void resetToDefaults() {
        ConfirmScreen confirmScreen = new ConfirmScreen(
            confirmed -> {
                if (confirmed) {
                    resetPositionsToDefault();
                    // リセット後に保存フラグをクリア（リセット自体は一時的な変更）
                    hasUnsavedChanges = false;
                }
                // 確認後は元の編集画面に戻る
                minecraft.setScreen(this);
            },
            Component.literal("Reset to Defaults"),
            Component.literal("Reset all HUD elements to default positions?")
        );
        minecraft.setScreen(confirmScreen);
    }
    
    /**
     * 全HUD要素をVanillaのデフォルト位置にリセット（智の完璧な期待）
     */
    private void resetPositionsToDefault() {
        // 全オフセットを0,0にリセット（智の期待：Vanilla位置 = 0,0）
        for (VanillaHudOverlay overlay : vanillaOverlays) {
            overlay.saveOffset(new Vector2i(0, 0));
            LOGGER.info("Reset {}: offset set to (0,0) = Vanilla default position", overlay.getDisplayName());
        }
        
        try {
            HUDConfig.SPEC.save();
            hasUnsavedChanges = false;
            LOGGER.info("All HUD elements reset to Vanilla defaults (offset 0,0) - 智's perfect expectation fulfilled");
        } catch (Exception e) {
            LOGGER.error("Failed to save reset config: {}", e.getMessage());
        }
    }
    
    /**
     * HUD要素のデフォルト位置を取得
     */
    private Vector2i getDefaultPosition(String hudId, int screenWidth, int screenHeight) {
        switch (hudId) {
            case "health":
                return HUDConfig.getDefaultHealthPosition(screenWidth, screenHeight);
            case "food":
                return HUDConfig.getDefaultFoodPosition(screenWidth, screenHeight);
            case "experience":
                return HUDConfig.getDefaultExperiencePosition(screenWidth, screenHeight);
            case "hotbar":
                return HUDConfig.getDefaultHotbarPosition(screenWidth, screenHeight);
            case "air":
                return HUDConfig.getDefaultAirPosition(screenWidth, screenHeight);
            case "chat":
                return HUDConfig.getDefaultChatPosition(screenWidth, screenHeight);
            default:
                return new Vector2i(20, 40);
        }
    }
    
    /**
     * 画面を閉じる
     */
    private void closeScreen() {
        // 編集モードを終了
        HUDPositionHandler.setEditMode(false);
        
        LOGGER.info("HUD Edit Screen closed, edit mode disabled");
        
        // 元の画面に戻る
        if (parentScreen != null) {
            minecraft.setScreen(parentScreen);
        } else {
            minecraft.setScreen(null);
        }
    }
    
    /**
     * 変更があったことを記録
     */
    public void markChanged() {
        hasUnsavedChanges = true;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Hキーでも閉じることができる
        if (keyCode == 72) { // H key
            attemptClose();
            return true;
        }
        
        // Tキーでボタンパネルの表示/非表示切り替え
        if (keyCode == 84) { // T key
            toggleButtonsVisibility();
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    /**
     * ボタンパネルの表示/非表示を切り替え
     */
    private void toggleButtonsVisibility() {
        buttonsVisible = !buttonsVisible;
        
        // ボタンの表示状態を更新
        if (saveButton != null) saveButton.visible = buttonsVisible;
        if (resetButton != null) resetButton.visible = buttonsVisible;
        if (exitButton != null) exitButton.visible = buttonsVisible;
        
        LOGGER.info("Button panel visibility toggled: {}", buttonsVisible ? "VISIBLE" : "HIDDEN");
    }
    
    /**
     * Vanilla HUD オーバーレイシステムの初期化（智の完璧な設計）
     */
    private void initializeVanillaHudOverlays() {
        vanillaOverlays.clear();
        
        // Health Bar
        vanillaOverlays.add(new VanillaHudOverlay("health", "Health Bar", 81, 9));
        // Food Bar
        vanillaOverlays.add(new VanillaHudOverlay("food", "Food Bar", 81, 9));
        // Experience Bar
        vanillaOverlays.add(new VanillaHudOverlay("experience", "Experience Bar", 182, 5));
        // Hotbar
        vanillaOverlays.add(new VanillaHudOverlay("hotbar", "Hotbar", 182, 22));
        // Air Bar
        vanillaOverlays.add(new VanillaHudOverlay("air", "Air Bar", 81, 9));
        
        LOGGER.info("Initialized {} Vanilla HUD overlays", vanillaOverlays.size());
    }
    
    /**
     * Vanilla HUD オーバーレイクラス（智の革命的アイデア）
     */
    private static class VanillaHudOverlay {
        private final String id;
        private final String displayName;
        private final int width;
        private final int height;
        
        public VanillaHudOverlay(String id, String displayName, int width, int height) {
            this.id = id;
            this.displayName = displayName;
            this.width = width;
            this.height = height;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        
        /**
         * Vanilla HUDの実際の描画位置を取得（ResourcePackCompatibleOverlaysと同じ計算）
         */
        public Vector2i getActualPosition(int screenWidth, int screenHeight) {
            // ResourcePackCompatibleOverlaysと同じ位置計算を使用
            return ResourcePackCompatibleOverlays.getActualHudPosition(id, screenWidth, screenHeight);
        }
        
        /**
         * 現在のオフセットを取得
         */
        public Vector2i getCurrentOffset() {
            switch (id) {
                case "health": return HUDConfig.getHealthPosition();
                case "food": return HUDConfig.getFoodPosition();
                case "experience": return HUDConfig.getExperiencePosition();
                case "hotbar": return HUDConfig.getHotbarPosition();
                case "air": return HUDConfig.getAirPosition();
                default: return new Vector2i(0, 0);
            }
        }
        
        /**
         * オフセットをConfigに保存（リアルタイム反映）
         */
        public void saveOffset(Vector2i newOffset) {
            switch (id) {
                case "health": HUDConfig.setHealthPosition(newOffset); break;
                case "food": HUDConfig.setFoodPosition(newOffset); break;
                case "experience": HUDConfig.setExperiencePosition(newOffset); break;
                case "hotbar": HUDConfig.setHotbarPosition(newOffset); break;
                case "air": HUDConfig.setAirPosition(newOffset); break;
            }
        }
    }
}