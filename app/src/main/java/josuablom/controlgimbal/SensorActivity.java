package josuablom.controlgimbal;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import com.google.android.glass.media.Sounds;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Blommie on 2015-08-12.
 */
public class SensorActivity extends Activity
{
    private static final float TOO_STEEP_PITCH_DEGREES = 70.0f;
    private boolean mLowAccuracy, mTooSteep;
    private TextView mAzimuthText, mPitchText, mRollText, mWarningText;
    private SensorManager mSensorManager;
    private Sensor mRotationVectorSensor, mMagneticSensor;
    private float[] mRotationMatrix = new float[16];
    private float[] mOrientation = new float[3];
    private Date mSensorDataUpdatedTime;

    public static final String SERVERIP = "192.168.43.82";
    public static final int SERVERPORT = 2390;
    public String sendOrientation = "";
    public String message = "";

    float azimuthDeg = 0;
    float pitchDeg = 0;
    float rollDeg = 0;
    float initialAzimuth = 0;
    boolean firstData = false;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor);
        mAzimuthText = (TextView) findViewById(R.id.azimuth);
        mPitchText = (TextView) findViewById(R.id.pitch);
        mRollText = (TextView) findViewById(R.id.roll);
        mWarningText = (TextView) findViewById(R.id.warning);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Log.e("Alert","Unknown Error Caught");
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {    if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
        ((AudioManager)getSystemService(Context.AUDIO_SERVICE))
                .playSoundEffect(Sounds.DISALLOWED);
        return true;    }
    else {
        return super.onKeyDown(keyCode, event);
    } }


    private void registerForSensorUpdates()
    {
        mSensorManager.registerListener(mSensorEventListener, mRotationVectorSensor,
                SensorManager.SENSOR_DELAY_UI);
        // obtain accuracy updates from the magnetic field sensor since the rotation vector
        // sensor does not provide any
         mSensorManager.registerListener(mSensorEventListener, mMagneticSensor, SensorManager.SENSOR_DELAY_UI);
    }

        @Override
    protected void onResume()
    {
        super.onResume();
        //mSensorManager.registerListener(this, mRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorDataUpdatedTime = new Date();
        registerForSensorUpdates();
    }

    @Override protected void onPause()
    {
        mSensorManager.unregisterListener(mSensorEventListener);
        super.onPause();
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener()
    {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // convert rotation vector to azimuth, pitch, & roll
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                // take into account Glass's coordinate system
                SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
                SensorManager.getOrientation(mRotationMatrix, mOrientation);
                azimuthDeg = (float) Math.toDegrees(mOrientation[0]) - initialAzimuth;
                pitchDeg = (float) Math.toDegrees(mOrientation[1]);
                rollDeg = (float) Math.toDegrees(mOrientation[2]);
                mTooSteep = pitchDeg > TOO_STEEP_PITCH_DEGREES ||
                        pitchDeg < -TOO_STEEP_PITCH_DEGREES;
                updateWarning();


                if(firstData == false)
                {
                    initialAzimuth = azimuthDeg;
                    firstData = true;
                }

                sendOrientation = Float.toString(pitchDeg) + "," + Float.toString(azimuthDeg) + "," + Float.toString(rollDeg);

                if (new Date().getTime() - mSensorDataUpdatedTime.getTime() < 100) return;
                {
                    mAzimuthText.setText(String.format(Locale.US, "%.1f", azimuthDeg));
                    mPitchText.setText(String.format(Locale.US, "%.1f", pitchDeg));
                    mRollText.setText(String.format(Locale.US, "%.1f", rollDeg));
                    mSensorDataUpdatedTime = new Date();

                    message = sendOrientation;
                    new Thread(new Client()).start();

                }

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {
            mLowAccuracy = accuracy < SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
            updateWarning();
        }
    };

    public class Client implements Runnable {
        @Override
        public void run() {
            try {

                // send message to Arduino
                InetAddress serverAddr = InetAddress.getByName(SERVERIP);
                DatagramSocket clientSocket = new DatagramSocket();
                byte[] sendData = new byte[1024];
                String sentence = message;
                sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddr, SERVERPORT);
                clientSocket.send(sendPacket);

                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateWarning()
    {
        String warning;
        if(mLowAccuracy)
        {
            warning = "Glass is detecting too much interference";
            azimuthDeg = 0;


        } else if(mTooSteep)
        {
            warning = "The pitch value is approaching gimbal lock";
        }
        else
        {
            warning = "Control OK";
        }
        mWarningText.setText(warning);
    }

}