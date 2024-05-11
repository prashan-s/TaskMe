package com.mpcs.taskme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.mpcs.taskme.edittask.EditFragment
import com.mpcs.taskme.listtasks.ListFragment
import java.util.*

class MainActivity : AppCompatActivity(), ListFragment.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if(currentFragment == null){
            val fragment = ListFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }

    }

    override fun onTaskSelect(view: View?, taskId: UUID) {
        val fragment = EditFragment.newInstance(taskId)
        supportFragmentManager
            .beginTransaction()
            .addSharedElement(view!!, taskId.toString())
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
