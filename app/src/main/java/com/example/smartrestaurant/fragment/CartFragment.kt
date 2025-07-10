package com.example.smartrestaurant.fragment

import VerticalSpaceItemDecoration
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartrestaurant.Adapter.CartAdapter
import com.example.smartrestaurant.PayOutActivity
import com.example.smartrestaurant.databinding.FragmentCartBinding
import com.example.smartrestaurant.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var foodNames: MutableList<String>
    private lateinit var foodPrices: MutableList<String>
    private lateinit var foodDescriptions: MutableList<String>
    private lateinit var foodImages: MutableList<String>
    private lateinit var foodIngredients: MutableList<String>
    private lateinit var quantity: MutableList<Int>
    private lateinit var cartAdapter: CartAdapter
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        binding.proceedButton.setOnClickListener {
            getOrderItemsDetail()
        }

        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecyclerView.addItemDecoration(VerticalSpaceItemDecoration(16))

        retrieveCartItems()

        return binding.root
    }

    private fun retrieveCartItems() {
        val foodReference: DatabaseReference =
            database.reference.child("user").child(userId).child("CartItems")

        foodNames = mutableListOf()
        foodPrices = mutableListOf()
        foodDescriptions = mutableListOf()
        foodIngredients = mutableListOf()
        foodImages = mutableListOf()
        quantity = mutableListOf()

        foodReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                foodNames.clear()
                foodPrices.clear()
                foodDescriptions.clear()
                foodImages.clear()
                foodIngredients.clear()
                quantity.clear()

                for (foodSnapshot in snapshot.children) {
                    val cartItem = foodSnapshot.getValue(CartItems::class.java)
                    cartItem?.let {
                        foodNames.add(it.foodname ?: "")
                        foodPrices.add(it.foodPrice ?: "")
                        foodDescriptions.add(it.foodDescription ?: "")
                        foodImages.add(it.foodImage ?: "")
                        foodIngredients.add(it.foodIngredients ?: "")
                        quantity.add(it.foodQuantity ?: 1)
                    }
                }

                cartAdapter = CartAdapter(
                    foodNames,
                    foodPrices,
                    foodImages,
                    foodDescriptions,
                    foodIngredients,
                    quantity,
                    requireContext()
                )
                binding.cartRecyclerView.adapter = cartAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch cart data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getOrderItemsDetail() {
        val cartRef = database.reference.child("user").child(userId).child("CartItems")

        val foodName = mutableListOf<String>()
        val foodNPrice = mutableListOf<String>()
        val foodImage = mutableListOf<String>()
        val foodDescription = mutableListOf<String>()
        val foodIngredient = mutableListOf<String>()

        val foodQuantities = cartAdapter.getUpdatedItemsQuantities()

        cartRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for ((index, foodSnapshot) in snapshot.children.withIndex()) {
                    val cartItem = foodSnapshot.getValue(CartItems::class.java)
                    cartItem?.let {
                        foodName.add(it.foodname ?: "")
                        foodNPrice.add(it.foodPrice ?: "")
                        foodImage.add(it.foodImage ?: "")
                        foodDescription.add(it.foodDescription ?: "")
                        foodIngredient.add(it.foodIngredients ?: "")

                        // ✅ Save to History
                        val historyRef = database.reference
                            .child("user")
                            .child(userId)
                            .child("History")

                        val historyItemId = historyRef.push().key!!
                        val historyItem = mapOf(
                            "name" to it.foodname,
                            "price" to it.foodPrice,
                            "image" to it.foodImage
                        )

                        historyRef.child(historyItemId).setValue(historyItem)
                    }
                }

                orderNow(
                    foodName,
                    foodDescription,
                    foodIngredient,
                    foodQuantities,
                    foodNPrice,
                    foodImage
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Order Failed. Try Again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun orderNow(
        foodName: MutableList<String>,
        foodDescription: MutableList<String>,
        foodIngredient: MutableList<String>,
        foodQuantities: MutableList<Int>,
        foodNPrice: MutableList<String>,
        foodImage: MutableList<String>
    ) {
        if (isAdded && context != null) {
            val intent = Intent(requireContext(), PayOutActivity::class.java)
            intent.putExtra("foodItemName", ArrayList(foodName))
            intent.putExtra("foodItemPrice", ArrayList(foodNPrice))
            intent.putExtra("foodItemImage", ArrayList(foodImage))
            intent.putExtra("foodItemDescription", ArrayList(foodDescription))
            intent.putExtra("foodItemIngredient", ArrayList(foodIngredient))
            intent.putExtra("foodItemQuantity", ArrayList(foodQuantities))
            startActivity(intent)

            // ✅ Optional: Clear cart after placing order
            val cartRef = database.reference.child("user").child(userId).child("CartItems")
            cartRef.removeValue()
        }
    }
}
