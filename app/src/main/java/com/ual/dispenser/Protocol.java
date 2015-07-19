package com.ual.dispenser;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Pedro on 11/07/2015.
 */


public class Protocol {

    String messageIn, messageOut;
    private Button button1, button2, button3, button4, button5;
    public static final int MAXBUTTONS = 5;
    private Button tickeTbutton[] = new Button[MAXBUTTONS];
    private Socket socket = null;
    private PrintWriter out;
    private MainActivity act;
    Context context;



    //Construtores

    public Protocol() {
        // setContentView(R.layout.activity_main);
    }

    public Protocol(Socket socket) {
        this.socket = socket;


        try {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Protocol(Socket socket, MainActivity activity) throws Exception {
        this.socket = socket;
        act = activity;

        try {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Protocol - Exception", e.toString());
        }

    }


    public Protocol(Context context) {
        this.context = context;
    }


    // Métodos


    public boolean startingProtocol() {

        out.println("Hello from Asura CPRN!");
        return true;

    }

    public boolean sendMessage(String message) throws Exception {

        this.messageOut = message;

        try {
            out.println(messageOut);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (out.checkError()){
            Log.e("outError"," "+ out.checkError())  ;
            throw new IOException("Error transmitting data.");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("Keep Alive)","-"+MainActivity.keepAlive);
        return true;

    }


    public void receiveMessage(String message) {


        // Context applicationContext = act.getContextOfApplication();


        // Remove CRLF da string com o texto
        this.messageIn = message;
        message = message.replaceAll("[\n\r]", "");

        // Separar a messagem do protocolo
        // Ex: Buttons:Tesouraria, Secretaria - >  [0] Buttons  e [1] Tesouraria, Secretaria;
        String[] splitedProtocol;
        splitedProtocol = message.split(";");

        Log.v("Pedro - Protocol - ", splitedProtocol[0]);


        //Testa o protocolo
        String[] splitedMessage;
        switch (splitedProtocol[0]) {
            case "BUTTONS":

                // Separa a string com os vários botões separados por virgula, em string individuais

                splitedMessage = splitedProtocol[1].split(",");

                for (String s : splitedMessage) {
                    Log.v("Pedro - Message ", s);
                }
                Log.v("Pedro - Nº de elementos", Integer.toString(splitedMessage.length));

                MainActivity.connection.setVisibility(View.GONE);
                for (int i = 0; i < splitedMessage.length; i++) {
                    if (splitedMessage[i] != null) {
                        MainActivity.ticketButton[i].setText(splitedMessage[i]);
                        MainActivity.ticketButton[i].setVisibility(View.VISIBLE);
                    }
                }


                break;
            case "TICKET":
                splitedMessage = splitedProtocol[1].split(",");
                Log.v("Pedro - Message ", splitedProtocol[1]);
                ((MainActivity) context).createQRcode(splitedMessage[1]);

             //   MainActivity.ticket = splitedMessage[0] + "\n" + splitedMessage[1];

                ((MainActivity) context).printTicket(splitedMessage[0], splitedMessage[1]);


                break;
            default:
                MainActivity.ticket = " Erro na recepcao de dados !";
                break;
        }
    }


}

