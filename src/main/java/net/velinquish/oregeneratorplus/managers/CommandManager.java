package net.velinquish.oregeneratorplus.managers;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.velinquish.oregeneratorplus.OreGeneratorPlus;
import net.velinquish.oregeneratorplus.commands.anycommands.ListCommand;
import net.velinquish.oregeneratorplus.commands.anycommands.ReloadCommand;
import net.velinquish.oregeneratorplus.commands.anycommands.SetCommand;
import net.velinquish.oregeneratorplus.commands.anycommands.VersionCommand;
import net.velinquish.utils.AnyCommand;

public class CommandManager extends Command {

	private OreGeneratorPlus plugin = OreGeneratorPlus.getInstance();

	public CommandManager(String name) {
		super(name);
		setAliases(plugin.getConfig().getStringList("plugin-aliases"));
		setDescription("Main command for OreGeneratorPlus");
	}

	@Override
	public final boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (args.length > 0)
			if ("set".equalsIgnoreCase(args[0])) {
				handle(new SetCommand(), sender, args);
				return true;
			} else if ("list".equalsIgnoreCase(args[0])) {
				new ListCommand().execute(sender, args, false);
				return true; //TODO do list
			} else if ("enable".equalsIgnoreCase(args[0])) {
				handle(new EnableCommand(), sender, args);
				return true;
			} else if ("disable".equalsIgnoreCase(args[0])) {
				handle(new DisableCommand(), sender, args);
				return true;
			} else if ("reload".equalsIgnoreCase(args[0])) {
				handle(new ReloadCommand(), sender, args);
				return true;
			} else if ("ver".equalsIgnoreCase(args[0]) || "version".equalsIgnoreCase(args[0]) || "about".equalsIgnoreCase(args[0])) {
				new VersionCommand().execute(sender, args, false);
				return true;
			} //TODO In-game editor
		plugin.getLangManager().getNode("command-message").execute(sender);

		return false;
	}

	public void handle(AnyCommand cmd, CommandSender sender, String[] args) {
		cmd.execute(sender, args, Arrays.asList(args).contains("-s"));
	}
}
