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

    private val userDetails = MutableLiveData<UserDetails>()

    private val firestore = Firebase.firestore

     fun refreshDataFromFirebase(userEmail: String, userFullName: String, callback: (UserDetails?) -> Unit) {
        calculateSubCountAndMonthlySpending(userEmail){ subCountAndSpending ->

            val userDetail = UserDetails(userEmail, userFullName,
                subCountAndSpending.first, subCountAndSpending.second, subCountAndSpending.second * 12)

            userDetails.value = userDetail

            callback(userDetail)
        }
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


    fun getUserFullName(userEmail: String, callback: (String?) -> Unit) {
        launch {
            firestore.collection("users").document(userEmail).get().addOnSuccessListener { documentSnapshot ->
                val name = documentSnapshot.getString("name")
                val surname = documentSnapshot.getString("surname")

                if (!name.isNullOrEmpty() && !surname.isNullOrEmpty()) {
                    val fullName = "$name $surname"

                    callback(fullName)
                }
            }.addOnFailureListener { e->
                Log.e("HATA", "Hata oluştu: ${e.localizedMessage}", e)
            }
        }
    }


    private fun calculateSubCountAndMonthlySpending(userEmail: String, callback: (Pair<Int, Float>) -> Unit) {
        launch {
            var subCount = 0
            var monthlySpending = 0f

            val documentRef = firestore.collection("usersubscriptions").document(userEmail)
            documentRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val collections = documentSnapshot.data?.keys

                    if (collections != null) {
                        for (collectionName in collections) {
                            val subsInformation = documentRef.collection(collectionName).document("subsinfo")
                            subsInformation.get().addOnSuccessListener { subsDocument ->
                                val data = subsDocument.data

                                val planPrice = data?.get("planPrice") as? Number
                                if (planPrice != null) {
                                    monthlySpending += planPrice.toFloat()
                                }
                                subCount++

                                if (subCount == collections.size) {
                                    callback(Pair(subCount, monthlySpending))
                                }
                            }.addOnFailureListener { e ->
                                Log.e("HATA", "Alt koleksiyon okunurken hata oluştu: ${e.localizedMessage}", e)
                            }
                        }
                    }
                } else {
                    callback(Pair(subCount, monthlySpending))
                }
            }.addOnFailureListener { e ->
                Log.e("HATA", "Belge okunurken hata oluştu: ${e.localizedMessage}", e)
            }
        }

    }

}
