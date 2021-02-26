package net.velinquish.oregeneratorplus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import net.velinquish.oregeneratorplus.managers.CommandManager;
import net.velinquish.oregeneratorplus.managers.GeneratorManager;
import net.velinquish.utils.Common;
import net.velinquish.utils.ItemBuilder;
import net.velinquish.utils.VelinquishPlugin;
import net.velinquish.utils.lang.LangManager;

public class OreGeneratorPlus extends JavaPlugin implements VelinquishPlugin {

	@Getter
	private static OreGeneratorPlus instance;

	@Getter
	private static boolean debug;
	@Getter
	private String permission;

	@Getter
	private YamlConfiguration config;
	private File configFile;
	@Getter
	private String prefix;

	private YamlConfiguration lang;
	private File langFile;

	private File locationsFile;
	private YamlConfiguration locations;

	@Getter
	private LangManager langManager;
	@Getter
	private GeneratorManager generatorManager;

	@Override
	public void onEnable() {
		instance = this;
		Common.setInstance(this);

		langManager = new LangManager();

		try {
			loadFiles();
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		Common.registerCommand(new CommandManager(getConfig().getString("main-command")));
	}

	@Override
	public void onDisable() {
		instance = null;
		//TODO to log disable or to not log disable - eh, do it if you have a fancy message :P
	}

	public void loadFiles() throws IOException, InvalidConfigurationException {
		configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			saveResource("config.yml", false);
		}
		config = new YamlConfiguration();
		config.load(configFile);

		debug = getConfig().getBoolean("debug");
		prefix = getConfig().getString("plugin-prefix");
		permission = getConfig().getString("permission");

		langFile = new File(getDataFolder(), "lang.yml");
		if (!langFile.exists()) {
			langFile.getParentFile().mkdirs();
			saveResource("lang.yml", false);
		}
		lang = new YamlConfiguration();
		lang.load(langFile);

		langManager.clear();
		langManager.setPrefix(prefix);
		langManager.loadLang(lang);

		locationsFile = new File(getDataFolder(), "locations.yml");
		if (!locationsFile.exists()) {
			locationsFile.getParentFile().mkdirs();
			saveResource("locations.yml", false);
		}
		locations = new YamlConfiguration();
		locations.load(locationsFile);

		generatorManager = new GeneratorManager(this, locations, locationsFile);

		File generatorsDirectory = new File(getDataFolder().getAbsolutePath() + File.separator + "generators");
		if (!generatorsDirectory.exists()) generatorsDirectory.mkdir();

		if (generatorsDirectory.listFiles().length == 0) {
			File generatorsFile = new File(generatorsDirectory, "generators.yml");
			if (!generatorsFile.exists()) {
				debug("Parent file is " + generatorsFile.getParentFile().getName());
				generatorsFile.getParentFile().mkdirs();
				saveResource("generators.yml", false);
				Files.move(Paths.get("plugins/OreGeneratorPlus/generators.yml"), Paths.get("plugins/OreGeneratorPlus/generators/generators.yml"));
			}
		}

		for (File generatorsFile: generatorsDirectory.listFiles()) {
			YamlConfiguration generatorsConfig = new YamlConfiguration();
			generatorsConfig.load(generatorsFile);
			loadFromConfig(generatorsConfig, generatorsFile, generatorsConfig.getBoolean("advanced-items"));
		}
		generatorManager.initiateGenerators();
	}

	/**
	 * Experimental code that takes variables to save YamlConfiguration and File to and creates file.
	 * public void loadFile(YamlConfiguration configToSaveTo, File fileToSaveTo, String fileName) {
	 * 		fileToSaveTo = new File(getDataFolder(), fileName);
	 * 		if (!fileToSaveTo.exists()) {
	 * 			fileToSaveTo.getParentFile().mkdirs();
	 * 		}
	 * 		configToSaveTo = new YamlConfiguration();
	 * 		configToSaveTo.load(fileToSaveTo);
	 * }
	 */

	public void loadFromConfig(YamlConfiguration generatorsConfig, File generatorsFile, boolean advanced) {
		debug("Loading ores from " + generatorsFile.getName());
		for (String generator : generatorsConfig.getConfigurationSection("Generators").getKeys(false)) {
			Generator gen = new Generator(generator, defaultInt(generatorsConfig, "Generators." + generator, ".interval"), defaultInt(generatorsConfig, "Generators." + generator, ".required-player-distance"), defaultBoolean(generatorsConfig, "Generators." + generator, ".probability.enabled"))
					.setPermission(defaultString(generatorsConfig, "Generators." + generator, ".permission").replaceAll("%type%", generator));
			generatorManager.add(gen);
			for (String drop : generatorsConfig.getConfigurationSection("Generators." + generator + ".drops").getKeys(false))
				gen.add(defaultInt(generatorsConfig, generator, ".drops." + drop + ".probability"), loadFromConfig(generatorsConfig, generatorsFile, generator, drop, advanced, false));
		}
	}

