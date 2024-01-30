package com.acm431proje.hesapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.acm431proje.hesapp.Model.Plan
import com.acm431proje.hesapp.databinding.RowPlansBinding


class PlansAdapter(private val plansList: ArrayList<Plan>, private val onItemClick: (Plan) -> Unit)
    : RecyclerView.Adapter<PlansAdapter.RowHolder>() {

    class RowHolder(val binding: RowPlansBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    var selectedPlan: Plan? = null
    var lastCheckedPosition = -1 // Son kontrol edilen pozisyon


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
        val binding = RowPlansBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RowHolder(binding)
    }

    override fun getItemCount(): Int {
        return plansList.count()
    }

    override fun onBindViewHolder(holder: RowHolder, position: Int) {
        val currentPlan = plansList.get(position)

        holder.binding.textPlanName.text = currentPlan.name
        holder.binding.textPlanPrice.text = currentPlan.price + " \u20BA"

        holder.binding.checkBox.isChecked = (position == lastCheckedPosition)

        holder.binding.checkBox.setOnClickListener {
            lastCheckedPosition = position
            selectedPlan = currentPlan

            for (i in plansList.indices) {
                if (i != position) {
                    plansList[i].isSelected = false
                }
            }

            notifyDataSetChanged()

            onItemClick(currentPlan)
        }
    }
}