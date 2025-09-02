package top.maple_bamboo.mod.modules.impl.combat;

import top.maple_bamboo.api.events.eventbus.EventHandler;
import top.maple_bamboo.api.events.impl.UpdateWalkingPlayerEvent;
import top.maple_bamboo.mod.modules.Module;
import top.maple_bamboo.mod.modules.settings.impl.BooleanSetting;
import top.maple_bamboo.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

import java.util.ArrayList;
import java.util.List;

public class KillauraPlus extends Module {
    public static KillauraPlus INSTANCE;
    public static List<Entity> targets = new ArrayList<>();

    // ==== 模组配置 ====
    public final BooleanSetting attackHostiles = add(new BooleanSetting("AttackHostiles", true));
    public final BooleanSetting attackPlayers = add(new BooleanSetting("AttackPlayers", false));
    public final BooleanSetting attackAnimals = add(new BooleanSetting("AttackAnimals", false));
    public final BooleanSetting attackNeutrals = add(new BooleanSetting("AttackNeutrals", true));
    // 攻击已被激怒的中立生物选项，始终作为attackNeutrals的子选项
    public final BooleanSetting attackAngryNeutrals = add(new BooleanSetting("AttackAngryNeutrals", true,() -> attackNeutrals.getValue()));

    // ==== 核心常量与变量 ====
    public final SliderSetting attackRange = add(new SliderSetting("AttackRange", 6.3f, 1.0f, 8.0f));
    public final SliderSetting attackCooldown = add(new SliderSetting("AttackCooldown", 30, 1, 50, 1));

    private int attackTimer = 0;

    public KillauraPlus() {
        super("KillAura+", Category.Combat);
        setChinese("杀戮光环+");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        targets.clear();
        attackTimer = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        targets.clear();
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
        onUpdate();
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) {
            return;
        }

        attackTimer++;

        if (attackTimer >= attackCooldown.getValueInt()) {
            targets = findTargetsInRange();

            if (!targets.isEmpty()) {
                // 同时攻击所有目标
                for (Entity target : targets) {
                    if (target instanceof LivingEntity) {
                        attack((LivingEntity) target);
                    }
                }
                attackTimer = 0;
            }
        }
    }

    private List<Entity> findTargetsInRange() {
        List<Entity> targets = new ArrayList<>();

        for (Entity entity : mc.world.getEntities()) {
            if (entity.equals(mc.player) || entity.equals(mc.cameraEntity) || !(entity instanceof LivingEntity)) {
                continue;
            }

            if (mc.player.squaredDistanceTo(entity) < attackRange.getValue() * attackRange.getValue()) {
                // 检查是否攻击玩家
                if (attackPlayers.getValue() && entity instanceof PlayerEntity) {
                    targets.add(entity);
                    continue; // 已匹配，跳过其他检查
                }

                // 检查是否攻击动物
                if (attackAnimals.getValue() && entity instanceof AnimalEntity) {
                    // 排除中立生物，因为它们有自己的选项
                    if (!isNeutralMob(entity)) {
                        targets.add(entity);
                    }
                    continue; // 已匹配，跳过其他检查
                }

                // 检查是否攻击中立生物
                if (attackNeutrals.getValue() && isNeutralMob(entity)) {
                    // 如果开启了"只攻击被激怒的中立生物"选项
                    if (attackAngryNeutrals.getValue()) {
                        // 只攻击被激怒的中立生物
                        if (isAngryNeutral(entity)) {
                            targets.add(entity);
                        }
                    } else {
                        // 攻击所有中立生物，无论是否被激怒
                        targets.add(entity);
                    }
                    continue; // 已匹配，跳过其他检查
                }

                // 检查是否攻击敌对生物（排除中立生物）
                if (attackHostiles.getValue() && isHostileMob(entity) && !isNeutralMob(entity)) {
                    targets.add(entity);
                }
            }
        }
        return targets;
    }

    private boolean isHostileMob(Entity entity) {
        // 定义哪些实体属于敌对生物（不包括中立生物）
        return entity instanceof HostileEntity ||
                entity instanceof SlimeEntity ||
                entity instanceof MagmaCubeEntity ||
                entity instanceof HoglinEntity;
    }

    private boolean isNeutralMob(Entity entity) {
        // 定义哪些实体属于中立生物
        return entity instanceof EndermanEntity ||
                entity instanceof ZombifiedPiglinEntity ||
                entity instanceof PolarBearEntity ||
                entity instanceof WolfEntity ||
                entity instanceof IronGolemEntity ||
                entity instanceof PiglinEntity;
    }

    private boolean isAngryNeutral(Entity entity) {
        // 使用 instanceof 检查并强制转换为特定类型来访问方法
        if (entity instanceof EndermanEntity enderman) {
            return enderman.isAngry();
        }
        if (entity instanceof ZombifiedPiglinEntity piglin) {
            return piglin.isAttacking();
        }
        if (entity instanceof PolarBearEntity polarBear) {
            return polarBear.isAttacking();
        }
        if (entity instanceof WolfEntity wolf) {
            return wolf.isAttacking();
        }
        if (entity instanceof IronGolemEntity ironGolem) {
            return ironGolem.isAttacking();
        }
        if (entity instanceof PiglinEntity Piglin) {
            return Piglin.isAttacking();
        }


        return false;
    }

    private void attack(LivingEntity target) {
        PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(target, false);
        mc.getNetworkHandler().sendPacket(packet);
    }

    @Override
    public String getInfo() {
        return targets.isEmpty() ? null : String.valueOf(targets.size());
    }
}