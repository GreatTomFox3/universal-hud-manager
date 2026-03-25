package com.greattomfoxsora.universalhudmanager.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.joml.Vector2i;

import java.util.Arrays;
import java.util.List;

/**
 * シンプルなHUD設定システム - アーマーHUD方式ベース
 * Health + Armor HUDの2個のみに集中
 * 
 * @author GreatTomFox & Sora
 */
public class HUDConfig {
    
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    
    // Position configurations - Health、Armor、Food、Air、Experience、Hotbar、ItemName、Effects、VehicleHealth、JumpMeter、DismountMessage、BossBar、AttackIndicator
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> HEALTH_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ARMOR_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> FOOD_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> AIR_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> EXPERIENCE_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> HOTBAR_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ITEM_NAME_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> EFFECTS_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> VEHICLE_HEALTH_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> JUMP_METER_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> DISMOUNT_MESSAGE_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> BOSS_BAR_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> ATTACK_INDICATOR_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> CROSSHAIR_ATTACK_INDICATOR_POS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> HOTBAR_ATTACK_INDICATOR_POS;
    
    // Enable/disable configurations
    public static final ForgeConfigSpec.BooleanValue HEALTH_ENABLED;
    public static final ForgeConfigSpec.BooleanValue ARMOR_ENABLED;
    public static final ForgeConfigSpec.BooleanValue FOOD_ENABLED;
    public static final ForgeConfigSpec.BooleanValue AIR_ENABLED;
    public static final ForgeConfigSpec.BooleanValue EXPERIENCE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue HOTBAR_ENABLED;
    public static final ForgeConfigSpec.BooleanValue ITEM_NAME_ENABLED;
    public static final ForgeConfigSpec.BooleanValue EFFECTS_ENABLED;
    public static final ForgeConfigSpec.BooleanValue VEHICLE_HEALTH_ENABLED;
    public static final ForgeConfigSpec.BooleanValue JUMP_METER_ENABLED;
    public static final ForgeConfigSpec.BooleanValue DISMOUNT_MESSAGE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue BOSS_BAR_ENABLED;
    public static final ForgeConfigSpec.BooleanValue ATTACK_INDICATOR_ENABLED;
    
    // Armor display settings
    public static final ForgeConfigSpec.BooleanValue ARMOR_EMPTY_DISPLAY;
    
    // Food display settings
    public static final ForgeConfigSpec.BooleanValue FOOD_DECREASE_LEFT_TO_RIGHT;
    
    // Health display settings
    public static final ForgeConfigSpec.BooleanValue HEALTH_FLASH_ON_RECOVERY;
    public static final ForgeConfigSpec.BooleanValue HEALTH_WHITE_WAVE_ANIMATION;
    
    // Global settings
    public static final ForgeConfigSpec.BooleanValue HUD_EDIT_MODE;
    
    // Vehicle HUD settings
    public static final ForgeConfigSpec.BooleanValue SEPARATE_VEHICLE_HEALTH;
    public static final ForgeConfigSpec.BooleanValue SEPARATE_JUMP_METER;
    
    // デバッグは最小限に
    public static final ForgeConfigSpec.BooleanValue DEBUG_MODE;

    // AppleSkin 互換機能
    public static final ForgeConfigSpec.BooleanValue APPLESKIN_SATURATION;
    public static final ForgeConfigSpec.BooleanValue APPLESKIN_EXHAUSTION;
    public static final ForgeConfigSpec.BooleanValue APPLESKIN_HUNGER_RESTORED;
    public static final ForgeConfigSpec.BooleanValue APPLESKIN_HEALTH_RESTORED;
    
