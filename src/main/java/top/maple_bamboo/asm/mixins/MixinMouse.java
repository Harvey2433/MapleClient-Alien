package top.maple_bamboo.asm.mixins;

import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.api.events.impl.MouseUpdateEvent;
import top.maple_bamboo.mod.gui.clickgui.ClickGuiScreen;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static top.maple_bamboo.api.utils.Wrapper.mc;
@Mixin(Mouse.class)
public class MixinMouse {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouse(long window, int button, int action, int mods, CallbackInfo ci) {
        int key = -(button + 2);
        if (mc.currentScreen instanceof ClickGuiScreen && action == 1 && MapleClientMain.MODULE.setBind(key)) {
            return;
        }
        if (action == 1) {
            MapleClientMain.MODULE.onKeyPressed(key);
        }
        if (action == 0) {
            MapleClientMain.MODULE.onKeyReleased(key);
        }
    }

    @Inject(method = "updateMouse", at = @At("RETURN"))
    private void updateHook(CallbackInfo ci) {
        MapleClientMain.EVENT_BUS.post(new MouseUpdateEvent());
    }
}
