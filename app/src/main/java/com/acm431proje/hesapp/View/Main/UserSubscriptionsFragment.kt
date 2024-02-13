package com.acm431proje.hesapp.View.Main

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.acm431proje.hesapp.Adapter.UserSubscriptionsAdapter
import com.acm431proje.hesapp.Model.UserSubscription
import com.acm431proje.hesapp.ViewModel.UserSubsViewModel
import com.acm431proje.hesapp.databinding.FragmentUsersSubscriptionsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserSubscriptionsFragment : Fragment() {

    private lateinit var binding: FragmentUsersSubscriptionsBinding

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    //private lateinit var ownedServicesList: ArrayList<UserSubscription>
    private var userSubsAdapter: UserSubscriptionsAdapter? = null

    private lateinit var viewModel : UserSubsViewModel

    private var clickedServiceName: String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = Firebase.firestore
        auth = Firebase.auth
        currentUser = auth.currentUser
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUsersSubscriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ownedServicesList = ArrayList()

        val userEmail = auth.currentUser!!.email

        viewModel = ViewModelProvider(this)[UserSubsViewModel::class.java]

        binding.recyclerPlansView.layoutManager = LinearLayoutManager(context)

        userSubsAdapter = UserSubscriptionsAdapter(arrayListOf()) { clickedUserSub ->
            selectClickedUserSub(clickedUserSub)
        }

        binding.recyclerPlansView.adapter = userSubsAdapter

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.refreshData(userEmail!!)
            observeLiveData()
        }


        binding.btnRemoveSub.setOnClickListener {
            if (!clickedServiceName.isNullOrEmpty()){
                showRemoveConfirmDialog(clickedServiceName!!) { confirmed ->
                    if (confirmed) {
                        lifecycleScope.launch(Dispatchers.Main) {

                            val isSuccess = viewModel.removeSubFromUser(userEmail!!,clickedServiceName!!)

                            if (isSuccess) {
                                Toast.makeText(requireContext(), "'$clickedServiceName' aboneliğiniz başarıyla kaldırıldı.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Abonelik kaldırılırken bir hata oluştu.", Toast.LENGTH_SHORT).show()
                            }

                            viewModel.refreshData(userEmail!!)
                        }
                    }
                }
                userSubsAdapter!!.selectedPosition = -1

                HomeFragment.isChangedUserPlans = true
            } else {
                Toast.makeText(requireContext(), "Lütfen bir abonelik seçiniz!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                when (position) {
                    1 -> userSubsAdapter!!.sortByPriceAscending()
                    2 -> userSubsAdapter!!.sortByPriceDescending()
                    3 -> userSubsAdapter!!.sortByNameAscending()
                    4 -> userSubsAdapter!!.sortByNameDescending()
                    else -> {
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun selectClickedUserSub(userSub: UserSubscription){
        clickedServiceName = userSub.serviceName
    }

    private fun observeLiveData(){
        viewModel.userSubs.observe(viewLifecycleOwner, Observer { userSubs ->
            userSubs?.let {
                userSubsAdapter?.updateData(userSubs)

                updateUIVisibility(userSubs)
            }
        })


    }


    private fun showRemoveConfirmDialog(serviceName: String, callback: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Aboneliği Kaldır")
        builder.setMessage("'$serviceName' aboneliğinzi kaldırmak istediğinizden emin misiniz?")

        builder.setPositiveButton("Evet") { dialog, which ->
            callback(true)
            dialog.dismiss()
        }
        builder.setNegativeButton("Hayır") { dialog, which ->
            callback(false)
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    private fun updateUIVisibility(list: List<UserSubscription>) {
        if (list.isEmpty()) {
            binding.recyclerPlansView.visibility = View.GONE
            binding.textNoSubs.text = "Herhangi bir\n aboneliğiniz\n bulunmamaktadır."
            binding.textButtonInfo.visibility = View.INVISIBLE
            binding.btnRemoveSub.visibility = View.INVISIBLE
            binding.recyclerPlansView.visibility = View.INVISIBLE
        } else {
            binding.recyclerPlansView.visibility = View.VISIBLE
            binding.textNoSubs.text = ""
            binding.textButtonInfo.visibility = View.VISIBLE
            binding.btnRemoveSub.visibility = View.VISIBLE
            binding.recyclerPlansView.visibility = View.VISIBLE
        }
    }

}