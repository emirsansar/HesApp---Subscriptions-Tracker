package com.acm431proje.hesapp.View.Main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.acm431proje.hesapp.Model.UserDetails
import com.acm431proje.hesapp.R
import com.acm431proje.hesapp.View.Login.LoginActivity
import com.acm431proje.hesapp.ViewModel.UserDetailViewModel
import com.acm431proje.hesapp.databinding.FragmentHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class HomeFragment: Fragment() {

    private lateinit var binding : FragmentHomeBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUser : FirebaseUser? = null

    private lateinit var viewModel : UserDetailViewModel

    companion object {
        var isChangedUserPlans: Boolean = false
        var shouldFetchDataFromFirebase: Boolean = true
    }

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
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userEmail = auth.currentUser!!.email

        viewModel = ViewModelProvider(this)[UserDetailViewModel::class.java]

        loadingUserDetails(userEmail!!)

        setLogOutListener()
    }


    private fun loadingUserDetails(userEmail: String){
        viewModel.getUserFullName(userEmail!!) { userFullName ->
            if(shouldFetchDataFromFirebase){
                viewModel.fetchDataFromFirebase(userEmail, userFullName!!){ userDetail ->
                    updateUI(userDetail!!)
                    viewModel.insertUserDetailToDB(userDetail)
                }
                shouldFetchDataFromFirebase = false
            }
            else {
                if (isChangedUserPlans) {
                    viewModel.fetchDataFromFirebase(userEmail, userFullName!!){ userDetail ->
                        updateUI(userDetail!!)
                        viewModel.updateUserDetailToDB(userDetail)
                    }
                    isChangedUserPlans = false
                }
                else{
                    viewModel.fetchDataFromRoomDB(userEmail) { userDetail -> updateUI(userDetail!!) }
                }
            }
        }
    }

    private fun updateUI(userDetail: UserDetails){
        with(binding) {
            textGreeting.text = userDetail.fullName
            textTotalSub.text = userDetail.subCount.toString()

            val monthlySpendFormatted = String.format("%.2f", userDetail.spendingMonth)
            textMonthlySpend.text = getString(R.string.turkish_lira_icon, monthlySpendFormatted)
            val annualSpendFormatted = String.format("%.2f", userDetail.spendingAnnual )
            textAnnualSpend.text = getString(R.string.turkish_lira_icon, annualSpendFormatted)
        }
    }

    private fun setLogOutListener(){
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmDialog()
        }
    }

    private fun showLogoutConfirmDialog(){
        AlertDialog.Builder(requireContext())
            .setTitle("Çıkış Yap")
            .setMessage("Çıkış yapmak istediğinizden emin misiniz?")
            .setPositiveButton("Evet") { dialog, which ->
                auth.signOut()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("Hayır") { dialog, which ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

}