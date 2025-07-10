package com.example.smartrestaurant

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.smartrestaurant.databinding.ActivityStartactivityBinding

class startActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartactivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStartactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            val intent = Intent(this,SigninActivity::class.java)
            startActivity(intent)
        }
    }
}
