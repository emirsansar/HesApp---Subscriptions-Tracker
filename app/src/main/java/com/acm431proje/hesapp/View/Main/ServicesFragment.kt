package com.acm431proje.hesapp.View.Main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.acm431proje.hesapp.Adapter.ServicesAdapter
import com.acm431proje.hesapp.ViewModel.ServicesViewModel
import com.acm431proje.hesapp.databinding.FragmentServicesBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class ServicesFragment : Fragment() {

    private lateinit var binding: FragmentServicesBinding
    private lateinit var firestore: FirebaseFirestore

    private lateinit var viewModel : ServicesViewModel
    private val servicesAdapter = ServicesAdapter(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = Firebase.firestore
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

        binding.recyclerServicesView.layoutManager = LinearLayoutManager(context)
        binding.recyclerServicesView.adapter = servicesAdapter

        viewModel = ViewModelProvider(this)[ServicesViewModel::class.java]

        viewModel.refreshData()

        observeLiveData()


        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshData()

            refreshUI()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    servicesAdapter.updateData(viewModel.findServicesByName(query))
                    binding.searchCancel.visibility = View.VISIBLE
                } else {
                    viewModel.refreshData()
                    binding.searchCancel.visibility = View.GONE
                }
            }
        })

        binding.spinnerOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                when (position) {
                    1 -> servicesAdapter.sortByNameAscending()
                    2 -> servicesAdapter.sortByNameDescending()
                }
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
                    1 -> viewModel.getServicesByType("Alışveriş")
                        ?.let { servicesAdapter.updateData(it) }
                    2 -> viewModel.getServicesByType("Dizi/Film")
                        ?.let { servicesAdapter.updateData(it) }
                    3 -> viewModel.getServicesByType("Gelişim")
                        ?.let { servicesAdapter.updateData(it) }
                    4 -> viewModel.getServicesByType("Müzik")
                        ?.let { servicesAdapter.updateData(it) }
                    5 -> viewModel.getServicesByType("Oyun")
                        ?.let { servicesAdapter.updateData(it) }
                    else -> viewModel.getServicesByType("Spor")
                        ?.let { servicesAdapter.updateData(it) }
                }

                binding.spinnerOrder.setSelection(0)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        binding.btnRefresh.setOnClickListener {
            refreshUI()
            viewModel.refreshData()
        }

        binding.searchCancel.setOnClickListener {
            binding.searchText.setText("")
            binding.searchCancel.visibility = View.GONE
        }
    }


    private fun observeLiveData(){
        viewModel.services.observe(viewLifecycleOwner, Observer { services ->
            services?.let {
                servicesAdapter.updateData(services)
            }
        })

        viewModel.servicesError.observe(viewLifecycleOwner, Observer { error->
            error?.let {
                if(it) {
                    binding.textError.visibility = View.VISIBLE
                } else {
                    binding.textError.visibility = View.GONE
                }
            }
        })

        viewModel.servicesLoading.observe(viewLifecycleOwner, Observer { loading->
            loading?.let {
                if (it) {
                    binding.servicesLoading.visibility = View.VISIBLE
                    binding.recyclerServicesView.visibility = View.INVISIBLE
                    binding.textError.visibility = View.INVISIBLE
                } else {
                    binding.servicesLoading.visibility = View.GONE
                    binding.recyclerServicesView.visibility = View.VISIBLE
                }
            }
        })
    }


    private fun refreshUI(){
        binding.spinnerOrder.setSelection(0)
        binding.spinnerCategory.setSelection(0)
        binding.searchText.setText("")
    }
}