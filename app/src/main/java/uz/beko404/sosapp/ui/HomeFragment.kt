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
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.provider.Settings
import android.telephony.SmsManager
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import uz.beko404.sosapp.Pref
import uz.beko404.sosapp.R
import uz.beko404.sosapp.WarningDialog
import uz.beko404.sosapp.databinding.FragmentHomeBinding
import uz.beko404.sosapp.viewBinding
import kotlin.math.abs

@Suppress("DEPRECATION")
class HomeFragment : Fragment(R.layout.fragment_home), SensorEventListener {
    private val binding by viewBinding { FragmentHomeBinding.bind(it) }

    private lateinit var sensorManager: SensorManager
    private var lastUpdate: Long = 0
    private var last_x = 0f
    private var last_y = 0f
    private var last_z = 0f
    private val SHAKE_THRESHOLD = 300
    private var lastBackPressedTime = 0L
    private var isFirst = true
    private var isShowing = false

    private lateinit var audioManager: AudioManager
    var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
        lastUpdate = System.currentTimeMillis()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressedTime < 2000) {
                requireActivity().finishAffinity()
            } else {
                lastBackPressedTime = currentTime
                Toast.makeText(requireContext(), getString(R.string.exit_app), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdate > 100) {
            val diffTime = currentTime - lastUpdate
            lastUpdate = currentTime

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000

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
        mediaPlayer?.stop()
        if (isFirst)
            isFirst = false
        else {
            val vibrator = requireActivity().getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(1000)
            Toast.makeText(requireContext(), "Diqqat Zilzila. Ehtiyot bo'ling", Toast.LENGTH_SHORT)
                .show()
            mediaPlayer?.pause()
            playSound()
        }
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
        childFragmentManager.beginTransaction().add(R.id.main_container, MapFragment()).commit()
        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setupUI()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    private fun setupUI() = with(binding) {
        shortNumbers.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_numbersFragment)
        }

        settings.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        sos.setOnClickListener {
            if (!Pref.isFind) {
                Pref.isFind = true
                mediaPlayer?.pause()
                playSound()
            }
            sendSMS()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                checkNotificationPolicyAccess()
            else
                Toast.makeText(
                    requireContext(),
                    "Ushbu funksiya bu qurilmada ishlamaydi",
                    Toast.LENGTH_SHORT
                ).show()
        }
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
            if (Pref.smsNumber.isNotEmpty()) {
                Pref.smsNumber.split("$").forEach {
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
        showMessage()
        mediaPlayer = MediaPlayer.create(context, R.raw.sos_sound)
        mediaPlayer?.start()

        // Set maximum volume
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, 0)

        mediaPlayer?.setOnCompletionListener {
            Pref.isFind = false
            mediaPlayer?.release()
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

    @Deprecated("Deprecated in Java")
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

    private fun showMessage() {
        if (!isShowing) {
            isShowing = true
            val dialog = WarningDialog.newInstance()
            dialog.onClickListener = {
                dialog.dismiss()
                mediaPlayer?.release()
                Pref.isFind = false
                isShowing = false
            }
            dialog.show(parentFragmentManager, "error_dialog")
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_SEND_SMS = 123
    }
}