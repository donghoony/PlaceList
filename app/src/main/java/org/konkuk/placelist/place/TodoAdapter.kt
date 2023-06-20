package org.konkuk.placelist.place

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.konkuk.placelist.PlacesListDatabase
import org.konkuk.placelist.databinding.TodoRowBinding
import org.konkuk.placelist.domain.Todo

class TodoAdapter(private val db: PlacesListDatabase, var items : ArrayList<Todo>, private val placeId: Long) : RecyclerView.Adapter<TodoAdapter.ViewHolder>(){

    interface OnItemClickListener{
        fun onItemClick(data: Todo, pos: Int)
        fun onItemCheck(data: Todo, pos: Int, isChecked: Boolean)
    }
    var itemClickListener : OnItemClickListener? = null
    inner class ViewHolder(val binding: TodoRowBinding) : RecyclerView.ViewHolder(binding.root){
        init{
            binding.checkbox.setOnClickListener {
                itemClickListener?.onItemCheck(items[adapterPosition], adapterPosition, binding.checkbox.isChecked)
                binding.todoNameText.paintFlags = if (binding.checkbox.isChecked) Paint.STRIKE_THRU_TEXT_FLAG else 0
            }
            binding.todoNameText.setOnClickListener {
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
            items = db.TodoDao().findByPlaceId(placeId) as ArrayList<Todo>
            withContext(Dispatchers.Main){
                notifyItemRangeChanged(0, items.size)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(TodoRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            val todo = items[position]
            this.todoNameText.text = todo.name
            this.checkbox.isChecked = todo.isCompleted
            this.todoNameText.paintFlags = if (todo.isCompleted) Paint.STRIKE_THRU_TEXT_FLAG else 0
        }
    }

    fun addTodo(todo: Todo) {
        CoroutineScope(Dispatchers.IO).launch{
            db.TodoDao().insert(todo)
            items = db.TodoDao().findByPlaceId(placeId) as ArrayList<Todo>
            withContext(Dispatchers.Main){
                notifyItemRangeChanged(0, items.size)
            }
        }
    }
}