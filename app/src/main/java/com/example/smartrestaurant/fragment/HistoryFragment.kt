package com.example.smartrestaurant.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartrestaurant.Adapter.OrderHistoryAdapter
import com.example.smartrestaurant.R
import com.example.smartrestaurant.databinding.FragmentHistoryBinding
import com.example.smartrestaurant.model.OrderDetails
import com.example.smartrestaurant.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var buyAgainAdapter: OrderHistoryAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userId: String

    private val recentOrders = ArrayList<OrderDetails>()
    private val previousOrders = ArrayList<OrderDetails>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        setupRecyclerView()
        fetchOrderHistory()

        return binding.root
    }

    private fun setupRecyclerView() {
        buyAgainAdapter = OrderHistoryAdapter(requireContext())

        binding.historyRecyleView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = buyAgainAdapter
        }
    }

    private fun fetchOrderHistory() {
        val historyReference = database.reference.child("user").child(userId).child("BuyHistory")

        historyReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recentOrders.clear()
                previousOrders.clear()

                if (!snapshot.exists()) {
                    updateUI()
                    return
                }

                val allOrders = mutableListOf<OrderDetails>()

                for (orderSnapshot in snapshot.children) {
                    val cartItemsSnapshot = orderSnapshot.child("CartItems")
                    if (cartItemsSnapshot.exists()) {
                        val order = OrderDetails()

                        val foodNames = mutableListOf<String>()
                        val foodPrices = mutableListOf<String>()
                        val foodImages = mutableListOf<String>()
                        val foodQuantities = mutableListOf<Int>()

                        for (itemSnapshot in cartItemsSnapshot.children) {
                            val item = itemSnapshot.getValue(CartItems::class.java)
                            item?.let {
                                foodNames.add(it.foodname ?: "")
                                foodPrices.add(it.foodPrice ?: "")
                                foodImages.add(it.foodImage ?: "")
                                foodQuantities.add(it.foodQuantity ?: 1)
                            }
                        }

                        order.foodNames = foodNames
                        order.foodPrices = foodPrices
                        order.foodImages = foodImages
                        order.foodQuantities = foodQuantities
                        order.orderAccepted = true
                        order.paymentReceived = true
                        order.currentTime = orderSnapshot.child("currentTime").getValue(Long::class.java) ?: System.currentTimeMillis()

                        allOrders.add(order)
                    }
                }

                // Sort orders by time (most recent first)
                allOrders.sortByDescending { it.currentTime }

                // Split into recent and previous orders
                if (allOrders.isNotEmpty()) {
                    recentOrders.add(allOrders[0])
                    previousOrders.addAll(allOrders.subList(1, allOrders.size))
                }

                updateUI()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HistoryFragment", "Error fetching data: ${error.message}")
                Toast.makeText(requireContext(), "Failed to load order history.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI() {
        // Update recent orders section
        if (recentOrders.isNotEmpty()) {
            binding.cardViewRecentOrder.visibility = View.VISIBLE
            val recentOrder = recentOrders[0]
            
            // Show first item from recent order
            binding.buyAgainFoodName.text = recentOrder.foodNames?.getOrNull(0) ?: "No items"
            binding.buyAgainFoodPrice.text = recentOrder.foodPrices?.getOrNull(0) ?: "0"

            recentOrder.foodImages?.getOrNull(0)?.let { imageUrl ->
                if (imageUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.menu7)
                        .error(R.drawable.menu7)
                        .into(binding.buyAgainFoodImage)
                } else {
                    binding.buyAgainFoodImage.setImageResource(R.drawable.menu7)
                }
            } ?: run {
                binding.buyAgainFoodImage.setImageResource(R.drawable.menu7)
            }

            binding.buyAgainButton.setOnClickListener {
                reorderItems(recentOrder)
            }
        } else {
            binding.cardViewRecentOrder.visibility = View.GONE
        }

        // Update previous orders RecyclerView
        if (previousOrders.isNotEmpty()) {
            binding.textView23.visibility = View.VISIBLE
            binding.historyRecyleView.visibility = View.VISIBLE
            buyAgainAdapter.setData(previousOrders)
        } else {
            binding.textView23.visibility = View.GONE
            binding.historyRecyleView.visibility = View.GONE
        }
    }

    private fun reorderItems(order: OrderDetails) {
        val cartReference = database.reference.child("user").child(userId).child("CartItems")

        order.foodNames?.forEachIndexed { index, name ->
            val cartItem = CartItems(
                foodname = name,
                foodPrice = order.foodPrices?.getOrNull(index) ?: "",
                foodDescription = "",
                foodImage = order.foodImages?.getOrNull(index) ?: "",
                foodQuantity = order.foodQuantities?.getOrNull(index) ?: 1
            )

            cartReference.push().setValue(cartItem)
        }
    }
}
