package com.acm431proje.hesapp.Main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.acm431proje.hesapp.Login.LoginActivity
import com.acm431proje.hesapp.databinding.FragmentHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUser : FirebaseUser? = null

    private lateinit var savedTotalSub :String
    private lateinit var savedMonthlySpend :String
    private lateinit var savedAnnualSpend :String
    private lateinit var savedGreeting : String

    companion object {
        var isChangedUserPlans: Boolean = true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = Firebase.firestore
        auth = Firebase.auth
        currentUser = auth.currentUser
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userEmail = auth.currentUser!!.email

        if (isChangedUserPlans) {

            lifecycleScope.launch(Dispatchers.Main) {
                if (userEmail != null) {
                    Greeting(userEmail)

                    val countAndSpending = calculateSubCountAndMonthlySpending(userEmail)

                    binding.textTotalSub.text = countAndSpending.first.toString()

                    val monthlySpend = countAndSpending.second

                    val monthlySpendFormatted = String.format("%.2f", monthlySpend)
                    binding.textMonthlySpend.text = "$monthlySpendFormatted \u20BA"
                    val annualSpendFormatted = String.format("%.2f", monthlySpend * 12)
                    binding.textAnnualSpend.text = "$annualSpendFormatted \u20BA"


                    savedTotalSub = binding.textTotalSub.text.toString()
                    savedMonthlySpend = binding.textMonthlySpend.text.toString()
                    savedAnnualSpend = binding.textAnnualSpend.text.toString()
                    savedGreeting = binding.textGreeting.text.toString()

                    isChangedUserPlans = false
                }
            }
        }
        else{
            binding.textTotalSub.setText(savedTotalSub)
            binding.textMonthlySpend.setText(savedMonthlySpend)
            binding.textAnnualSpend.setText(savedAnnualSpend)
            binding.textGreeting.setText(savedGreeting)
        }


        binding.btnLogout.setOnClickListener {
            showLogoutConfirmDialog()
        }
    }



    private fun Greeting(userEmail: String){
        firestore.collection("users").document(userEmail).get().addOnSuccessListener { documentSnapshot ->
            val name = documentSnapshot.getString("name")
            val surname = documentSnapshot.getString("surname")

            if (!name.isNullOrEmpty() && !surname.isNullOrEmpty()) {
                binding.textGreeting.setText("$name $surname")
            } else {
                binding.textGreeting.text = ""
            }
        }.addOnFailureListener { e->
            Toast.makeText(requireContext(),"Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }


    private suspend fun calculateSubCountAndMonthlySpending(userEmail: String): Pair<Int, Float> {
        var subCount = 0
        var monthlySpending = 0f

        try {
            val documentRef = firestore.collection("usersubscriptions").document(userEmail)
            val documentSnapshot = documentRef.get().await()

            if (documentSnapshot.exists()) {
                val collections = documentSnapshot.data?.keys

                if (collections != null) {
                    for (collectionName in collections) {
                        val subsInformation = documentRef.collection(collectionName).document("subsinfo")
                        val document = subsInformation.get().await()

                        val data = document.data

                        val planPrice = data?.get("planPrice") as? Number
                        if (planPrice != null) {
                            monthlySpending += planPrice.toFloat()
                        }
                        subCount++
                    }
                }
            }
        }
        catch (e: Exception) {
            Toast.makeText(requireContext(), "Bir hata oluştu: ${e.localizedMessage}",Toast.LENGTH_SHORT).show()
        }

        return Pair(first = subCount, second = monthlySpending)
    }


//    private fun calculateSubCountAndMonthlySpending(userEmail: String): Pair<Int, Float> {
//        var subCount = 0
//        var monthlySpending = 0f
//
//        val documentRef = firestore.collection("usersubscriptions").document(userEmail)
//
//        documentRef.get().addOnSuccessListener { documentSnapshot ->
//            if (documentSnapshot.exists()) {
//                val collections = documentSnapshot.data?.keys
//
//                if (collections != null) {
//                    for (collectionName in collections) {
//
//                        documentRef.collection(collectionName)
//                            .document("subsinfo").get().addOnSuccessListener { subsInfoSnapshot ->
//                                val data = subsInfoSnapshot.data
//
//                                val planPrice = data?.get("planPrice") as? Number
//                                if (planPrice != null) {
//                                    monthlySpending += planPrice.toFloat()
//                                }
//
//                                subCount++ }
//                    }
//                }
//            }
//        }
//
//        return Pair(first = subCount, second = monthlySpending)
//    }




    private fun showLogoutConfirmDialog(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Çıkış Yap")
        builder.setMessage("Çıkış yapmak istediğinizden emin misiniz?")

        builder.setPositiveButton("Evet") { dialog, which ->
            auth.signOut()

            isChangedUserPlans = true

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        builder.setNegativeButton("Hayır") { dialog, which ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}