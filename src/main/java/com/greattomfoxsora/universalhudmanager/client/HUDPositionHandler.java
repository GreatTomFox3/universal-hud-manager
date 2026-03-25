package com.greattomfoxsora.universalhudmanager.client;

import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector2i;
import java.util.Collection;
import java.util.List;

/**
 * HUD位置制御ハンドラー - シンプル版
 * アーマーHUD成功方式をベースに、Health BarとArmor Barのみを制御
 * 
 * @author GreatTomFox & Sora
 */
@Mod.EventBusSubscriber(modid = "universalhudmanager", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HUDPositionHandler {
    
    private static final ResourceLocation ICONS = new ResourceLocation("minecraft", "textures/gui/icons.png");
    private static final ResourceLocation WIDGETS = new ResourceLocation("minecraft", "textures/gui/widgets.png");
    
    // Health Bar 回復アニメーション用変数（バニラ準拠）
    private static int lastHealth = 20;
    private static long lastHealthTime = 0;
    private static long healthBlinkTime = 0;   // バニラ準拠：long型でtickCount管理
    private static int displayHealth = 20;
    private static int tickCount = 0;
    
    // Food Bar アニメーション用（バニラではランダムシードをtickで管理）
    
    // Item Name表示用変数（バニラ準拠）
    private static int toolHighlightTimer = 0;  // バニラのtoolHighlightTimer相当
    private static ItemStack lastToolHighlight = null;  // バニラのlastToolHighlight相当（null = EMPTY扱い）
    private static int lastTickCount = 0;  // タイマー更新制御用
    
    // Dismount Message表示用変数（Item Name同様のフェード機能）
    private static int dismountMessageTimer = 0;  // フェードアウトタイマー
    private static boolean wasRiding = false;     // 前回乗り物に乗っていたかどうか
    
    /**
     * バニラHUDを無効化する
     */
    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        // デバッグモード時にすべてのオーバーレイIDを出力
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("🔍 Overlay ID: " + event.getOverlay().id().toString());
        }
        // Health Barを無効化
        if (HUDConfig.HEALTH_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            event.setCanceled(true);
            
            if (HUDConfig.DEBUG_MODE.get()) {
                System.out.println("🚫 Canceled vanilla Health Bar");
            }
        }
        
        // Armor Barを無効化
        if (HUDConfig.ARMOR_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.ARMOR_LEVEL.id())) {
            event.setCanceled(true);
            
            if (HUDConfig.DEBUG_MODE.get()) {
                System.out.println("🚫 Canceled vanilla Armor Bar");
            }
        }
        
        // Food Barを無効化
        if (HUDConfig.FOOD_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.FOOD_LEVEL.id())) {
            event.setCanceled(true);
            
            if (HUDConfig.DEBUG_MODE.get()) {
                System.out.println("🚫 Canceled vanilla Food Bar");
            }
        }
        
        // Air Barを無効化
        if (HUDConfig.AIR_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.AIR_LEVEL.id())) {
            event.setCanceled(true);
            
            if (HUDConfig.DEBUG_MODE.get()) {
                System.out.println("🚫 Canceled vanilla Air Bar");
            }
        }
        
        // Experience Barを無効化
        if (HUDConfig.EXPERIENCE_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) {
            event.setCanceled(true);
            
            if (HUDConfig.DEBUG_MODE.get()) {
                System.out.println("🚫 Canceled vanilla Experience Bar");
            }
        }
        
        // Hotbarを無効化
        if (HUDConfig.HOTBAR_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            event.setCanceled(true);
            
            if (HUDConfig.DEBUG_MODE.get()) {
                System.out.println("🚫 Canceled vanilla Hotbar");
            }
        }
        
        // Item Name（Selected Item Name）を無効化
        // Forgeでは複数の可能性があるため、複数パターンをチェック
        if (HUDConfig.ITEM_NAME_ENABLED.get()) {
            String overlayId = event.getOverlay().id().toString().toLowerCase();
            if (overlayId.contains("selected_item") || 
                overlayId.contains("item_name") || 
                overlayId.contains("tool_highlight")) {
                event.setCanceled(true);
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("🚫 Canceled vanilla Item Name overlay: " + overlayId);
                }
            }
        }
        
        // Effects（Potion Effects）を無効化
        if (HUDConfig.EFFECTS_ENABLED.get()) {
            String overlayId = event.getOverlay().id().toString().toLowerCase();
            if (overlayId.contains("effect") || 
                overlayId.contains("potion") || 
                overlayId.contains("mob_effect")) {
                event.setCanceled(true);
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("🚫 Canceled vanilla Effects overlay: " + overlayId);
                }
            }
        }
        
        // Vehicle Healthを無効化
        if (HUDConfig.VEHICLE_HEALTH_ENABLED.get()) {
            String overlayId = event.getOverlay().id().toString().toLowerCase();
            if (overlayId.contains("vehicle") || 
                overlayId.contains("mount")) {
                event.setCanceled(true);
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("🚫 Canceled vanilla Vehicle Health overlay: " + overlayId);
                }
            }
        }
        
        // Jump Meterを無効化
        if (HUDConfig.JUMP_METER_ENABLED.get()) {
            String overlayId = event.getOverlay().id().toString().toLowerCase();
            if (overlayId.contains("jump")) {
                event.setCanceled(true);
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("🚫 Canceled vanilla Jump Meter overlay: " + overlayId);
                }
            }
        }
        
        // Dismount Messageを無効化
        if (HUDConfig.DISMOUNT_MESSAGE_ENABLED.get()) {
            String overlayId = event.getOverlay().id().toString().toLowerCase();
            if (overlayId.contains("dismount") || 
                overlayId.contains("sneak") ||
                overlayId.contains("vehicle_text") ||
                overlayId.contains("mount_text")) {
                event.setCanceled(true);
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("🚫 Canceled vanilla Dismount Message overlay: " + overlayId);
                }
            }
        }
        
        // Boss Barを無効化
        if (HUDConfig.BOSS_BAR_ENABLED.get() && 
            event.getOverlay().id().equals(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id())) {
            event.setCanceled(true);
            
            if (HUDConfig.DEBUG_MODE.get()) {
                System.out.println("🚫 Canceled vanilla Boss Bar");
            }
        }
        
        // NOTE: CROSSHAIRオーバーレイは無効化しない（カスタムHUD描画が止まるため）
        // 代わりに、Post処理でAttack Indicatorを上書き描画する方式を使用
    }
    
    /**
     * IGuiOverlay から呼ばれる全HUD描画エントリポイント
     * CROSSHAIR イベントに依存しないため、TACZなど他modがクロスヘアをキャンセルしても動作する
     */
    public static void renderAll(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();

        // HUD編集モード中は描画しない（編集画面で処理）
        if (minecraft.screen instanceof HudEditScreen) {
            return;
        }

        // プレイヤーがいない場合は描画しない
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        // Health Bar描画
        if (HUDConfig.HEALTH_ENABLED.get()) {
            renderHealthBar(guiGraphics, player, screenWidth, screenHeight);
            // AppleSkin 互換：健康回復予測オーバーレイ
            Vector2i healthDefault = HUDConfig.getDefaultHealthPosition(screenWidth, screenHeight);
            Vector2i healthOff = HUDConfig.getHealthPosition();
            int healthLeft = healthDefault.x + healthOff.x;
            int healthTop = healthDefault.y + healthOff.y;
            com.greattomfoxsora.universalhudmanager.client.appleskin.FoodOverlayRenderer
                .renderHealthOverlay(guiGraphics, healthLeft, healthTop);
        }

        // Armor Bar描画
        if (HUDConfig.ARMOR_ENABLED.get()) {
            renderArmorBar(guiGraphics, player, screenWidth, screenHeight);
        }

        // Food Bar描画
        if (HUDConfig.FOOD_ENABLED.get()) {
            // 乗り物に乗っていてVehicle Healthが非分離モードの時はFood overlayを描画しない
            boolean isShowingVehicleHealth = HUDConfig.VEHICLE_HEALTH_ENABLED.get()
                && !HUDConfig.SEPARATE_VEHICLE_HEALTH.get()
                && getPlayerVehicleWithHealth(player) != null
                && getVehicleMaxHearts(getPlayerVehicleWithHealth(player)) > 0;
            Vector2i foodDefault = HUDConfig.getDefaultFoodPosition(screenWidth, screenHeight);
            Vector2i foodOff = HUDConfig.getFoodPosition();
            int foodRight = foodDefault.x + foodOff.x + 81;
            int foodTop = foodDefault.y + foodOff.y;

            // 疲労度アンダーレイ：フードバーより先に描画（背景レイヤー）
            if (!isShowingVehicleHealth && HUDConfig.APPLESKIN_EXHAUSTION.get()) {
                com.greattomfoxsora.universalhudmanager.client.appleskin.FoodOverlayRenderer
                    .drawExhaustionUnderlay(guiGraphics, foodRight, foodTop);
            }

            // フードバー本体
            renderFoodBar(guiGraphics, player, screenWidth, screenHeight);

            // 飽和度・満腹度予測：フードバーの上のレイヤー
            if (!isShowingVehicleHealth) {
                com.greattomfoxsora.universalhudmanager.client.appleskin.FoodOverlayRenderer
                    .renderFoodOverlayPost(guiGraphics, foodRight, foodTop);
            }
        }

        // Air Bar描画
        if (HUDConfig.AIR_ENABLED.get()) {
            renderAirBar(guiGraphics, player, screenWidth, screenHeight);
        }

        // Experience Bar描画
        if (HUDConfig.EXPERIENCE_ENABLED.get()) {
            renderExperienceBar(guiGraphics, player, screenWidth, screenHeight);
        }

        // Hotbar描画
        if (HUDConfig.HOTBAR_ENABLED.get()) {
            renderHotbar(guiGraphics, player, screenWidth, screenHeight);
        }

        // Item Name描画
        if (HUDConfig.ITEM_NAME_ENABLED.get()) {
            renderItemName(guiGraphics, player, screenWidth, screenHeight);
        }

        // Effects描画
        if (HUDConfig.EFFECTS_ENABLED.get()) {
            renderEffects(guiGraphics, player, screenWidth, screenHeight);
        }

        // Vehicle Health描画
        if (HUDConfig.VEHICLE_HEALTH_ENABLED.get()) {
            renderVehicleHealth(guiGraphics, player, screenWidth, screenHeight);
        }

        // Jump Meter描画
        if (HUDConfig.JUMP_METER_ENABLED.get()) {
            renderJumpMeter(guiGraphics, player, screenWidth, screenHeight);
        }

        // Dismount Message描画
        if (HUDConfig.DISMOUNT_MESSAGE_ENABLED.get()) {
            renderDismountMessage(guiGraphics, player, screenWidth, screenHeight);
        }

        // Boss Bar描画
        if (HUDConfig.BOSS_BAR_ENABLED.get()) {
            renderBossBar(guiGraphics, player, screenWidth, screenHeight);
        }

        // Attack Indicator描画
        if (HUDConfig.ATTACK_INDICATOR_ENABLED.get()) {
            renderAttackIndicator(guiGraphics, player, screenWidth, screenHeight);
        }
    }

    /**
     * @deprecated IGuiOverlay（UHMHudOverlay）に移行済み。このメソッドは使用されない。
     */
    @SubscribeEvent
    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        // 描画は UHMHudOverlay.render() で行う（CROSSHAIR依存を排除）
    }
    
    /**
     * Health Barを描画（エフェクト対応・バニラ準拠・Absorption対応）
     */
    private static void renderHealthBar(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        // デフォルト位置 + オフセットを計算
        Vector2i defaultPos = HUDConfig.getDefaultHealthPosition(screenWidth, screenHeight);
        Vector2i offset = HUDConfig.getHealthPosition();
        int x = defaultPos.x + offset.x;
        int y = defaultPos.y + offset.y;
        
        // Get player health data
        int health = (int) Math.ceil(player.getHealth());
        int maxHealth = (int) Math.ceil(player.getMaxHealth());
        int absorption = (int) Math.ceil(player.getAbsorptionAmount());
        
        // バニラ準拠の回復検知とアニメーションタイマー設定
        Minecraft minecraft = Minecraft.getInstance();
        tickCount = minecraft.gui.getGuiTicks();
        long currentTime = System.currentTimeMillis();
        
        // 体力変化を検知（バニラGUI.java 727-733行目準拠）
        if (health < lastHealth && player.invulnerableTime > 0) {
            // ダメージを受けた
            lastHealthTime = currentTime;
            healthBlinkTime = (long)(tickCount + 20);  // 1秒間の点滅
        } else if (health > lastHealth && player.invulnerableTime > 0) {
            // 回復した時！
            lastHealthTime = currentTime;
            healthBlinkTime = (long)(tickCount + 10);  // 0.5秒間の点滅
        }
        
        // 1秒経過したらdisplayHealthを更新（バニラ735-739行目準拠）
        if (currentTime - lastHealthTime > 1000L) {
            lastHealth = health;
            displayHealth = health;
            lastHealthTime = currentTime;
        }
        
        lastHealth = health;
        
        // Check for status effects that change heart appearance
        boolean hasPoison = player.hasEffect(MobEffects.POISON);
        boolean hasWither = player.hasEffect(MobEffects.WITHER);
        boolean isFrozen = player.isFullyFrozen();  // 粉雪で凍った状態
        
        // バニラ準拠の再生エフェクトアニメーション（GUI.java 757-759行目）
        int regenerationHeartIndex = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            float totalHearts = Math.max(maxHealth, Math.max(displayHealth, health)) + absorption;
            regenerationHeartIndex = tickCount % (int)Math.ceil(totalHearts + 5.0F);
        }
        
        // Calculate rows for hearts
        int rows = (int) Math.ceil((maxHealth + absorption) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (rows - 2), 3);
        
        // バニラ準拠のランダムシード設定（GUI.java 743行目）
        RandomSource random = RandomSource.create((long)(tickCount * 312871));
        
        // ハードコアモード判定（GUI.java 838行目準拠）
        boolean isHardcore = player.level().getLevelData().isHardcore();
        int hardcoreOffset = isHardcore ? 45 : 0;  // ハードコア時は9*5=45ピクセル下のテクスチャ
        
        // Render all hearts
        for (int i = 0; i < (int) Math.ceil((maxHealth + absorption) / 2.0F); ++i) {
            int row = i / 10;
            int col = i % 10;
            int heartX = x + col * 8;
            int heartY = y - row * rowHeight;
            
            // バニラ準拠の点滅判定（GUI.java 725行目準拠）
            boolean shouldBlink = healthBlinkTime > (long)tickCount && 
                                 (healthBlinkTime - (long)tickCount) / 3L % 2L == 1L;
            
            // バニラ準拠の極低体力シェイク演出（GUI.java 848-850行目）
            if (health + absorption <= 4) {
                heartY += random.nextInt(2);  // 各ハートごとに0-1ピクセルランダム振動
            }
            
            // バニラ準拠の再生エフェクトアニメーション（GUI.java 852-854行目）
            if (i < (int)Math.ceil((maxHealth + absorption) / 2.0F) && i == regenerationHeartIndex) {
                heartY -= 2;  // バニラ準拠：2ピクセル上に移動
            }
            
            // [Layer 1] 背景コンテナ（点滅中は白コンテナ U=25、通常は U=16）
            int containerU = shouldBlink ? 25 : 16;
            guiGraphics.blit(ICONS, heartX, heartY, containerU, hardcoreOffset, 9, 9);

            // [Layer 2] 吸収ハート（Absorption）- canBlink=false なので白点滅なし
            if (i >= maxHealth / 2) {
                int absIndex = i - maxHealth / 2;
                if (absIndex * 2 + 1 < absorption) {
                    if (hasWither) {
                        guiGraphics.blit(ICONS, heartX, heartY, 142, hardcoreOffset, 9, 9); // Withered full absorption
                    } else {
                        guiGraphics.blit(ICONS, heartX, heartY, 160, hardcoreOffset, 9, 9); // Normal full absorption (yellow)
                    }
                } else if (absIndex * 2 + 1 == absorption) {
                    if (hasWither) {
                        guiGraphics.blit(ICONS, heartX, heartY, 151, hardcoreOffset, 9, 9); // Withered half absorption
                    } else {
                        guiGraphics.blit(ICONS, heartX, heartY, 169, hardcoreOffset, 9, 9); // Normal half absorption (yellow)
                    }
                }
            }

            // [Layer 3] 旧HP（displayHealth）範囲を白点滅テクスチャで描画
            // バニラ準拠: 失ったハートの位置が白く光る演出
            if (shouldBlink && i < maxHealth / 2 && i * 2 < displayHealth) {
                boolean halfHeart = i * 2 + 1 == displayHealth;
                if (hasWither) {
                    int u = halfHeart ? 151 : 142;
                    guiGraphics.blit(ICONS, heartX, heartY, u, hardcoreOffset, 9, 9); // Withered blink
                } else if (hasPoison) {
                    int u = halfHeart ? 115 : 106;
                    guiGraphics.blit(ICONS, heartX, heartY, u, hardcoreOffset, 9, 9); // Poison blink
                } else if (!isFrozen) { // Frozen は canBlink=false
                    int u = halfHeart ? 79 : 70;
                    guiGraphics.blit(ICONS, heartX, heartY, u, hardcoreOffset, 9, 9); // Normal blink
                }
            }

            // [Layer 4] 現在HP（health）を通常テクスチャで上書き描画
            // Layer 3 の白点滅を上書きするので、残ってるハートは普通の色になる
            if (i < maxHealth / 2) {
                if (i * 2 + 1 < health) {
                    if (hasWither) {
                        guiGraphics.blit(ICONS, heartX, heartY, 124, hardcoreOffset, 9, 9); // Withered full
                    } else if (hasPoison) {
                        guiGraphics.blit(ICONS, heartX, heartY, 88, hardcoreOffset, 9, 9);  // Poison full
                    } else if (isFrozen) {
                        guiGraphics.blit(ICONS, heartX, heartY, 178, hardcoreOffset, 9, 9); // Frozen full
                    } else {
                        guiGraphics.blit(ICONS, heartX, heartY, 52, hardcoreOffset, 9, 9);  // Normal full
                    }
                } else if (i * 2 + 1 == health) {
                    if (hasWither) {
                        guiGraphics.blit(ICONS, heartX, heartY, 133, hardcoreOffset, 9, 9); // Withered half
                    } else if (hasPoison) {
                        guiGraphics.blit(ICONS, heartX, heartY, 97, hardcoreOffset, 9, 9);  // Poison half
                    } else if (isFrozen) {
                        guiGraphics.blit(ICONS, heartX, heartY, 187, hardcoreOffset, 9, 9); // Frozen half
                    } else {
                        guiGraphics.blit(ICONS, heartX, heartY, 61, hardcoreOffset, 9, 9);  // Normal half
                    }
                }
            }
        }
        
        if (HUDConfig.DEBUG_MODE.get()) {
            boolean isAnimating = healthBlinkTime > (long)tickCount;
            long ticksLeft = Math.max(0, healthBlinkTime - tickCount);
            System.out.println("✅ Rendered Health Bar at (" + x + ", " + y + ") - health: " + health + "/" + maxHealth + 
                             ", display: " + displayHealth + ", effects: poison=" + hasPoison + ", wither=" + hasWither + 
                             ", frozen=" + isFrozen + ", absorption=" + absorption + ", hardcore=" + isHardcore +
                             ", regen_index=" + regenerationHeartIndex + ", animating: " + isAnimating + ", ticks_left: " + ticksLeft);
        }
    }
    
    /**
     * Armor Barを描画（バニラライク・空ゲージ対応）
     */
    private static void renderArmorBar(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        // デフォルト位置 + オフセットを計算
        Vector2i defaultPos = HUDConfig.getDefaultArmorPosition(screenWidth, screenHeight);
        Vector2i offset = HUDConfig.getArmorPosition();
        int x = defaultPos.x + offset.x;
        int y = defaultPos.y + offset.y;
        
        // アーマー値を取得
        int armor = player.getArmorValue();
        
        // アーマーが0の場合は表示しない（バニラの動作）
        if (armor <= 0) {
            return;
        }
        
        // アーマーアイコンを描画（最大10個）
        for (int i = 0; i < 10; i++) {
            int armorX = x + i * 8;
            
            // 空ゲージ表示が有効な場合、背景を描画
            if (HUDConfig.ARMOR_EMPTY_DISPLAY.get()) {
                guiGraphics.blit(ICONS,
                    armorX, y,      // 描画位置
                    16, 9,          // テクスチャ位置（空のアーマー）
                    9, 9,           // サイズ
                    256, 256);      // テクスチャ全体サイズ
            }
            
            // 実際のアーマー値を描画
            if (i * 2 < armor) {
                // アーマーアイコンの種類を決定
                int armorValue = Math.min(2, armor - i * 2);
                if (armorValue == 2) {
                    // フルアーマー
                    guiGraphics.blit(ICONS,
                        armorX, y,      // 描画位置
                        34, 9,          // テクスチャ位置（フルアーマー）
                        9, 9,           // サイズ
                        256, 256);      // テクスチャ全体サイズ
                } else if (armorValue == 1) {
                    // ハーフアーマー
                    guiGraphics.blit(ICONS,
                        armorX, y,      // 描画位置
                        25, 9,          // テクスチャ位置（ハーフアーマー）
                        9, 9,           // サイズ
                        256, 256);      // テクスチャ全体サイズ
                }
            }
        }
        
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("✅ Rendered Armor Bar at (" + x + ", " + y + ") - armor: " + armor + ", empty_display: " + HUDConfig.ARMOR_EMPTY_DISPLAY.get());
        }
    }
    
    /**
     * Food Barを描画（バニラ準拠・飢餓効果対応・バニラライク動的切り替え対応）
     */
    private static void renderFoodBar(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        // バニラライク動作: 馬に乗っている場合、Vehicle Healthが有効でかつ非分離モードの場合は馬のHPを表示
        if (HUDConfig.VEHICLE_HEALTH_ENABLED.get() && !HUDConfig.SEPARATE_VEHICLE_HEALTH.get()) {
            LivingEntity vehicle = getPlayerVehicleWithHealth(player);
            if (vehicle != null && getVehicleMaxHearts(vehicle) > 0) {
                // 馬に乗っている場合は馬のHPを表示
                renderVehicleHealthAtPosition(guiGraphics, player, vehicle, screenWidth, screenHeight, 
                                             HUDConfig.getDefaultFoodPosition(screenWidth, screenHeight), 
                                             HUDConfig.getFoodPosition());
                return;
            }
        }
        // デフォルト位置 + オフセットを計算
        Vector2i defaultPos = HUDConfig.getDefaultFoodPosition(screenWidth, screenHeight);
        Vector2i offset = HUDConfig.getFoodPosition();
        int x = defaultPos.x + offset.x;
        int y = defaultPos.y + offset.y;
        
        // Food data取得
        int foodLevel = player.getFoodData().getFoodLevel();
        float saturation = player.getFoodData().getSaturationLevel();
        
        // エフェクト状態を判定
        boolean hasHunger = player.hasEffect(MobEffects.HUNGER);  // 空腹エフェクト（腐った肉等）
        
        // バニラ準拠のアニメーション計算
        Minecraft minecraft = Minecraft.getInstance();
        int tickCount = minecraft.gui.getGuiTicks();
        RandomSource random = RandomSource.create(tickCount);
        
        // Food アイコンを描画（10個、設定に応じて左右どちらからでも）
        for (int i = 0; i < 10; i++) {
            // 配置方向をconfigで制御
            int foodX;
            if (HUDConfig.FOOD_DECREASE_LEFT_TO_RIGHT.get()) {
                // 左から右へ減る（Health Barと同じ）
                foodX = x + i * 8;
            } else {
                // 右から左へ減る（バニラと同じ）
                foodX = x + (9 - i) * 8;
            }
            int foodY = y;
            
            // バニラ準拠のピクピクアニメーション適用
            // 条件：Saturation <= 0 かつ 周期的タイミング
            if (saturation <= 0.0F && tickCount % (foodLevel * 3 + 1) == 0) {
                // バニラと同じ±1ピクセルの控えめな揺れ
                foodY += (random.nextInt(3) - 1);
            }
            
            // 背景（空の食料アイコン）を描画 - Hunger時はU=133（緑アウトライン）、通常はU=16
            int backgroundU = hasHunger ? 133 : 16;
            guiGraphics.blit(ICONS,
                foodX, foodY,        // 描画位置
                backgroundU, 27,    // テクスチャ位置（Hunger時は緑アウトライン）
                9, 9,               // サイズ
                256, 256);          // テクスチャ全体サイズ
            
            // 実際の食料を描画
            if (i * 2 + 1 < foodLevel) {
                // フル食料アイコン - 空腹エフェクト時のみ緑色
                int textureU = hasHunger ? 88 : 52;  // 空腹エフェクト時は緑、通常は茶色
                
                guiGraphics.blit(ICONS,
                    foodX, foodY,        // 描画位置
                    textureU, 27,        // テクスチャ位置（フル食料）
                    9, 9,               // サイズ
                    256, 256);          // テクスチャ全体サイズ
            } else if (i * 2 + 1 == foodLevel) {
                // ハーフ食料アイコン - 空腹エフェクト時のみ緑色
                int textureU = hasHunger ? 97 : 61;  // 空腹エフェクト時は緑ハーフ、通常は茶色ハーフ
                
                guiGraphics.blit(ICONS,
                    foodX, foodY,        // 描画位置
                    textureU, 27,        // テクスチャ位置（ハーフ食料）
                    9, 9,               // サイズ
                    256, 256);          // テクスチャ全体サイズ
            }
        }
        
        if (HUDConfig.DEBUG_MODE.get()) {
            boolean isAnimating = saturation <= 0.0F && tickCount % (foodLevel * 3 + 1) == 0;
            System.out.println("✅ Rendered Food Bar at (" + x + ", " + y + ") - food: " + foodLevel + ", saturation: " + saturation + ", hunger: " + hasHunger + ", left_to_right: " + HUDConfig.FOOD_DECREASE_LEFT_TO_RIGHT.get() + ", shake: " + isAnimating);
        }
    }
    
    /**
     * Air Barを描画（バニラ準拠・水中時バブル対応）
     */
    private static void renderAirBar(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        // デフォルト位置 + オフセットを計算
        Vector2i defaultPos = HUDConfig.getDefaultAirPosition(screenWidth, screenHeight);
        Vector2i offset = HUDConfig.getAirPosition();
        int x = defaultPos.x + offset.x;
        int y = defaultPos.y + offset.y;
        
        // プレイヤーの酸素レベルを取得
        int air = player.getAirSupply();
        int maxAir = player.getMaxAirSupply();
        
        // バニラ準拠: 水中 OR air が maxAir 未満の場合に表示（水から出た後も回復中は表示）
        int clampedAir = Math.min(air, maxAir);
        if (!player.isEyeInFluid(FluidTags.WATER) && clampedAir >= maxAir) {
            return;
        }
        
        // バニラ準拠の計算（GUI.java 820-821行目）
        int fullBubbles = (int) Math.ceil((double)(clampedAir - 2) * 10.0D / (double)maxAir);
        int popBubbles = (int) Math.ceil((double)clampedAir * 10.0D / (double)maxAir) - fullBubbles;
        
        // バブルアイコンを描画（GUI.java 823-829行目準拠）
        for(int i = 0; i < fullBubbles + popBubbles; ++i) {
            int bubbleX = x + (9 - i) * 8;  // 右から左へ配置（j1 - j5 * 8 - 9）
            int bubbleY = y;
            
            if (i < fullBubbles) {
                // フルバブル
                guiGraphics.blit(ICONS,
                    bubbleX, bubbleY,        // 描画位置
                    16, 18,                 // テクスチャ位置（フルバブル）
                    9, 9,                   // サイズ
                    256, 256);              // テクスチャ全体サイズ
            } else {
                // 破裂バブル（ポップしたバブル）
                guiGraphics.blit(ICONS,
                    bubbleX, bubbleY,        // 描画位置
                    25, 18,                 // テクスチャ位置（破裂バブル）
                    9, 9,                   // サイズ
                    256, 256);              // テクスチャ全体サイズ
            }
        }
        
        if (HUDConfig.DEBUG_MODE.get()) {
            boolean isUnderwater = player.isEyeInFluid(FluidTags.WATER);
            System.out.println("✅ Rendered Air Bar at (" + x + ", " + y + ") - air: " + air + "/" + maxAir + 
                             ", fullBubbles: " + fullBubbles + ", popBubbles: " + popBubbles + ", underwater: " + isUnderwater);
        }
    }
    
    /**
     * Experience Barを描画（バニラ準拠・バニラライク動的切り替え対応）
     */
    private static void renderExperienceBar(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        // バニラライク動作: 馬に乗っている場合、Jump Meterが有効でかつ非分離モードの場合はJump Meterを表示
        if (HUDConfig.JUMP_METER_ENABLED.get() && !HUDConfig.SEPARATE_JUMP_METER.get()) {
            var vehicle = player.getVehicle();
            if (vehicle instanceof PlayerRideableJumping) {
                // ジャンプ可能な乗り物に乗っている場合はJump Meterを表示
                renderJumpMeterAtPosition(guiGraphics, player, screenWidth, screenHeight,
                                        HUDConfig.getDefaultExperiencePosition(screenWidth, screenHeight),
                                        HUDConfig.getExperiencePosition());
                return;
            }
        }
        // デフォルト位置 + オフセットを計算
        Vector2i defaultPos = HUDConfig.getDefaultExperiencePosition(screenWidth, screenHeight);
        Vector2i offset = HUDConfig.getExperiencePosition();
        int x = defaultPos.x + offset.x;
        int y = defaultPos.y + offset.y;
        
        // 経験値データを取得
        int xpNeeded = player.getXpNeededForNextLevel();
        
        // 経験値が必要な場合のみバーを描画（バニラ準拠：GUI.java 551行目）
        if (xpNeeded > 0) {
            float progress = player.experienceProgress;
            int fillWidth = (int)(progress * 183.0F);  // バニラ準拠の計算
            
            // 背景バー（空のバー）を描画
            guiGraphics.blit(ICONS,
                x, y,           // 描画位置
                0, 64,          // テクスチャ位置（空の経験値バー）
                182, 5,         // サイズ
                256, 256);      // テクスチャ全体サイズ
            
            // 緑の経験値バー（進捗分）
            if (fillWidth > 0) {
                guiGraphics.blit(ICONS,
                    x, y,           // 描画位置
                    0, 69,          // テクスチャ位置（緑の経験値バー）
                    fillWidth, 5,   // 進捗分の幅
                    256, 256);      // テクスチャ全体サイズ
            }
        }
        
        // レベル数字を描画（レベル1以上の場合のみ）
        if (player.experienceLevel > 0) {
            String levelText = "" + player.experienceLevel;
            int textWidth = Minecraft.getInstance().font.width(levelText);
            int textX = x + 91 - textWidth / 2;  // バーの中央
            int textY = y - 6;  // バーの6px上（バニラ: screenHeight - 31 - 4）
            
            // 黒い影（上下左右に描画）- バニラ準拠（GUI.java 567-570行目）
            guiGraphics.drawString(Minecraft.getInstance().font, levelText, textX + 1, textY, 0, false);
            guiGraphics.drawString(Minecraft.getInstance().font, levelText, textX - 1, textY, 0, false);
            guiGraphics.drawString(Minecraft.getInstance().font, levelText, textX, textY + 1, 0, false);
            guiGraphics.drawString(Minecraft.getInstance().font, levelText, textX, textY - 1, 0, false);
            
            // 緑色の数字本体（バニラ: 8453920 = 0x80FF20）
            guiGraphics.drawString(Minecraft.getInstance().font, levelText, textX, textY, 8453920, false);
        }
        
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("✅ Rendered Experience Bar at (" + x + ", " + y + ") - level: " + player.experienceLevel + 
                             ", progress: " + player.experienceProgress + ", xpNeeded: " + xpNeeded);
        }
    }
    
    /**
     * Hotbarを描画（バニラ準拠）
     */
    private static void renderHotbar(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        // デフォルト位置 + オフセットを計算
        Vector2i defaultPos = HUDConfig.getDefaultHotbarPosition(screenWidth, screenHeight);
        Vector2i offset = HUDConfig.getHotbarPosition();
        int x = defaultPos.x + offset.x;
        int y = defaultPos.y + offset.y;
        
        // バニラ準拠のHotbar描画（智と他の子の分析に基づく）
        RenderSystem.enableBlend();        // ブレンディングを有効化
        RenderSystem.defaultBlendFunc();   // デフォルトのブレンド関数を使用
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);  // Z座標を-90に設定
        
        // メインのホットバー背景を描画
        guiGraphics.blit(WIDGETS, x, y, 0, 0, 182, 22);
        
        // 選択枠を描画
        int selectedSlot = player.getInventory().selected;
        guiGraphics.blit(WIDGETS, x - 1 + selectedSlot * 20, y - 1, 0, 22, 24, 22);
        
        // オフハンドアイテムがある場合
        ItemStack offhandItem = player.getOffhandItem();
        if (!offhandItem.isEmpty()) {
            HumanoidArm mainArm = player.getMainArm();
            HumanoidArm offArm = mainArm.getOpposite();
            
            if (offArm == HumanoidArm.LEFT) {
                // 左手にオフハンド
                guiGraphics.blit(WIDGETS, x - 29, y - 1, 24, 22, 29, 24);
            } else {
                // 右手にオフハンド
                guiGraphics.blit(WIDGETS, x + 182, y - 1, 53, 22, 29, 24);
            }
        }
        
        guiGraphics.pose().popPose();
        
        // アイテムを各スロットに描画
        int seed = 1;
        for (int i = 0; i < 9; i++) {
            int slotX = x + 3 + i * 20;
            int slotY = y + 3;
            ItemStack itemStack = player.getInventory().items.get(i);
            
            if (!itemStack.isEmpty()) {
                // アイテムを描画（バニラ準拠）
                renderSlot(guiGraphics, slotX, slotY, player, itemStack, seed++);
            }
        }
        
        // オフハンドアイテムを描画（バニラ完全準拠・screenCenterX基準）
        if (!offhandItem.isEmpty()) {
            HumanoidArm mainArm = player.getMainArm();
            HumanoidArm offArm = mainArm.getOpposite();
            int screenCenterX = screenWidth / 2;  // バニラと同じi
            int offhandX, offhandY = y + 3;
            
            // バニラGUI.java 503-510行目と完全同一の計算
            if (offArm == HumanoidArm.LEFT) {
                // バニラ: this.renderSlot(p_282108_, i - 91 - 26, i2, ...)
                offhandX = screenCenterX - 91 - 26;
            } else {
                // バニラ: this.renderSlot(p_282108_, i + 91 + 10, i2, ...)
                offhandX = screenCenterX + 91 + 10;
            }
            
            renderSlot(guiGraphics, offhandX, offhandY, player, offhandItem, seed++);
        }
        
        // ブレンディングを無効化
        RenderSystem.disableBlend();
        
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("✅ Rendered Hotbar at (" + x + ", " + y + ") - selected: " + selectedSlot + 
                             ", offhand: " + !offhandItem.isEmpty());
        }
    }
    
    /**
     * スロット内のアイテムを描画（バニラ準拠・アニメーション対応）
     */
    private static void renderSlot(GuiGraphics guiGraphics, int x, int y, Player player, ItemStack itemStack, int seed) {
        if (!itemStack.isEmpty()) {
            // デバッグ: offhandアイテムの場合のみ座標出力
            if (HUDConfig.DEBUG_MODE.get() && !player.getInventory().items.contains(itemStack)) {
                System.out.println("🔍 OFFHAND renderSlot: x=" + x + ", y=" + y + ", item=" + itemStack.getDisplayName().getString());
            }
            
            // バニラ準拠のポップアニメーション処理（スワップ時のスケーリング効果）
            float partialTick = Minecraft.getInstance().getFrameTime();
            float popTime = (float)itemStack.getPopTime() - partialTick;
            if (popTime > 0.0F) {
                // スケーリング計算（バニラと同じ）
                float scale = 1.0F + popTime / 5.0F;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate((float)(x + 8), (float)(y + 12), 0.0F);  // アイテム中央基準
                guiGraphics.pose().scale(1.0F / scale, (scale + 1.0F) / 2.0F, 1.0F);  // バニラと同じスケール
                guiGraphics.pose().translate((float)(-(x + 8)), (float)(-(y + 12)), 0.0F);  // 座標復帰
            }
            
            // アイテムアイコンを描画
            guiGraphics.renderItem(player, itemStack, x, y, seed);
            
            if (popTime > 0.0F) {
                guiGraphics.pose().popPose();
            }
            
            // アイテムの個数やダメージバーを描画
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, itemStack, x, y);
        }
    }
    
    /**
     * Item Nameを描画（バニラ準拠・renderSelectedItemName実装）
     */
    private static void renderItemName(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        // デフォルト位置 + オフセットを計算
        Vector2i defaultPos = HUDConfig.getDefaultItemNamePosition(screenWidth, screenHeight);
        Vector2i offset = HUDConfig.getItemNamePosition();
        int x = defaultPos.x + offset.x;
        int y = defaultPos.y + offset.y;
        
        // バニラ準拠のアイテム変更検知・タイマー管理（GUI.java 1050-1062行目準拠）
        Minecraft minecraft = Minecraft.getInstance();
        int currentTick = minecraft.gui.getGuiTicks();
        
        // 現在選択されているアイテムを取得
        ItemStack selectedItem = player.getInventory().getSelected();
        
        // tickが進んだ場合のみタイマー処理を実行（バニラのtick()メソッド再現）
        if (currentTick != lastTickCount) {
            lastTickCount = currentTick;
            
            // バニラのtick()メソッドロジックを再現
            if (selectedItem.isEmpty()) {
                toolHighlightTimer = 0;  // アイテムが空の場合、タイマーをリセット
            } else if (lastToolHighlight != null && !lastToolHighlight.isEmpty() &&
                       selectedItem.getItem() == lastToolHighlight.getItem() &&
                       selectedItem.getHoverName().equals(lastToolHighlight.getHoverName())) {
                // 同じアイテムの場合、タイマーをカウントダウン
                if (toolHighlightTimer > 0) {
                    toolHighlightTimer--;
                }
            } else {
                // 違うアイテムの場合、タイマーをリセット（バニラ準拠: 40tick × 設定値）
                toolHighlightTimer = (int)(40.0D * minecraft.options.notificationDisplayTime().get());
            }
            
            // 前回のアイテムを更新
            lastToolHighlight = selectedItem.copy();
        }
        
        // タイマーが0以下の場合は表示しない
        if (toolHighlightTimer <= 0 || selectedItem.isEmpty()) {
            return;
        }
        
        // アルファ値計算（バニラのfade計算準拠）
        float alpha = (float)toolHighlightTimer * 256.0F / 10.0F;
        if (alpha > 255) {
            alpha = 255;
        }
        alpha = alpha / 255.0F;  // 0.0F-1.0F範囲に正規化
        
        if (alpha <= 0) {
            return;
        }
        
        // アイテム名を取得
        Component itemName = selectedItem.getHoverName();
        String nameText = itemName.getString();
        
        // テキストの幅を計算して中央配置（プレイ画面用補正）
        int textWidth = minecraft.font.width(nameText);
        int textX = x + 33 - textWidth / 2;  // HudElementの中央補正
        int textY = y;
        
        // アルファ値を適用した色（バニラ準拠: 16777215 + (alpha << 24)）
        int alphaInt = (int)(255 * alpha);
        int color = 16777215 + (alphaInt << 24);  // バニラと同じ計算
        
        // バニラ準拠の影付きテキスト描画
        guiGraphics.drawString(minecraft.font, nameText, textX, textY, color, true);
        
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("✅ Rendered Item Name at (" + textX + ", " + textY + ") - item: " + nameText + 
                             ", timer: " + toolHighlightTimer + ", alpha: " + alpha);
        }
    }
    
    /**
     * Effects（ステータス効果）を描画（バニラ準拠・renderEffects実装）
     */
    private static void renderEffects(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        // プレイヤーの現在のエフェクトを取得
        Collection<MobEffectInstance> effects = player.getActiveEffects();
        if (effects.isEmpty()) {
            return;
        }
        
        // デフォルト位置 + オフセットを計算
        Vector2i defaultPos = HUDConfig.getDefaultEffectsPosition(screenWidth, screenHeight);
        Vector2i offset = HUDConfig.getEffectsPosition();
        int baseX = defaultPos.x + offset.x;
        int baseY = defaultPos.y + offset.y;
        
        Minecraft minecraft = Minecraft.getInstance();
        
        // デモモード時のY位置調整
        if (minecraft.isDemo()) {
            baseY += 15;
        }
        
        // ブレンド有効化（バニラ準拠）
        RenderSystem.enableBlend();
        
        int beneficialCount = 0;  // 有益エフェクトカウント
        int harmfulCount = 0;     // 有害エフェクトカウント
        
        // エフェクトテクスチャマネージャー取得
        MobEffectTextureManager textureManager = minecraft.getMobEffectTextures();
        List<Runnable> renderQueue = Lists.newArrayListWithExpectedSize(effects.size());
        
        // エフェクトをソートして処理（バニラ準拠）
        for (MobEffectInstance effectInstance : Ordering.natural().reverse().sortedCopy(effects)) {
            var effect = effectInstance.getEffect();
            
            // Forge互換性チェック
            var renderer = net.minecraftforge.client.extensions.common.IClientMobEffectExtensions.of(effectInstance);
            if (!renderer.isVisibleInGui(effectInstance)) continue;
            
            // アイコンを表示するエフェクトのみ処理
            if (!effectInstance.showIcon()) continue;
            
            // 位置計算（バニラ準拠）
            int x = baseX;
            int y = baseY;
            
            if (effect.isBeneficial()) {
                // 有益エフェクト（上段）
                x -= 25 * beneficialCount;  // 右から左へ配置
                beneficialCount++;
            } else {
                // 有害エフェクト（下段）
                x -= 25 * harmfulCount;     // 右から左へ配置
                y += 26;
                harmfulCount++;
            }
            
            // フェード計算（残り10秒でパルス効果）
            float alpha = 1.0F;
            if (effectInstance.endsWithin(200)) {
                int duration = effectInstance.getDuration();
                int l = 10 - duration / 20;
                alpha = net.minecraft.util.Mth.clamp((float)duration / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + 
                       net.minecraft.util.Mth.cos((float)duration * (float)Math.PI / 5.0F) * 
                       net.minecraft.util.Mth.clamp((float)l / 10.0F * 0.25F, 0.0F, 0.25F);
            }
            
            // 背景描画（環境エフェクトか通常か）
            if (effectInstance.isAmbient()) {
                // 環境エフェクト（ビーコン等）の背景
                guiGraphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, x, y, 165, 166, 24, 24);
            } else {
                // 通常エフェクトの背景
                guiGraphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, x, y, 141, 166, 24, 24);
            }
            
            // Forgeカスタムレンダリングチェック
            if (!renderer.renderGuiIcon(effectInstance, minecraft.gui, guiGraphics, x, y, 0, alpha)) {
                // バニラのアイコン描画
                TextureAtlasSprite sprite = textureManager.get(effect);
                int finalX = x;
                int finalY = y;
                float finalAlpha = alpha;
                
                renderQueue.add(() -> {
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, finalAlpha);
                    guiGraphics.blit(finalX + 3, finalY + 3, 0, 18, 18, sprite);
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                });
            }
        }
        
        // キューに溜めたアイコンを描画
        renderQueue.forEach(Runnable::run);
        
        // ブレンド無効化
        RenderSystem.disableBlend();
        
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("✅ Rendered Effects at base (" + baseX + ", " + baseY + ") - " +
                             "beneficial: " + beneficialCount + ", harmful: " + harmfulCount);
        }
    }
    
    /**
     * Vehicle Health（馬のHP）を描画（バニラ準拠・分離モード専用）
     */
    private static void renderVehicleHealth(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        // 分離モードでない場合は何もしない（Food Barで処理される）
        if (!HUDConfig.SEPARATE_VEHICLE_HEALTH.get()) {
            return;
        }
        
        LivingEntity vehicle = getPlayerVehicleWithHealth(player);
        if (vehicle == null) {
            return;
        }
        
        int maxHearts = getVehicleMaxHearts(vehicle);
        if (maxHearts == 0) {
            return;
        }
        
        // デフォルト位置 + オフセットを計算（分離モード用の位置）
        Vector2i defaultPos = HUDConfig.getDefaultVehicleHealthPosition(screenWidth, screenHeight);
        Vector2i offset = HUDConfig.getVehicleHealthPosition();
        
        renderVehicleHealthAtPosition(guiGraphics, player, vehicle, screenWidth, screenHeight, defaultPos, offset);
    }
    
    /**
     * Jump Meter（ジャンプゲージ）を描画（バニラ準拠・分離モード専用）
     */
    private static void renderJumpMeter(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        // 分離モードでない場合は何もしない（Experience Barで処理される）
        if (!HUDConfig.SEPARATE_JUMP_METER.get()) {
            return;
        }
        
        // 乗り物がジャンプ可能かどうかをチェック
        var vehicle = player.getVehicle();
        if (!(vehicle instanceof PlayerRideableJumping)) {
            return;
        }
        
        // デフォルト位置 + オフセットを計算（分離モード用の位置）
        Vector2i defaultPos = HUDConfig.getDefaultJumpMeterPosition(screenWidth, screenHeight);
        Vector2i offset = HUDConfig.getJumpMeterPosition();
        
        renderJumpMeterAtPosition(guiGraphics, player, screenWidth, screenHeight, defaultPos, offset);
    }
    
    /**
     * プレイヤーが乗っているLivingEntityを取得（HP表示対象）
     */
    private static LivingEntity getPlayerVehicleWithHealth(Player player) {
        if (player != null) {
            var vehicle = player.getVehicle();
            if (vehicle instanceof LivingEntity livingVehicle) {
                return livingVehicle;
            }
        }
        return null;
    }
    
    /**
     * Vehicle Healthを指定位置に描画（共通処理）
     */
    private static void renderVehicleHealthAtPosition(GuiGraphics guiGraphics, Player player, LivingEntity vehicle, 
                                                     int screenWidth, int screenHeight, 
                                                     Vector2i defaultPos, Vector2i offset) {
        int maxHearts = getVehicleMaxHearts(vehicle);
        if (maxHearts == 0) {
            return;
        }
        
        int baseX = defaultPos.x + offset.x;
        int baseY = defaultPos.y + offset.y;
        int currentHealth = (int) Math.ceil(vehicle.getHealth());
        
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getProfiler().popPush("mountHealth");
        
        int rowY = baseY;
        int heartsInCurrentRow = 0;
        
        // バニラ準拠のハート描画処理（10ハートずつ行で表示）
        for (int heartIndex = 0; maxHearts > 0; heartsInCurrentRow += 20) {
            int heartsInThisRow = Math.min(maxHearts, 10);
            maxHearts -= heartsInThisRow;
            
            for (int i = 0; i < heartsInThisRow; i++) {
                // 常に右から左へ配置（Foodバーと同じ方向）
                int heartX = baseX + (9 - i) * 8;  // 右から左へ配置
                
                // 背景ハート（空）を描画
                guiGraphics.blit(ICONS, heartX, rowY, 52, 9, 9, 9);
                
                // 実際のHPを描画
                int heartValue = (i * 2 + 1) + heartsInCurrentRow;
                if (heartValue < currentHealth) {
                    // フルハート
                    guiGraphics.blit(ICONS, heartX, rowY, 88, 9, 9, 9);
                } else if (heartValue == currentHealth) {
                    // ハーフハート
                    guiGraphics.blit(ICONS, heartX, rowY, 97, 9, 9, 9);
                }
            }
            
            rowY -= 10;  // 次の行は10px上に
        }
        
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("✅ Rendered Vehicle Health at (" + baseX + ", " + baseY + ") - hearts: " + 
                             maxHearts + ", health: " + currentHealth);
        }
    }
    
    /**
     * Jump Meterを指定位置に描画（共通処理）
     */
    private static void renderJumpMeterAtPosition(GuiGraphics guiGraphics, Player player, 
                                                 int screenWidth, int screenHeight,
                                                 Vector2i defaultPos, Vector2i offset) {
        // 乗り物がジャンプ可能かどうかをチェック
        var vehicle = player.getVehicle();
        if (!(vehicle instanceof PlayerRideableJumping jumpableVehicle)) {
            return;
        }
        
        int baseX = defaultPos.x + offset.x;
        int baseY = defaultPos.y + offset.y;
        
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getProfiler().push("jumpBar");
        
        // ジャンプスケールを取得（Minecraftインスタンス経由）
        float jumpScale = minecraft.player.getJumpRidingScale();
        int barWidth = 182;
        int fillWidth = (int) (jumpScale * 183.0F);
        
        // 背景バー（空のジャンプゲージ）を描画
        guiGraphics.blit(ICONS, baseX, baseY, 0, 84, barWidth, 5);
        
        if (jumpableVehicle.getJumpCooldown() > 0) {
            // クールダウン中（暗い色）
            guiGraphics.blit(ICONS, baseX, baseY, 0, 74, barWidth, 5);
        } else if (fillWidth > 0) {
            // ジャンプゲージ（緑色）
            guiGraphics.blit(ICONS, baseX, baseY, 0, 89, fillWidth, 5);
        }
        
        minecraft.getProfiler().pop();
        
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("✅ Rendered Jump Meter at (" + baseX + ", " + baseY + ") - scale: " + 
                             jumpScale + ", fillWidth: " + fillWidth + ", cooldown: " + jumpableVehicle.getJumpCooldown());
        }
    }
    
    /**
     * Dismount Message（降車メッセージ）を描画（GitHub Copilot方式・フェードアウト機能付き）
     */
    private static void renderDismountMessage(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        
        // 乗り物に乗っているかチェック
        var vehicle = player.getVehicle();
        if (vehicle != null) {
            if (!wasRiding) {
                // バニラ準拠のDismount Message実装
                dismountMessageTimer = 60;  // バニラと同じ3秒間（20tick/秒 × 3）
                wasRiding = true;
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("🔍 DEBUG: Vehicle mounted, starting dismount message timer = " + dismountMessageTimer);
                }
            }
        } else {
            wasRiding = false;
            // バニラ準拠: 降車時にタイマーリセットしない（自然なフェードアウト維持）
        }
        
        // バニラの表示条件をチェック
        if (minecraft.options.hideGui ||
            minecraft.gameMode.getPlayerMode() == net.minecraft.world.level.GameType.SPECTATOR ||
            dismountMessageTimer <= 0) {
            return;
        }
        
        // バニラ準拠のフェードアウト計算（調整）
        int fadeStartTime = 20; // バニラと同じ1秒フェード（20tick/秒 × 1）
        float alphaFloat = 1.0F;
        
        if (dismountMessageTimer <= fadeStartTime) {
            // 最後の10tickでフェードアウト
            alphaFloat = (float)dismountMessageTimer / (float)fadeStartTime;
        }
        
        int alpha = (int)(alphaFloat * 255.0F);
        if (alpha <= 0) {
            return;
        }
        
        // 降車メッセージを作成（実際のスニークキーバインド取得）
        Component dismountMessage = Component.translatable("mount.onboard", 
            minecraft.options.keyShift.getTranslatedKeyMessage());
        
        // デフォルト位置 + オフセットを計算
        Vector2i defaultPos = HUDConfig.getDefaultDismountMessagePosition(screenWidth, screenHeight);
        Vector2i offset = HUDConfig.getDismountMessagePosition();
        
        // カスタム位置での描画（Item Nameと同じ方式でオフセット対応）
        int centerX = defaultPos.x + offset.x;
        int centerY = defaultPos.y + offset.y;
        
        // バニラ準拠の色計算（白色 + アルファブレンド）
        int color = alpha << 24 | 0xFFFFFF;
        
        // 旧実装の参考情報（現在は使用せず、Mixinで制御）
        String vanillaMessage = "Press Shift to dismount";
        int vanillaTextWidth = minecraft.font.width(vanillaMessage);
        int vanillaCenterX = screenWidth / 2;
        int vanillaY = screenHeight - 68;  // バニラのoverlayMessage実際の位置
        
        // カスタムDismount Messageを描画
        guiGraphics.drawCenteredString(minecraft.font, dismountMessage, centerX, centerY, color);
        
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("✅ Rendered Dismount Message at (" + centerX + ", " + centerY + ") - vehicle: " + vehicle.getClass().getSimpleName() + 
                             ", timer: " + dismountMessageTimer + ", alpha: " + alpha + ", color: " + Integer.toHexString(color));
        }
    }
    
    /**
     * 乗り物の最大ハート数を計算（バニラ準拠）
     */
    private static int getVehicleMaxHearts(LivingEntity vehicle) {
        if (vehicle != null && vehicle.showVehicleHealth()) {
            float maxHealth = vehicle.getMaxHealth();
            int hearts = (int) (maxHealth + 0.5F) / 2;
            return Math.min(hearts, 30);  // 最大5行まで（30ハート）
        }
        return 0;
    }
    
    /**
     * バニラ準拠のタイマー管理 - tick()でタイマーを更新
     */
    /**
     * バニラのVehicle Health（Dismount Message含む）をキャンセル
     */
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        Player player = Minecraft.getInstance().player;
        
        // デバッグ: 乗り物に乗っている時のオーバーレイを確認
        if (player != null && player.getVehicle() != null && event.getOverlay() != null) {
            if (HUDConfig.DEBUG_MODE.get()) {
                System.out.println("🔍 Overlay during mount: " + event.getOverlay().id());
            }
            
            // MOUNT_HEALTHをキャンセル（馬の体力バー）
            if (event.getOverlay().id() != null &&
                event.getOverlay().id().equals(VanillaGuiOverlay.MOUNT_HEALTH.id())) {
                if (HUDConfig.VEHICLE_HEALTH_ENABLED.get()) {
                    event.setCanceled(true);
                }
            }
            
            // HOTBARもキャンセル（dismount messageが含まれる可能性）
            if (event.getOverlay().id() != null &&
                event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
                if (HUDConfig.DISMOUNT_MESSAGE_ENABLED.get()) {
                    // 注意: これはHotbar全体をキャンセルしてしまう可能性がある
                    // 実際のDismount Messageの場所を特定後、より細かい制御が必要
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // Dismount Messageタイマーをバニラ準拠で更新
            if (dismountMessageTimer > 0) {
                dismountMessageTimer--;
                
                if (HUDConfig.DEBUG_MODE.get() && dismountMessageTimer % 10 == 0) {
                    System.out.println("🔄 Tick update: dismountMessageTimer = " + dismountMessageTimer);
                }
            }
        }
    }
    
    /**
     * Boss Bar（ボスHP）を描画（バニラBossHealthOverlay使用）
     */
    private static void renderBossBar(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        
        // バニラのBossHealthOverlayを取得してレンダリング
        if (minecraft.gui != null && minecraft.gui.getBossOverlay() != null) {
            // デフォルト位置 + オフセットを計算
            Vector2i defaultPos = HUDConfig.getDefaultBossBarPosition(screenWidth, screenHeight);
            Vector2i offset = HUDConfig.getBossBarPosition();
            
            // 描画位置を調整
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(offset.x, offset.y, 0.0F);
            
            // バニラのBoss Barを描画
            minecraft.gui.getBossOverlay().render(guiGraphics);
            
            guiGraphics.pose().popPose();
            
            if (HUDConfig.DEBUG_MODE.get()) {
                System.out.println("✅ Rendered Boss Bar with offset (" + offset.x + ", " + offset.y + ")");
            }
        }
    }
    
    /**
     * Attack Indicator（攻撃クールダウンインジケーター）を描画
     * バニラのAttackIndicatorStatus設定に応じてクロスヘア版・ホットバー版・非表示を切り替え
     */
    private static void renderAttackIndicator(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        
        // バニラの攻撃インジケーター設定を取得
        AttackIndicatorStatus indicatorStatus = minecraft.options.attackIndicator().get();
        
        // OFFの場合は描画しない
        if (indicatorStatus == AttackIndicatorStatus.OFF) {
            return;
        }
        
        // 攻撃力を取得
        float attackStrength = player.getAttackStrengthScale(0.0F);
        
        // 攻撃力が1.0F未満の場合のみ描画（バニラ準拠）
        if (attackStrength >= 1.0F) {
            return;
        }
        
        // バニラ設定に応じて適切な設定を使用して最終位置を計算
        Vector2i finalPos;
        
        if (indicatorStatus == AttackIndicatorStatus.CROSSHAIR) {
            // クロスヘア版: 独立設定を使用
            Vector2i offset = HUDConfig.getCrosshairAttackIndicatorPosition();
            Vector2i crosshairDefaultPos = new Vector2i(
                screenWidth / 2 - 8,     // クロスヘア中央-8px
                screenHeight / 2 - 7 + 16  // クロスヘア下16px
            );
            finalPos = HUDConfig.getFinalPosition(crosshairDefaultPos, offset);
        } else if (indicatorStatus == AttackIndicatorStatus.HOTBAR) {
            // ホットバー版: 独立設定を使用
            Vector2i offset = HUDConfig.getHotbarAttackIndicatorPosition();
            HumanoidArm mainArm = player.getMainArm();
            int hotbarCenterX = screenWidth / 2;
            int hotbarY = screenHeight - 20;
            
            if (mainArm == HumanoidArm.RIGHT) {
                // 右利き: ホットバーの右側
                finalPos = new Vector2i(hotbarCenterX + 91 + 6 + offset.x, hotbarY + offset.y);
            } else {
                // 左利き: ホットバーの左側
                finalPos = new Vector2i(hotbarCenterX - 91 - 22 + offset.x, hotbarY + offset.y);
            }
        } else {
            // OFF状態: ここには到達しないはずだが、安全のため
            return; // 早期リターン
        }
        
        // 攻撃インジケーターのプログレスを計算
        int progressWidth = (int)(attackStrength * 17.0F);  // バニラ準拠：最大17px
        
        // 描画開始
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        if (indicatorStatus == AttackIndicatorStatus.CROSSHAIR) {
            // クロスヘア版: バニラと同じ半透明ブレンド設定
            RenderSystem.enableBlend(); // ブレンド有効化
            RenderSystem.blendFuncSeparate(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, 
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, 
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE, 
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO
            );
            
            // バニラと同じ4px高のバー
            guiGraphics.blit(ICONS, finalPos.x, finalPos.y, 36, 94, 16, 4);  // Background
            if (progressWidth > 0) {
                guiGraphics.blit(ICONS, finalPos.x, finalPos.y, 52, 94, progressWidth, 4);  // Progress
            }
            
            // デフォルトブレンド設定に戻す
            RenderSystem.defaultBlendFunc();
        } else if (indicatorStatus == AttackIndicatorStatus.HOTBAR) {
            // ホットバー版: 通常の描画（半透明効果なし）
            guiGraphics.blit(ICONS, finalPos.x, finalPos.y, 0, 94, 18, 18);  // Background icon
            if (progressWidth > 0) {
                int progressHeight = (int)(attackStrength * 18.0F);
                guiGraphics.blit(ICONS, finalPos.x, finalPos.y + 18 - progressHeight, 18, 112 - progressHeight, 18, progressHeight);  // Progress
            }
        }
        
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("✅ Rendered Attack Indicator at (" + finalPos.x + ", " + finalPos.y + ") with " + (attackStrength * 100) + "% charge");
        }
    }
}