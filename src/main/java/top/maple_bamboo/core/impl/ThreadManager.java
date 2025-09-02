package top.maple_bamboo.core.impl;

import top.maple_bamboo.api.utils.world.BlockUtil;
import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.api.events.eventbus.EventHandler;
import top.maple_bamboo.api.events.eventbus.EventPriority;
import top.maple_bamboo.api.events.impl.TickEvent;
import top.maple_bamboo.mod.modules.impl.render.PlaceRender;

public class ThreadManager {
    public static ClientService clientService;

    public ThreadManager() {
        MapleClientMain.EVENT_BUS.subscribe(this);
        clientService = new ClientService();
        clientService.setName("MapleClientService");
        clientService.setDaemon(true);
        clientService.start();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(TickEvent event) {
        if (event.isPre()) {
            if (!clientService.isAlive()) {
                clientService = new ClientService();
                clientService.setName("MapleClientService");
                clientService.setDaemon(true);
                clientService.start();
            }
            BlockUtil.placedPos.forEach(pos -> PlaceRender.renderMap.put(pos, PlaceRender.INSTANCE.create(pos)));
            BlockUtil.placedPos.clear();
            MapleClientMain.SERVER.onUpdate();
            MapleClientMain.PLAYER.onUpdate();
            MapleClientMain.MODULE.onUpdate();
            MapleClientMain.GUI.onUpdate();
            MapleClientMain.POP.onUpdate();
        }
    }

    public static class ClientService extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (MapleClientMain.MODULE != null) {
                        MapleClientMain.MODULE.onThread();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
