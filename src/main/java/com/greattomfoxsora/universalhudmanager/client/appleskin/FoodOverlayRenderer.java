package com.greattomfoxsora.universalhudmanager.client.appleskin;

import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.Random;
import java.util.Vector;

/**
 * AppleSkin の HUD オーバーレイ機能を UHM に移植したクラス。
 * AppleSkin (public domain, squeek502) のコードをベースに改変。
 *
 * UHM の Food/Health バーの座標を受け取り、
 * 飽和度・疲労度・満腹度予測・健康回復予測を描画する。
 *
 * @author GreatTomFox & Sora (ported from AppleSkin by squeek502)
 */
public class FoodOverlayRenderer
{
    // テクスチャ
    private static final ResourceLocation MC_ICONS = new ResourceLocation("textures/gui/icons.png");
    private static final ResourceLocation MOD_ICONS = new ResourceLocation("universalhudmanager", "textures/icons.png");

    // フラッシュアニメーション
    private static float unclampedFlashAlpha = 0f;
    private static float flashAlpha = 0f;
    private static byte alphaDir = 1;

    // アイコンオフセット
    public static final Vector<IntPoint> healthBarOffsets = new Vector<>();
    public static final Vector<IntPoint> foodBarOffsets = new Vector<>();

    private static final Random random = new Random();

    /**
     * 疲労度アンダーレイのみ描画する。フードバー本体より前に呼ぶこと（背景レイヤー）。
     */
    public static void drawExhaustionUnderlay(GuiGraphics guiGraphics, int right, int top)
    {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        drawExhaustionOverlay(player.getFoodData().getExhaustionLevel(), mc, guiGraphics, right, top, 1f);
    }

    /**
     * 飽和度・満腹度予測を描画する。フードバー本体より後に呼ぶこと（前景レイヤー）。
     */
    public static void renderFoodOverlayPost(GuiGraphics guiGraphics, int right, int top)
    {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        FoodData stats = player.getFoodData();
        generateHungerBarOffsets(top, right, mc.gui.getGuiTicks(), player);

        // 飽和度オーバーレイ
        if (HUDConfig.APPLESKIN_SATURATION.get())
        {
            drawSaturationOverlay(stats.getSaturationLevel(), 0, mc, guiGraphics, right, top, 1f);
        }

        // 食べ物を持っている時の満腹度・飽和度予測
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty() || !FoodHelper.canConsume(heldItem, player))
            heldItem = player.getOffhandItem();

