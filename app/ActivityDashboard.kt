package com.mpcs.taskme.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.mpcs.taskme.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityDashboard : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        val v = this
        scope.launch {

        }
    }

}