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
    int ticketExchangePoint;
    int freeChipsSeconds;
    int freeChipsFlips;
    int playCountUpAds;
    
    private boolean[] bools={true,false};
    private byte[] bytes={6,8};

    int pointDivisor;
    Map<Integer, LevelInfo> levels;

    GameSettings()
    {
        bjPay=1.5f; surrenderPay=-0.5f; insurancePay=2f; winPay=1f;
        decks=1; splits=1; burns=(byte)(5.0*Math.random()); shuffleTimes = 3;
        startCash=1500; tableMin=10;
        ddPostSplit=true; surrender=true; insurance=true; stand17soft=true; aceResplit=true; dd1011=true;
        loginBonusSeconds=28800;freeChipsSeconds=3600;loginBonusCoins=3;coinBonusCount=3;
        freeChipsFlips = 3;
        playCountUpAds = 30;
        ticketExchangePoint = 15000;
        pointDivisor = 10;

        levels = new HashMap<Integer, LevelInfo>();
        levels.put(  1,new LevelInfo(  1,13  ,13   ,1000,3, 500));
        levels.put(  2,new LevelInfo(  2,26  ,39   ,1200,3, 600));
        levels.put(  3,new LevelInfo(  3,39  ,78   ,1300,3, 650));
        levels.put(  4,new LevelInfo(  4,52  ,130  ,1400,3, 700));
        levels.put(  5,new LevelInfo(  5,65  ,195  ,1500,3, 750));
        levels.put(  6,new LevelInfo(  6,78  ,273  ,1600,3, 800));
        levels.put(  7,new LevelInfo(  7,91  ,364  ,1700,3, 850));
        levels.put(  8,new LevelInfo(  8,104 ,468  ,1800,3, 900));
        levels.put(  9,new LevelInfo(  9,117 ,585  ,1900,3, 950));
        levels.put( 10,new LevelInfo( 10,130 ,715  ,2000,3,1000));
        levels.put( 11,new LevelInfo( 11,154 ,869  ,2100,3,1050));
        levels.put( 12,new LevelInfo( 12,168 ,1037 ,2200,3,1100));
        levels.put( 13,new LevelInfo( 13,182 ,1219 ,2300,3,1150));
        levels.put( 14,new LevelInfo( 14,196 ,1415 ,2400,3,1200));
        levels.put( 15,new LevelInfo( 15,210 ,1625 ,2500,3,1250));
        levels.put( 16,new LevelInfo( 16,224 ,1849 ,2600,3,1300));
        levels.put( 17,new LevelInfo( 17,238 ,2087 ,2700,3,1350));
        levels.put( 18,new LevelInfo( 18,252 ,2339 ,2800,3,1400));
        levels.put( 19,new LevelInfo( 19,266 ,2605 ,2900,3,1450));
        levels.put( 20,new LevelInfo( 20,280 ,2885 ,3000,3,1500));
        levels.put( 21,new LevelInfo( 21,315 ,3200 ,3100,3,1550));
        levels.put( 22,new LevelInfo( 22,330 ,3530 ,3200,3,1600));
        levels.put( 23,new LevelInfo( 23,345 ,3875 ,3300,3,1650));
        levels.put( 24,new LevelInfo( 24,360 ,4235 ,3400,3,1700));
        levels.put( 25,new LevelInfo( 25,375 ,4610 ,3500,3,1750));
        levels.put( 26,new LevelInfo( 26,390 ,5000 ,3600,3,1800));
        levels.put( 27,new LevelInfo( 27,405 ,5405 ,3700,3,1850));
        levels.put( 28,new LevelInfo( 28,420 ,5825 ,3800,3,1900));
        levels.put( 29,new LevelInfo( 29,435 ,6260 ,3900,3,1950));
        levels.put( 30,new LevelInfo( 30,450 ,6710 ,4000,3,2000));
        levels.put( 31,new LevelInfo( 31,496 ,7206 ,4100,3,2050));
        levels.put( 32,new LevelInfo( 32,512 ,7718 ,4200,3,2100));
        levels.put( 33,new LevelInfo( 33,528 ,8246 ,4300,3,2150));
        levels.put( 34,new LevelInfo( 34,544 ,8790 ,4400,3,2200));
        levels.put( 35,new LevelInfo( 35,560 ,9350 ,4500,3,2250));
        levels.put( 36,new LevelInfo( 36,576 ,9926 ,4600,3,2300));
        levels.put( 37,new LevelInfo( 37,592 ,10518,4700,3,2350));
        levels.put( 38,new LevelInfo( 38,608 ,11126,4800,3,2400));
        levels.put( 39,new LevelInfo( 39,624 ,11750,4900,3,2450));
        levels.put( 40,new LevelInfo( 40,640 ,12390,5000,3,2500));
        levels.put( 41,new LevelInfo( 41,697 ,13087,5100,3,2550));
        levels.put( 42,new LevelInfo( 42,714 ,13801,5200,3,2600));
        levels.put( 43,new LevelInfo( 43,731 ,14532,5300,3,2650));
        levels.put( 44,new LevelInfo( 44,748 ,15280,5400,3,2700));
        levels.put( 45,new LevelInfo( 45,765 ,16045,5500,3,2750));
        levels.put( 46,new LevelInfo( 46,782 ,16827,5600,3,2800));
        levels.put( 47,new LevelInfo( 47,799 ,17626,5700,3,2850));
        levels.put( 48,new LevelInfo( 48,816 ,18442,5800,3,2900));
        levels.put( 49,new LevelInfo( 49,833 ,19275,5900,3,2950));
        levels.put( 50,new LevelInfo( 50,850 ,20125,6000,3,3000));
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