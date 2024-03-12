package uz.beko404.sosapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import uz.beko404.sosapp.Pref
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

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        if (Pref.getNumber().isNotEmpty())
            number.setText(Pref.getNumber())

        save.setOnClickListener {
            if (number.text!!.isNotEmpty() && number.unMaskedText?.length == 12) {
                Pref.setNumber(number.text.toString())
                Toast.makeText(requireContext(), getString(R.string.success), Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else
                Toast.makeText(requireContext(), getString(R.string.enter_number), Toast.LENGTH_SHORT).show()
        }
    }
}