package com.example.smartrestaurant.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.smartrestaurant.databinding.CartitemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class CartAdapter(
    private val cartItems: MutableList<String>,
    private val cartItemPrices: MutableList<String>,
    private val cartImages: MutableList<String>,
    private var cartDescriptions: MutableList<String>,
    private val cartIngredient:MutableList<String>,
    private val cartQuantity: MutableList<Int>,
    private val context: Context

) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    init {
        itemQuantities = cartQuantity.toIntArray()
        val database = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        cartItemsReference = database.reference.child("user").child(userId).child("CartItems")
    }

    companion object {
        private var itemQuantities: IntArray = intArrayOf()
        private lateinit var cartItemsReference: DatabaseReference
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CartViewHolder(private val binding: CartitemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.apply {
                val quantity = itemQuantities[position]
                CartFoodName.text = cartItems[position]
                cartitemprice.text = cartItemPrices[position]
                Picasso.get()
                    .load(cartImages[position])
                    .into(cartimage)
                quantitytext.text = quantity.toString()

                minusbutton.setOnClickListener {
                    decreaseQuantity(position)
                }
                plusbutton.setOnClickListener {
                    increaseQuantity(position)
                }
                deletebutton.setOnClickListener {
                    val itemPosition = adapterPosition
                    if (itemPosition != RecyclerView.NO_POSITION) {
                        deleteItem(itemPosition)
                    }
                }
            }
        }
    }

    private fun increaseQuantity(position: Int) {
        if (itemQuantities[position] < 10) {
            itemQuantities[position]++
            notifyItemChanged(position)
        }
    }

    private fun decreaseQuantity(position: Int) {
        if (itemQuantities[position] > 1) {
            itemQuantities[position]--
            notifyItemChanged(position)
        }
    }

    private fun deleteItem(position: Int) {
        val positionRetrieve: Int = position
        getUniqueAtPosition(positionRetrieve) { uniqueKey ->
            if (uniqueKey != null) {
                removeItem(position, uniqueKey)
            } else {
                Toast.makeText(context, "Failed to find item key", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeItem(position: Int, uniqueKey: String) {
        cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {
            cartItems.removeAt(position)
            cartImages.removeAt(position)
            cartDescriptions.removeAt(position)
            cartQuantity.removeAt(position)
            cartItemPrices.removeAt(position)
            cartIngredient.removeAt(position)

            itemQuantities = itemQuantities.filterIndexed { index, _ -> index != position }.toIntArray()

            Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()

            notifyItemRemoved(position)
            notifyItemRangeChanged(position, cartItems.size)
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUniqueAtPosition(positionRetrieve: Int, onComplete: (String?) -> Unit) {
        cartItemsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var uniqueKey: String? = null
                snapshot.children.forEachIndexed { index, dataSnapshot ->
                    if (index == positionRetrieve) {
                        uniqueKey = dataSnapshot.key
                        return@forEachIndexed
                    }
                }
                onComplete(uniqueKey)
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(null)
            }
        })
    }
    fun getUpdatedItemsQuantities(): MutableList<Int> {
        val itemQuantityList = itemQuantities.toMutableList()
        return itemQuantityList
    }
    }

