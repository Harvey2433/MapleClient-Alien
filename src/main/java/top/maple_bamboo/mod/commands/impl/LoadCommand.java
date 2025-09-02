package top.maple_bamboo.mod.commands.impl;

import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.core.Manager;
import top.maple_bamboo.core.impl.CommandManager;
import top.maple_bamboo.core.impl.ConfigManager;
import top.maple_bamboo.mod.commands.Command;

import java.util.List;

public class LoadCommand extends Command {

	public LoadCommand() {
		super("load", "[config]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
		CommandManager.sendChatMessage("§fLoading..");
		ConfigManager.options = Manager.getFile(parameters[0] + ".cfg");
		MapleClientMain.CONFIG = new ConfigManager();
		MapleClientMain.PREFIX = MapleClientMain.CONFIG.getString("prefix", MapleClientMain.PREFIX);
		MapleClientMain.CONFIG.loadSettings();
        ConfigManager.options = Manager.getFile("options.txt");
		MapleClientMain.save();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
