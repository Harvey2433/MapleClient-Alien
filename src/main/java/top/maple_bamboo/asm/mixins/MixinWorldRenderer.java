package top.maple_bamboo.asm.mixins;

import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.core.impl.ShaderManager;
import top.maple_bamboo.mod.modules.impl.player.Freecam;
import top.maple_bamboo.mod.modules.impl.render.Shader;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import static top.maple_bamboo.api.utils.Wrapper.mc;
@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal = 0))
	void replaceShaderHook(PostEffectProcessor instance, float tickDelta) {
		ShaderManager.Shader shaders = Shader.INSTANCE.mode.getValue();
		if (Shader.INSTANCE.isOn() && mc.world != null) {
			MapleClientMain.SHADER.setupShader(shaders, MapleClientMain.SHADER.getShaderOutline(shaders));
		} else {
			instance.render(tickDelta);
		}
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"), index = 3)
	private boolean renderSetupTerrainModifyArg(boolean spectator) {
		return Freecam.INSTANCE.isOn() || spectator;
	}
}
