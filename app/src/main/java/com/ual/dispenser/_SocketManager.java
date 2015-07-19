package com.ual.dispenser;

import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Criacao da thread para a abertura dos sockets para iniciar a comunicacao
 */


public class _SocketManager implements Runnable {


    private Handler updateConversationHandler;
    private String serverIP;
    private int port;
    private String response;
    private Socket socket;

    public _SocketManager(String s, int p) {
        this.serverIP = s;
        this.port = p;
        updateConversationHandler = new Handler();
    }

    public _SocketManager(String s, int p, Socket socket) {
        this.serverIP = s;
        this.port = p;
        this.socket=socket;
        updateConversationHandler = new Handler();
    }



    @Override
    public void run() {

        InputStream inputStream;

        try {
            // while (!socket.isConnected()){
            InetAddress serverAddr = InetAddress.getByName(this.serverIP);



            //Abertura do Socket

            do {
                socket = null;
                socket = new Socket(serverAddr, this.port);
                try {
                    Thread.sleep(1800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while ((socket == null) || socket.isClosed());

            //  Toast.makeText(getBaseContext(), "Ligacao ao servidor conseguida ! ", Toast.LENGTH_LONG).show();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int bytesRead;

            inputStream = socket.getInputStream();

            // Inicio do protocolo

            // Teste de passagem do objecto protocolo para dentro da updateUIthread
           // zzzProcessingProtocol processingProtocol= new zzzProcessingProtocol(socket);
            //processingProtocol.startingProtocol();

            new zzzProcessingProtocol(socket).startingProtocol();

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response = byteArrayOutputStream.toString("UTF-8");

                updateConversationHandler.post(new updateUIThread(response));

                byteArrayOutputStream.reset();
            }

        } catch (UnknownHostException e1) {
            e1.printStackTrace();

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }


    class updateUIThread implements Runnable {
        private String msg;

        public updateUIThread(String str) {
            this.msg = str;

        }
        @Override
        public void run() {
          new zzzProcessingProtocol(socket).receiveProtocol();
            Log.v("Pedro - UpdateUIThread", msg.toString());

       }
    }

}
