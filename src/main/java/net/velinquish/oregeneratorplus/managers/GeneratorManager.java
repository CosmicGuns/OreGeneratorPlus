package net.velinquish.oregeneratorplus.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import net.velinquish.oregeneratorplus.Generator;
import net.velinquish.oregeneratorplus.OreGeneratorPlus;

public class GeneratorManager {
	private OreGeneratorPlus plugin;

	private Map<String, Generator> generators;
	private YamlConfiguration locations;
	private File locationsFile;

	private Map<String, BukkitTask> runningGenerators;

	public GeneratorManager(OreGeneratorPlus plugin, YamlConfiguration locationsConfig, File locationsFile) {
		this.plugin = plugin;
		generators = new HashMap<>();
		locations = locationsConfig;
		this.locationsFile = locationsFile;
		runningGenerators = new HashMap<>();
	}

	public void add(Generator gen) {
		generators.put(gen.getId(), gen);
	}

	public Generator getGenerator(String id) {
		return generators.get(id);
	}

	public List<Generator> getGenerators() {
		return new ArrayList<>(generators.values());
	}

	public void clear() {
		generators.clear();
	}

	public void saveLocation(Location loc, Generator gen) {
		YamlConfiguration.createPath(locations, gen.getId());
		locations.set(gen.getId(), loc);
		try {
			locations.save(locationsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initiateGenerators() {
		for (String gen : locations.getKeys(false))
			initiateGenerators((Location) locations.get(gen), generators.get(gen));
	}

	public int initiateGenerators(String generator) {
		if (runningGenerators.containsKey(generator)) {
			disableGenerators(generator);
			initiateGenerators(generator);
			return 1; //Generator already running
		}
		Location loc = (Location) locations.get(generator);
		if (loc == null)
			return 2; //Generator not set
		initiateGenerators((Location) locations.get(generator), getGenerator(generator));
		return 0; //success
	}

	public void initiateGenerators(Location loc, Generator gen) {
		BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			int dist = gen.getDistance();
			if (dist != -1) {
				for (Player p : loc.getWorld().getPlayers())
					if (p.getLocation().distanceSquared(loc) < dist * dist) {
						loc.getWorld().dropItem(loc, gen.chooseOre()).setVelocity(new Vector(0,0,0));
						return;
					}
			} else //TODO Make configurable required online players requirement
				loc.getWorld().dropItem(loc, gen.chooseOre()).setVelocity(new Vector(0,0,0));
		}, 0, gen.getInterval());
		runningGenerators.put(gen.getId(), task);
	}

	public boolean disableGenerators(String id) {
		if (runningGenerators.containsKey(id)) {
			runningGenerators.get(id).cancel();
			runningGenerators.remove(id);
			return true;
		}
		return false;
	}

	public void disableGenerators() {
		for (BukkitTask task : runningGenerators.values())
			task.cancel();
	}
}
