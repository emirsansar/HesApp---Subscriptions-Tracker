package com.acm431proje.hesapp.Login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.acm431proje.hesapp.Main.MainActivity
import com.acm431proje.hesapp.R
import com.acm431proje.hesapp.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private var navHostFragment: NavHostFragment?= null

    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentLoginContainer) as NavHostFragment
        NavigationUI.setupWithNavController(binding.bottomLoginMenu, navHostFragment!!.navController)

        auth = Firebase.auth
        firestore = Firebase.firestore

        if (auth.currentUser != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}