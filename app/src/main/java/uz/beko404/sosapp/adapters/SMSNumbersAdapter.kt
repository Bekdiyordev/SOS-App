package uz.beko404.sosapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import uz.beko404.sosapp.databinding.ItemNumberBinding
import uz.beko404.sosapp.databinding.ItemSmsBinding
import uz.beko404.sosapp.models.Number

class SMSNumbersAdapter(private val delete: (position: Int) -> Unit) :
    RecyclerView.Adapter<SMSNumbersAdapter.ViewHolder>() {
    private val dif = AsyncListDiffer(this, ITEM_DIFF)

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSmsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dif.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.setContent(position)

    inner class ViewHolder(private val binding: ItemSmsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
        fun setContent(position: Int) = with(binding) {
            val data = dif.currentList[position]
            number.text = data

            delete.setOnClickListener {
                delete(position)
            }
        }
    }

    fun submitList(numbers: List<String>) {
        dif.submitList(numbers)
    }

    companion object {
        private val ITEM_DIFF = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem.length == newItem.length

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }
    }
}