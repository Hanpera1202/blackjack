package com.ivoid.bj;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bj.R;

public class LevelUpDialogFragment extends DialogFragment {

    private GameSettings settings;
    private Player player;
    private int getBonusCoin;
    private int getBonusChip;

    public static LevelUpDialogFragment newInstance() {
        LevelUpDialogFragment frag = new LevelUpDialogFragment();
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getActivity());

        // タイトル非表示
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // フルスクリーン
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        // キャンセルを無効にする
        this.setCancelable(false);
        dialog.setContentView(R.layout.level_up);

        player = new Player(getActivity(), "God");
        settings = new GameSettings();

        ((TextView) dialog.findViewById(R.id.prevMaxBet)).setText(String.valueOf(player.getMaxBet()));

        if(player.isLevelUp()) {
            player.levelUp();
            if(player.getLevel() > settings.levels.size()){
                getBonusChip = settings.levels.get(settings.levels.size()).getChipCnt;
                getBonusCoin = settings.levels.get(settings.levels.size()).getCoinCnt;
                dialog.findViewById(R.id.maxBet).setVisibility(LinearLayout.GONE);
            }else {
                getBonusChip = settings.levels.get(player.getLevel()).getChipCnt;
                getBonusCoin = settings.levels.get(player.getLevel()).getCoinCnt;
                ((TextView) dialog.findViewById(R.id.newMaxBet)).setText(String.valueOf(player.getMaxBet()));
            }
            player.deposit(getBonusChip);
            player.depositCoin(getBonusCoin);
        }

        ((TextView) dialog.findViewById(R.id.newLevel)).setText("You are now level " + player.getLevel() + ".");
        ((TextView) dialog.findViewById(R.id.getBonusChip)).setText("+" + String.valueOf(getBonusChip));
        ((TextView) dialog.findViewById(R.id.getBonusCoin)).setText("+" + String.valueOf(getBonusCoin));

        // OK ボタンのリスナ
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ((Playing)getActivity()).setPlayerDataWhenReturnDialog();
            }
        });

        return dialog;
    }
}