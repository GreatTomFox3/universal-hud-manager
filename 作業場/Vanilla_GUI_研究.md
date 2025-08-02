# Vanilla GUI描画メソッド研究
## Resource Pack対応HUD位置制御のためのバニラ描画ロジック流用

**目的**: Resource Pack・modエフェクト完全対応の「位置だけ変更」方式実装

---

## 🎯 新実装方針

### ✅ 従来方式の問題
- **固定テクスチャ描画**: `graphics.blit(GUI_ICONS_LOCATION, ...)` 
- **Resource Pack無視**: カスタムテクスチャが適用されない
- **mod非互換**: 他modのHUD拡張が適用されない

### 🌟 新方式の利点
- **バニラ描画ロジック流用**: Minecraft本体の描画メソッドを位置調整して呼び出し
- **Resource Pack完全対応**: テクスチャ・エフェクト・アニメーション全て適用
- **mod互換性**: 他modのHUD拡張も自動適用

---

## 🔍 Minecraft Gui クラス調査

### 推定されるバニラ描画メソッド
```java
net.minecraft.client.gui.Gui クラス内:
- renderPlayerHealth() : 体力バー描画
- renderFood() : 満腹度バー描画  
- renderExperienceBar() : 経験値バー描画
- renderHotbar() : ホットバー描画
- renderAir() : 酸素バー描画
```

### アクセス方法
```java
Minecraft mc = Minecraft.getInstance();
Gui vanillaGui = mc.gui;

// 位置調整してバニラメソッド呼び出し
// PoseStack操作で描画位置を変更
```

---

## 🛠️ 実装戦略

### 1. PoseStack座標変換方式
```java
public static final IGuiOverlay HEALTH_OVERLAY = (gui, graphics, partialTick, width, height) -> {
    if (!HUDConfig.HEALTH_ENABLED.get()) return;
    
    // カスタム位置計算
    Vector2i offset = HUDConfig.getHealthPosition();
    
    // PoseStack操作で描画位置変更
    graphics.pose().pushPose();
    graphics.pose().translate(offset.x, offset.y, 0);
    
    // バニラ描画メソッド呼び出し（位置調整済み）
    // gui.renderPlayerHealth(graphics, partialTick, width, height);
    
    graphics.pose().popPose();
};
```

### 2. Mixin座標インジェクション方式（代替案）
```java
@Mixin(Gui.class)
public class GuiMixin {
    @ModifyArg(method = "renderPlayerHealth", 
              at = @At(value = "INVOKE", target = "graphics.blit"))
    private int modifyHealthX(int x) {
        if (HUDConfig.HEALTH_ENABLED.get()) {
            return x + HUDConfig.getHealthPosition().x;
        }
        return x;
    }
}
```

---

## 📊 期待効果

### Resource Pack対応
- ✅ **カスタムハートテクスチャ**: 適用される
- ✅ **アニメーション**: 適用される  
- ✅ **色変更・エフェクト**: 適用される

### mod互換性
- ✅ **Tinkers' Construct**: カスタム体力バー適用
- ✅ **JEI**: アイテム情報表示適用
- ✅ **その他UI拡張mod**: 自動適用

### パフォーマンス
- ✅ **軽量**: バニラロジック流用で最適化済み
- ✅ **安定性**: Minecraft本体と同じ動作保証

---

## 🚀 次ステップ

1. **Gui クラスメソッド調査**: 実際のメソッド名・シグネチャ確認
2. **PoseStack方式実装**: 座標変換による位置制御
3. **Resource Pack テスト**: カスタムテクスチャ適用確認
4. **mod互換性テスト**: TFC等のHUD拡張mod連携確認

---

**更新日**: 2025-08-02  
**方針**: 「位置だけ変更・描画はバニラそのまま」でResource Pack完全対応実現 ✨