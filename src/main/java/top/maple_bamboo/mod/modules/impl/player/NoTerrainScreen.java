package top.maple_bamboo.mod.modules.impl.player;

import top.maple_bamboo.api.events.eventbus.EventHandler;
import top.maple_bamboo.api.events.impl.TickEvent;
import top.maple_bamboo.mod.modules.Module;
import top.maple_bamboo.mod.modules.impl.exploit.PortalGod;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;

public class NoTerrainScreen extends Module {
    public NoTerrainScreen() {
        super("NoTerrainScreen", Category.Player);
        setChinese("没有加载界面");
    }

    @EventHandler
    public void onEvent(TickEvent event) {
        if (PortalGod.INSTANCE.isOn()) return;
        if (mc.currentScreen instanceof DownloadingTerrainScreen) {
            mc.currentScreen = null;
        }
    }
}
