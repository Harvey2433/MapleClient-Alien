package top.maple_bamboo.mod.commands.impl;

import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.core.impl.CommandManager;
import top.maple_bamboo.mod.commands.Command;

import java.util.List;

public class ReloadAllCommand extends Command {

	public ReloadAllCommand() {
		super("reloadall", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("§fReloading..");
		MapleClientMain.unload();
        try {
            MapleClientMain.load();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
