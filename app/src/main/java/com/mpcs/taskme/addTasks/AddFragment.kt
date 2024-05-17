package com.mpcs.taskme.addtasks

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
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
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.mpcs.taskme.DatePickerFragment
import android.text.format.DateFormat
import android.util.Log
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

import com.mpcs.taskme.R
import com.mpcs.taskme.listtasks.DATE_FORMAT
import java.util.*

private const val DIALOG_DATE = "DateDialog"
private const val RETURN_DATE = 0
private const val NO_TITLE_TEXT = "Add a task title to save."

class AddFragment : Fragment(), DatePickerFragment.Callbacks {

    private lateinit var titleInput: TextInputEditText
    private lateinit var descInput: TextInputEditText
    private lateinit var dateTextView: TextView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var prioritySelect: TextView
    private lateinit var priorityIndicator: ImageView
    private lateinit var addTaskLayout: ConstraintLayout

    private val addFragmentViewModel: AddFragmentViewModel by lazy {
        ViewModelProvider(this)[AddFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add, container, false)

        saveButton = view.findViewById(R.id.savebtn)
        cancelButton = view.findViewById(R.id.cancelbtn)
        dateTextView = view.findViewById(R.id.taskDate)
        titleInput = view.findViewById(R.id.titleTextInput)
        descInput = view.findViewById(R.id.descTextInput)
        prioritySelect = view.findViewById(R.id.prioritySelect)
        priorityIndicator = view.findViewById(R.id.priorityIndicator)
        addTaskLayout = view.findViewById(R.id.addtasklayout)

        val constraintLayout: ConstraintLayout = addTaskLayout
        val rootView:View = addTaskLayout
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

    fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus
        view?.let {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply{
            fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
            duration = 350
        }
    }

    override fun onStart() {
        super.onStart()
        updateUI()

        val titleWatcher = object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //Intentionally Left Blank
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Intentionally Left Blank
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                addFragmentViewModel.currentTask.title = s.toString()
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
                addFragmentViewModel.currentTask.desc = s.toString()
            }
        }

        titleInput.addTextChangedListener(titleWatcher)
        descInput.addTextChangedListener(descWatcher)

        dateTextView.setOnClickListener {
            DatePickerFragment.newInstance(addFragmentViewModel.currentTask.dueDate).apply {
                setTargetFragment(this@AddFragment, RETURN_DATE)
                show(this@AddFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        prioritySelect.setOnClickListener {
            val itemSelected = addFragmentViewModel.currentTask.priorty;
            context?.let { it1 ->
                MaterialAlertDialogBuilder(it1)
                    .setTitle(resources.getString(R.string.priorityDialog))
                    .setSingleChoiceItems(resources.getStringArray(R.array.priority_dialog), itemSelected) { _, which ->
                        addFragmentViewModel.currentTask.priorty = which
                    }
                    .setPositiveButton("Ok") {dialog, _ ->
                        dialog.dismiss()
                        updateUI()
                    }
                    .show()
            }
        }

        saveButton.setOnClickListener {
            hideKeyboard()
            if(addFragmentViewModel.currentTask.title.isEmpty()){
                Snackbar.make(addTaskLayout, NO_TITLE_TEXT, Snackbar.LENGTH_SHORT)
                .show()
            }else{
                val toast = Toast.makeText(context, "Task Added!", Toast.LENGTH_SHORT)
                toast.show()
                addFragmentViewModel.addTask(addFragmentViewModel.currentTask)
                parentFragmentManager.popBackStack()

            }

        }

        cancelButton.setOnClickListener{
            hideKeyboard()
            parentFragmentManager.popBackStack()
        }

    }

    override fun onDateSelected(date: Date) {
        addFragmentViewModel.currentTask.dueDate = date
        updateUI()
    }

    private fun updateUI() {
        titleInput.setText(addFragmentViewModel.currentTask.title)
        descInput.setText(addFragmentViewModel.currentTask.desc)
        dateTextView.setText(DateFormat.format(DATE_FORMAT, addFragmentViewModel.currentTask.dueDate))
        updatePriority()
    }

    @SuppressLint("SetTextI18n")
    private fun updatePriority() {
        if(addFragmentViewModel.currentTask.priorty == 0){
            prioritySelect.text = "Low"
            priorityIndicator.setImageResource(R.drawable.ic_prio_low)
        }else if(addFragmentViewModel.currentTask.priorty == 1){
            prioritySelect.text = "Medium"
            priorityIndicator.setImageResource(R.drawable.ic_prio_medium)
        }else {
            prioritySelect.text = "High"
            priorityIndicator.setImageResource(R.drawable.ic_prio_high)
        }
    }
}
