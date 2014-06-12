package me.supermaxman.casino;

import java.util.ArrayList;
import java.util.Random;

public class Card
{
	Random r = new Random();
	String suit;
	String name;
	int value;

	public Card()
	{
		this.value = (this.r.nextInt(13) + 1);
		setName();
		pickSuit();
	}

	public String getSuit() {
		return this.suit;
	}
	public String getName() { return this.name; } 
	public int getValue() {
		return this.value;
	}
	public void setValue(int v) { this.value = v; }

	public void setName()
	{
		if (this.value == 10) {
			this.name = "Ten";
		} else if (this.value == 11) {
			this.name = "Jack";
			this.value = 10;
		} else if (this.value == 12) {
			this.name = "Queen";
			this.value = 10;
		} else if (this.value == 13) {
			this.name = "King";
			this.value = 10;
		} else if (this.value == 1) {
			this.name = "Ace";
			this.value = 11;
		} else if (this.value == 2) {
			this.name = "Two";
		} else if (this.value == 3) {
			this.name = "Three";
		} else if (this.value == 4) {
			this.name = "Four";
		} else if (this.value == 5) {
			this.name = "Five";
		} else if (this.value == 6) {
			this.name = "Six";
		} else if (this.value == 7) {
			this.name = "Seven";
		} else if (this.value == 8) {
			this.name = "Eight";
		} else if (this.value == 9) {
			this.name = "Nine";
		}
	}

	public void pickSuit()
	{
		int temp = this.r.nextInt(4) + 1;
		if (temp == 1)
			this.suit = "Hearts";
		else if (temp == 2)
			this.suit = "Diamonds";
		else if (temp == 3)
			this.suit = "Spades";
		else if (temp == 4)
			this.suit = "Clubs";
	}

	public boolean checkDuplicate(ArrayList<Card> a)
	{
		boolean duplicate = false;
		for (Card card : a) {
			if ((this.name.equals(card.getName())) && (this.suit.equals(card.getSuit()))) {
				duplicate = true;
			}
		}
		return duplicate;
	}
}