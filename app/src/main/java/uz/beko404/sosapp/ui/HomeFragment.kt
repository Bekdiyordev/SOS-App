package uz.beko404.sosapp.ui

import android.app.Activity.RESULT_OK
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import uz.beko404.sosapp.R
import uz.beko404.sosapp.databinding.FragmentHomeBinding
import uz.beko404.sosapp.viewBinding


class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding by viewBinding { FragmentHomeBinding.bind(it) }

    private lateinit var audioManager: AudioManager
    private val PERMISSION_REQUEST_CODE = 123


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
//        startActivity(intent)
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
//                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            }

            AudioManager.RINGER_MODE_VIBRATE -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }

//            AudioManager.RINGER_MODE_NORMAL -> {
//                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
//            }
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