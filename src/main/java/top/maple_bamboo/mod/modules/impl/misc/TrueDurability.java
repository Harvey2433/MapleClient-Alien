package top.maple_bamboo.mod.modules.impl.misc;

import top.maple_bamboo.api.events.eventbus.EventHandler;
import top.maple_bamboo.api.events.impl.DurabilityEvent;
import top.maple_bamboo.mod.modules.Module;

public class TrueDurability extends Module {

    public TrueDurability() {
        super("TrueDurability", Category.Misc);
        setChinese("耐久度修正");
    }

    @EventHandler
    public void onDurability(DurabilityEvent event) {
        int dura = event.getItemDamage();
        if (event.getDamage() < 0) {
            dura = event.getDamage();
        }
        event.cancel();
        event.setDamage(dura);
    }
}
