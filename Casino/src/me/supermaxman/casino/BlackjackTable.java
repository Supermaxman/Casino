package me.supermaxman.casino;

import java.util.ArrayList;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlackjackTable
{
	private PlayerInteractEvent e;
	private Player p = null;
	private String owner;
	private String winner;
	private String user = null;
	private double amount;
	private Block block;
	private int handsPlayed;
	private int maxBet;
	private ArrayList<Card> playerCards = new ArrayList<Card>();
	private ArrayList<Card> dealerCards = new ArrayList<Card>();
	private boolean inUse = false; private boolean playerBusted = false; private boolean dealerBusted = false;
	private int currentBet;
	private String stage;
	private Casino plugin;
	private String blackjack = ChatColor.YELLOW + "[Blackjack]";
	private Sign s;

	public BlackjackTable(String o, double a, Block b, int h, String m, Casino pl)
	{
		this.owner = o;
		this.amount = a;
		this.block = b;
		this.handsPlayed = h;
		this.s = ((Sign)this.block.getState());
		this.plugin = pl;
		this.currentBet = this.plugin.getSettings().betIncrement();
		this.maxBet = Integer.parseInt(m);
	}
	public int getUses() {
		return this.handsPlayed;
	}
	public double getAmount() { return this.amount; } 
	public void subtractAmount(double amt) {
		this.amount -= amt;
	}
	public void addAmount(double amt) { this.amount += amt; } 
	public void setAmount(double amt) {
		this.amount = amt;
	}
	public String getOwner() { return this.owner; } 
	public String getUser() {
		return this.user;
	}
	public int getMaxBet() { return this.maxBet; } 
	public boolean getInUse() {
		return this.inUse;
	}
	public void setInUse(boolean bool) { this.inUse = bool; } 
	public Block getBlock() {
		return this.block;
	}

	public boolean isOwner(Player player) {
		return this.owner.equalsIgnoreCase(player.getName());
	}

	public boolean equals(Object o)
	{
		if ((o instanceof BlackjackTable))
		{
			return ((BlackjackTable)o).getBlock().getLocation().equals(this.block.getLocation());
		}
		if ((o instanceof Block))
		{
			return ((Block)o).getLocation().equals(this.block.getLocation());
		}
		if ((o instanceof Location))
		{
			return ((Location)o).equals(this.block.getLocation());
		}

		return false;
	}

	public void runGame(PlayerInteractEvent event)
	{
		this.e = event;
		this.p = this.e.getPlayer();
		this.s = ((Sign)this.e.getClickedBlock().getState());
		if (!this.inUse) {
			if (this.e.getAction() == Action.LEFT_CLICK_BLOCK) {
				return;
			}
			if ((this.amount <= 0.0D) && (isOwned()) && (!Casino.plugin.getSettings().linkedToAccounts())) {
				this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "This Blackjack Table doesn't have enough money in it!");
				return;
			}
			this.user = this.p.getName();
			if (Casino.plugin.getDataHandler().getUserList().contains(this.user)) {
				this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You are already using another Table!");
				closeTable();
				Casino.plugin.getDataHandler().getUserList().add(this.user);
				return;
			}
			Casino.plugin.getDataHandler().getUserList().add(this.user);
			this.inUse = true;
			this.stage = "betting";
			this.currentBet = Casino.plugin.getSettings().betIncrement();
			updateSign(this.blackjack, "", ChatColor.WHITE + "Betting: " + this.currentBet, "");
		}
		else {
			if (!this.user.equals(this.p.getName())) {
				this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "This Blackjack Table is Already in Use.");
				return;
			}
			runMode();
		}
	}

	public void runMode()
	{
		if (this.stage.equals("betting"))
			betting();
		else if (this.stage.equals("dealing"))
			dealing();
		else if (this.stage.equals("idle"))
			idleTable();
		else if (this.stage.equals("ended")) {
			handEnded();
		}
		if (this.stage.equals("idle"))
			dealWinnings();
	}

	public void betting()
	{
		if (this.e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (this.currentBet >= this.maxBet) {
				this.currentBet = this.maxBet;
				return;
			}
			this.currentBet += Casino.plugin.getSettings().betIncrement();
			this.s.setLine(2, ChatColor.WHITE + "Betting: " + this.currentBet);
			this.s.update();
			return;
		}if (this.e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (!Casino.economy.has(this.p.getName(), this.currentBet)) {
				this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You can't bet that much.");
				closeTable();
				return;
			}
			Casino.economy.withdrawPlayer(this.p.getName(), this.currentBet);

			if (Casino.plugin.getSettings().linkedToAccounts()) {
				if (isOwned())
					Casino.economy.depositPlayer(this.p.getName(), this.currentBet);
				else
					this.amount += this.currentBet;
			}
			else {
				this.amount += this.currentBet;
			}

			this.stage = "dealing";
			Card c = dealCard(this.playerCards);
			String name = c.getName() + " of " + c.getSuit();
			c = dealCard(this.playerCards);
			String name2 = c.getName() + " of " + c.getSuit();
			this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You were dealt the " + name + " and the " + name2 + ".");
			if (checkBlackjack(this.playerCards)) {
				c = dealCard(this.dealerCards);
				updateSign(this.blackjack, ChatColor.YELLOW + "You: " + ChatColor.WHITE + getTotal(this.playerCards), ChatColor.YELLOW + "[Hit]  [Stay]", ChatColor.YELLOW + "Dealer:" + ChatColor.WHITE + getTotal(this.dealerCards) + " ?");
				this.stage = "idle";
				return;
			}
			checkBust(this.playerCards);
			c = dealCard(this.dealerCards);
			updateSign(this.blackjack, ChatColor.YELLOW + "You: " + ChatColor.WHITE + getTotal(this.playerCards), ChatColor.YELLOW + "[Hit]  [Stay]", ChatColor.YELLOW + "Dealer:" + ChatColor.WHITE + getTotal(this.dealerCards) + " ?");
		}
	}

	public void dealing() {
		if (this.e.getAction() == Action.LEFT_CLICK_BLOCK) {
			Card c = dealCard(this.playerCards);
			if (checkBust(this.playerCards)) {
				dealerTurn();
				return;
			}
			this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You were dealt the " + c.getName() + " of " + c.getSuit() + ". (" + ChatColor.WHITE + getTotal(this.playerCards) + ChatColor.DARK_GREEN + ")");
			updateSign(this.blackjack, ChatColor.YELLOW + "You: " + ChatColor.WHITE + getTotal(this.playerCards), ChatColor.YELLOW + "[Hit]  [Stay]", ChatColor.YELLOW + "Dealer:" + ChatColor.WHITE + getTotal(this.dealerCards) + " ?");
			if (checkBlackjack(this.playerCards)) {
				updateSign(this.blackjack, ChatColor.YELLOW + "You: " + ChatColor.WHITE + getTotal(this.playerCards), ChatColor.YELLOW + "[Hit]  [Stay]", ChatColor.YELLOW + "Dealer:" + ChatColor.WHITE + getTotal(this.dealerCards) + " ?");
				this.stage = "idle";
				return;
			}
		} else {
			dealerTurn();
		}
	}

	public void idleTable() {
		updateSign(ChatColor.YELLOW + "Left-Click", ChatColor.WHITE + "Leave Table", ChatColor.YELLOW + "Right-Click", ChatColor.WHITE + "to Continue");
		this.stage = "ended";
		this.handsPlayed += 1;
	}

	public void handEnded() {
		this.playerCards.clear();
		this.dealerCards.clear();
		this.playerBusted = false;
		this.dealerBusted = false;
		this.currentBet = Casino.plugin.getSettings().betIncrement();
		if (this.e.getAction() == Action.LEFT_CLICK_BLOCK) {
			closeTable();
		} else {
			this.stage = "betting";
			updateSign(this.blackjack, "", ChatColor.WHITE + "Betting: " + this.currentBet, "");
		}
	}

	public void dealerTurn() {
		Card c = dealCard(this.dealerCards);
		checkBust(this.dealerCards);
		this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "Dealer reveals a " + c.getName() + " of " + c.getSuit() + ". (" + ChatColor.WHITE + getTotal(this.dealerCards) + ChatColor.DARK_GREEN + ")");
		if (this.playerBusted)
			updateSign(this.blackjack, ChatColor.YELLOW + "You: " + ChatColor.DARK_RED + "Bust", ChatColor.YELLOW + "[Hit]  [Stay]", ChatColor.YELLOW + "Dealer: " + ChatColor.WHITE + getTotal(this.dealerCards));
		else {
			updateSign(this.blackjack, ChatColor.YELLOW + "You: " + ChatColor.WHITE + getTotal(this.playerCards), ChatColor.YELLOW + "[Hit]  [Stay]", ChatColor.YELLOW + "Dealer: " + ChatColor.WHITE + getTotal(this.dealerCards));
		}
		while (getTotal(this.dealerCards) < 17) {
			c = dealCard(this.dealerCards);
			if (this.playerBusted)
				updateSign(this.blackjack, ChatColor.YELLOW + "You: " + ChatColor.DARK_RED + " Bust", ChatColor.YELLOW + "[Hit]  [Stay]", ChatColor.YELLOW + "Dealer: " + ChatColor.WHITE + getTotal(this.dealerCards));
			else {
				updateSign(this.blackjack, ChatColor.YELLOW + "You: " + ChatColor.WHITE + getTotal(this.playerCards), ChatColor.YELLOW + "[Hit]  [Stay]", ChatColor.YELLOW + "Dealer: " + ChatColor.WHITE + getTotal(this.dealerCards));
			}
			this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "Dealer hits and gets a " + c.getName() + " of " + c.getSuit() + ". (" + ChatColor.WHITE + getTotal(this.dealerCards) + ChatColor.DARK_GREEN + ")");
			if (checkBlackjack(this.playerCards)) {
				return;
			}
			checkBust(this.dealerCards);
		}
		this.stage = "idle";
	}

	public int getTotal(ArrayList<Card> a)
	{
		int tmp = 0;
		for (Card card : a) {
			tmp += card.getValue();
		}
		return tmp;
	}

	public boolean checkBlackjack(ArrayList<Card> a) {
		if (getTotal(a) == 21) {
			if (a.equals(this.playerCards)) {
				this.winner = "player";
				this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You got Blackjack!");
				return true;
			}
			this.winner = "dealer";
			return true;
		}

		return false;
	}

	public boolean checkBust(ArrayList<Card> a) {
		boolean busted = false;
		if (getTotal(a) > 21) {
			for (Card card : a) {
				if ((card.getName().equals("Ace")) && (card.getValue() == 11)) {
					card.setValue(1);
					updateSign(this.blackjack, ChatColor.YELLOW + "You: " + ChatColor.WHITE + getTotal(this.playerCards), ChatColor.YELLOW + "[Hit]  [Stay]", ChatColor.YELLOW + "Dealer:" + ChatColor.WHITE + getTotal(this.dealerCards));
					return busted;
				}
			}
			if (getTotal(a) <= 21) {
				return busted;
			}
			if (a.equals(this.playerCards)) {
				this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You have Busted! (" + ChatColor.WHITE + getTotal(a) + ChatColor.DARK_GREEN + ")");
				this.playerBusted = true;
				busted = true;
				updateSign(this.blackjack, ChatColor.YELLOW + "You: " + ChatColor.DARK_RED + "Bust", ChatColor.YELLOW + "[Hit]  [Stay]", ChatColor.YELLOW + "Dealer:" + ChatColor.WHITE + getTotal(this.dealerCards) + " ?");
			} else if (a.equals(this.dealerCards)) {
				this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "Dealer has Busted!");
				this.dealerBusted = true;
				busted = true;
				this.stage = "idle";
				if (getTotal(this.playerCards) > 21)
					updateSign(this.blackjack, ChatColor.YELLOW + "You: " + ChatColor.DARK_RED + "Bust", ChatColor.YELLOW + "[Hit]  [Stay]", ChatColor.YELLOW + "Dealer:" + ChatColor.DARK_RED + "Bust");
				else {
					updateSign(this.blackjack, ChatColor.YELLOW + "You: " + ChatColor.WHITE + getTotal(this.playerCards), ChatColor.YELLOW + "[Hit]  [Stay]", ChatColor.YELLOW + "Dealer:" + ChatColor.DARK_RED + "Bust");
				}
			}
		}
		return busted;
	}

	public Card dealCard(ArrayList<Card> a) {
		boolean bool = false;
		Card c = null;
		while (!bool) {
			c = new Card();
			if (!c.checkDuplicate(a)) {
				a.add(c);
				bool = true;
			}
		}
		return c;
	}

	public void closeTable() {
		updateSign(this.blackjack, "", "", "");
		this.stage = "";
		this.user = "";
		this.playerCards.clear();
		this.dealerCards.clear();
		this.playerBusted = false;
		this.dealerBusted = false;
		this.currentBet = Casino.plugin.getSettings().betIncrement();
		this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "Table Closed.");
		Casino.plugin.getDataHandler().getUserList().remove(this.p.getName());
		this.inUse = false;
	}

	public void closeTable(Player p) {
		updateSign(this.blackjack, "", "", "");
		this.stage = "";
		this.user = "";
		this.playerCards.clear();
		this.dealerCards.clear();
		this.playerBusted = false;
		this.dealerBusted = false;
		this.currentBet = Casino.plugin.getSettings().betIncrement();
		p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "Table Closed.");
		Casino.plugin.getDataHandler().getUserList().remove(p.getName());
		this.inUse = false;
	}

	public void updateSign(String zero, String one, String two, String three) {
		this.s.setLine(0, zero);
		this.s.setLine(1, one);
		this.s.setLine(2, two);
		this.s.setLine(3, three);
		this.s.update();
	}

	public void dealWinnings() {
		double pay = 0.0D;
		if ((this.playerBusted) && (this.dealerBusted)) {
			this.winner = "tie";
			this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You tied and broke even.");
		} else if ((getTotal(this.dealerCards) < getTotal(this.playerCards)) && (!this.playerBusted)) {
			this.winner = "player";
			this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You Won " + ChatColor.WHITE + this.currentBet * 2 + " " + ChatColor.DARK_GREEN + Casino.economy.currencyNamePlural() + ".");
		} else if ((getTotal(this.dealerCards) < getTotal(this.playerCards)) && (this.playerBusted)) {
			this.winner = "dealer";
			this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You Lost " + ChatColor.WHITE + this.currentBet + " " + ChatColor.DARK_GREEN + Casino.economy.currencyNamePlural() + ".");
		} else if (getTotal(this.dealerCards) == getTotal(this.playerCards)) {
			this.winner = "tie";
			this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You tied and broke even.");
		} else if ((getTotal(this.dealerCards) > getTotal(this.playerCards)) && (!this.dealerBusted)) {
			this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You Lost " + ChatColor.WHITE + this.currentBet + " " + ChatColor.DARK_GREEN + Casino.economy.currencyNamePlural() + ".");
			this.winner = "dealer";
		} else {
			this.p.sendMessage(ChatColor.GOLD + "[Blackjack] " + ChatColor.DARK_GREEN + "You Won " + ChatColor.WHITE + this.currentBet * 2 + ChatColor.DARK_GREEN + " " + Casino.economy.currencyNamePlural() + ".");
			this.winner = "player";
		}
		if (this.winner.equals("player"))
			pay = this.currentBet * 2;
		else if (this.winner.equals("tie"))
			pay = this.currentBet;
		else {
			pay = 0.0D;
		}


		if (!Casino.plugin.getSettings().linkedToAccounts())
		{
			if (isOwned())
			{
				if (pay > getAmount())
				{
					if (!Casino.plugin.getSettings().subtractOvercost())
					{
						this.p.sendMessage("The pay went over the table's balance, so you only won " + Casino.economy.format(getAmount()));
						pay = getAmount();
					}
					else if (Casino.economy.has(getOwner(), pay - getAmount()))
					{
						Casino.economy.withdrawPlayer(getOwner(), pay - getAmount());
						this.p.sendMessage(ChatColor.GREEN + "The pay went over the table's balance, the owner paid " + Casino.economy.format(pay - getAmount()) + " directly out of their account.");
					}
					else
					{
						pay = getAmount();
						pay +=  Casino.economy.getBalance(getOwner());

						Casino.economy.depositPlayer(getOwner(), 0.0D);

						this.p.sendMessage(ChatColor.GREEN + "The pay went over the table's balance and the owner's account balance, you only won " + Casino.economy.format(pay));
					}

					if (isOwned())
						setAmount(0.0D);
				}
				else
				{
					subtractAmount(pay);
				}
			}
			else
			{
				Casino.economy.depositPlayer(this.p.getName(),pay);
			}

		}
		else if (isOwned())
		{
			if (pay > Casino.economy.getBalance(getOwner()))
			{
				pay = Casino.economy.getBalance(getOwner());
				this.p.sendMessage(ChatColor.GREEN + "The pay went over the owner's account balance, you only won " + Casino.economy.format(pay) + ".");
				Casino.economy.depositPlayer(getOwner(), 0.0D);
			}
			else
			{
				Casino.economy.withdrawPlayer(getOwner(), pay);
			}

		}

		this.winner = "";
	}

	public boolean isOwned()
	{
		boolean owned = false;
		if (this.owner != null) {
			owned = true;
		}
		return owned;
	}
}