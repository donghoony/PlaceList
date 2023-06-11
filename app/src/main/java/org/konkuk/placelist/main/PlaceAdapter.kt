package org.konkuk.placelist.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.konkuk.placelist.PlacesListDatabase
import org.konkuk.placelist.databinding.PlacesRowBinding
import org.konkuk.placelist.domain.Place

class PlaceAdapter(private val db: PlacesListDatabase, var items : ArrayList<Place>) : RecyclerView.Adapter<PlaceAdapter.ViewHolder>(){

    interface OnItemClickListener{
        fun onItemClick(data: Place, pos: Int)
    }
    var itemClickListener : OnItemClickListener? = null
    inner class ViewHolder(val binding: PlacesRowBinding) : RecyclerView.ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener{
                itemClickListener?.onItemClick(items[adapterPosition], adapterPosition)
            }
        }
    }
    fun removeItem(pos: Int) {
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        CoroutineScope(Dispatchers.IO).launch{
            items = db.placesDao().getAll() as ArrayList<Place>
            withContext(Dispatchers.Main){
                notifyItemRangeChanged(0, items.size)
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

    fun addPlace(id : Int, name: String, latitude: String, longitude: String, radius: Float) {
        CoroutineScope(Dispatchers.IO).launch{
            db.placesDao().insertAll(Place(0, name, latitude, longitude, radius))
            items = db.placesDao().getAll() as ArrayList<Place>
            withContext(Dispatchers.Main){
                notifyItemInserted(items.size)
            }
        }
    }

    fun refresh() {
        CoroutineScope(Dispatchers.IO).launch{
            items = db.placesDao().getAll() as ArrayList<Place>
            withContext(Dispatchers.Main){
                notifyItemRangeChanged(0, items.size)
            }
        }
    }
}