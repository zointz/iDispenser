package com.ual.dispenser;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.StarMicronics.jasura.JAException;

/**
 * Created by Pedro on 19/07/2015.
 */
public class buttonsControl {
    Context context;

    //contrutor
    public buttonsControl(Context context) {
            this.context = context;
        }


    /**
     * Método que mostra o menu indicando que aguarda connecção
     *
     * @param numberButtons
     */

    public void showTryingConnection(final int numberButtons) {

        ((MainActivity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.connection.setVisibility(View.VISIBLE);
                for (int i = 0; i < numberButtons; i++) {
                    MainActivity.ticketButton[i].setVisibility(View.GONE);
                }
                try {
                    if (!MainActivity.socketStayClosed) {
                        ((MainActivity) context).printer.ledGradualShift(500
                                , (byte) 0, (byte) 0, (byte) 0, (byte) 100, (byte) 20, (byte) 0);
                    }
                } catch (JAException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showButtons(final int numberButtons, final String[] splitedMessage) {

        ((MainActivity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.connection.setVisibility(View.GONE);
                for (int i = 0; i < numberButtons; i++) {
                    MainActivity.ticketButton[i].setVisibility(View.VISIBLE);
                    MainActivity.ticketButton[i].setText(splitedMessage[i]);
                }
                try {

                    ((MainActivity)context).printer.ledGradualShift(2000, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 73, (byte) 100);
                } catch (JAException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
