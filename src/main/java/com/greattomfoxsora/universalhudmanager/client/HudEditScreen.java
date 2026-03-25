package com.greattomfoxsora.universalhudmanager.client;

import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.client.AttackIndicatorStatus;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2i;

/**
 * シンプルなHUD編集画面 - アーマーHUD方式ベース
 * Health + Armor + Food の3個に対応
 * 
 * @author GreatTomFox & Sora
 */
public class HudEditScreen extends Screen {
    
    private static final ResourceLocation ICONS = new ResourceLocation("minecraft", "textures/gui/icons.png");
    private static final ResourceLocation WIDGETS = new ResourceLocation("minecraft", "textures/gui/widgets.png");
    private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("minecraft", "textures/gui/bars.png");
    
    // 静的な編集モード状態管理
    private static HudEditScreen currentInstance = null;
    private static final java.util.Map<String, Vector2i> temporaryDragOffsets = new java.util.HashMap<>();
    
    // ドラッグ状態管理
    private HudElement selectedElement = null;
    private HudElement draggedElement = null;
    private double dragStartX, dragStartY;
    private Vector2i dragStartOffset;

    // ⚙設定ボタン表示状態（TabキーでトグルOFF→HUD編集に集中できるモード）
    private boolean showConfigButton = true;
    private net.minecraft.client.gui.components.Button configButton;
    private net.minecraft.client.gui.components.Button resetLayoutButton;
    
    // HUD要素定義
    private HudElement healthElement;
    private HudElement armorElement;
    private HudElement foodElement;
    private HudElement airElement;
    private HudElement experienceElement;
    private HudElement hotbarElement;
    private HudElement itemNameElement;
    private HudElement effectsElement;
    private HudElement vehicleHealthElement;
    private HudElement jumpMeterElement;
    private HudElement dismountMessageElement;
    private HudElement bossBarElement;
    private HudElement crosshairAttackIndicatorElement;
    private HudElement hotbarAttackIndicatorElement;
    
