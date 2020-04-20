package dk.birkb85.ceilingledclient.models

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Compass.
 *
 * Based on:
 * https://developer.android.com/guide/topics/sensors/sensors_position
 *
 * Maybe also read:
 * https://stackoverflow.com/questions/8315913/how-to-get-direction-in-android-such-as-north-west
 * https://web.archive.org/web/20180501005730/http://www.codingforandroid.com/2011/01/using-orientation-sensors-simple.html
 *
 * Remember to implement at least:
 * onResume()
 * onPause()
 */
class Compass(context: Context?) : SensorEventListener {
    private var mSensorManager: SensorManager? =
        context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val mAccelerometerReading = FloatArray(3)
    private val mMagnetometerReading = FloatArray(3)

    private val mRotationMatrix = FloatArray(9)
    private val mOrientationAngles = FloatArray(3)

    private var mUpdateListener: UpdateListener? = null

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    fun onResume(updateListener: UpdateListener?) {
        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).also { accelerometer ->
            mSensorManager?.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI,//SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI//SENSOR_DELAY_UI
            )
        }
        mSensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD).also { magneticField ->
            mSensorManager?.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_UI,//SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI//SENSOR_DELAY_UI
            )
        }

        mUpdateListener = updateListener
    }

    fun onPause() {
        // Don't receive any more updates from either sensor.
        mSensorManager?.unregisterListener(this)

        mUpdateListener = null
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(it.values, 0, mAccelerometerReading, 0, mAccelerometerReading.size)
            } else if (it.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(it.values, 0, mMagnetometerReading, 0, mMagnetometerReading.size)
            }
        }

        updateOrientationAngles()
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    private fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            mRotationMatrix,
            null,
            mAccelerometerReading,
            mMagnetometerReading
        )

        // "mRotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles)

        // "mOrientationAngles" now has up-to-date information.

//        Log.d(
//            "DEBUG",
//            "Azimuth: ${mOrientationAngles[0]}, Pitch: ${mOrientationAngles[1]}, Roll: ${mOrientationAngles[2]}"
//        )

//        val azimuth = mOrientationAngles[0] * 360 / (2 * 3.14159f)
//        val pitch = mOrientationAngles[1] * 360 / (2 * 3.14159f)
//        val roll = mOrientationAngles[2] * 360 / (2 * 3.14159f)
//
//        mUpdateListener?.onUpdate(
//            azimuth,
//            pitch,
//            roll
//        )

        mUpdateListener?.onUpdate(
            mOrientationAngles[0],
            mOrientationAngles[1],
            mOrientationAngles[2]
        )
    }

    /**
     * Listen for status updates.
     *
     * Azimuth (degrees of rotation about the -z axis).
     * This is the angle between the device's current compass direction and magnetic north.
     * If the top edge of the device faces magnetic north, the azimuth is 0 degrees;
     * if the top edge faces south, the azimuth is 180 degrees.
     * Similarly, if the top edge faces east, the azimuth is 90 degrees, and if the top edge faces west, the azimuth is 270 degrees.
     *
     * Pitch (degrees of rotation about the x axis).
     * This is the angle between a plane parallel to the device's screen and a plane parallel to the ground.
     * If you hold the device parallel to the ground with the bottom edge closest to you and tilt the top edge of the device toward the ground, the pitch angle becomes positive.
     * Tilting in the opposite direction— moving the top edge of the device away from the ground—causes the pitch angle to become negative.
     * The range of values is -180 degrees to 180 degrees.
     *
     * Roll (degrees of rotation about the y axis).
     * This is the angle between a plane perpendicular to the device's screen and a plane perpendicular to the ground.
     * If you hold the device parallel to the ground with the bottom edge closest to you and tilt the left edge of the device toward the ground, the roll angle becomes positive.
     * Tilting in the opposite direction—moving the right edge of the device toward the ground— causes the roll angle to become negative.
     * The range of values is -90 degrees to 90 degrees.
     */
    interface UpdateListener {
        fun onUpdate(azimuth: Float, pitch: Float, roll: Float)
    }
}