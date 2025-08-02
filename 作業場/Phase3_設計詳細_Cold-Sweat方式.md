# Universal HUD Manager Phase 3 設計詳細
## Cold-Sweat方式による実HUD制御実装計画

**設計日**: 2025-08-02  
**設計者**: GreatTomFox & Sora  
**技術基盤**: Cold-Sweat mod分析結果

---

## 🎯 設計概要

### 従来設計からの重要変更
- **Mixin方式 → ForgeGuiOverlay方式**: 安全性・互換性向上
- **個別座標管理 → Vector2i統一**: 設定管理の簡素化
- **独自HUD描画 → オーバーレイ再描画**: 既存HUD非表示化 + カスタム描画

### Cold-Sweat技術の活用ポイント
1. **IGuiOverlay**: Forge公式のHUD描画システム
2. **Vector2i**: 座標管理の統一フォーマット
3. **DynamicHolder**: リアルタイム設定変更対応
4. **RegisterGuiOverlaysEvent**: 安全なオーバーレイ登録

---

## 🏗️ アーキテクチャ設計

### 1. ForgeGuiOverlayベースシステム

#### 基本構造
```java
public class UniversalOverlays {
    // バニラHUD要素のオーバーレイ定義
    public static IGuiOverlay HEALTH_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (HUDConfig.HEALTH_ENABLED.get() && shouldRenderElement()) {
            Vector2i pos = HUDConfig.HEALTH_POS.get();
            int x = getDefaultHealthX(width) + pos.x();
            int y = getDefaultHealthY(height) + pos.y();
            renderCustomHealth(graphics, x, y);
        }
    };
    
    public static IGuiOverlay FOOD_OVERLAY = (gui, graphics, partialTick, width, height) -> {
        if (HUDConfig.FOOD_ENABLED.get() && shouldRenderElement()) {
            Vector2i pos = HUDConfig.FOOD_POS.get();
            int x = getDefaultFoodX(width) + pos.x();
            int y = getDefaultFoodY(height) + pos.y();
            renderCustomFood(graphics, x, y);
        }
    };
    
    // [他のHUD要素...]
}
```

#### 登録システム
```java
@SubscribeEvent
public static void registerOverlays(RegisterGuiOverlaysEvent event) {
    // バニラHUD要素を隠すために、上位レイヤーで再描画
    event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "custom_health", HEALTH_OVERLAY);
    event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "custom_food", FOOD_OVERLAY);
    event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "custom_experience", EXPERIENCE_OVERLAY);
    event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "custom_hotbar", HOTBAR_OVERLAY);
    // [他のオーバーレイ...]
}
```

### 2. Vector2i位置制御システム

#### 設定定義（HUDConfig.java）
```java
public class HUDConfig {
    // バニラ要素の位置設定
    public static final DynamicHolder<Vector2i> HEALTH_POS = 
        registerPosition("health_position", new Vector2i(0, 0));
    public static final DynamicHolder<Vector2i> FOOD_POS = 
        registerPosition("food_position", new Vector2i(0, 0));
    public static final DynamicHolder<Vector2i> EXPERIENCE_POS = 
        registerPosition("experience_position", new Vector2i(0, 0));
    public static final DynamicHolder<Vector2i> HOTBAR_POS = 
        registerPosition("hotbar_position", new Vector2i(0, 0));
    public static final DynamicHolder<Vector2i> AIR_POS = 
        registerPosition("air_position", new Vector2i(0, 0));
    public static final DynamicHolder<Vector2i> CHAT_POS = 
        registerPosition("chat_position", new Vector2i(0, 0));
    
    // 有効/無効設定
    public static final DynamicHolder<Boolean> HEALTH_ENABLED = 
        registerBoolean("health_enabled", true);
    public static final DynamicHolder<Boolean> FOOD_ENABLED = 
        registerBoolean("food_enabled", true);
    // [他の要素...]
    
    private static DynamicHolder<Vector2i> registerPosition(String name, Vector2i defaultValue) {
        return DynamicHolder.create(CONFIG_SPEC, name, defaultValue,
            (config) -> {
                List<Integer> list = config.get(name);
                return new Vector2i(list.get(0), list.get(1));
            },
            (value) -> Arrays.asList(value.x(), value.y())
        );
    }
}
```

### 3. バニラHUD要素の制御方式

#### 方式A: オーバーレイ上書き（推奨）
```java
public static void renderCustomHealth(GuiGraphics graphics, int x, int y) {
    Player player = Minecraft.getInstance().player;
    if (player == null) return;
    
    // 1. バニラHUDを透明化（RenderSystem操作）
    RenderSystem.enableBlend();
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.0F); // 透明化
    
    // 2. カスタム位置で再描画
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // 不透明化
    // バニラと同じ描画ロジックを指定位置で実行
    renderHealthBar(graphics, x, y, player);
    
    RenderSystem.disableBlend();
}
```

