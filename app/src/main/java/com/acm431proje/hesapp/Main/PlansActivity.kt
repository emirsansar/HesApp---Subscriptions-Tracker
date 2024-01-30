package com.acm431proje.hesapp.Main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.acm431proje.hesapp.Adapter.PlansAdapter
import com.acm431proje.hesapp.Model.Plan
import com.acm431proje.hesapp.R
import com.acm431proje.hesapp.databinding.ActivityPlansBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.hashMapOf as hashMapOf1
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class PlansActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlansBinding
    private lateinit var serviceName: String

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private lateinit var plansList: ArrayList<Plan>
    private var feedPlanAdapter: PlansAdapter? = null

    private lateinit var layoutManager: LinearLayoutManager

    private var clickedPlanName: String ?= null
    private var clickedPlanPrice: String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlansBinding.inflate(layoutInflater)
        setContentView(binding.root)

        serviceName = intent.getStringExtra("serviceName").toString()
        binding.textServiceName.text = serviceName

        auth = Firebase.auth
        firestore = Firebase.firestore
        currentUser = auth.currentUser

        layoutManager = LinearLayoutManager(this@PlansActivity)
        binding.recyclerView.layoutManager = layoutManager

        plansList = ArrayList()
        getPlans(serviceName)

        feedPlanAdapter = PlansAdapter(plansList) { clickedPlan ->
            selectClickedPlan(clickedPlan)
        }
        binding.recyclerView.adapter = feedPlanAdapter

        setServiceImage()


        binding.btnSelect.setOnClickListener {

            if (clickedPlanName != null && clickedPlanPrice != null){
                CoroutineScope(Dispatchers.Main).launch {

                    val userEmail = auth.currentUser?.email
                    if (userEmail != null) {
                        addServiceToUser(userEmail, serviceName, clickedPlanName!!, clickedPlanPrice!!)
                    }

                    finish()

                    HomeFragment.isChangedUserPlans = true
                }
            } else {
                Toast.makeText(this@PlansActivity, "Bir plan seçiniz!", Toast.LENGTH_LONG).show()
            }
        }

        binding.imageBackToServiceScreen.setOnClickListener {
            backToServiceScreen()
        }
    }


    private fun getPlans(serviceName: String) {
        val serviceDocRef = firestore.collection("services").document(serviceName)

        serviceDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val planData = documentSnapshot.data

                    // planData içindeki tüm alanları döngü ile gez
                    if (planData != null) {
                        for ((fieldName, fieldValue) in planData) {
                            // Eğer bu alan bir String ise ve değeri null değilse, Plan nesnesine ekle
                            if (fieldValue is String && fieldValue != null && fieldName != "Tür") {
                                val plan = Plan(fieldName, fieldValue, false)
                                plansList.add(plan)
                            }
                        }
                    }

                    plansList?.sortBy { it.price }

                    feedPlanAdapter?.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "Service not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching plans: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectClickedPlan(plan: Plan){
        clickedPlanName = plan.name
        clickedPlanPrice = plan.price
    }

    private suspend fun addServiceToUser(userMail: String, serviceName: String, planName: String, planPrice: String) {

        try {
            val userDocRef = firestore.collection("usersubscriptions").document(userMail)
            val userDocSnapshot = userDocRef.get().await()

            if (!userDocSnapshot.exists()) {
                // Belirtilen userMail ile doküman bulunamazsa, dokümanı oluşturur
                val userData = hashMapOf1<String, Any>()
                userDocRef.set(userData).await()
            }

            val existingServiceName = userDocSnapshot.getString(serviceName)

            if (existingServiceName == null) {
                // Servis adı ile eşleşen bir alan yok, yeni bir alan ekler
                userDocRef.update(serviceName, serviceName).await()

                val subscriptionDocRef = userDocRef.collection(serviceName).document("subsinfo")
                val subscriptionDocSnapshot = subscriptionDocRef.get().await()

                if (!subscriptionDocSnapshot.exists()) {
                    // Abonelik dokümanı yoksa oluşturur ve ekler

                    val planPriceNumber: Number = try {
                        planPrice.toFloat()  // String'i Number'a dönüştür
                    } catch (e: NumberFormatException) {
                        0  // dönüştüremezse default değer
                    }

                    val subscriptionData = hashMapOf1(
                        "serviceName" to serviceName,
                        "planName" to planName,
                        "planPrice" to planPriceNumber
                    )

                    subscriptionDocRef.set(subscriptionData).await()

                    Toast.makeText(this@PlansActivity, "Abonelik başarıyla eklendi!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PlansActivity, "'$serviceName' aboneliğiniz zaten bulunmaktadır.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@PlansActivity, "'$serviceName' zaten kullanımda.", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            println("Hata: $e")
        }
    }



    private fun setServiceImage(){
        var imageName = "${(serviceName.replace(" ", "").replace("+", "")).lowercase()}"

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