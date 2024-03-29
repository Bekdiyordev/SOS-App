package uz.beko404.sosapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import uz.beko404.sosapp.Pref
import uz.beko404.sosapp.R
import uz.beko404.sosapp.adapters.SMSNumbersAdapter
import uz.beko404.sosapp.databinding.FragmentAddSMSNumberBinding
import uz.beko404.sosapp.viewBinding

class AddSMSNumberFragment : Fragment(R.layout.fragment_add_s_m_s_number) {
    private val binding by viewBinding { FragmentAddSMSNumberBinding.bind(it) }
    private val adapter by lazy { SMSNumbersAdapter(::deleteNumber) }
    private val dataList = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        setupUI()
    }

    private fun loadData() {
        dataList.clear()
        if (Pref.smsNumber.isNotEmpty()) {
            dataList.addAll(Pref.smsNumber.split("$"))
            adapter.submitList(dataList)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupUI() = with(binding) {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        smsRecycler.adapter = adapter

        save.setOnClickListener {
            if (number.text!!.isNotEmpty() && number.unMaskedText?.length == 12) {
                if (Pref.smsNumber.isEmpty())
                    Pref.smsNumber = Pref.smsNumber.plus(number.unMaskedText)
                else
                    Pref.smsNumber = Pref.smsNumber.plus("$+").plus(number.unMaskedText)
                dataList.add("+" + number.unMaskedText.toString())
                adapter.submitList(dataList)
                adapter.notifyDataSetChanged()
                number.text!!.clear()
            }
        }
    }

    private fun deleteNumber(position: Int) {
        dataList.removeAt(position)
        adapter.notifyItemRemoved(position)
        Pref.smsNumber = getNumbersStr()
    }

    private fun getNumbersStr(): String {
        var string = ""
        if (dataList.isEmpty())
            return string
        dataList.forEach {
            string += it
            string += "$"
        }
        return string.substring(0, string.lastIndexOf("$"))
    }
}