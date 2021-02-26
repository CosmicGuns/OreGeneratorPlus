package net.velinquish.oregeneratorplus.commands.anycommands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import net.velinquish.oregeneratorplus.Generator;
import net.velinquish.oregeneratorplus.OreGeneratorPlus;
import net.velinquish.utils.AnyCommand;

public class SetCommand extends AnyCommand {

	private static OreGeneratorPlus plugin = OreGeneratorPlus.getInstance();

	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) {
		OreGeneratorPlus.debug("Permission from main class is null? " + (plugin.getPermission() == null));

		checkPermission(plugin.getPermission());

		checkArgs(2, plugin.getLangManager().getNode("command-set-usage"));
		OreGeneratorPlus.debug("Setting for generator " + args[1]);
		Generator gen = plugin.getGeneratorManager().getGenerator(args[1]);
		if (gen == null) {
			Map<String, String> replace = new HashMap<>();
			replace.put("%generator%", args[1]);
			returnTell(plugin.getLangManager().getNode("invalid-generator").replace(replace));
		}
		OreGeneratorPlus.debug(gen.getPermission());
		checkPermission(gen.getPermission().replaceAll("%type%", gen.getId()));
		Location loc = getLocation(2, plugin.getLangManager().getNode("command-set-usage"), plugin.getLangManager().getNode("command-set-console-usage"));

		plugin.getGeneratorManager().saveLocation(loc, gen);
		plugin.getGeneratorManager().disableGenerators(gen.getId());
		plugin.getGeneratorManager().initiateGenerators(loc, gen);

		Map<String, String> replace = new HashMap<>();
		replace.put("%generator%", args[1]);
		tell(plugin.getLangManager().getNode("location-set").replace(replace));
	}

}
