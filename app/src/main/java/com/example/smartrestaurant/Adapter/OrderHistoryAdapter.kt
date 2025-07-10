package com.example.smartrestaurant.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartrestaurant.DetailsActivity
import com.example.smartrestaurant.R
import com.example.smartrestaurant.databinding.BuyagainitemBinding
import com.example.smartrestaurant.model.OrderDetails
import com.squareup.picasso.Picasso

class OrderHistoryAdapter(
    private val context: Context
) : RecyclerView.Adapter<OrderHistoryAdapter.BuyAgainViewHolder>() {

    private val allItemsList = mutableListOf<Triple<String, String, String>>() // name, price, image

    fun setData(orderList: List<OrderDetails>) {
        allItemsList.clear()
        orderList.forEach { order ->
            val names = order.foodNames ?: emptyList()
            val prices = order.foodPrices ?: emptyList()
            val images = order.foodImages ?: emptyList()
            for (i in names.indices) {
                val name = names.getOrNull(i) ?: ""
                val price = prices.getOrNull(i) ?: ""
                val image = images.getOrNull(i) ?: ""
                allItemsList.add(Triple(name, price, image))
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyAgainViewHolder {
        val binding = BuyagainitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BuyAgainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BuyAgainViewHolder, position: Int) {
        holder.bind(allItemsList[position])
    }

    override fun getItemCount(): Int = allItemsList.size

    inner class BuyAgainViewHolder(private val binding: BuyagainitemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Triple<String, String, String>) {
            binding.buyAgainFoodName.text = item.first
            binding.buyAgainFoodPrice.text = item.second

            Picasso.get()
                .load(item.third)
                .placeholder(R.drawable.img_1)
                .into(binding.buyAgainFoodImage)

            binding.root.setOnClickListener {
                val intent = Intent(context, DetailsActivity::class.java).apply {
                    putExtra("MenuItemName", item.first)
                    putExtra("MenuItemPrice", item.second)
                    putExtra("MenuItemImage", item.third)
                }
                context.startActivity(intent)
            }
        }
    }
}