	public ItemStack loadFromConfig(YamlConfiguration generatorsConfig, File generatorsFile, String generator, String drop, boolean advanced, boolean secondTry) {
		if (advanced) {
			if (!generatorsConfig.isItemStack("Generators." + generator + ".drops." + drop + ".item")) {
				ItemStack ore = loadFromConfig(generatorsConfig, generatorsFile, generator, drop, false, true);
				if (!secondTry && ore != null) {
					debug("Changing from basic to advanced item configuration for " + generator);
					saveToConfig(generatorsConfig, generatorsFile, drop, ore, generator);
					return ore;
				}
				return null;
			}
			debug("Successfully loaded drop " + drop + " for " + generator + " as advanced item.");
			return generatorsConfig.getItemStack("Generators." + generator + ".drops." + drop + ".item");
		} else
			try {
				if (!secondTry && generatorsConfig.isItemStack("Generators." + generator + ".drops." + drop + ".item"))
					throw new IllegalArgumentException();
				if (Material.matchMaterial(defaultString(generatorsConfig, "Generators." + generator, ".drops." + drop + ".item.material").toUpperCase()) == null) {
					getLogger().log(Level.SEVERE, "Invalid item material for " + generator + ". See https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html for a list of materials.");
					return null;
				}
				debug("Successfully loaded drop " + drop + " for " + generator + " as basic item.");
				return basicConfiguration(generatorsConfig, generator, drop);
			} catch (IllegalArgumentException e) {
				ItemStack ore = loadFromConfig(generatorsConfig, generatorsFile, generator, drop, true, true);
				if (!secondTry && ore == null) {
					debug("Changing from advanced to basic item configuration for drop " + drop + " of " + generator);
					saveToConfig(generatorsConfig, generatorsFile, drop, generatorsConfig.getItemStack("Generators." + generator + ".drops." + drop + ".item"), generator);
					return ore;
				} else {
					getLogger().log(Level.SEVERE, "Invalid item configuration for " + generator + ". Doesn't match advanced configuration (MetaData) nor basic configuration: " + e.getMessage() + ".");
					return null;
				}
			}
	}

	private ItemStack basicConfiguration(YamlConfiguration generatorsConfig, String generator, String drop) {
		Material item = Material.matchMaterial(defaultString(generatorsConfig, "Generators." + generator, ".drops." + drop + ".item.material").toUpperCase());
		boolean isAir = item.equals(Material.AIR);
		return new ItemBuilder(item,
				isAir ? null : Common.colorize(defaultString(generatorsConfig, "Generators." + generator, ".drops." + drop + ".item.name")))
				.lore(isAir ? null : Common.colorize(defaultStringList(generatorsConfig, "Generators." + generator, ".drops." + drop + ".item.lore")))
				.amount(defaultInt(generatorsConfig, "Generators." + generator, ".drops." + drop + ".item.quantity"))
				.build();
	}

	/**
	 * Writes a single ore to config
	 * @param ore
	 * @param Generator
	 */
	public void saveToConfig(YamlConfiguration generatorsConfig, File generatorsFile, String drop, ItemStack ore, String generator) {
		//TODO To be called upon /generator setOre <generator> in addition to adding it to GeneratorManager
		if (generatorsConfig.getBoolean("advanced-items")) {
			generatorsConfig.set("Generators." + generator + ".drops." + drop + ".item", ore);
			try {
				generatorsConfig.save(generatorsFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			ConfigurationSection item = generatorsConfig.createSection("Generators." + generator + ".drops." + drop + ".item");
			generatorsConfig.set(FileConfiguration.createPath(item, "name"), ore.getItemMeta().getDisplayName());
			generatorsConfig.set(FileConfiguration.createPath(item, "lore"), ore.getItemMeta().getLore());
			generatorsConfig.set(FileConfiguration.createPath(item, "material"), ore.getType().name());
			generatorsConfig.set(FileConfiguration.createPath(item, "quantity"), ore.getAmount());
			try {
				generatorsConfig.save(generatorsFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Replaces with default String if null
	 * @param path
	 * @return
	 */
	public String defaultString(YamlConfiguration generatorsConfig, String loc, String path) {
		String string = generatorsConfig.getString(loc + path);
		if (string == null)
			return getConfig().getString("Default." + path);
		return string;
	}

	public List<String> defaultStringList(YamlConfiguration generatorsConfig, String loc, String path) {
		List<String> list = generatorsConfig.getStringList(loc + path);
		if (list == null)
			return getConfig().getStringList("Default." + path);
		return list;

	}

	public boolean defaultBoolean(YamlConfiguration generatorsConfig, String loc, String path) {
		if (!generatorsConfig.isBoolean(loc + path))
			return getConfig().getBoolean("Default." + path);
		return generatorsConfig.getBoolean(loc + path);
	}

	public int defaultInt(YamlConfiguration generatorsConfig, String loc, String path) {
		if (!generatorsConfig.isInt(loc + path))
			return getConfig().getInt("Default." + path);
		return generatorsConfig.getInt(loc + path);
	}

	public ItemStack defaultItemStack(YamlConfiguration generatorsConfig, String loc, String path) {
		if (!generatorsConfig.isItemStack(loc + path))
			return getConfig().getItemStack("Default." + path);
		return generatorsConfig.getItemStack(loc + path);
	}

	public static void debug(String message) {
		if (debug == true)
			Common.log(message);
	}
}
