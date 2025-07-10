package com.example.smartrestaurant.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartrestaurant.DetailsActivity
import com.example.smartrestaurant.databinding.PopularitemBinding

class Popularitem(
    private val items: List<String>,
    private val images: List<Int>,
    private val prices: List<String>,
    private val context: Context // Pass context from the fragment/activity
) : RecyclerView.Adapter<Popularitem.PopularViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        val binding = PopularitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PopularViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        holder.bind(items[position], prices[position], images[position])
    }

    override fun getItemCount(): Int = items.size

    class PopularViewHolder(private val binding: PopularitemBinding, private val context: Context) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String, price: String, image: Int) {
            binding.foodNamePopular.text = item
            binding.pricePopular.text = price
            binding.imageView5.setImageResource(image)

            // Set Click Listener to open DetailsActivity
            binding.root.setOnClickListener {
                val intent = Intent(context, DetailsActivity::class.java)
                intent.putExtra("menuItemsName", item)
                intent.putExtra("MenuImage", image)
                context.startActivity(intent)
            }
        }
    }
}
