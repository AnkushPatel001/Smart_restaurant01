package com.example.smartrestaurant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.smartrestaurant.databinding.ActivitySigninBinding
import com.example.smartrestaurant.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SigninActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var username: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient

    private val binding: ActivitySigninBinding by lazy {
        ActivitySigninBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Google Sign-In Options
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        binding.CreateAccountbutton.setOnClickListener {
            username = binding.NameEdittext.text.toString().trim()
            email = binding.EmailEdittext.text.toString().trim()
            password = binding.PaaswordEdittext.text.toString().trim()

            if (username.isEmpty() || email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(email, password)
            }
        }

        binding.gotoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.Googlebutton.setOnClickListener {
            val signIntent = googleSignInClient.signInIntent
            launcher.launch(signIntent)
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account: GoogleSignInAccount? = task.result
                    val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                    auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
                            // Google Sign-In Successful
                            val currentUser = auth.currentUser
                            val user = UserModel(
                                name = account?.displayName ?: "",
                                email = account?.email ?: "",
                                password = "" // No password for Google accounts
                            )
                            val userId = currentUser?.uid
                            userId?.let {
                                database.child("user").child(it).setValue(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Signed in Successfully!", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Google Sign In Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account created successfully. Please login now.", Toast.LENGTH_SHORT).show()
                    saveUserData()
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Account creation failed", Toast.LENGTH_SHORT).show()
                    Log.e("Account", "CreateAccount: Failure", task.exception)
                }
            }
    }

    private fun saveUserData() {
        val user = UserModel(username, email, password)
        val userId = auth.currentUser?.uid

        userId?.let {
            database.child("user").child(it).setValue(user)
        }
    }
}
