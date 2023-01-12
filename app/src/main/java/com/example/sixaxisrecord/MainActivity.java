package com.example.sixaxisrecord;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import com.google.android.material.snackbar.Snackbar;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private boolean isStartRecord = false;
    private static final ArrayList<String> gyroData = new ArrayList<>();
    private static final ArrayList<String> accelerometerData = new ArrayList<>();
    private static final ArrayList<String> actionData = new ArrayList<>();
    private SensorManager sensorManager;
    private Sensor mGyroscope = null;
    private Sensor mAccelerometer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //Gyroscope
        mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //Accelerometer
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Button harshAccelerateBtn = findViewById(R.id.harshAccelerateBtn);
        Button harshBrakeBtn = findViewById(R.id.harshBrakeBtn);
        Button harshCorneringBtn = findViewById(R.id.harshCorneringBtn);
        Button startBtn = findViewById(R.id.startBtn);
        Button endBtn = findViewById(R.id.endBtn);
        View contentView = findViewById(android.R.id.content);

        startBtn.setOnClickListener(v -> {
            isStartRecord = true;
            Snackbar.make(contentView, "測試開始", Snackbar.LENGTH_LONG).show();
        });
        endBtn.setOnClickListener(v -> {
            fileWriter(this);
            isStartRecord = false;
            gyroData.clear();
            accelerometerData.clear();
            actionData.clear();
            gyroLastTimesStamp = 0;
            accLastTimesStamp = 0;
            Snackbar.make(contentView, "測試結束", Snackbar.LENGTH_LONG).show();
        });
        /*harshAccelerateBtn.setOnClickListener(v -> {
            if(isStartRecord) {
                long timesStamp = System.currentTimeMillis();
                String dataLine = timesStamp + " " + "急加速\n";
                actionData.add(dataLine);
                Snackbar.make(contentView, "急加速", Snackbar.LENGTH_LONG).show();
            }
        });
        harshBrakeBtn.setOnClickListener(v -> {
            if(isStartRecord) {
                long timesStamp = System.currentTimeMillis();
                String dataLine = timesStamp + " " + "急煞車\n";
                actionData.add(dataLine);
                Snackbar.make(contentView, "急煞車", Snackbar.LENGTH_LONG).show();
            }
        });
        harshCorneringBtn.setOnClickListener(v -> {
            if(isStartRecord) {
                long timesStamp = System.currentTimeMillis();
                String dataLine = timesStamp + " " + "急轉彎\n";
                actionData.add(dataLine);
                Snackbar.make(contentView, "急轉彎", Snackbar.LENGTH_LONG).show();
            }
        });*/
    }
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroListener, mGyroscope,
                SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(accListener, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);
    }

    private long gyroLastTimesStamp = 0;
    private final SensorEventListener gyroListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE && isStartRecord) {
                long timesStamp = System.currentTimeMillis();
                long diff = timesStamp - gyroLastTimesStamp;
                // timesStamp gx gy gz
                if (diff > 200) {
                    String dataLine = "timestamp:" + timesStamp + " value:[" + event.values[0] + ","
                            + event.values[1] + "," + event.values[2] + "]\n";
                    gyroLastTimesStamp = timesStamp;
                    gyroData.add(dataLine);
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // do nothing
        }
    };
    private long accLastTimesStamp = 0;
    private final SensorEventListener accListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && isStartRecord) {
                long timesStamp = System.currentTimeMillis();
                long diff = timesStamp - accLastTimesStamp;
                // timesStamp ax ay az
                if (diff > 200) {
                    accLastTimesStamp = timesStamp;
                    String dataLine = "timestamp:" + timesStamp + " value:[" + event.values[0] + ","
                            + event.values[1] + "," + event.values[2] + "]\n";
                    accelerometerData.add(dataLine);
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // do nothing
        }
    };

    private void fileWriter(Context context){
        /**決定檔案名稱*/
        String gyroFileName = "gyroFile";
        String accelerometerFileName = "accelerometerFile";
        String actionFileName = "actionFile";

        /**決定檔案被存放的路徑*/

        String absoluteFilePath = "";
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            int currentApiVersion = Build.VERSION.SDK_INT;
            if(currentApiVersion < Build.VERSION_CODES.Q){
                absoluteFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            }else{
                File external = context.getExternalFilesDir(null);
                if(external != null){
                    absoluteFilePath = external.getAbsolutePath();
                }
            }
        }else{
            absoluteFilePath = context.getFilesDir().getAbsolutePath();
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        System.out.println("absoluteFilePath: " + absoluteFilePath);
        try {
            /**新增Data的資料夾*/
            File file = new File(absoluteFilePath);
            if (file.mkdir()) {
                System.out.println("新增資料夾");
            } else {
                System.out.println("資料夾無法生成或已存在");
            }

            File gyroFileLocation =
                    new File(absoluteFilePath + "/" + gyroFileName + ".txt");
            File accelerometerFileLocation =
                    new File(absoluteFilePath + "/" + accelerometerFileName + ".txt");
            File actionFileLocation =
                    new File(absoluteFilePath + "/" + actionFileName + ".txt");

            /**撰寫檔案內容*/
            FileOutputStream gyroFos = new FileOutputStream(gyroFileLocation);
            FileOutputStream accelerometerFos = new FileOutputStream(accelerometerFileLocation);
            FileOutputStream actionFos = new FileOutputStream(actionFileLocation);

            //gyro
            for(int i = 0; i < gyroData.size(); i++){
                gyroFos.write(gyroData.get(i).getBytes());
            }
            //acc
            for(int i = 0; i < accelerometerData.size(); i++){
                accelerometerFos.write(accelerometerData.get(i).getBytes());
            }
            //action
            for(int i = 0; i < actionData.size(); i++){
                actionFos.write(actionData.get(i).getBytes());
            }
            gyroFos.close();
            accelerometerFos.close();
            actionFos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}