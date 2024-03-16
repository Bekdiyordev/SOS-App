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

        time.setText(Pref.time.toString())
        distance.setText(Pref.distance.toString())

        save.setOnClickListener {
            if (time.text!!.isNotEmpty() && distance.text!!.isNotEmpty()) {
                Pref.time = time.text.toString().toLong()
                Pref.distance = distance.text.toString().toInt()
                findNavController().navigateUp()
            } else
                Toast.makeText(requireContext(), getString(R.string.enter_number), Toast.LENGTH_SHORT).show()
        }
    }
}