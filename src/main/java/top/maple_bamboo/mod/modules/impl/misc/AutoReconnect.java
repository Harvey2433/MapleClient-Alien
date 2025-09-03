package top.maple_bamboo.mod.modules.impl.misc;

import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.api.events.eventbus.EventHandler;
import top.maple_bamboo.api.events.impl.ServerConnectBeginEvent;
import top.maple_bamboo.api.utils.entity.InventoryUtil;
import top.maple_bamboo.api.utils.math.Timer;
import top.maple_bamboo.mod.modules.Module;
import top.maple_bamboo.mod.modules.settings.impl.BooleanSetting;
import top.maple_bamboo.mod.modules.settings.impl.SliderSetting;
import top.maple_bamboo.mod.modules.settings.impl.StringSetting;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoReconnect extends Module {
    public final BooleanSetting rejoin = add(new BooleanSetting("Rejoin", true));
    public final SliderSetting delay =
            add(new SliderSetting("Delay", 5, 0, 20,.1).setSuffix("s"));
    public final BooleanSetting autoLogin = add(new BooleanSetting("AutoAuth", true));
    public final SliderSetting afterLoginTime =
            add(new SliderSetting("AfterLoginTime", 3, 0, 10,.1).setSuffix("s"));
    private final StringSetting password = add(new StringSetting("password", "123456"));
    public final BooleanSetting autoQueue = add(new BooleanSetting("AutoQueue", true));
    public final SliderSetting joinQueueDelay =
            add(new SliderSetting("JoinQueueDelay", 3, 0, 10,.1).setSuffix("s"));
    public Pair<ServerAddress, ServerInfo> lastServerConnection;

    public static AutoReconnect INSTANCE;

    // 异步执行器
    private ScheduledExecutorService executor;
    private final AtomicBoolean executorActive = new AtomicBoolean(false);

    public AutoReconnect() {
        super("AutoReconnect", Category.Misc);
        setChinese("自动重连");
        INSTANCE = this;
        MapleClientMain.EVENT_BUS.subscribe(new StaticListener());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        // 启用模块时创建执行器
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor();
            executorActive.set(true);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        // 禁用模块时关闭执行器
        executorActive.set(false);
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        // 重置状态
        waitingForMenu = false;
    }

    private final Timer queueTimer = new Timer();
    private final Timer timer = new Timer();
    private boolean login = false;

    // 新增状态变量
    private boolean waitingForMenu = false;
    private final Timer menuTimer = new Timer();

    @Override
    public void onUpdate() {
        if (login && timer.passedS(afterLoginTime.getValue())) {
            mc.getNetworkHandler().sendChatCommand("login " + password.getValue());
            login = false;
        }

        if (autoQueue.getValue() && executorActive.get()) {
            if (!waitingForMenu) {
                // 第一次点击：物品栏中的指南针
                if (InventoryUtil.findItem(Items.COMPASS) != -1 && queueTimer.passedS(joinQueueDelay.getValue())) {
                    InventoryUtil.switchToSlot(InventoryUtil.findItem(Items.COMPASS));
                    sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
                    queueTimer.reset();
                    waitingForMenu = true;
                    menuTimer.reset(); // 重置菜单计时器

                    // 异步检查菜单并点击
                    scheduleMenuCheck();
                }
            }
        }
    }

    @Override
    public void onLogin() {
        if (autoLogin.getValue()) {
            login = true;
            timer.reset();
        }
        // 重置状态
        waitingForMenu = false;
    }

    public boolean rejoin() {
        return isOn() && rejoin.getValue();
    }

    /**
     * 异步检查菜单并点击指南针
     */
    private void scheduleMenuCheck() {
        if (!executorActive.get() || executor == null || executor.isShutdown()) {
            return;
        }

        try {
            executor.schedule(() -> {
                if (!executorActive.get()) return;

                // 在主线程执行GUI操作
                mc.execute(() -> {
                    if (!isOn() || !executorActive.get()) return;

                    if (mc.currentScreen instanceof HandledScreen) {
                        int compassSlot = findCompassSlotInScreen();
                        if (compassSlot != -1) {
                            clickSlotInScreen(compassSlot);
                            waitingForMenu = false; // 重置状态
                        } else {
                            // 如果没找到，稍后重试
                            if (executorActive.get()) {
                                executor.schedule(this::scheduleMenuCheck, 200, TimeUnit.MILLISECONDS);
                            }
                        }
                    } else if (menuTimer.passedMs(5000)) {
                        // 如果超过5秒还没找到菜单，重置状态
                        waitingForMenu = false;
                    } else {
                        // 如果菜单还没打开，稍后重试
                        if (executorActive.get()) {
                            executor.schedule(this::scheduleMenuCheck, 200, TimeUnit.MILLISECONDS);
                        }
                    }
                });
            }, 500, TimeUnit.MILLISECONDS); // 延迟500ms确保菜单已打开
        } catch (Exception e) {
            // 处理可能的执行异常
            waitingForMenu = false;
        }
    }

    /**
     * 在打开的屏幕中查找指南针槽位
     * @return 指南针槽位索引，如果未找到则返回-1
     */
    private int findCompassSlotInScreen() {
        if (mc.currentScreen instanceof HandledScreen) {
            HandledScreen<?> screen = (HandledScreen<?>) mc.currentScreen;
            for (int i = 0; i < screen.getScreenHandler().slots.size(); i++) {
                Slot slot = screen.getScreenHandler().getSlot(i);
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty() && stack.getItem() == Items.COMPASS) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 点击屏幕中的指定槽位
     * @param slot 要点击的槽位索引
     */
    private void clickSlotInScreen(int slot) {
        if (mc.currentScreen instanceof HandledScreen && mc.interactionManager != null) {
            HandledScreen<?> screen = (HandledScreen<?>) mc.currentScreen;
            mc.interactionManager.clickSlot(
                    screen.getScreenHandler().syncId,
                    slot,
                    0, // 按钮
                    SlotActionType.PICKUP,
                    mc.player
            );
        }
    }

    private class StaticListener {
        @EventHandler
        private void onGameJoined(ServerConnectBeginEvent event) {
            lastServerConnection = new ObjectObjectImmutablePair<>(event.address, event.info);
        }
    }
}