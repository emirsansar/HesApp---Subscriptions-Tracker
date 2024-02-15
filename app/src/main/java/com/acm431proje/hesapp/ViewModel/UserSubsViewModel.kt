package com.acm431proje.hesapp.ViewModel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.acm431proje.hesapp.Model.Plan
import com.acm431proje.hesapp.Model.UserSubscription
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class UserSubsViewModel: ViewModel() {

    var userSubs = MutableLiveData<List<UserSubscription>>()

    var userSubsError = MutableLiveData<Boolean>()
    var userSubsLoading = MutableLiveData<Boolean>()
    init {
        userSubsLoading.value = true
    }

    private var firestore = Firebase.firestore

    suspend fun refreshData(userEmail: String){
        val userSubsList = arrayListOf<UserSubscription>()

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
                                userSubsList.add(userSubscription)
                            }
                        }
                    }
                    userSubsList.sortBy { it.planPrice }

                    userSubs.value = userSubsList

                    userSubsError.value = false
                    userSubsLoading.value = false
                }
            }
        }
        catch (e: Exception) {
            userSubsError.value = true
            userSubsLoading.value = false

            Log.e("HATA", e.localizedMessage)
        }
    }

    suspend fun removeSubFromUser(userEmail: String, clickedServiceName: String): Boolean{
        try {
            val userServicesRef = firestore.collection("usersubscriptions").document(userEmail)

            userServicesRef.update(clickedServiceName, FieldValue.delete()).await()

            val serviceCollectionRef = userServicesRef.collection(clickedServiceName)
            serviceCollectionRef.get().addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    document.reference.delete()
                }
            }.await()

            return true
        }
        catch (e: Exception) {
            Log.e("HATA", e.localizedMessage)
        }

        return false
    }
}