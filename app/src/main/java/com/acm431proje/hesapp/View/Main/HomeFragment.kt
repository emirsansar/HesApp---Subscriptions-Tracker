package com.acm431proje.hesapp.View.Main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.acm431proje.hesapp.Model.UserDetails
import com.acm431proje.hesapp.Room.UserDetailDB
import com.acm431proje.hesapp.Room.UserDetailDao
import com.acm431proje.hesapp.View.Login.LoginActivity
import com.acm431proje.hesapp.ViewModel.UserDetailViewModel
import com.acm431proje.hesapp.databinding.FragmentHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUser : FirebaseUser? = null

    private lateinit var viewModel : UserDetailViewModel

    private lateinit var db: UserDetailDB
    private lateinit var userDetailDao: UserDetailDao

    companion object {
        var isChangedUserPlans: Boolean = false
        var isAppLaunched: Boolean = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = Firebase.firestore
        auth = Firebase.auth
        currentUser = auth.currentUser

        db = Room.databaseBuilder(requireContext().applicationContext,UserDetailDB::class.java,"UserDetail").build()
        userDetailDao = db.userDetailDao()
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

        viewModel = ViewModelProvider(this)[UserDetailViewModel::class.java]

        val userEmail = auth.currentUser!!.email
        val userID = viewModel.getUserID(userEmail!!)

        lifecycleScope.launch(Dispatchers.Main){
            val userFullName = viewModel.getUserFullName(userEmail)

            if(isAppLaunched){
                viewModel.refreshDataFromFirebase(userEmail, userID, userFullName!!)

                viewModel.userDetails.value?.let { userDetail ->
                    updateUI(userDetail)
                    userDetailDao.update(userDetail)
                }

                isAppLaunched = false
            }
            else {
                if (isChangedUserPlans) {
                    viewModel.refreshDataFromFirebase(userEmail, userID, userFullName!!)

                    viewModel.userDetails.value?.let { userDetail ->
                        updateUI(userDetail)
                    }

                    isChangedUserPlans = false
                }
                else{
                    userDetailDao.getUserDetail(userID)?.let {
                        updateUI(it) }
                }
            }
        }


        binding.btnLogout.setOnClickListener {
            showLogoutConfirmDialog()
        }
    }



    private fun updateUI(userDetail: UserDetails){
        binding.textGreeting.text = userDetail.fullName
        binding.textTotalSub.text = userDetail.subCount.toString()

        val monthlySpendFormatted = String.format("%.2f", userDetail.spendingMonth)
        binding.textMonthlySpend.text = "$monthlySpendFormatted \u20BA"
        val annualSpendFormatted = String.format("%.2f", userDetail.spendingAnnual )
        binding.textAnnualSpend.text = "$annualSpendFormatted \u20BA"
    }

    private fun showLogoutConfirmDialog(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Çıkış Yap")
        builder.setMessage("Çıkış yapmak istediğinizden emin misiniz?")

        builder.setPositiveButton("Evet") { dialog, which ->
            auth.signOut()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        builder.setNegativeButton("Hayır") { dialog, which ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}