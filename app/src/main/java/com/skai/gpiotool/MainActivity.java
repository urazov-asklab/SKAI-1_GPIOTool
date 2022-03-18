package com.skai.gpiotool;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.ContextCompat;
//import androidx.core.app.ActivityCompat;
//import android.Manifest;
//import android.content.pm.PackageManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AdapterView;

import java.io.File;

import java.util.regex.Pattern;
import java.util.ArrayList;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    private Button   mButtonUpdate;
    private Spinner  mSpinnerNumGPIO;
    private CheckBox mCheckBoxDirIn;
    private CheckBox mCheckBoxDirOut;
    private CheckBox mCheckBoxStateLow;
    private CheckBox mCheckBoxStateHight;

    //private String pathDirGpio = "/sys/class/gpio/";
    private String pathDirGpio = "/sdcard/gpio/";
    private ArrayList<String> listGpio;
    private String[] gpioDirections;
    private String[] gpioStates;
    private static final String TAG = "GPIOTOOL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonUpdate       = (Button)   findViewById(R.id.buttonUpdate);
        mSpinnerNumGPIO     = (Spinner)  findViewById(R.id.spinnerNumGPIO);
        mCheckBoxDirIn      = (CheckBox) findViewById(R.id.checkBoxDirIn);
        mCheckBoxDirOut     = (CheckBox) findViewById(R.id.checkBoxDirOut);
        mCheckBoxStateLow   = (CheckBox) findViewById(R.id.checkBoxStateLow);
        mCheckBoxStateHight = (CheckBox) findViewById(R.id.checkBoxStateHight);

        //int REQUEST_READ_EXTERNAL_STORAGE = 1;
        //int REQUEST_WRITE_EXTERNAL_STORAGE = 2;
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        //    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
        //}
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        //    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        //}

        // prepare gpio
        Log.i(TAG, "path: " + pathDirGpio);
        File directory = new File(pathDirGpio);
        File[] files = directory.listFiles();
        listGpio = new ArrayList<String>();
        Pattern pattern = Pattern.compile(".*gpio[0-9]{1}.*");

        for (int i = 0; i < files.length; i++) {
            if (pattern.matcher(files[i].getAbsolutePath()).matches()) {
                //File file = new File(files[i].getAbsolutePath().concat("/value"));
                //if (file.canWrite()) {
                    listGpio.add(files[i].getAbsolutePath().replace(pathDirGpio, "").replace("/", ""));
                //}
            }
        }

        gpioDirections = new String[listGpio.size()];
        gpioStates     = new String[listGpio.size()];

        // prepare spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listGpio);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerNumGPIO.setAdapter(adapter);
        mSpinnerNumGPIO.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatedView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // update data gpios
        updatedData();
        updatedView();

        // prepare check boxes direction
        mCheckBoxDirIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setGpioDirection(isChecked ? "in" : "out");
                mCheckBoxDirOut.setChecked(!mCheckBoxDirIn.isChecked());
                mCheckBoxStateHight.setClickable(mCheckBoxDirOut.isChecked());
                mCheckBoxStateLow.setClickable(mCheckBoxDirOut.isChecked());
                mCheckBoxStateHight.setClickable(mCheckBoxDirOut.isChecked());
                mCheckBoxStateLow.setClickable(mCheckBoxDirOut.isChecked());
            }
        });

        mCheckBoxDirOut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setGpioDirection(isChecked ? "out" : "in");
                mCheckBoxDirIn.setChecked(!mCheckBoxDirOut.isChecked());
                mCheckBoxStateHight.setClickable(mCheckBoxDirOut.isChecked());
                mCheckBoxStateLow.setClickable(mCheckBoxDirOut.isChecked());
                mCheckBoxStateHight.setClickable(mCheckBoxDirOut.isChecked());
                mCheckBoxStateLow.setClickable(mCheckBoxDirOut.isChecked());
            }
        });

        // prepare check boxes state
        mCheckBoxStateLow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setGpioState(isChecked ? "0" : "1");
                mCheckBoxStateHight.setChecked(!isChecked);
            }
        });

        mCheckBoxStateHight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setGpioState(isChecked ? "1" : "0");
                mCheckBoxStateLow.setChecked(!isChecked);
            }
        });


        // prepare button
        View.OnClickListener clickButtonUpdate = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatedData();
                updatedView();
            }
        };
        mButtonUpdate.setOnClickListener(clickButtonUpdate);
    }

    protected void updatedData() {
        // update gpio direction
        for (int i = 0; i < listGpio.size(); i++) {
            try {
                RandomAccessFile file = new RandomAccessFile(pathDirGpio.concat(listGpio.get(i).concat("/direction")), "r");
                gpioDirections[i] = file.readLine();
                Log.i(TAG, "direction: " + pathDirGpio.concat(listGpio.get(i).concat("/direction")) + " " + gpioDirections[i]);
                file.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // update state gpio
        for (int i = 0; i < listGpio.size(); i++) {
            try {
                RandomAccessFile file = new RandomAccessFile(pathDirGpio.concat(listGpio.get(i).concat("/value")), "r");
                gpioStates[i] = file.readLine();
                Log.i(TAG, "value: " + pathDirGpio.concat(listGpio.get(i).concat("/value")) + " " + gpioStates[i]);
                file.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void updatedView() {
        // get current gpio
        int id = mSpinnerNumGPIO.getSelectedItemPosition();

        // update check boxes direction
        mCheckBoxDirOut.setChecked(gpioDirections[id].equals("out"));
        mCheckBoxDirIn.setChecked(gpioDirections[id].equals("in"));

        // update check boxes state
        mCheckBoxStateHight.setChecked(gpioStates[id].equals("1"));
        mCheckBoxStateLow.setChecked(gpioStates[id].equals("0"));
        mCheckBoxStateHight.setClickable(gpioDirections[id].equals("out"));
        mCheckBoxStateLow.setClickable(gpioDirections[id].equals("out"));
    }

    protected void setGpioDirection(String direction) {
        // get current gpio
        int id = mSpinnerNumGPIO.getSelectedItemPosition();
        // set data
        try {
            FileOutputStream file = new FileOutputStream(pathDirGpio.concat(listGpio.get(id).concat("/direction")));
            file.write(direction.getBytes(), 0, direction.length());
            gpioStates[id] = direction;
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void setGpioState(String state) {
        // get current gpio
        int id = mSpinnerNumGPIO.getSelectedItemPosition();
        // set data
        try {
            FileOutputStream file = new FileOutputStream(pathDirGpio.concat(listGpio.get(id).concat("/value")));
            file.write(state.getBytes(), 0, state.length());
            gpioStates[id] = state;
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
