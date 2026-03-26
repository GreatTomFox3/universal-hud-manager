package com.greattomfoxsora.universalhudmanager.client;

import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Universal HUD Manager - Config Screen
 * スクロールリスト + 3カテゴリ + Save/Default/Back
 *
 * @author GreatTomFox & Sora
 */
public class ConfigScreen extends Screen {

    private final Screen parent;

    // カテゴリ: 0=HUD Visibility, 1=Display Options, 2=General
    private int currentCategory = 0;

    // 一時変数: config.set() はSave時だけ呼ぶ
    private final Map<String, Boolean> pendingValues = new HashMap<>();
    private boolean isDirty = false;

    // UI parts
    private Button tabVisibility;
    private Button tabDisplayOptions;
    private Button tabGeneral;
    private Button btnSave;
    private Button btnDefault;
    private Button btnBack;
    private SettingsList settingsList;

    // スクロールエリアの上下
    private static final int LIST_TOP = 48;
    private static final int LIST_BOTTOM_OFFSET = 36;

    // -------------------------
    // 設定エントリ定義 (tooltip は Display Options のみ使用)
    // -------------------------
    private record SettingEntry(
        String key,
        String label,
        ForgeConfigSpec.BooleanValue config,
        boolean defaultValue,
        String tooltip   // null = ツールチップなし
    ) {
        // tooltip・defaultValueなし用ショートカット（デフォルトtrue）
        SettingEntry(String key, String label, ForgeConfigSpec.BooleanValue config) {
            this(key, label, config, true, null);
        }
        // defaultValue指定あり、tooltipなし
        SettingEntry(String key, String label, ForgeConfigSpec.BooleanValue config, boolean defaultValue) {
            this(key, label, config, defaultValue, null);
        }
    }

    private static final String LANG_PREFIX = "setting.universalhudmanager.";

    private static final List<SettingEntry> VISIBILITY_ENTRIES = List.of(
        new SettingEntry("health",          LANG_PREFIX + "health",          HUDConfig.HEALTH_ENABLED,           true),
        new SettingEntry("armor",           LANG_PREFIX + "armor",           HUDConfig.ARMOR_ENABLED,            true),
        new SettingEntry("food",            LANG_PREFIX + "food",            HUDConfig.FOOD_ENABLED,             true),
        new SettingEntry("air",             LANG_PREFIX + "air",             HUDConfig.AIR_ENABLED,              true),
        new SettingEntry("experience",      LANG_PREFIX + "experience",      HUDConfig.EXPERIENCE_ENABLED,       true),
        new SettingEntry("hotbar",          LANG_PREFIX + "hotbar",          HUDConfig.HOTBAR_ENABLED,           true),
        new SettingEntry("item_name",       LANG_PREFIX + "item_name",       HUDConfig.ITEM_NAME_ENABLED,        true),
        new SettingEntry("effects",         LANG_PREFIX + "effects",         HUDConfig.EFFECTS_ENABLED,          true),
        new SettingEntry("vehicle_health",  LANG_PREFIX + "vehicle_health",  HUDConfig.VEHICLE_HEALTH_ENABLED,   true),
        new SettingEntry("jump_meter",      LANG_PREFIX + "jump_meter",      HUDConfig.JUMP_METER_ENABLED,       true),
        new SettingEntry("dismount_msg",    LANG_PREFIX + "dismount_msg",    HUDConfig.DISMOUNT_MESSAGE_ENABLED, true),
        new SettingEntry("boss_bar",        LANG_PREFIX + "boss_bar",        HUDConfig.BOSS_BAR_ENABLED,         true),
        new SettingEntry("attack_indicator",LANG_PREFIX + "attack_indicator",HUDConfig.ATTACK_INDICATOR_ENABLED, true)
    );

