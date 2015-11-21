package com.ivoid.bj;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConfirmDialogFragment extends DialogFragment {

    public static ConfirmDialogFragment newInstance(String message) {
        ConfirmDialogFragment frag = new ConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String message = getArguments().getString("message");

        AlertDialog.Builder AlertDialogBuilder = new AlertDialog.Builder(getActivity());
        AlertDialogBuilder.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        switch(getTag()) {
                            case "confirmAdDialog":
                                ((Dealer) getActivity()).showMovieAd();
                                break;
                            case "confirmCoinDialog":
                                ((Dealer)getActivity()).useCoin();
                                break;
                        }
                    }
                })
                .setNegativeButton("CANCEL", null);
        return AlertDialogBuilder.create();
    }
}