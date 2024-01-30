package com.acm431proje.hesapp.Main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.acm431proje.hesapp.Adapter.OffersAdapter
import com.acm431proje.hesapp.Model.Offer
import com.acm431proje.hesapp.databinding.FragmentOffersBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OffersFragment : Fragment() {

    private lateinit var binding: FragmentOffersBinding
    private lateinit var firestore: FirebaseFirestore

    private lateinit var offersList: ArrayList<Offer>
    private var feedOffersAdapter: OffersAdapter? = null

    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = Firebase.firestore

        offersList = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOffersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerOffersView.layoutManager = layoutManager

        getOffers()

        feedOffersAdapter = OffersAdapter(offersList)
        binding.recyclerOffersView.adapter = feedOffersAdapter
        feedOffersAdapter?.notifyDataSetChanged()
    }


    private fun getOffers(){

        firestore.collection("offers").get().addOnSuccessListener { snapshot ->
            offersList?.clear()

            for (document in snapshot){
                val company = document.getString("Company")
                val offerText = document.getString("Info")

                val offer = Offer(company!!, offerText!!)
                offersList.add(offer)
            }

            offersList.sortBy { it.company }

            feedOffersAdapter?.notifyDataSetChanged()
        }.addOnFailureListener { error ->
            Toast.makeText(requireContext(), "Bir hata oldu: ${error.localizedMessage}",Toast.LENGTH_SHORT).show()
        }

    }

}