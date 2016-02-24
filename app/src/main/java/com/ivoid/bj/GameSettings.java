package com.ivoid.bj;

import java.util.HashMap;
import java.util.Map;

class GameSettings
{
	byte decks, splits, burns, shuffleTimes; 
	int startCash, tableMin;
	float bjPay, surrenderPay, insurancePay, winPay;
	boolean ddPostSplit, surrender, insurance, stand17soft, aceResplit, dd1011;
    int loginBonusSeconds;
	int loginBonusCoins;
	int coinBonusCount;
    int necessaryPoint;
    int freeChipsSeconds;
    int freeChipsFlips;
    int playCountUpAds;
	
	private boolean[] bools={true,false};
	private byte[] bytes={6,8};

    Map<Integer, LevelInfo> levels;

	GameSettings()
	{
		bjPay=1.5f; surrenderPay=-0.5f; insurancePay=2f; winPay=1f;
		decks=1; splits=1; burns=(byte)(5.0*Math.random()); shuffleTimes = 3;
		startCash=1000; tableMin=10;
		ddPostSplit=true; surrender=true; insurance=true; stand17soft=true; aceResplit=true; dd1011=true;
        loginBonusSeconds=28800;freeChipsSeconds=3600;loginBonusCoins=3;coinBonusCount=3;
        freeChipsFlips = 3;
        necessaryPoint=10000;
        playCountUpAds = 20;

        levels = new HashMap<Integer, LevelInfo>();
        levels.put(  1,new LevelInfo(  1,13  ,13   ,3 ,  50));
        levels.put(  2,new LevelInfo(  2,26  ,39   ,3 , 100));
        levels.put(  3,new LevelInfo(  3,39  ,78   ,3 , 150));
        levels.put(  4,new LevelInfo(  4,52  ,130  ,3 , 200));
        levels.put(  5,new LevelInfo(  5,65  ,195  ,3 , 250));
        levels.put(  6,new LevelInfo(  6,78  ,273  ,3 , 300));
        levels.put(  7,new LevelInfo(  7,91  ,364  ,3 , 350));
        levels.put(  8,new LevelInfo(  8,104 ,468  ,3 , 400));
        levels.put(  9,new LevelInfo(  9,117 ,585  ,3 , 450));
        levels.put( 10,new LevelInfo( 10,130 ,715  ,4 , 500));
        levels.put( 11,new LevelInfo( 11,154 ,869  ,4 , 550));
        levels.put( 12,new LevelInfo( 12,168 ,1037 ,4 , 600));
        levels.put( 13,new LevelInfo( 13,182 ,1219 ,4 , 650));
        levels.put( 14,new LevelInfo( 14,196 ,1415 ,4 , 700));
        levels.put( 15,new LevelInfo( 15,210 ,1625 ,4 , 750));
        levels.put( 16,new LevelInfo( 16,224 ,1849 ,4 , 800));
        levels.put( 17,new LevelInfo( 17,238 ,2087 ,4 , 850));
        levels.put( 18,new LevelInfo( 18,252 ,2339 ,4 , 900));
        levels.put( 19,new LevelInfo( 19,266 ,2605 ,4 , 950));
        levels.put( 20,new LevelInfo( 20,280 ,2885 ,5 ,1000));
        levels.put( 21,new LevelInfo( 21,315 ,3200 ,5 ,1050));
        levels.put( 22,new LevelInfo( 22,330 ,3530 ,5 ,1100));
        levels.put( 23,new LevelInfo( 23,345 ,3875 ,5 ,1150));
        levels.put( 24,new LevelInfo( 24,360 ,4235 ,5 ,1200));
        levels.put( 25,new LevelInfo( 25,375 ,4610 ,5 ,1250));
        levels.put( 26,new LevelInfo( 26,390 ,5000 ,5 ,1300));
        levels.put( 27,new LevelInfo( 27,405 ,5405 ,5 ,1350));
        levels.put( 28,new LevelInfo( 28,420 ,5825 ,5 ,1400));
        levels.put( 29,new LevelInfo( 29,435 ,6260 ,5 ,1450));
        levels.put( 30,new LevelInfo( 30,450 ,6710 ,6 ,1500));
        levels.put( 31,new LevelInfo( 31,496 ,7206 ,6 ,1550));
        levels.put( 32,new LevelInfo( 32,512 ,7718 ,6 ,1600));
        levels.put( 33,new LevelInfo( 33,528 ,8246 ,6 ,1650));
        levels.put( 34,new LevelInfo( 34,544 ,8790 ,6 ,1700));
        levels.put( 35,new LevelInfo( 35,560 ,9350 ,6 ,1750));
        levels.put( 36,new LevelInfo( 36,576 ,9926 ,6 ,1800));
        levels.put( 37,new LevelInfo( 37,592 ,10518,6 ,1850));
        levels.put( 38,new LevelInfo( 38,608 ,11126,6 ,1900));
        levels.put( 39,new LevelInfo( 39,624 ,11750,6 ,1950));
        levels.put( 40,new LevelInfo( 40,640 ,12390,7 ,2000));
        levels.put( 41,new LevelInfo( 41,697 ,13087,7 ,2050));
        levels.put( 42,new LevelInfo( 42,714 ,13801,7 ,2100));
        levels.put( 43,new LevelInfo( 43,731 ,14532,7 ,2150));
        levels.put( 44,new LevelInfo( 44,748 ,15280,7 ,2200));
        levels.put( 45,new LevelInfo( 45,765 ,16045,7 ,2250));
        levels.put( 46,new LevelInfo( 46,782 ,16827,7 ,2300));
        levels.put( 47,new LevelInfo( 47,799 ,17626,7 ,2350));
        levels.put( 48,new LevelInfo( 48,816 ,18442,7 ,2400));
        levels.put( 49,new LevelInfo( 49,833 ,19275,7 ,2450));
        levels.put( 50,new LevelInfo( 50,850 ,20125,8 ,2500));
        levels.put( 51,new LevelInfo( 51,918 ,21043,8 ,2550));
        levels.put( 52,new LevelInfo( 52,936 ,21979,8 ,2600));
        levels.put( 53,new LevelInfo( 53,954 ,22933,8 ,2650));
        levels.put( 54,new LevelInfo( 54,972 ,23905,8 ,2700));
        levels.put( 55,new LevelInfo( 55,990 ,24895,8 ,2750));
        levels.put( 56,new LevelInfo( 56,1008,25903,8 ,2800));
        levels.put( 57,new LevelInfo( 57,1026,26929,8 ,2850));
        levels.put( 58,new LevelInfo( 58,1044,27973,8 ,2900));
        levels.put( 59,new LevelInfo( 59,1062,29035,8 ,2950));
        levels.put( 60,new LevelInfo( 60,1080,30115,9 ,3000));
        levels.put( 61,new LevelInfo( 61,1159,31274,9 ,3050));
        levels.put( 62,new LevelInfo( 62,1178,32452,9 ,3100));
        levels.put( 63,new LevelInfo( 63,1197,33649,9 ,3150));
        levels.put( 64,new LevelInfo( 64,1216,34865,9 ,3200));
        levels.put( 65,new LevelInfo( 65,1235,36100,9 ,3250));
        levels.put( 66,new LevelInfo( 66,1254,37354,9 ,3300));
        levels.put( 67,new LevelInfo( 67,1273,38627,9 ,3350));
        levels.put( 68,new LevelInfo( 68,1292,39919,9 ,3400));
        levels.put( 69,new LevelInfo( 69,1311,41230,9 ,3450));
        levels.put( 70,new LevelInfo( 70,1330,42560,10,3500));
        levels.put( 71,new LevelInfo( 71,1420,43980,10,3550));
        levels.put( 72,new LevelInfo( 72,1440,45420,10,3600));
        levels.put( 73,new LevelInfo( 73,1460,46880,10,3650));
        levels.put( 74,new LevelInfo( 74,1480,48360,10,3700));
        levels.put( 75,new LevelInfo( 75,1500,49860,10,3750));
        levels.put( 76,new LevelInfo( 76,1520,51380,10,3800));
        levels.put( 77,new LevelInfo( 77,1540,52920,10,3850));
        levels.put( 78,new LevelInfo( 78,1560,54480,10,3900));
        levels.put( 79,new LevelInfo( 79,1580,56060,10,3950));
        levels.put( 80,new LevelInfo( 80,1600,57660,11,4000));
        levels.put( 81,new LevelInfo( 81,1701,59361,11,4050));
        levels.put( 82,new LevelInfo( 82,1722,61083,11,4100));
        levels.put( 83,new LevelInfo( 83,1743,62826,11,4150));
        levels.put( 84,new LevelInfo( 84,1764,64590,11,4200));
        levels.put( 85,new LevelInfo( 85,1785,66375,11,4250));
        levels.put( 86,new LevelInfo( 86,1806,68181,11,4300));
        levels.put( 87,new LevelInfo( 87,1827,70008,11,4350));
        levels.put( 88,new LevelInfo( 88,1848,71856,11,4400));
        levels.put( 89,new LevelInfo( 89,1869,73725,11,4450));
        levels.put( 90,new LevelInfo( 90,1890,75615,12,4500));
        levels.put( 91,new LevelInfo( 91,2002,77617,12,4550));
        levels.put( 92,new LevelInfo( 92,2024,79641,12,4600));
        levels.put( 93,new LevelInfo( 93,2046,81687,12,4650));
        levels.put( 94,new LevelInfo( 94,2068,83755,12,4700));
        levels.put( 95,new LevelInfo( 95,2090,85845,12,4750));
        levels.put( 96,new LevelInfo( 96,2112,87957,12,4800));
        levels.put( 97,new LevelInfo( 97,2134,90091,12,4850));
        levels.put( 98,new LevelInfo( 98,2156,92247,12,4900));
        levels.put( 99,new LevelInfo( 99,2178,94425,12,4950));
        levels.put(100,new LevelInfo(100,2200,96625,12,5000));
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