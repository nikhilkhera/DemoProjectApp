package com.example.demoprojectapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clientapp.OffloadingInterface;

import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    TextView outputTextView;
    OffloadingInterface offloadingInterface;
    Button button;
    ImageView connectedImageView;
    StatusThread myThread;
    boolean status = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            offloadingInterface = OffloadingInterface.Stub.asInterface(iBinder);
            Log.d("Service Connection", "Remote Config Service Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        outputTextView = findViewById(R.id.outputTextView);
        button = findViewById(R.id.button);
        connectedImageView = findViewById(R.id.imageView);

//------------------------Set Intent to another package----------------------------------
        Intent intent = new Intent("com.example.clientapp.OffloadingService");
        intent.setPackage("com.example.clientapp");

//-------------------------Binding to the service-------------------------------------------
        boolean ret = bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        Toast.makeText(this, "" + ret, Toast.LENGTH_SHORT).show();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if status True offload else do not offload
                if (!status) {
                    RunLocalThread runLocalThread = new RunLocalThread();
                    runLocalThread.run();
                } else {

                    if (offloadingInterface != null) {
                        try {
                            offloadingInterface.offload("here");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        RunOffloadThread runThread1 = new RunOffloadThread();
                        runThread1.run();

                    } else {
                        outputTextView.setText("Service Not Available");
                    }
                }
            }
        });

        //Need to add delay while activity is created, otherwise app crashes

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                myThread = new StatusThread();
                myThread.setPriority(1);
                myThread.start();
            }
        }, 1000);
    }
    //----------------------------------checking status---------------------------------------
    private class StatusThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    boolean connected = offloadingInterface.get_status_connected();

                    connectedImageView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (connected) {
                                connectedImageView.setImageResource(R.drawable.status_on);
                                status = true;
                            } else {
                                connectedImageView.setImageResource(R.drawable.status_off);
                                status = false;
                            }
                        }
                    });
                    Thread.sleep(2000);
                } catch (RemoteException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    //------------------------local---------------------------------------------------
    class RunLocalThread implements Runnable{

        @Override
        public void run() {
            helperclass helper = new helperclass();
           // String x = helper.fib_metric(46);
           // outputTextView.setText(x);
        }
    }
    //--------------------------Offload----------------------------------------------
    class RunOffloadThread implements Runnable {

        @Override
        public void run() {
            try {

                for (int i = 0; i < 200; i++) {

                    Thread.sleep(500);

                    if (offloadingInterface.get_response_result()) {
                        String response_data = offloadingInterface.get_response_data();
                        outputTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                outputTextView.setText(response_data);
                            }
                        });

                        break;
                    }
                }
                if (!offloadingInterface.get_response_result()) {
                    outputTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            outputTextView.setText("Timeout");
                        }

                    });
                }
                offloadingInterface.set_response_result_false();

            } catch (InterruptedException | RemoteException e) {
                e.printStackTrace();

            }
        }
    }
    //----------------------------------------------------------------------------------


}