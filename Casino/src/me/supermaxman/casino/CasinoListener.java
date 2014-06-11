package me.supermaxman.casino;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;


public class CasinoListener implements Listener {



	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		Action action = event.getAction();
		if (action.equals(Action.RIGHT_CLICK_BLOCK)){
			Player player = event.getPlayer();
			Block block = event.getClickedBlock();
			if ((block.getType().equals(Material.SIGN) || block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN))) {
				Casino.conf = Casino.plugin.getConfig();
				String i = Casino.makeString(block.getLocation());
				if(Casino.conf.contains("slots."+i)) {
					slotGo(block, player);
				}
			}
		}
	}
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player p = event.getPlayer();
		if (p.isOp()) {
			Block block = event.getBlock();
			Material type = block.getType();

			if (!(type.equals(Material.SIGN) || type.equals(Material.SIGN_POST) || type.equals(Material.WALL_SIGN))) {
				return;
			}

			String text[] = event.getLines();
			String line2 = text[1];
			String l1 = "---------------";
			String l2 = "#    #    #";
			String l3 = "---------------";
			String l4 = "{}";
			if (line2.equalsIgnoreCase("[SLOT]")){
				Casino.conf = Casino.plugin.getConfig();
				String i = Casino.makeString(block.getLocation());
				Casino.conf.set("slots."+i, "1");
				Casino.plugin.saveConfig();
				event.setLine(0, l1);
				event.setLine(1, l2);
				event.setLine(2, l3);
				event.setLine(3, l4);
			}
		}

	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		Player p = event.getPlayer();
		Block block = event.getBlock();
		if ((block.getType().equals(Material.SIGN) || block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN))) {
			Casino.conf = Casino.plugin.getConfig();
			String i = Casino.makeString(block.getLocation());
			if(Casino.conf.contains("slots."+i)) {
				if ((p.isOp())) {
					Casino.conf = Casino.plugin.getConfig();
					Casino.conf.set("slots."+i, null);
					Casino.plugin.saveConfig();
				}else{
					event.setCancelled(true);
				}
			}
		}

	}


	public static void slotGo(final Block block, final Player player){
		Casino.conf = Casino.plugin.getConfig();
		final String i = Casino.makeString(block.getLocation());
		if (Casino.conf.getString("slots."+i).toString().equals("1")){
			int price = Casino.conf.getInt("settings.Cost");
			if (Casino.economy.has(player.getName(), price)){
				Casino.conf.set("slots."+i, "0");
				Casino.plugin.saveConfig();
				Casino.economy.withdrawPlayer(player.getName(), price);
				player.sendMessage(ChatColor.AQUA+"You have paid "+ChatColor.GOLD+price+ChatColor.AQUA+" coins to play the slots!");
				final Sign sign = (Sign) block.getState(); 
				signEffect(sign, 10L);
				signEffect(sign, 20L);
				signEffect(sign, 30L);
				signScore(sign, player);
			}else{
				player.sendMessage(ChatColor.RED+ "You do not have enough money!");
			}
		}
	}

	public static void signScore(final Sign sign, final Player player) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Casino.plugin, new Runnable(){
			public void run() {
				sign.getLocation().getWorld().playEffect(sign.getLocation(), Effect.CLICK1, 0);
				String text[] = sign.getLines();
				String line = text[1].replace("    ", "");
				int r1 = new Random().nextInt(100)+1;
				int r2 = new Random().nextInt(100)+1;
				int r3 = new Random().nextInt(100)+1;
				String l1 = "#";
				String l2 = "#";		
				String l3 = "#";
				String[] p = CasinoListener.getPercents();
				l1 = p[r1];
				l2 = p[r2];
				l3 = p[r3];
				
				line = l1 + "    " + l2 + "    " + l3;
				sign.setLine(1, line);	
				sign.update();

				if ((l1.equals(l2))&&(l1.equals(l3))){
					if(l1=="➐"){
						pay(player, Casino.conf.getInt("settings.Seven") * Casino.conf.getInt("settings.TrippleMod"));
					}else if(l1=="❤"){
						pay(player, Casino.conf.getInt("settings.Heart") * Casino.conf.getInt("settings.TrippleMod"));
					}else if(l1=="✔"){
						pay(player, Casino.conf.getInt("settings.Check") * Casino.conf.getInt("settings.TrippleMod"));
					}else if(l1=="♚"){
						pay(player, Casino.conf.getInt("settings.Knight") * Casino.conf.getInt("settings.TrippleMod"));
					}else if(l1=="▲"){
						pay(player, Casino.conf.getInt("settings.UpTriangle") * Casino.conf.getInt("settings.TrippleMod"));
					}else if(l1=="▼"){
						pay(player, Casino.conf.getInt("settings.DownTriangle") * Casino.conf.getInt("settings.TrippleMod"));
					}else if(l1=="●"){
						pay(player, Casino.conf.getInt("settings.Circle") * Casino.conf.getInt("settings.TrippleMod"));
					}else if(l1=="▢"){
						pay(player, Casino.conf.getInt("settings.Square") * Casino.conf.getInt("settings.TrippleMod"));
					}
					sign.getLocation().getWorld().playEffect(sign.getLocation(), Effect.ZOMBIE_CHEW_IRON_DOOR, 0);
				}else if ((l1.equals(l2))||(l2.equals(l3))){
					if(l2=="➐"){
						pay(player, Casino.conf.getInt("settings.Seven") * Casino.conf.getInt("settings.NearMod"));
					}else if(l2=="❤"){
						pay(player, Casino.conf.getInt("settings.Heart") * Casino.conf.getInt("settings.NearMod"));
					}else if(l2=="✔"){
						pay(player, Casino.conf.getInt("settings.Check") * Casino.conf.getInt("settings.NearMod"));
					}else if(l2=="♚"){
						pay(player, Casino.conf.getInt("settings.Knight") * Casino.conf.getInt("settings.NearMod"));
					}else if(l2=="▲"){
						pay(player, Casino.conf.getInt("settings.UpTriangle") * Casino.conf.getInt("settings.NearMod"));
					}else if(l2=="▼"){
						pay(player, Casino.conf.getInt("settings.DownTriangle") * Casino.conf.getInt("settings.NearMod"));
					}else if(l2=="●"){
						pay(player, Casino.conf.getInt("settings.Circle") * Casino.conf.getInt("settings.NearMod"));
					}else if(l2=="▢"){
						pay(player, Casino.conf.getInt("settings.Square") * Casino.conf.getInt("settings.NearMod"));
					}
					sign.getLocation().getWorld().playEffect(sign.getLocation(), Effect.ZOMBIE_CHEW_WOODEN_DOOR, 0);
				}else if ((l1.equals(l3))){
					if(l1=="➐"){
						pay(player, Casino.conf.getInt("settings.Seven") * Casino.conf.getInt("settings.SideMod"));
					}else if(l1=="❤"){
						pay(player, Casino.conf.getInt("settings.Heart") * Casino.conf.getInt("settings.SideMod"));
					}else if(l1=="✔"){
						pay(player, Casino.conf.getInt("settings.Check") * Casino.conf.getInt("settings.SideMod"));
					}else if(l1=="♚"){
						pay(player, Casino.conf.getInt("settings.Knight") * Casino.conf.getInt("settings.SideMod"));
					}else if(l1=="▲"){
						pay(player, Casino.conf.getInt("settings.UpTriangle") * Casino.conf.getInt("settings.SideMod"));
					}else if(l1=="▼"){
						pay(player, Casino.conf.getInt("settings.DownTriangle") * Casino.conf.getInt("settings.SideMod"));
					}else if(l1=="●"){
						pay(player, Casino.conf.getInt("settings.Circle") * Casino.conf.getInt("settings.SideMod"));
					}else if(l1=="▢"){
						pay(player, Casino.conf.getInt("settings.Square") * Casino.conf.getInt("settings.SideMod"));
					}
					sign.getLocation().getWorld().playEffect(sign.getLocation(), Effect.EXTINGUISH, 0);
				}
				

				line = l1 + "    " + l2 + "    " + l3;
				sign.setLine(1, line);	
				sign.update();

				String i1 = Casino.makeString(sign.getLocation());
				Casino.conf.set("slots."+i1, "1");
				Casino.plugin.saveConfig();
			}
		}, 40L);	
	}

	public static void signEffect(final Sign sign, final long delay) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Casino.plugin, new Runnable(){
			public void run() {
				sign.getLocation().getWorld().playEffect(sign.getLocation(), Effect.CLICK2, 0);
				String text[] = sign.getLines();
				String line = text[1].replace("    ", "");
				int r1 = new Random().nextInt(30)+1;
				int r2 = new Random().nextInt(30)+1;
				int r3 = new Random().nextInt(30)+1;
				String l1 = "#";
				String l2 = "#";		
				String l3 = "#";
				String[] p = CasinoListener.getPercents();
				l1 = p[r1];
				l2 = p[r2];
				l3 = p[r3];
				
				line = l1 + "    " + l2 + "    " + l3;
				sign.setLine(1, line);	
				sign.update();
			}
		}, delay);
	}

	public static void pay(Player p, int i) {
		Casino.economy.depositPlayer(p.getName(), i);
		p.sendMessage(ChatColor.AQUA+"You have won "+ChatColor.GOLD+i+ChatColor.AQUA+" coins!");//change messages
	}
	
	public static String[] getPercents() {
		int a = 0;
		String[] s1 = new String[100];
		int i = Casino.conf.getInt("settings.SevenChance");
		while(i>0) {
			s1[a] = "➐";
			i--;
			a++;
		}
		i = Casino.conf.getInt("settings.HeartChance");
		while(i>0) {
			s1[a] = "❤";
			i--;
			a++;
		}
		i = Casino.conf.getInt("settings.CheckChance");
		while(i>0) {
			s1[a] = "✔";
			i--;
			a++;
		}
		i = Casino.conf.getInt("settings.KnightChance");
		while(i>0) {
			s1[a] = "♚";
			i--;
			a++;
		}
		i = Casino.conf.getInt("settings.UpTriangleChance");
		while(i>0) {
			s1[a] = "▲";
			i--;
			a++;
		}
		i = Casino.conf.getInt("settings.DownTriangleChance");
		while(i>0) {
			s1[a] = "▼";
			i--;
			a++;
		}
		i = Casino.conf.getInt("settings.CircleChance");
		while(i>0) {
			s1[a] = "●";
			i--;
			a++;
		}
		i = Casino.conf.getInt("settings.SquareChance");
		while(i>0) {
			s1[a] = "▢";
			i--;
			a++;
		}
		return s1;
	}
}
