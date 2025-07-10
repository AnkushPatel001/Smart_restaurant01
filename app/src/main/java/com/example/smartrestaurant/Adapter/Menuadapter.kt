package com.example.smartrestaurant.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartrestaurant.DetailsActivity
import com.example.smartrestaurant.databinding.MenuitemBinding
import com.example.smartrestaurant.model.MenuItem
import com.squareup.picasso.Picasso

class MenuAdapter(
    private val menuItems: List<MenuItem>,
    private val requireContext: Context
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = MenuitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(menuItems[position])
    }

    override fun getItemCount(): Int = menuItems.size

    inner class MenuViewHolder(private val binding: MenuitemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(menuItem: MenuItem) {
            binding.apply {
                menuFoodName.text = menuItem.foodName ?: "N/A"
                menuitemprice.text = menuItem.foodPrice ?: "N/A"

                // Load image from URL using Picasso
                Picasso.get()
                    .load(menuItem.foodImage)
                    .into(menuimage)
            }

            // OnClickListener to open DetailsActivity
            binding.root.setOnClickListener {
                openDetailsActivity(menuItem)
            }
        }
    }

    private fun openDetailsActivity(menuItem: MenuItem) {
        val intent = Intent(requireContext, DetailsActivity::class.java).apply {
            putExtra("MenuItemName", menuItem.foodName)
            putExtra("MenuItemPrice", menuItem.foodPrice)
            putExtra("MenuItemImage", menuItem.foodImage)
            putExtra("MenuItemDescription", menuItem.foodDescription)
            putExtra("MenuItemIngredients", menuItem.foodIngredients)
        }
        requireContext.startActivity(intent)
    }
}
