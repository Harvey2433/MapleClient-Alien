package top.maple_bamboo.mod.commands.impl;

import top.maple_bamboo.core.Manager;
import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.core.impl.CommandManager;
import top.maple_bamboo.core.impl.ConfigManager;
import top.maple_bamboo.mod.commands.Command;

import java.util.List;

public class SaveCommand extends Command {

	public SaveCommand() {
		super("save", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 1) {
			CommandManager.sendChatMessage("§fSaving config named " + parameters[0]);
			ConfigManager.options = Manager.getFile(parameters[0] + ".cfg");
			MapleClientMain.save();
			ConfigManager.options = Manager.getFile("options.txt");
		} else {
			CommandManager.sendChatMessage("§fSaving..");
		}
		MapleClientMain.save();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
