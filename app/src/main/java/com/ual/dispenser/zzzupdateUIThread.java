package com.ual.dispenser;


import java.net.Socket;

/**
 * Created by Pedro on 01/07/2015.
 */
public class zzzupdateUIThread implements Runnable {
    private String msg;
    private zzzProcessingProtocol zzzProcessingProtocol;
    private Socket socket;

    public zzzupdateUIThread(String response, zzzProcessingProtocol zzzProcessingProtocol, Socket socket) {
//        this.context = context.getApplicationContext();
        this.zzzProcessingProtocol = zzzProcessingProtocol;
        this.socket = socket;
        this.msg = response;

    }

    @Override
    public void run() {
   //     zzzProcessingProtocol()
     //mostra(msg);


/*
        try {
            new zzzTransportMessage(receive(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
    }
}
