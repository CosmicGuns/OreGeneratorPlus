package net.velinquish.oregeneratorplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;

/**
 * Boundaries are boundaries of chances. For example, if a boundary of 20 were put in, any number from 1-20 would result in that ore.
 * If an additional boundary of 10 were put in, any number from 20-30 would result in that ore. Thus, the user only needs to be
 * concerned with putting in the probability of each ore.
 */

public class Generator {
	@Getter
	private String id;
	@Getter
	private String permission;
	@Getter
	private int interval;
	@Getter
	private int distance;
	@Getter
	private boolean enabledProbability;

	private Map<Integer, ItemStack> ores;
	private int lastBoundary;

	//TODO Gradually change this to builder in preparation for in-game editing
	public Generator(String id, int interval, int minPlayerDistance, boolean probability) {
		ores = new HashMap<>();
		lastBoundary = 0;
		this.id = id;
		this.interval = interval;
		distance = minPlayerDistance;
		enabledProbability = probability;
	}

	public void add(Integer probability, ItemStack ore) {
		lastBoundary += probability;
		ores.put(lastBoundary, ore);
	}

	public Generator setPermission(String perm) {
		permission = perm;
		return this;
	}

	public ItemStack chooseOre() {
		if (!enabledProbability)
			return new ArrayList<>(ores.values()).get((int) (Math.random() * ores.size()));
		int number = (int) (Math.random() * 100 + 1); //Generates random number from 1-100
		for (Integer boundary : ores.keySet())
			if (boundary >= number)
				return ores.get(boundary);
		return new ItemStack(Material.AIR);
	}
}
