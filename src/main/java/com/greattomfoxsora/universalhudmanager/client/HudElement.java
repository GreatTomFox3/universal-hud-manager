package com.greattomfoxsora.universalhudmanager.client;

import org.joml.Vector2i;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * シンプルなHUD要素 - アーマーHUD方式ベース
 * Health + Armor の2個のみに対応
 * 
 * @author GreatTomFox & Sora
 */
public class HudElement {
    
    public final String id;
    public final String displayName;
    public final int width;
    public final int height;
    
    // 位置計算関数
    private final BiFunction<Integer, Integer, Vector2i> defaultPositionProvider;
    private final Supplier<Vector2i> currentOffsetProvider;
    private final Consumer<Vector2i> offsetSetter;
    
    // 一時ドラッグ状態
    private Vector2i temporaryOffset = null;
    
    public HudElement(
        String id,
        String displayName,
        int width,
        int height,
        BiFunction<Integer, Integer, Vector2i> defaultPositionProvider,
        Supplier<Vector2i> currentOffsetProvider,
        Consumer<Vector2i> offsetSetter
    ) {
        this.id = id;
        this.displayName = displayName;
        this.width = width;
        this.height = height;
        this.defaultPositionProvider = defaultPositionProvider;
        this.currentOffsetProvider = currentOffsetProvider;
        this.offsetSetter = offsetSetter;
    }
    
    /**
     * デフォルト位置を取得 - アーマーHUD方式
     */
    public Vector2i getDefaultPosition(int screenWidth, int screenHeight) {
        return defaultPositionProvider.apply(screenWidth, screenHeight);
    }
    
    /**
     * 現在のオフセットを取得
     */
    public Vector2i getCurrentOffset() {
        return currentOffsetProvider.get();
    }
    
    /**
     * 最終位置を計算（デフォルト位置 + オフセット）- アーマーHUD成功方式
     */
    public Vector2i getFinalPosition(int screenWidth, int screenHeight) {
        Vector2i defaultPos = getDefaultPosition(screenWidth, screenHeight);
        Vector2i offset = (temporaryOffset != null) ? temporaryOffset : getCurrentOffset();
        
        return new Vector2i(defaultPos.x + offset.x, defaultPos.y + offset.y);
    }
    
    /**
     * 一時的なオフセットを設定（ドラッグ中）
     */
    public void setTemporaryOffset(Vector2i offset) {
        this.temporaryOffset = offset;
    }
    
    /**
     * 一時的なオフセットを取得
     */
    public Vector2i getTemporaryOffset() {
        return temporaryOffset;
    }
    
    /**
     * 一時的なオフセットをクリア
     */
    public void clearTemporaryOffset() {
        this.temporaryOffset = null;
    }
    
    /**
     * オフセットを保存
     */
    public void saveOffset(Vector2i offset) {
        offsetSetter.accept(offset);
    }
    
    @Override
    public String toString() {
        return String.format("HudElement{id='%s', display='%s', size=(%dx%d)}", 
                           id, displayName, width, height);
    }
}