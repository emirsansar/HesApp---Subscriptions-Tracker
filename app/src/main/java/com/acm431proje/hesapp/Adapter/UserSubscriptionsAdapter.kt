package com.acm431proje.hesapp.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.acm431proje.hesapp.Model.UserSubscription
import com.acm431proje.hesapp.R
import com.acm431proje.hesapp.databinding.RowUsersubscriptionsBinding

class UserSubscriptionsAdapter(private val ownedServicesList: ArrayList<UserSubscription>, )
    : RecyclerView.Adapter<UserSubscriptionsAdapter.RowOwnedHolder>() {

    class RowOwnedHolder(val binding: RowUsersubscriptionsBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    val colors: Array<String> = arrayOf("#a295cf","#8c7cbf")

    var selectedPosition: Int = RecyclerView.NO_POSITION


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowOwnedHolder {
        val binding = RowUsersubscriptionsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RowOwnedHolder(binding)
    }

    override fun getItemCount(): Int {
        return ownedServicesList.count()
    }

    override fun onBindViewHolder(holder: RowOwnedHolder, position: Int) {
        val currentService = ownedServicesList.get(position)

        val serviceName = currentService.serviceName
        val planName = currentService.planName
        val planPrice = currentService.planPrice


        holder.binding.textServiceName.text = serviceName
        holder.binding.textPlanName.text = planName
        holder.binding.textPlanPrice.text = planPrice.toString() + " \u20BA"

        holder.binding.linearLayout.setBackgroundColor(Color.parseColor(colors[position%2]))

        var imagename = "${(serviceName.replace(" ", "").replace("+", "")).lowercase()}"

        val resourceId = holder.itemView.context.resources.getIdentifier(imagename, "drawable", holder.itemView.context.packageName)
        if (resourceId != 0) {
            holder.binding.imageService.setImageResource(resourceId)
        } else {
            holder.binding.imageService.setImageResource(R.drawable.no_image)
        }


        holder.binding.checkBox.isChecked = (selectedPosition == position)

        holder.binding.checkBox.setOnClickListener {
            if (selectedPosition != position) {

                selectedPosition = position

                notifyDataSetChanged()
            }
        }
    }
}