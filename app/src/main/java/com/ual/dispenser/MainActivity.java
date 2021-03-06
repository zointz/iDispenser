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
import com.StarMicronics.jasura.JAPrinter;
import com.StarMicronics.jasura.JAPower;
import com.StarMicronics.jasura.JAPrinter.JAPrintDithering;
import com.StarMicronics.jasura.JAPrinter.JAPrintSpeed;
import com.StarMicronics.jasura.JAPrinterStatus;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
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

    public static Boolean keepAlive, inactive = false, shutdown=false, socketStayClosed;
    private final int paperWidth = 576;
    private final int[] exitSequence = {0, 2, 2, 6, 8, 5, 12, 14, 8, 18};  //Sequência de saida
    VideoView videoview;

    //  Private
    private Button txtDateButton;
    private Button code1,code2;
    private Socket socket;
    private Handler updateConversationHandler;
    private Handler handler = new Handler();
    private ImageButton analogClock, logo;
    private int exitCounter = 0;
    private JAPower power;
    //  Criadas por asura
    /*
    private Button buttonPrint;
    private Button buttonCoverLock;
    private Button buttonCoverUnlock;
    private Button buttonExit;
    private TextView textViewOperation;
    private TextView textViewPaper;
    private TextView textViewCover;
    private TextView textViewDensity;
    private RadioGroup radioGroupGrey;
    private RadioGroup radioGroupSpeed;
    private SeekBar seekBarDensity;
    */
    private ScheduledExecutorService servicePollStatus;
    protected JAPrinter printer;
    private JAPrinterStatus status;
    private JABarcodeGenerator barcodeGenerator;
    private Bitmap logoBitmap, qrBitmap, textBitmap, bottomTextBitmap;
    private Bitmap barcodeBitmap_QR;
    private Object printerLock = new Object();

    //  Enum
    public enum Align {
        ALIGN_LEFT,
        ALIGN_CENTER,
        ALIGN_RIGHT
    }

    //  on Create
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        socketStayClosed = false;


        Log.w("#onCreate#", "Arranque método on create");

        Toast.makeText(MainActivity.this, "O sistema irá desligar às " + getString(R.string.switchAzuraOff), Toast.LENGTH_LONG).show();

        // Turn Asura off at defined time in res.strings.xml

        Calendar rightNow = Calendar.getInstance();

        // If not on UTC
        long offset = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET);

        long sinceMidnight = (rightNow.getTimeInMillis() + offset) %
                (24 * 60 * 60 * 1000);
        String [] time = getString(R.string.switchAzuraOff).split(":");

        long shutdownTime =  (Integer.parseInt(time[0])*60+Integer.parseInt(time[1]))*60*1000-sinceMidnight;

        if (shutdownTime <=10000) {
            shutdownTime = shutdownTime + 86400000;
        }
        Log.w("#onCreate#", "Desligar às (Horas) " +getString(R.string.switchAzuraOff));
        Log.w("#onCreate#", "Desligar dentro de (milisegundos) " + shutdownTime);
        Log.w("#onCreate#", "Desde a meia noite " + sinceMidnight);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                power = new JAPower();
                Toast.makeText(MainActivity.this, "O sistema irá desligar às " + getString(R.string.switchAzuraOff), Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, "Sistema a desligar !", Toast.LENGTH_LONG).show();
                    shutdown=true;
                    onDestroy();
            }
        }, shutdownTime);
        Log.w("#onCreate#", "Handler timer criado com sucesso");

