package com.acm431proje.hesapp.View.Main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.acm431proje.hesapp.Adapter.PlansAdapter
import com.acm431proje.hesapp.Model.Plan
import com.acm431proje.hesapp.R
import com.acm431proje.hesapp.ViewModel.PlansViewModel
import com.acm431proje.hesapp.databinding.ActivityPlansBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class PlansActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlansBinding
    private lateinit var serviceName: String

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private lateinit var viewModel : PlansViewModel

    private var plansAdapter: PlansAdapter? = null

    private var clickedPlanName: String ?= null
    private var clickedPlanPrice: String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlansBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        currentUser = auth.currentUser
        firestore = Firebase.firestore

        serviceName = intent.getStringExtra("serviceName").toString()
        binding.textServiceName.text = serviceName
        setServiceImage()

        binding.recyclerPlansView.layoutManager = LinearLayoutManager(this)

        plansAdapter = PlansAdapter(arrayListOf()) { clickedPlan ->
            selectClickedPlan(clickedPlan)
        }
        binding.recyclerPlansView.adapter = plansAdapter

        viewModel = ViewModelProvider(this)[PlansViewModel::class.java]
        viewModel.refreshData(serviceName)

        observeLiveData()


        binding.btnSelect.setOnClickListener {

            if (clickedPlanName != null && clickedPlanPrice != null){
                CoroutineScope(Dispatchers.Main).launch {
                    val userEmail = auth.currentUser?.email

                    if (userEmail != null) {
                        val isSuccess = viewModel.addServiceToUser(userEmail, serviceName, clickedPlanName!!, clickedPlanPrice!!)
                            if (isSuccess){
                                Toast.makeText(this@PlansActivity, "Abonelik başarıyla eklendi.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@PlansActivity, "Abonelik eklenirken hata oluştu!", Toast.LENGTH_SHORT).show()
                            }
                    }
                    HomeFragment.isChangedUserPlans = true
                    finish()
                }
            } else {
                Toast.makeText(this@PlansActivity, "Bir plan seçiniz!", Toast.LENGTH_LONG).show()
            }
        }

        binding.imageBackToServiceScreen.setOnClickListener {
            backToServiceScreen()
        }
    }

    private fun observeLiveData(){
        viewModel.plans.observe(this, Observer { plans ->
            plans?.let {
                plansAdapter?.updateData(plans)
            }
        })

        viewModel.plansError.observe(this, Observer { error->
            error?.let {
                if(it) {
                    binding.textError.visibility = View.VISIBLE
                } else {
                    binding.textError.visibility = View.GONE
                }
            }
        })

        viewModel.plansLoading.observe(this, Observer { loading->
            loading?.let {
                if (it) {
                    binding.plansLoading.visibility = View.VISIBLE
                    binding.recyclerPlansView.visibility = View.INVISIBLE
                    binding.textError.visibility = View.INVISIBLE
                } else {
                    binding.plansLoading.visibility = View.GONE
                }
            }
        })
    }


    private fun selectClickedPlan(plan: Plan){
        clickedPlanName = plan.name
        clickedPlanPrice = plan.price
    }

    private fun setServiceImage(){
        val imageName = serviceName.replace(" ", "").replace("+", "").lowercase()

        val resourceId = resources.getIdentifier(imageName, "drawable", packageName)
        if (resourceId != 0) {
            binding.imageService.setImageResource(resourceId)
        } else {
            binding.imageService.setImageResource(R.drawable.no_image)
        }
    }

    private fun backToServiceScreen(){
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Uyarı")
        builder.setMessage("Geri dönmek istediğinizden emin misiniz?")

        builder.setPositiveButton("Evet") { dialog, which ->
            finish()
        }
        builder.setNegativeButton("Hayır") { dialog, which ->
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}