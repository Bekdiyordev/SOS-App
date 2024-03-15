package uz.beko404.sosapp.ui

import android.app.Activity.RESULT_OK
import android.app.NotificationManager
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.provider.Settings
import android.view.View
import android.widget.Toast

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import uz.beko404.sosapp.R
import uz.beko404.sosapp.databinding.FragmentHomeBinding
import uz.beko404.sosapp.viewBinding

class HomeFragment : Fragment(R.layout.fragment_home), SensorEventListener {
    private val binding by viewBinding { FragmentHomeBinding.bind(it) }

    private lateinit var sensorManager: SensorManager
    private var lastUpdate: Long = 0
    private var last_x = 0f
    private var last_y = 0f
    private var last_z = 0f
    private val SHAKE_THRESHOLD = 500

    private lateinit var audioManager: AudioManager
    var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.beginTransaction().add(R.id.main_container, MapFragment()).commit()
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
        lastUpdate = System.currentTimeMillis()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdate > 100) {
            val diffTime = currentTime - lastUpdate
            lastUpdate = currentTime

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000

            if (speed > SHAKE_THRESHOLD) {
                // Device is shaken
                performShakeAction()
            }

            last_x = x
            last_y = y
            last_z = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }

    private fun performShakeAction() {
        // Vibrate the device
        val vibrator = requireActivity().getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(2000)

        // Show a toast
        Toast.makeText(requireContext(), "Device Shaken!", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setupUI()
    }

    private fun setupUI() = with(binding) {
        shortNumbers.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_numbersFragment)
        }

        settings.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        sos.setOnClickListener {
            checkNotificationPolicyAccess()
        }
    }

    private fun changeSoundMode() {
        when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            }

            AudioManager.RINGER_MODE_VIBRATE -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            }

            AudioManager.RINGER_MODE_NORMAL -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            }
        }

        playSound()
    }

    private fun playSound() {
        if (mediaPlayer == null || !mediaPlayer!!.isPlaying) {
            mediaPlayer = MediaPlayer.create(context, R.raw.sos_sound)
            mediaPlayer?.start()
        } else {
            mediaPlayer?.release()
            mediaPlayer = null
        }

        // Set maximum volume
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)

        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.release()
            Toast.makeText(requireContext(), "Finish", Toast.LENGTH_SHORT).show()
        }
    }

    private val REQUEST_NOTIFICATION_POLICY_ACCESS = 1001

    private fun checkNotificationPolicyAccess() {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        if (!notificationManager!!.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivityForResult(intent, REQUEST_NOTIFICATION_POLICY_ACCESS)
        } else {
            // Permission already granted, you can change DND mode here
            changeSoundMode()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_NOTIFICATION_POLICY_ACCESS) {
            if (resultCode == RESULT_OK) {
                // Permission granted, you can change DND mode here
                changeSoundMode()
            } else {
                // Permission denied, handle accordingly
            }
        }
    }

}