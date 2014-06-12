package me.supermaxman.casino;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class BlackjackDataHandler
{
	private ArrayList<BlackjackTable> tableList = new ArrayList<BlackjackTable> ();
	private ArrayList<String> userList = new ArrayList<String>();
	private Casino plugin;
	private FileConfiguration tablesConfig;

	public BlackjackDataHandler(Casino pl)
	{
		plugin = pl;
	}

	public ArrayList<BlackjackTable> getTableList()
	{
		return this.tableList;
	}
	public ArrayList<String> getUserList() { return this.userList; }

	public BlackjackTable getTable(Block block)
	{
		for (BlackjackTable table : this.tableList) {
			if (table.equals(block))
				return table;
		}
		return null;
	}

	public void addBlackjackTable(String owner, double amount, int uses, String maxBet, Block block)
			throws IOException
			{
		this.tableList.add(new BlackjackTable(owner, 0.0D, block, uses, maxBet, Casino.plugin));
			}

	public boolean removeBlackjackTable(Block block)
	{
		for (BlackjackTable table : this.tableList)
		{
			if (!table.equals(block))
				continue;
			this.tableList.remove(table);
			if ((table.getAmount() > 0.0D) && (table.isOwned())) {
				Casino.economy.depositPlayer(table.getOwner(), table.getAmount());
				table.setAmount(0.0D);
			}
			return true;
		}

		return false;
	}

	public boolean removeBlackjackTable(BlackjackTable table)
	{
		if ((table.getAmount() > 0.0D) && (table.isOwned())) {
			Casino.economy.depositPlayer(table.getOwner(), table.getAmount());
			table.setAmount(0.0D);
		}

		return this.tableList.remove(table);
	}

	public void saveBlackjackData(File file)
			throws IOException
			{
		int tableNum = 0;
		file.delete();
		this.tablesConfig = YamlConfiguration.loadConfiguration(file);

		for (BlackjackTable table : this.tableList)
		{
			String key = "tables." + tableNum + ".";

			Object[] list = new Object[4];

			list[0] = table.getBlock().getWorld().getName();
			list[1] = Integer.valueOf(table.getBlock().getX());
			list[2] = Integer.valueOf(table.getBlock().getY());
			list[3] = Integer.valueOf(table.getBlock().getZ());

			this.tablesConfig.set(key + "loc", list);
			if (table.getOwner() == null)
				this.tablesConfig.set(key + "owner", "");
			else {
				this.tablesConfig.set(key + "owner", table.getOwner());
			}
			this.tablesConfig.set(key + "uses", Integer.valueOf(table.getUses()));
			this.tablesConfig.set(key + "amount", Double.valueOf(table.getAmount()));
			this.tablesConfig.set(key + "maxBet", Integer.valueOf(table.getMaxBet()));

			tableNum++;
		}

		this.tablesConfig.save(file);
			}

	public void loadBlackjackData(File file)
	{
		this.tablesConfig = YamlConfiguration.loadConfiguration(file);

		ConfigurationSection tablesSection = this.tablesConfig.getConfigurationSection("tables");
		try
		{
			Set<String> tables = tablesSection.getKeys(false);
			if (tables == null) {
				return;
			}
			for (String key : tables)
			{
				try
				{
					List<?> loc = this.tablesConfig.getList("tables." + key + ".loc");

					String worldname = (String)loc.get(0);

					World world = Casino.plugin.getServer().getWorld(worldname);

					int x = ((Integer)loc.get(1)).intValue();
					int y = ((Integer)loc.get(2)).intValue();
					int z = ((Integer)loc.get(3)).intValue();
					int uses = this.tablesConfig.getInt("tables." + key + ".uses", 0);
					String owner = this.tablesConfig.getString("tables." + key + ".owner");
					int maxBet = this.tablesConfig.getInt("tables." + key + ".maxBet");
					if ((owner != null) && (owner.isEmpty()))
						owner = null;
					double amount = this.tablesConfig.getDouble("tables." + key + ".amount", 0.0D);

					Block block = new Location(world, x, y, z).getBlock();
					if ((block.getType() != Material.SIGN_POST) && (block.getType() != Material.WALL_SIGN))
					{
						System.out.println("[Slots] Error key " + key + " is not a sign!");
					}
					else
					{
						BlackjackTable table = new BlackjackTable(owner, amount, block, uses, maxBet+"", Casino.plugin);

						this.tableList.add(table);
					}
				}
				catch (Exception e)
				{
					System.out.println("[Slots] Error when loading slot data at key : " + key + ".");
				}
			}
		}
		catch (Exception localException1)
		{
		}
	}
}