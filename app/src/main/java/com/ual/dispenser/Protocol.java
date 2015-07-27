package com.ual.dispenser;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Pedro on 11/07/2015.
 */


/**
 * Class que processa o protocolo recebido
 */

public class Protocol {

    String messageIn, messageOut;
    Context context;
    private Socket socket = null;
    private PrintWriter out;


    //Construtores

    public Protocol(Context context) {
        this.context = context;
    }


    public Protocol(Socket socket) {
        this.socket = socket;

        try {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("#Protocol#", " - Exception - " + e.getMessage());

        }
    }


    // Métodos

    /**
     * Metodo que envia a mensagem do protocolo inicial "Hello"
     *
     * @return true or false
     */


    public boolean startingProtocol() {

        out.println("Hello from Asura CPRN!");
        return true;

    }


    /**
     * Método que envia mensagens ao servidor
     *
     * @param message recebe a mensagem para enviar ao servidor
     * @return true or false
     * @throws Exception
     */

    public boolean sendMessage(String message) throws Exception {

        this.messageOut = message;

        try {
            out.println(messageOut);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (out.checkError()) {
            Log.e("#Protocol#", " - outError - " + out.checkError());
            throw new IOException("Error transmitting data.");
        }

        return true;

    }

    /**
     * Método que trata as mensagens recebidas do servidor e age em conformidade
     *
     * @param message - Recebe a mensagem do servidor
     */


    public void receiveMessage(String message) {


        // Remove CRLF da string com o texto
        this.messageIn = message;
        message = message.replaceAll("[\n\r]", "");

        // Separar a messagem do protocolo
        // Ex: Buttons:Tesouraria, Secretaria - >  [0] Buttons  e [1] Tesouraria, Secretaria;
        String[] splitedProtocol;
        splitedProtocol = message.split(";");

        Log.v("#Protocol#", " - Protocol - " + splitedProtocol[0]);


        //Testa o protocolo
        String[] splitedMessage;
        switch (splitedProtocol[0]) {
            case "BUTTONS":

                // Separa a string com os vários botões separados por virgula, em string individuais
                splitedMessage = splitedProtocol[1].split(",");

                for (String s : splitedMessage) {
                    Log.v("#Protocol#", " - Message -" + s);
                }
                Log.v("#Protocol#", "- Nº de elementos - " + Integer.toString(splitedMessage.length));

                //Chama o controle dos botões, para mostrar os departamentos.
                new buttonsControl(context).showButtons(splitedMessage.length, splitedMessage);

                break;

            case "TICKET":
                splitedMessage = splitedProtocol[1].split(",");

                Log.w("#Protocol#", "Message - " + splitedProtocol[1]);

                ((MainActivity) context).createQRcode(splitedMessage[1]);

                ((MainActivity) context).printTicket(splitedMessage[0], splitedMessage[1], splitedMessage[2]);

                break;

            case "INACTIVE":

                MainActivity.inactive = true;
                Log.v("#Protocol#", " - Recebido INACTIVE");
                Toast.makeText(context, R.string.inactive, Toast.LENGTH_LONG).show();

                break;

            case "KEEPALIVE":
                MainActivity.keepAlive = true;
                Log.v("#Protocol#", " - Recebido KEEPALIVE");

                break;

            default:
                Log.v("#Protocol#", " - Não consta do protocolo (comando desconhecido)");

                break;

        }
    }


}

