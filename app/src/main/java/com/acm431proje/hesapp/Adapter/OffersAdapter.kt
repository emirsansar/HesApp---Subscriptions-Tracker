package com.acm431proje.hesapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.acm431proje.hesapp.Model.Offer
import com.acm431proje.hesapp.databinding.RowOffersBinding
import com.acm431proje.hesapp.R

class OffersAdapter(private val offersList: ArrayList<Offer>): RecyclerView.Adapter<OffersAdapter.RowHolder>() {

    class RowHolder(val binding: RowOffersBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
        val binding = RowOffersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RowHolder(binding)
    }

    override fun getItemCount(): Int {
        return offersList.size
    }

    override fun onBindViewHolder(holder: RowHolder, position: Int) {
        val currentOffer = offersList[position]

        holder.binding.textOfferInfo.text = currentOffer.offerText

        val companyName = currentOffer.company

        val imageName = "offer_" + companyName.replace(" ", "").lowercase()

        val resourceId = holder.itemView.context.resources.getIdentifier(imageName, "drawable", holder.itemView.context.packageName)
        if (resourceId != 0) {
            holder.binding.companyIcon.setImageResource(resourceId)
        }
        else {
            holder.binding.companyIcon.setImageResource(R.drawable.no_image)
        }
    }

    fun updateData(newList: List<Offer>){
        offersList.clear()
        offersList.addAll(newList)
        notifyDataSetChanged()
    }


}