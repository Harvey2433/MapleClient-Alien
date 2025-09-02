package top.maple_bamboo.asm.mixins;

import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.api.events.impl.WorldBreakEvent;
import net.minecraft.client.render.BlockBreakingInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBreakingInfo.class)
public class MixinBlockBreakingInfo {
    @Inject(method = "compareTo", at = @At("HEAD"))
    public void onCompareTo(BlockBreakingInfo blockBreakingInfo, CallbackInfoReturnable<Integer> cir) {
        MapleClientMain.EVENT_BUS.post(new WorldBreakEvent(blockBreakingInfo));
    }
}
