package com.ivoid.bj;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConfirmDialogFragment extends DialogFragment {

    public static ConfirmDialogFragment newInstance() {
        ConfirmDialogFragment frag = new ConfirmDialogFragment();
        return frag;
    }

    public static ConfirmDialogFragment newInstance(String message) {
        ConfirmDialogFragment frag = new ConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    public static ConfirmDialogFragment newInstance(String message, String id) {
        ConfirmDialogFragment frag = new ConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        args.putString("id", id);
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
                            case "confirmHintByCoinDialog":
                                ((Dealer)getActivity()).getHint();
                                break;
                            case "confirmApplyDialog":
                                String competitionId = getArguments().getString("id");
                                ((Competition)getActivity()).applyCompetition(competitionId);
                                break;
                            case "confirmFreeChipsByCoinDialog":
                                ((Dealer)getActivity()).beginFreeChipsByCoin();
                                break;
                        }
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        switch(getTag()) {
                            case "confirmShareDialog":
                                ((Competition)getActivity()).showAd();
                                break;
                        }
                    }
                });
        return AlertDialogBuilder.create();
    }
}