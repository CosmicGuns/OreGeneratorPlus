package net.velinquish.oregeneratorplus.commands.anycommands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

import net.velinquish.oregeneratorplus.Generator;
import net.velinquish.oregeneratorplus.OreGeneratorPlus;
import net.velinquish.utils.AnyCommand;

public class ListCommand extends AnyCommand {

	private OreGeneratorPlus plugin = OreGeneratorPlus.getInstance();

	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) {
		checkPermission(plugin.getConfig().getString("permission"));

		tell(plugin.getLangManager().getNode("generator-list-heading"));

		for (Generator gen : plugin.getGeneratorManager().getGenerators()) {
			Map<String, String> replace = new HashMap<>();
			replace.put("%generator%", gen.getId());
			tell(plugin.getLangManager().getNode("generator-list-item").replace(replace));
		}
	}

}
