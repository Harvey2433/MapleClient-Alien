package top.maple_bamboo.mod.modules.impl.movement;

import top.maple_bamboo.mod.modules.Module;

public final class NoJumpDelay
        extends Module {
    public static NoJumpDelay INSTANCE;
    public NoJumpDelay() {
        super("NoJumpDelay", Category.Movement);
        setChinese("无跳跃冷却");
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        mc.player.jumpingCooldown = 0;
    }
}
