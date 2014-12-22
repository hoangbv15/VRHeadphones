package com.vrheadphones.toyprojects.buivuhoang.androidgyroscopereading;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GyroReadingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GyroReadingFragment extends Fragment implements SensorEventListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final int FROM_RADS_TO_DEGS = -57;

    private SensorManager mSensorManager;
    private Sensor mRotationSensor;
    private TextView gyroTextView;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private float[] originalCubeCentre = {0, 4, 0};
    private float[] currentCubeCentre = {0, 4, 0};

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GyroReadingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GyroReadingFragment newInstance(String param1, String param2) {
        GyroReadingFragment fragment = new GyroReadingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public GyroReadingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gyro_reading, container, false);
        gyroTextView = (TextView)rootView.findViewById(R.id.gyroTextView);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        //unregister the sensor listener
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mRotationSensor) {
            if (event.values.length > 4) {
                float[] truncatedRotationVector = new float[4];
                System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4);
                update(truncatedRotationVector);
            } else {
                update(event.values);
            }
        }
    }

    private void update(float[] vectors) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);

        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, adjustedRotationMatrix);

        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        float x = originalCubeCentre[0];
        float y = originalCubeCentre[1];
        currentCubeCentre[0] = (float) (x * Math.cos(orientation[0]) - y * Math.sin(orientation[0]));
        currentCubeCentre[1] = (float) (x * Math.sin(orientation[0]) + y * Math.cos(orientation[0]));

        y = currentCubeCentre[1];
        float z = originalCubeCentre[2];
        currentCubeCentre[1] = (float) (y * Math.cos(orientation[2]) - z * Math.sin(orientation[2]));
        currentCubeCentre[2] = (float) (y * Math.sin(orientation[2]) + z * Math.cos(orientation[2]));

        x = currentCubeCentre[0];
        z = currentCubeCentre[2];
        currentCubeCentre[0] = (float) (  x * Math.cos(orientation[1]) - z * Math.sin(orientation[1]));
        currentCubeCentre[2] = (float) (- x * Math.sin(orientation[1]) + z * Math.cos(orientation[1]));

        gyroTextView.setText("x = " + currentCubeCentre[0] + "\n" +
                "y = " + currentCubeCentre[1] + "\n" +
                "z = " + currentCubeCentre[2] + "\n");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }
}
