package uz.beko404.sosapp.ui

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.fragment.findNavController
import uz.beko404.sosapp.R
import uz.beko404.sosapp.databinding.FragmentHomeBinding
import uz.beko404.sosapp.viewBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding by viewBinding { FragmentHomeBinding.bind(it) }

    private lateinit var audioManager: AudioManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        startActivity(intent)
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
            changeSoundMode()
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
            }

            AudioManager.RINGER_MODE_NORMAL -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            }
        }
    }
}