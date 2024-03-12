package uz.beko404.sosapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import uz.beko404.sosapp.R
import uz.beko404.sosapp.databinding.FragmentSettingsBinding
import uz.beko404.sosapp.viewBinding


class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private val binding by viewBinding { FragmentSettingsBinding.bind(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() = with(binding) {

    }
}