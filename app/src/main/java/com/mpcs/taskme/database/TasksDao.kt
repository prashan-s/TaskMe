package com.mpcs.taskme.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mpcs.taskme.models.Task
import java.util.*

@Dao
interface TasksDao {
    @Query("SELECT * FROM task WHERE completed = :isCompleted ORDER BY priorty DESC, dueDate DESC")
    fun getTasks(isCompleted: Boolean = false): LiveData<List<Task>>

    @Query("SELECT * FROM task WHERE id=(:id)")
    fun getTaskFromId(id: UUID): LiveData<Task?>

    @Query("SELECT COUNT(*) FROM task WHERE completed = :isCompleted")
    fun getCount(isCompleted: Boolean = false): LiveData<Int>

    @Update
    fun updateTask(task: Task)

    @Insert
    fun addTask(task: Task)

    @Delete
    fun deleteTask(task: Task)

}