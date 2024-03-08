package uz.beko404.sosapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import uz.beko404.sosapp.R
import uz.beko404.sosapp.databinding.FragmentNumbersBinding
import uz.beko404.sosapp.viewBinding

class NumbersFragment : Fragment(R.layout.fragment_numbers) {
    private val binding by viewBinding { FragmentNumbersBinding.bind(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() = with(binding){

    }
}