package me.supermaxman.casino;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;



public class Casino extends JavaPlugin {
	public static Casino plugin;
	public static FileConfiguration conf;
	public static final Logger log = Logger.getLogger("Minecraft");

	public static Economy economy = null;

	public void onEnable() {
		plugin = this;
		if (!setupEconomy() ) {
			log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(new CasinoListener(), plugin);
		log.info(getName() + " has been enabled.");
	}

	public void onDisable() {
		log.info(getName() + " has been disabled.");
	}

	public static String makeString(Location loc) {
		return loc.getWorld().getName() + "&&" + loc.getBlockX() + "&&" + loc.getBlockY() + "&&" + loc.getBlockZ(); 
	}

	public static Location makeLocation(String s) {
		String[] loc = s.split("&&");

		return new Location(Casino.plugin.getServer().getWorld(loc[0]), Integer.parseInt(loc[1]), Integer.parseInt(loc[2]), Integer.parseInt(loc[3])); 
	}



	private boolean setupEconomy(){
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		
		return (economy != null);
	}
}