    private static final List<SettingEntry> DISPLAY_ENTRIES = List.of(
        new SettingEntry("armor_empty",      LANG_PREFIX + "armor_empty",      HUDConfig.ARMOR_EMPTY_DISPLAY,          true,  LANG_PREFIX + "armor_empty.tooltip"),
        new SettingEntry("food_dir",         LANG_PREFIX + "food_dir",         HUDConfig.FOOD_DECREASE_LEFT_TO_RIGHT,  false, LANG_PREFIX + "food_dir.tooltip"),
        new SettingEntry("health_flash",     LANG_PREFIX + "health_flash",     HUDConfig.HEALTH_FLASH_ON_RECOVERY,     true,  LANG_PREFIX + "health_flash.tooltip"),
        new SettingEntry("health_wave",      LANG_PREFIX + "health_wave",      HUDConfig.HEALTH_WHITE_WAVE_ANIMATION,  false, LANG_PREFIX + "health_wave.tooltip"),
        new SettingEntry("sep_vehicle",      LANG_PREFIX + "sep_vehicle",      HUDConfig.SEPARATE_VEHICLE_HEALTH,      false, LANG_PREFIX + "sep_vehicle.tooltip"),
        new SettingEntry("sep_jump",         LANG_PREFIX + "sep_jump",         HUDConfig.SEPARATE_JUMP_METER,          false, LANG_PREFIX + "sep_jump.tooltip"),
        new SettingEntry("as_saturation",    LANG_PREFIX + "as_saturation",    HUDConfig.APPLESKIN_SATURATION,         true,  LANG_PREFIX + "as_saturation.tooltip"),
        new SettingEntry("as_exhaustion",    LANG_PREFIX + "as_exhaustion",    HUDConfig.APPLESKIN_EXHAUSTION,         true,  LANG_PREFIX + "as_exhaustion.tooltip"),
        new SettingEntry("as_hunger",        LANG_PREFIX + "as_hunger",        HUDConfig.APPLESKIN_HUNGER_RESTORED,    true,  LANG_PREFIX + "as_hunger.tooltip"),
        new SettingEntry("as_health",        LANG_PREFIX + "as_health",        HUDConfig.APPLESKIN_HEALTH_RESTORED,    true,  LANG_PREFIX + "as_health.tooltip")
    );

    private static final List<SettingEntry> GENERAL_ENTRIES = List.of(
        new SettingEntry("debug", LANG_PREFIX + "debug", HUDConfig.DEBUG_MODE, false)
    );

    // -------------------------
    // Constructor
    // -------------------------
    public ConfigScreen(Screen parent) {
        super(Component.translatable("screen.universalhudmanager.config.title"));
        this.parent = parent;
    }

    // -------------------------
    // init
    // -------------------------
    @Override
    protected void init() {
        super.init();

        // pendingValues を現在のconfig値で初期化（isDirty=trueのときは既存の値を保持）
        if (!isDirty) {
            pendingValues.clear();
            for (SettingEntry e : VISIBILITY_ENTRIES) pendingValues.put(e.key(), e.config().get());
            for (SettingEntry e : DISPLAY_ENTRIES)    pendingValues.put(e.key(), e.config().get());
            for (SettingEntry e : GENERAL_ENTRIES)    pendingValues.put(e.key(), e.config().get());
        }

        // ---- タブボタン ----
        int tabY = 22;
        int tabW = (this.width - 20) / 3;
        tabVisibility = Button.builder(Component.translatable("tab.universalhudmanager.visibility"),
                btn -> switchCategory(0))
                .bounds(10, tabY, tabW, 20).build();
        tabDisplayOptions = Button.builder(Component.translatable("tab.universalhudmanager.display_options"),
                btn -> switchCategory(1))
                .bounds(10 + tabW, tabY, tabW, 20).build();
        tabGeneral = Button.builder(Component.translatable("tab.universalhudmanager.general"),
                btn -> switchCategory(2))
                .bounds(10 + tabW * 2, tabY, tabW, 20).build();
        this.addRenderableWidget(tabVisibility);
        this.addRenderableWidget(tabDisplayOptions);
        this.addRenderableWidget(tabGeneral);

        // ---- 下部ボタン行 ----
        int bottomY = this.height - 28;
        btnDefault = Button.builder(Component.translatable("button.universalhudmanager.default"),
                btn -> onDefaultPressed())
                .bounds(10, bottomY, 80, 20).build();
        btnSave = Button.builder(Component.translatable("button.universalhudmanager.save"),
                btn -> onSavePressed())
                .bounds(this.width / 2 - 40, bottomY, 80, 20).build();
        btnBack = Button.builder(Component.translatable("button.universalhudmanager.back"),
                btn -> onBackPressed())
                .bounds(this.width - 90, bottomY, 80, 20).build();
        this.addRenderableWidget(btnDefault);
        this.addRenderableWidget(btnSave);
        this.addRenderableWidget(btnBack);

        rebuildList();
        updateSaveButton();
    }

    // -------------------------
    // スクロールリスト再構築
    // -------------------------
    private void rebuildList() {
        if (settingsList != null) {
            this.removeWidget(settingsList);
        }
        List<SettingEntry> entries = switch (currentCategory) {
            case 1  -> DISPLAY_ENTRIES;
            case 2  -> GENERAL_ENTRIES;
            default -> VISIBILITY_ENTRIES;
        };
        settingsList = new SettingsList(entries);
        this.addWidget(settingsList);
    }

