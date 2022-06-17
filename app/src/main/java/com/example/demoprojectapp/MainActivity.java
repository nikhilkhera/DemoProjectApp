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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clientapp.OffloadingInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    TextView outputTextView;
    TextView executedOnTextview;
    TextView timeTakenForExecutionTextview;
    TextView functionTextview;
    TextView totalTimeTakenTextView;
    OffloadingInterface offloadingInterface;
    Button button;
    Button button2;
    ImageView connectedImageView;
    StatusThread myThread;
    EditText editText;
    boolean status = false;
    Long start;
    Long end;
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
        editText = findViewById(R.id.editTextTextPersonName);
        button2 = findViewById(R.id.button2);
        executedOnTextview = findViewById(R.id.executedOnTextView);
        timeTakenForExecutionTextview = findViewById(R.id.timeTakenForExecutionTextView);
        functionTextview = findViewById(R.id.functiontextview);
        totalTimeTakenTextView = findViewById(R.id.totalTimeTakenTextView);


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
                if (!status) {
                    RunLocalThread runLocalThread = new RunLocalThread();
                    runLocalThread.run();
                } else {
                    button_basic_logic("1");
                }
            }

        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!status) {
                    RunLocalThread2 runLocalThread2 = new RunLocalThread2();
                    runLocalThread2.run();
                } else {
                    button_basic_logic("2");
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
            try {
                start = System.currentTimeMillis();
                JSONObject x = helper.function_metric(Integer.parseInt(editText.getText().toString()));
                end = System.currentTimeMillis();
                add_to_ui(x);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class RunLocalThread2 implements Runnable{

        @Override
        public void run() {
            helperclass helper = new helperclass();
            try {
                start = System.currentTimeMillis();
                JSONObject x = helperclass.function_metric_add_million();
                end = System.currentTimeMillis();
                add_to_ui(x);
            } catch (JSONException e) {
                e.printStackTrace();
            }

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
                        end = System.currentTimeMillis();
                        JSONObject jsonObject = new JSONObject(response_data);
                        add_to_ui(jsonObject);
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

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    //----------------------------------------------------------------------------------
            void button_basic_logic(String func){
                if (!status) {
                    RunLocalThread runLocalThread = new RunLocalThread();
                    runLocalThread.run();
                } else {

                    if (offloadingInterface != null) {
                        try {
                            start = System.currentTimeMillis();
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("func", func+"");
                            jsonObject.put("para",editText.getText());
                            offloadingInterface.offload(jsonObject.toString());
                        } catch (RemoteException | JSONException e) {
                            e.printStackTrace();
                        }
                        RunOffloadThread runThread1 = new RunOffloadThread();
                        runThread1.run();

                    } else {
                        outputTextView.setText("Service Not Available");
                    }
                }
            }

            void add_to_ui(JSONObject jsonObject) throws JSONException {
                String output = jsonObject.getString("output");
                String computation_time = jsonObject.getString("computation_time");
                String function = jsonObject.getString("function");
                String device = jsonObject.getString("device");

                outputTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        outputTextView.setText(output);
                    }
                });

                executedOnTextview.post(new Runnable() {
                    @Override
                    public void run() {
                        executedOnTextview.setText(device);
                    }
                });

                functionTextview.post(new Runnable() {
                    @Override
                    public void run() {
                        functionTextview.setText(function);
                    }
                });

                timeTakenForExecutionTextview.post(new Runnable() {
                    @Override
                    public void run() {
                        timeTakenForExecutionTextview.setText(computation_time);
                    }
                });

                totalTimeTakenTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        totalTimeTakenTextView.setText((end-start)+"");
                    }
                });



            }
}