        if (!heldItem.isEmpty() && FoodHelper.canConsume(heldItem, player))
        {
            FoodValues modifiedFoodValues = FoodHelper.getModifiedFoodValues(heldItem, player);

            if (HUDConfig.APPLESKIN_HUNGER_RESTORED.get())
            {
                drawHungerOverlay(modifiedFoodValues.hunger, stats.getFoodLevel(), mc, guiGraphics, right, top,
                    flashAlpha, FoodHelper.isRotten(heldItem, player));
            }

            if (HUDConfig.APPLESKIN_SATURATION.get())
            {
                float foodSaturationIncrement = modifiedFoodValues.getSaturationIncrement();
                int newFoodValue = stats.getFoodLevel() + modifiedFoodValues.hunger;
                float newSaturationValue = stats.getSaturationLevel() + foodSaturationIncrement;
                float saturationGained = newSaturationValue > newFoodValue
                    ? newFoodValue - stats.getSaturationLevel()
                    : foodSaturationIncrement;
                drawSaturationOverlay(stats.getSaturationLevel(), saturationGained, mc, guiGraphics, right, top, flashAlpha);
            }
        }
        else
        {
            resetFlash();
        }
    }

    /**
     * ヘルスバーの上に AppleSkin 相当の情報を描画する。
     * UHM の renderHealthBar 直後に呼ぶこと。
     *
     * @param guiGraphics 描画コンテキスト
     * @param left ヘルスバーの左端 X 座標（UHM が描画した位置）
     * @param top ヘルスバーの Y 座標（UHM が描画した位置）
     */
    public static void renderHealthOverlay(GuiGraphics guiGraphics, int left, int top)
    {
        if (!HUDConfig.APPLESKIN_HEALTH_RESTORED.get()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (player.level().getDifficulty() == Difficulty.PEACEFUL) return;
        if (player.getFoodData().getFoodLevel() >= 18) return;
        if (player.hasEffect(MobEffects.POISON)) return;
        if (player.hasEffect(MobEffects.WITHER)) return;
        if (player.hasEffect(MobEffects.REGENERATION)) return;

        generateHealthBarOffsets(top, left, mc.gui.getGuiTicks(), player);
        if (healthBarOffsets.size() == 0) return;

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty() || !FoodHelper.canConsume(heldItem, player))
            heldItem = player.getOffhandItem();

        if (heldItem.isEmpty() || !FoodHelper.canConsume(heldItem, player)) return;

        FoodValues modifiedFoodValues = FoodHelper.getModifiedFoodValues(heldItem, player);
        float foodHealthIncrement = FoodHelper.getEstimatedHealthIncrement(heldItem, modifiedFoodValues, player);
        float currentHealth = player.getHealth();
        float modifiedHealth = Math.min(currentHealth + foodHealthIncrement, player.getMaxHealth());

        if (currentHealth < modifiedHealth)
        {
            drawHealthOverlay(currentHealth, modifiedHealth, mc, guiGraphics, left, top, flashAlpha);
        }
    }

    // --- 描画メソッド ---

    public static void drawSaturationOverlay(float saturationLevel, float saturationGained, Minecraft mc, GuiGraphics guiGraphics, int right, int top, float alpha)
    {
        if (saturationLevel + saturationGained < 0) return;

        enableAlpha(alpha);

        float modifiedSaturation = Math.max(0, Math.min(saturationLevel + saturationGained, 20));
        int startSaturationBar = 0;
        int endSaturationBar = (int) Math.ceil(modifiedSaturation / 2.0F);

        if (saturationGained != 0)
            startSaturationBar = (int) Math.max(saturationLevel / 2.0F, 0);

        int iconSize = 9;

        for (int i = startSaturationBar; i < endSaturationBar; ++i)
        {
            if (i >= foodBarOffsets.size()) break;
            IntPoint offset = foodBarOffsets.get(i);
            if (offset == null) continue;

            int x = right + offset.x;
            int y = top + offset.y;

            int u = 0;
            float effectiveSaturation = (modifiedSaturation / 2.0F) - i;

            if (effectiveSaturation >= 1)       u = 3 * iconSize;
            else if (effectiveSaturation > .5)  u = 2 * iconSize;
            else if (effectiveSaturation > .25) u = 1 * iconSize;

            guiGraphics.blit(MOD_ICONS, x, y, u, 0, iconSize, iconSize, 256, 256);
        }

        RenderSystem.setShaderTexture(0, MC_ICONS);
        disableAlpha(alpha);
    }

    public static void drawHungerOverlay(int hungerRestored, int foodLevel, Minecraft mc, GuiGraphics guiGraphics, int right, int top, float alpha, boolean useRottenTextures)
    {
        if (hungerRestored <= 0) return;

        enableAlpha(alpha);

        int modifiedFood = Math.max(0, Math.min(20, foodLevel + hungerRestored));
        int startFoodBars = Math.max(0, foodLevel / 2);
        int endFoodBars = (int) Math.ceil(modifiedFood / 2.0F);

        int iconStartOffset = 16;
        int iconSize = 9;

        for (int i = startFoodBars; i < endFoodBars; ++i)
        {
            if (i >= foodBarOffsets.size()) break;
            IntPoint offset = foodBarOffsets.get(i);
            if (offset == null) continue;

            int x = right + offset.x;
            int y = top + offset.y;

            int v = 3 * iconSize;
            int u = iconStartOffset + 4 * iconSize;
            int ub = iconStartOffset + 1 * iconSize;

            if (useRottenTextures) { u += 4 * iconSize; ub += 12 * iconSize; }
            if (i * 2 + 1 == modifiedFood) u += 1 * iconSize;

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha * 0.25F);
            guiGraphics.blit(MC_ICONS, x, y, ub, v, iconSize, iconSize);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            guiGraphics.blit(MC_ICONS, x, y, u, v, iconSize, iconSize);
        }

        disableAlpha(alpha);
    }

    public static void drawHealthOverlay(float health, float modifiedHealth, Minecraft mc, GuiGraphics guiGraphics, int left, int top, float alpha)
    {
        if (modifiedHealth <= health) return;

        enableAlpha(alpha);

        int fixedModifiedHealth = (int) Math.ceil(modifiedHealth);
        boolean isHardcore = mc.player.level() != null && mc.player.level().getLevelData().isHardcore();

        int startHealthBars = (int) Math.max(0, Math.ceil(health) / 2.0F);
        int endHealthBars = (int) Math.max(0, Math.ceil(modifiedHealth / 2.0F));

        int iconStartOffset = 16;
        int iconSize = 9;

        for (int i = startHealthBars; i < endHealthBars; ++i)
        {
            if (i >= healthBarOffsets.size()) break;
            IntPoint offset = healthBarOffsets.get(i);
            if (offset == null) continue;

            int x = left + offset.x;
            int y = top + offset.y;

            int v = 0;
            int u = iconStartOffset + 4 * iconSize;
            int ub = iconStartOffset + 1 * iconSize;

            if (i * 2 + 1 == fixedModifiedHealth) u += 1 * iconSize;
            if (isHardcore) v = 5 * iconSize;

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha * 0.25F);
            guiGraphics.blit(MC_ICONS, x, y, ub, v, iconSize, iconSize);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            guiGraphics.blit(MC_ICONS, x, y, u, v, iconSize, iconSize);
        }

        disableAlpha(alpha);
    }

    public static void drawExhaustionOverlay(float exhaustion, Minecraft mc, GuiGraphics guiGraphics, int right, int top, float alpha)
    {
        float maxExhaustion = 4.0f;
        float ratio = Math.min(1, Math.max(0, exhaustion / maxExhaustion));
        int width = (int) (ratio * 81);
        int height = 9;

        if (width <= 0) return;
        enableAlpha(.75f);
        // blit(texture, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight)
        guiGraphics.blit(MOD_ICONS, right - width, top, 81 - width, 18, width, height, 256, 256);
        disableAlpha(.75f);

        RenderSystem.setShaderTexture(0, MC_ICONS);
    }

    // --- アニメーション ---

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;

        unclampedFlashAlpha += alphaDir * 0.125f;
        if (unclampedFlashAlpha >= 1.5f)       alphaDir = -1;
        else if (unclampedFlashAlpha <= -0.5f) alphaDir = 1;
        flashAlpha = Math.max(0F, Math.min(1F, unclampedFlashAlpha));
    }

    public static void resetFlash()
    {
        unclampedFlashAlpha = flashAlpha = 0f;
        alphaDir = 1;
    }

    // --- オフセット生成 ---

    private static void generateHungerBarOffsets(int top, int right, int ticks, Player player)
    {
        final int preferFoodBars = 10;

        FoodData stats = player.getFoodData();
        boolean shouldAnimatedFood = stats.getSaturationLevel() <= 0.0F
            && stats.getFoodLevel() > 0
            && ticks % (stats.getFoodLevel() * 3 + 1) == 0;

        if (foodBarOffsets.size() != preferFoodBars)
            foodBarOffsets.setSize(preferFoodBars);

        for (int i = 0; i < preferFoodBars; ++i)
        {
            int x = right - i * 8 - 9;
            int y = top;
            if (shouldAnimatedFood) y += random.nextInt(3) - 1;

            IntPoint point = foodBarOffsets.get(i);
            if (point == null) { point = new IntPoint(); foodBarOffsets.set(i, point); }

            point.x = x - right;
            point.y = y - top;
        }
    }

    private static void generateHealthBarOffsets(int top, int left, int ticks, Player player)
    {
        random.setSeed((long) (ticks * 312871L));

        final int preferHealthBars = 10;
        final float maxHealth = player.getMaxHealth();
        final float absorptionHealth = (float) Math.ceil(player.getAbsorptionAmount());

        int healthBars = (int) Math.ceil((maxHealth + absorptionHealth) / 2.0F);
        if (healthBars < 0 || healthBars > 1000) { healthBarOffsets.setSize(0); return; }

        int healthRows = (int) Math.ceil((float) healthBars / 10.0F);
        int healthRowHeight = Math.max(10 - (healthRows - 2), 3);
        boolean shouldAnimatedHealth = Math.ceil(player.getHealth()) <= 4;

        if (healthBarOffsets.size() != healthBars)
            healthBarOffsets.setSize(healthBars);

        for (int i = healthBars - 1; i >= 0; --i)
        {
            int row = (int) Math.ceil((float) (i + 1) / (float) preferHealthBars) - 1;
            int x = left + i % preferHealthBars * 8;
            int y = top - row * healthRowHeight;
            if (shouldAnimatedHealth) y += random.nextInt(2);

            IntPoint point = healthBarOffsets.get(i);
            if (point == null) { point = new IntPoint(); healthBarOffsets.set(i, point); }

            point.x = x - left;
            point.y = y - top;
        }
    }

    // --- ユーティリティ ---

    private static void enableAlpha(float alpha)
    {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void disableAlpha(float alpha)
    {
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
