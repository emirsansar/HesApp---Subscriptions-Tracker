package com.acm431proje.hesapp.Main

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.acm431proje.hesapp.Adapter.UserSubscriptionsAdapter
import com.acm431proje.hesapp.Model.UserSubscription
import com.acm431proje.hesapp.databinding.FragmentUsersSubscriptionsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserSubscriptionsFragment : Fragment() {

    private lateinit var binding: FragmentUsersSubscriptionsBinding

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private lateinit var ownedServicesList: ArrayList<UserSubscription>
    private var feedOwnedServicesAdapter: UserSubscriptionsAdapter? = null

    private lateinit var layoutManager: LinearLayoutManager

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
        binding = FragmentUsersSubscriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ownedServicesList = ArrayList()

        layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager

        val userEmail = auth.currentUser!!.email


        lifecycleScope.launch(Dispatchers.Main) {
            getUsersOwnedServices(userEmail!!)

            feedOwnedServicesAdapter = UserSubscriptionsAdapter(ownedServicesList)
            binding.recyclerView.adapter = feedOwnedServicesAdapter
            feedOwnedServicesAdapter?.notifyDataSetChanged()

            updateUIVisibility(ownedServicesList)
        }


        binding.btnRemoveSub.setOnClickListener {
            handleRemoveSubscription(userEmail!!)

            HomeFragment.isChangedUserPlans = true
        }


        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                when (position) {
                    1 -> ownedServicesList.sortBy { it.planPrice } // fiyat artan
                    2 -> ownedServicesList.sortByDescending { it.planPrice } // fiyat azalan
                    3 -> ownedServicesList.sortBy { it.serviceName } // alfabetik artan
                    4 -> ownedServicesList.sortByDescending { it.serviceName } // alfabetik azalan
                    else -> {
                    }
                }
                feedOwnedServicesAdapter?.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }


    private fun handleRemoveSubscription(userEmail: String) {
        val selectedPosition = feedOwnedServicesAdapter?.selectedPosition

        if (selectedPosition != RecyclerView.NO_POSITION) {
            val selectedService = ownedServicesList[selectedPosition!!]
            val selectedServiceName = selectedService.serviceName

            showRemoveConfirmDialog(selectedServiceName) { confirmed ->
                if (confirmed) {
                    lifecycleScope.launch(Dispatchers.Main) {

                        if ( removeServiceFromUser(userEmail, selectedServiceName)){
                            feedOwnedServicesAdapter!!.selectedPosition = -1

                            updateUIVisibility(ownedServicesList)
                        }
                    }
                }

            }
        } else {
            Toast.makeText(requireContext(), "Lütfen bir servis seçin", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun removeServiceFromUser(userEmail: String, serviceName: String): Boolean{
        return try {
            val userServicesRef = firestore.collection("usersubscriptions").document(userEmail)

            userServicesRef.update(serviceName, FieldValue.delete()).await()

            val serviceCollectionRef = userServicesRef.collection(serviceName)
            serviceCollectionRef.get().addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    document.reference.delete()
                }

                ownedServicesList.removeAll { list -> list.serviceName == serviceName }
                feedOwnedServicesAdapter?.notifyDataSetChanged()
            }.await()

            Toast.makeText(requireContext(), "'$serviceName' aboneliğiniz başarıyla kaldırıldı.", Toast.LENGTH_SHORT).show()

            true
        }
        catch (e: Exception) {
            Toast.makeText(requireContext(), "Abonelik kaldırılamadı: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }

    }

    private suspend fun getUsersOwnedServices(userEmail: String) {

        try {
            val documentRef = firestore.collection("usersubscriptions").document(userEmail)
            val documentSnapshot = documentRef.get().await()

            if (documentSnapshot.exists()) {
                val collections = documentSnapshot.data?.keys

                if (collections != null) {
                    for (collectionName in collections) {

                        val subCollectionReference = documentRef.collection(collectionName)
                        val documentsInSubCollection = subCollectionReference.get().await()

                        for (document in documentsInSubCollection) {
                            val data = document.data

                            val serviceName = data?.get("serviceName") as? String
                            val planName = data?.get("planName") as? String
                            val planPrice = data?.get("planPrice") as? Number

                            if (serviceName != null && planName != null && planPrice != null) {
                                val userSubscription =
                                    UserSubscription(serviceName, planName, planPrice.toFloat())
                                ownedServicesList.add(userSubscription)
                            }
                        }
                    }

                    ownedServicesList.sortBy { it.planPrice }
                }
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }



    private fun showRemoveConfirmDialog(serviceName: String, callback: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Aboneliği Kaldır")
        builder.setMessage("'$serviceName' aboneliğinzi kaldırmak istediğinizden emin misiniz?")

        builder.setPositiveButton("Evet") { dialog, which ->
            callback(true)
            dialog.dismiss()
        }
        builder.setNegativeButton("Hayır") { dialog, which ->
            callback(false)
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    private fun updateUIVisibility(list: ArrayList<UserSubscription>) {
        if (list.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            binding.textNoSubs.text = "Herhangi bir\n aboneliğiniz\n bulunmamaktadır."
            binding.textButtonInfo.visibility = View.INVISIBLE
            binding.btnRemoveSub.visibility = View.INVISIBLE
            binding.recyclerView.visibility = View.INVISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.textNoSubs.text = ""
            binding.textButtonInfo.visibility = View.VISIBLE
            binding.btnRemoveSub.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

}