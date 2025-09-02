package top.maple_bamboo.mod.modules.impl.player.freelook;

import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.api.events.eventbus.EventHandler;
import top.maple_bamboo.api.events.impl.Render3DEvent;
import top.maple_bamboo.mod.modules.Module;

public class FreeLook extends Module {
    public static FreeLook INSTANCE;
    public FreeLook() {
        super("FreeLook", Category.Player);
        setChinese("自由视角");
        camera = new CameraState();
        INSTANCE = this;
        MapleClientMain.EVENT_BUS.subscribe(new FreeLookUpdate());
    }

    private final CameraState camera;

    public CameraState getCameraState() {
        return camera;
    }

    public class FreeLookUpdate {
        @EventHandler
        public void onRender3D(Render3DEvent event) {
            CameraState camera = getCameraState();
            var doLock = isOn() && !camera.doLock;
            var doUnlock = !isOn() && camera.doLock;

            if (doLock) {
                if (!camera.doTransition) {
                    camera.lookYaw = camera.originalYaw();
                    camera.lookPitch = camera.originalPitch();
                }

                camera.doLock = true;
            }

            if (doUnlock) {
                camera.doLock = false;
                camera.doTransition = true;

                camera.transitionInitialYaw = camera.lookYaw;
                camera.transitionInitialPitch = camera.lookPitch;
            }
        }
    }

}
