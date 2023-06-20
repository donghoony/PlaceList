package org.konkuk.placelist.place

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.color.MaterialColors.ALPHA_FULL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.konkuk.placelist.MyGeofence
import org.konkuk.placelist.PlacesListDatabase
import org.konkuk.placelist.databinding.ActivityPlacesBinding
import org.konkuk.placelist.domain.Place
import org.konkuk.placelist.domain.Todo
import org.konkuk.placelist.main.AddPlaceDialogFragment
import org.konkuk.placelist.main.AddPlaceListener
import kotlin.math.abs

class PlacesActivity : AppCompatActivity(), AddTodoListener, AddPlaceListener {
    lateinit var binding: ActivityPlacesBinding
    lateinit var place : Place
    lateinit var todoAdapter: TodoAdapter
    lateinit var geo: MyGeofence

    private fun updateVisibility(force: Boolean = false){
        if (todoAdapter.items.isNotEmpty() or force){
            binding.helpText1.visibility = View.GONE
            binding.helpText2.visibility = View.GONE
        }
        else{
            binding.helpText1.visibility = View.VISIBLE
            binding.helpText2.visibility = View.VISIBLE
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesBinding.inflate(layoutInflater)

        place = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra("place", Place::class.java)!!
        else intent.getSerializableExtra("place") as Place

        geo = MyGeofence.getInstance(applicationContext)
        binding.name.text = place.name
        setContentView(binding.root)
        init()
        initTodo()
    }
    private fun initTodo() {
        val db = PlacesListDatabase.getDatabase(this)
        binding.todolist.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        CoroutineScope(Dispatchers.IO).launch{
            val items = db.TodoDao().findByPlaceId(place.id) as ArrayList<Todo>
            todoAdapter = TodoAdapter(db, items, place.id)
            updateVisibility()
            todoAdapter.itemClickListener = object : TodoAdapter.OnItemClickListener {
                override fun onItemClick(data: Todo, pos: Int) {
                    // 수정은 여기에서 진행해야 함
                    // DialogFragment으로 Todo 넘겨주기
                    AddTodoDialogFragment.toInstance(data).show(supportFragmentManager, "EditTodo")
                }

                override fun onItemCheck(data: Todo, pos: Int, isChecked: Boolean) {
                    val db = PlacesListDatabase.getDatabase(this@PlacesActivity)
                    //update isCompleted
                    val updatedTodo = Todo(data.id, data.placeId, data.name, isChecked, data.priority, data.repeatDays, data.situation)
                    CoroutineScope(Dispatchers.IO).launch {
                        db.TodoDao().update(updatedTodo)
                    }
                }
            }
            binding.todolist.adapter = todoAdapter
        }
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {return true}
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val p = Paint()
                    var icon: Bitmap
                    if (dX < 0) {
                        icon = BitmapFactory.decodeResource(resources, org.konkuk.placelist.R.drawable.btn_trash_1)
                        val h = abs((itemView.top - itemView.bottom) * 2 / 3)
                        val w = h*2/3
                        icon = Bitmap.createScaledBitmap(icon, w, h, false)
                        p.color = Color.parseColor("#FF5959")
                        c.drawRoundRect(itemView.right.toFloat()-20 + dX, itemView.top.toFloat() + 5, itemView.right.toFloat(), itemView.bottom.toFloat() - 10, 10f, 10f, p)
                        c.drawBitmap(icon, itemView.right.toFloat() - w - 20, itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - h + 10) / 2, p)
                    }
                    val alpha = ALPHA_FULL - abs(dX) / viewHolder.itemView.width.toFloat()
                    viewHolder.itemView.alpha = alpha
                    viewHolder.itemView.translationX = dX
                }
                else super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                CoroutineScope(Dispatchers.IO).launch{
                    db.TodoDao().delete(todoAdapter.items[viewHolder.adapterPosition])
                    withContext(Dispatchers.Main) {
                        todoAdapter.removeItem(viewHolder.adapterPosition)
                        updateVisibility()
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
        updateVisibility(true)
    }

    override fun getTodosPlaceId(): Long {
        return place.id
    }

    override fun addPlace(id: Long, name: String, latitude: String, longitude: String, radius: Float) {
        // editPlace
        val db = PlacesListDatabase.getDatabase(this)
        val updatedPlace = Place(id, name, latitude, longitude, radius)
        CoroutineScope(Dispatchers.IO).launch {
            db.placesDao().update(updatedPlace)
        }
        geo.removeGeofence(id)
        geo.addGeofence(id, LatLng(latitude.toDouble(), longitude.toDouble()), radius)
        this@PlacesActivity.place = updatedPlace
        binding.name.text = name
        updateVisibility(true)
    }
}