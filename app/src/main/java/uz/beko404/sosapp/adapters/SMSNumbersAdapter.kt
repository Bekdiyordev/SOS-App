package uz.beko404.sosapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import uz.beko404.sosapp.databinding.ItemSmsNumberBinding

class SMSNumbersAdapter(private val delete: (position: Int) -> Unit) :
    RecyclerView.Adapter<SMSNumbersAdapter.ViewHolder>() {
    private val dif = AsyncListDiffer(this, ITEM_DIFF)

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSmsNumberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dif.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.setContent(position)

    inner class ViewHolder(private val binding: ItemSmsNumberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
        fun setContent(position: Int) = with(binding) {
            numberText.text = dif.currentList[position]
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
                oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }
    }
}