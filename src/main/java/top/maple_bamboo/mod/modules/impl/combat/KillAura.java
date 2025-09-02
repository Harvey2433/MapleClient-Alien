package top.maple_bamboo.mod.modules.impl.combat;

import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.api.events.eventbus.EventHandler;
import top.maple_bamboo.api.events.impl.LookAtEvent;
import top.maple_bamboo.api.events.impl.PacketEvent;
import top.maple_bamboo.api.events.impl.UpdateWalkingPlayerEvent;
import top.maple_bamboo.api.utils.combat.CombatUtil;
import top.maple_bamboo.api.utils.entity.EntityUtil;
import top.maple_bamboo.api.utils.math.*;
import top.maple_bamboo.api.utils.math.Animation;
import top.maple_bamboo.api.utils.math.Easing;
import top.maple_bamboo.api.utils.math.MathUtil;
import top.maple_bamboo.api.utils.math.Timer;
import top.maple_bamboo.api.utils.render.ColorUtil;
import top.maple_bamboo.api.utils.render.JelloUtil;
import top.maple_bamboo.api.utils.render.Render3DUtil;
import top.maple_bamboo.asm.accessors.IEntity;
import top.maple_bamboo.asm.accessors.ILivingEntity;
import top.maple_bamboo.mod.modules.Module;
import top.maple_bamboo.mod.modules.settings.SwingSide;
import top.maple_bamboo.mod.modules.settings.impl.BooleanSetting;
import top.maple_bamboo.mod.modules.settings.impl.ColorSetting;
import top.maple_bamboo.mod.modules.settings.impl.EnumSetting;
import top.maple_bamboo.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class KillAura extends Module {

    public static KillAura INSTANCE;
    public static List<Entity> targets = new ArrayList<>(); // 改为存储多个目标
    public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));

    public final SliderSetting range =
            add(new SliderSetting("Range", 6.0f, 0.1f, 7.0f, () -> page.getValue() == Page.General));
    private final EnumSetting<Cooldown> cd = add(new EnumSetting<>("CooldownMode", Cooldown.Delay, () -> page.getValue() == Page.General));
    private final SliderSetting cooldown =
            add(new SliderSetting("Cooldown", 1.1f, 0f, 1.2f, 0.01, () -> page.getValue() == Page.General));
    private final SliderSetting wallRange =
            add(new SliderSetting("WallRange", 6.0f, 0.1f, 7.0f, () -> page.getValue() == Page.General));
    private final BooleanSetting whileEating =
            add(new BooleanSetting("WhileUsing", true, () -> page.getValue() == Page.General));
    private final BooleanSetting weaponOnly =
            add(new BooleanSetting("WeaponOnly", true, () -> page.getValue() == Page.General));
    private final EnumSetting<SwingSide> swingMode = add(new EnumSetting<>("Swing", SwingSide.All, () -> page.getValue() == Page.General));
    private final BooleanSetting onlyCritical =
            add(new BooleanSetting("OnlyCritical", false, () -> page.getValue() == Page.General));
    private final BooleanSetting onlyTick =
            add(new BooleanSetting("OnlyTick", false, () -> page.getValue() == Page.General));

    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true, () -> page.getValue() == Page.Rotate));
    private final BooleanSetting yawStep =
            add(new BooleanSetting("YawStep", false, () -> page.getValue() == Page.Rotate));
    private final SliderSetting steps =
            add(new SliderSetting("Steps", 0.05, 0, 1, 0.01, () -> page.getValue() == Page.Rotate));
    private final BooleanSetting checkFov =
            add(new BooleanSetting("OnlyLooking", true, () -> page.getValue() == Page.Rotate));
    private final SliderSetting fov =
            add(new SliderSetting("Fov", 5f, 0f, 30f, () -> checkFov.getValue() && page.getValue() == Page.Rotate));
    private final SliderSetting priority = add(new SliderSetting("Priority", 10,0 ,100, () ->page.getValue() == Page.Rotate));
    private final EnumSetting<TargetMode> targetMode =
            add(new EnumSetting<>("Filter", TargetMode.DISTANCE, () -> page.getValue() == Page.Target));
    public final BooleanSetting Players = add(new BooleanSetting("Players", true, () -> page.getValue() == Page.Target).setParent());
    public final BooleanSetting armorLow = add(new BooleanSetting("ArmorLow", true, () -> page.getValue() == Page.Target && Players.isOpen()));
    public final BooleanSetting Mobs = add(new BooleanSetting("Mobs", true, () -> page.getValue() == Page.Target));
    public final BooleanSetting Animals = add(new BooleanSetting("Animals", true, () -> page.getValue() == Page.Target));
    public final BooleanSetting Villagers = add(new BooleanSetting("Villagers", true, () -> page.getValue() == Page.Target));
    public final BooleanSetting Slimes = add(new BooleanSetting("Slimes", true, () -> page.getValue() == Page.Target));

    private final EnumSetting<TargetESP> mode = add(new EnumSetting<>("TargetESP", TargetESP.Box, () -> page.getValue() == Page.Render));
    private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 50), () -> page.getValue() == Page.Render));
    private final ColorSetting hitColor = add(new ColorSetting("HitColor", new Color(255, 255, 255, 150), () -> page.getValue() == Page.Render));
    public final SliderSetting animationTime = add(new SliderSetting("AnimationTime", 200, 0, 2000, 1, () -> page.getValue() == Page.Render && mode.is(TargetESP.Box)));
    public final EnumSetting<Easing> ease = add(new EnumSetting<>("Ease", Easing.CubicInOut, () -> page.getValue() == Page.Render && mode.is(TargetESP.Box)));

    private final Animation animation = new Animation();
    public enum TargetESP {
        Box,
        Jello,
        None
    }
    public enum Cooldown {
        Vanilla,
        Delay
    }
    public Vec3d directionVec = null;
    private final Timer tick = new Timer();

    public KillAura() {
        super("KillAura", Category.Combat);
        setChinese("杀戮光环");
        INSTANCE = this;
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (!targets.isEmpty()) {
            for (Entity target : targets) {
                doRender(matrixStack, mc.getTickDelta(), target, mode.getValue());
            }
        }
    }
    public void doRender(MatrixStack matrixStack, float partialTicks, Entity entity, TargetESP mode) {
        switch (mode) {
            case Box -> Render3DUtil.draw3DBox(matrixStack, ((IEntity) entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), partialTicks), MathUtil.interpolate(entity.lastRenderY, entity.getY(), partialTicks), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), partialTicks))).expand(0, 0.1, 0), ColorUtil.fadeColor(color.getValue(), hitColor.getValue(), animation.get(0, animationTime.getValueInt(), ease.getValue())), false, true);
            case Jello -> JelloUtil.drawJello(matrixStack, entity, color.getValue());
        }
    }

    public static void doRender(MatrixStack matrixStack, float partialTicks, Entity entity, Color color, TargetESP mode) {
        switch (mode) {
            case Box -> Render3DUtil.draw3DBox(matrixStack, ((IEntity) entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), partialTicks), MathUtil.interpolate(entity.lastRenderY, entity.getY(), partialTicks), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), partialTicks))).expand(0, 0.1, 0), color, false, true);
            case Jello -> JelloUtil.drawJello(matrixStack, entity, color);
        }
    }

    @Override
    public String getInfo() {
        return targets.isEmpty() ? null : String.valueOf(targets.size()); // 显示攻击目标数量
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
        if (!onlyTick.getValue())
            onUpdate();
    }
    @Override
    public void onUpdate() {
        if (weaponOnly.getValue() && !EntityUtil.isHoldingWeapon(mc.player)) {
            targets.clear();
            return;
        }
        targets = getTargets(); // 获取所有目标
        if (targets.isEmpty()) {
            return;
        }
        doAura();
    }

    @EventHandler
    public void onRotate(LookAtEvent event) {
        if (!targets.isEmpty() && rotate.getValue() && yawStep.getValue()) {
            // 选择第一个目标进行旋转
            directionVec = targets.get(0).getEyePos();
            event.setTarget(directionVec, steps.getValueFloat(), priority.getValueFloat());
        }
    }
    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof HandSwingC2SPacket || packet instanceof PlayerInteractEntityC2SPacket && Criticals.getInteractType((PlayerInteractEntityC2SPacket) packet) == Criticals.InteractType.ATTACK) {
            tick.reset();
        }
    }
    private boolean check() {
        if (onlyCritical.getValue()) {
            if (!(Criticals.INSTANCE.isOn() || mc.player.fallDistance > 0)) {
                return false;
            }
        }
        int at = (int) (tick.getPassedTimeMs() / 50);
        if (cd.getValue() == Cooldown.Vanilla) {
            at = ((ILivingEntity) mc.player).getLastAttackedTicks();
        }
        at = (int) (at * MapleClientMain.SERVER.getTPSFactor());
        if (!(Math.max(at / getAttackCooldownProgressPerTick(), 0.0F) >= cooldown.getValue()))
            return false;
        return whileEating.getValue() || !mc.player.isUsingItem();
    }

    public static float getAttackCooldownProgressPerTick() {
        return (float) (1.0 / mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * 20.0);
    }

    private void doAura() {
        if (!check()) {
            return;
        }

        // 攻击所有目标
        for (Entity target : targets) {
            if (rotate.getValue()) {
                if (!faceVector(target.getEyePos())) continue;
            }
            animation.to = 1;
            animation.from = 1;
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
        }

        mc.player.resetLastAttackedTicks();
        EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
        tick.reset();
    }

    public boolean faceVector(Vec3d directionVec) {
        if (!yawStep.getValue()) {
            MapleClientMain.ROTATION.lookAt(directionVec);
            return true;
        } else {
            this.directionVec = directionVec;
            if (MapleClientMain.ROTATION.inFov(directionVec, fov.getValueFloat())) {
                return true;
            }
        }
        return !checkFov.getValue();
    }

    private List<Entity> getTargets() {
        List<Entity> targetList = new ArrayList<>();
        double maxHealth = 36.0;

        for (Entity entity : mc.world.getEntities()) {
            if (!isEnemy(entity)) continue;
            if (!mc.player.canSee(entity) && mc.player.distanceTo(entity) > wallRange.getValue()) {
                continue;
            }
            if (!CombatUtil.isValid(entity, range.getValue())) continue;

            // 添加所有符合条件的实体到列表
            targetList.add(entity);
        }

        // 根据目标模式排序
        if (targetMode.getValue() == TargetMode.HEALTH) {
            targetList.sort((e1, e2) -> (int) (EntityUtil.getHealth(e1) - EntityUtil.getHealth(e2)));
        } else if (targetMode.getValue() == TargetMode.DISTANCE) {
            targetList.sort((e1, e2) -> (int) (mc.player.distanceTo(e1) - mc.player.distanceTo(e2)));
        }

        // 如果开启了护甲低优先，将护甲低的玩家移到列表前面
        if (armorLow.getValue()) {
            List<Entity> lowArmorPlayers = new ArrayList<>();
            List<Entity> others = new ArrayList<>();

            for (Entity entity : targetList) {
                if (entity instanceof PlayerEntity && EntityUtil.isArmorLow((PlayerEntity) entity, 10)) {
                    lowArmorPlayers.add(entity);
                } else {
                    others.add(entity);
                }
            }

            targetList.clear();
            targetList.addAll(lowArmorPlayers);
            targetList.addAll(others);
        }

        return targetList;
    }

    private boolean isEnemy(Entity entity) {
        if (entity instanceof SlimeEntity && Slimes.getValue()) return true;
        if (entity instanceof PlayerEntity && Players.getValue()) return true;
        if (entity instanceof VillagerEntity && Villagers.getValue()) return true;
        if (!(entity instanceof VillagerEntity) && entity instanceof MobEntity && Mobs.getValue()) return true;
        return entity instanceof AnimalEntity && Animals.getValue();
    }

    private enum TargetMode {
        DISTANCE,
        HEALTH,
    }

    public enum Page {
        General,
        Rotate,
        Target,
        Render
    }
}