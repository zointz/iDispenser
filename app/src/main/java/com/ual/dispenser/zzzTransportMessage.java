package com.ual.dispenser;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Pedro on 06/07/2015.
 */
public class zzzTransportMessage {
    Socket socket;
    PrintWriter out;
    Context context;


    //Construtores

    public zzzTransportMessage(Socket socket) throws IOException {
        this.socket = socket;
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    }

    public zzzTransportMessage(Context context) throws IOException {
        this.context=context;

    }

    public void send(String msg) {
        out.println(msg);
    }


    public void receive(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    //  Toast.makeText(getBaseContext(), "Ligacao ao servidor conseguida ! ", Toast.LENGTH_LONG).show();

    public void toast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

    }

}
