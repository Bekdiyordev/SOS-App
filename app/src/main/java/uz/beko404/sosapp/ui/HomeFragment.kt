package uz.beko404.sosapp.ui

import android.app.Activity.RESULT_OK
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import uz.beko404.sosapp.R
import uz.beko404.sosapp.databinding.FragmentHomeBinding
import uz.beko404.sosapp.viewBinding


class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding by viewBinding { FragmentHomeBinding.bind(it) }

    private lateinit var audioManager: AudioManager
    var mediaPlayer: MediaPlayer? = null


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
//        val audioAttributes = AudioAttributes.Builder()
//            .setUsage(AudioAttributes.USAGE_MEDIA)
//            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//            .build()
//        mediaPlayer?.setAudioAttributes(audioAttributes)

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