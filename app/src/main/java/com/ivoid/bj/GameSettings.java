package com.ivoid.bj;

import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

class GameSettings
{
	byte decks, splits, burns, shuffleTimes; 
	float startCash, tableMin, tableMax;
	float bjPay, surrenderPay, insurancePay, winPay;
	boolean ddPostSplit, surrender, insurance, stand17soft, aceResplit, dd1011;
	
	private boolean[] bools={true,false};
	private byte[] bytes={6,8};

    private Map<Integer, Integer> levels;
    private Map<Integer, Integer> avelableBets;
	
	GameSettings()
	{
		bjPay=1.5f; surrenderPay=-0.5f; insurancePay=2f; winPay=1f;
		decks=1; splits=1; burns=(byte)(5.0*Math.random()); shuffleTimes = 3;
		startCash=100000f; tableMin=10f; tableMax= 10000f;
		ddPostSplit=true; surrender=false; insurance=false; stand17soft=true; aceResplit=true; dd1011=true;
/*
        levels = new HashMap<Integer, Integer>();
        levels.put(1,0);
        levels.put(2,1);
        levels.put(3,0);
        levels.put(4,2);
        levels.put(5,0);
        levels.put(6,2);
        levels.put(7,0);
        levels.put(8,2);
        levels.put(9,0);
        levels.put(10,2);
        levels.put(11,0);
        levels.put(12,2);
        levels.put(13,0);
        levels.put(14,2);
        levels.put(15,0);
        levels.put(16,2);
        levels.put(17,0);
        levels.put(18,2);
        levels.put(19,0);
        levels.put(20,2);
        levels.put(21,0);
        levels.put(22,2);
        levels.put(23,0);
        levels.put(24,2);
        levels.put(25,0);
        levels.put(26,2);
        levels.put(27,0);
        levels.put(28,2);
        levels.put(29,0);
        levels.put(30,2);
        levels.put(31,0);
        levels.put(32,2);
        levels.put(33,0);
        levels.put(34,2);
        levels.put(35,0);
        levels.put(36,2);
        levels.put(37,0);
        levels.put(38,2);
        levels.put(39,2);
        levels.put(40,2);
        levels.put(41,0);
        levels.put(42,2);
        levels.put(43,0);
        levels.put(44,2);
        levels.put(45,0);
        levels.put(46,2);
        levels.put(47,0);
        levels.put(48,2);
        levels.put(49,2);
        levels.put(50,2);

        avelableBets = new HashMap<Integer, Integer>();
        avelableBets.put(1,1);
        avelableBets.put(2,5);
        avelableBets.put(3,10);
        avelableBets.put(4,25);
        avelableBets.put(5,50);
        avelableBets.put(6,75);
        avelableBets.put(7,100);
        avelableBets.put(8,125);
        avelableBets.put(9,150);
        avelableBets.put(10,200);
        avelableBets.put(11,250);
        avelableBets.put(12,300);
        avelableBets.put(13,350);
        avelableBets.put(14,400);
        avelableBets.put(15,500);
        avelableBets.put(16,600);
        avelableBets.put(17,700);
        avelableBets.put(18,800);
        avelableBets.put(19,900);
        avelableBets.put(20,1000);
        avelableBets.put(21,1250);
        avelableBets.put(22,1500);
        avelableBets.put(23,1750);
        avelableBets.put(24,2000);
        avelableBets.put(25,2500);
        avelableBets.put(26,3000);
        avelableBets.put(27,3500);
        avelableBets.put(28,4000);
        avelableBets.put(29,4500);
        avelableBets.put(30,5000);
        avelableBets.put(31,5500);
        avelableBets.put(32,6000);
        avelableBets.put(33,6500);
        avelableBets.put(34,7000);
        avelableBets.put(35,7500);
        avelableBets.put(36,8000);
        avelableBets.put(37,8500);
        avelableBets.put(38,9000);
        avelableBets.put(39,9500);
        avelableBets.put(40,11000);
        avelableBets.put(41,12000);
        avelableBets.put(42,13000);
        avelableBets.put(43,14000);
        avelableBets.put(44,15000);
        avelableBets.put(45,20000);
        avelableBets.put(46,30000);
        avelableBets.put(47,40000);
        avelableBets.put(48,50000);
        avelableBets.put(49,100000);
        avelableBets.put(50,1000000);
**/
    }
	
	private boolean sometimes(boolean[] choices)
	{
		short i=(short)Math.round(Math.random());
		if (i==1){ return choices[0]; }
		else{ return choices[1]; }	
	}
	
	private byte sometimes(byte[] choices)
	{
		short s=(short)Math.round(Math.random());
		if (s==1){ return choices[0]; }
		else{ return choices[1]; }
	}
	
	void vegasStrip()
	{
		insurance=false;
		decks=4;
		stand17soft=false;
		dd1011=false;
	}
	
	void downtownVegas()
	{
		dd1011=false;
		surrender=false;
		ddPostSplit=sometimes(bools);
		aceResplit=sometimes(bools);
	}
	
	void rino()
	{	
		insurance=sometimes(bools);
		surrender=false;
		aceResplit=false;	
	}
	
	void atlanticCity()
	{
		decks=sometimes(bytes);
		stand17soft=false;
		dd1011=false;
		surrender=false;
		aceResplit=sometimes(bools);	
	}
}