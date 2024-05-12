package com.mpcs.taskme.edittask

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.mpcs.taskme.DatePickerFragment
import android.text.format.DateFormat
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialArcMotion

import com.mpcs.taskme.R
import com.mpcs.taskme.listtasks.DATE_FORMAT
import com.mpcs.taskme.models.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

private const val ARG_TASK_ID = "task_id"
private const val DIALOG_DATE = "DateDialog"
private const val RETURN_DATE = 0
private const val NO_TITLE_TEXT = "Add a task title to save."

class EditFragment : Fragment(), DatePickerFragment.Callbacks {

    private lateinit var task: Task

    private lateinit var titleInput: com.google.android.material.textfield.TextInputEditText
    private lateinit var descInput: com.google.android.material.textfield.TextInputEditText
    private lateinit var dateTextView: TextView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var editTaskLayout: ConstraintLayout
    private lateinit var prioritySelect: TextView
    private lateinit var priorityIndicator: ImageView


    private val editFragmentViewModel: EditFragmentViewModel by lazy {
        ViewModelProvider(this)[EditFragmentViewModel::class.java]
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit, container, false)

        titleInput = view.findViewById(R.id.titleTextInput)
        descInput = view.findViewById(R.id.descTextInput)
        dateTextView = view.findViewById(R.id.taskDate)
        saveButton = view.findViewById(R.id.savebtn)
        cancelButton = view.findViewById(R.id.cancelbtn)
        editTaskLayout = view.findViewById(R.id.edittasklayout)
        prioritySelect = view.findViewById(R.id.prioritySelect)
        priorityIndicator = view.findViewById(R.id.priorityIndicator)

        task = Task()
        val taskId: UUID = arguments?.getSerializable(ARG_TASK_ID) as UUID
        editFragmentViewModel.loadTask(taskId)
        editTaskLayout.transitionName = taskId.toString()

        val constraintLayout: ConstraintLayout = editTaskLayout
        val rootView:View = editTaskLayout
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is open
                moveButtonAboveKeyboard(constraintLayout, saveButton, keypadHeight)
                moveButtonAboveKeyboard(constraintLayout, cancelButton, keypadHeight)
            } else {
                // Keyboard is closed
                resetButtonPosition(constraintLayout, saveButton)
                resetButtonPosition(constraintLayout, cancelButton)
            }
        }

        return view
    }

    private fun moveButtonAboveKeyboard(constraintLayout: ConstraintLayout, button: View, keypadHeight: Int) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        // Move the button up by the height of the keyboard plus some margin
        constraintSet.clear(button.id, ConstraintSet.BOTTOM)
        constraintSet.connect(button.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, keypadHeight + 100)

        constraintSet.applyTo(constraintLayout)
    }

    private fun resetButtonPosition(constraintLayout: ConstraintLayout, button: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        // Reset the button to its original position
        constraintSet.clear(button.id, ConstraintSet.BOTTOM)
        constraintSet.connect(button.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 8)

        constraintSet.applyTo(constraintLayout)
    }

    fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus
        view?.let {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply{
            fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
            duration = 375
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editFragmentViewModel.taskLiveData.observe(viewLifecycleOwner, Observer{ task ->
            task?.let{
                this.task = task
                updateUI()
            }
        })
    }


    override fun onStart() {
        super.onStart()

        val titleWatcher = object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //Intentionally Left Blank
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Intentionally Left Blank
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                task.title = s.toString()
            }
        }

        val descWatcher = object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //Intentionally Left Blank
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Intentionally Left Blank
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                task.desc = s.toString()
            }
        }

        titleInput.addTextChangedListener(titleWatcher)
        descInput.addTextChangedListener(descWatcher)

        dateTextView.setOnClickListener{
            DatePickerFragment.newInstance(task.dueDate).apply {
                setTargetFragment(this@EditFragment, RETURN_DATE)
                show(this@EditFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }


        prioritySelect.setOnClickListener {
            val itemSelected = task.priorty;
            context?.let { it1 ->
                MaterialAlertDialogBuilder(it1)
                    .setTitle(resources.getString(R.string.priorityDialog))
                    .setSingleChoiceItems(resources.getStringArray(R.array.priority_dialog), itemSelected) { _, which ->
                        task.priorty = which
                    }
                    .setPositiveButton("Ok") {dialog, _ ->
                        dialog.dismiss()
                        updateUI()
                    }
                    .show()
            }
        }

        saveButton.setOnClickListener {
            if(task.title.isEmpty()){
                Snackbar.make(editTaskLayout, NO_TITLE_TEXT, Snackbar.LENGTH_SHORT)
                    .show()
            }

            editFragmentViewModel.saveTask(task)
            hideKeyboard()
            lifecycleScope.launch {
                delay(300)
                parentFragmentManager.popBackStack()
            }

        }

        cancelButton.setOnClickListener{
            hideKeyboard()
            lifecycleScope.launch {
                delay(300)
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDateSelected(date: Date) {
        task.dueDate = date
        updateUI()
    }

    private fun updateUI() {
        titleInput.setText(task.title)
        descInput.setText(task.desc)
        dateTextView.setText(DateFormat.format(DATE_FORMAT, task.dueDate))
        updatePriority()
    }

    @SuppressLint("SetTextI18n")
    private fun updatePriority() {
        if(task.priorty == 0){
            prioritySelect.text = "Low"
            priorityIndicator.setImageResource(R.drawable.ic_prio_low)
        }else if(task.priorty == 1){
            prioritySelect.text = "Medium"
            priorityIndicator.setImageResource(R.drawable.ic_prio_medium)
        }else {
            prioritySelect.text = "High"
            priorityIndicator.setImageResource(R.drawable.ic_prio_high)
        }
    }

    companion object{
        fun newInstance(taskId: UUID): EditFragment {
            val args = Bundle().apply {
                putSerializable(ARG_TASK_ID, taskId)
            }

            return EditFragment().apply {
                arguments = args
            }
        }
    }
}
