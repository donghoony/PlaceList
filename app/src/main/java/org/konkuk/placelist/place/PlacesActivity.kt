package org.konkuk.placelist.place

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.konkuk.placelist.PlacesListDatabase
import org.konkuk.placelist.databinding.ActivityPlacesBinding
import org.konkuk.placelist.domain.Todo

class PlacesActivity : AppCompatActivity(), AddTodoListener {
    lateinit var binding: ActivityPlacesBinding
    var placeId = 0
    lateinit var todoAdapter: TodoAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesBinding.inflate(layoutInflater)
        val name = intent.getStringExtra("name")
        placeId = intent.getIntExtra("id", 0)
        binding.name.text = name
        setContentView(binding.root)
        init()
        initTodo()
    }
    private fun initTodo() {
        val db = PlacesListDatabase.getDatabase(this)
        binding.todolist.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        CoroutineScope(Dispatchers.IO).launch{
            val items = db.TodoDao().findTodoByPlaceId(placeId) as ArrayList<Todo>
            todoAdapter = TodoAdapter(db, items, placeId)
            todoAdapter.itemClickListener = object : TodoAdapter.OnItemClickListener {
                override fun onItemClick(data: Todo, pos: Int) {
                    Toast.makeText(this@PlacesActivity,
                        "${data.name}",
                        Toast.LENGTH_SHORT).show()

                }
            }
            binding.todolist.adapter = todoAdapter
        }
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                CoroutineScope(Dispatchers.IO).launch{
                    db.TodoDao().delete(todoAdapter.items[viewHolder.adapterPosition])
                    withContext(Dispatchers.Main) {
                        todoAdapter.removeItem(viewHolder.adapterPosition)
                    }
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.todolist)
    }
    private fun init(){
        binding.backBtn.setOnClickListener { finish() }
        binding.btnPlus.setOnClickListener {
            AddTodoDialogFragment().show(supportFragmentManager, "AddTodo")
        }


    }

    override fun update(todo: Todo) {
        todoAdapter.addTodo(todo)
    }

    override fun getTodosPlaceId(): Int {
        return placeId
    }
}