    // -------------------------
    // カテゴリ切り替え
    // -------------------------
    private void switchCategory(int category) {
        currentCategory = category;
        rebuildList();
    }

    // -------------------------
    // Save / Default / Back
    // -------------------------
    private void onSavePressed() {
        applyPendingToConfig();
        isDirty = false;
        updateSaveButton();
    }

    private void onDefaultPressed() {
        this.minecraft.setScreen(new ConfirmScreen(
            confirmed -> {
                if (confirmed) {
                    // init()が呼ばれる前にpendingValuesをデフォルト値で上書き
                    for (SettingEntry e : VISIBILITY_ENTRIES) pendingValues.put(e.key(), getSpecDefault(e.config()));
                    for (SettingEntry e : DISPLAY_ENTRIES)    pendingValues.put(e.key(), getSpecDefault(e.config()));
                    for (SettingEntry e : GENERAL_ENTRIES)    pendingValues.put(e.key(), getSpecDefault(e.config()));
                    isDirty = true;
                }
                // setScreen → init() が呼ばれるが、isDirty=trueの間はpendingValuesを保持する
                this.minecraft.setScreen(ConfigScreen.this);
            },
            Component.translatable("confirm.universalhudmanager.reset_default.title"),
            Component.translatable("confirm.universalhudmanager.reset_default.message"),
            Component.translatable("confirm.universalhudmanager.reset_default.yes"),
            Component.translatable("confirm.universalhudmanager.reset_default.no")
        ));
    }

