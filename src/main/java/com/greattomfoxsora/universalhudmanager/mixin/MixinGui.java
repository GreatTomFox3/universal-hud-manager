package com.greattomfoxsora.universalhudmanager.mixin;

import javax.annotation.Nullable;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraft.client.Options;

import com.greattomfoxsora.universalhudmanager.config.HUDConfig;

/**
 * Mixin for hiding vanilla dismount message when custom dismount message is enabled
 * Target: overlayMessage system (used for dismount messages)
 * 
 * @author GreatTomFox & Sora
 */
@Mixin(Gui.class)
public class MixinGui {
    
    @Shadow 
    @Nullable
    protected Component f_92990_; // overlayMessageString
    
    @Shadow 
    protected int f_92991_; // overlayMessageTime
    
    /**
     * Intercept the setOverlayMessage method to prevent vanilla dismount messages
     * when our custom dismount message is enabled
     * ClientPacketListener calls this with "mount.onboard" message
     */
    @Inject(
        method = "m_93063_", // setOverlayMessage(Component, boolean)
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSetOverlayMessage(Component message, boolean animate, CallbackInfo ci) {
        // Only intercept if our custom dismount message is enabled
        if (!HUDConfig.DISMOUNT_MESSAGE_ENABLED.get()) {
            return;
        }
        
        // Debug: Log all overlay messages to identify dismount message
        if (message != null) {
            String messageText = message.getString();
            System.out.println("🔍 OverlayMessage intercepted: '" + messageText + "' (animate=" + animate + ")");
            
            // Check if this is a dismount message using translation key detection
            // The dismount message is created with Component.translatable("mount.onboard", keyShift)
            // We can detect this by checking if it's a TranslatableComponent with the "mount.onboard" key
            if (message instanceof net.minecraft.network.chat.MutableComponent) {
                // Try to access the translation key if it's a translatable component
                // This is a more reliable way than string matching across languages
                String translationKey = getTranslationKey(message);
                if ("mount.onboard".equals(translationKey)) {
                    System.out.println("🚫 Cancelling vanilla dismount message (mount.onboard): " + messageText);
                    ci.cancel();
                    return;
                }
            }
            
            // Fallback: Check for known dismount message patterns (less reliable)
            if (messageText.contains("Dismount") || 
                messageText.contains("dismount") ||
                messageText.contains("Shift") || 
                messageText.toLowerCase().contains("shift") ||
                messageText.contains("Control")) {
                System.out.println("🚫 Cancelling vanilla dismount message (pattern match): " + messageText);
                ci.cancel();
            }
        }
    }
    
    /**
     * Helper method to extract translation key from Component
     * This is a safer approach than string matching
     */
    private String getTranslationKey(Component component) {
        // TranslatableComponent stores the translation key
        // We need reflection to access it safely
        try {
            if (component.getClass().getSimpleName().contains("Translatable")) {
                // Use reflection to get the key field
                java.lang.reflect.Field keyField = component.getClass().getDeclaredField("key");
                keyField.setAccessible(true);
                return (String) keyField.get(component);
            }
        } catch (Exception e) {
            // If reflection fails, fall back to string matching
            System.out.println("⚠️ Could not extract translation key: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Redirect Options.attackIndicator() call in renderCrosshair to return fake OptionInstance
     * when our custom attack indicator is enabled and mode is CROSSHAIR
     * This prevents vanilla crosshair attack indicator from rendering
     */
    @Redirect(
        method = "m_280130_", // renderCrosshair(GuiGraphics) - obfuscated name
        at = @At(value = "INVOKE", 
                 target = "Lnet/minecraft/client/Options;m_232120_()Lnet/minecraft/client/OptionInstance;")
    )
    private net.minecraft.client.OptionInstance<AttackIndicatorStatus> redirectAttackIndicatorOption(Options options) {
        // Only intercept if our custom attack indicator is enabled
        if (!HUDConfig.ATTACK_INDICATOR_ENABLED.get()) {
            return options.attackIndicator(); // Return original OptionInstance
        }
        
        // Get the original attack indicator setting
        net.minecraft.client.OptionInstance<AttackIndicatorStatus> originalOption = options.attackIndicator();
        AttackIndicatorStatus originalStatus = originalOption.get();
        
        // If it's CROSSHAIR mode, return a fake OptionInstance that returns OFF
        if (originalStatus == AttackIndicatorStatus.CROSSHAIR) {
            if (HUDConfig.DEBUG_MODE.get()) {
                System.out.println("🚫 Redirecting vanilla crosshair attack indicator to OFF");
            }
            
            // Create a fake OptionInstance that returns OFF
            return new net.minecraft.client.OptionInstance<>(
                "fake_attack_indicator",
                net.minecraft.client.OptionInstance.noTooltip(),
                net.minecraft.client.OptionInstance.forOptionEnum(),
                new net.minecraft.client.OptionInstance.Enum<>(
                    java.util.Arrays.asList(AttackIndicatorStatus.values()),
                    com.mojang.serialization.Codec.INT.xmap(AttackIndicatorStatus::byId, AttackIndicatorStatus::getId)
                ),
                AttackIndicatorStatus.OFF, // Always return OFF
                (value) -> {} // No-op callback
            );
        }
        
        // For all other cases, return the original OptionInstance
        return originalOption;
    }
}