package com.greattomfoxsora.universalhudmanager;

import com.greattomfoxsora.universalhudmanager.client.KeyBindings;
import com.greattomfoxsora.universalhudmanager.client.HUDPositionHandler;
import com.greattomfoxsora.universalhudmanager.config.HUDConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import com.greattomfoxsora.universalhudmanager.client.UHMHudOverlay;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Universal HUD Manager - シンプル版
 * Health + Armor HUD の2個のみに集中
 * アーマーHUD方式ベース
 * 
 * @author GreatTomFox & Sora
 */
@Mod("universalhudmanager")
public class UniversalHudManager {
    
    public static final String MOD_ID = "universalhudmanager";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    public UniversalHudManager() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Config registration
        HUDConfig.register();

        // ネットワーク同期（疲労度・飽和度をクライアントに送る）
        com.greattomfoxsora.universalhudmanager.network.SyncHandler.init();
        
        // Client setup
        modEventBus.addListener(this::doClientSetup);
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::registerOverlays);
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        // Register HUD position handler for actual HUD movement
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(HUDPositionHandler.class);
            MinecraftForge.EVENT_BUS.register(new com.greattomfoxsora.universalhudmanager.client.appleskin.FoodOverlayRenderer());
            LOGGER.info("📍 HUD Position Handler registered");
        }
        
        LOGGER.info("🎯 Universal HUD Manager initialized - Health + Armor focus");
    }
    
    @SubscribeEvent
    public void doClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("🎮 Client setup complete");
    }
    
    @SubscribeEvent
    public void registerKeyMappings(RegisterKeyMappingsEvent event) {
        KeyBindings.register(event);
        LOGGER.info("🔑 Key bindings registered");
    }

    @SubscribeEvent
    public void registerOverlays(RegisterGuiOverlaysEvent event) {
        // 全オーバーレイの最上位に登録することで、他modのオーバーレイキャンセルの影響を受けない
        event.registerAboveAll("uhm_hud", new UHMHudOverlay());
        LOGGER.info("🖥️ UHM HUD Overlay registered");
    }
}