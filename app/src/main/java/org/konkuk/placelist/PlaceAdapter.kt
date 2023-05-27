package org.konkuk.placelist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.konkuk.placelist.databinding.PlacesRowBinding
import org.konkuk.placelist.domain.Place

class PlaceAdapter : RecyclerView.Adapter<PlaceAdapter.ViewHolder>(){
    val items = ArrayList<Place>()
    interface OnItemClickListener{
        fun onItemClick(data: Place, pos: Int)
    }
    val itemClickListener : OnItemClickListener? = null
    inner class ViewHolder(val binding: PlacesRowBinding) : RecyclerView.ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener{
                itemClickListener?.onItemClick(items[adapterPosition], adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PlacesRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            val place = items[position]
            this.nameField.text = place.name
        }
    }
}