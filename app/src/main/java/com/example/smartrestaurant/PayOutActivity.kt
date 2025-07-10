package com.example.smartrestaurant

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.smartrestaurant.databinding.ActivityPayOutBinding
import com.example.smartrestaurant.model.CartItems
import com.example.smartrestaurant.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class PayOutActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: ActivityPayOutBinding

    private lateinit var Name: String
    private lateinit var Address: String
    private lateinit var Phone: String
    private lateinit var TotalAmount: String
    private lateinit var userId: String

    private lateinit var foodItemName: ArrayList<String>
    private lateinit var foodItemPrice: ArrayList<String>
    private lateinit var foodItemQuantity: ArrayList<Int>
    private lateinit var foodItemImage: ArrayList<String>
    private lateinit var foodItemDescription: ArrayList<String>
    private lateinit var foodItemIngredient: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        setUserData()

        // Get data from intent safely
        val intent: Intent = intent
        foodItemName = intent.getStringArrayListExtra("foodItemName") ?: arrayListOf()
        foodItemPrice = intent.getStringArrayListExtra("foodItemPrice") ?: arrayListOf()
        foodItemQuantity = intent.getIntegerArrayListExtra("foodItemQuantity") ?: arrayListOf()
        foodItemImage = intent.getStringArrayListExtra("foodItemImage") ?: arrayListOf()
        foodItemDescription = intent.getStringArrayListExtra("foodItemDescription") ?: arrayListOf()
        foodItemIngredient = intent.getStringArrayListExtra("foodItemIngredient") ?: arrayListOf()

        // Calculate and display total amount
        TotalAmount = "${calculateTotalAmount()} Rs"
        binding.TotalAmount.setText(TotalAmount)
        binding.TotalAmount.isEnabled = false

        // Place order action
        binding.placedorderbutton.setOnClickListener {
            Name = binding.Name.text.toString().trim()
            Address = binding.Address.text.toString().trim()
            Phone = binding.Phone.text.toString().trim()

            if (Name.isBlank() || Address.isBlank() || Phone.isBlank()) {
                Toast.makeText(this, "Please Enter All The Details", Toast.LENGTH_SHORT).show()
            } else {
                userId = auth.currentUser?.uid ?: ""
                val time: Long = System.currentTimeMillis()
                val itemPushKey = databaseReference.child("OrderDetails").push().key ?: return@setOnClickListener

                val orderDetails = OrderDetails(
                    userId = userId,
                    userName = Name,
                    address = Address,
                    totalPrice = TotalAmount,
                    itemPushKey = itemPushKey,
                    phoneNumber = Phone,
                    orderAccepted = true,
                    paymentReceived = true,
                    foodNames = foodItemName,
                    foodImages = foodItemImage,
                    foodPrices = foodItemPrice,
                    foodQuantities = foodItemQuantity,
                    currentTime = time
                )

                // Save order
                val orderRef = databaseReference.child("user").child(userId).child("BuyHistory").child(itemPushKey)

                // 1. Save order details (except food items)
                val orderDetailsMap = mapOf(
                    "address" to Address,
                    "currentTime" to time,
                    "itemPushKey" to itemPushKey,
                    "orderAccepted" to true,
                    "paymentReceived" to true,
                    "phoneNumber" to Phone,
                    "totalPrice" to TotalAmount,
                    "userId" to userId,
                    "userName" to Name
                )
                orderRef.setValue(orderDetailsMap)
                    .addOnSuccessListener {
                        // 2. Save each food item under CartItems
                        val cartItemsRef = orderRef.child("CartItems")
                        for (i in foodItemName.indices) {
                            val cartItem = CartItems(
                                foodname = foodItemName[i],
                                foodPrice = foodItemPrice[i],
                                foodDescription = foodItemDescription[i],
                                foodImage = foodItemImage[i],
                                foodQuantity = foodItemQuantity[i],
                                foodIngredients = foodItemIngredient[i]
                            )
                            cartItemsRef.push().setValue(cartItem)
                        }

                        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()

                        val bottomSheetDialog = CongratsBottomSheet()
                        bottomSheetDialog.show(supportFragmentManager, "Test")

                        removeItemFromCart()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed To Order", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
    private fun removeItemFromCart() {
        val cartItemReference = databaseReference.child("user").child(userId).child("CartItems")
        cartItemReference.removeValue()
    }

    private fun calculateTotalAmount(): Int {
        var totalAmount = 0
        for (i in foodItemPrice.indices) {
            val priceString = foodItemPrice[i]
            val numericPrice = priceString.filter { it.isDigit() }.toIntOrNull() ?: 0
            val quantity = foodItemQuantity[i]
            totalAmount += numericPrice * quantity
        }
        return totalAmount
    }

    private fun setUserData() {
        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            val userId: String = user.uid
            val userReference = databaseReference.child("user").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Name = snapshot.child("name").getValue(String::class.java) ?: ""
                        Address = snapshot.child("address").getValue(String::class.java) ?: ""
                        Phone = snapshot.child("phone").getValue(String::class.java) ?: ""

                        binding.Name.setText(Name)
                        binding.Address.setText(Address)
                        binding.Phone.setText(Phone)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PayOutActivity, "Failed to load user data.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
