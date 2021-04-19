package com.example.ass15.User_Interface;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.ass15.R;

public class Password_Dialog extends AppCompatDialogFragment {
    private EditText editTextPassword;


    private PasswordDialogListener listener;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);
        builder.setView(view).setTitle("Password Check").setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = editTextPassword.getText().toString();
                listener.applyText(password);

            }
        });
        editTextPassword = view.findViewById(R.id.Check_Password);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (PasswordDialogListener) context;
        } catch (ClassCastException e) {
            throw  new ClassCastException(context.toString() + "Must implement");
        }
    }


    public interface PasswordDialogListener{
        void applyText(String password);
    }
}
