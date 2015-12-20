package com.ivoid.bj;

import java.util.HashMap;
import java.util.Map;

class GameSettings
{
	byte decks, splits, burns, shuffleTimes; 
	float startCash, tableMin, tableMax;
	float bjPay, surrenderPay, insurancePay, winPay;
	boolean ddPostSplit, surrender, insurance, stand17soft, aceResplit, dd1011;
    int loginBonusSeconds;
	int loginBonusCoins;
	int coninBonusCount;
    int necessaryPoint;
    int freeChipsSeconds;
	
	private boolean[] bools={true,false};
	private byte[] bytes={6,8};

    Map<Integer, LevelInfo> levels;

	GameSettings()
	{
		bjPay=1.5f; surrenderPay=-0.5f; insurancePay=2f; winPay=1f;
		decks=1; splits=1; burns=(byte)(5.0*Math.random()); shuffleTimes = 3;
		startCash=1000f; tableMin=10f;
		ddPostSplit=true; surrender=true; insurance=true; stand17soft=true; aceResplit=true; dd1011=true;
        loginBonusSeconds=600;freeChipsSeconds=60;loginBonusCoins=3;coninBonusCount=3;
        necessaryPoint=3000;

        levels = new HashMap<Integer, LevelInfo>();
        levels.put(1, new LevelInfo(1,100,100,3,100));
        levels.put(2, new LevelInfo(2,110,210,3,200));
        levels.put(3, new LevelInfo(3,121,331,3,300));
        levels.put(4, new LevelInfo(4,133,464,3,400));
        levels.put(5, new LevelInfo(5,146,611,3,500));
        levels.put(6, new LevelInfo(6,161,772,3,600));
        levels.put(7, new LevelInfo(7,177,949,3,700));
        levels.put(8, new LevelInfo(8,195,1141,3,800));
        levels.put(9, new LevelInfo(9,214,1358,3,900));
        levels.put(10,new LevelInfo(10,236,1594,3,1000));
        levels.put(11,new LevelInfo(11,259,1853,3,1100));
        levels.put(12,new LevelInfo(12,285,2138,3,1200));
        levels.put(13,new LevelInfo(13,314,2452,3,1300));
        levels.put(14,new LevelInfo(14,345,2797,3,1400));
        levels.put(15,new LevelInfo(15,380,3177,3,1500));
        levels.put(16,new LevelInfo(16,418,3595,3,1600));
        levels.put(17,new LevelInfo(17,459,4054,3,1700));
        levels.put(18,new LevelInfo(18,505,4560,3,1800));
        levels.put(19,new LevelInfo(19,556,5116,3,1900));
        levels.put(20,new LevelInfo(20,612,5727,3,2000));
        levels.put(21,new LevelInfo(21,673,6400,3,2100));
        levels.put(22,new LevelInfo(22,740,7140,3,2200));
        levels.put(23,new LevelInfo(23,814,7954,3,2300));
        levels.put(24,new LevelInfo(24,895,8850,3,2400));
        levels.put(25,new LevelInfo(25,985,9835,3,2500));
        levels.put(26,new LevelInfo(26,1083,10918,3,2600));
        levels.put(27,new LevelInfo(27,1192,12110,3,2700));
        levels.put(28,new LevelInfo(28,1311,13421,3,2800));
        levels.put(29,new LevelInfo(29,1442,14863,3,2900));
        levels.put(30,new LevelInfo(30,1586,16449,3,3000));
        levels.put(31,new LevelInfo(31,1745,18194,3,3100));
        levels.put(32,new LevelInfo(32,1919,20114,3,3200));
        levels.put(33,new LevelInfo(33,2111,22225,3,3300));
        levels.put(34,new LevelInfo(34,2323,24548,3,3400));
        levels.put(35,new LevelInfo(35,2555,27102,3,3500));
        levels.put(36,new LevelInfo(36,2810,29913,3,3600));
        levels.put(37,new LevelInfo(37,3091,33004,3,3700));
        levels.put(38,new LevelInfo(38,3400,36404,3,3800));
        levels.put(39,new LevelInfo(39,3740,40145,3,3900));
        levels.put(40,new LevelInfo(40,4114,44259,3,4000));
        levels.put(41,new LevelInfo(41,4526,48785,3,4100));
        levels.put(42,new LevelInfo(42,4979,53764,3,4200));
        levels.put(43,new LevelInfo(43,5476,59240,3,4300));
        levels.put(44,new LevelInfo(44,6024,65264,3,4400));
        levels.put(45,new LevelInfo(45,6626,71890,3,4500));
        levels.put(46,new LevelInfo(46,7289,79180,3,4600));
        levels.put(47,new LevelInfo(47,8018,87197,3,4700));
        levels.put(48,new LevelInfo(48,8820,96017,3,4800));
        levels.put(49,new LevelInfo(49,9702,105719,3,4900));
        levels.put(50,new LevelInfo(50,10672,116391,3,5000));
        levels.put(51,new LevelInfo(51,11739,128130,3,5100));
        levels.put(52,new LevelInfo(52,12913,141043,3,5200));
        levels.put(53,new LevelInfo(53,14204,155247,3,5300));
        levels.put(54,new LevelInfo(54,15625,170872,3,5400));
        levels.put(55,new LevelInfo(55,17187,188059,3,5500));
        levels.put(56,new LevelInfo(56,18906,206965,3,5600));
        levels.put(57,new LevelInfo(57,20797,227762,3,5700));
        levels.put(58,new LevelInfo(58,22876,250638,3,5800));
        levels.put(59,new LevelInfo(59,25164,275801,3,5900));
        levels.put(60,new LevelInfo(60,27680,303482,3,6000));
        levels.put(61,new LevelInfo(61,30448,333930,3,6100));
        levels.put(62,new LevelInfo(62,33493,367423,3,6200));
        levels.put(63,new LevelInfo(63,36842,404265,3,6300));
        levels.put(64,new LevelInfo(64,40527,444792,3,6400));
        levels.put(65,new LevelInfo(65,44579,489371,3,6500));
        levels.put(66,new LevelInfo(66,49037,538408,3,6600));
        levels.put(67,new LevelInfo(67,53941,592349,3,6700));
        levels.put(68,new LevelInfo(68,59335,651683,3,6800));
        levels.put(69,new LevelInfo(69,65268,716952,3,6900));
        levels.put(70,new LevelInfo(70,71795,788747,3,7000));
        levels.put(71,new LevelInfo(71,78975,867722,3,7100));
        levels.put(72,new LevelInfo(72,86872,954594,3,7200));
        levels.put(73,new LevelInfo(73,95559,1050153,3,7300));
        levels.put(74,new LevelInfo(74,105115,1155269,3,7400));
        levels.put(75,new LevelInfo(75,115627,1270895,3,7500));
        levels.put(76,new LevelInfo(76,127190,1398085,3,7600));
        levels.put(77,new LevelInfo(77,139908,1537993,3,7700));
        levels.put(78,new LevelInfo(78,153899,1691893,3,7800));
        levels.put(79,new LevelInfo(79,169289,1861182,3,7900));
        levels.put(80,new LevelInfo(70,186218,2047400,3,8000));
        levels.put(81,new LevelInfo(81,204840,2252240,3,8100));
        levels.put(82,new LevelInfo(82,225324,2477564,3,8200));
        levels.put(83,new LevelInfo(83,247856,2725421,3,8300));
        levels.put(84,new LevelInfo(84,272642,2998063,3,8400));
        levels.put(85,new LevelInfo(85,299906,3297969,3,8500));
        levels.put(86,new LevelInfo(86,329897,3627866,3,8600));
        levels.put(87,new LevelInfo(87,362887,3990753,3,8700));
        levels.put(88,new LevelInfo(88,399175,4389928,3,8800));
        levels.put(89,new LevelInfo(89,439093,4829021,3,8900));
        levels.put(90,new LevelInfo(90,483002,5312023,3,9000));
        levels.put(91,new LevelInfo(91,531302,5843325,3,9100));
        levels.put(92,new LevelInfo(92,584432,6427757,3,9200));
        levels.put(93,new LevelInfo(93,642876,7070633,3,9300));
        levels.put(94,new LevelInfo(94,707163,7777796,3,9400));
        levels.put(95,new LevelInfo(95,777880,8555676,3,9500));
        levels.put(96,new LevelInfo(96,855668,9411344,3,9600));
        levels.put(97,new LevelInfo(97,941234,10352578,3,9700));
        levels.put(98,new LevelInfo(98,1035358,11387936,3,9800));
        levels.put(99,new LevelInfo(99,1138894,12526829,3,9900));
        levels.put(100,new LevelInfo(100,0,0,0,10000));
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