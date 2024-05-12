package com.mpcs.taskme.addtasks

import androidx.lifecycle.ViewModel
import com.mpcs.taskme.TaskRepository
import com.mpcs.taskme.models.Task

class AddFragmentViewModel: ViewModel() {
    var currentTask: Task = Task()

    private val taskRepository = TaskRepository.get()

    fun addTask(task: Task){
        taskRepository.addTask(task)
    }

}