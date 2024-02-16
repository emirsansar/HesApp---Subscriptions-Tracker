package com.acm431proje.hesapp.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.acm431proje.hesapp.Model.Offer
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class OfferViewModel(application: Application): BaseViewModel(application) {

    val offers = MutableLiveData<List<Offer>>()

    var offersLoading = MutableLiveData<Boolean>()

    private val firestore = Firebase.firestore

    fun fetchOffers(){
        var offerList = arrayListOf<Offer>()

        offersLoading.value = true

        launch{
            firestore.collection("offers").get().addOnSuccessListener { snapshot ->
                for (document in snapshot){
                    val company = document.getString("Company")
                    val offerText = document.getString("Info")

                    val offer = Offer(company!!, offerText!!)
                    offerList.add(offer)
                }
                offerList.sortBy { it.company }

                offers.value = offerList

                offersLoading.value = false
            }.addOnFailureListener { error ->
                Log.e("HATA", error.localizedMessage)
                offersLoading.value = false
            }
        }
    }


    fun getServicesByCompany(company: String): List<Offer>? {
        return offers.value?.filter { it.company == company }
    }
}