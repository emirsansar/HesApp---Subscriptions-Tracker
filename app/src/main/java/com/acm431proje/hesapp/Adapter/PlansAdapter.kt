package com.acm431proje.hesapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.acm431proje.hesapp.Model.Plan
import com.acm431proje.hesapp.Model.Service
import com.acm431proje.hesapp.databinding.RowPlansBinding

class PlansAdapter(private val plansList: ArrayList<Plan>, private val onItemClick: (Plan) -> Unit) : RecyclerView.Adapter<PlansAdapter.RowHolder>() {

    class RowHolder(val binding: RowPlansBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    var selectedPlan: Plan? = null
    var lastCheckedPosition = -1

    fun updateData(newList: List<Plan>) {
        plansList.clear()
        plansList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
        val binding = RowPlansBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RowHolder(binding)
    }

    override fun getItemCount(): Int {
        return plansList.count()
    }

    override fun onBindViewHolder(holder: RowHolder, position: Int) {
        val currentPlan = plansList[position]

        holder.binding.textPlanName.text = currentPlan.name
        holder.binding.textPlanPrice.text = currentPlan.price + " \u20BA"

        holder.binding.checkBox.isChecked = (position == lastCheckedPosition)

//        holder.binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
//            println(position)
//            if (isChecked) {
//                if (lastCheckedPosition != -1 && lastCheckedPosition != position) {
//                    plansList[lastCheckedPosition].isSelected = false
//                    notifyItemChanged(lastCheckedPosition)
//                }
//                selectedPlan = currentPlan
//                lastCheckedPosition = position
//                onItemClick(selectedPlan!!)
//            } else {
//                selectedPlan = null
//                lastCheckedPosition = -1
//                onItemClick(Plan("","",false))
//            }
//        }

        holder.binding.checkBox.setOnClickListener {
            if (position != RecyclerView.NO_POSITION &&  position != lastCheckedPosition) {
                if (lastCheckedPosition != -1) plansList[lastCheckedPosition].isSelected = false

                notifyItemChanged(lastCheckedPosition)

                lastCheckedPosition = position

                selectedPlan = currentPlan
                onItemClick(selectedPlan!!)
            }
            else if (position == lastCheckedPosition) {
                holder.binding.checkBox.isChecked = false
                selectedPlan = null
                lastCheckedPosition = -1
                onItemClick(Plan("", "", false))
            }
        }

    }




}