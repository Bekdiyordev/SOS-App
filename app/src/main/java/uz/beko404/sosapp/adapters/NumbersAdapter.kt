package uz.beko404.sosapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import uz.beko404.sosapp.databinding.ItemNumberBinding
import uz.beko404.sosapp.models.Number

class NumbersAdapter(private val call: (number: String) -> Unit) :
    RecyclerView.Adapter<NumbersAdapter.ViewHolder>() {
    private val dif = AsyncListDiffer(this, ITEM_DIFF)

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNumberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dif.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.setContent(position)

    inner class ViewHolder(private val binding: ItemNumberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
        fun setContent(position: Int) = with(binding) {
            val number = dif.currentList[position]
            name.text = number.name
            shortNumber.text = number.number
            image.setImageResource(number.image)
            item.setOnClickListener {
                call(number.number)
            }
        }
    }

    fun submitList(orderProduct: List<Number>) {
        dif.submitList(orderProduct)
    }

    companion object {
        private val ITEM_DIFF = object : DiffUtil.ItemCallback<Number>() {
            override fun areItemsTheSame(oldItem: Number, newItem: Number): Boolean =
                oldItem.name == newItem.name

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Number, newItem: Number): Boolean =
                oldItem == newItem
        }
    }
}