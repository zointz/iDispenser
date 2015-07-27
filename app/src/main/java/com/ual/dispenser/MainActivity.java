package com.ual.dispenser;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.StarMicronics.jasura.JABarcodeGenerator;
import com.StarMicronics.jasura.JABarcodeGenerator.QR_CELL_SIZE;
import com.StarMicronics.jasura.JABarcodeGenerator.QR_CORRECTION_LEVEL;
import com.StarMicronics.jasura.JABarcodeGenerator.QR_MODEL;
import com.StarMicronics.jasura.JAException;
import com.StarMicronics.jasura.JAPower;
import com.StarMicronics.jasura.JAPrinter;
import com.StarMicronics.jasura.JAPrinter.JAPrintDithering;
import com.StarMicronics.jasura.JAPrinter.JAPrintSpeed;
import com.StarMicronics.jasura.JAPrinterStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    //  Criadas por mim

    //  Static
    public static final int MAXBUTTONS = 5;
    public static Button ticketButton[] = new Button[MAXBUTTONS];
    public static TextView connection;
    public static String ticket = null;

    public static Boolean keepAlive, inactive = false, shutdown = false, socketStayClosed;
    private final int paperWidth = 576;
    private final int[] exitSequence = {0, 2, 2, 6, 8, 5, 12, 14, 8, 18};  //Sequência de saida
    protected JAPrinter printer;
    VideoView videoview;

    //  Private
    private Button txtDateButton;
    private Button code1, code2;
    private Socket socket;
    private Handler updateConversationHandler;
    private Handler handler = new Handler();
    private ImageButton analogClock, logo;
    private int exitCounter = 0;



    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String departamento;

            switch (v.getId()) {

                case R.id.button1:
                    //Arranque do video
                    videoview.start();

                    // Identificar o texto do botão
                    departamento = ticketButton[0].getText().toString();

                    //  Começa a preencher o textBitmap com o conteudo do botão
                    textBitmap = addLineTextImage(null, departamento, 52, Align.ALIGN_CENTER);

                    //  Inicia o envio do request
                    try {
                        if (new Protocol(socket).sendMessage("Request:" + departamento)) {
                            Log.v("#inClickListener#", "Btn1 sends - " + departamento);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.server_Connection_Error, Toast.LENGTH_LONG).show();
                            Log.e("#inClickListener#", " - Btn1 - Error sending message");
                            startSocket();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("#inClickListener#", "- Btn1 - Catch you, trying to reconnect ! ");
                        startSocket();
                    }

                    break;

                case R.id.button2:

                    //Arranca o video
                    videoview.start();

                    // Identificar o texto do botão
                    departamento = ticketButton[1].getText().toString();

                    //  Começa a preencher o textBitmap com o conteudo do botão
                    textBitmap = addLineTextImage(null, departamento, 52, Align.ALIGN_CENTER);

                    //  Inicia o envio do request
                    try {
                        if (new Protocol(socket).sendMessage("Request:" + departamento)) {
                            Log.v("#inClickListener#", "Btn2 sends - " + departamento);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.server_Connection_Error, Toast.LENGTH_LONG).show();
                            Log.e("#inClickListener#", " - Btn2 - Error sending message");
                            startSocket();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("#inClickListener#", "- Btn2 - Catch you, trying to reconnect ! ");
                        startSocket();
                    }
                    break;


                case R.id.button3:

                    //Arranque do video
                    videoview.start();

                    // Identificar o texto do botão
                    departamento = ticketButton[2].getText().toString();

                    //  Começa a preencher o textBitmap com o conteudo do botão
                    textBitmap = addLineTextImage(null, departamento, 52, Align.ALIGN_CENTER);

                    //  Inicia o envio do request
                    try {
                        if (new Protocol(socket).sendMessage("Request:" + departamento)) {
                            Log.v("#inClickListener#", "Btn3 sends - " + departamento);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.server_Connection_Error, Toast.LENGTH_LONG).show();
                            Log.e("#inClickListener#", " - Btn3 - Error sending message");
                            startSocket();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("#inClickListener#", "- Btn3 - Catch you, trying to reconnect ! ");
                        startSocket();
                    }

                    break;

                case R.id.button4:

                    //Arranque do Video
                    videoview.start();

                    // Identificar o texto do botão
                    departamento = ticketButton[3].getText().toString();

                    //  Começa a preencher o textBitmap com o conteudo do botão
                    textBitmap = addLineTextImage(null, departamento, 52, Align.ALIGN_CENTER);

                    //  Inicia o envio do request
                    try {
                        if (new Protocol(socket).sendMessage("Request:" + departamento)) {
                            Log.v("#inClickListener#", "Btn4 sends - " + departamento);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.server_Connection_Error, Toast.LENGTH_LONG).show();
                            Log.e("#inClickListener#", " - Btn4 - Error sending message");
                            startSocket();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("#inClickListener#", "- Btn4 - Catch you, trying to reconnect ! ");
                        startSocket();
                    }

                    break;

                case R.id.button5:

                    //Arranque do video
                    videoview.start();

                    // Identificar o texto do botão
                    departamento = ticketButton[4].getText().toString();

                    //  Começa a preencher o textBitmap com o conteudo do botão
                    textBitmap = addLineTextImage(null, departamento, 52, Align.ALIGN_CENTER);

                    //  Inicia o envio do request
                    try {
                        if (new Protocol(socket).sendMessage("Request:" + departamento)) {
                            Log.v("#inClickListener#", "Btn5 sends - " + departamento);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.server_Connection_Error, Toast.LENGTH_LONG).show();
                            Log.e("#inClickListener#", " - Btn5 - Error sending message");
                            startSocket();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("#inClickListener#", "- Btn5 - Catch you, trying to reconnect ! ");
                        startSocket();
                    }

                    break;

                case R.id.txtDateButton:

                    //Tratamento da sequencia de saida
                    exitSequenceTest(1 * exitCounter);
                    break;

                case (R.id.logo):

                    //tratamento da sequencia de saida
                    exitSequenceTest(2 * exitCounter);
                    break;


                case R.id.code1:

                    //tratamento da sequencia de saida
                    exitSequenceTest(2 * exitCounter);
                    break;

                case (R.id.code2):

                    //tratamento da Sequencia de saida
                    exitSequenceTest(1 * exitCounter);
                    break;

            }
        }
    };


    private JAPower power;

    //  Criadas por asura
    private ScheduledExecutorService servicePollStatus;
    private JAPrinterStatus status;
    private JABarcodeGenerator barcodeGenerator;
    private Bitmap logoBitmap, qrBitmap, textBitmap, bottomTextBitmap;
    private Bitmap barcodeBitmap_QR;
    private Object printerLock = new Object();


    /**
     * Método onCreate - dispara sempre que a aplciação inicia
     * @param savedInstanceState - Estado da instancia
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.w("#onCreate#", "Arranque método on create");

        socketStayClosed = false;

        Toast.makeText(MainActivity.this, "O sistema irá desligar às " + getString(R.string.switchAzuraOff), Toast.LENGTH_LONG).show();


        /**
         * shutdown timer configure
         */

        // Turn Asura off at defined time in res.strings.xml
        Calendar rightNow = Calendar.getInstance();

        // If not on UTC
        long offset = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET);

        long sinceMidnight = (rightNow.getTimeInMillis() + offset) %
                (24 * 60 * 60 * 1000);
        String[] time = getString(R.string.switchAzuraOff).split(":");

        long shutdownTime = (Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1])) * 60 * 1000 - sinceMidnight;

        if (shutdownTime <= 10000) {
            shutdownTime = shutdownTime + 86400000;
        }
        Log.w("#onCreate#", "Desligar às (Horas) " + getString(R.string.switchAzuraOff));
        Log.w("#onCreate#", "Desligar dentro de (milisegundos) " + shutdownTime);
        Log.w("#onCreate#", "Desde a meia noite " + sinceMidnight);

        // Handler para tratar do processo de desligar
        new Handler().postDelayed(new Runnable() {
            public void run() {
                power = new JAPower();
                Toast.makeText(MainActivity.this, "São " + getString(R.string.switchAzuraOff), Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, "Sistema a desligar !", Toast.LENGTH_LONG).show();
                shutdown = true;
                onDestroy();
            }
        }, shutdownTime);
        Log.w("#onCreate#", "Handler timer criado com sucesso");



        try {
            super.onCreate(savedInstanceState);

            //chamada para ecran completo
            fullScreencall();

            setContentView(R.layout.activity_main);


            //Configuração e arranque do video

            videoview = (VideoView) findViewById(R.id.videoView);

            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ualVideo);

            videoview.setVideoURI(uri);

            videoview.start();


            //  Handler dos botões
            ticketButton[0] = (Button) findViewById(R.id.button1);
            ticketButton[1] = (Button) findViewById(R.id.button2);
            ticketButton[2] = (Button) findViewById(R.id.button3);
            ticketButton[3] = (Button) findViewById(R.id.button4);
            ticketButton[4] = (Button) findViewById(R.id.button5);
            code1 = (Button) findViewById(R.id.code1);
            code2 = (Button) findViewById(R.id.code2);
            connection = (TextView) findViewById(R.id.connection);

            logo = (ImageButton) findViewById(R.id.logo);

            //  Handler sockets
            updateConversationHandler = new Handler();

            //  Data
            txtDateButton = (Button) findViewById(R.id.txtDateButton);
            txtDateButton.setText(java.text.DateFormat.getDateInstance().format(Calendar.getInstance().getTime()));

            // Buttons Listner
            ticketButton[0].setOnClickListener(listener);
            ticketButton[1].setOnClickListener(listener);
            ticketButton[2].setOnClickListener(listener);
            ticketButton[3].setOnClickListener(listener);
            ticketButton[4].setOnClickListener(listener);
            code1.setOnClickListener(listener);
            code2.setOnClickListener(listener);
            logo.setOnClickListener(listener);
            txtDateButton.setOnClickListener(listener);


        /*
         *  Generate logo Bitmap from resource and align it center for sample receipt
         */
            Bitmap loadLogoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ualLogo); // add a image from resources
            loadLogoBitmap = Bitmap.createScaledBitmap(loadLogoBitmap, 291, 200, true); // set size to ignore scale
            int alignLogoCenter = (paperWidth - loadLogoBitmap.getWidth()) / 2;
            logoBitmap = Bitmap.createBitmap(loadLogoBitmap.getWidth() + alignLogoCenter, loadLogoBitmap.getHeight(), Bitmap.Config.RGB_565);

            Canvas canvasLogo = new Canvas(logoBitmap);
            canvasLogo.drawColor(Color.WHITE);
            canvasLogo.drawBitmap(loadLogoBitmap, alignLogoCenter, 0, null);

        /*
         *  Initialize JABarcodeGenerator instance and generate barcode Bitmap for sample receipt
         */
            barcodeGenerator = new JABarcodeGenerator();


            /*******************************************************************************************
             *  Initialize JAPrinter instance and start to use printer
             ******************************************************************************************/
            printer = new JAPrinter();

            synchronized (printerLock) {
                printer.claim();
                printerLock.notifyAll();
                printer.resetToDefault();

            }
        } catch (JAException e) {
            Toast.makeText(MainActivity.this, "Failed to claim printer", Toast.LENGTH_LONG).show();
        }

        servicePollStatus = Executors.newSingleThreadScheduledExecutor();

        servicePollStatus.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    synchronized (printerLock) {
                        /*
                         * Initialize JAPrinterStatus
                         */
                        status = printer.status();
                        printerLock.notifyAll();
                    }
                } catch (JAException e) {
                    Toast.makeText(MainActivity.this, "Failed to get printer status", Toast.LENGTH_SHORT).show();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (status.offline == true) {
                            Toast.makeText(MainActivity.this, "Impressora Offline", Toast.LENGTH_SHORT).show();
                        }
                        if (status.receiptPaperEmpty == true) {
                            Toast.makeText(MainActivity.this, "Sem Papel", Toast.LENGTH_SHORT).show();
                        } else if (status.receiptPaperLow == true) {
                            Toast.makeText(MainActivity.this, "Papel a terminar", Toast.LENGTH_SHORT).show();
                        }
                        if (status.coverOpen == true) {
                            Toast.makeText(MainActivity.this, "Portinhola aberta", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }, 0, 500, TimeUnit.MILLISECONDS);


        /*
         *  Start socket
         */
        startSocket();
    }


    /**
     * Metodo trata a sequencia de saida
     * @param code
     */

    private void exitSequenceTest(int code) {

        Log.w("#exitSequenceTest#", "Código premido - " + code);

        if (code != exitSequence[exitCounter]) {
            Log.e("#exitSequenceTest#", "Sequencia Errada - fez " + code + "mas devia ter feito " + exitSequence[exitCounter]);

            // Tom - primeiro valor é o volume, o segundo valor é a duração
            new ToneGenerator(AudioManager.STREAM_SYSTEM, 100).startTone(AudioManager.STREAM_SYSTEM, 10);
            exitCounter = 0;

        } else if (exitCounter == 5) {

            finish();

        } else {

            exitCounter++;
            Log.d("#exitSequenceTest#", "Counter-" + exitCounter);

        }


    }

    /**
     * Metodo onDestroy
     */
    @Override
    protected void onDestroy() {

        super.onDestroy();


        socketStayClosed = true;
        try {
            printer.ledReset();
            printer.ledGradualShift(3000, (byte) 0, (byte) 0, (byte) 0, (byte) 100, (byte) 100, (byte) 100);
        } catch (JAException e) {
            e.printStackTrace();
        }


        //  finish to use printer
        try {
            synchronized (printerLock) {
                printer.release();
                printerLock.notifyAll();
            }
        } catch (JAException e) {
            // ignore
        }


        // Close Socket
        try {
            if (socket != null) {
                //socket.shutdownOutput();
                socket.close();

            }

        } catch (IOException e) {
            //ignore
        }

        // In case of shutdown flag is true, then shutdown Asura
        if (shutdown) {
            try {
                power.shutdown();
            } catch (JAException e) {
                e.printStackTrace();
            }
        }

    }

    public void printTicket(String ticket, String urlString, String easyUrl) {

        //Alguma côr - Luz verde durante a impressão
        try {
            printer.ledSet((byte) 0, (byte) 100, (byte) 0);

        } catch (JAException e) {
            e.printStackTrace();
        }

        try {
            synchronized (printerLock) {

                // Construction text of the ticket
                textBitmap = addLineTextImage(textBitmap, " ", 20, Align.ALIGN_CENTER);
                textBitmap = addLineTextImage(textBitmap, "Senha Nº" + ticket, 60, Align.ALIGN_CENTER);
                textBitmap = addLineTextImage(textBitmap, " ", 20, Align.ALIGN_CENTER);
                textBitmap = addLineTextImage(textBitmap, java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()), 24, Align.ALIGN_CENTER);

                easyUrl = easyUrl.substring(0, easyUrl.length() - 1);

                String[] code = urlString.split("=");
                //url deverá ser sempre inferior a 90 caracteres

                if (easyUrl.length() > 45) {
                    bottomTextBitmap = addLineTextImage(null, easyUrl.substring(0, 45), 25, Align.ALIGN_CENTER);
                    bottomTextBitmap = addLineTextImage(bottomTextBitmap, easyUrl.substring(45, easyUrl.length()), 25, Align.ALIGN_CENTER);
                } else {
                    bottomTextBitmap = addLineTextImage(null, easyUrl, 25, Align.ALIGN_CENTER);
                }
                bottomTextBitmap = addLineTextImage(bottomTextBitmap, "Código -" + code[1], 25, Align.ALIGN_CENTER);


                // Set grey level, print speed, dithering and density settings
                printer.setGrayLevel(1);
                printer.setPrintSpeed(JAPrintSpeed.JAPRINT_SPEED_HIGH);
                printer.setDithering(JAPrintDithering.JAPRINT_DITHERING_NONE);
                printer.setDensity(0);

                // Start to print job

                //Logo da Ual
                printer.printBitmapImage(logoBitmap);
                //Espaço de 3mm
                printer.feedMM(3);

                //Nome do departamento, e numero de senha
                printer.printBitmapImage(textBitmap);
                textBitmap = null;
                //Espaço de 3mm
                printer.feedMM(5);

                //QR Code
                printer.printBitmapImage(barcodeBitmap_QR);
                barcodeBitmap_QR = null;
                //Espaço de 3mm
                printer.feedMM(3);

                //URL de acesso e senha para remotePanel
                printer.printBitmapImage(bottomTextBitmap);
                bottomTextBitmap = null;
                //Corte de Papel
                printer.cut(true);

                //release
                printerLock.notifyAll();
                printer.ledGradualShift(2000, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 73, (byte) 100);
            }
        } catch (JAException e) {
            Toast.makeText(MainActivity.this, "Failed to print sample", Toast.LENGTH_LONG).show();
        }

    }



    /*
     * Impressão do ticket
     */

    private Bitmap addLineTextImage(Bitmap dstBitmap, String lineText, int fontSize, Align align) {
        int alignment = 0;
        Bitmap expandedBitmap;
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setTextSize(fontSize);
        paint.setColor(Color.BLACK);

        FontMetrics fontMetrics = paint.getFontMetrics();
        int textWidth = (int) Math.ceil(paint.measureText(lineText));

        int textHeight = (int) Math.ceil(Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent) + Math.abs(fontMetrics.leading));

        switch (align) {
            case ALIGN_RIGHT:
                if (textWidth < paperWidth) {
                    alignment = paperWidth - textWidth;
                }
                break;

            case ALIGN_CENTER:
                if (textWidth < paperWidth) {
                    alignment = (paperWidth - textWidth) / 2;
                }
                break;

            default: // case ALIGN_LEFT:
                alignment = 0;
                break;
        }

        if (dstBitmap == null) {
            expandedBitmap = Bitmap.createBitmap(textWidth + alignment, textHeight, Bitmap.Config.RGB_565);

        } else {

            if (textWidth + alignment < dstBitmap.getWidth()) {

                textWidth = dstBitmap.getWidth();
            } else {

                textWidth += alignment;
            }
            expandedBitmap = Bitmap.createBitmap(textWidth, textHeight + dstBitmap.getHeight(), Bitmap.Config.RGB_565);
        }

        Canvas canvasText = new Canvas(expandedBitmap);
        canvasText.drawColor(Color.WHITE);

        if (dstBitmap == null) {
            canvasText.drawText(lineText, alignment, Math.abs(paint.getFontMetrics().ascent), paint);
        } else {
            canvasText.drawBitmap(dstBitmap, 0, 0, paint);
            canvasText.drawText(lineText, alignment, dstBitmap.getHeight() + Math.abs(paint.getFontMetrics().ascent), paint);
        }
        Log.w("#addLineTextImage#", "Até aqui foi rápido " + lineText);
        return expandedBitmap;
    }


    /**
     * Metodo para colocar o asura em full screen
     */

    public void fullScreencall() {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
    }




    public void createQRcode(String urlString) {
        try {
            barcodeBitmap_QR = barcodeGenerator.generateQrcode(urlString, QR_MODEL.QR_MODEL_2, QR_CELL_SIZE.QR_CELL_SIZE_6, QR_CORRECTION_LEVEL.QR_CORRECTION_LEVEL_1); // generate QRCode
            int alignQRCenter = (paperWidth - barcodeBitmap_QR.getWidth()) / 2;
            qrBitmap = Bitmap.createBitmap(barcodeBitmap_QR.getWidth() + alignQRCenter, barcodeBitmap_QR.getHeight(), Bitmap.Config.RGB_565);

            Canvas canvasQR = new Canvas(qrBitmap);
            canvasQR.drawColor(Color.WHITE);
            canvasQR.drawBitmap(barcodeBitmap_QR, alignQRCenter, 0, null);

            barcodeBitmap_QR = qrBitmap;

        } catch (JAException e) {
            Toast.makeText(MainActivity.this, "Failed to generate barcode: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    public void startSocket() {
        if (socketStayClosed) {
        } else {
            new Thread(new ClientThread(getString(R.string.serverIP), getResources().getInteger(R.integer.port))).start();
        }
    }

    //  Enum
    public enum Align {
        ALIGN_LEFT,
        ALIGN_CENTER,
        ALIGN_RIGHT
    }

    class ClientThread implements Runnable {

        private String serverIP;
        private int port;
        private String response;

        public ClientThread(String s, int p) {
            this.serverIP = s;
            this.port = p;
            Log.w("#ClientThread#", "Construtor");
        }


        @Override
        public void run() {

            InputStream inputStream;
            try {

                InetAddress serverAddr = InetAddress.getByName(this.serverIP);

                //Abertura do Socket

                do {
                    socket = null;
                    Log.i("#Socket#", "A estabelecer uma ligação ao servidor...");

                    try {

                        try {
                            printer.ledGradualShift(500
                                    , (byte) 0, (byte) 0, (byte) 0, (byte) 100, (byte) 20, (byte) 0);
                        } catch (JAException e) {
                            e.printStackTrace();
                        }
                        socket = new Socket(serverAddr, getResources().getInteger(R.integer.port));
                    } catch (IOException e) {
                        Log.e("#ClientThread#", "Ligação socket falhou - " + e.getMessage());
                        socket = null;
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }

                } while ((socket == null) || socket.isClosed());
                Log.i("#Socket#", "Ligação estabelecida :)");


                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];
                int bytesRead;

                inputStream = socket.getInputStream();

                if (!new Protocol(socket).startingProtocol()) {
                    Toast.makeText(MainActivity.this, "Falha ao iniciar o protocolo", Toast.LENGTH_LONG).show();
                }

                try {
                    while ((bytesRead = inputStream.read(buffer)) != -1)
                    {
                        Log.wtf("#ClientThread#", " inputstream - " + inputStream.available());

                        Log.wtf("#ClientThread#", " inputstream - a");

                        byteArrayOutputStream.write(buffer, 0, bytesRead);

                        Log.wtf("#ClientThread#", " inputstream - b");
                        response = byteArrayOutputStream.toString("UTF-8");

                        Log.wtf("#ClientThread#", " inputstream - c");
                        updateConversationHandler.post(new updateUIThread(response));

                        Log.wtf("#ClientThread#", " inputstream - d");
                        byteArrayOutputStream.reset();

                        Log.wtf("#ClientThread#", " inputstream - e");

                    }
                } catch (SocketException e) {
                    Log.e("#ClientThread#", "e.fillInStackTrace()", e.fillInStackTrace());
                    try {
                        new buttonsControl(MainActivity.this).showTryingConnection(MAXBUTTONS);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    startSocket();
                }


            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }

    public class updateUIThread implements Runnable {
        private String msg;


        public updateUIThread(String response) {
            this.msg = response;
        }

        @Override
        public void run() {

            Log.v("#updateUIThread#", " - Recebe - " + msg);

            new Protocol(MainActivity.this).receiveMessage(msg);

        }
    }


}

