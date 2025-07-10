package com.example.smartrestaurant

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smartrestaurant.databinding.ActivityDetailsBinding
import com.example.smartrestaurant.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailsBinding
    private var foodname: String? = null
    private var foodImage: String? = null
    private var foodPrice: String? = null
    private var foodIngredients: String? = null
    private var foodDescription: String? = null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize Firebase Auth
        auth= FirebaseAuth.getInstance()

        foodname = intent.getStringExtra("MenuItemName")
        foodDescription = intent.getStringExtra("MenuItemDescription")
        foodIngredients = intent.getStringExtra("MenuItemIngredients")
        foodPrice = intent.getStringExtra("MenuItemPrice")
        foodImage = intent.getStringExtra("MenuItemImage")

        with(binding){
            DetailfoodName.text=foodname
            descriptionEdittext.text =foodDescription
            ingredientEdittext.text= foodIngredients
            Picasso.get()
                .load(foodImage)
                .into(DetailfoodImage)
        }
        binding.imageButton2.setOnClickListener {
            finish()
        }
        binding.addToCartButton.setOnClickListener {
            aadItemToCart()
        }
    }

    private fun aadItemToCart() {
       val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid?:""
        //create a cartItems object
        val cartItem = CartItems(foodname.toString(),foodPrice.toString(),foodDescription.toString(),foodImage.toString(), foodQuantity = 1)
        //save data to cart item to firebase
        database.child("user").child(userId).child("CartItems").push().setValue(cartItem).addOnSuccessListener {
            Toast.makeText(this, "Items Added into cart SuccessFully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Item Is Not Added", Toast.LENGTH_SHORT).show()
        }
    }
}