    private void onBackPressed() {
        if (isDirty) {
            this.minecraft.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) applyPendingToConfig();
                    this.minecraft.setScreen(parent);
                },
                Component.translatable("confirm.universalhudmanager.unsaved.title"),
                Component.translatable("confirm.universalhudmanager.unsaved.message"),
                Component.translatable("confirm.universalhudmanager.unsaved.yes"),
                Component.translatable("confirm.universalhudmanager.unsaved.no")
            ));
        } else {
            this.minecraft.setScreen(parent);
        }
    }

    private void applyPendingToConfig() {
        for (SettingEntry e : VISIBILITY_ENTRIES) e.config().set(pendingValues.get(e.key()));
        for (SettingEntry e : DISPLAY_ENTRIES)    e.config().set(pendingValues.get(e.key()));
        for (SettingEntry e : GENERAL_ENTRIES)    e.config().set(pendingValues.get(e.key()));
    }

    private void resetToDefaults() {
        for (SettingEntry e : VISIBILITY_ENTRIES) pendingValues.put(e.key(), getSpecDefault(e.config()));
        for (SettingEntry e : DISPLAY_ENTRIES)    pendingValues.put(e.key(), getSpecDefault(e.config()));
        for (SettingEntry e : GENERAL_ENTRIES)    pendingValues.put(e.key(), getSpecDefault(e.config()));
        isDirty = true;
        updateSaveButton();
        rebuildList();
    }

    private void updateSaveButton() {
        btnSave.active = isDirty;
    }

    /**
     * ForgeConfigSpec.getRaw(path) で ValueSpec を取得してデフォルト値を返す
     * Configured mod の ForgeConfigHelper.gatherValuesFromForgeConfig() と同じアプローチ
     */
    private boolean getSpecDefault(ForgeConfigSpec.BooleanValue configValue) {
        try {
            ForgeConfigSpec.ValueSpec valueSpec = HUDConfig.SPEC.getRaw(configValue.getPath());
            return (Boolean) valueSpec.getDefault();
        } catch (Exception e) {
            System.out.println("⚠️ getSpecDefault failed: " + e.getMessage());
            return true;
        }
    }

    @Override
    public void onClose() {
        onBackPressed();
    }

    // -------------------------
    // render
    // -------------------------
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(g);

        settingsList.render(g, mouseX, mouseY, partialTicks);

        // タイトル
        g.drawCenteredString(this.font, "UHM Settings", this.width / 2, 6, 0xFFFFFF);

        // 境界線
        g.fill(0, LIST_TOP - 2,                        this.width, LIST_TOP - 1,                        0xFF888888);
        g.fill(0, this.height - LIST_BOTTOM_OFFSET,    this.width, this.height - LIST_BOTTOM_OFFSET + 1, 0xFF888888);

        // アクティブタブ下線
        Button activeTab = switch (currentCategory) {
            case 1  -> tabDisplayOptions;
            case 2  -> tabGeneral;
            default -> tabVisibility;
        };
        g.fill(activeTab.getX(), activeTab.getY() + activeTab.getHeight(),
               activeTab.getX() + activeTab.getWidth(), activeTab.getY() + activeTab.getHeight() + 2,
               0xFFFFFFFF);

        super.render(g, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // =========================================================
    // Inner class: スクロールリスト
    // =========================================================
    class SettingsList extends ObjectSelectionList<SettingsList.Entry> {

        SettingsList(List<SettingEntry> entries) {
            super(ConfigScreen.this.minecraft,
                  ConfigScreen.this.width,
                  ConfigScreen.this.height,
                  LIST_TOP,
                  ConfigScreen.this.height - LIST_BOTTOM_OFFSET,
                  24);
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
            for (SettingEntry e : entries) {
                this.addEntry(new Entry(e));
            }
        }

        @Override
        protected int getScrollbarPosition() {
            return ConfigScreen.this.width - 8;
        }

        @Override
        public int getRowWidth() {
            // 中央に固定幅のコンテンツエリアを作る（左右に空きが生まれる）
            return Math.min(240, ConfigScreen.this.width - 80);
        }

        // ---- 各行 ----
        class Entry extends ObjectSelectionList.Entry<Entry> {
            private final SettingEntry setting;

            // ON/OFFバッジの寸法
            private static final int BADGE_W = 28;
            private static final int BADGE_H = 12;

            Entry(SettingEntry setting) {
                this.setting = setting;
            }

            @Override
            public void render(GuiGraphics g, int index, int top, int left,
                               int width, int height, int mouseX, int mouseY,
                               boolean isHovered, float partialTick) {

                // ---- ON/OFFバッジ位置計算 ----
                int badgeX = left + width - BADGE_W - 4;
                int badgeY = top + (height - BADGE_H) / 2;

                // ---- ホバー時: 行全体を外から囲む枠線 ----
                if (isHovered) {
                    drawBorder(g, left - 2, top, badgeX + BADGE_W + 2, top + height, 0xAAFFFFFF);
                }

                // ---- ラベル左揃え ----
                int labelY = top + (height - 8) / 2;
                g.drawString(ConfigScreen.this.font, Component.translatable(setting.label()), left + 4, labelY, 0xFFFFFF, false);

                // ---- ON/OFFバッジ（白枠のみ、背景なし） ----
                boolean value = pendingValues.get(setting.key());
                int badgeColor = value ? 0xFF55FF55 : 0xFFFF5555; // 緑/赤
                drawBorder(g, badgeX, badgeY, badgeX + BADGE_W, badgeY + BADGE_H, badgeColor);
                String stateText = value ? "ON" : "OFF";
                int textX = badgeX + (BADGE_W - ConfigScreen.this.font.width(stateText)) / 2;
                int textY = badgeY + (BADGE_H - 8) / 2;
                g.drawString(ConfigScreen.this.font, stateText, textX, textY, badgeColor, false);

                // ---- ホバー時: 右の空きに説明文 ----
                if (isHovered && setting.tooltip() != null) {
                    int tooltipX = left + width + 10;
                    int tooltipAreaWidth = ConfigScreen.this.width - tooltipX - 10;
                    if (tooltipAreaWidth > 60) {
                        java.util.List<net.minecraft.util.FormattedCharSequence> lines =
                            ConfigScreen.this.font.split(
                                Component.translatable(setting.tooltip()),
                                tooltipAreaWidth
                            );
                        int lineY = top + (height - lines.size() * 10) / 2;
                        for (net.minecraft.util.FormattedCharSequence line : lines) {
                            g.drawString(ConfigScreen.this.font, line, tooltipX, lineY, 0xAAAAAA, false);
                            lineY += 10;
                        }
                    }
                }
            }

            /** 1px枠線を描画するヘルパー */
            private void drawBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
                g.fill(x1,     y1,     x2,     y1 + 1, color); // 上
                g.fill(x1,     y2 - 1, x2,     y2,     color); // 下
                g.fill(x1,     y1,     x1 + 1, y2,     color); // 左
                g.fill(x2 - 1, y1,     x2,     y2,     color); // 右
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    // 行全体クリックでトグル
                    boolean newVal = !pendingValues.get(setting.key());
                    pendingValues.put(setting.key(), newVal);
                    isDirty = true;
                    updateSaveButton();
                    minecraft.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
                    );
                    return true;
                }
                return false;
            }

            @Override
            public Component getNarration() {
                return Component.translatable(setting.label());
            }
        }
    }
}
