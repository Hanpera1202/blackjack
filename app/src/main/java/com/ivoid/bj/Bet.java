package com.ivoid.bj;

class Bet 
{
    private int betValue;
    
    Bet()
    { betValue=0; }
    
    Bet(int betValue) //used when splitting
    { this.betValue = betValue; }
    
    void clear()
    { betValue=0; }
    
    void incrementBet(int stake)
    {
        betValue+=stake;
    }
    
    //Accessors
    int getValue()
    { return betValue; }

}