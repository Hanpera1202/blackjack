package com.ivoid.bj;

/**
 * Created by nakazato on 2015/12/12.
 */
public class LevelInfo {
    public int level;
    public int nextExp;
    public int totalExp;
    public int getCoinCnt;
    public int maxBet;

    LevelInfo(int lv, int nExp, int tExpt, int gCoinCnt, int mBet){
        level = lv;
        nextExp = nExp;
        totalExp = tExpt;
        getCoinCnt = gCoinCnt;
        maxBet = mBet;
    }
}
