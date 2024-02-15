package com.acm431proje.hesapp.ViewModel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.acm431proje.hesapp.Model.UserDetails
import com.acm431proje.hesapp.Room.UserDetailDB
import com.acm431proje.hesapp.View.Main.HomeFragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserDetailViewModel(application: Application): BaseViewModel(application) {

    val userDetails = MutableLiveData<UserDetails>()

    val firestore = Firebase.firestore

    suspend fun refreshDataFromFirebase(userEmail: String, userID: String, userFullName: String) {
        val countAndSpending: Pair<Int, Float> = calculateSubCountAndMonthlySpending(userEmail)

        val userDetail = UserDetails(userID, userFullName,
            countAndSpending.first, countAndSpending.second, countAndSpending.second * 12)

        userDetails.value = userDetail
    }

    fun refreshDataFromRoomDB(userID: String, callback: (UserDetails?) -> Unit) {
        launch {
            val userDetail = UserDetailDB(getApplication()).userDetailDao().getUserDetail(userID)
            callback(userDetail)
        }
    }

    fun insertUserDetailToDB(userDetails: UserDetails){
        launch {
            UserDetailDB(getApplication()).userDetailDao().insert(userDetails)
        }
    }

    fun updateUserDetailToDB(userDetails: UserDetails){
        launch {
            UserDetailDB(getApplication()).userDetailDao().insert(userDetails)
        }
    }

    fun getUserID(userEmail: String): String{
        var uid: String ?= null

        firestore.collection("users").document(userEmail).get().addOnSuccessListener { documentSnapshot ->
            uid = documentSnapshot.getString("uid")
        }.addOnFailureListener { e->
        }

        return uid.toString()
    }



    suspend fun getUserFullName(userEmail: String): String {
        var fullName: String? = null

        try {
            val documentRef = firestore.collection("users").document(userEmail)
            val documentSnapshot = documentRef.get().await()

            if (documentSnapshot.exists()) {
                val name = documentSnapshot.getString("name")
                val surname = documentSnapshot.getString("surname")

                if (!name.isNullOrEmpty() && !surname.isNullOrEmpty()) {
                    fullName = "$name $surname"
                }
            }
        } catch (e: Exception) {
            Log.e("HATA", "Hata oluştu: ${e.localizedMessage}", e)
        }

        return fullName.toString()
    }

    fun getUserFullName2(userEmail: String, callback: (String?) -> Unit) {
        launch {
            var fullName: String? = null

            firestore.collection("users").document(userEmail).get().addOnSuccessListener { documentSnapshot ->
                val name = documentSnapshot.getString("uid")
                val surname = documentSnapshot.getString("surname")

                if (!name.isNullOrEmpty() && !surname.isNullOrEmpty()) {
                    fullName = "$name $surname"
                }
            }.addOnFailureListener { e->
                Log.e("HATA", "Hata oluştu: ${e.localizedMessage}", e)
            }
            callback(fullName)
        }
    }

    suspend fun calculateSubCountAndMonthlySpending(userEmail: String): Pair<Int, Float> {
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
        } catch (e: Exception) {
            //Toast.makeText(requireContext(), "Bir hata oluştu: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }

        println(subCount)
        println(monthlySpending)

        return Pair(first = subCount, second = monthlySpending)
    }
}
