package com.acm431proje.hesapp.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.acm431proje.hesapp.Model.Service
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ServicesViewModel: ViewModel() {

    val services = MutableLiveData<List<Service>>()

    val servicesError = MutableLiveData<Boolean>()
    val servicesLoading = MutableLiveData<Boolean>()

    private val firestore = Firebase.firestore


    fun refreshData(){
        val servicesList = arrayListOf<Service>()

        firestore.collection("services").get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot != null) {
                servicesList.clear()

                for (document in querySnapshot.documents) {
                    val serviceName = document.id
                    if (serviceName.isNotBlank()) {
                        val serviceType = document.getString("Tür")

                        val service = Service(serviceName, serviceType!!)
                        servicesList.add(service)
                    }
                }
                servicesList.sortBy { it.name }

                services.value = servicesList

                servicesError.value = false
                servicesLoading.value = false
            }
        }.addOnFailureListener {
            servicesError.value = true
            servicesLoading.value = false
        }

    }


    fun findServicesByName(input: String): List<Service> {
        val searchList: MutableList<Service> = mutableListOf()

        services.value?.let { serviceList ->
            for (service in serviceList) {
                if (service.name.contains(input, ignoreCase = true)) {
                    searchList.add(service)
                }
            }
        }

        return searchList
    }

    fun getServicesByType(type: String): List<Service>? {
        return services.value?.filter { it.type == type }
    }

}