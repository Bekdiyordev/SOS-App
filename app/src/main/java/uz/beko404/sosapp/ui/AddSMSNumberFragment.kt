package uz.beko404.sosapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import uz.beko404.sosapp.R
import uz.beko404.sosapp.databinding.FragmentAddSMSNumberBinding
import uz.beko404.sosapp.viewBinding

class AddSMSNumberFragment : Fragment(R.layout.fragment_add_s_m_s_number) {
    private val binding by viewBinding { FragmentAddSMSNumberBinding.bind(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() = with(binding) {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }


    }
}