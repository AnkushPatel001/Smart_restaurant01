package com.example.smartrestaurant.fragment

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartrestaurant.Adapter.notificationAdapter
import com.example.smartrestaurant.R
import com.example.smartrestaurant.databinding.FragmentNotificationbottomfragmentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.ArrayList


class notificationbottomfragment :BottomSheetDialogFragment() {
private lateinit var binding: FragmentNotificationbottomfragmentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentNotificationbottomfragmentBinding.inflate(layoutInflater,container,false)
       val notification = listOf("your order has been Canceled Successfully","Order has been taken by the driver","Congrats Your Order Placed")
       val notificationImage = listOf(R.drawable.sademoji,R.drawable.congrats,R.drawable.truck)
       val adapter = notificationAdapter(
           ArrayList(notification),
           ArrayList(notificationImage)
       )
        binding.notificationRecycleview.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationRecycleview.adapter = adapter
        return binding.root
    }

    companion object {

    }
}