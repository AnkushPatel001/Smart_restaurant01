package com.example.smartrestaurant.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartrestaurant.databinding.FragmentProfileBinding
import com.example.smartrestaurant.model.UserModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ProfileFragment : Fragment() {

    private val IMGUR_CLIENT_ID = "b85329841b5a4e2"
    private var selectedImageUri: Uri? = null

    private val auth = FirebaseAuth.getInstance()
    private lateinit var binding: FragmentProfileBinding
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileImage.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .galleryOnly()
                .start()
        }

        setFieldsEditable(false)

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.editProfileButton.setOnClickListener {
            setFieldsEditable(true)
            Toast.makeText(requireContext(), "Editing enabled", Toast.LENGTH_SHORT).show()
        }

        binding.SaveButton.setOnClickListener {
            val name = binding.NameEdit.text.toString()
            val email = binding.EmailEdit.text.toString()
            val address = binding.AdressEdit.text.toString()
            val phone = binding.PhoneEdit.text.toString()
            val password = binding.PasswordEdit.text.toString()
            updateUserData(name, email, address, phone, password)
            setFieldsEditable(false)
        }

        setUserData()
    }

    private fun setFieldsEditable(enabled: Boolean) {
        binding.NameEdit.isEnabled = enabled
        binding.EmailEdit.isEnabled = enabled
        binding.AdressEdit.isEnabled = enabled
        binding.PhoneEdit.isEnabled = enabled
        binding.PasswordEdit.isEnabled = enabled
    }

    private fun updateUserData(name: String, email: String, address: String, phone: String, password: String) {
        val userId = auth.currentUser?.uid ?: return
        val userReference = database.getReference("user").child(userId)
        val userData = mapOf(
            "name" to name,
            "email" to email,
            "address" to address,
            "phone" to phone,
            "password" to password
        )

        userReference.updateChildren(userData).addOnSuccessListener {
            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Profile update failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUserData() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("user").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)
                user?.let {
                    binding.NameEdit.setText(it.name)
                    binding.EmailEdit.setText(it.email)
                    binding.AdressEdit.setText(it.address)
                    binding.PhoneEdit.setText(it.phone)
                    binding.PasswordEdit.setText(it.password)
                    if (!it.imageUrl.isNullOrEmpty()) {
                        Picasso.get()
                            .load(it.imageUrl)
                            .placeholder(com.example.smartrestaurant.R.drawable.profileankush)
                            .into(binding.profileImage)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            binding.profileImage.setImageURI(selectedImageUri)
            selectedImageUri?.let { uploadToImgur(it) }
        }
    }

    private fun uploadToImgur(imageUri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes()
        inputStream?.close()

        if (imageBytes == null) {
            Toast.makeText(requireContext(), "Failed to read image", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", Base64.encodeToString(imageBytes, Base64.DEFAULT))
            .build()

        val request = Request.Builder()
            .url("https://api.imgur.com/3/image")
            .addHeader("Authorization", "Client-ID $IMGUR_CLIENT_ID")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: "")
                val imageUrl = json.getJSONObject("data").getString("link")
                saveImageUrlToFirebase(imageUrl)
            }
        })
    }

    private fun saveImageUrlToFirebase(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("user").child(userId)

        userRef.child("imageUrl").setValue(imageUrl)
            .addOnSuccessListener {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    // Reload image using Picasso after saving
                    Picasso.get()
                        .load(imageUrl)
                        .placeholder(com.example.smartrestaurant.R.drawable.profileankush)
                        .into(binding.profileImage)
                }
            }
            .addOnFailureListener {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to save image URL", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
