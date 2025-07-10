package com.example.smartrestaurant.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.smartrestaurant.Adapter.MenuAdapter
import com.example.smartrestaurant.R
import com.example.smartrestaurant.databinding.FragmentHomeBinding
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var menuItems: MutableList<com.example.smartrestaurant.model.MenuItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.viewmenu.setOnClickListener {
            val bottomSheetDialog = MenuBottomSheetFragment()
            bottomSheetDialog.show(parentFragmentManager, "test")
        }

        retrieveAndDisplayPopularItems()
        return binding.root
    }

    private fun retrieveAndDisplayPopularItems() {
        database = FirebaseDatabase.getInstance()
        val foodRef: DatabaseReference = database.reference.child("menu")
        menuItems = mutableListOf()

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(com.example.smartrestaurant.model.MenuItem::class.java)
                    menuItem?.let { menuItems.add(it) }
                }
                randomPopularItems()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun randomPopularItems() {
        val index = menuItems.indices.shuffled()
        val numItemsToShow = 6
        val subsetMenuItems = index.take(numItemsToShow).map { menuItems[it] }
        setPopularItemsAdapter(subsetMenuItems)
    }

    private fun setPopularItemsAdapter(subsetMenuItems: List<com.example.smartrestaurant.model.MenuItem>) {
        val adapter = MenuAdapter(subsetMenuItems, requireContext())
        binding.popularrecycleview.layoutManager = LinearLayoutManager(requireContext())
        binding.popularrecycleview.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Image Slider Configuration
        val imageList = arrayListOf(
            SlideModel(R.drawable.banner1, ScaleTypes.FIT),
            SlideModel(R.drawable.banner2, ScaleTypes.FIT),
            SlideModel(R.drawable.banner3, ScaleTypes.FIT)
        )

        val imageSlider = binding.imageSlider
        imageSlider.setImageList(imageList, ScaleTypes.FIT)

        imageSlider.setItemClickListener(object : ItemClickListener {
            override fun doubleClick(position: Int) {
                Toast.makeText(requireContext(), "Double clicked on $position", Toast.LENGTH_SHORT).show()
            }

            override fun onItemSelected(position: Int) {
                val itemMessage = "Selected Image $position"
                Toast.makeText(requireContext(), itemMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
