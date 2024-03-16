package uz.beko404.sosapp.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.NotificationManager
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.provider.Settings
import android.telephony.SmsManager
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import uz.beko404.sosapp.Pref
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
        call()

        // Show a toast
        Toast.makeText(requireContext(), "Diqqat Zilzila. Ehtiyot bo'ling", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer != null)
            mediaPlayer?.release()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
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
            playSound()
//            sendSMS()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                checkNotificationPolicyAccess()
            else
                Toast.makeText(requireContext(), "Ushbu funksiya bu qurilmada ishlamaydi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun call() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CALL_PHONE),
                PERMISSION_REQUEST_CALL
            )
        } else {
            makePhoneCall(Pref.getNumber())
        }
    }

    private fun makePhoneCall(number: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$number")
        startActivity(callIntent)
    }

    private fun sendSMS() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission granted, send SMS
            val messageToSend =
                "Men favqulotda holatga tushub qoldim. Iltimos sudlik bilan menga aloqaga chiqing yoki qutqaruv xizmatiga xabar bering."
            if (Pref.getSMSNumber().isNotEmpty()) {
                Pref.getSMSNumber().split("$").forEach {
                    SmsManager.getDefault().sendTextMessage(it, null, messageToSend, null, null)
                }
                Toast.makeText(requireContext(), "SMS Yuborildi", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Request SMS permissions
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.SEND_SMS),
                PERMISSION_REQUEST_SEND_SMS
            )
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

    companion object {
        private const val PERMISSION_REQUEST_SEND_SMS = 123
        private const val PERMISSION_REQUEST_CALL = 1234
    }
}