package com.ivoid.bj;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bj.R;

public class ConfirmDialogFragment extends DialogFragment {

    public static ConfirmDialogFragment newInstance(String dialogType, String message) {
        ConfirmDialogFragment frag = new ConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString("dialogType", dialogType);
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    public static ConfirmDialogFragment newInstance(String dialogType, String message, String id) {
        ConfirmDialogFragment frag = new ConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString("dialogType", dialogType);
        args.putString("message", message);
        args.putString("id", id);
        frag.setArguments(args);
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
        dialog.setContentView(R.layout.confirm);

        String message = getArguments().getString("message");
        ((TextView)dialog.findViewById(R.id.message)).setText(message);

        // OK ボタンのリスナ
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                switch(getArguments().getString("dialogType")) {
                    case "confirmAdDialog":
                        ((Playing) getActivity()).showMovieAd();
                        break;
                    case "confirmHintByCoinDialog":
                        ((Playing)getActivity()).getHint();
                        break;
                    case "confirmApplyDialog":
                        String competitionId = getArguments().getString("id");
                        ((Competition)getActivity()).applyCompetition(competitionId);
                        break;
                    case "confirmFreeChipsByCoinDialog":
                        ((Playing)getActivity()).beginFreeChipsByCoin();
                        break;
                }
            }
        });

        // CANCEL ボタンのリスナ
        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                switch(getArguments().getString("dialogType")) {
                    case "confirmAdDialog":
                    case "confirmFreeChipsByCoinDialog":
                        ((Playing)getActivity()).execInitUIWhenBetting(0, false);
                        break;
                }
            }
        });

        return dialog;
    }
}