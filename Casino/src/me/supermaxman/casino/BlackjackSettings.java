package me.supermaxman.casino;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class BlackjackSettings
{
	private boolean subtractOvercost;
	private boolean saveOnDisable;
	private boolean saveOnSlotChange;
	private boolean linkedToAccounts;
	private int betIncrement;
	private int maxBet;
	LinkedHashMap<String, Object> map = null;
	@SuppressWarnings("unused")
	private Casino bj;

	public BlackjackSettings(Casino bja)
	{
		this.bj = bja;
	}

	public boolean subtractOvercost()
	{
		return this.subtractOvercost;
	}
	public void setSubtractOvercost(boolean val) { this.subtractOvercost = val; } 
	public boolean saveOnDisable() {
		return this.saveOnDisable;
	}
	public void setSaveOnDisable(boolean val) { this.saveOnDisable = val; } 
	public boolean saveOnSlotChange() {
		return this.saveOnSlotChange;
	}
	public void setSaveOnSlotChange(boolean val) { this.saveOnSlotChange = val; } 
	public boolean linkedToAccounts() {
		return this.linkedToAccounts;
	}
	public void setLinkedToAccounts(boolean val) { this.linkedToAccounts = val; } 
	public int betIncrement() {
		return this.betIncrement;
	}
	public int maxBet() { return this.maxBet; }

	public void loadBlackjackSettings(File file)
	{
		if (!file.exists()) {
			try {
				file.createNewFile();
			}
			catch (Exception localException)
			{
			}
			InputStream is = getClass().getResourceAsStream("/blackjack.yml");
			try
			{
				FileOutputStream out = new FileOutputStream(file);

				byte[] bytes = new byte[2000];
				int len;
				while ((len = is.read(bytes)) > 0)
				{
					out.write(bytes, 0, len);
				}out.close();
				is.close();
			}
			catch (FileNotFoundException localFileNotFoundException) {
			}
			catch (IOException localIOException) {
			}
		}
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		this.subtractOvercost = config.getBoolean("subtract-overcost", true);
		this.saveOnDisable = config.getBoolean("save-on-disable", true);
		this.saveOnSlotChange = config.getBoolean("save-on-slot-change", false);
		this.linkedToAccounts = config.getBoolean("linked-to-accounts", true);
		this.betIncrement = config.getInt("betIncrement", 5);
		this.maxBet = config.getInt("maxBet", 100);
		if (this.maxBet == 0)
			this.maxBet = 2147483647;
	}

	public void saveBlackjackSettings(File file)
			throws IOException
			{
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		config.set("subtract-overcost", Boolean.valueOf(this.subtractOvercost));
		config.set("save-on-disable", Boolean.valueOf(this.saveOnDisable));
		config.set("save-on-slot-change", Boolean.valueOf(this.saveOnSlotChange));
		config.set("linked-to-accounts", Boolean.valueOf(this.linkedToAccounts));
		config.set("betIncrement", Integer.valueOf(this.betIncrement));
		config.set("maxBet", Integer.valueOf(this.maxBet));

		config.save(file);
			}
}