package top.maple_bamboo.mod.modules.impl.render;

import top.maple_bamboo.api.utils.combat.CombatUtil;
import top.maple_bamboo.api.utils.entity.EntityUtil;
import top.maple_bamboo.api.utils.render.Render3DUtil;
import top.maple_bamboo.api.utils.world.BlockPosX;
import top.maple_bamboo.api.utils.world.BlockUtil;
import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.mod.modules.Module;
import top.maple_bamboo.mod.modules.impl.client.AntiCheat;
import top.maple_bamboo.mod.modules.settings.impl.BooleanSetting;
import top.maple_bamboo.mod.modules.settings.impl.ColorSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CityESP extends Module {

	public CityESP() {
		super("CityESP", Category.Render);
		setChinese("水晶阻挡显示");
	}

	private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
	private final BooleanSetting box = add(new BooleanSetting("Box", true));
	private final BooleanSetting outline = add(new BooleanSetting("Outline", true));
	private final BooleanSetting burrow = add(new BooleanSetting("Burrow", true));
	private final BooleanSetting surround = add(new BooleanSetting("Surround", true));
	final List<BlockPos> renderList = new ArrayList<>();
    @Override
	public void onRender3D(MatrixStack matrixStack) {
		renderList.clear();
		float pOffset = (float) AntiCheat.getOffset();
		for (Entity player : CombatUtil.getEnemies(10)) {
			if (burrow.getValue()) {
				float[] offset = new float[]{-pOffset, 0f, pOffset};
				for (float x : offset) {
					for (float z : offset) {
						BlockPos tempPos;
						if (isObsidian(tempPos = new BlockPosX(player.getPos().add(x, 0, z)))) {
							renderList.add(tempPos);
						}
						if (isObsidian(tempPos = new BlockPosX(player.getPos().add(x, 0.5, z)))) {
							renderList.add(tempPos);
						}
					}
				}
			}

			if (surround.getValue()) {
				BlockPos pos = EntityUtil.getEntityPos(player, true);
				if (!MapleClientMain.HOLE.isHole(pos)) continue;
				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (isObsidian(pos.offset(i))) {
						renderList.add(pos.offset(i));
					}
				}
			}
		}
		for (BlockPos pos : renderList) {
			Render3DUtil.draw3DBox(matrixStack, new Box(pos), color.getValue(), outline.getValue(), box.getValue());
		}
	}

	private boolean isObsidian(BlockPos pos) {
		return (BlockUtil.getBlock(pos) == Blocks.OBSIDIAN || BlockUtil.getBlock(pos) == Blocks.ENDER_CHEST) && !renderList.contains(pos);
	}
}
