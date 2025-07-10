package com.example.smartrestaurant

import android.os.Bundle
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smartrestaurant.databinding.ActivityChooselocationBinding

class ChooselocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChooselocationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChooselocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val locationlist: Array<String> = arrayOf("faridabad","delhi","mumbai","banglore","lucknow")
        val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,locationlist)
        val autoCompleteTextView: AutoCompleteTextView = binding.listOfLocation
        autoCompleteTextView.setAdapter(adapter)
    }
}