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
import com.greattomfoxsora.universalhudmanager.config.HUDConfig;

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
    
    /**
     * デバッグログが有効かどうかチェック
     */
    private static boolean isDebugEnabled() {
        return HUDConfig.DEBUG_MODE.get();
    }
    
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
    private VanillaHudOverlay selectedOverlay = null;  // 選択中のHUD要素
    private VanillaHudOverlay draggedOverlay = null;
    private double dragStartX, dragStartY;
    private Vector2i dragStartOffset;
    private Vector2i temporaryDragOffset = null;  // ドラッグ中の一時オフセット（Config保存せず）
    
    // 静的インスタンス参照（HUDPositionHandlerから一時オフセットを取得するため）
    private static HudEditScreen currentEditInstance = null;
    
    // 中央コンパクトHUD選択パネルUI
    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 200;
    private static final int ITEM_HEIGHT = 20;
    private static final int PADDING = 8;
    private int scrollOffset = 0;
    private boolean panelVisible = true; // T keyで切り替え
    
    public HudEditScreen(Screen parentScreen) {
        super(Component.translatable("screen.universalhudmanager.hud_edit"));
        this.parentScreen = parentScreen;
    }
    
    @Override
    protected void init() {
        super.init();
        
        LOGGER.info("HudEditScreen init() called! Screen size: {}x{}", width, height);
        
        // 静的インスタンス参照を設定（HUD描画システムからアクセス可能にする）
        currentEditInstance = this;
        
        // 新方式：Vanilla HUD + オーバーレイ方式（智の革命的設計）
        hudElements.clear();
        
        if (isDebugEnabled()) {
            LOGGER.debug("HudEditScreen initialized with Vanilla HUD overlay system (智's perfect design)");
        }
        
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
        
        if (isDebugEnabled()) {
            LOGGER.debug("HUD Edit Screen initialized with {} elements and control buttons", hudElements.size());
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // renderBackground()を使用して適切な半透明背景を描画
        this.renderBackground(guiGraphics);
        
        // 選択されたHUD要素のみ緑枠を描画
        renderSelectedHudOverlay(guiGraphics, mouseX, mouseY);
        
        // 中央パネル（T keyで表示/非表示）を描画
        if (panelVisible) {
            renderCenterPanel(guiGraphics, mouseX, mouseY);
        } else {
            // パネル非表示時はT keyヒントを中央に表示
            renderTKeyHint(guiGraphics);
        }
        
        // ボタンを描画（panelVisibleに連動）
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
    
    
    /**
     * 選択されたHUD要素のみ緑枠を描画（リアルタイム位置更新対応）
     */
    private void renderSelectedHudOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (selectedOverlay == null) return;
        
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        // 🌟 リアルタイム緑線位置：ドラッグ中は一時座標、通常時は確定座標
        Vector2i position;
        
        if (draggedOverlay == selectedOverlay) {
            // ドラッグ中：マウス位置ベースの一時座標を計算
            double deltaMouseX = mouseX - dragStartX;
            double deltaMouseY = mouseY - dragStartY;
            Vector2i tempOffset = new Vector2i(
                (int)(dragStartOffset.x + deltaMouseX),
                (int)(dragStartOffset.y + deltaMouseY)
            );
            Vector2i defaultPos = selectedOverlay.getDefaultPosition(screenWidth, screenHeight);
            position = new Vector2i(defaultPos.x + tempOffset.x, defaultPos.y + tempOffset.y);
        } else {
            // 通常時：最新の確定座標を使用（Config更新後の正確な位置）
            position = selectedOverlay.getActualPosition(screenWidth, screenHeight);
        }
        
        int x = position.x;
        int y = position.y;
        int w = selectedOverlay.getWidth();
        int h = selectedOverlay.getHeight();
        
        // 枠の色を決定（選択された要素のみ表示）
        int borderColor = 0xFF00FF00; // 緑色
        if (draggedOverlay == selectedOverlay) {
            borderColor = 0xFFFFFF00; // 黄色（ドラッグ中）
        }
        
        // 緑枠を描画（HUDと連動）
        drawHudOverlayInline(guiGraphics, x, y, w, h, borderColor);
        
        // ドラッグ中の表示
        if (draggedOverlay == selectedOverlay) {
            // 🌟 リアルタイム座標表示修正: ドラッグ中は temporaryDragOffset を使用
            Vector2i displayOffset;
            if (temporaryDragOffset != null) {
                // ドラッグ中: リアルタイムの一時オフセットを表示
                displayOffset = temporaryDragOffset;
            } else {
                // ドラッグ開始直後など: 現在の確定オフセットを表示
                displayOffset = selectedOverlay.getCurrentOffset();
            }
            
            // リアルタイム座標情報を準備
            Vector2i defaultPos = selectedOverlay.getDefaultPosition(screenWidth, screenHeight);
            int actualX = defaultPos.x + displayOffset.x;
            int actualY = defaultPos.y + displayOffset.y;
            
            // 🎯 画面端自動調整機能: 座標表示を賢く配置
            renderSmartCoordinateTooltip(guiGraphics, x, y, w, h, 
                                        selectedOverlay.getDisplayName(),
                                        displayOffset, actualX, actualY);
        }
    }
    
    /**
     * 🎯 画面端を考慮した賢い座標表示（智の要望: Minecraftツールチップ風自動調整）
     */
    private void renderSmartCoordinateTooltip(GuiGraphics guiGraphics, int hudX, int hudY, int hudW, int hudH,
                                             String hudName, Vector2i offset, int actualX, int actualY) {
        // 表示するテキストを準備
        String line1 = "Dragging " + hudName;
        String line2 = String.format("Offset: (%d, %d)", offset.x, offset.y);
        String line3 = String.format("Pos: (%d, %d)", actualX, actualY);
        
        // テキストサイズ計算
        int maxWidth = Math.max(font.width(line1), 
                       Math.max(font.width(line2), font.width(line3)));
        int tooltipWidth = maxWidth + 12;  // パディング込み
        int tooltipHeight = (font.lineHeight * 3) + 8;  // 3行分 + パディング
        
        // 画面サイズ取得
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        // 🌟 優先順位で位置を決定（智の要望: 画面端でも見えるように）
        int tooltipX, tooltipY;
        boolean showAbove = false;
        
        // 第1候補: HUD要素の下側（通常位置）
        tooltipX = hudX;
        tooltipY = hudY + hudH + 4;
        
        // 下端チェック: 画面外に出る場合は上側に表示
        if (tooltipY + tooltipHeight > screenHeight - 4) {
            tooltipY = hudY - tooltipHeight - 4;
            showAbove = true;
        }
        
        // 上端チェック: それでも画面外なら画面内に強制配置
        if (tooltipY < 4) {
            // HUD要素の横に表示を試みる
            tooltipY = Math.max(4, hudY);
            tooltipX = hudX + hudW + 4;
            
            // 右端チェック
            if (tooltipX + tooltipWidth > screenWidth - 4) {
                tooltipX = hudX - tooltipWidth - 4;
            }
        }
        
        // 左右端の最終チェック
        tooltipX = Math.max(4, Math.min(tooltipX, screenWidth - tooltipWidth - 4));
        tooltipY = Math.max(4, Math.min(tooltipY, screenHeight - tooltipHeight - 4));
        
        // 🎨 背景描画（智の要望: 薄い半透明背景、枠線なしでHUDが見えやすく）
        // 薄い半透明黒背景（透明度を高くして下が見えるように）
        guiGraphics.fill(tooltipX - 2, tooltipY - 2, 
                        tooltipX + tooltipWidth, tooltipY + tooltipHeight, 
                        0x80000000);  // 50%透明度の黒（以前は0xE0で12%透明度だった）
        
        // 📝 テキスト描画（影付きで読みやすく）
        int textY = tooltipY + 3;
        
        // Line 1: ドラッグ中メッセージ（黄色・影付き）
        guiGraphics.drawString(font, line1, tooltipX + 4, textY, 0xFFFFFF00, true);
        textY += font.lineHeight;
        
        // Line 2: オフセット座標（白・影付き）
        guiGraphics.drawString(font, line2, tooltipX + 4, textY, 0xFFFFFFFF, true);
        textY += font.lineHeight;
        
        // Line 3: 実際の座標（オレンジ・影付き）
        guiGraphics.drawString(font, line3, tooltipX + 4, textY, 0xFFFFAA00, true);
        
        // 🔺 位置インジケーター（上に表示している場合、控えめな下向き矢印）
        if (showAbove && hudY - tooltipY > 10) {
            // 小さな半透明矢印（邪魔にならないように）
            int arrowX = tooltipX + tooltipWidth / 2;
            int arrowY = tooltipY + tooltipHeight;
            guiGraphics.fill(arrowX - 1, arrowY, arrowX + 1, arrowY + 2, 0x80FFFF00);
        }
    }
    
    /**
     * Vanilla HUD上に緑枠オーバーレイを描画（智の革命的アイデア）
     */
    private void renderVanillaHudOverlays(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        LOGGER.info("renderVanillaHudOverlays called! Screen: {}x{}, Overlays: {}, Mouse: ({},{})", 
                   screenWidth, screenHeight, vanillaOverlays.size(), mouseX, mouseY);
        
        for (VanillaHudOverlay overlay : vanillaOverlays) {
            Vector2i position = overlay.getActualPosition(screenWidth, screenHeight);
            int x = position.x;
            int y = position.y;
            int w = overlay.getWidth();
            int h = overlay.getHeight();
            
            // 常にHealth Barの位置を表示
            if (overlay.getId().equals("health")) {
                LOGGER.info("Health Bar position: ({},{}) size: {}x{} offset: ({},{})", 
                           x, y, w, h, 
                           overlay.getCurrentOffset().x, overlay.getCurrentOffset().y);
            }
            
            // マウスオーバー検知
            boolean isMouseOver = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
            boolean isDragging = draggedOverlay == overlay;
            
            // デバッグ情報を追加
            if (isMouseOver && isDebugEnabled()) {
                LOGGER.debug("Mouse over {}: mouse=({},{}) overlay=({},{},{},{}) isOver={}", 
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
            if (overlay.getId().equals("health")) {
                LOGGER.info("Drawing Health Bar border! Color: 0x{}, Position: ({},{}) Size: {}x{}", 
                           Integer.toHexString(borderColor), x, y, w, h);
            }
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
        LOGGER.info("drawHudOverlayInline: Drawing at ({},{}) size {}x{} color=0x{}", 
                   x, y, width, height, Integer.toHexString(color));
        
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
        return false;  // ゲームプレイを継続（マルチプレイ対応）
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        LOGGER.info("=== MOUSE CLICK DEBUG ===");
        LOGGER.info("Mouse clicked: ({},{}) button={}", mouseX, mouseY, button);
        LOGGER.info("Screen size: {}x{}", width, height);
        
        if (button == 0) { // 左クリック
            // 中央パネル内でのクリック処理（最優先）
            if (panelVisible && handleCenterPanelClick(mouseX, mouseY)) {
                return true;
            }
            
            // ボタンエリアクリックチェック
            if (buttonsVisible && mouseY >= 5 && mouseY <= 25) {
                if (isDebugEnabled()) {
                    LOGGER.debug("Click detected in button area Y=5-25, allowing button handling");
                }
                // ボタンクリックを先に処理
                return super.mouseClicked(mouseX, mouseY, button);
            }
            
            // 🚨緊急テスト：すべてのクリックでドラッグ開始チェックを実行
            LOGGER.info("🚨 EMERGENCY TEST: All clicks will show drag check");
            
            // 選択されたHUD要素のみドラッグ開始可能
            if (selectedOverlay != null) {
                int screenWidth = minecraft.getWindow().getGuiScaledWidth();
                int screenHeight = minecraft.getWindow().getGuiScaledHeight();
                
                // 🌟 重要修正: 緑枠描画と同じ座標計算を使用（完全一致）
                Vector2i position;
                
                if (draggedOverlay == selectedOverlay) {
                    // ドラッグ中：一時座標を使用（緑枠描画と完全同期）
                    double deltaMouseX = mouseX - dragStartX;
                    double deltaMouseY = mouseY - dragStartY;
                    Vector2i tempOffset = new Vector2i(
                        (int)(dragStartOffset.x + deltaMouseX),
                        (int)(dragStartOffset.y + deltaMouseY)
                    );
                    Vector2i defaultPos = selectedOverlay.getDefaultPosition(screenWidth, screenHeight);
                    position = new Vector2i(defaultPos.x + tempOffset.x, defaultPos.y + tempOffset.y);
                } else {
                    // 通常時：確定座標を使用（緑枠描画と完全同期）
                    position = selectedOverlay.getActualPosition(screenWidth, screenHeight);
                }
                
                int x = position.x;
                int y = position.y;
                int w = selectedOverlay.getWidth();
                int h = selectedOverlay.getHeight();
                
                // 🔧 座標をint型に変換して精密判定
                int intMouseX = (int) Math.round(mouseX);
                int intMouseY = (int) Math.round(mouseY);
                
                // 🎯 ドラッグ範囲拡大：Armor Bar成功例に合わせて判定を緩和
                int margin = 10; // 10ピクセルのマージンを追加（アーマーバーレベルの操作性実現）
                boolean withinX = intMouseX >= (x - margin) && intMouseX <= (x + w + margin);
                boolean withinY = intMouseY >= (y - margin) && intMouseY <= (y + h + margin);
                boolean withinBounds = withinX && withinY;
                
                LOGGER.info("=== DRAG START CHECK ===");
                LOGGER.info("Selected: {} (ID: {})", selectedOverlay.getDisplayName(), selectedOverlay.getId());
                LOGGER.info("Mouse: ({}, {}) -> Rounded: ({}, {})", mouseX, mouseY, intMouseX, intMouseY);
                LOGGER.info("Overlay bounds: x={}-{} (width={}), y={}-{} (height={})", x, x+w, w, y, y+h, h);
                LOGGER.info("🎯 MARGIN: {} px added -> Effective bounds: x={}-{}, y={}-{}", margin, x-margin, x+w+margin, y-margin, y+h+margin);
                LOGGER.info("Within X: {} ({} >= {} && {} <= {})", withinX, intMouseX, x-margin, intMouseX, x+w+margin);
                LOGGER.info("Within Y: {} ({} >= {} && {} <= {})", withinY, intMouseY, y-margin, intMouseY, y+h+margin);
                LOGGER.info("Within bounds: {}", withinBounds);
                LOGGER.info("Current offset: ({}, {})", selectedOverlay.getCurrentOffset().x, selectedOverlay.getCurrentOffset().y);
                
                if (withinBounds) {
                    // ドラッグ開始（選択された要素のみ）
                    draggedOverlay = selectedOverlay;
                    dragStartX = mouseX;
                    dragStartY = mouseY;
                    dragStartOffset = selectedOverlay.getCurrentOffset();
                    
                    LOGGER.info("✅ DRAG STARTED: {} at mouse=({},{}) with offset=({},{})", 
                               selectedOverlay.getDisplayName(), mouseX, mouseY, 
                               dragStartOffset.x, dragStartOffset.y);
                    return true; // HUD要素のクリックは最優先
                } else {
                    LOGGER.info("❌ DRAG NOT STARTED: Click outside {} bounds", selectedOverlay.getDisplayName());
                }
            } else {
                LOGGER.info("⚠️ No overlay selected - cannot start drag");
            }
        }
        
        // HUD要素に当たらなかった場合のみボタンクリックを処理
        boolean buttonResult = super.mouseClicked(mouseX, mouseY, button);
        if (isDebugEnabled()) {
            LOGGER.debug("Button click result: {}", buttonResult);
        }
        return buttonResult;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && draggedOverlay != null) {
            // マウス移動量を計算
            double deltaMouseX = mouseX - dragStartX;
            double deltaMouseY = mouseY - dragStartY;
            
            // 新しいオフセットを計算（一時的に保持、Config保存しない）
            temporaryDragOffset = new Vector2i(
                (int)(dragStartOffset.x + deltaMouseX),
                (int)(dragStartOffset.y + deltaMouseY)
            );
            
            // 🚨 重要：ドラッグ中はConfig保存しない（FileSystemException防止）
            // draggedOverlay.saveOffset(temporaryDragOffset); // この行をコメントアウト
            hasUnsavedChanges = true;
            
            // 🌟 リアルタイム座標表示のための詳細ログ（Opus 4強化版）
            if (isDebugEnabled()) {
                Vector2i defaultPos = draggedOverlay.getDefaultPosition(
                    minecraft.getWindow().getGuiScaledWidth(), 
                    minecraft.getWindow().getGuiScaledHeight()
                );
                int actualX = defaultPos.x + temporaryDragOffset.x;
                int actualY = defaultPos.y + temporaryDragOffset.y;
                
                LOGGER.debug("🎯 REALTIME DRAG: {} | MouseDelta: ({},{}) | Offset: ({},{}) | ActualPos: ({},{})", 
                           draggedOverlay.getDisplayName(), 
                           (int)deltaMouseX, (int)deltaMouseY, 
                           temporaryDragOffset.x, temporaryDragOffset.y,
                           actualX, actualY);
            }
            
            return true;
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggedOverlay != null) {
            // ドラッグ終了時に一時オフセットを確定してConfigファイルに保存（FileSystemException防止完了）
            try {
                // 🚨 CRITICAL FIX: Save temporary offset from drag operation
                if (temporaryDragOffset != null) {
                    draggedOverlay.saveOffset(temporaryDragOffset);
                    LOGGER.info("✅ DRAG COMPLETE: {} final offset saved: ({},{})", 
                               draggedOverlay.getDisplayName(), temporaryDragOffset.x, temporaryDragOffset.y);
                    temporaryDragOffset = null;
                } else {
                    LOGGER.warn("⚠️ DRAG COMPLETE: No temporary offset to save for {}", draggedOverlay.getDisplayName());
                }
                
                // Save config file once (not every frame like before)
                HUDConfig.SPEC.save();
                hasUnsavedChanges = false;
                
                if (isDebugEnabled()) {
                    LOGGER.debug("Finished dragging {}: final offset=({},{}) - Config saved", 
                               draggedOverlay.getDisplayName(), 
                               draggedOverlay.getCurrentOffset().x, draggedOverlay.getCurrentOffset().y);
                }
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
            if (isDebugEnabled()) {
                LOGGER.debug("All HUD offsets saved to config (智's perfect system)");
            }
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
            if (isDebugEnabled()) {
                LOGGER.debug("Reset {}: offset set to (0,0) = Vanilla default position", overlay.getDisplayName());
            }
        }
        
        try {
            HUDConfig.SPEC.save();
            hasUnsavedChanges = false;
            if (isDebugEnabled()) {
                LOGGER.debug("All HUD elements reset to Vanilla defaults (offset 0,0) - 智's perfect expectation fulfilled");
            }
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
        // 静的インスタンス参照をクリア
        currentEditInstance = null;
        
        // 編集モードを終了
        HUDPositionHandler.setEditMode(false);
        
        if (isDebugEnabled()) {
            LOGGER.debug("HUD Edit Screen closed, edit mode disabled");
        }
        
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
    
    /**
     * 静的メソッド：HUDPositionHandlerから一時ドラッグオフセットを取得
     * @param hudId HUD要素のID
     * @return 一時ドラッグオフセット（ドラッグ中でない場合はnull）
     */
    public static Vector2i getTemporaryDragOffset(String hudId) {
        if (currentEditInstance != null && currentEditInstance.draggedOverlay != null && 
            currentEditInstance.draggedOverlay.getId().equals(hudId) && 
            currentEditInstance.temporaryDragOffset != null) {
            // 🌟 DEBUG: ドラッグ中の一時オフセット返却ログ
            LOGGER.info("getTemporaryDragOffset({}): returning offset ({},{})", 
                       hudId, currentEditInstance.temporaryDragOffset.x, currentEditInstance.temporaryDragOffset.y);
            return currentEditInstance.temporaryDragOffset;
        }
        // 🌟 DEBUG: 一時オフセットがnullの理由を記録
        LOGGER.info("getTemporaryDragOffset({}): null - editInstance={}, draggedOverlay={}, tempOffset={}", 
                   hudId, 
                   currentEditInstance != null ? "exists" : "null",
                   (currentEditInstance != null && currentEditInstance.draggedOverlay != null) ? currentEditInstance.draggedOverlay.getId() : "null",
                   (currentEditInstance != null && currentEditInstance.temporaryDragOffset != null) ? "exists" : "null");
        return null;
    }
    
    /**
     * 静的メソッド：編集モード中かどうかをチェック
     * @return 編集モード中の場合true
     */
    public static boolean isEditModeActive() {
        return currentEditInstance != null;
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
     * パネル全体の表示/非表示を切り替え（智の要望：全UI統一切り替え）
     */
    private void toggleButtonsVisibility() {
        panelVisible = !panelVisible;
        buttonsVisible = panelVisible;
        
        // ボタンの表示状態を更新
        if (saveButton != null) saveButton.visible = buttonsVisible;
        if (resetButton != null) resetButton.visible = buttonsVisible;
        if (exitButton != null) exitButton.visible = buttonsVisible;
        
        if (isDebugEnabled()) {
            LOGGER.debug("Panel visibility toggled: {}", panelVisible ? "VISIBLE" : "HIDDEN");
        }
    }
    
    /**
     * 中央コンパクトパネルを描画（智の要望：画面中央、最小限、HUDに邪魔しない）
     */
    private void renderCenterPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 画面中央にパネルを配置
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        
        // パネル背景（半透明黒、縁付き）
        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xE0000000);
        // 白い境界線
        guiGraphics.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelY + PANEL_HEIGHT + 1, 0xFFFFFFFF);
        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xE0000000);
        
        // タイトル
        guiGraphics.drawCenteredString(font, "HUD Elements", panelX + PANEL_WIDTH / 2, panelY + 8, 0xFFFFFF);
        
        // HUD要素リスト（スクロール対応）
        int listY = panelY + 25;
        int listHeight = PANEL_HEIGHT - 60; // ボタン領域を除く
        int maxVisible = listHeight / ITEM_HEIGHT;
        
        for (int i = 0; i < vanillaOverlays.size() && i < maxVisible; i++) {
            VanillaHudOverlay overlay = vanillaOverlays.get(i + scrollOffset);
            int itemY = listY + (i * ITEM_HEIGHT);
            
            // 選択状態・ホバー状態
            boolean isSelected = overlay == selectedOverlay;
            boolean isHovered = mouseX >= panelX + 5 && mouseX <= panelX + PANEL_WIDTH - 5 &&
                              mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT;
            
            if (isSelected) {
                guiGraphics.fill(panelX + 5, itemY, panelX + PANEL_WIDTH - 5, itemY + ITEM_HEIGHT, 0x8000FF00);
            } else if (isHovered) {
                guiGraphics.fill(panelX + 5, itemY, panelX + PANEL_WIDTH - 5, itemY + ITEM_HEIGHT, 0x40FFFFFF);
            }
            
            // テキスト描画
            int textColor = isSelected ? 0x00FF00 : 0xFFFFFF;
            guiGraphics.drawString(font, overlay.getDisplayName(), 
                                 panelX + PADDING, itemY + 4, textColor, false);
            
            // 🌟 オフセット情報（ドラッグ中はリアルタイム表示）
            Vector2i offset;
            if (draggedOverlay == overlay && temporaryDragOffset != null) {
                // ドラッグ中: リアルタイムオフセット
                offset = temporaryDragOffset;
            } else {
                // 通常時: 確定オフセット
                offset = overlay.getCurrentOffset();
            }
            
            String offsetText = String.format("(%d, %d)", offset.x, offset.y);
            int offsetColor = (draggedOverlay == overlay) ? 0xFFFF00 : 0xAAAAAA;
            guiGraphics.drawString(font, offsetText, 
                                 panelX + PANEL_WIDTH - 80, itemY + 4, offsetColor, false);
        }
        
        // 操作説明（パネル下部）
        if (selectedOverlay != null) {
            guiGraphics.drawCenteredString(font, "Selected: " + selectedOverlay.getDisplayName(), 
                                         panelX + PANEL_WIDTH / 2, panelY + PANEL_HEIGHT - 35, 0x00FF00);
            guiGraphics.drawCenteredString(font, "Drag green frame to move", 
                                         panelX + PANEL_WIDTH / 2, panelY + PANEL_HEIGHT - 20, 0xFFFFFF);
        } else {
            guiGraphics.drawCenteredString(font, "Select HUD element from list above", 
                                         panelX + PANEL_WIDTH / 2, panelY + PANEL_HEIGHT - 20, 0xAAAAAA);
        }
        
        // T key ヒント（パネル下）
        guiGraphics.drawCenteredString(font, "Press T to hide this panel", 
                                     panelX + PANEL_WIDTH / 2, panelY + PANEL_HEIGHT + 10, 0xAAAAAA);
    }
    
    /**
     * T keyヒントを中央に表示（パネル非表示時）
     */
    private void renderTKeyHint(GuiGraphics guiGraphics) {
        // エイム位置を避けて、少し上に配置
        int hintY = height / 2 - 40;
        
        // 半透明背景
        String hintText = "Press T to show HUD panel";
        int textWidth = font.width(hintText);
        int bgX = (width - textWidth - 20) / 2;
        int bgY = hintY - 8;
        
        guiGraphics.fill(bgX, bgY, bgX + textWidth + 20, bgY + 16, 0x80000000);
        guiGraphics.drawCenteredString(font, hintText, width / 2, hintY, 0xFFFFFF);
        
        // 未保存変更警告（必要時）
        if (hasUnsavedChanges) {
            guiGraphics.drawCenteredString(font, "Unsaved changes! H: Exit", 
                                         width / 2, hintY + 20, 0xFFFF5555);
        }
    }
    
    /**
     * 中央パネル内でのクリック処理
     */
    private boolean handleCenterPanelClick(double mouseX, double mouseY) {
        if (!panelVisible) return false;
        
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        int listY = panelY + 25;
        int listHeight = PANEL_HEIGHT - 60;
        int maxVisible = listHeight / ITEM_HEIGHT;
        
        // パネル内クリック判定
        if (mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH &&
            mouseY >= listY && mouseY <= listY + listHeight) {
            
            for (int i = 0; i < vanillaOverlays.size() && i < maxVisible; i++) {
                int itemY = listY + (i * ITEM_HEIGHT);
                if (mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT) {
                    selectedOverlay = vanillaOverlays.get(i + scrollOffset);
                    LOGGER.info("Selected HUD element: {}", selectedOverlay.getDisplayName());
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * サイドバーのクリック処理（旧方式・削除予定）
     */
    private boolean handleSidebarClick(double mouseX, double mouseY) {
        // 中央パネル方式に変更のため、この処理は使用されない
        return false;
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
        // Chat
        vanillaOverlays.add(new VanillaHudOverlay("chat", "Chat", 320, 160));
        // Armor Bar (追加)
        vanillaOverlays.add(new VanillaHudOverlay("armor", "Armor Bar", 81, 9));
        
        // 🚨 デバッグ：各HUD要素のデフォルト位置を表示
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        LOGGER.info("🚨 DEBUG: HUD Default Positions (Screen: {}x{})", screenWidth, screenHeight);
        for (VanillaHudOverlay overlay : vanillaOverlays) {
            Vector2i defaultPos = overlay.getDefaultPosition(screenWidth, screenHeight);
            Vector2i offset = overlay.getCurrentOffset();
            Vector2i actualPos = overlay.getActualPosition(screenWidth, screenHeight);
            
            LOGGER.info("  {}: default=({},{}) offset=({},{}) actual=({},{})", 
                       overlay.getId(), defaultPos.x, defaultPos.y, 
                       offset.x, offset.y, actualPos.x, actualPos.y);
        }
        
        if (isDebugEnabled()) {
            LOGGER.debug("Initialized {} Vanilla HUD overlays", vanillaOverlays.size());
        }
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
                case "armor": return HUDConfig.getArmorPosition();
                case "chat": return HUDConfig.getChatPosition();
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
                case "armor": HUDConfig.setArmorPosition(newOffset); break;
                case "chat": HUDConfig.setChatPosition(newOffset); break;
            }
        }
        
        /**
         * デフォルト位置を取得
         */
        public Vector2i getDefaultPosition(int screenWidth, int screenHeight) {
            switch (id) {
                case "health": return HUDConfig.getDefaultHealthPosition(screenWidth, screenHeight);
                case "food": return HUDConfig.getDefaultFoodPosition(screenWidth, screenHeight);
                case "experience": return HUDConfig.getDefaultExperiencePosition(screenWidth, screenHeight);
                case "hotbar": return HUDConfig.getDefaultHotbarPosition(screenWidth, screenHeight);
                case "air": return HUDConfig.getDefaultAirPosition(screenWidth, screenHeight);
                case "armor": return HUDConfig.getDefaultArmorPosition(screenWidth, screenHeight);
                case "chat": return HUDConfig.getDefaultChatPosition(screenWidth, screenHeight);
                default: return new Vector2i(0, 0);
            }
        }
    }
}