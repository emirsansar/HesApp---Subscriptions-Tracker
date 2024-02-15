package com.acm431proje.hesapp.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.acm431proje.hesapp.View.Main.PlansActivity
import com.acm431proje.hesapp.R
import com.acm431proje.hesapp.databinding.RowServicesBinding
import com.acm431proje.hesapp.Model.Service

class ServicesAdapter(private val serviceList: ArrayList<Service>) : RecyclerView.Adapter<ServicesAdapter.RowHolder>() {

    class RowHolder(val binding: RowServicesBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    fun updateData(newList: List<Service>) {
        serviceList.clear()
        serviceList.addAll(newList)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
        val binding = RowServicesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RowHolder(binding)
    }

    override fun getItemCount(): Int {
        return serviceList.count()
    }

    override fun onBindViewHolder(holder: RowHolder, position: Int) {
        val currentService = serviceList.get(position)

        val currServiceName = currentService.name
        val imageName = "service_" + currServiceName.replace(" ", "").replace("+", "").lowercase()

        val resourceId = holder.itemView.context.resources.getIdentifier(imageName, "drawable", holder.itemView.context.packageName)
        if (resourceId != 0) {
            holder.binding.imageViewService.setImageResource(resourceId)
        } else {
            holder.binding.imageViewService.setImageResource(R.drawable.no_image)
        }


        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, PlansActivity::class.java)
            intent.putExtra("serviceName", currentService.name)
            holder.itemView.context.startActivity(intent)
        }

    }

    fun sortByNameAscending() {
        serviceList.sortBy { it.name }
        notifyDataSetChanged()
    }

    fun sortByNameDescending() {
        serviceList.sortByDescending { it.name }
        notifyDataSetChanged()
    }

}