package com.acm431proje.hesapp.ViewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.acm431proje.hesapp.Model.Plan
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class PlansViewModel(): ViewModel() {

    val plans = MutableLiveData<List<Plan>>()

    val plansError = MutableLiveData<Boolean>()
    val plansLoading = MutableLiveData<Boolean>()

    private val firestore = Firebase.firestore


    fun fetchPlanDataFromFirebase(serviceName: String){
        val plansList = arrayListOf<Plan>()

        plansLoading.value = true

        val serviceDocRef = firestore.collection("services").document(serviceName)

        serviceDocRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val planData = documentSnapshot.data

                    if (planData != null) {
                        for ((fieldName, fieldValue) in planData) {
                            if (fieldValue is String && fieldValue != null && fieldName != "Tür") {
                                val plan = Plan(fieldName, fieldValue, false)
                                plansList.add(plan)
                            }
                        }
                    }
                    plans.value = plansList

                    plansError.value = false
                    plansLoading.value = false
                }
            }.addOnFailureListener { e ->
                plansError.value = true
                plansLoading.value = false

                Log.e("HATA", e.localizedMessage!!)
            }
    }


    suspend fun addServiceToUser(userMail: String, serviceName: String, planName: String, planPrice: String): Boolean {
        if(planName.isNullOrEmpty() || planPrice.isNullOrEmpty()) return false

        try {
            val userDocRef = firestore.collection("usersubscriptions").document(userMail)
            val userDocSnapshot = userDocRef.get().await()

            if (!userDocSnapshot.exists()) {
                val userData = hashMapOf<String, Any>()
                userDocRef.set(userData).await()
            }

            val existingServiceName = userDocSnapshot.getString(serviceName)

            if (existingServiceName == null) {
                userDocRef.update(serviceName, serviceName).await()

                val subscriptionDocRef = userDocRef.collection(serviceName).document("subsinfo")
                val subscriptionDocSnapshot = subscriptionDocRef.get().await()

                if (!subscriptionDocSnapshot.exists()) {
                    val planPriceNumber: Number = try {
                        planPrice.toFloat()
                    } catch (e: NumberFormatException) {
                        0
                    }

                    val subscriptionData = hashMapOf(
                        "serviceName" to serviceName,
                        "planName" to planName,
                        "planPrice" to planPriceNumber
                    )

                    subscriptionDocRef.set(subscriptionData).await()

                    return true
                }
            }
        } catch (e: Exception) {
            Log.e("HATA", e.localizedMessage!!)
        }

        return false
    }
}