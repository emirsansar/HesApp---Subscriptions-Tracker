package com.acm431proje.hesapp.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.acm431proje.hesapp.R
import com.acm431proje.hesapp.databinding.RowServicesBinding
import com.acm431proje.hesapp.Model.Service


class ServicesAdapter(private val serviceList: ArrayList<Service>, private val listener: Listener) : RecyclerView.Adapter<ServicesAdapter.RowHolder>() {

    class RowHolder(val binding: RowServicesBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    private val colors: Array<String> = arrayOf("#a295cf","#8c7cbf")

    interface Listener {
        fun onItemClick(service: Service, position: Int)
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

        //holder.binding.textServiceName.setText(currentService.name)

        holder.itemView.setOnClickListener {
            listener.onItemClick(currentService, position)
        }

        val currServiceName = currentService.name
        val imageName = "service_" + "${(currServiceName.replace(" ", "").replace("+", "")).lowercase()}"

        val resourceId = holder.itemView.context.resources.getIdentifier(imageName, "drawable", holder.itemView.context.packageName)
        if (resourceId != 0) {
            holder.binding.imageViewService.setImageResource(resourceId)
        } else {
            holder.binding.imageViewService.setImageResource(R.drawable.no_image)
        }

        //holder.binding.linearLayout.setBackgroundColor(Color.parseColor(colors.get(position%2)))
    }
}