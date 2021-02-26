package net.velinquish.oregeneratorplus.commands.anycommands;

import org.bukkit.command.CommandSender;

import net.velinquish.oregeneratorplus.OreGeneratorPlus;
import net.velinquish.utils.AnyCommand;

public class VersionCommand extends AnyCommand {

	private OreGeneratorPlus plugin = OreGeneratorPlus.getInstance();

	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) {
		tellRaw("&8&m= = = = = = = = = = = = = = = = = = = = = = =");
		tellRaw("&3Plugin: &7OreGeneratorPlus");
		tellRaw("&3Version: &7" + plugin.getDescription().getVersion());
		tellRaw("&3Author: &bVelinquish");
		tellRaw("&8&m= = = = = = = = = = = = = = = = = = = = = = =");
		//TODO Add plugin page, other plugins by this author page, and wiki page.
	}

}
