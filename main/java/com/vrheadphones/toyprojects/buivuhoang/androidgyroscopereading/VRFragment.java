package com.vrheadphones.toyprojects.buivuhoang.androidgyroscopereading;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crickettechnology.audio.Bank;
import com.crickettechnology.audio.Ck;
import com.crickettechnology.audio.Config;
import com.crickettechnology.audio.Sound;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortOut;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// Import Cricket Audio library

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VRFragment extends Fragment {

    private static final String TAG = "VRHeadphones";
    //
    private OSCPortOut sender;
    // power lock
    private PowerManager.WakeLock lock;

    private GLSurfaceView mGLSurfaceView;
    private SensorManager mSensorManager;
    private GyroRenderer mRenderer;

    private TextView gyroTextView;
//    private Sound sound;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VRFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VRFragment newInstance(String param1, String param2) {
        VRFragment fragment = new VRFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public VRFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Settings.init(getActivity().getApplicationContext());
        // Hide the title bar
        if (this.lock == null) {
            Context appContext = this.getActivity().getApplicationContext();
            // get wake lock
            PowerManager manager = (PowerManager) appContext
                    .getSystemService(Context.POWER_SERVICE);
            this.lock = manager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, this
                    .getString(R.string.app_name));
        }

        try {
            this.sender = new OSCPortOut(InetAddress.getByName(Settings.ip), OSCPort
                    .defaultSCOSCPort());
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vr, container, false);

        gyroTextView = (TextView)view.findViewById(R.id.gyroTextView);

        mGLSurfaceView = (GLSurfaceView)view.findViewById(R.id.glSurfaceView);

        // Get an instance of the SensorManager
        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);

        // Create our Preview view and set it as the content of our
        // Activity
        mRenderer = new GyroRenderer();
        mGLSurfaceView.setRenderer(mRenderer);

        // Initialising Cricket Audio
        Config config = new Config();
        Ck.init(getActivity(), config);

//        Bank bank = Bank.newBank("sound.ckb");
//        sound = Sound.newBankSound(bank, "A Woman's Heart");
//        sound.set3dEnabled(true);
//        Sound.set3dListenerPosition(0, 0, 0, 0, 1, 0, 0, 0, 1);
//        sound.play();
        return view;
    }

    @Override
    public void onResume() {
        if (mGLSurfaceView != null) {
            mRenderer.start();
            mGLSurfaceView.onResume();
//            sound.setPaused(false);
//            Ck.resume();
            lock.acquire();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mGLSurfaceView != null) {
            mRenderer.stop();
            mGLSurfaceView.onPause();
//            sound.setPaused(true);
//            Ck.suspend();
            lock.release();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.sender.close();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    class GyroRenderer implements GLSurfaceView.Renderer, SensorEventListener {
        private Cube mCube;
        private Sensor mRotationVectorSensor;
        private final float[] mRotationMatrix = new float[16];

        private float[] originalCubeCentre = {0, 4, 0};
        private float[] currentCubeCentre = {0, 4, 0};

        public GyroRenderer() {
            // find the rotation-vector sensor
            mRotationVectorSensor = mSensorManager.getDefaultSensor(
                    Sensor.TYPE_ROTATION_VECTOR);

            mCube = new Cube();
            // initialize the rotation matrix to identity
            mRotationMatrix[ 0] = 1;
            mRotationMatrix[ 4] = 1;
            mRotationMatrix[ 8] = 1;
            mRotationMatrix[12] = 1;

        }

        public void start() {
            // enable our sensor when the activity is resumed, ask for
            // 10 ms updates.
            mSensorManager.registerListener(this, mRotationVectorSensor, 10000);
        }

        public void stop() {
            // make sure to turn our sensor off when the activity is paused
            mSensorManager.unregisterListener(this);
        }

        public void onSensorChanged(SensorEvent event) {
            // we received a sensor event. it is a good practice to check
            // that we received the proper event
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // convert the rotation-vector to a 4x4 matrix. the matrix
                // is interpreted by Open GL as the inverse of the
                // rotation-vector, which is what we want.
                SensorManager.getRotationMatrixFromVector(
                        mRotationMatrix , event.values);

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

//            gyroTextView.setText("x = " + orientation[0] + "\n" +
//                    "y = " + orientation[1] + "\n" +
//                    "z = " + orientation[2] + "\n");

//            sound.set3dPosition(currentCubeCentre[0], currentCubeCentre[1], currentCubeCentre[2]);

            Object[] args = new Object[3];
            args[0] = orientation[0];
            args[1] = orientation[1];
            args[2] = orientation[2];
            OSCMessage msg = new OSCMessage("/rotate", args);

            new OSCSender().execute(msg);

            Ck.update();
        }

        public void onDrawFrame(GL10 gl) {
            // clear screen
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

            // set-up modelview matrix
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslatef(0, 0, -3.0f);
            gl.glMultMatrixf(mRotationMatrix, 0);

            // draw our object
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

            mCube.draw(gl);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // set view-port
            gl.glViewport(0, 0, width, height);
            // set projection matrix
            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // dither is enabled by default, we don't need it
            gl.glDisable(GL10.GL_DITHER);
            // clear screen in white
            gl.glClearColor(1,1,1,1);
        }

        class Cube {
            // initialize our cube
            private FloatBuffer mVertexBuffer;
            private FloatBuffer mColorBuffer;
            private ByteBuffer mIndexBuffer;

            public Cube() {
                final float vertices[] = {
                        // centre cube
//                        -1, -1, -1,		 1, -1, -1,
//                        1,  1, -1,	    -1,  1, -1,
//                        -1, -1,  1,      1, -1,  1,
//                        1,  1,  1,     -1,  1,  1,

                        // off-centre cube. y + 4
                        -1, 3, -1,		 1, 3, -1,
                        1,  5, -1,	    -1, 5, -1,
                        -1, 3,  1,       1, 3,  1,
                        1,  5,  1,      -1, 5,  1,
                };

                final float colors[] = {
                        0,  0,  0,  1,  1,  0,  0,  1,
                        1,  1,  0,  1,  0,  1,  0,  1,
                        0,  0,  1,  1,  1,  0,  1,  1,
                        1,  1,  1,  1,  0,  1,  1,  1,
                };

                final byte indices[] = {
                        0, 4, 5,    0, 5, 1,
                        1, 5, 6,    1, 6, 2,
                        2, 6, 7,    2, 7, 3,
                        3, 7, 4,    3, 4, 0,
                        4, 7, 6,    4, 6, 5,
                        3, 0, 1,    3, 1, 2,
                };

                ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
                vbb.order(ByteOrder.nativeOrder());
                mVertexBuffer = vbb.asFloatBuffer();
                mVertexBuffer.put(vertices);
                mVertexBuffer.position(0);

                ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length*4);
                cbb.order(ByteOrder.nativeOrder());
                mColorBuffer = cbb.asFloatBuffer();
                mColorBuffer.put(colors);
                mColorBuffer.position(0);

                mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
                mIndexBuffer.put(indices);
                mIndexBuffer.position(0);
            }

            public void draw(GL10 gl) {
                gl.glEnable(GL10.GL_CULL_FACE);
                gl.glFrontFace(GL10.GL_CW);
                gl.glShadeModel(GL10.GL_SMOOTH);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
                gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Do nothing here
        }
    }

    class OSCSender extends AsyncTask<OSCMessage, Void, Void> {

        @Override
        protected Void doInBackground(OSCMessage... params) {
            for (OSCMessage param: params) {
                try {
                    sender.send(param);
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }
            }
            return null;
        }
    }
}
