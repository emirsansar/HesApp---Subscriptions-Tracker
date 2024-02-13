package com.acm431proje.hesapp.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.acm431proje.hesapp.Model.Plan
import com.acm431proje.hesapp.Model.UserSubscription
import com.acm431proje.hesapp.R
import com.acm431proje.hesapp.databinding.RowUsersubscriptionsBinding

class UserSubscriptionsAdapter(private val ownedServicesList: ArrayList<UserSubscription>, private val onItemClick: (UserSubscription) -> Unit )
    : RecyclerView.Adapter<UserSubscriptionsAdapter.RowOwnedHolder>() {

    class RowOwnedHolder(val binding: RowUsersubscriptionsBinding) : RecyclerView.ViewHolder(binding.root) {}

    private val colors: Array<String> = arrayOf("#a295cf","#8c7cbf")

    var selectedUserSub: UserSubscription? = null
    var selectedPosition: Int = RecyclerView.NO_POSITION

    fun updateData(newList: List<UserSubscription>){
        ownedServicesList.clear()
        ownedServicesList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowOwnedHolder {
        val binding = RowUsersubscriptionsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RowOwnedHolder(binding)
    }

    override fun getItemCount(): Int {
        return ownedServicesList.count()
    }

    override fun onBindViewHolder(holder: RowOwnedHolder, position: Int) {
        val currentService = ownedServicesList[position]

        val serviceName = currentService.serviceName
        val planName = currentService.planName
        val planPrice = currentService.planPrice


        holder.binding.textServiceName.text = serviceName
        holder.binding.textPlanName.text = planName
        holder.binding.textPlanPrice.text = "%.2f ₺".format(planPrice)

        holder.binding.linearLayout.setBackgroundColor(Color.parseColor(colors[position%2]))

        val imageName = serviceName.replace(" ", "").replace("+", "").lowercase()

        val resourceId = holder.itemView.context.resources.getIdentifier(imageName, "drawable", holder.itemView.context.packageName)
        if (resourceId != 0) {
            holder.binding.imageService.setImageResource(resourceId)
        } else {
            holder.binding.imageService.setImageResource(R.drawable.no_image)
        }


        holder.binding.checkBox.isChecked = (selectedPosition == position)

        holder.binding.checkBox.setOnClickListener {
            if (selectedPosition != position) {
                selectedPosition = position

                selectedUserSub = currentService
                onItemClick(selectedUserSub!!)
            } else {
                selectedPosition = RecyclerView.NO_POSITION
                holder.binding.checkBox.isChecked = false

                onItemClick(UserSubscription("","", 0F,))
            }
            notifyDataSetChanged()
        }
    }

    fun sortByNameAscending() {
        ownedServicesList.sortBy { it.serviceName }
        notifyDataSetChanged()
    }

    fun sortByNameDescending() {
        ownedServicesList.sortByDescending { it.serviceName }
        notifyDataSetChanged()
    }

    fun sortByPriceAscending(){
        ownedServicesList.sortBy { it.planPrice }
        notifyDataSetChanged()
    }

    fun sortByPriceDescending(){
        ownedServicesList.sortByDescending { it.planPrice }
        notifyDataSetChanged()
    }
}