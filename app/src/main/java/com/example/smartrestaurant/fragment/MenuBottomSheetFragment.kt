package com.example.smartrestaurant.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartrestaurant.Adapter.MenuAdapter
import com.example.smartrestaurant.databinding.FragmentMenuBottomSheetBinding
import com.example.smartrestaurant.model.MenuItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.*

class MenuBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentMenuBottomSheetBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var menuItems: MutableList<MenuItem>
    private lateinit var menuAdapter: MenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBottomSheetBinding.inflate(inflater, container, false)

        binding.buttonBack.setOnClickListener {
            dismiss()
        }

        menuItems = mutableListOf()
        menuAdapter = MenuAdapter(menuItems, requireContext())
        binding.menuRecycleview.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecycleview.adapter = menuAdapter

        retrieveMenuItems()

        return binding.root
    }

    private fun retrieveMenuItems() {
        database = FirebaseDatabase.getInstance()
        val foodRef: DatabaseReference = database.reference.child("menu")

        foodRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuItems.clear()
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let {
                        menuItems.add(it)
                    }
                }
                menuAdapter.notifyDataSetChanged()
                Log.d("Item", "onDataChange: Data Received and Adapter Notified")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Item", "onCancelled: ${error.message}")
            }
        })
    }
}
