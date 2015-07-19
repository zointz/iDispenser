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
public class zzzProcessingProtocol {
    Socket socket;
    String messageIn;
    PrintWriter out;

   /* public zzzProcessingProtocol(Socket socket, String messageIn) {
        this.socket = socket;
        this.messageIn = messageIn;
        try {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
*/

    public zzzProcessingProtocol(Socket socket) {
        this.socket = socket;
    }

    public boolean startingProtocol() {
        try {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out.println("Hello from Asura CPRN!");

            return true;

        } catch (
                IOException e
                )
        {
            e.printStackTrace();
            return false;
        }
    }

    public void receiveProtocol() {


    }


}



