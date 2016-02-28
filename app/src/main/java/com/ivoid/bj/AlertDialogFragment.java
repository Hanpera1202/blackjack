package com.ivoid.bj;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bj.R;

public class AlertDialogFragment extends DialogFragment {

    public static AlertDialogFragment newInstance(String message) {
        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("dialogType", "default");
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    public static AlertDialogFragment newInstance(String dialogType, String message) {
        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("dialogType", dialogType);
        args.putString("message", message);
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
        dialog.setContentView(R.layout.alert);

        String message = getArguments().getString("message");
        ((TextView)dialog.findViewById(R.id.message)).setText(message);

        // OK ボタンのリスナ
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                switch(getArguments().getString("dialogType")) {
                    case "applyCompletedDialog":
                        ((Competition) getActivity()).showAd();
                        break;
                    case "compRegistMailDialog":
                        getActivity().finish();
                        break;
                    case "nowLoadingAdDialog":
                    case "noHintCoinDialog":
                        ((Playing) getActivity()).execInitUIWhenBetting(0, false);
                        break;
                    case "registErrorDialog":
                        ((Title) getActivity()).onResume();
                        break;
                }
            }
        });

        return dialog;
    }
}