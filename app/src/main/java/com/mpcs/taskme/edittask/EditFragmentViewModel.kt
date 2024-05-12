package com.mpcs.taskme.edittask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.mpcs.taskme.TaskRepository
import com.mpcs.taskme.models.Task
import java.util.*

class EditFragmentViewModel: ViewModel() {
    private val taskRepository = TaskRepository.get()
    private val taskIdLiveData = MutableLiveData<UUID>()

    var taskLiveData: LiveData<Task?> = taskIdLiveData.switchMap { taskId ->
        taskRepository.getTaskFromId(taskId)
    }


    fun loadTask(taskId: UUID) {
        taskIdLiveData.value = taskId
    }

    fun saveTask(task: Task) {
        taskRepository.updateTask(task)
    }

}