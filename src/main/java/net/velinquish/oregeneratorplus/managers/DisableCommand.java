package net.velinquish.oregeneratorplus.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

import net.velinquish.oregeneratorplus.Generator;
import net.velinquish.oregeneratorplus.OreGeneratorPlus;
import net.velinquish.utils.AnyCommand;

public class DisableCommand extends AnyCommand {

	private OreGeneratorPlus plugin = OreGeneratorPlus.getInstance();

	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) {
		checkPermission(plugin.getPermission());

		checkArgs(2, plugin.getLangManager().getNode("command-disable-usage"));
		Generator gen = plugin.getGeneratorManager().getGenerator(args[1]);
		checkNotNull(gen, plugin.getLangManager().getNode("invalid-generator"));
		checkPermission(gen.getPermission());

		Map<String, String> replace = new HashMap<>();
		replace.put("%generator%", args[1]);

		if (!plugin.getGeneratorManager().disableGenerators(args[1]))
			tell(plugin.getLangManager().getNode("generator-already-disabled").replace(replace));
		else
			tell(plugin.getLangManager().getNode("generator-disabled").replace(replace));
	}

}
