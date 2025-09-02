package top.maple_bamboo.mod.modules.impl.misc;

import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.mod.modules.impl.client.ClientSetting;
import top.maple_bamboo.mod.modules.settings.impl.BooleanSetting;
import top.maple_bamboo.api.events.eventbus.EventHandler;
import top.maple_bamboo.api.events.impl.DeathEvent;
import top.maple_bamboo.api.events.impl.TotemEvent;
import top.maple_bamboo.core.impl.CommandManager;
import top.maple_bamboo.mod.modules.Module;
import net.minecraft.entity.player.PlayerEntity;

public class PopCounter
        extends Module {

    public static PopCounter INSTANCE;
    public final BooleanSetting unPop =
            add(new BooleanSetting("Dead", true));
    public PopCounter() {
        super("PopCounter", "Counts players totem pops", Category.Misc);
        setChinese("图腾计数器");
        INSTANCE = this;
    }

    @EventHandler
    public void onPlayerDeath(DeathEvent event) {
        PlayerEntity player = event.getPlayer();
        if (MapleClientMain.POP.popContainer.containsKey(player.getName().getString())) {
            int l_Count = MapleClientMain.POP.popContainer.get(player.getName().getString());
            if (l_Count == 1) {
                if (player.equals(mc.player)) {
                    sendMessage("§fYou§r died after popping " + "§f" + l_Count + "§r totem.", player.getId());
                } else {
                    sendMessage("§f" + player.getName().getString() + "§r died after popping " + "§f" + l_Count + "§r totem.", player.getId());
                }
            } else {
                if (player.equals(mc.player)) {
                    sendMessage("§fYou§r died after popping " + "§f" + l_Count + "§r totems.", player.getId());
                } else {
                    sendMessage("§f" + player.getName().getString() + "§r died after popping " + "§f" + l_Count + "§r totems.", player.getId());
                }
            }
        } else if (unPop.getValue()) {
            if (player.equals(mc.player)) {
                sendMessage("§fYou§r died.", player.getId());
            } else {
                sendMessage("§f" + player.getName().getString() + "§r died.", player.getId());
            }
        }
    }

    @EventHandler
    public void onTotem(TotemEvent event) {
        PlayerEntity player = event.getPlayer();
        int l_Count = 1;
        if (MapleClientMain.POP.popContainer.containsKey(player.getName().getString())) {
            l_Count = MapleClientMain.POP.popContainer.get(player.getName().getString());
        }
        if (l_Count == 1) {
            if (player.equals(mc.player)) {
                sendMessage("§fYou§r popped " + "§f" + l_Count + "§r totem.", player.getId());
            } else {
                sendMessage("§f" + player.getName().getString() + " §rpopped " + "§f" + l_Count + "§r totems.", player.getId());
            }
        } else {
            if (player.equals(mc.player)) {
                sendMessage("§fYou§r popped " + "§f" + l_Count + "§r totem.", player.getId());
            } else {
                sendMessage("§f" + player.getName().getString() + " §rhas popped " + "§f" + l_Count + "§r totems.", player.getId());
            }
        }
    }
    
    public void sendMessage(String message, int id) {
        if (!nullCheck()) {
            if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Moon) {
                CommandManager.sendChatMessageWidthId("§f[" + "§3" + getName() + "§f] " + message, id);
                return;
            }
            CommandManager.sendChatMessageWidthId(message, id);//"§6[!] " + message, id);
        }
    }
}

