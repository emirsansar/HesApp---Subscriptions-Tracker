package com.acm431proje.hesapp.View.Main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.acm431proje.hesapp.Adapter.OffersAdapter
import com.acm431proje.hesapp.ViewModel.OfferViewModel
import com.acm431proje.hesapp.databinding.FragmentOffersBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class OffersFragment : Fragment() {

    private lateinit var binding: FragmentOffersBinding
    private lateinit var firestore: FirebaseFirestore

    private val offersAdapter = OffersAdapter(arrayListOf())

    private lateinit var viewModel: OfferViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = Firebase.firestore
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

        viewModel = ViewModelProvider(this)[OfferViewModel::class.java]

        setupRecyclerView()

        observeViewModel()

        setListeners()
    }


    private fun observeLiveData(){
        viewModel.offers.observe(viewLifecycleOwner, Observer { offers ->
            offers?.let {
                offersAdapter!!.updateData(offers)
            }
        })

        viewModel.offersLoading.observe(viewLifecycleOwner, Observer { loading ->
            binding.swipeRefreshLayout.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        })
    }

    private fun observeViewModel(){
        viewModel.fetchOffers()

        observeLiveData()
    }

    private fun setupRecyclerView(){
        binding.recyclerOffersView.layoutManager = LinearLayoutManager(context)
        binding.recyclerOffersView.adapter = offersAdapter
    }
    private fun setSpinnerCompanyListener(){
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (position != 0){
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    viewModel.getServicesByCompany(selectedItem)?.let {
                        offersAdapter.updateData(it)
                    }
                }
                else viewModel.fetchOffers()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun setSwipeRefreshListener(){
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchOffers()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setListeners(){
        setSpinnerCompanyListener()
        setSwipeRefreshListener()
    }
}