    public HudEditScreen() {
        super(Component.translatable("screen.universalhudmanager.hud_edit.title"));
        currentInstance = this;
        temporaryDragOffsets.clear();
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Health HUD要素を初期化 - アーマーHUD方式
        healthElement = new HudElement(
            "health",
            "hud.universalhudmanager.health",
            81, 9,  // Vanilla health bar size
            HUDConfig::getDefaultHealthPosition,
            HUDConfig::getHealthPosition,
            HUDConfig::setHealthPosition
        );
        
        // Armor HUD要素を初期化 - 実証済み方式
        armorElement = new HudElement(
            "armor",
            "hud.universalhudmanager.armor",
            81, 9,  // Same size as health
            HUDConfig::getDefaultArmorPosition,
            HUDConfig::getArmorPosition,
            HUDConfig::setArmorPosition
        );
        
        // Food HUD要素を初期化 - アーマーHUD方式
        foodElement = new HudElement(
            "food",
            "hud.universalhudmanager.food",
            81, 9,  // Same size as health
            HUDConfig::getDefaultFoodPosition,
            HUDConfig::getFoodPosition,
            HUDConfig::setFoodPosition
        );
        
        // Air HUD要素を初期化 - アーマーHUD方式
        airElement = new HudElement(
            "air",
            "hud.universalhudmanager.air",
            81, 9,  // Same size as health
            HUDConfig::getDefaultAirPosition,
            HUDConfig::getAirPosition,
            HUDConfig::setAirPosition
        );
        
        // Experience HUD要素を初期化 - バニラサイズ
        experienceElement = new HudElement(
            "experience",
            "hud.universalhudmanager.experience",
            182, 5,  // バニラのExperience Barサイズ（幅182、高さ5）
            HUDConfig::getDefaultExperiencePosition,
            HUDConfig::getExperiencePosition,
            HUDConfig::setExperiencePosition
        );
        
        // Hotbar HUD要素を初期化 - バニラサイズ
        hotbarElement = new HudElement(
            "hotbar",
            "hud.universalhudmanager.hotbar",
            182, 22,  // バニラのHotbarサイズ（幅182、高さ22）
            HUDConfig::getDefaultHotbarPosition,
            HUDConfig::getHotbarPosition,
            HUDConfig::setHotbarPosition
        );
        
        // Item Name HUD要素を初期化 - テキスト表示用（サイズ調整）
        itemNameElement = new HudElement(
            "item_name",
            "hud.universalhudmanager.item_name",
            66, 19,  // アイテム名表示エリア（幅66=200/3、高さ19=20-1）
            HUDConfig::getDefaultItemNamePosition,
            HUDConfig::getItemNamePosition,
            HUDConfig::setItemNamePosition
        );
        
        // Effects HUD要素を初期化 - エフェクトアイコン表示用
        effectsElement = new HudElement(
            "effects",
            "hud.universalhudmanager.effects",
            50, 52,  // エフェクト表示エリア（2列×25px幅、2行×26px高さ）
            HUDConfig::getDefaultEffectsPosition,
            HUDConfig::getEffectsPosition,
            HUDConfig::setEffectsPosition
        );
        
        // Vehicle Health HUD要素を初期化 - 馬のHP表示用
        vehicleHealthElement = new HudElement(
            "vehicle_health",
            "hud.universalhudmanager.vehicle_health",
            81, 9,  // バニラHealth Barと同じサイズ
            HUDConfig::getDefaultVehicleHealthPosition,
            HUDConfig::getVehicleHealthPosition,
            HUDConfig::setVehicleHealthPosition
        );
        
        // Jump Meter HUD要素を初期化 - ジャンプゲージ表示用
        jumpMeterElement = new HudElement(
            "jump_meter",
            "hud.universalhudmanager.jump_meter",
            182, 5,  // バニラJump Meterサイズ
            HUDConfig::getDefaultJumpMeterPosition,
            HUDConfig::getJumpMeterPosition,
            HUDConfig::setJumpMeterPosition
        );
        
        // Dismount Message HUD要素を初期化 - 降車メッセージ表示用
        dismountMessageElement = new HudElement(
            "dismount_message",
            "hud.universalhudmanager.dismount_message",
            66, 19,  // Item Nameと同じサイズ（幅66、高さ19）
            HUDConfig::getDefaultDismountMessagePosition,
            HUDConfig::getDismountMessagePosition,
            HUDConfig::setDismountMessagePosition
        );
        
        // Boss Bar HUD要素を初期化 - ボスHP表示用
        bossBarElement = new HudElement(
            "boss_bar",
            "hud.universalhudmanager.boss_bar",
            182, 10,  // バニラBoss Barサイズ（幅182、高さ10）
            HUDConfig::getDefaultBossBarPosition,
            HUDConfig::getBossBarPosition,
            HUDConfig::setBossBarPosition
        );
        
        // Attack Indicator HUD要素を2つに分離して初期化（独立設定使用）
        // クロスヘア版: 16x4サイズ（バニラのクロスヘア版に準拠）
        crosshairAttackIndicatorElement = new HudElement(
            "crosshair_attack_indicator",
            "hud.universalhudmanager.crosshair_attack_indicator",
            16, 4,  // バニラクロスヘア版Attack Indicatorサイズ
            (screenWidth, screenHeight) -> new Vector2i(screenWidth / 2 - 8, screenHeight / 2 - 7 + 16),
            HUDConfig::getCrosshairAttackIndicatorPosition,  // 独立したクロスヘア版設定
            HUDConfig::setCrosshairAttackIndicatorPosition
        );
        
        // ホットバー版: 18x18サイズ（バニラのホットバー版に準拠）
        hotbarAttackIndicatorElement = new HudElement(
            "hotbar_attack_indicator",
            "hud.universalhudmanager.hotbar_attack_indicator",
            18, 18,  // バニラホットバー版Attack Indicatorサイズ
            (screenWidth, screenHeight) -> {
                // プレイヤーの利き手に応じて左右位置を決定
                if (this.minecraft != null && this.minecraft.player != null) {
                    HumanoidArm mainArm = this.minecraft.player.getMainArm();
                    int hotbarCenterX = screenWidth / 2;
                    int hotbarY = screenHeight - 20;
                    
                    if (mainArm == HumanoidArm.RIGHT) {
                        // 右利き: ホットバーの右側
                        return new Vector2i(hotbarCenterX + 91 + 6, hotbarY);
                    } else {
                        // 左利き: ホットバーの左側
                        return new Vector2i(hotbarCenterX - 91 - 22, hotbarY);
                    }
                }
                // フォールバック: 右利き位置（右側）
                return new Vector2i(screenWidth / 2 + 91 + 6, screenHeight - 20);
            },
            HUDConfig::getHotbarAttackIndicatorPosition,  // 独立したホットバー版設定
            HUDConfig::setHotbarAttackIndicatorPosition
        );
        
        
        // ⚙設定ボタンを右上に配置（幅80px、高さ20px）
        configButton = net.minecraft.client.gui.components.Button.builder(
            Component.translatable("button.universalhudmanager.settings"),
            btn -> {
                this.minecraft.setScreen(new ConfigScreen(this));
            }
        ).bounds(this.width - 94, this.height - 28, 90, 20).build();
        this.addRenderableWidget(configButton);

        // Reset Layoutボタンをその上に配置（縦並び）
        resetLayoutButton = net.minecraft.client.gui.components.Button.builder(
            Component.translatable("button.universalhudmanager.reset_layout"),
            btn -> {
                this.minecraft.setScreen(new net.minecraft.client.gui.screens.ConfirmScreen(
                    confirmed -> {
                        if (confirmed) {
                            resetAllPositions();
                        }
                        this.minecraft.setScreen(HudEditScreen.this);
                    },
                    Component.translatable("confirm.universalhudmanager.reset_layout.title"),
                    Component.translatable("confirm.universalhudmanager.reset_layout.message")
                ));
            }
        ).bounds(this.width - 94, this.height - 52, 90, 20).build();
        this.addRenderableWidget(resetLayoutButton);

        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("🎯 HUD Edit Screen initialized - Health + Armor + Food + Air + Experience + Hotbar + ItemName + Effects + VehicleHealth + JumpMeter + DismountMessage + BossBar + AttackIndicator(2タイプ)");
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 背景を半透明にして、ゲーム画面を見えるように
        this.renderBackground(guiGraphics);
        
        // Boss Bar HUD要素を描画（最優先）
        if (HUDConfig.BOSS_BAR_ENABLED.get()) {
            renderHudElement(guiGraphics, bossBarElement, mouseX, mouseY);
        }

        // Attack Indicator HUD要素を2つ描画（Minecraft設定に応じて可視性切り替え）
        if (HUDConfig.ATTACK_INDICATOR_ENABLED.get()) {
            AttackIndicatorStatus currentMode = getCurrentAttackIndicatorStatus();
            if (currentMode == AttackIndicatorStatus.CROSSHAIR) {
                // クロスヘア版のみ表示
                renderHudElement(guiGraphics, crosshairAttackIndicatorElement, mouseX, mouseY);
            } else if (currentMode == AttackIndicatorStatus.HOTBAR) {
                // ホットバー版のみ表示
                renderHudElement(guiGraphics, hotbarAttackIndicatorElement, mouseX, mouseY);
            }
            // OFFの場合は何も表示しない
        }

        // Health HUD要素を描画
        if (HUDConfig.HEALTH_ENABLED.get()) {
            renderHudElement(guiGraphics, healthElement, mouseX, mouseY);
        }

        // Armor HUD要素を描画
        if (HUDConfig.ARMOR_ENABLED.get()) {
            renderHudElement(guiGraphics, armorElement, mouseX, mouseY);
        }

        // Food HUD要素を描画
        if (HUDConfig.FOOD_ENABLED.get()) {
            renderHudElement(guiGraphics, foodElement, mouseX, mouseY);
        }

        // Air HUD要素を描画
        if (HUDConfig.AIR_ENABLED.get()) {
            renderHudElement(guiGraphics, airElement, mouseX, mouseY);
        }

        // Experience HUD要素を描画
        if (HUDConfig.EXPERIENCE_ENABLED.get()) {
            renderHudElement(guiGraphics, experienceElement, mouseX, mouseY);
        }

        // Hotbar HUD要素を描画
        if (HUDConfig.HOTBAR_ENABLED.get()) {
            renderHudElement(guiGraphics, hotbarElement, mouseX, mouseY);
        }

        // Item Name HUD要素を描画
        if (HUDConfig.ITEM_NAME_ENABLED.get()) {
            renderHudElement(guiGraphics, itemNameElement, mouseX, mouseY);
        }

        // Effects HUD要素を描画
        if (HUDConfig.EFFECTS_ENABLED.get()) {
            renderHudElement(guiGraphics, effectsElement, mouseX, mouseY);
        }

        // Vehicle Health HUD要素を描画（有効かつ分離モードの場合のみ）
        if (HUDConfig.VEHICLE_HEALTH_ENABLED.get() && HUDConfig.SEPARATE_VEHICLE_HEALTH.get()) {
            renderHudElement(guiGraphics, vehicleHealthElement, mouseX, mouseY);
        }

        // Jump Meter HUD要素を描画（有効かつ分離モードの場合のみ）
        if (HUDConfig.JUMP_METER_ENABLED.get() && HUDConfig.SEPARATE_JUMP_METER.get()) {
            renderHudElement(guiGraphics, jumpMeterElement, mouseX, mouseY);
        }

        // Dismount Message HUD要素を描画
        if (HUDConfig.DISMOUNT_MESSAGE_ENABLED.get()) {
            renderHudElement(guiGraphics, dismountMessageElement, mouseX, mouseY);
        }
        
        
        // ⚙設定ボタンとReset Layoutボタンの表示状態を同期
        configButton.visible = showConfigButton;
        resetLayoutButton.visible = showConfigButton;

        // 使用方法を表示（クロスヘアの上、HUD要素との重複を避けるため中央上部に配置）
        int centerY = height / 2;
        if (showConfigButton) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("screen.universalhudmanager.hud_edit.hint1"), width / 2, centerY - 45, 0xFFFFFF);
            guiGraphics.drawCenteredString(this.font, Component.translatable("screen.universalhudmanager.hud_edit.hint2"), width / 2, centerY - 30, 0xFFFFFF);
            guiGraphics.drawCenteredString(this.font, Component.translatable("screen.universalhudmanager.hud_edit.hint3"), width / 2, centerY - 15, 0xFFFFFF);
        } else {
            guiGraphics.drawCenteredString(this.font, Component.translatable("screen.universalhudmanager.hud_edit.hint4"), width / 2, centerY - 15, 0xAAAAAA);
        }
        
        // 中央クロスライン描画（HUD編集用ガイドライン）
        renderCenterCrossLines(guiGraphics);
        
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
    
    /**
     * HUD要素を描画（緑枠＋実際のHUD＋ラベル）- Archive版準拠
     */
    private void renderHudElement(GuiGraphics guiGraphics, HudElement element, int mouseX, int mouseY) {
        Vector2i pos = element.getFinalPosition(width, height);
        Player player = Minecraft.getInstance().player;
        
        // 実際のHUDを描画（緑枠の中に）
        if (player != null) {
            if (element.id.equals("health")) {
                renderHealthHUD(guiGraphics, player, pos.x, pos.y);
            } else if (element.id.equals("armor")) {
                renderArmorHUD(guiGraphics, player, pos.x, pos.y);
            } else if (element.id.equals("food")) {
                renderFoodHUD(guiGraphics, player, pos.x, pos.y);
            } else if (element.id.equals("air")) {
                renderAirHUD(guiGraphics, player, pos.x, pos.y);
            } else if (element.id.equals("experience")) {
                renderExperienceHUD(guiGraphics, player, pos.x, pos.y);
            } else if (element.id.equals("hotbar")) {
                renderHotbarHUD(guiGraphics, player, pos.x, pos.y);
            } else if (element.id.equals("item_name")) {
                renderItemNameHUD(guiGraphics, player, pos.x, pos.y);
            } else if (element.id.equals("effects")) {
                renderEffectsHUD(guiGraphics, player, pos.x, pos.y);
            } else if (element.id.equals("vehicle_health")) {
                renderVehicleHealthHUD(guiGraphics, player, pos.x, pos.y);
            } else if (element.id.equals("jump_meter")) {
                renderJumpMeterHUD(guiGraphics, player, pos.x, pos.y);
            } else if (element.id.equals("dismount_message")) {
                renderDismountMessageHUD(guiGraphics, player, pos.x, pos.y);
            } else if (element.id.equals("boss_bar")) {
                renderBossBarHUD(guiGraphics, player, pos.x, pos.y);
            } else if (element.id.equals("crosshair_attack_indicator")) {
                renderCrosshairAttackIndicatorHUD(guiGraphics, player, pos.x, pos.y);
            } else if (element.id.equals("hotbar_attack_indicator")) {
                renderHotbarAttackIndicatorHUD(guiGraphics, player, pos.x, pos.y);
            }
        }
        
        // Item Name用の特別な緑枠描画（黒い背景ボックスに合わせる）
        if (element.id.equals("item_name")) {
            // サンプルテキストのサイズを計算して緑枠を調整
            String sampleItemName = Component.translatable("hud.universalhudmanager.preview.item_name").getString();
            int textWidth = this.font.width(sampleItemName);
            int textX = pos.x + 33 - textWidth / 2;
            int textY = pos.y;
            
            // 黒い背景ボックスのサイズに合わせた緑枠
            int frameLeft = textX - 4;
            int frameTop = textY - 2;
            int frameRight = textX + textWidth + 4;
            int frameBottom = textY + 12;
            
            int color = (element == selectedElement) ? 0xFF00FF00 : 0xFF008800;
            
            // 枠線を描画（黒い背景に沿って）
            guiGraphics.fill(frameLeft - 1, frameTop - 1, frameRight + 1, frameTop, color);         // 上
            guiGraphics.fill(frameLeft - 1, frameBottom, frameRight + 1, frameBottom + 1, color);   // 下
            guiGraphics.fill(frameLeft - 1, frameTop, frameLeft, frameBottom, color);               // 左
            guiGraphics.fill(frameRight, frameTop, frameRight + 1, frameBottom, color);             // 右
        } else if (element.id.equals("dismount_message")) {
            // Dismount Message用の特別な緑枠描画（中央揃えHUDに合わせる）
            String sampleMessage = Component.translatable("mount.onboard",
                    this.minecraft.options.keyShift.getTranslatedKeyMessage()).getString();
            int textWidth = this.font.width(sampleMessage);
            
            // drawCenteredStringと同じ中央計算
            int centerX = pos.x;
            int centerY = pos.y;
            int textX = centerX - textWidth / 2;
            int textY = centerY;
            
            // 中央揃えされたテキストに合わせた緑枠
            int frameLeft = textX - 4;
            int frameTop = textY - 2;
            int frameRight = textX + textWidth + 4;
            int frameBottom = textY + 11;
            
            int color = (element == selectedElement) ? 0xFF00FF00 : 0xFF008800;
            
            // 枠線を描画（黒い背景に沿って）
            guiGraphics.fill(frameLeft - 1, frameTop - 1, frameRight + 1, frameTop, color);         // 上
            guiGraphics.fill(frameLeft - 1, frameBottom, frameRight + 1, frameBottom + 1, color);   // 下
            guiGraphics.fill(frameLeft - 1, frameTop, frameLeft, frameBottom, color);               // 左
            guiGraphics.fill(frameRight, frameTop, frameRight + 1, frameBottom, color);             // 右
        } else if (element.id.equals("boss_bar")) {
            // Boss Bar用の緑枠描画（実際のBoss Barサイズに合わせて5px高）
            int color = (element == selectedElement) ? 0xFF00FF00 : 0xFF008800;
            int actualHeight = 5;  // 実際のBoss Barテクスチャの高さ
            
            // Boss Bar実サイズに合わせた緑枠
            guiGraphics.fill(pos.x - 1, pos.y - 1, pos.x + element.width + 1, pos.y, color);                    // 上
            guiGraphics.fill(pos.x - 1, pos.y + actualHeight, pos.x + element.width + 1, pos.y + actualHeight + 1, color);  // 下
            guiGraphics.fill(pos.x - 1, pos.y, pos.x, pos.y + actualHeight, color);                          // 左
            guiGraphics.fill(pos.x + element.width, pos.y, pos.x + element.width + 1, pos.y + actualHeight, color);  // 右
        } else if (element.id.equals("crosshair_attack_indicator")) {
            // クロスヘアAttack Indicator用の緑枠描画（16x4サイズ）
            int color = (element == selectedElement) ? 0xFF00FF00 : 0xFF008800;
            
            // 16x4サイズの緑枠描画
            guiGraphics.fill(pos.x - 1, pos.y - 1, pos.x + element.width + 1, pos.y, color);                    // 上
            guiGraphics.fill(pos.x - 1, pos.y + element.height, pos.x + element.width + 1, pos.y + element.height + 1, color);  // 下
            guiGraphics.fill(pos.x - 1, pos.y, pos.x, pos.y + element.height, color);                          // 左
            guiGraphics.fill(pos.x + element.width, pos.y, pos.x + element.width + 1, pos.y + element.height, color);  // 右
        } else if (element.id.equals("hotbar_attack_indicator")) {
            // ホットバーAttack Indicator用の緑枠描画（18x18サイズ）
            int color = (element == selectedElement) ? 0xFF00FF00 : 0xFF008800;
            
            // 18x18サイズの緑枠描画
            guiGraphics.fill(pos.x - 1, pos.y - 1, pos.x + element.width + 1, pos.y, color);                    // 上
            guiGraphics.fill(pos.x - 1, pos.y + element.height, pos.x + element.width + 1, pos.y + element.height + 1, color);  // 下
            guiGraphics.fill(pos.x - 1, pos.y, pos.x, pos.y + element.height, color);                          // 左
            guiGraphics.fill(pos.x + element.width, pos.y, pos.x + element.width + 1, pos.y + element.height, color);  // 右
        } else if (element.id.equals("effects")) {
            // Effects用の特別な緑枠描画（4個のエフェクトアイコンを囲む）
            int color = (element == selectedElement) ? 0xFF00FF00 : 0xFF008800;
            
            // 緑枠の範囲計算（右上から左下まで）
            int frameLeft = pos.x - 25;      // 左端（左から2番目のアイコンの左端）
            int frameTop = pos.y;            // 上端
            int frameRight = pos.x + 24;     // 右端（一番右のアイコンの右端）
            int frameBottom = pos.y + 50;    // 下端（下段のアイコンの下端）
            
            // 枠線を描画
            guiGraphics.fill(frameLeft - 1, frameTop - 1, frameRight + 1, frameTop, color);        // 上
            guiGraphics.fill(frameLeft - 1, frameBottom, frameRight + 1, frameBottom + 1, color);  // 下
            guiGraphics.fill(frameLeft - 1, frameTop, frameLeft, frameBottom, color);              // 左
            guiGraphics.fill(frameRight, frameTop, frameRight + 1, frameBottom, color);            // 右
        } else {
            // 他の要素は通常の緑枠
            int color = (element == selectedElement) ? 0xFF00FF00 : 0xFF008800;
            
            // 枠線を描画
            guiGraphics.fill(pos.x - 1, pos.y - 1, pos.x + element.width + 1, pos.y, color);           // 上
            guiGraphics.fill(pos.x - 1, pos.y + element.height, pos.x + element.width + 1, pos.y + element.height + 1, color); // 下
            guiGraphics.fill(pos.x - 1, pos.y, pos.x, pos.y + element.height, color);                   // 左
            guiGraphics.fill(pos.x + element.width, pos.y, pos.x + element.width + 1, pos.y + element.height, color); // 右
        }
        
        // ラベルを描画（Effects、Item Name、Dismount Messageは特別な位置）
        if (element.id.equals("effects")) {
            // Effectsラベルは緑枠の中央に表示
            int frameCenterX = pos.x - 1;  // 緑枠の中央位置
            guiGraphics.drawCenteredString(this.font, Component.translatable(element.displayName), frameCenterX, pos.y - 15, 0xFFFFFF);
        } else if (element.id.equals("item_name")) {
            // Item Nameラベルは黒背景の中央に表示
            String sampleItemName = Component.translatable("hud.universalhudmanager.preview.item_name").getString();
            int textWidth = this.font.width(sampleItemName);
            int textX = pos.x + 33 - textWidth / 2;
            guiGraphics.drawCenteredString(this.font, Component.translatable(element.displayName), textX + textWidth / 2, pos.y - 15, 0xFFFFFF);
        } else if (element.id.equals("dismount_message")) {
            // Dismount Messageラベルは中央揃えHUDの上に表示
            guiGraphics.drawCenteredString(this.font, Component.translatable(element.displayName), pos.x, pos.y - 15, 0xFFFFFF);
        } else if (element.id.equals("boss_bar")) {
            // Boss Barラベルは左端基準で中央に表示（Example Bossとの重複を避けるため少し上に）
            guiGraphics.drawCenteredString(this.font, Component.translatable(element.displayName), pos.x + element.width / 2, pos.y - 20, 0xFFFFFF);
        } else if (element.id.equals("crosshair_attack_indicator")) {
            // クロスヘアAttack Indicatorラベルは中央上に表示
            guiGraphics.drawCenteredString(this.font, Component.translatable(element.displayName), pos.x + element.width / 2, pos.y - 15, 0xFFFFFF);
        } else if (element.id.equals("hotbar_attack_indicator")) {
            // ホットバーAttack Indicatorラベルは中央上に表示
            guiGraphics.drawCenteredString(this.font, Component.translatable(element.displayName), pos.x + element.width / 2, pos.y - 15, 0xFFFFFF);
        } else {
            guiGraphics.drawCenteredString(this.font, Component.translatable(element.displayName), pos.x + element.width / 2, pos.y - 15, 0xFFFFFF);
        }
        
        // マウス座標を表示（ドラッグ中のみ）
        if (element == draggedElement) {
            if (element.id.equals("effects")) {
                // Effectsは緑枠の中央に座標表示
                int frameCenterX = pos.x - 1;  // 緑枠の中央位置
                int frameCenterY = pos.y + 55;  // 緑枠の下5px
                guiGraphics.drawCenteredString(this.font, 
                    String.format("(%d, %d)", pos.x, pos.y), 
                    frameCenterX, frameCenterY, 0xFFFF00);
            } else if (element.id.equals("item_name")) {
                // Item Nameは特別な緑枠に合わせて座標表示
                String sampleItemName = Component.translatable("hud.universalhudmanager.preview.item_name").getString();
                int textWidth = this.font.width(sampleItemName);
                int textX = pos.x + 33 - textWidth / 2;
                guiGraphics.drawCenteredString(this.font, 
                    String.format("(%d, %d)", pos.x, pos.y), 
                    textX + textWidth / 2, pos.y + 19 - 3, 0xFFFF00);  // 緑枠の下-3px（2px戻した）
            } else if (element.id.equals("dismount_message")) {
                // Dismount Message座標表示は中央揃えHUDの下に表示
                guiGraphics.drawCenteredString(this.font, 
                    String.format("(%d, %d)", pos.x, pos.y), 
                    pos.x, pos.y + 16, 0xFFFF00);
            } else if (element.id.equals("boss_bar")) {
                // Boss Bar座標表示は中央に表示（実際のBoss Barサイズに合わせて調整）
                int actualHeight = 5;  // 実際のBoss Barテクスチャの高さ
                guiGraphics.drawCenteredString(this.font, 
                    String.format("(%d, %d)", pos.x, pos.y), 
                    pos.x + element.width / 2, pos.y + actualHeight + 5, 0xFFFF00);
            } else if (element.id.equals("crosshair_attack_indicator")) {
                // クロスヘアAttack Indicator座標表示は中央下に表示
                guiGraphics.drawCenteredString(this.font, 
                    String.format("(%d, %d)", pos.x, pos.y), 
                    pos.x + element.width / 2, pos.y + element.height + 5, 0xFFFF00);
            } else if (element.id.equals("hotbar_attack_indicator")) {
                // ホットバーAttack Indicator座標表示は中央下に表示
                guiGraphics.drawCenteredString(this.font, 
                    String.format("(%d, %d)", pos.x, pos.y), 
                    pos.x + element.width / 2, pos.y + element.height + 5, 0xFFFF00);
            } else {
                guiGraphics.drawCenteredString(this.font, 
                    String.format("(%d, %d)", pos.x, pos.y), 
                    pos.x + element.width / 2, pos.y + element.height + 5, 0xFFFF00);
            }
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            
            if (HUDConfig.DEBUG_MODE.get()) {
                System.out.println("🖱️ Mouse clicked: (" + mouseX + ", " + mouseY + ")");
            }
            
            // ドラッグ開始情報を記録
            dragStartX = mouseX;
            dragStartY = mouseY;
            
            // Health HUD要素のクリック判定
            if (isMouseOver(healthElement, mouseX, mouseY)) {
                selectedElement = healthElement;
                draggedElement = healthElement;
                dragStartOffset = healthElement.getCurrentOffset();
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("✅ Health HUD clicked - drag started");
                }
                return true;
            }
            
            // Armor HUD要素のクリック判定
            if (isMouseOver(armorElement, mouseX, mouseY)) {
                selectedElement = armorElement;
                draggedElement = armorElement;
                dragStartOffset = armorElement.getCurrentOffset();
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("✅ Armor HUD clicked - drag started");
                }
                return true;
            }
            
            // Food HUD要素のクリック判定
            if (isMouseOver(foodElement, mouseX, mouseY)) {
                selectedElement = foodElement;
                draggedElement = foodElement;
                dragStartOffset = foodElement.getCurrentOffset();
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("✅ Food HUD clicked - drag started");
                }
                return true;
            }
            
            // Air HUD要素のクリック判定
            if (isMouseOver(airElement, mouseX, mouseY)) {
                selectedElement = airElement;
                draggedElement = airElement;
                dragStartOffset = airElement.getCurrentOffset();
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("✅ Air HUD clicked - drag started");
                }
                return true;
            }
            
            // Experience HUD要素のクリック判定
            if (isMouseOver(experienceElement, mouseX, mouseY)) {
                selectedElement = experienceElement;
                draggedElement = experienceElement;
                dragStartOffset = experienceElement.getCurrentOffset();
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("✅ Experience HUD clicked - drag started");
                }
                return true;
            }
            
            // Hotbar HUD要素のクリック判定
            if (isMouseOver(hotbarElement, mouseX, mouseY)) {
                selectedElement = hotbarElement;
                draggedElement = hotbarElement;
                dragStartOffset = hotbarElement.getCurrentOffset();
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("✅ Hotbar HUD clicked - drag started");
                }
                return true;
            }
            
            // Item Name HUD要素のクリック判定
            if (isMouseOver(itemNameElement, mouseX, mouseY)) {
                selectedElement = itemNameElement;
                draggedElement = itemNameElement;
                dragStartOffset = itemNameElement.getCurrentOffset();
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("✅ Item Name HUD clicked - drag started");
                }
                return true;
            }
            
            // Effects HUD要素のクリック判定
            if (isMouseOver(effectsElement, mouseX, mouseY)) {
                selectedElement = effectsElement;
                draggedElement = effectsElement;
                dragStartOffset = effectsElement.getCurrentOffset();
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("✅ Effects HUD clicked - drag started");
                }
                return true;
            }
            
            // Vehicle Health HUD要素のクリック判定（分離モードの場合のみ）
            if (HUDConfig.SEPARATE_VEHICLE_HEALTH.get() && isMouseOver(vehicleHealthElement, mouseX, mouseY)) {
                selectedElement = vehicleHealthElement;
                draggedElement = vehicleHealthElement;
                dragStartOffset = vehicleHealthElement.getCurrentOffset();
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("✅ Vehicle Health HUD clicked - drag started");
                }
                return true;
            }
            
            // Jump Meter HUD要素のクリック判定（分離モードの場合のみ）
            if (HUDConfig.SEPARATE_JUMP_METER.get() && isMouseOver(jumpMeterElement, mouseX, mouseY)) {
                selectedElement = jumpMeterElement;
                draggedElement = jumpMeterElement;
                dragStartOffset = jumpMeterElement.getCurrentOffset();
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("✅ Jump Meter HUD clicked - drag started");
                }
                return true;
            }
            
            // Dismount Message HUD要素のクリック判定
            if (isMouseOver(dismountMessageElement, mouseX, mouseY)) {
                selectedElement = dismountMessageElement;
                draggedElement = dismountMessageElement;
                dragStartOffset = dismountMessageElement.getCurrentOffset();
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("✅ Dismount Message HUD clicked - drag started");
                }
                return true;
            }
            
            // Boss Bar HUD要素のクリック判定
            if (isMouseOver(bossBarElement, mouseX, mouseY)) {
                selectedElement = bossBarElement;
                draggedElement = bossBarElement;
                dragStartOffset = bossBarElement.getCurrentOffset();
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("✅ Boss Bar HUD clicked - drag started");
                }
                return true;
            }
            
            // Attack Indicator HUD要素のクリック判定（現在表示されている方のみ）
            AttackIndicatorStatus currentMode = getCurrentAttackIndicatorStatus();
            if (currentMode == AttackIndicatorStatus.CROSSHAIR && isMouseOver(crosshairAttackIndicatorElement, mouseX, mouseY)) {
                selectedElement = crosshairAttackIndicatorElement;
                draggedElement = crosshairAttackIndicatorElement;
                dragStartOffset = crosshairAttackIndicatorElement.getCurrentOffset();
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("✅ Crosshair Attack Indicator HUD clicked - drag started");
                }
                return true;
            }
            
            if (currentMode == AttackIndicatorStatus.HOTBAR && isMouseOver(hotbarAttackIndicatorElement, mouseX, mouseY)) {
                selectedElement = hotbarAttackIndicatorElement;
                draggedElement = hotbarAttackIndicatorElement;
                dragStartOffset = hotbarAttackIndicatorElement.getCurrentOffset();
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("✅ Hotbar Attack Indicator HUD clicked - drag started");
                }
                return true;
            }
            
            
            // 何もクリックされなかった場合
            selectedElement = null;
            draggedElement = null;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    /**
     * マウスがHUD要素上にあるかチェック - アーマーHUD成功方式
     */
    private boolean isMouseOver(HudElement element, double mouseX, double mouseY) {
        Vector2i pos = element.getFinalPosition(width, height);
        
        // Effectsは特別なドラッグ範囲を使用
        if (element.id.equals("effects")) {
            int frameLeft = pos.x - 25;
            int frameTop = pos.y;
            int frameRight = pos.x + 24;
            int frameBottom = pos.y + 50;
            
            return mouseX >= frameLeft && mouseX <= frameRight &&
                   mouseY >= frameTop && mouseY <= frameBottom;
        }
        
        // Item Nameは黒背景の範囲でドラッグ可能
        if (element.id.equals("item_name")) {
            String sampleItemName = Component.translatable("hud.universalhudmanager.preview.item_name").getString();
            int textWidth = this.font.width(sampleItemName);
            int textX = pos.x + 33 - textWidth / 2;
            int textY = pos.y;
            
            int frameLeft = textX - 4;
            int frameTop = textY - 2;
            int frameRight = textX + textWidth + 4;
            int frameBottom = textY + 12;
            
            return mouseX >= frameLeft && mouseX <= frameRight &&
                   mouseY >= frameTop && mouseY <= frameBottom;
        }
        
        // Dismount Messageは中央揃えHUDの範囲でドラッグ可能
        if (element.id.equals("dismount_message")) {
            String sampleMessage = Component.translatable("mount.onboard",
                    this.minecraft.options.keyShift.getTranslatedKeyMessage()).getString();
            int textWidth = this.font.width(sampleMessage);

            // drawCenteredStringと同じ中央計算
            int centerX = pos.x;
            int centerY = pos.y;
            int textX = centerX - textWidth / 2;
            int textY = centerY;
            
            int frameLeft = textX - 4;
            int frameTop = textY - 2;
            int frameRight = textX + textWidth + 4;
            int frameBottom = textY + 11;
            
            return mouseX >= frameLeft && mouseX <= frameRight &&
                   mouseY >= frameTop && mouseY <= frameBottom;
        }
        
        // Boss Barは標準の矩形範囲でドラッグ可能
        if (element.id.equals("boss_bar")) {
            return mouseX >= pos.x && mouseX <= pos.x + element.width &&
                   mouseY >= pos.y && mouseY <= pos.y + element.height;
        }
        
        
        return mouseX >= pos.x && mouseX <= pos.x + element.width &&
               mouseY >= pos.y && mouseY <= pos.y + element.height;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && draggedElement != null) {
            
            // ドラッグオフセットを計算
            double offsetX = mouseX - dragStartX;
            double offsetY = mouseY - dragStartY;
            
            Vector2i newOffset = new Vector2i(
                (int) (dragStartOffset.x + offsetX),
                (int) (dragStartOffset.y + offsetY)
            );
            
            // 一時的にオフセットを設定（まだ保存はしない）
            draggedElement.setTemporaryOffset(newOffset);
            
            // 静的なtemporaryDragOffsetsにも設定
            setTemporaryDragOffset(draggedElement.id, newOffset);
            
            if (HUDConfig.DEBUG_MODE.get()) {
                System.out.println("🔄 Dragging " + draggedElement.id + " to offset: " + newOffset);
            }
            
            return true;
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggedElement != null) {
            
            // ドラッグ終了 - オフセットを保存
            Vector2i finalOffset = draggedElement.getTemporaryOffset();
            if (finalOffset != null) {
                draggedElement.saveOffset(finalOffset);
                
                if (HUDConfig.DEBUG_MODE.get()) {
                    System.out.println("💾 Saved " + draggedElement.id + " offset: " + finalOffset);
                }
            }
            
            // 一時オフセットをクリア
            draggedElement.clearTemporaryOffset();
            setTemporaryDragOffset(draggedElement.id, null);
            draggedElement = null;
            
            return true;
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    /**
     * Health HUDを描画（編集画面用・エフェクト対応・Absorption対応）
     */
    private void renderHealthHUD(GuiGraphics guiGraphics, Player player, int x, int y) {
        // Get player health data
        int health = (int) Math.ceil(player.getHealth());
        int maxHealth = (int) Math.ceil(player.getMaxHealth());
        int absorption = (int) Math.ceil(player.getAbsorptionAmount());
        
        // Check for status effects that change heart appearance
        boolean hasPoison = player.hasEffect(MobEffects.POISON);
        boolean hasWither = player.hasEffect(MobEffects.WITHER);
        
        // Calculate rows for hearts
        int rows = (int) Math.ceil((maxHealth + absorption) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (rows - 2), 3);
        
        // Render all hearts
        for (int i = 0; i < (int) Math.ceil((maxHealth + absorption) / 2.0F); ++i) {
            int row = i / 10;
            int col = i % 10;
            int heartX = x + col * 8;
            int heartY = y - row * rowHeight;
            
            // Background heart
            guiGraphics.blit(ICONS, heartX, heartY, 16, 0, 9, 9);
            
            // Heart type and fill with status effect colors
            if (i < maxHealth / 2) {
                // Normal hearts with status effect coloring
                if (i * 2 + 1 < health) {
                    // Full heart - choose sprite based on status effects
                    if (hasWither) {
                        guiGraphics.blit(ICONS, heartX, heartY, 124, 0, 9, 9); // Withered full heart (black)
                    } else if (hasPoison) {
                        guiGraphics.blit(ICONS, heartX, heartY, 88, 0, 9, 9); // Poisoned full heart (green)
                    } else {
                        guiGraphics.blit(ICONS, heartX, heartY, 52, 0, 9, 9); // Normal full heart (red)
                    }
                } else if (i * 2 + 1 == health) {
                    // Half heart - choose sprite based on status effects
                    if (hasWither) {
                        guiGraphics.blit(ICONS, heartX, heartY, 133, 0, 9, 9); // Withered half heart (black)
                    } else if (hasPoison) {
                        guiGraphics.blit(ICONS, heartX, heartY, 97, 0, 9, 9); // Poisoned half heart (green)
                    } else {
                        guiGraphics.blit(ICONS, heartX, heartY, 61, 0, 9, 9); // Normal half heart (red)
                    }
                }
            } else {
                // Absorption hearts (golden apple effect)
                int absIndex = i - maxHealth / 2;
                if (absIndex * 2 + 1 < absorption) {
                    if (hasWither) {
                        guiGraphics.blit(ICONS, heartX, heartY, 142, 0, 9, 9); // Withered full absorption (black)
                    } else {
                        guiGraphics.blit(ICONS, heartX, heartY, 160, 0, 9, 9); // Normal full absorption (yellow)
                    }
                } else if (absIndex * 2 + 1 == absorption) {
                    if (hasWither) {
                        guiGraphics.blit(ICONS, heartX, heartY, 151, 0, 9, 9); // Withered half absorption (black)
                    } else {
                        guiGraphics.blit(ICONS, heartX, heartY, 169, 0, 9, 9); // Normal half absorption (yellow)
                    }
                }
            }
        }
    }
    
    /**
     * Armor HUDを描画（編集画面用）
     */
    private void renderArmorHUD(GuiGraphics guiGraphics, Player player, int x, int y) {
        // 編集画面では常に全アーマーを表示（装備なしでも見えるように）
        for (int i = 0; i < 10; i++) {
            int armorX = x + i * 8;
            
            // 空ゲージ背景を描画
            guiGraphics.blit(ICONS,
                armorX, y,      // 描画位置
                16, 9,          // テクスチャ位置（空のアーマー）
                9, 9,           // サイズ
                256, 256);      // テクスチャ全体サイズ
            
            // フルアーマーアイコンを上から描画（編集画面なので全部表示）
            guiGraphics.blit(ICONS,
                armorX, y,      // 描画位置
                34, 9,          // テクスチャ位置（フルアーマー）
                9, 9,           // サイズ
                256, 256);      // テクスチャ全体サイズ
        }
    }
    
    /**
     * Food HUDを描画（編集画面用・飢餓効果・空腹エフェクト対応）
     */
    private void renderFoodHUD(GuiGraphics guiGraphics, Player player, int x, int y) {
        // Food data取得
        int foodLevel = player.getFoodData().getFoodLevel();
        
        // エフェクト状態を判定
        boolean isStarving = foodLevel <= 6;  // 飢餓状態（体力が減る）
        boolean hasHunger = player.hasEffect(MobEffects.HUNGER);  // 空腹エフェクト（腐った肉等）
        
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
            
            // 背景（空の食料アイコン）を描画
            guiGraphics.blit(ICONS,
                foodX, foodY,        // 描画位置
                16, 27,             // テクスチャ位置（空の食料）
                9, 9,               // サイズ
                256, 256);          // テクスチャ全体サイズ
            
            // 実際の食料を描画
            if (i * 2 + 1 < foodLevel) {
                // フル食料アイコン - エフェクトに応じてテクスチャを選択
                int textureU;
                if (hasHunger) {
                    textureU = 88;  // 空腹エフェクト時（緑色）
                } else if (isStarving) {
                    textureU = 88;  // 飢餓時も同じ緑色
                } else {
                    textureU = 52;  // 通常（茶色）
                }
                
                guiGraphics.blit(ICONS,
                    foodX, foodY,        // 描画位置
                    textureU, 27,        // テクスチャ位置（フル食料）
                    9, 9,               // サイズ
                    256, 256);          // テクスチャ全体サイズ
            } else if (i * 2 + 1 == foodLevel) {
                // ハーフ食料アイコン - エフェクトに応じてテクスチャを選択
                int textureU;
                if (hasHunger) {
                    textureU = 97;  // 空腹エフェクト時（緑色ハーフ）
                } else if (isStarving) {
                    textureU = 97;  // 飢餓時も同じ緑色ハーフ
                } else {
                    textureU = 61;  // 通常（茶色ハーフ）
                }
                
                guiGraphics.blit(ICONS,
                    foodX, foodY,        // 描画位置
                    textureU, 27,        // テクスチャ位置（ハーフ食料）
                    9, 9,               // サイズ
                    256, 256);          // テクスチャ全体サイズ
            }
        }
    }
    
    /**
     * Air HUDを描画（編集画面用・水中時バブル対応）
     */
    private void renderAirHUD(GuiGraphics guiGraphics, Player player, int x, int y) {
        // 編集画面では全部のバブルを表示（視認性のため）
        for (int i = 0; i < 10; i++) {
            int bubbleX = x + (9 - i) * 8;  // 右から左へ配置（バニラと同じ）
            int bubbleY = y;
            
            // 全部フルバブルで表示（編集画面なので見やすく）
            guiGraphics.blit(ICONS,
                bubbleX, bubbleY,        // 描画位置
                16, 18,                 // テクスチャ位置（フルバブル）
                9, 9,                   // サイズ
                256, 256);              // テクスチャ全体サイズ
        }
    }
    
    /**
     * Experience HUDを描画（編集画面用・バニラ準拠）
     */
    private void renderExperienceHUD(GuiGraphics guiGraphics, Player player, int x, int y) {
        // 編集画面では常に経験値バーを表示（見やすさのため）
        
        // 背景バー（空のバー）を描画
        guiGraphics.blit(ICONS,
            x, y,           // 描画位置
            0, 64,          // テクスチャ位置（空の経験値バー）
            182, 5,         // サイズ（幅182、高さ5）
            256, 256);      // テクスチャ全体サイズ
        
        // 緑の経験値バー（半分まで満たされた状態で表示）
        int fillWidth = 91;  // 半分の幅
        guiGraphics.blit(ICONS,
            x, y,           // 描画位置
            0, 69,          // テクスチャ位置（緑の経験値バー）
            fillWidth, 5,   // サイズ
            256, 256);      // テクスチャ全体サイズ
        
        // レベル数字を中央に表示（編集画面用のサンプル）
        String levelText = "30";
        int textWidth = this.font.width(levelText);
        int textX = x + 91 - textWidth / 2;  // バーの中央
        int textY = y - 6;  // バーの上
        
        // 黒い影（上下左右に描画）
        guiGraphics.drawString(this.font, levelText, textX + 1, textY, 0x000000, false);
        guiGraphics.drawString(this.font, levelText, textX - 1, textY, 0x000000, false);
        guiGraphics.drawString(this.font, levelText, textX, textY + 1, 0x000000, false);
        guiGraphics.drawString(this.font, levelText, textX, textY - 1, 0x000000, false);
        
        // 緑色の数字本体
        guiGraphics.drawString(this.font, levelText, textX, textY, 0x80FF20, false);
    }
    
    /**
     * Hotbar HUDを描画（編集画面用・バニラ準拠）
     */
    private void renderHotbarHUD(GuiGraphics guiGraphics, Player player, int x, int y) {
        // 編集画面では常にホットバーを表示（見やすさのため）
        
        // メインのホットバー背景を描画（182×22）
        guiGraphics.blit(WIDGETS,
            x, y,           // 描画位置
            0, 0,           // テクスチャ位置（ホットバー背景）
            182, 22,        // サイズ
            256, 256);      // テクスチャ全体サイズ
        
        // 選択枠を中央スロット（5番目）に表示（編集画面用サンプル）
        int selectedSlot = 4;  // 0-indexed, so 4 = 5th slot
        guiGraphics.blit(WIDGETS,
            x - 1 + selectedSlot * 20, y - 1,  // 選択枠位置
            0, 22,          // テクスチャ位置（選択枠）
            24, 22,         // サイズ
            256, 256);      // テクスチャ全体サイズ
        
        // アイテムスロット描画（簡易表示 - 編集画面なので実際のアイテムは表示しない）
        // 各スロットの位置を示すために小さな印を描画
        for (int i = 0; i < 9; i++) {
            int slotX = x + 3 + i * 20;
            int slotY = y + 3;
            
            // スロット番号を描画（1-9）
            String slotNum = String.valueOf(i + 1);
            guiGraphics.drawString(this.font, slotNum, slotX + 4, slotY + 4, 0x808080, false);
        }
    }
    
    /**
     * Item Name HUDを描画（編集画面用・サンプル表示）
     */
    private void renderItemNameHUD(GuiGraphics guiGraphics, Player player, int x, int y) {
        // 編集画面では常にサンプルのアイテム名を表示（見やすさのため）
        
        // サンプルテキスト
        String sampleItemName = Component.translatable("hud.universalhudmanager.preview.item_name").getString();
        
        // 緑枠の中央を基準にテキストを配置（HudElementサイズ: 66×19）
        int textWidth = this.font.width(sampleItemName);
        int textX = x + 33 - textWidth / 2;  // 緑枠の中央（x + 66/2）
        int textY = y;
        
        // 半透明の背景を描画（編集画面で見やすくするため）
        guiGraphics.fill(textX - 4, textY - 2, textX + textWidth + 4, textY + 12, 0x80000000);
        
        // サンプルテキストを白色で描画（影付き）
        guiGraphics.drawString(this.font, sampleItemName, textX, textY, 0xFFFFFF, true);
    }
    
    /**
     * Effects HUDを描画（編集画面用・サンプル表示）
     */
    private void renderEffectsHUD(GuiGraphics guiGraphics, Player player, int x, int y) {
        // 編集画面では常にサンプルのエフェクトアイコンを表示（バニラ準拠の配置）
        ResourceLocation INVENTORY_LOCATION = new ResourceLocation("minecraft", "textures/gui/container/inventory.png");
        
        // 上段：グッドエフェクト（有益効果）2個 - Regenerationで統一（模様なし）
        int[][] goodEffectIcons = {
            {144, 198}, // Regeneration - 模様なし
            {144, 198}  // Regeneration - 模様なし
        };
        
        for (int i = 0; i < goodEffectIcons.length; i++) {
            int iconX = x - i * 25;  // 緑枠内の左側から配置
            int iconY = y;
            
            // 背景（青いポーション効果背景）
            guiGraphics.blit(INVENTORY_LOCATION,
                iconX, iconY,       // 描画位置
                141, 166,          // テクスチャ位置（通常背景）
                24, 24,            // サイズ
                256, 256);         // テクスチャ全体サイズ
            
            // エフェクトアイコン
            guiGraphics.blit(INVENTORY_LOCATION,
                iconX + 3, iconY + 3,           // 描画位置（背景内に中央配置）
                goodEffectIcons[i][0], goodEffectIcons[i][1],  // テクスチャ位置
                18, 18,            // サイズ
                256, 256);         // テクスチャ全体サイズ
            
            // 時間表示は編集画面では非表示（サンプルなので省略）
        }
        
        // 下段：バッドエフェクト（有害効果）2個
        int[][] badEffectIcons = {
            {36, 198},  // Slowness
            {108, 198}  // Poison
        };
        
        for (int i = 0; i < badEffectIcons.length; i++) {
            int iconX = x - i * 25;  // 緑枠内の左側から配置
            int iconY = y + 26;  // 下段（26px下）
            
            // 背景（赤いポーション効果背景）
            guiGraphics.blit(INVENTORY_LOCATION,
                iconX, iconY,       // 描画位置
                165, 166,          // テクスチャ位置（有害効果背景）
                24, 24,            // サイズ
                256, 256);         // テクスチャ全体サイズ
            
            // エフェクトアイコン
            guiGraphics.blit(INVENTORY_LOCATION,
                iconX + 3, iconY + 3,           // 描画位置（背景内に中央配置）
                badEffectIcons[i][0], badEffectIcons[i][1],  // テクスチャ位置
                18, 18,            // サイズ
                256, 256);         // テクスチャ全体サイズ
            
            // 時間表示は編集画面では非表示（サンプルなので省略）
        }
    }
    
    /**
     * Vehicle Health HUDを描画（編集画面用・サンプル表示）
     */
    private void renderVehicleHealthHUD(GuiGraphics guiGraphics, Player player, int x, int y) {
        // 編集画面では常にサンプルの馬のHPを表示（見やすさのため）
        
        // サンプルでは10ハートを表示（右から左へ、Foodバーと同じ配置）
        for (int i = 0; i < 10; i++) {
            int heartX = x + (9 - i) * 8;  // 右から左へ配置（Foodバーと同じ）
            int heartY = y;
            
            // 背景ハート（空）を描画
            guiGraphics.blit(ICONS, heartX, heartY, 52, 9, 9, 9);
            
            // サンプルでは最初の7ハートをフル、次の1つをハーフで表示
            if (i < 7) {
                // フルハート（馬のHP色）
                guiGraphics.blit(ICONS, heartX, heartY, 88, 9, 9, 9);
            } else if (i == 7) {
                // ハーフハート（馬のHP色）
                guiGraphics.blit(ICONS, heartX, heartY, 97, 9, 9, 9);
            }
            // i >= 8 は空のハートのまま
        }
    }
    
    /**
     * Jump Meter HUDを描画（編集画面用・サンプル表示）
     */
    private void renderJumpMeterHUD(GuiGraphics guiGraphics, Player player, int x, int y) {
        // 編集画面では常にサンプルのジャンプゲージを表示
        
        // 背景バー（空のジャンプゲージ）を描画
        guiGraphics.blit(ICONS, x, y, 0, 84, 182, 5);
        
        // サンプルでは60%まで充電された状態で表示
        int fillWidth = (int) (182 * 0.6F);
        guiGraphics.blit(ICONS, x, y, 0, 89, fillWidth, 5);
    }
    
    /**
     * Dismount Message HUDを描画（編集画面用・サンプル表示）
     */
    private void renderDismountMessageHUD(GuiGraphics guiGraphics, Player player, int x, int y) {
        // 編集画面では常にサンプルのメッセージを表示（見やすさのため）
        
        // バニラと同じ生成方法でプレイヤーの実際のキーバインドを使用
        Component dismountComponent = Component.translatable("mount.onboard",
                this.minecraft.options.keyShift.getTranslatedKeyMessage());
        String sampleMessage = dismountComponent.getString();

        // プレイ画面と同じ描画方式でズレを解消
        int centerX = x;  // 編集画面のx座標をそのまま中心点として使用
        int centerY = y;  // 編集画面のy座標をそのまま中心点として使用

        // テキストの幅を計算（黒背景用）
        int textWidth = this.font.width(sampleMessage);
        int textX = centerX - textWidth / 2;

        // Item Nameと同じ半透明の黒背景を描画
        guiGraphics.fill(textX - 4, centerY - 2, textX + textWidth + 4, centerY + 12, 0x80000000);

        // プレイ画面と同じdrawCenteredStringを使用
        guiGraphics.drawCenteredString(this.font, dismountComponent, centerX, centerY, 0xFFFFFF);
    }
    
    /**
     * Boss Bar HUD要素のサンプル表示
     */
    private void renderBossBarHUD(GuiGraphics guiGraphics, Player player, int x, int y) {
        // デバッグ: Boss Bar描画位置を確認
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("🎯 Boss Bar rendering at: x=" + x + ", y=" + y);
        }
        
        // バニラのBossHealthOverlayと同じ方法でエンダードラゴン風ボスバーを描画
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        // エンダードラゴンの設定: PINK (4), NOTCHED_10 (3)
        int colorOrdinal = 4;  // BossBarColor.PINK.ordinal()
        int overlayOrdinal = 3;  // BossBarOverlay.NOTCHED_10.ordinal()
        
        // 1. 背景バー描画（バニラの方式）
        guiGraphics.blit(GUI_BARS_LOCATION, x, y, 0, colorOrdinal * 5 * 2, 182, 5);
        
        // 2. プログレスバー描画（100%）
        int progress = 182;  // フルHP
        if (progress > 0) {
            guiGraphics.blit(GUI_BARS_LOCATION, x, y, 0, colorOrdinal * 5 * 2 + 5, progress, 5);
        }
        
        // 3. オーバーレイ（NOTCHED_10の切り込み効果）
        RenderSystem.enableBlend();
        guiGraphics.blit(GUI_BARS_LOCATION, x, y, 0, 80 + (overlayOrdinal - 1) * 5 * 2, 182, 5);
        if (progress > 0) {
            guiGraphics.blit(GUI_BARS_LOCATION, x, y, 0, 80 + (overlayOrdinal - 1) * 5 * 2 + 5, progress, 5);
        }
        RenderSystem.disableBlend();
        
        // 4. サンプルボス名を表示
        String bossName = Component.translatable("hud.universalhudmanager.preview.boss_name").getString();
        int nameWidth = this.font.width(bossName);
        int nameX = x + 91 - nameWidth / 2;  // Boss Barの中央に配置
        int nameY = y - 9;  // Boss Barの上に表示（バニラと同じ -9）
        
        // ボス名を白色で描画
        guiGraphics.drawString(this.font, bossName, nameX, nameY, 16777215);
    }
    
    
    /**
     * クロスヘアAttack Indicator HUD描画（編集画面用サンプル）
     * 16x4サイズのバーで半透明効果付き
     */
    private void renderCrosshairAttackIndicatorHUD(GuiGraphics guiGraphics, Player player, int x, int y) {
        // デバッグ: Crosshair Attack Indicator描画位置を確認
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("🎯 Crosshair Attack Indicator rendering at: x=" + x + ", y=" + y);
        }
        
        // バニラのAttack Indicatorテクスチャを使用（編集画面用サンプル）
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        // クロスヘア版の半透明ブレンド設定（HUDPositionHandlerと同じ）
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, 
            com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, 
            com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE, 
            com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO
        );
        
        // デモ用: 75%チャージ状態のクロスヘア版Attack Indicatorを表示
        // Background (empty bar) - 16x4サイズ
        guiGraphics.blit(ICONS, x, y, 36, 94, 16, 4);
        
        // Progress (75% filled) - 16の75% = 12px
        int progress = 12;
        guiGraphics.blit(ICONS, x, y, 52, 94, progress, 4);
        
        // デフォルトブレンド設定に戻す
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        
        // サンプルラベル削除（座標表示との重複を避けるため）
        // 75%表示は不要 - HUD編集では座標表示で十分
    }
    
    /**
     * ホットバーAttack Indicator HUD描画（編集画面用サンプル）
     * 18x18サイズのアイコンで半透明効果なし
     */
    private void renderHotbarAttackIndicatorHUD(GuiGraphics guiGraphics, Player player, int x, int y) {
        // デバッグ: Hotbar Attack Indicator描画位置を確認
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("🎯 Hotbar Attack Indicator rendering at: x=" + x + ", y=" + y);
        }
        
        // バニラのAttack Indicatorテクスチャを使用（編集画面用サンプル）
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        // ホットバー版は半透明効果なし（通常の描画）
        
        // デモ用: 75%チャージ状態のホットバー版Attack Indicatorを表示
        // Background icon - 18x18サイズ
        guiGraphics.blit(ICONS, x, y, 0, 94, 18, 18);
        
        // Progress (75% filled) - 18の75% = 13.5 → 14px
        float attackStrength = 0.75F;
        int progressHeight = (int)(attackStrength * 18.0F);
        guiGraphics.blit(ICONS, x, y + 18 - progressHeight, 18, 112 - progressHeight, 18, progressHeight);
        
        // サンプルラベル削除（座標表示との重複を避けるため）
        // 75%表示は不要 - HUD編集では座標表示で十分
    }
    
    
    /**
     * 現在のAttack Indicator設定モードを取得（String版）
     */
    private String getCurrentAttackIndicatorMode() {
        if (this.minecraft != null && this.minecraft.options != null) {
            net.minecraft.client.AttackIndicatorStatus status = this.minecraft.options.attackIndicator().get();
            switch (status) {
                case CROSSHAIR: return "Crosshair";
                case HOTBAR: return "Hotbar";
                case OFF: return "Off";
                default: return "Unknown";
            }
        }
        return "Unknown";
    }
    
    /**
     * 現在のAttack Indicator設定モードを取得（enum版）
     */
    private AttackIndicatorStatus getCurrentAttackIndicatorStatus() {
        if (this.minecraft != null && this.minecraft.options != null) {
            return this.minecraft.options.attackIndicator().get();
        }
        return AttackIndicatorStatus.OFF;  // デフォルトはOFF
    }
    
    /**
     * 中央クロスライン描画（HUD編集用ガイドライン）
     */
    private void renderCenterCrossLines(GuiGraphics guiGraphics) {
        int centerX = width / 2;
        int centerY = height / 2;
        
        // 半透明の白色ライン（薄めで邪魔にならないように）
        int lineColor = 0x40FFFFFF;  // アルファ値0x40で薄い白色
        
        // 縦線（画面上部から下部まで）
        guiGraphics.fill(centerX - 1, 0, centerX, height, lineColor);
        
        // 横線（画面左端から右端まで）
        guiGraphics.fill(0, centerY, width, centerY + 1, lineColor);
        
        // 中央クロスポイントを少し濃い色で強調
        int crossColor = 0x80FFFFFF;  // 少し濃い白色
        guiGraphics.fill(centerX - 2, centerY - 1, centerX + 1, centerY + 2, crossColor);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false; // ゲームを一時停止しない
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Tab (keyCode=258): ⚙設定ボタンの表示をトグル
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_TAB) {
            showConfigButton = !showConfigButton;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * 全HUD要素の位置を初期値 (0, 0) にリセットして保存
     */
    private void resetAllPositions() {
        Vector2i zero = new Vector2i(0, 0);
        HUDConfig.setHealthPosition(zero);
        HUDConfig.setArmorPosition(zero);
        HUDConfig.setFoodPosition(zero);
        HUDConfig.setAirPosition(zero);
        HUDConfig.setExperiencePosition(zero);
        HUDConfig.setHotbarPosition(zero);
        HUDConfig.setItemNamePosition(zero);
        HUDConfig.setEffectsPosition(zero);
        HUDConfig.setVehicleHealthPosition(zero);
        HUDConfig.setJumpMeterPosition(zero);
        HUDConfig.setDismountMessagePosition(zero);
        HUDConfig.setBossBarPosition(zero);
        HUDConfig.setCrosshairAttackIndicatorPosition(zero);
        HUDConfig.setHotbarAttackIndicatorPosition(zero);
        HUDConfig.SPEC.save();
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("🔄 All HUD positions reset to default (0, 0)");
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        currentInstance = null;
        temporaryDragOffsets.clear();
        if (HUDConfig.DEBUG_MODE.get()) {
            System.out.println("💾 HUD Edit Screen closed - settings saved");
        }
    }
    
    /**
     * 編集モードがアクティブかどうかを確認
     */
    public static boolean isEditModeActive() {
        return currentInstance != null;
    }
    
    /**
     * 一時ドラッグオフセットを取得
     */
    public static Vector2i getTemporaryDragOffset(String elementId) {
        return temporaryDragOffsets.get(elementId);
    }
    
    /**
     * 一時ドラッグオフセットを設定
     */
    public static void setTemporaryDragOffset(String elementId, Vector2i offset) {
        if (offset == null) {
            temporaryDragOffsets.remove(elementId);
        } else {
            temporaryDragOffsets.put(elementId, offset);
        }
    }
}