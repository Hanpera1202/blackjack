package com.ivoid.bj;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

public class RegistMailDialogFragment extends DialogFragment {

    public static RegistMailDialogFragment newInstance(String message, String mail_address) {
        RegistMailDialogFragment frag = new RegistMailDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        args.putString("mail_address", mail_address);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = getArguments().getString("message");
        String mail_address = getArguments().getString("mail_address");

        final EditText editView = new EditText(getActivity());
        editView.setText(mail_address);

        AlertDialog.Builder AlertDialogBuilder = new AlertDialog.Builder(getActivity());
        AlertDialogBuilder.setView(editView)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String inputMailAddress = editView.getText().toString();
                        ((ResultDialog)getActivity()).registMailAddress(inputMailAddress);
                    }
                })
                .setNegativeButton("CANCEL", null);
        return AlertDialogBuilder.create();
    }
}