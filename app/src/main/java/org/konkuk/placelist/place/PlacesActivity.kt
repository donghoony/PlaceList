package org.konkuk.placelist.place

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.konkuk.placelist.PlacesListDatabase
import org.konkuk.placelist.databinding.ActivityPlacesBinding
import org.konkuk.placelist.domain.Place
import org.konkuk.placelist.domain.Todo
import org.konkuk.placelist.main.AddPlaceDialogFragment
import org.konkuk.placelist.main.AddPlaceListener

class PlacesActivity : AppCompatActivity(), AddTodoListener, AddPlaceListener {
    lateinit var binding: ActivityPlacesBinding
    lateinit var place : Place
    lateinit var todoAdapter: TodoAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesBinding.inflate(layoutInflater)

        place = intent.getSerializableExtra("place", Place::class.java)!!
        binding.name.text = place.name
        setContentView(binding.root)
        init()
        initTodo()
    }
    private fun initTodo() {
        val db = PlacesListDatabase.getDatabase(this)
        binding.todolist.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        CoroutineScope(Dispatchers.IO).launch{
            val items = db.TodoDao().findTodoByPlaceId(place.id) as ArrayList<Todo>
            todoAdapter = TodoAdapter(db, items, place.id)
            todoAdapter.itemClickListener = object : TodoAdapter.OnItemClickListener {
                override fun onItemClick(data: Todo, pos: Int) {
                    Toast.makeText(this@PlacesActivity,
                        data.name,
                        Toast.LENGTH_SHORT).show()

                }
            }
            binding.todolist.adapter = todoAdapter
        }
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT
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
        binding.editBtn.setOnClickListener {
            AddPlaceDialogFragment.toInstance(place).show(
                supportFragmentManager, "EditPlace"
            )
        }
    }

    override fun update(todo: Todo) {
        todoAdapter.addTodo(todo)
    }

    override fun getTodosPlaceId(): Int {
        return place.id
    }

    override fun addPlace(id: Int, name: String, coordinate: LatLng, radius: Float) {
        // editPlace
        val db = PlacesListDatabase.getDatabase(this)
        val updatedPlace = Place(id, name, coordinate.latitude, coordinate.longitude, radius)
        CoroutineScope(Dispatchers.IO).launch {
            db.placesDao().update(updatedPlace)
        }
        this@PlacesActivity.place = updatedPlace
        binding.name.text = name
    }
}