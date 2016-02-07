package com.ivoid.bj;

import java.util.HashMap;
import java.util.Map;

class GameSettings
{
	byte decks, splits, burns, shuffleTimes; 
	float startCash, tableMin;
	float bjPay, surrenderPay, insurancePay, winPay;
	boolean ddPostSplit, surrender, insurance, stand17soft, aceResplit, dd1011;
    int loginBonusSeconds;
	int loginBonusCoins;
	int coninBonusCount;
    int necessaryPoint;
    int freeChipsSeconds;
    int freeChipsFlips;
	
	private boolean[] bools={true,false};
	private byte[] bytes={6,8};

    Map<Integer, LevelInfo> levels;

	GameSettings()
	{
		bjPay=1.5f; surrenderPay=-0.5f; insurancePay=2f; winPay=1f;
		decks=1; splits=1; burns=(byte)(5.0*Math.random()); shuffleTimes = 3;
		startCash=1000f; tableMin=10f;
		ddPostSplit=true; surrender=true; insurance=true; stand17soft=true; aceResplit=true; dd1011=true;
        loginBonusSeconds=28800;freeChipsSeconds=3600;loginBonusCoins=3;coninBonusCount=3;
        //loginBonusSeconds=10;freeChipsSeconds=3600;loginBonusCoins=3;coninBonusCount=3;
        freeChipsFlips = 3;
        necessaryPoint=10000;

        levels = new HashMap<Integer, LevelInfo>();
        levels.put(  1,new LevelInfo(  1,  10,   10,3,  50));
        levels.put(  2,new LevelInfo(  2,  20,   30,3, 100));
        levels.put(  3,new LevelInfo(  3,  30,   60,3, 150));
        levels.put(  4,new LevelInfo(  4,  40,  100,3, 200));
        levels.put(  5,new LevelInfo(  5,  50,  150,3, 250));
        levels.put(  6,new LevelInfo(  6,  60,  210,3, 300));
        levels.put(  7,new LevelInfo(  7,  70,  280,3, 350));
        levels.put(  8,new LevelInfo(  8,  80,  360,3, 400));
        levels.put(  9,new LevelInfo(  9,  90,  450,3, 450));
        levels.put( 10,new LevelInfo( 10, 100,  550,3, 500));
        levels.put( 11,new LevelInfo( 11, 110,  660,3, 550));
        levels.put( 12,new LevelInfo( 12, 120,  780,3, 600));
        levels.put( 13,new LevelInfo( 13, 130,  910,3, 650));
        levels.put( 14,new LevelInfo( 14, 140, 1050,3, 700));
        levels.put( 15,new LevelInfo( 15, 150, 1200,3, 750));
        levels.put( 16,new LevelInfo( 16, 160, 1360,3, 800));
        levels.put( 17,new LevelInfo( 17, 170, 1530,3, 850));
        levels.put( 18,new LevelInfo( 18, 180, 1710,3, 900));
        levels.put( 19,new LevelInfo( 19, 190, 1900,3, 950));
        levels.put( 20,new LevelInfo( 20, 200, 2100,3,1000));
        levels.put( 21,new LevelInfo( 21, 210, 2310,3,1050));
        levels.put( 22,new LevelInfo( 22, 220, 2530,3,1100));
        levels.put( 23,new LevelInfo( 23, 230, 2760,3,1150));
        levels.put( 24,new LevelInfo( 24, 240, 3000,3,1200));
        levels.put( 25,new LevelInfo( 25, 250, 3250,3,1250));
        levels.put( 26,new LevelInfo( 26, 260, 3510,3,1300));
        levels.put( 27,new LevelInfo( 27, 270, 3780,3,1350));
        levels.put( 28,new LevelInfo( 28, 280, 4060,3,1400));
        levels.put( 29,new LevelInfo( 29, 290, 4350,3,1450));
        levels.put( 30,new LevelInfo( 30, 300, 4650,3,1500));
        levels.put( 31,new LevelInfo( 31, 310, 4960,3,1550));
        levels.put( 32,new LevelInfo( 32, 320, 5280,3,1600));
        levels.put( 33,new LevelInfo( 33, 330, 5610,3,1650));
        levels.put( 34,new LevelInfo( 34, 340, 5950,3,1700));
        levels.put( 35,new LevelInfo( 35, 350, 6300,3,1750));
        levels.put( 36,new LevelInfo( 36, 360, 6660,3,1800));
        levels.put( 37,new LevelInfo( 37, 370, 7030,3,1850));
        levels.put( 38,new LevelInfo( 38, 380, 7410,3,1900));
        levels.put( 39,new LevelInfo( 39, 390, 7800,3,1950));
        levels.put( 40,new LevelInfo( 40, 400, 8200,3,2000));
        levels.put( 41,new LevelInfo( 41, 410, 8610,3,2050));
        levels.put( 42,new LevelInfo( 42, 420, 9030,3,2100));
        levels.put( 43,new LevelInfo( 43, 430, 9460,3,2150));
        levels.put( 44,new LevelInfo( 44, 440, 9900,3,2200));
        levels.put( 45,new LevelInfo( 45, 450,10350,3,2250));
        levels.put( 46,new LevelInfo( 46, 460,10810,3,2300));
        levels.put( 47,new LevelInfo( 47, 470,11280,3,2350));
        levels.put( 48,new LevelInfo( 48, 480,11760,3,2400));
        levels.put( 49,new LevelInfo( 49, 490,12250,3,2450));
        levels.put( 50,new LevelInfo( 50, 500,12750,3,2500));
        levels.put( 51,new LevelInfo( 51, 510,13260,3,2550));
        levels.put( 52,new LevelInfo( 52, 520,13780,3,2600));
        levels.put( 53,new LevelInfo( 53, 530,14310,3,2650));
        levels.put( 54,new LevelInfo( 54, 540,14850,3,2700));
        levels.put( 55,new LevelInfo( 55, 550,15400,3,2750));
        levels.put( 56,new LevelInfo( 56, 560,15960,3,2800));
        levels.put( 57,new LevelInfo( 57, 570,16530,3,2850));
        levels.put( 58,new LevelInfo( 58, 580,17110,3,2900));
        levels.put( 59,new LevelInfo( 59, 590,17700,3,2950));
        levels.put( 60,new LevelInfo( 60, 600,18300,3,3000));
        levels.put( 61,new LevelInfo( 61, 610,18910,3,3050));
        levels.put( 62,new LevelInfo( 62, 620,19530,3,3100));
        levels.put( 63,new LevelInfo( 63, 630,20160,3,3150));
        levels.put( 64,new LevelInfo( 64, 640,20800,3,3200));
        levels.put( 65,new LevelInfo( 65, 650,21450,3,3250));
        levels.put( 66,new LevelInfo( 66, 660,22110,3,3300));
        levels.put( 67,new LevelInfo( 67, 670,22780,3,3350));
        levels.put( 68,new LevelInfo( 68, 680,23460,3,3400));
        levels.put( 69,new LevelInfo( 69, 690,24150,3,3450));
        levels.put( 70,new LevelInfo( 70, 700,24850,3,3500));
        levels.put( 71,new LevelInfo( 71, 710,25560,3,3550));
        levels.put( 72,new LevelInfo( 72, 720,26280,3,3600));
        levels.put( 73,new LevelInfo( 73, 730,27010,3,3650));
        levels.put( 74,new LevelInfo( 74, 740,27750,3,3700));
        levels.put( 75,new LevelInfo( 75, 750,28500,3,3750));
        levels.put( 76,new LevelInfo( 76, 760,29260,3,3800));
        levels.put( 77,new LevelInfo( 77, 770,30030,3,3850));
        levels.put( 78,new LevelInfo( 78, 780,30810,3,3900));
        levels.put( 79,new LevelInfo( 79, 790,31600,3,3950));
        levels.put( 80,new LevelInfo( 80, 800,32400,3,4000));
        levels.put( 81,new LevelInfo( 81, 810,33210,3,4050));
        levels.put( 82,new LevelInfo( 82, 820,34030,3,4100));
        levels.put( 83,new LevelInfo( 83, 830,34860,3,4150));
        levels.put( 84,new LevelInfo( 84, 840,35700,3,4200));
        levels.put( 85,new LevelInfo( 85, 850,36550,3,4250));
        levels.put( 86,new LevelInfo( 86, 860,37410,3,4300));
        levels.put( 87,new LevelInfo( 87, 870,38280,3,4350));
        levels.put( 88,new LevelInfo( 88, 880,39160,3,4400));
        levels.put( 89,new LevelInfo( 89, 890,40050,3,4450));
        levels.put( 90,new LevelInfo( 90, 900,40950,3,4500));
        levels.put( 91,new LevelInfo( 91, 910,41860,3,4550));
        levels.put( 92,new LevelInfo( 92, 920,42780,3,4600));
        levels.put( 93,new LevelInfo( 93, 930,43710,3,4650));
        levels.put( 94,new LevelInfo( 94, 940,44650,3,4700));
        levels.put( 95,new LevelInfo( 95, 950,45600,3,4750));
        levels.put( 96,new LevelInfo( 96, 960,46560,3,4800));
        levels.put( 97,new LevelInfo( 97, 970,47530,3,4850));
        levels.put( 98,new LevelInfo( 98, 980,48510,3,4900));
        levels.put( 99,new LevelInfo( 99, 990,49500,3,4950));
        levels.put(100,new LevelInfo(100,1000,50500,3,5000));
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