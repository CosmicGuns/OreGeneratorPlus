package net.velinquish.oregeneratorplus.commands.anycommands;

import java.io.IOException;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;

import net.velinquish.oregeneratorplus.OreGeneratorPlus;
import net.velinquish.utils.AnyCommand;
import net.velinquish.utils.Common;

public class ReloadCommand extends AnyCommand {

	private OreGeneratorPlus plugin = OreGeneratorPlus.getInstance();

	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) {
		checkPermission(plugin.getPermission());
		try {
			plugin.getGeneratorManager().disableGenerators();
			plugin.loadFiles();
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
			Common.tell(sender, "An error has occurred when reloading the configuration files!");
		}
		tell(plugin.getLangManager().getNode("plugin-reloaded"));
	}

}
