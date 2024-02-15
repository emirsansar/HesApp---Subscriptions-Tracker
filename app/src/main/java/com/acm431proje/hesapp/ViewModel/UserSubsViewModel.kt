package com.acm431proje.hesapp.ViewModel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.acm431proje.hesapp.Model.UserSubscription
import com.acm431proje.hesapp.Room.AppDatabase
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserSubsViewModel(application: Application): BaseViewModel(application) {

    var userSubs = MutableLiveData<List<UserSubscription>>()

    var userSubsError = MutableLiveData<Boolean>()
    var userSubsLoading = MutableLiveData<Boolean>()

    private var firestore = Firebase.firestore

     fun getUserSubsFromFirebase(userEmail: String, callback: (ArrayList<UserSubscription>?) -> Unit){
        val userSubsList = arrayListOf<UserSubscription>()

        userSubsLoading.value = true

        launch {
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

                                val serviceName = data["serviceName"] as? String
                                val planName = data["planName"] as? String
                                val planPrice = data["planPrice"] as? Number

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

            callback(userSubsList)
        }

    }

    fun removeSubFromFirebase(userEmail: String, clickedServiceName: String, callback: (Boolean?) -> Unit){
         var isSuccess = false

         userSubsLoading.value = true

        launch {
            try {
                val userServicesRef = firestore.collection("usersubscriptions").document(userEmail)

                userServicesRef.update(clickedServiceName, FieldValue.delete()).await()

                val serviceCollectionRef = userServicesRef.collection(clickedServiceName)
                serviceCollectionRef.get().addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        document.reference.delete()
                    }
                }.await()

                isSuccess = true
            }
            catch (e: Exception) {
                Log.e("HATA", e.localizedMessage)
            }

            callback(isSuccess)
        }
    }

    fun insertAllSubsToDB(list: List<UserSubscription>){
        launch {
            AppDatabase(getApplication()).userSubsDao().insertAll(list)
        }
    }

    fun getAllSubsFromDB(){
        launch {
            val userSubsriptions = AppDatabase(getApplication()).userSubsDao().getAllUserSubs()

            println(userSubsriptions.isEmpty())

            for ( i in userSubsriptions){
                println(i.serviceName)
            }

            userSubs.value = userSubsriptions

            Toast.makeText(getApplication(),"UserSubs From SQLite",Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteSubFromDB(serviceName: String){
        launch {
            AppDatabase(getApplication()).userSubsDao().deleteUserSub(serviceName)
        }
    }
}