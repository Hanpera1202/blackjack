package com.ivoid.bj;

class BJHand extends Hand
{
	private boolean busted = false, // Did the hand bust?
					hasBJ  = false, // Does the hand have blackjack?
					has21  = false, // Does the hand have 21?
					didDD  = false; // Did the hand double down?
	
	private byte hardValue, softValue; // Hand's hard and soft BJ values.
		
	BJHand(String name)
	{ super(name); }
	
	BJHand(String name, Card card, int betValue)
	{ super(name,card,betValue); }
	
	void update()
	{
		// hand.removeAll(hand); // Clear hand of cards.
		hand.clear();
		
		busted = false;
		hasBJ  = false;
		has21  = false;
		didDD  = false;
		hasAce = false;
		
		hardValue = 0;
		softValue = 0;	
	}
	
	void takeDD()
	{ didDD = true; }
	
	void checkIfBusted()
	{
		if (hardValue > 21)		
			busted = true;
	}
	
    void checkIfHasBJ()
	{
		if (hardValue == 21 && getCardCount() == 2)
			hasBJ = true;
	}

	void checkIfHas21()
	{
		if (hardValue == 21 || softValue == 21)
			has21 = true;
	}

	boolean splitable(boolean aceResplit)
    {
		if (getCardCount() > 2)		return false;

		//Values of first and second cards in hand.
		byte first  = hand.get((byte)0).getValue(), 
			 second = hand.get((byte)1).getValue(); 
		
		if (first == 1 && second == 1)
		{
			if (aceResplit)	return true;
			else			return false;
		}
		return (first == second);
	}
	
	void addCard(Card c) 
	{
		if (c != null)	hand.add(c); // add a card to the hand
		calculateBJValue();
	}
	
	private void calculateBJValue() 
	{ 
		 // Determines the soft and hard values of the hand in the game of 21.

		 hardValue=0;	// The value computed for the hand. 0 to begin with.
		 
		 boolean face=false;	// True if there is at least one face card. False by default.

		 for ( Card card: hand )	
		 {
			 int cardValue = card.getValue();  // The normal (index) value, 1 to 13.
			 
			 if (cardValue >= 10)
			 {
				 face = true;
				 cardValue = 10;   // For a Jack, Queen, or King. 
			 }
			 
			 else if (cardValue == 1)
				 hasAce = true;
				
			 hardValue += cardValue; 
		 }
		 
		 if ( hasAce && hardValue<=11 )	 
		 {	
			 softValue = (byte)(hardValue + 10);
			 if (face)	{
				 hardValue=softValue;
				 softValue=0;
			 }
		 }
		 else	softValue = 0;
	}
	
	//Accessors
	boolean hasBJ()
	{ return hasBJ; }

	boolean has21()
	{ return has21; }

	boolean didBust()
	{ return busted; }
	
	boolean didDD()
	{ return didDD; }

	byte getBJValue()
	{
		if(hardValue > softValue){
			return hardValue;
		}else{
			return softValue;
		}
	}
		
	byte getHardBJValue()
	{ return hardValue; }
	
	byte getSoftBJValue()
	{ return softValue; }
}