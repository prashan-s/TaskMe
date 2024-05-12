package com.mpcs.taskme.listtasks

import androidx.lifecycle.ViewModel
import com.mpcs.taskme.TaskRepository
import com.mpcs.taskme.models.Task

class CompletedFragmentViewModel: ViewModel() {
    private val taskRepository = TaskRepository.get()

    val taskList = taskRepository.getTasks(true)

    var taskListSize = taskRepository.getCount(true)

    fun deleteItem(task: Task){
        taskRepository.deleteTask(task)
    }

    fun updateTask(task: Task){
        taskRepository.updateTask(task)
    }

}