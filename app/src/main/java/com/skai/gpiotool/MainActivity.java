package com.skai.gpiotool;
import androidx.appcompat.app.AppCompatActivity;

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
    private static final String TAG = "GPIOTOOL";

    private Button   mButtonUpdate;
    private Spinner  mSpinnerNumGPIO;
    private CheckBox mCheckBoxDirIn;
    private CheckBox mCheckBoxDirOut;
    private CheckBox mCheckBoxStateLow;
    private CheckBox mCheckBoxStateHight;

    private String mPathDirGpio = "/sdcard/gpio/"; //"/sys/class/gpio/"
    private ArrayList<String> mListGpio;
    private String[] mGpioDirections;
    private String[] mGpioStates;

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
        // get current gpio
        int id = mSpinnerNumGPIO.getSelectedItemPosition();

        // update check boxes direction
        mCheckBoxDirOut.setChecked(mGpioDirections[id].equals("out"));
        mCheckBoxDirIn.setChecked(mGpioDirections[id].equals("in"));

        // update check boxes state
        mCheckBoxStateHight.setChecked(mGpioStates[id].equals("1"));
        mCheckBoxStateLow.setChecked(mGpioStates[id].equals("0"));
        mCheckBoxStateHight.setClickable(mGpioDirections[id].equals("out"));
        mCheckBoxStateLow.setClickable(mGpioDirections[id].equals("out"));
    }

    protected void setGpioDirection(String direction) {
        // get current gpio
        int id = mSpinnerNumGPIO.getSelectedItemPosition();
        // set data
        try {
            FileOutputStream file = new FileOutputStream(mPathDirGpio.concat(mListGpio.get(id).concat("/direction")));
            file.write(direction.getBytes(), 0, direction.length());
            mGpioStates[id] = direction;
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
            FileOutputStream file = new FileOutputStream(mPathDirGpio.concat(mListGpio.get(id).concat("/value")));
            file.write(state.getBytes(), 0, state.length());
            mGpioStates[id] = state;
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
