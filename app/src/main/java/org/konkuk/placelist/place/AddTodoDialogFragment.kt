package org.konkuk.placelist.place

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ToggleButton
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import org.konkuk.placelist.R
import org.konkuk.placelist.databinding.FragmentAddTodoBinding
import org.konkuk.placelist.domain.Todo
import org.konkuk.placelist.domain.enums.PlaceSituation
import org.konkuk.placelist.domain.enums.TodoPriority
import android.os.Bundle as Bundle1


class AddTodoDialogFragment : DialogFragment() {
    lateinit var binding: FragmentAddTodoBinding
    lateinit var repeatToggleButtons: Array<ToggleButton>
    lateinit var addTodoListener: AddTodoListener
    var todo: Todo? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle1?
    ): View {
        binding = FragmentAddTodoBinding.inflate(inflater, container, false)
        dialog?.window?.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
        with(dialog?.window?.attributes) {
            this?.width = ViewGroup.LayoutParams.MATCH_PARENT
            this?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            this?.verticalMargin = 0f
            this?.horizontalMargin = 0f
        }
        addTodoListener = context as AddTodoListener
        initButtons()
        if (arguments != null){
            todo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                arguments?.getSerializable("todo", Todo::class.java)!!
            else arguments?.getSerializable("todo") as Todo
            binding.todoname.setText(todo?.name)
            binding.outToggleBtn.isChecked = (todo?.situation == PlaceSituation.BOTH) or (todo?.situation == PlaceSituation.ESCAPE)
            binding.inToggleButton.isChecked = (todo?.situation == PlaceSituation.BOTH) or (todo?.situation == PlaceSituation.ENTER)
            val array = arrayOf(binding.sun, binding.mon, binding.tue, binding.wed, binding.thu, binding.fri, binding.sat)
            for(i in 0 .. 6) array[i].isChecked = ((todo!!.repeatDays) and (1 shl i)) == (1 shl i)
        }
        return binding.root
    }

    private fun initButtons() {
        with(binding) {
            repeatToggleButtons = arrayOf(sun, mon, tue, wed, thu, fri, sat)

            repeatSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                repeatLayout.visibility = if (isChecked) View.VISIBLE else  View.GONE
            }

            inToggleButton.setOnCheckedChangeListener { btn, isChecked ->
                if (isChecked) btn.background = ResourcesCompat.getDrawable(resources,
                    R.drawable.toggle_on, null)
                else btn.background = ResourcesCompat.getDrawable(resources,
                    R.drawable.toggle_off, null)
            }
            outToggleBtn.setOnCheckedChangeListener { btn, isChecked ->
                if (isChecked) btn.background = ResourcesCompat.getDrawable(resources,
                    R.drawable.toggle_on, null)
                else btn.background = ResourcesCompat.getDrawable(resources,
                    R.drawable.toggle_off, null)
            }

            closeBtn.setOnClickListener {
                dismiss()
            }
            submitBtn.setOnClickListener {
                if(binding.todoname.text.isBlank()) {
                    binding.todoname.setHintTextColor(resources.getColor(R.color.red, null))
                    return@setOnClickListener
                }
                val placeSituation = if (inToggleButton.isChecked and outToggleBtn.isChecked) PlaceSituation.BOTH else (if (inToggleButton.isChecked) PlaceSituation.ENTER else PlaceSituation.ESCAPE)
                var repeatValue = 0
                for(i in 0..6){
                    if (!repeatToggleButtons[i].isChecked) continue
                    repeatValue = repeatValue or (1 shl i)
                }
                // 일요일부터 2진수로 7개 (가장 오른쪽이 일요일) -> 1111111(2) : 모두 반복, 0000001 : 토요일만 반복
                var todoId = 0L
                if (todo != null) todoId = todo!!.id
                if (!binding.repeatSwitch.isChecked) repeatValue = 0
                addTodoListener.update(Todo(todoId, addTodoListener.getTodosPlaceId(), todoname.text.toString(), false, TodoPriority.MEDIUM, repeatValue, placeSituation))
                dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        context?.dialogFragmentResize(1f, 0.5f)
        val window = dialog!!.window
        window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }
    companion object{
        fun toInstance(todo: Todo) : AddTodoDialogFragment {
            val obj = AddTodoDialogFragment()
            val args = Bundle1()
            args.putSerializable("todo", todo)
            obj.arguments = args
            return obj
        }
    }
    private fun Context.dialogFragmentResize(w: Float, h: Float) {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rect = windowManager.currentWindowMetrics.bounds
        val window = dialog?.window
        val x = (rect.width() * w).toInt()
        val y = (rect.height() * h).toInt()
        window?.setLayout(x, y)
    }
}