#### 方式B: Mixin最小限介入（代替案）
```java
// GuiMixin.java - 最小限のMixin
@Mixin(Gui.class)
public class GuiMixin {
    @Redirect(method = "renderPlayerHealth", 
              at = @At(value = "INVOKE", 
                      target = "renderHearts"))
    private void redirectHealthPosition(/* parameters */) {
        if (HUDConfig.HEALTH_ENABLED.get()) {
            Vector2i pos = HUDConfig.HEALTH_POS.get();
            // 位置調整してから描画
            renderHeartsAtPosition(x + pos.x(), y + pos.y(), /* other params */);
        } else {
            // 元の位置で描画
            renderHearts(/* original params */);
        }
    }
}
```

---

## 🎮 HUD要素別実装計画

### 対象バニラ要素（Phase 3）
1. **Health Bar** (体力バー)
   - 位置: 左下基準
   - 特徴: ハート形状・段階表示
   - 実装難易度: ★★☆

2. **Food Bar** (満腹度バー)
   - 位置: 右下基準  
   - 特徴: 肉形状・段階表示
   - 実装難易度: ★★☆

3. **Experience Bar** (経験値バー)
   - 位置: 下中央基準
   - 特徴: 緑のバー・数値表示
   - 実装難易度: ★☆☆

4. **Hotbar** (ホットバー)
   - 位置: 下中央基準
   - 特徴: アイテムスロット・選択枠
   - 実装難易度: ★★★ (複雑性)

5. **Air/Oxygen Bar** (酸素バー)
   - 位置: 右下基準（体力バー上）
   - 特徴: 水中時のみ表示
   - 実装難易度: ★★☆

6. **Chat** (チャット)
   - 位置: 左下基準
   - 特徴: メッセージリスト・フェード
   - 実装難易度: ★★★ (複雑性)

### mod対応要素（Phase 4予定）
- **JEI Search Box**: 自動検出・位置制御
- **Jade Block Info**: ツールチップ位置制御
- **Minimap系**: 各mod対応

---

## 🔧 実装フェーズ

### Phase 3.1: 基盤実装
1. **HUDConfig.java**: Vector2i設定システム
2. **UniversalOverlays.java**: ForgeGuiOverlay基盤
3. **基本オーバーレイ**: Health + Food Bar

### Phase 3.2: 拡張実装  
1. **Experience Bar**: 数値・バー描画
2. **Air Bar**: 条件付き表示制御
3. **編集モード統合**: 既存UI連携

### Phase 3.3: 高度実装
1. **Hotbar**: 複雑なスロット・選択枠制御
2. **Chat**: メッセージリスト・フェード効果
3. **設定永続化**: Config保存・復元

### Phase 3.4: 統合・テスト
1. **全要素統合テスト**: 46mod環境での動作確認
2. **Performance測定**: Spark測定・最適化
3. **UI/UX調整**: ドラッグ&ドロップ精度向上

---

## 📊 技術仕様詳細

### Vector2i座標系
```java
// 基準座標系（バニラ準拠）
// Health: (width/2 - 91, height - 39)
// Food: (width/2 + 91, height - 39)  
// Experience: (width/2 - 91, height - 32)
// Hotbar: (width/2 - 91, height - 22)

// オフセット適用
int finalX = baseX + offset.x();
int finalY = baseY + offset.y();
```

### 設定ファイル構造
```toml
[hud_positions]
    health_position = [0, 0]
    food_position = [0, 0]
    experience_position = [0, 0]
    hotbar_position = [0, 0]
    air_position = [0, 0]
    chat_position = [0, 0]

[hud_enabled]
    health_enabled = true
    food_enabled = true
    experience_enabled = true
    hotbar_enabled = true
    air_enabled = true
    chat_enabled = true
```

---

## 🎯 Cold-Sweat方式の利点

### 技術的利点
1. **安全性**: Mixin不要でForge公式API使用
2. **互換性**: 他mod競合リスク最小化
3. **保守性**: シンプルな構造・理解しやすい
4. **拡張性**: 新HUD要素追加が容易

### 実装利点
1. **設定統一**: Vector2i統一による管理簡素化
2. **リアルタイム変更**: DynamicHolder対応
3. **デバッグ容易**: オーバーレイ単位での制御可能
4. **Performance**: 軽量なオーバーレイ描画

---

## 🚀 次ステップ

1. **HUDConfig.java実装**: Vector2i設定システム構築
2. **UniversalOverlays.java実装**: ForgeGuiOverlay基盤構築  
3. **Health Bar サンプル**: 最初の実動確認
4. **編集モード統合**: 既存ドラッグ&ドロップUI連携

---

**設計完了**: 2025-08-02  
**実装予定**: Claude Code利用上限回復後  
**期待効果**: Phase 1-2の「緑枠ドラッグ」から「実HUD移動」への革命的進化 ✨