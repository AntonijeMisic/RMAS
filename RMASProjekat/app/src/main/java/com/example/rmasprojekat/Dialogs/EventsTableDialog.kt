package com.example.rmasprojekat.Dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.SearchView
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rmasprojekat.Adapters.RecycleViewAdapter
import com.example.rmasprojekat.Functionality.DogadjajiFunctionality
import com.example.rmasprojekat.Models.DogadjajViewModel
import com.example.rmasprojekat.R
import com.example.rmasprojekat.databinding.TableLayoutBinding

class EventsTableDialog(context: Context, dogadjajViewModel: DogadjajViewModel, owner: LifecycleOwner, dogadjajiFunctionality: DogadjajiFunctionality): Dialog(context) {
    private lateinit var tableDialogView: View
    public lateinit var tableBinding: TableLayoutBinding
    private  var dogadjajViewModel = dogadjajViewModel
    private var owner = owner
    private var dogadjajiFunc = dogadjajiFunctionality

    public fun open(@LayoutRes layout: Int)
    {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        tableDialogView = LayoutInflater.from(context).inflate(layout, null)
        tableBinding = TableLayoutBinding.inflate(LayoutInflater.from(context))
        this.setContentView(tableBinding.root)
        this.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.attributes?.windowAnimations = R.style.DialogAnimation
        this.window?.setGravity(Gravity.BOTTOM)

        val recycleView = this.findViewById<RecyclerView>(R.id.rvDogadjaji)
        recycleView.layoutManager = LinearLayoutManager(context)

        val searchTable = this.findViewById<SearchView>(R.id.searchDogadjaji)
        searchTable.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                PrikaziEventeTable(recycleView, dogadjajViewModel, owner, newText)
                return true
            }
        })

        PrikaziEventeTable(recycleView, dogadjajViewModel, owner)
        //recycleView.adapter = dogadjajiAdapter
        this.show()
    }

    private fun PrikaziEventeTable(recycleView: RecyclerView, dogadjajViewModel: DogadjajViewModel, owner: LifecycleOwner, searchTerm: String? =null )
    {
        dogadjajViewModel.getEvents(null, null, null, searchTerm)
        dogadjajViewModel.dogadjaji.observe(owner, androidx.lifecycle.Observer {
            if(!it.isEmpty())
            {
                val adapter = RecycleViewAdapter(it, context, dogadjajiFunc)
                recycleView.adapter = adapter
            }
        })
    }
}