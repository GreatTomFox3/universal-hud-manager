package com.greattomfoxsora.universalhudmanager.client;

import com.mojang.logging.LogUtils;
import com.greattomfoxsora.universalhudmanager.UniversalHudManager;
import com.greattomfoxsora.universalhudmanager.core.HUDElement;
import com.greattomfoxsora.universalhudmanager.core.HUDRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Universal HUD Manager のHUDオーバーレイ制御システム
 * TerraFirmaCraftのアプローチを参考にForge Eventsを使用
 * 
 * @author GreatTomFox & Sora
 */
@Mod.EventBusSubscriber(modid = UniversalHudManager.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HUDOverlayHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // バニラHUDオーバーレイのResourceLocation
    private static final ResourceLocation VANILLA_HEALTH = VanillaGuiOverlay.PLAYER_HEALTH.id();
    private static final ResourceLocation VANILLA_FOOD = VanillaGuiOverlay.FOOD_LEVEL.id();
    private static final ResourceLocation VANILLA_EXP = VanillaGuiOverlay.EXPERIENCE_BAR.id();
    
    /**
     * カスタムHUDオーバーレイの登録
     */
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        // バニラヘルスバーの上にカスタムヘルスオーバーレイを登録
        event.registerAbove(VANILLA_HEALTH, 
            "custom_health", 
            HUDOverlayHandler::renderCustomHealth);
            
        // バニラフードバーの上にカスタムフードオーバーレイを登録
        event.registerAbove(VANILLA_FOOD, 
            "custom_food", 
            HUDOverlayHandler::renderCustomFood);
            
        // バニラ経験値バーの上にカスタム経験値オーバーレイを登録
        event.registerAbove(VANILLA_EXP, 
            "custom_experience", 
            HUDOverlayHandler::renderCustomExperience);
        
        LOGGER.info("Universal HUD Manager overlays registered");
    }
    
    /**
     * バニラHUDの描画制御 - HUD Manager制御下の要素はキャンセル
     */
    @Mod.EventBusSubscriber(modid = UniversalHudManager.MODID, value = Dist.CLIENT)
    public static class RenderEvents {
        
        @SubscribeEvent
        public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
            final ResourceLocation overlayId = event.getOverlay().id();
            
            // ヘルスバーがHUD Manager制御下にある場合はバニラ描画をキャンセル
            if (overlayId.equals(VANILLA_HEALTH)) {
                HUDElement healthElement = HUDRegistry.getHUD("minecraft:health");
                if (healthElement != null) {
                    LOGGER.debug("Canceling vanilla health overlay - under HUD Manager control");
                    event.setCanceled(true);
                }
            }
            
            // フードバーがHUD Manager制御下にある場合はバニラ描画をキャンセル
            if (overlayId.equals(VANILLA_FOOD)) {
                HUDElement foodElement = HUDRegistry.getHUD("minecraft:food");
                if (foodElement != null) {
                    LOGGER.debug("Canceling vanilla food overlay - under HUD Manager control");
                    event.setCanceled(true);
                }
            }
            
            // 経験値バーがHUD Manager制御下にある場合はバニラ描画をキャンセル
            if (overlayId.equals(VANILLA_EXP)) {
                HUDElement expElement = HUDRegistry.getHUD("minecraft:experience");
                if (expElement != null) {
                    LOGGER.debug("Canceling vanilla experience overlay - under HUD Manager control");
                    event.setCanceled(true);
                }
            }
        }
    }
    
    /**
     * カスタムヘルスバー描画 (Phase 3暫定実装)
     */
    private static void renderCustomHealth(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height) {
        HUDElement healthElement = HUDRegistry.getHUD("minecraft:health");
        if (healthElement != null) {
            LOGGER.info("Custom Health rendering at position: {}, {} - 実装が必要", healthElement.getX(), healthElement.getY());
            
            // TODO: 実際のヘルスバー描画をカスタム位置で実装
            // 現段階では位置情報のログ出力のみ
            
            // バニラ描画を元の位置でフォールバック（暫定）
            final NamedGuiOverlay vanillaOverlay = GuiOverlayManager.findOverlay(VANILLA_HEALTH);
            if (vanillaOverlay != null) {
                vanillaOverlay.overlay().render(gui, graphics, partialTicks, width, height);
            }
        }
    }
    
    /**
     * カスタムフードバー描画 (Phase 3暫定実装)
     */
    private static void renderCustomFood(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height) {
        HUDElement foodElement = HUDRegistry.getHUD("minecraft:food");
        if (foodElement != null) {
            LOGGER.info("Custom Food rendering at position: {}, {} - 実装が必要", foodElement.getX(), foodElement.getY());
            
            // TODO: 実際のフードバー描画をカスタム位置で実装
            // 現段階では位置情報のログ出力のみ
            
            // バニラ描画を元の位置でフォールバック（暫定）
            final NamedGuiOverlay vanillaOverlay = GuiOverlayManager.findOverlay(VANILLA_FOOD);
            if (vanillaOverlay != null) {
                vanillaOverlay.overlay().render(gui, graphics, partialTicks, width, height);
            }
        }
    }
    
    /**
     * カスタム経験値バー描画 (Phase 3暫定実装)
     */
    private static void renderCustomExperience(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height) {
        HUDElement expElement = HUDRegistry.getHUD("minecraft:experience");
        if (expElement != null) {
            LOGGER.info("Custom Experience rendering at position: {}, {} - 実装が必要", expElement.getX(), expElement.getY());
            
            // TODO: 実際の経験値バー描画をカスタム位置で実装
            // 現段階では位置情報のログ出力のみ
            
            // バニラ描画を元の位置でフォールバック（暫定）
            final NamedGuiOverlay vanillaOverlay = GuiOverlayManager.findOverlay(VANILLA_EXP);
            if (vanillaOverlay != null) {
                vanillaOverlay.overlay().render(gui, graphics, partialTicks, width, height);
            }
        }
    }
}