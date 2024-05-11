package com.mpcs.taskme.listtasks

import androidx.lifecycle.ViewModel
import com.mpcs.taskme.TaskRepository
import com.mpcs.taskme.models.Task

class ListFragmentViewModel: ViewModel() {
    private val taskRepository = TaskRepository.get()

    val taskList = taskRepository.getTasks()

    var taskListSize = taskRepository.getCount()

    fun deleteItem(task: Task){
        taskRepository.deleteTask(task)
    }

}