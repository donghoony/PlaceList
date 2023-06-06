package org.konkuk.placelist.place

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.konkuk.placelist.PlacesListDatabase
import org.konkuk.placelist.databinding.PlacesRowBinding
import org.konkuk.placelist.domain.Todo

class TodoAdapter(private val db: PlacesListDatabase, var items : ArrayList<Todo>, private val placeId: Int) : RecyclerView.Adapter<TodoAdapter.ViewHolder>(){

    interface OnItemClickListener{
        fun onItemClick(data: Todo, pos: Int)
    }
    var itemClickListener : OnItemClickListener? = null
    inner class ViewHolder(val binding: PlacesRowBinding) : RecyclerView.ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener{
                itemClickListener?.onItemClick(items[adapterPosition], adapterPosition)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        CoroutineScope(Dispatchers.IO).launch{
            items = db.TodoDao().findTodoByPlaceId(placeId) as ArrayList<Todo>
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
            val todo = items[position]
            this.nameField.text = todo.name
        }
    }

    fun addTodo(todo: Todo) {
        CoroutineScope(Dispatchers.IO).launch{
            db.TodoDao().insert(todo)
            items = db.TodoDao().findTodoByPlaceId(placeId) as ArrayList<Todo>
            for(i in items){
                Log.i("ITEM", i.name)
            }
            withContext(Dispatchers.Main){
                notifyItemInserted(items.size)
            }
        }
    }
}