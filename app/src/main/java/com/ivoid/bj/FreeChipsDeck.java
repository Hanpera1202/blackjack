package com.ivoid.bj;

import java.util.ArrayList;

class FreeChipsDeck
{
    private ArrayList<Card> deck;   // An array of Cards, representing the Deck.
    private Integer level;

    FreeChipsDeck(Integer pLevel)
       {
        level = pLevel;
        deck = new ArrayList<Card>(); // Instantiate the ArrayList.

           fillDeck(); // Fill it with Cards.
        shuffleDeck(); //shuffle the deck
    }
        
    Card drawCard()
    {
        Card card = deck.get(0); // Draw a Card from the top of the deck.
        deck.remove(0); // Remove the Card from the Deck.
        return card; // Return the Card.
    }
    
    private Card popCard(short s)
    {
        Card card = deck.get(s); // Fetch a Card.
        deck.remove(s); // Remove the Card from the Deck.
        return card; // Return the Card.
    }
    
    void fillDeck()
    {
        for ( byte value = 2 ; value < 14 ; value++ ) {
            int cnt = 4;
            if(value < 5){
                cnt = value - 1;
            }
            for (byte suit = 0; suit < cnt; suit++)
                deck.add(new Card(value, suit, true));
        }
        // Level / 5 個分の小さいカードを削除
        int removeCnt = level / 5;
        if(removeCnt > 20){
            removeCnt = 20;
        }
        for(byte i = 0; i < removeCnt; i++) {
            drawCard();
        }
    }

    void shuffleDeck()
    {
        for ( byte t = 0; t < 5; t++ )
        {
            for ( short i = (short)(deck.size()-1); i > 0; i-- )    
        
            {     
                short rand = (short)(Math.random()*i); // Fetch a random Card in the range 0-i         
            
                // Take care of case where indexes rand and i represent the same card.
                while (rand==i){ rand = (short)(Math.random()*i); } 
            
                Card iCard = popCard(i); // Pop the OLD Card.
                Card randCard = popCard(rand); //Pop the NEW random card.
            
                //Put back them in swapped places.
                deck.add(i-1,randCard);  
                deck.add(rand,iCard);
            }
        }
    }
    
    //Accessors
    short cardsLeft()
    { return (short)deck.size(); }
}