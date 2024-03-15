package uz.beko404.sosapp.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.navigation.fragment.findNavController
import uz.beko404.sosapp.R
import uz.beko404.sosapp.adapters.NumbersAdapter
import uz.beko404.sosapp.databinding.FragmentNumbersBinding
import uz.beko404.sosapp.viewBinding
import uz.beko404.sosapp.models.Number

class NumbersFragment : Fragment(R.layout.fragment_numbers) {
    private val binding by viewBinding { FragmentNumbersBinding.bind(it) }
    private val adapter by lazy { NumbersAdapter(::call) }
    private val dataList = mutableListOf<Number>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        setupUI()
    }

    private fun loadData() {
        dataList.clear()
        dataList.add(Number("Yong'in xavfsizlik xizmati", "101", R.drawable.im_101))
        dataList.add(Number("Ichki ishlar organi", "102", R.drawable.im_102))
        dataList.add(Number("Tez tibbiy yordam", "103", R.drawable.im_103))
        dataList.add(Number("Favqulotda gaz xizmati", "104", R.drawable.im_104))
        dataList.add(Number("Qutqaruv xizmati", "1050", R.drawable.im_1050))
        adapter.submitList(dataList)
    }

    private fun setupUI() = with(binding) {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        recycler.adapter = adapter
    }

    private fun call(number: String) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_CALL_PERMISSION
            )
        } else {
            makePhoneCall(number)
        }
    }

    private fun makePhoneCall(number: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$number")
        startActivity(callIntent)
    }

    companion object {
        private const val REQUEST_CALL_PERMISSION = 1
    }
}