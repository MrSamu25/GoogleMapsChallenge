package com.appmoviles.muriel.googlemapschallenge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class DialogoMarker extends DialogFragment {

    private EditText et_nombre;



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_marker, null);

        et_nombre = (EditText) v.findViewById(R.id.et_nombre);

        builder.setView(v)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        String txt_nombre = et_nombre.getText().toString();

                        if (txt_nombre != null && !txt_nombre.equals("") && !txt_nombre.trim().equals("")) {
                            listener.crearMarcador(txt_nombre);
                        }else{
                            Toast.makeText(getContext(),"Debe ingresar un Nombre válido", Toast.LENGTH_LONG).show();
                        }


                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogoMarker.this.getDialog().cancel();
                    }
                });


        return builder.create();
    }

    public interface ComunicacionDialogo {
        void crearMarcador(String nombre);
    }

    ComunicacionDialogo listener;


    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (ComunicacionDialogo) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }


}