    static {
        BUILDER.push("hud_positions");
        
        // Health Bar position
        HEALTH_POS = BUILDER
                .comment("Health bar position offset [x, y]")
                .defineList("health_position", Arrays.asList(0, 0), 
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        // Armor Bar position
        ARMOR_POS = BUILDER
                .comment("Armor bar position offset [x, y]")
                .defineList("armor_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        // Food Bar position
        FOOD_POS = BUILDER
                .comment("Food bar position offset [x, y]")
                .defineList("food_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        // Air Bar position
        AIR_POS = BUILDER
                .comment("Air bar position offset [x, y]")
                .defineList("air_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        // Experience Bar position
        EXPERIENCE_POS = BUILDER
                .comment("Experience bar position offset [x, y]")
                .defineList("experience_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        // Hotbar position
        HOTBAR_POS = BUILDER
                .comment("Hotbar position offset [x, y]")
                .defineList("hotbar_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        // Item Name position
        ITEM_NAME_POS = BUILDER
                .comment("Item name display position offset [x, y]")
                .defineList("item_name_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        // Effects position
        EFFECTS_POS = BUILDER
                .comment("Effects display position offset [x, y]")
                .defineList("effects_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        // Vehicle Health position
        VEHICLE_HEALTH_POS = BUILDER
                .comment("Vehicle health display position offset [x, y]")
                .defineList("vehicle_health_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        // Jump Meter position
        JUMP_METER_POS = BUILDER
                .comment("Jump meter display position offset [x, y]")
                .defineList("jump_meter_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        // Dismount Message position
        DISMOUNT_MESSAGE_POS = BUILDER
                .comment("Dismount message display position offset [x, y]")
                .defineList("dismount_message_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        // Boss Bar position
        BOSS_BAR_POS = BUILDER
                .comment("Boss bar display position offset [x, y]")
                .defineList("boss_bar_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        // Attack Indicator positions - クロスヘア版とホットバー版を分離
        ATTACK_INDICATOR_POS = BUILDER
                .comment("Attack cooldown indicator position offset [x, y] - Legacy setting (deprecated, use crosshair/hotbar specific)")
                .defineList("attack_indicator_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        // クロスヘア版Attack Indicator位置（独立設定）
        CROSSHAIR_ATTACK_INDICATOR_POS = BUILDER
                .comment("Crosshair Attack Indicator position offset [x, y] (independent from hotbar version)")
                .defineList("crosshair_attack_indicator_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        // ホットバー版Attack Indicator位置（独立設定）
        HOTBAR_ATTACK_INDICATOR_POS = BUILDER
                .comment("Hotbar Attack Indicator position offset [x, y] (independent from crosshair version)")
                .defineList("hotbar_attack_indicator_position", Arrays.asList(0, 0),
                           obj -> obj instanceof Integer && (Integer)obj >= -1000 && (Integer)obj <= 1000);
        
        
        BUILDER.pop();
        
        BUILDER.push("hud_enabled");
        
        // Enable/disable settings
        HEALTH_ENABLED = BUILDER
                .comment("Enable custom health bar positioning")
                .define("health_enabled", true);
        
        ARMOR_ENABLED = BUILDER
                .comment("Enable custom armor bar positioning")
                .define("armor_enabled", true);
        
        FOOD_ENABLED = BUILDER
                .comment("Enable custom food bar positioning")
                .define("food_enabled", true);
        
        AIR_ENABLED = BUILDER
                .comment("Enable custom air bar positioning")
                .define("air_enabled", true);
        
        EXPERIENCE_ENABLED = BUILDER
                .comment("Enable custom experience bar positioning")
                .define("experience_enabled", true);
        
        HOTBAR_ENABLED = BUILDER
                .comment("Enable custom hotbar positioning")
                .define("hotbar_enabled", true);
        
        ITEM_NAME_ENABLED = BUILDER
                .comment("Enable custom item name display positioning")
                .define("item_name_enabled", true);
        
        EFFECTS_ENABLED = BUILDER
                .comment("Enable custom effects display positioning")
                .define("effects_enabled", true);
        
        VEHICLE_HEALTH_ENABLED = BUILDER
                .comment("Enable custom vehicle health positioning")
                .define("vehicle_health_enabled", true);
        
        JUMP_METER_ENABLED = BUILDER
                .comment("Enable custom jump meter positioning")
                .define("jump_meter_enabled", true);
        
        DISMOUNT_MESSAGE_ENABLED = BUILDER
                .comment("Enable custom dismount message positioning")
                .define("dismount_message_enabled", true);
        
        BOSS_BAR_ENABLED = BUILDER
                .comment("Enable custom boss bar positioning")
                .define("boss_bar_enabled", true);
        
        ATTACK_INDICATOR_ENABLED = BUILDER
                .comment("Enable custom attack cooldown indicator positioning (follows vanilla AttackIndicatorStatus setting)")
                .define("attack_indicator_enabled", true);
        
        
        BUILDER.pop();
        
        BUILDER.push("armor_display");
        
        // Armor display settings
        ARMOR_EMPTY_DISPLAY = BUILDER
                .comment("Show empty armor bar background (vanilla-like behavior)")
                .define("armor_empty_display", true);
        
        BUILDER.pop();
        
        BUILDER.push("food_display");
        
        // Food display settings
        FOOD_DECREASE_LEFT_TO_RIGHT = BUILDER
                .comment("Food bar decreases from left to right (false = vanilla right to left, true = left to right like health)")
                .define("food_decrease_left_to_right", false);
        
        BUILDER.pop();
        
        BUILDER.push("health_display");
        
        // Health display settings
        HEALTH_FLASH_ON_RECOVERY = BUILDER
                .comment("Show flash effect when health regenerates")
                .define("health_flash_on_recovery", true);
        
        HEALTH_WHITE_WAVE_ANIMATION = BUILDER
                .comment("Use white wave animation instead of heart bounce (original UHM feature)")
                .define("health_white_wave_animation", false);
        
        BUILDER.pop();
        
        BUILDER.push("general");
        
        // General settings
        HUD_EDIT_MODE = BUILDER
                .comment("Enable HUD edit mode (shows draggable elements)")
                .define("hud_edit_mode", false);
        
        DEBUG_MODE = BUILDER
                .comment("Enable debug logging (default: false)")
                .define("debug_mode", false);
        
        BUILDER.pop();
        
        BUILDER.push("vehicle_hud");
        
        // Vehicle HUD settings
        SEPARATE_VEHICLE_HEALTH = BUILDER
                .comment("Separate vehicle health from food bar (false = vanilla-like replacement, true = separate positioning)")
                .define("separate_vehicle_health", false);
        
        SEPARATE_JUMP_METER = BUILDER
                .comment("Separate jump meter from experience bar (false = vanilla-like replacement, true = separate positioning)")
                .define("separate_jump_meter", false);
        
        BUILDER.pop();

        BUILDER.push("appleskin_compat");

        APPLESKIN_SATURATION = BUILDER
                .comment("Show saturation overlay on food bar (AppleSkin-style)")
                .define("saturation_overlay", true);

        APPLESKIN_EXHAUSTION = BUILDER
                .comment("Show exhaustion underlay on food bar (AppleSkin-style)")
                .define("exhaustion_overlay", true);

        APPLESKIN_HUNGER_RESTORED = BUILDER
                .comment("Show hunger restored preview when holding food (AppleSkin-style)")
                .define("hunger_restored_overlay", true);

        APPLESKIN_HEALTH_RESTORED = BUILDER
                .comment("Show health restored preview when holding food (AppleSkin-style)")
                .define("health_restored_overlay", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
    
    // Utility methods
    
    /**
     * Get Vector2i position from config value
     */
    public static Vector2i getPosition(ForgeConfigSpec.ConfigValue<List<? extends Integer>> configValue) {
        List<? extends Integer> list = configValue.get();
        if (list.size() >= 2) {
            return new Vector2i(list.get(0), list.get(1));
        }
        return new Vector2i(0, 0);
    }
    
    /**
     * Set Vector2i position to config value
     */
    public static void setPosition(ForgeConfigSpec.ConfigValue<List<? extends Integer>> configValue, Vector2i position) {
        configValue.set(Arrays.asList(position.x, position.y));
    }
    
    // Health Bar convenience methods
    public static Vector2i getHealthPosition() {
        return getPosition(HEALTH_POS);
    }
    
    public static void setHealthPosition(Vector2i position) {
        setPosition(HEALTH_POS, position);
    }
    
    // Armor Bar convenience methods
    public static Vector2i getArmorPosition() {
        return getPosition(ARMOR_POS);
    }
    
    public static void setArmorPosition(Vector2i position) {
        setPosition(ARMOR_POS, position);
    }
    
    // Food Bar convenience methods
    public static Vector2i getFoodPosition() {
        return getPosition(FOOD_POS);
    }
    
    public static void setFoodPosition(Vector2i position) {
        setPosition(FOOD_POS, position);
    }
    
    // Air Bar convenience methods
    public static Vector2i getAirPosition() {
        return getPosition(AIR_POS);
    }
    
    public static void setAirPosition(Vector2i position) {
        setPosition(AIR_POS, position);
    }
    
    // Experience Bar convenience methods
    public static Vector2i getExperiencePosition() {
        return getPosition(EXPERIENCE_POS);
    }
    
    public static void setExperiencePosition(Vector2i position) {
        setPosition(EXPERIENCE_POS, position);
    }
    
    // Hotbar convenience methods
    public static Vector2i getHotbarPosition() {
        return getPosition(HOTBAR_POS);
    }
    
    public static void setHotbarPosition(Vector2i position) {
        setPosition(HOTBAR_POS, position);
    }
    
    // Item Name convenience methods
    public static Vector2i getItemNamePosition() {
        return getPosition(ITEM_NAME_POS);
    }
    
    public static void setItemNamePosition(Vector2i position) {
        setPosition(ITEM_NAME_POS, position);
    }
    
    // Effects convenience methods
    public static Vector2i getEffectsPosition() {
        return getPosition(EFFECTS_POS);
    }
    
    public static void setEffectsPosition(Vector2i position) {
        setPosition(EFFECTS_POS, position);
    }
    
    // Vehicle Health convenience methods
    public static Vector2i getVehicleHealthPosition() {
        return getPosition(VEHICLE_HEALTH_POS);
    }
    
    public static void setVehicleHealthPosition(Vector2i position) {
        setPosition(VEHICLE_HEALTH_POS, position);
    }
    
    // Jump Meter convenience methods
    public static Vector2i getJumpMeterPosition() {
        return getPosition(JUMP_METER_POS);
    }
    
    public static void setJumpMeterPosition(Vector2i position) {
        setPosition(JUMP_METER_POS, position);
    }
    
    // Dismount Message convenience methods
    public static Vector2i getDismountMessagePosition() {
        return getPosition(DISMOUNT_MESSAGE_POS);
    }
    
    public static void setDismountMessagePosition(Vector2i position) {
        setPosition(DISMOUNT_MESSAGE_POS, position);
    }
    
    // Boss Bar convenience methods
    public static Vector2i getBossBarPosition() {
        return getPosition(BOSS_BAR_POS);
    }
    
    public static void setBossBarPosition(Vector2i position) {
        setPosition(BOSS_BAR_POS, position);
    }
    
    // Attack Indicator convenience methods
    public static Vector2i getAttackIndicatorPosition() {
        return getPosition(ATTACK_INDICATOR_POS);
    }
    
    public static void setAttackIndicatorPosition(Vector2i position) {
        setPosition(ATTACK_INDICATOR_POS, position);
    }
    
    // Crosshair Attack Indicator convenience methods
    public static Vector2i getCrosshairAttackIndicatorPosition() {
        return getPosition(CROSSHAIR_ATTACK_INDICATOR_POS);
    }
    
    public static void setCrosshairAttackIndicatorPosition(Vector2i position) {
        setPosition(CROSSHAIR_ATTACK_INDICATOR_POS, position);
    }
    
    // Hotbar Attack Indicator convenience methods
    public static Vector2i getHotbarAttackIndicatorPosition() {
        return getPosition(HOTBAR_ATTACK_INDICATOR_POS);
    }
    
    public static void setHotbarAttackIndicatorPosition(Vector2i position) {
        setPosition(HOTBAR_ATTACK_INDICATOR_POS, position);
    }
    
    
    // Default position calculations - ARMOR HUD SUCCESS PATTERN
    
    /**
     * Health Bar default position - アーマーHUDと同じ固定値方式
     */
    public static Vector2i getDefaultHealthPosition(int screenWidth, int screenHeight) {
        // 🌟 ARMOR HUD PROVEN METHOD: Simple fixed offset values
        int left = screenWidth / 2 - 91;    // Vanilla health position
        int top = screenHeight - 39;        // Vanilla health position
        return new Vector2i(left, top);
    }
    
    /**
     * Armor Bar default position - 実績のある方式をそのまま使用
     */
    public static Vector2i getDefaultArmorPosition(int screenWidth, int screenHeight) {
        Vector2i healthPos = getDefaultHealthPosition(screenWidth, screenHeight);
        return new Vector2i(healthPos.x, healthPos.y - 10); // Health の10px上
    }
    
    /**
     * Food Bar default position - バニラ右側配置
     */
    public static Vector2i getDefaultFoodPosition(int screenWidth, int screenHeight) {
        // バニラのFood Bar位置: 画面右側、Health Barと同じ高さ
        int right = screenWidth / 2 + 10;    // Health Barの反対側
        int top = screenHeight - 39;         // Health Barと同じ高さ
        return new Vector2i(right, top);
    }
    
    /**
     * Air Bar default position - バニラ右側配置（Food Barの下）
     */
    public static Vector2i getDefaultAirPosition(int screenWidth, int screenHeight) {
        // バニラのAir Bar位置: Food Barの下、水中時のみ表示
        Vector2i foodPos = getDefaultFoodPosition(screenWidth, screenHeight);
        return new Vector2i(foodPos.x, foodPos.y - 10); // Food Barの10px上
    }
    
    /**
     * Experience Bar default position - バニラ下部中央配置
     */
    public static Vector2i getDefaultExperiencePosition(int screenWidth, int screenHeight) {
        // バニラのExperience Bar位置: 画面下部中央、ホットバーの上
        int left = screenWidth / 2 - 91;    // 182 / 2 = 91
        int top = screenHeight - 32 + 3;    // バニラのY座標計算
        return new Vector2i(left, top);
    }
    
    /**
     * Hotbar default position - バニラ下部中央配置
     */
    public static Vector2i getDefaultHotbarPosition(int screenWidth, int screenHeight) {
        // バニラのHotbar位置: 画面下部中央
        int left = screenWidth / 2 - 91;    // 182 / 2 = 91
        int top = screenHeight - 22;        // バニラのY座標（GUI.java 484行目）
        return new Vector2i(left, top);
    }
    
    /**
     * Item Name default position - バニラ準拠配置
     */
    public static Vector2i getDefaultItemNamePosition(int screenWidth, int screenHeight) {
        // HudElement用の位置計算（緑枠の中央が画面中央になるように調整）
        int centerX = screenWidth / 2 - 33; // 66px幅の緑枠の中央が画面中央に来るよう調整
        int nameY = screenHeight - 59;      // バニラ準拠のY座標
        
        // スペクテイターモード判定（実装簡略化のため省略、必要に応じて追加）
        // if (!this.minecraft.gameMode.canHurtPlayer()) nameY += 14;
        
        return new Vector2i(centerX, nameY);
    }
    
    /**
     * Effects default position - バニラ準拠右上配置
     */
    public static Vector2i getDefaultEffectsPosition(int screenWidth, int screenHeight) {
        // バニラのEffects位置: 右上（GUI.java 430-442行目準拠）
        int right = screenWidth - 25;  // 最初のエフェクトのX座標
        int top = 1;                    // Y座標
        
        return new Vector2i(right, top);
    }
    
    /**
     * Vehicle Health default position - バニラ準拠右下配置 OR 分離モード時はFoodの上
     */
    public static Vector2i getDefaultVehicleHealthPosition(int screenWidth, int screenHeight) {
        if (SEPARATE_VEHICLE_HEALTH.get()) {
            // 分離モード: Food Barの上に表示（Airが通常ある位置）
            Vector2i foodPos = getDefaultFoodPosition(screenWidth, screenHeight);
            return new Vector2i(foodPos.x, foodPos.y - 10);  // Foodの10px上
        } else {
            // バニラライク: Food Bar位置（馬に乗った時にFoodが馬のHPになる）
            return getDefaultFoodPosition(screenWidth, screenHeight);
        }
    }
    
    /**
     * Jump Meter default position - バニラ準拠中央下配置 OR 分離モード時はボスHP下
     */
    public static Vector2i getDefaultJumpMeterPosition(int screenWidth, int screenHeight) {
        if (SEPARATE_JUMP_METER.get()) {
            // 分離モード: ボスHPバーの下に表示
            int bossBarBottomY = getBossBarBottomY();
            int center = screenWidth / 2 - 91;  // 中央から182pxの半分左
            int top = bossBarBottomY + 12;       // ボスバーの下12px
            return new Vector2i(center, top);
        } else {
            // バニラライク: Experience Bar位置（馬に乗った時にExperienceがジャンプゲージになる）
            return getDefaultExperiencePosition(screenWidth, screenHeight);
        }
    }
    
    /**
     * Dismount Message default position - バニラ準拠中央下部配置
     */
    public static Vector2i getDefaultDismountMessagePosition(int screenWidth, int screenHeight) {
        // バニラのoverlayMessage位置: translate(screenWidth/2, screenHeight-68) + drawString(-4)
        int centerX = screenWidth / 2;        // バニラと同じ中央X座標
        int bottomY = screenHeight - 68 - 4;  // translate Y座標 + drawString Y座標
        return new Vector2i(centerX, bottomY);
    }
    
    /**
     * Boss Bar default position - バニラ準拠上部中央配置
     */
    public static Vector2i getDefaultBossBarPosition(int screenWidth, int screenHeight) {
        // バニラのBoss Bar位置: 上部中央（BossHealthOverlay.java準拠）
        int centerX = screenWidth / 2 - 91;  // 182px幅バーの左端（中央-91px）
        int topY = 12;                       // バニラの標準Y座標
        return new Vector2i(centerX, topY);
    }
    
    /**
     * Attack Indicator default position - バニラ設定に応じてクロスヘア版・ホットバー版の位置
     * ここではクロスヘア版のデフォルト位置を返す（実際の描画時にAttackIndicatorStatusで判定）
     */
    public static Vector2i getDefaultAttackIndicatorPosition(int screenWidth, int screenHeight) {
        // クロスヘア版の位置: クロスヘアの下（GUI.java 376-377行目準拠）
        int centerX = screenWidth / 2 - 8;   // クロスヘア中央-8px（16px幅アイコンの左端）
        int crosshairY = screenHeight / 2 - 7 + 16;  // クロスヘア中央から下へオフセット
        return new Vector2i(centerX, crosshairY);
    }
    
    
    /**
     * ボスHPバーの一番下のY座標を計算（ボス数に応じて動的変化）
     */
    private static int getBossBarBottomY() {
        // ボスバーの基本情報（バニラ準拠）
        int bossBarStartY = 12;        // 最初のボスバーのY位置
        int bossBarHeight = 10;        // ボスバーの高さ
        int bossBarSpacing = 19;       // ボスバー間の間隔
        
        // TODO: 実際のボス数を取得する必要がある
        // 現在はデフォルトでボスなしと仮定
        int activeBossCount = 0;
        
        if (activeBossCount == 0) {
            // ボスがいない場合、最初のボスバー位置を返す
            return bossBarStartY;
        } else {
            // 最後のボスバーの下端を返す
            return bossBarStartY + (activeBossCount - 1) * bossBarSpacing + bossBarHeight;
        }
    }
    
    /**
     * Calculate final position with offset applied
     */
    public static Vector2i getFinalPosition(Vector2i defaultPos, Vector2i offset) {
        return new Vector2i(defaultPos.x + offset.x, defaultPos.y + offset.y);
    }
    
    /**
     * Register config specification
     */
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC, "universalhudmanager-client.toml");
    }
}