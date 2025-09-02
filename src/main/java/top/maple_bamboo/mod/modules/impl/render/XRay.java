package top.maple_bamboo.mod.modules.impl.render;

import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.mod.modules.Module;
import net.minecraft.block.Block;

public class XRay extends Module {
    public static XRay INSTANCE;
    public XRay() {
        super("XRay", Category.Render);
        setChinese("矿物透视");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        mc.chunkCullingEnabled = false;
        mc.worldRenderer.reload();
    }

    @Override
    public void onDisable() {
        mc.chunkCullingEnabled = true;
        mc.worldRenderer.reload();
    }

    public boolean isCheckableOre(Block block) {
        return MapleClientMain.XRAY.inWhitelist(block.getTranslationKey());
    }
}
