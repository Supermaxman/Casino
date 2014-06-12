package me.supermaxman.casino;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;



public class Casino extends JavaPlugin {
	public static Casino plugin;
	public static FileConfiguration conf;
	public static final Logger log = Logger.getLogger("Minecraft");
	private HashMap<String, PlayerState> playerState = new HashMap<String, PlayerState>();
	private BlackjackDataHandler DataHandler = null;
	private File blackjackSaveFile = null; 
	private File blackjackConfigFile = null; 
	private File dataFolder = null; 
	private File saveFolder = null;

	private BlackjackSettings Settings = null;
	public static Economy economy = null;
	protected ArrayList<String> rollers = null;
	public void onEnable() {
		plugin = this;
		if (!setupEconomy() ) {
			log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		this.DataHandler = new BlackjackDataHandler(this);
		
		this.Settings = new BlackjackSettings(this);

		this.rollers = new ArrayList<String>();
		saveDefaultConfig();
		this.blackjackSaveFile = new File("plugins/Casino/Blackjack/tables.yml");
		this.blackjackConfigFile = new File("plugins/Casino/Blackjack/blackjack.yml");
		this.dataFolder = new File("plugins/Casino");
		this.saveFolder = new File("plugins/Casino/Blackjack");

		if (!this.dataFolder.exists()) {
			this.dataFolder.mkdir();
		}
		if (!this.saveFolder.exists()) {
			this.saveFolder.mkdir();
		}
		if (!this.blackjackConfigFile.exists()) try {
			this.Settings.saveBlackjackSettings(this.blackjackConfigFile); } catch (Exception e) { outConsole("Error creating Blackjack save file.");
			}
		if (!this.blackjackSaveFile.exists()) try {
			this.blackjackSaveFile.createNewFile(); } catch (Exception e) { outConsole("Error creating Blackjack save file.");
			}
		this.Settings.loadBlackjackSettings(this.blackjackConfigFile);
		loadAllData();


		getServer().getPluginManager().registerEvents(new CasinoListener(), plugin);
		log.info(getName() + " has been enabled.");
	}
	public static void outConsole(String s)
	{
		log.log(Level.INFO, "[Sign Casino Blackjack] " + s);
	}

	public static void outConsole(Level l, String s)
	{
		log.log(l, "[Sign Casino Blackjack] " + s);
	}
	public void onDisable() {
		for (BlackjackTable table : this.DataHandler.getTableList()) {
			if (table.getInUse()) {
				table.closeTable();
			}
			log.info(getName() + " has been disabled.");
		}
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

	public void loadAllData()
	{
		this.DataHandler.loadBlackjackData(this.blackjackSaveFile);
		this.Settings.loadBlackjackSettings(this.blackjackConfigFile);

		log.info("[Sign Casino Blackjack] All data loaded.");
	}

	public void saveAllData()
			throws IOException
			{
		this.DataHandler.saveBlackjackData(this.blackjackSaveFile);
		this.Settings.saveBlackjackSettings(this.blackjackConfigFile);

		outConsole("[Sign Casino Blackjack] All Blackjack data saved.");
			}

	public File getBlackjackDataFile() {
		return this.blackjackSaveFile;
	}
	public File getBlackjackConfigFile() { return this.blackjackConfigFile; } 
	public BlackjackDataHandler getDataHandler() {
		return this.DataHandler;
	}
	public BlackjackSettings getSettings() { return this.Settings; } 
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player)sender;

		if ((command.getName().equalsIgnoreCase("bj")) || (command.getName().equalsIgnoreCase("blackjack"))) {
			if (args.length == 0) {
				player.sendMessage(ChatColor.DARK_GREEN + "======= Sign Casino Blackjack v" + getDescription().getVersion() + " =======");
				player.sendMessage(ChatColor.DARK_GREEN + "/Blackjack" + ChatColor.GOLD + " info");
				player.sendMessage(ChatColor.DARK_GREEN + "/Blackjack" + ChatColor.GOLD + " deposit <amount>");
				player.sendMessage(ChatColor.DARK_GREEN + "/Blackjack" + ChatColor.GOLD + " withdraw <amount>");
				player.sendMessage(ChatColor.DARK_GREEN + "/Blackjack" + ChatColor.GOLD + " save");
				player.sendMessage(ChatColor.DARK_GREEN + "/Blackjack" + ChatColor.GOLD + " load");
				return true;
			}

			for (int i = 0; i < args.length; i++) {
				args[i] = args[i].toLowerCase();
			}
			if (args[0].equals("info"))
			{
				this.playerState.put(player.getName(), PlayerState.INFO);

				player.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "Left click a Blackjack Table to get its info.");
			}
			else if (args[0].equals("deposit"))
			{
				if ((args.length == 1) || (this.Settings.linkedToAccounts())) {
					return false;
				}
				PlayerState state = PlayerState.DEPOSIT;

				double amount = 0.0D;
				try
				{
					amount = Double.parseDouble(args[1]);
					if (amount < 0.0D) {
						player.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You can't deposit negative amounts.");
						amount = 0.0D;
						return true;
					}
				}
				catch (Exception e)
				{
					player.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "Error getting deposit amount.");
					return false;
				}

				state.setVal(amount);

				this.playerState.put(player.getName(), state);

				player.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "Left click a Blackjack Table to deposit money into it.");
			}
			else if (args[0].equals("withdraw"))
			{
				if ((args.length == 1) || (this.Settings.linkedToAccounts())) {
					return false;
				}
				PlayerState state = PlayerState.WITHDRAW;

				double amount = 0.0D;
				try
				{
					amount = Double.parseDouble(args[1]);
					if (amount < 0.0D) {
						player.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You can't withdraw negative amounts.");
						amount = 0.0D;
						return true;
					}
				}
				catch (Exception e)
				{
					player.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "Error getting withdraw amount.");
					return false;
				}

				state.setVal(amount);

				this.playerState.put(player.getName(), state);

				player.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "Left click a Blackjack Table to withdraw money from it.");
			}
			else if (args[0].equals("save"))
			{
				if (!player.isOp())
				{
					player.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You aren't allowed to save data!");
					return false;
				}

				try
				{
					saveAllData();
				}
				catch (IOException localIOException) {
				}
				player.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "Saved data successfully.");
			}
			else if (args[0].equals("load"))
			{
				if (!player.isOp())
				{
					player.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You aren't allowed to reload data!");
					return false;
				}

				loadAllData();
				player.sendMessage(ChatColor.GREEN + "Reloaded data successfully.");
			}
			else {
				player.sendMessage(ChatColor.DARK_GREEN + "======= Sign Casino Blackjack v" + getDescription().getVersion() + " =======");
				player.sendMessage(ChatColor.DARK_GREEN + "/Blackjack" + ChatColor.GOLD + " info");
				player.sendMessage(ChatColor.DARK_GREEN + "/Blackjack" + ChatColor.GOLD + " deposit <amount>");
				player.sendMessage(ChatColor.DARK_GREEN + "/Blackjack" + ChatColor.GOLD + " withdraw <amount>");
				player.sendMessage(ChatColor.DARK_GREEN + "/Blackjack" + ChatColor.GOLD + " save");
				player.sendMessage(ChatColor.DARK_GREEN + "/Blackjack" + ChatColor.GOLD + " load");
			}
		}

		return true;
	}


	public PlayerState getState(Player player)
	{
		PlayerState state = (PlayerState)this.playerState.remove(player.getName());

		if (state == null) {
			return PlayerState.NONE;
		}
		return state;
	}

	public void removeState(Player player)
	{
		this.playerState.remove(player.getName());
	}

	public static enum PlayerState
	{
		NONE, 
		DELETE, 
		INFO, 
		DEPOSIT, 
		WITHDRAW;

		double val;

		public void setVal(double arg) {
			this.val = arg;
		}

		public double getVal() {
			return this.val;
		}
	}
}

