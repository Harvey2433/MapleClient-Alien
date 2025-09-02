package top.maple_bamboo.mod.commands.impl;

import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.core.impl.CommandManager;
import top.maple_bamboo.core.impl.ConfigManager;
import top.maple_bamboo.mod.commands.Command;

import java.util.List;

public class ReloadCommand extends Command {

	public ReloadCommand() {
		super("reload", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("§fReloading..");
		MapleClientMain.CONFIG = new ConfigManager();
		MapleClientMain.PREFIX = MapleClientMain.CONFIG.getString("prefix", MapleClientMain.PREFIX);
		MapleClientMain.CONFIG.loadSettings();
		MapleClientMain.XRAY.read();
		MapleClientMain.TRADE.read();
		MapleClientMain.FRIEND.read();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
