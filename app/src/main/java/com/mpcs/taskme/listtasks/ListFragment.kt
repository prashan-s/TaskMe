package com.mpcs.taskme.listtasks

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.Hold
import com.mpcs.taskme.R
import com.mpcs.taskme.addtasks.AddFragment
import com.mpcs.taskme.models.Task
import android.text.format.DateFormat
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import java.util.*

const val DATE_FORMAT = "EEEE, MMM, dd, yyyy"

class ListFragment : Fragment() {

    interface Callbacks {
        fun onTaskSelect(view: View?, taskId: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var fabButton: FloatingActionButton
    private lateinit var seeCompleted: Button
    private lateinit var taskRecyclerView: RecyclerView
    private lateinit var subTitle: TextView
    private var adapter: TaskAdapter? = TaskAdapter(emptyList())

    private val taskListViewModel: ListFragmentViewModel by lazy {
        ViewModelProvider(this)[ListFragmentViewModel::class.java]
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        taskRecyclerView = view.findViewById(R.id.tasks_list)
        taskRecyclerView.layoutManager = LinearLayoutManager(context)
        taskRecyclerView.adapter = adapter

        subTitle = view.findViewById(R.id.subtitle)

        fabButton = view.findViewById(R.id.floating_action_button)
        fabButton.setOnClickListener {
            val fragment = AddFragment()
            parentFragmentManager
                .beginTransaction()
                .addSharedElement(it, "list_to_add")
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        seeCompleted = view.findViewById(R.id.see_completed)
        seeCompleted.setOnClickListener {
            val fragment = CompletedFragment()
            parentFragmentManager
                .beginTransaction()
//                .addSharedElement(it, "completed_list")
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called
                // when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // this method is called when we swipe our item to right direction.
                // on below line we are getting the item at a particular position.
                val position = viewHolder.adapterPosition
                val task = taskListViewModel.taskList.value?.get(position)

                task?.let {
                    taskListViewModel.deleteItem(task)
                }

            }

        }).attachToRecyclerView(taskRecyclerView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = (requireActivity() as AppCompatActivity)
        taskListViewModel.taskList.observe(viewLifecycleOwner,
            Observer { tasks ->
            tasks?.let {
                updateList(tasks)
            }})


        taskListViewModel.taskListSize.observe(viewLifecycleOwner,
        Observer { taskCount ->
            updateOverview(taskCount)
        })

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Hold()
        setHasOptionsMenu(true)
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun updateList(tasks: List<Task>) {
        adapter?.submitList(tasks)
    }

    private fun updateOverview(listSize: Int) {
        lateinit var overviewString:String
        if(listSize == 0){
           overviewString = getString(R.string.all_completed)
        }else{
            overviewString = getString(R.string.overview, listSize.toString())
        }
        subTitle.text = overviewString
    }


    //TaskAdapter for RecyclerView
    private inner class TaskAdapter(var tasks: List<Task>): androidx.recyclerview.widget.ListAdapter<Task, TaskHolder>(TaskDiffCallback()){
        override fun onBindViewHolder(holder: TaskHolder, position: Int) {
            val task = getItem(position)
            holder.onBind(task)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
            val view = layoutInflater.inflate(R.layout.task_list_item, parent, false)
            return TaskHolder(view)
        }

    }


    //TaskHolder for RecyclerView
    private inner class TaskHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var task: Task

        val taskTitleView: TextView = itemView.findViewById(R.id.task_title)
        val dateTextView: TextView = itemView.findViewById(R.id.task_date)
        val priorityIndicator: ImageView = itemView.findViewById(R.id.priorityIndicator)
        val taskCheckBox: CheckBox = itemView.findViewById(R.id.task_checkbox)

        init{
            itemView.setOnClickListener(this)
            taskCheckBox.setOnClickListener {
                task.completed = true
                taskListViewModel.updateTask(task)
            }
        }

        fun onBind(task: Task){
            this.task = task
            taskTitleView.text = this.task.title
            dateTextView.text = DateFormat.format(DATE_FORMAT, this.task.dueDate)
            updatePriority(this.task.priorty)
            itemView.transitionName = task.id.toString()
        }

        private fun updatePriority(priority:Int) {
            when (priority) {
                0 -> {
                    priorityIndicator.setImageResource(R.drawable.ic_prio_low)
                }
                1 -> {
                    priorityIndicator.setImageResource(R.drawable.ic_prio_medium)
                }
                else -> {
                    priorityIndicator.setImageResource(R.drawable.ic_prio_high)
                }
            }
        }

        override fun onClick(v: View?) {
            callbacks?.onTaskSelect(v, task.id)
        }

    }

    class TaskDiffCallback: DiffUtil.ItemCallback<Task>() {
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

    }

}
