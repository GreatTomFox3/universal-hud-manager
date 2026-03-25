package com.greattomfoxsora.universalhudmanager.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/**
 * Universal HUD Manager の独立オーバーレイ
 * IGuiOverlay として登録することで、他 mod（TACZなど）が
 * CROSSHAIR オーバーレイをキャンセルしても UHM の HUD が消えない。
 *
 * @author GreatTomFox & Sora
 */
public class UHMHudOverlay implements IGuiOverlay {

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        HUDPositionHandler.renderAll(guiGraphics, screenWidth, screenHeight);
    }
}
