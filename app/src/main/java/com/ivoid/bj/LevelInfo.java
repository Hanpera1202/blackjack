package com.ivoid.bj;

/**
 * Created on 2015/12/12.
 */
public class LevelInfo {
    public int level;
    public int nextExp;
    public int totalExp;
    public int getChipCnt;
    public int getCoinCnt;
    public int maxBet;

    LevelInfo(int lv, int nExp, int tExpt, int gChipCnt, int gCoinCnt, int mBet){
        level = lv;
        nextExp = nExp;
        totalExp = tExpt;
        getChipCnt = gChipCnt;
        getCoinCnt = gCoinCnt;
        maxBet = mBet;
    }
}
