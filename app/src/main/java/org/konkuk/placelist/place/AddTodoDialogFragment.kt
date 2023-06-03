package org.konkuk.placelist.place

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import org.konkuk.placelist.R
import org.konkuk.placelist.databinding.FragmentAddTodoBinding
import org.konkuk.placelist.domain.Todo
import org.konkuk.placelist.domain.enums.PlaceSituation
import org.konkuk.placelist.domain.enums.TodoPriority
import android.os.Bundle as Bundle1


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class AddTodoDialogFragment : DialogFragment() {
    lateinit var binding: FragmentAddTodoBinding
    var repeat = arrayOf(false, false, false, false, false, false, false)
    lateinit var addTodoListener : AddTodoListener

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
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        context?.dialogFragmentResize(1f, 0.4f)

        val window = dialog!!.window
//        window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }

    private fun initButtons() {
        with(binding) {
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

            timeonoff.setOnCheckedChangeListener { buttonView, isChecked ->

            }

            closeBtn.setOnClickListener {
                dismiss()
            }
            submitBtn.setOnClickListener {
                val placeSituation = if (inToggleButton.isChecked and outToggleBtn.isChecked) PlaceSituation.BOTH else (if (inToggleButton.isChecked) PlaceSituation.ENTER else PlaceSituation.ESCAPE)
                addTodoListener.update(Todo(0, addTodoListener.getTodosPlaceId(), todoname.text.toString(), false, TodoPriority.MEDIUM, placeSituation, 0.0))
                dismiss()
            }
        }
    }


    private fun Context.dialogFragmentResize(w: Float, h: Float) {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT < 30) {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val window = dialog?.window
            val x = (size.x * w).toInt()
            val y = (size.y * h).toInt()
            window?.setLayout(x, y)
        } else {
            val rect = windowManager.currentWindowMetrics.bounds
            val window = dialog?.window
            val x = (rect.width() * w).toInt()
            val y = (rect.height() * h).toInt()
            window?.setLayout(x, y)
        }
    }
}