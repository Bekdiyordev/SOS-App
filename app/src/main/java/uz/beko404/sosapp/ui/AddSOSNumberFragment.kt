package uz.beko404.sosapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import uz.beko404.sosapp.R
import uz.beko404.sosapp.databinding.FragmentAddSOSNumberBinding
import uz.beko404.sosapp.viewBinding

class AddSOSNumberFragment : Fragment(R.layout.fragment_add_s_o_s_number) {
    private val binding by viewBinding { FragmentAddSOSNumberBinding.bind(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() = with(binding) {

    }
}