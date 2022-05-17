package com.skai.gpiotool;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
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
    private static final String TAG = "GPIOTOOL";

    private Button   mButtonUpdate;
    private Button   mButtonValue;
    private Button   mButtonDirection;
    private Spinner  mSpinnerNumGPIO;

    private String mPathDirGpio = "/sdcard/gpio/"; //"/sys/class/gpio/"
    private ArrayList<String> mListGpio;
    private String[] mGpioDirections;
    private String[] mGpioStates;

    private int mCurrentGpioID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonUpdate    = (Button)  findViewById(R.id.buttonUpdate);
        mButtonValue     = (Button)  findViewById(R.id.buttonValue);
        mButtonDirection = (Button)  findViewById(R.id.buttonDirection);
        mSpinnerNumGPIO  = (Spinner) findViewById(R.id.spinnerNumGPIO);

        // prepare gpio
        Log.i(TAG, "path: " + mPathDirGpio);
        File directory = new File(mPathDirGpio);
        File[] files = directory.listFiles();
        mListGpio = new ArrayList<String>();
        Pattern pattern = Pattern.compile(".*gpio[0-9]{1}.*");

        for (int i = 0; i < files.length; i++) {
            if (pattern.matcher(files[i].getAbsolutePath()).matches()) {
                File file = new File(files[i].getAbsolutePath().concat("/value"));
                if (file.canWrite()) {
                    mListGpio.add(files[i].getAbsolutePath().replace(mPathDirGpio, "").replace("/", ""));
                }
            }
        }

        mGpioDirections = new String[mListGpio.size()];
        mGpioStates     = new String[mListGpio.size()];

        // prepare spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListGpio);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerNumGPIO.setAdapter(adapter);
        mSpinnerNumGPIO.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentGpioID = (int)id;
                updatedView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // update data gpios
        updatedData();
        updatedView();

        // prepare button
        mButtonValue.setOnClickListener(v -> {
            if (mGpioStates[mCurrentGpioID].equals("1")) {
                setGpioState("0");
                mButtonValue.setText("Low");
            }
            else {
                setGpioState("1");
                mButtonValue.setText("High");
            }
        });

        mButtonDirection.setOnClickListener(v -> {
            if (mGpioDirections[mCurrentGpioID].equals("out")) {
                setGpioDirection("in");
                mButtonDirection.setText("Input");
            }
            else {
                setGpioDirection("out");
                mButtonDirection.setText("Output");
            }
        });

        mButtonUpdate.setOnClickListener(v -> {
            updatedData();
            updatedView();
        });
    }

    protected void updatedData() {
        // update gpio direction
        for (int i = 0; i < mListGpio.size(); i++) {
            try {
                RandomAccessFile file = new RandomAccessFile(mPathDirGpio.concat(mListGpio.get(i).concat("/direction")), "r");
                mGpioDirections[i] = file.readLine();
                Log.i(TAG, "direction: " + mPathDirGpio.concat(mListGpio.get(i).concat("/direction")) + " " + mGpioDirections[i]);
                file.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // update state gpio
        for (int i = 0; i < mListGpio.size(); i++) {
            try {
                RandomAccessFile file = new RandomAccessFile(mPathDirGpio.concat(mListGpio.get(i).concat("/value")), "r");
                mGpioStates[i] = file.readLine();
                Log.i(TAG, "value: " + mPathDirGpio.concat(mListGpio.get(i).concat("/value")) + " " + mGpioStates[i]);
                file.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void updatedView() {
        // update check boxes direction
        if (mGpioDirections[mCurrentGpioID].equals("out"))
            mButtonDirection.setText("Output");
        else
            mButtonDirection.setText("Input");

        // update check boxes state
        if (mGpioStates[mCurrentGpioID].equals("1"))
            mButtonValue.setText("High");
        else
            mButtonValue.setText("Low");
    }

    protected void setGpioDirection(String direction) {
        // set data
        try {
            FileOutputStream file = new FileOutputStream(mPathDirGpio.concat(mListGpio.get(mCurrentGpioID).concat("/direction")));
            file.write(direction.getBytes(), 0, direction.length());
            mGpioDirections[mCurrentGpioID] = direction;
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void setGpioState(String state) {
        // set data
        try {
            FileOutputStream file = new FileOutputStream(mPathDirGpio.concat(mListGpio.get(mCurrentGpioID).concat("/value")));
            file.write(state.getBytes(), 0, state.length());
            mGpioStates[mCurrentGpioID] = state;
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