/*
        Process proc = null;

        String ProcID = "79"; //HONEYCOMB AND OLDER

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            ProcID = "42"; //ICS AND NEWER
        }

        try {
            proc = Runtime.getRuntime().exec(new String[] { "su", "-c", "service call activity "+ProcID+" s16 com.android.systemui" });
        } catch (Exception e) {
            Log.w("Main","Failed to kill task bar (1).");
            e.printStackTrace();
        }
        try {
            proc.waitFor();
        } catch (Exception e) {
            Log.w("Main","Failed to kill task bar (2).");
            e.printStackTrace();
        }

*/
//        setSystemUIEnabled(true);


        try {
            super.onCreate(savedInstanceState);
            //  create method to inicial screen configurations handle fullscreen

            fullScreencall();

            setContentView(R.layout.activity_main);





            videoview = (VideoView) findViewById(R.id.videoView);

            Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.ual3);

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
            // analogClock = (Button) findViewById(R.id.analogClock);
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

            // Criados por Asura
            //buttonPrint        = (Button)findViewById(R.id.buttonPrint);
            //buttonCoverLock    = (Button)findViewById(R.id.buttonCoverLock);
            //buttonCoverUnlock  = (Button)findViewById(R.id.buttonCoverUnlock);
            //buttonExit         = (Button)findViewById(R.id.buttonExit);
            //textViewOperation  = (TextView)findViewById(R.id.textViewOperation);
            //textViewPaper      = (TextView)findViewById(R.id.textViewPaper);
            //textViewCover      = (TextView)findViewById(R.id.textViewCover);
            //textViewDensity    = (TextView)findViewById(R.id.textViewDensity);
            //radioGroupGrey     = (RadioGroup)findViewById(R.id.radioGroupGreyLevel);
            //radioGroupSpeed    = (RadioGroup)findViewById(R.id.radioGroupSpeed);
            //checkBoxDithering = (CheckBox)findViewById(R.id.checkBoxDithering);
            //seekBarDensity     = (SeekBar)findViewById(R.id.seekBarDensity);
            //buttonCoverLock.setOnClickListener(listener);
            //buttonCoverUnlock.setOnClickListener(listener);
            //buttonExit.setOnClickListener(listener);
            //radioGroupGrey.check(R.id.radioGreylevel1);
            //radioGroupSpeed.check(R.id.radioSpeedFull);

            /*seekBarDensity.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    textViewDensity.setText("Density: " + (seekBarDensity.getProgress() - 3));
                    }
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    // none
                    }
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    // none
                    }
                }
            );*/

        /*
         *  Generate logo Bitmap from resource and align it center for sample receipt
         */
            Bitmap loadLogoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ual_horizontal); // add a image from resources
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


        /********************************************************************************************
         Set Led Colors

         Methods to handle led colors

         ledFlashForANumberOfTime (int times, byte red1, byte green1, byte blue1, byte red2, byte green2, byte blue2)
         ledGradualShift (int time, byte red1, byte green1, byte blue1, byte red2, byte green2, byte blue2)
         ledGradualShiftThenFlash (int time, byte red1, byte green1, byte blue1, byte red2, byte green2, byte blue2)
         printer.ledSet((byte)100,(byte)0,(byte)40); (Valores de zero a 100)
         ledReset ()

         time	The interval of time for the lights to shift from one color to another in milliseconds. This should be from 50 to 10000.
         red1	This is the red component for the first color in the gradual shift. This should be 0 to 100.
         green1	This is the green component for the first color in the gradual shift. This should be 0 to 100.
         blue1	This is the blue component for the first color in the gradual shift. This should be 0 to 100.
         red2	This is the red component for the second color in the gradual shift. This should be 0 to 100.
         green2	This is the green component for the second color in the gradual shift. This should be 0 to 100.
         blue2	This is the blue component for the second color in the gradual shift. This should be 0 to 100.
         *********************************************************************************************/

        /*try {
            if (!socketStayClosed) {
            //    printer.ledReset();
              printer.ledGradualShift(1000, (byte) 0, (byte) 0, (byte) 0, (byte) 100, (byte) 30, (byte) 0);
                Log.w("#OnCreate#", " Led urgente");
                //printer.ledGradualShift(2000, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 73, (byte) 100);
                // printer.ledGradualShift (2000,(byte)100, (byte)100, (byte)0, (byte)0,(byte)0, (byte) 0);
            }
        } catch (JAException e) {
            e.printStackTrace();
        }*/

        /*
         *  Start to polling printer status
         */

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

        //        try {
        //            new zzzTransportMessage(socket).send("222Hello from Asura CPRN!");
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        }
    }


    //  Listener

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        String departamento;

            switch (v.getId()) {

                case R.id.button1:
                    videoview.start();
                    // Identificar o texto do botão
                    departamento = ticketButton[0].getText().toString();
                    //  Começa a preencher o textBitmap com o conteudo do botão
                    textBitmap = addLineTextImage(null, departamento , 52, Align.ALIGN_CENTER);
                    //  Inicia o envio do request
                    try {
                        if (new Protocol(socket, MainActivity.this).sendMessage("Request:" + departamento)) {
                            Log.v("#inClickListener#", "Btn1 sends - " + departamento);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.server_Connection_Error, Toast.LENGTH_LONG).show();
                            Log.e("#inClickListener#"," - Btn1 - Error sending message");
                            startSocket();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("#inClickListener#", "- Btn1 - Catch you, trying to reconnect ! ");
                        startSocket();
                    }


                    break;

                case R.id.button2:
                    videoview.start();
                    // Identificar o texto do botão
                    departamento = ticketButton[1].getText().toString();
                    //  Começa a preencher o textBitmap com o conteudo do botão
                    textBitmap = addLineTextImage(null, departamento , 52, Align.ALIGN_CENTER);
                    //  Inicia o envio do request
                    try {
                        if (new Protocol(socket, MainActivity.this).sendMessage("Request:" + departamento)) {
                            Log.v("#inClickListener#", "Btn2 sends - " + departamento);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.server_Connection_Error, Toast.LENGTH_LONG).show();
                            Log.e("#inClickListener#"," - Btn2 - Error sending message");
                            startSocket();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("#inClickListener#", "- Btn2 - Catch you, trying to reconnect ! ");
                        startSocket();
                    }
                    break;


                case R.id.button3:
                    videoview.start();
                    // Identificar o texto do botão
                    departamento = ticketButton[2].getText().toString();
                    //  Começa a preencher o textBitmap com o conteudo do botão
                    textBitmap = addLineTextImage(null, departamento , 52, Align.ALIGN_CENTER);
                    //  Inicia o envio do request
                    try {
                        if (new Protocol(socket, MainActivity.this).sendMessage("Request:" + departamento)) {
                            Log.v("#inClickListener#", "Btn3 sends - " + departamento);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.server_Connection_Error, Toast.LENGTH_LONG).show();
                            Log.e("#inClickListener#"," - Btn3 - Error sending message");
                            startSocket();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("#inClickListener#", "- Btn3 - Catch you, trying to reconnect ! ");
                        startSocket();
                    }
                    break;

                case R.id.button4:
                    videoview.start();
                    // Identificar o texto do botão
                    departamento = ticketButton[3].getText().toString();
                    //  Começa a preencher o textBitmap com o conteudo do botão
                    textBitmap = addLineTextImage(null, departamento , 52, Align.ALIGN_CENTER);
                    //  Inicia o envio do request
                    try {
                        if (new Protocol(socket, MainActivity.this).sendMessage("Request:" + departamento)) {
                            Log.v("#inClickListener#", "Btn4 sends - " + departamento);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.server_Connection_Error, Toast.LENGTH_LONG).show();
                            Log.e("#inClickListener#"," - Btn4 - Error sending message");
                            startSocket();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("#inClickListener#", "- Btn4 - Catch you, trying to reconnect ! ");
                        startSocket();
                    }
                    break;

                case R.id.button5:
                    videoview.start();
                    // Identificar o texto do botão
                    departamento = ticketButton[4].getText().toString();
                    //  Começa a preencher o textBitmap com o conteudo do botão
                    textBitmap = addLineTextImage(null, departamento , 52, Align.ALIGN_CENTER);
                    //  Inicia o envio do request
                    try {
                        if (new Protocol(socket, MainActivity.this).sendMessage("Request:" + departamento)) {
                            Log.v("#inClickListener#", "Btn5 sends - " + departamento);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.server_Connection_Error, Toast.LENGTH_LONG).show();
                            Log.e("#inClickListener#"," - Btn5 - Error sending message");
                            startSocket();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("#inClickListener#", "- Btn5 - Catch you, trying to reconnect ! ");
                        startSocket();
                    }
                    break;

                case R.id.txtDateButton:
                    exitSequenceTest(1 * exitCounter);
                    break;

                case (R.id.logo):
                    exitSequenceTest(2 * exitCounter);
                    break;


                case R.id.code1:
                    exitSequenceTest(2 * exitCounter);
                    break;

                case (R.id.code2):
                    exitSequenceTest(1 * exitCounter);
                    break;

            }
        }
    };

    /**
     * Metodo exit sequence
     */


    private void exitSequenceTest(int code) {

        Log.w("#exitSequenceTest#", "Código premido - " + code);

        if (code != exitSequence[exitCounter]) {
            Log.e("#exitSequenceTest#","Sequencia Errada - fez "+ code + "mas devia ter feito " + exitSequence[exitCounter]);

            // Tom - primeiro valor é o volume, o segundo valor é a duração
            new ToneGenerator(AudioManager.STREAM_SYSTEM, 100).startTone(AudioManager.STREAM_SYSTEM, 10);
            exitCounter = 0;

        } else if (exitCounter == 5) {
/*
            Process proc = null;
            try {
                proc = Runtime.getRuntime().exec(new String[] { "su", "-c", "am startservice -n com.android.systemui/.SystemUIService" }); //"am startservice -n com.android.systemui/.SystemUIService" });
                Log.w("Main","Executed command");
            } catch (Exception e) {
                Log.w("Main","Failed to kill task bar (1).");
                e.printStackTrace();
            }
            try {
                proc.waitFor();
            } catch (Exception e) {
                Log.w("Main","Failed to kill task bar (2).");
                e.printStackTrace();
            }*/
            finish();
        } else {
            exitCounter++;
            Log.d("#exitSequenceTest#","Counter-" + exitCounter);
        }


    }

    /**
     * Metodo onDestroy
     */
    @Override
    protected void onDestroy() {

        super.onDestroy();


        socketStayClosed=true;
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



    /*
     * Impressão do ticket
     */

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

                easyUrl =  easyUrl.substring(0, easyUrl.length() - 1);

                String[] code = urlString.split("=");
                //url deverá ser sempre inferior a 90 caracteres
                if (easyUrl.length()>45){
                    bottomTextBitmap = addLineTextImage(null, easyUrl.substring(0, 45), 25, Align.ALIGN_CENTER);
                    bottomTextBitmap = addLineTextImage(bottomTextBitmap,easyUrl.substring(45,easyUrl.length()), 25, Align.ALIGN_CENTER);
               }else {
                    bottomTextBitmap = addLineTextImage(null, easyUrl, 25, Align.ALIGN_CENTER);
                }
                bottomTextBitmap = addLineTextImage(bottomTextBitmap,"Código -" + code[1], 25, Align.ALIGN_CENTER);





                // Set grey level, print speed, dithering and density settings
                printer.setGrayLevel(1);
                printer.setPrintSpeed(JAPrintSpeed.JAPRINT_SPEED_HIGH);
               // printer.setPrintSpeed(JAPrintSpeed.JAPRINT_SPEED_MEDIUM);
                printer.setDithering(JAPrintDithering.JAPRINT_DITHERING_NONE);
                printer.setDensity(0);

                // Start to print job

                //Logo da Ual
                printer.printBitmapImage(logoBitmap);
                //Espaço de 3mm
                printer.feedMM(3);

                //Nome do departamento, e numero de senha
                printer.printBitmapImage(textBitmap);
                textBitmap=null;
                //Espaço de 3mm
                printer.feedMM(5);

                //QR Code
                printer.printBitmapImage(barcodeBitmap_QR);
                barcodeBitmap_QR=null;
                //Espaço de 3mm
                printer.feedMM(3);

                //URL de acesso e senha para remotePanel
                printer.printBitmapImage(bottomTextBitmap);
                bottomTextBitmap=null;
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
        Log.w("#addLineTextImage#","Até aqui foi rápido "+lineText);
        return expandedBitmap;
    }

    public void fullScreencall() {

        //* The next 2 lines code is to except application title bar and set full screen.
        //* If already set "android:theme="@android:style/Theme.Holo.Light.NoActionBar.Fullscreen"" in AndroidManifest.xml, do not need to add the following codes.
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //mContentView.setSystemUiVisibility(
        //View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LOW_PROFILE
        //View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        /*
          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
          | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
        */
/*
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
*/

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
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
                       // Toast.makeText(MainActivity. "Hello", Toast.LENGTH_SHORT).show();

                        // new buttonsControl(MainActivity.this).showTryingConnection(MAXBUTTONS);

                        try {
                            printer.ledGradualShift(500
                                    , (byte) 0, (byte) 0, (byte) 0, (byte) 100, (byte) 20, (byte) 0);
                        } catch (JAException e) {
                            e.printStackTrace();
                        }
                        socket = new Socket(serverAddr, getResources().getInteger(R.integer.port));
                    } catch (IOException e) {
                        Log.e("#ClientThread#","Ligação socket falhou - " + e.getMessage() );
                        socket=null;
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }

                } while ((socket == null) || socket.isClosed());
                Log.i("#Socket#","Ligação estabelecida :)");

        
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];
                int bytesRead;

                inputStream = socket.getInputStream();

                // Inicio do protocolo
                // new Protocol(socket).startingProtocol();
                // if (!new zzzProcessingProtocol(socket).startingProtocol()) {

                if (!new Protocol(socket).startingProtocol()) {
                    Toast.makeText(MainActivity.this, "Falha ao iniciar o protocolo", Toast.LENGTH_LONG).show();
                }

                try {
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
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
                      /*  try {
                         //   Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
                    }
                } catch (SocketException e) {
                    Log.e("#ClientThread#", "e.fillInStackTrace()",e.fillInStackTrace());
                    try {
                        new buttonsControl(MainActivity.this).showTryingConnection(MAXBUTTONS);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                        startSocket();
                    }


            } catch (UnknownHostException e1) {
                e1.printStackTrace();

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
            /*try {
                new zzzTransportMessage(getBaseContext()).receive("Chegou:" + msg);                               //IMPORTANTE
            } catch (IOException e) {
                e.printStackTrace();
            }
*/

            Log.v("#updateUIThread#", " - Recebe - " + msg);
            new Protocol(MainActivity.this).receiveMessage(msg);

            //new Protocol(getBaseContext()).receiveMessage(msg);
            // printTicket();


        }
    }


/*

    @Override
    public void onBackPressed() {
        Toast.makeText(MainActivity.this, "Para sair proceda com a sequência de escape !", Toast.LENGTH_SHORT).show();


    }
*/


    public void setSystemUIEnabled(boolean enabled) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("pm " + (enabled ? "enable" : "disable")
                    + " com.android.systemui\n");
            os.writeBytes("exit\n");
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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


    /**
     * Testa a connecção ao servidor
     *
     * @return Se a ligação ainda está viva
     */

    public boolean keepAlive() {

        keepAlive = false;


        try {
            new Protocol(socket, MainActivity.this).sendMessage("KEEPALIVE");
            Log.v("#keepalive#", "Sent");
        } catch (Exception e) {
            e.printStackTrace();
        }


        Log.v("#keepalive# - Estado", keepAlive + "");
        if (keepAlive)
            return true;
        else {
            startSocket();
            return false;
        }

    }

public void startSocket(){
    if (socketStayClosed){
    }
    else {
        new Thread(new ClientThread(getString(R.string.serverIP), getResources().getInteger(R.integer.port))).start();
    }
}


}

