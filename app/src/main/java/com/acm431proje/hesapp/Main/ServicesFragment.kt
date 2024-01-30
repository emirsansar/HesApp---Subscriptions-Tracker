package com.acm431proje.hesapp.Main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.acm431proje.hesapp.Adapter.ServicesAdapter
import com.acm431proje.hesapp.Model.Service
import com.acm431proje.hesapp.databinding.FragmentServicesBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class ServicesFragment : Fragment() {

    private lateinit var binding: FragmentServicesBinding
    private lateinit var firestore: FirebaseFirestore

    private lateinit var servicesList: ArrayList<Service>
    private var feedServiceAdapter: ServicesAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = Firebase.firestore
        servicesList = ArrayList()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentServicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerServicesView.layoutManager = layoutManager

        getServices()

        feedServiceAdapter = ServicesAdapter(servicesList, object : ServicesAdapter.Listener {
            override fun onItemClick(service: Service, position: Int) {
                Toast.makeText(requireContext(), "Tıklanan servis: ${service.name}", Toast.LENGTH_SHORT).show()

                openPlansActivity(service)
            }
        })
        binding.recyclerServicesView.adapter = feedServiceAdapter


        binding.btnSearch.setOnClickListener {
            resetServices()
            hideKeyboard()

            val input = binding.textInputSearch.text.toString()
            if (input.isNotEmpty()) {
                val searchResult = findServicesByName(input)
                feedServiceAdapter?.updateData(searchResult)
            } else {
                Toast.makeText(requireContext(),"Lütfen bir arama kriteri giriniz.", Toast.LENGTH_SHORT).show()
                getServices()
            }

            binding.textInputSearch.text?.clear()
        }


        binding.spinnerOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                when (position) {
                    1 -> servicesList.sortBy { it.name }
                    2 -> servicesList.sortByDescending { it.name }
                }

                feedServiceAdapter?.notifyDataSetChanged()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                when (position) {
                    1 -> getServicesByType("Alışveriş")
                    2 -> getServicesByType("Dizi/Film")
                    3 -> getServicesByType("Gelişim")
                    4 -> getServicesByType("Müzik")
                    5 -> getServicesByType("Oyun")
                    else -> getServicesByType("Spor")
                }

                servicesList.sortBy { it.name }

                binding.spinnerOrder.setSelection(0)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        binding.btnRefresh.setOnClickListener {
            binding.textInputSearch.setText("")

            binding.spinnerOrder.setSelection(0)
            binding.spinnerCategory.setSelection(0)

            getServices()
        }

    }



    private fun getServices() {
        firestore.collection("services").get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot != null) {
                servicesList?.clear()

                for (document in querySnapshot.documents) {
                    val serviceName = document.id
                    if (serviceName.isNotBlank()) {
                        val serviceType = document.getString("Tür")

                        val service = Service(serviceName, serviceType!!)
                        servicesList?.add(service)
                    }
                }
                servicesList?.sortBy { it.name }

                feedServiceAdapter?.notifyDataSetChanged()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Bir hata meydana geldi: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun findServicesByName(input: String): ArrayList<Service> {
        val searchList: ArrayList<Service> = ArrayList()

        for (service in servicesList) {
            if (service.name.contains(input, ignoreCase = true)) {
                searchList.add(service)
            }
        }

        if (searchList.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Uygun sonuç bulunamadı.", Toast.LENGTH_SHORT).show()
        }

        return searchList
    }

    private fun getServicesByType(type: String){
        resetServices()

        val filteredList = servicesList?.filter { service ->
            service.type == type }

        if (servicesList != null && filteredList != null) {
            servicesList.clear()
            servicesList.addAll(filteredList)
        }

        feedServiceAdapter?.notifyDataSetChanged()
    }

    private fun resetServices(){
        firestore.collection("services").get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot != null) {
                servicesList?.clear()

                for (document in querySnapshot.documents) {
                    val serviceName = document.id
                    if (serviceName.isNotBlank()) {
                        val serviceType = document.getString("Tür")

                        val service = Service(serviceName, serviceType!!)
                        servicesList?.add(service)
                    }
                }
                servicesList?.sortBy { it.name }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Bir hata meydana geldi: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }



    private fun openPlansActivity(selectedService: Service){
        val intent = Intent(requireContext(), PlansActivity::class.java)
        intent.putExtra("serviceName", selectedService.name)
        startActivity(intent)
    }


    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}