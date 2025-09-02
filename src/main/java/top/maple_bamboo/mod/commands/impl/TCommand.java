package top.maple_bamboo.mod.commands.impl;

import top.maple_bamboo.MapleClientMain;
import top.maple_bamboo.core.impl.CommandManager;
import top.maple_bamboo.core.impl.ModuleManager;
import top.maple_bamboo.mod.commands.Command;
import top.maple_bamboo.mod.modules.Module;

import java.util.ArrayList;
import java.util.List;

public class TCommand extends Command {

	public TCommand() {
		super("t", "[module]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
		String moduleName = parameters[0];
		Module module = MapleClientMain.MODULE.getModuleByName(moduleName);
		if (module == null) {
			CommandManager.sendChatMessage("§fUnknown module!");
			return;
		}
		module.toggle();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		if (count == 1) {
			String input = seperated.get(seperated.size() - 1).toLowerCase();
			ModuleManager cm = MapleClientMain.MODULE;
			List<String> correct = new ArrayList<>();
			for (Module x : cm.modules) {
				if (input.equalsIgnoreCase(MapleClientMain.PREFIX + "toggle") || x.getName().toLowerCase().startsWith(input)) {
					correct.add(x.getName());
				}
			}
			int numCmds = correct.size();
			String[] commands = new String[numCmds];

			int i = 0;
			for (String x : correct) {
				commands[i++] = x;
			}

			return commands;
		}
		return null;
	}
}
