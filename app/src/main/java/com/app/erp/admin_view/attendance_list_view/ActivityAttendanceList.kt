package com.app.erp.admin_view.attendance_list_view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import com.google.firebase.firestore.QuerySnapshot
import com.app.erp.R
import com.app.erp.databinding.ActivityAttendanceListBinding
import com.app.erp.gloabal_functions.capitalizeWords
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.google.firebase.firestore.EventListener
import java.util.*
import kotlin.collections.ArrayList

class ActivityAttendanceList : AppCompatActivity() {
    private lateinit var recyclerViewAdapter: AttendanceListRecyclerViewAdapter
    private lateinit var arrayList: ArrayList<AttendanceListRecyclerViewDataClass>
    private lateinit var tempArrayList: ArrayList<AttendanceListRecyclerViewDataClass>
    private val activityViewModel: ActivityAttendanceListViewModel by viewModels()

    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityAttendanceListBinding

    private var clicked = false

    override fun onBackPressed() {
        if(binding.obstructor.isVisible){
            binding.editFab.performClick()
        }
        else{
            super.onBackPressed()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColorFromAttribute(this,R.attr.colorSurfaceVariant)
        binding.obstructor.setBackgroundColor(ColorUtils.setAlphaComponent(getColorFromAttribute(this,R.attr.colorOnPrimary),235))

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        arrayList = arrayListOf<AttendanceListRecyclerViewDataClass>()
        tempArrayList = arrayListOf<AttendanceListRecyclerViewDataClass>()
        recyclerViewAdapter = AttendanceListRecyclerViewAdapter(arrayList)
        binding.recyclerView.adapter = recyclerViewAdapter
        recyclerViewAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        getAttendanceData()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(text: String?): Boolean {
                arrayList.clear()
                if(text!!.isBlank()){
                    activityViewModel.setQueryState(false)
                    arrayList.addAll(tempArrayList)
                    recyclerViewAdapter.notifyDataSetChanged()
                }
                else if(text.isNotBlank()){
                    activityViewModel.setQueryState(true)
                    tempArrayList.forEach {
                        if(it.Date!!.contains(text.toString())){
                            arrayList.add(it)
                        }
                        if(it.Teacher!!.contains(capitalizeWords(text.toString()).toString())){
                            arrayList.add(it)
                        }
                        if(it.Course!!.contains(capitalizeWords(text.toString()).toString())){
                            arrayList.add(it)
                        }
                        if(it.Batch!!.contains(capitalizeWords(text.toString()).toString())){
                            arrayList.add(it)
                        }
                        if(it.Semester!!.contains(capitalizeWords(text.toString()).toString())){
                            arrayList.add(it)
                        }
                    }
                    recyclerViewAdapter.notifyDataSetChanged()
                }
                return false
            }

        })

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) binding.editFab.shrink() else if (dy < 0) binding.editFab.extend()
                if (dy > 0) binding.filterFab.hide() else if (dy < 0) binding.filterFab.show()
            }
        })

        binding.obstructor.setOnClickListener {
            onEditButtonClicked()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            getAttendanceData()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.editFab.setOnClickListener {
            onEditButtonClicked()
        }
    }

//    private fun getCollectionExistInfo() {
//        db.collection("Attendance").get()
//            .addOnCompleteListener {
//                if (it.isSuccessful){
//                    for(doc in it.result!!){
//                        val date = doc.data.getValue("Date").toString()
//                        val tempAlt = ArrayList<String>()
//                        tempAlt.addAll(doc.data.getValue("Time") as Collection<String>)
//                        if(tempAlt.size == 0){
//                            db.collection("Attendance").document(date).delete()
//                        }
//                        else{
//                            for (i in tempAlt){
//                                db.collection("Attendance").document(date).collection(i).get()
//                                    .addOnCompleteListener { task ->
//                                        if (task.isSuccessful) {
//                                            if (task.result.size() > 0) {
//                                                //TO CHECK FOR ATTENDANCE COLLECTION EXISTENCE
//                                            } else {
//                                                val updates = hashMapOf<String, Any>(
//                                                    "Time" to FieldValue.arrayRemove(i)
//                                                )
//                                                db.collection("Attendance").document(date).update(updates).addOnSuccessListener {
//                                                    db.collection("Attendance").document(date).get()
//                                                        .addOnCompleteListener { itAlt ->
//                                                            if(itAlt.isSuccessful){
//                                                                val temp = ArrayList<String>()
//                                                                temp.addAll(itAlt.result.data?.getValue("Time") as Collection<String>)
//                                                                if(temp.size == 0){
//                                                                    db.collection("Attendance").document(date).delete()
//                                                                }
//                                                            }
//                                                        }
//                                                }
//                                            }
//                                        } else {
//                                            Toast.makeText(this,"Error ${task.exception}",Toast.LENGTH_SHORT).show()
//                                        }
//                                    }
//                            }
//                        }
//                    }
//                }
//            }
//    }

    private fun getAttendanceData() {
        tempArrayList.clear()
        if(!activityViewModel.getQueryState()){
            arrayList.clear()
        }
        db.collection("Attendance").get()
            .addOnCompleteListener { itAlt ->
                if (itAlt.isSuccessful){
                    for(doc in itAlt.result!!){
                        val date = doc.data.getValue("Date").toString()
                        val tempAlt = ArrayList<String>()
                        tempAlt.addAll(doc.data.getValue("Time") as Collection<String>)
                        for(i in tempAlt){
                            db.collection("Attendance").document(date).collection(i).addSnapshotListener(object: EventListener<QuerySnapshot> {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onEvent(
                                    value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                                    if(error != null){
                                        Toast.makeText(this@ActivityAttendanceList,"Firestore Error, ${error.message}",Toast.LENGTH_SHORT).show()
                                        return
                                    }
                                    for(dc : DocumentChange in value?.documentChanges!!){
                                        if(dc.type == DocumentChange.Type.ADDED){
                                            tempArrayList.add(dc.document.toObject(AttendanceListRecyclerViewDataClass::class.java))
                                            if(!activityViewModel.getQueryState()){
                                                arrayList.add(dc.document.toObject(AttendanceListRecyclerViewDataClass::class.java))
                                            }
                                        }
                                    }
                                    recyclerViewAdapter.notifyDataSetChanged()
                                }
                            })
                        }
                    }
                }
            }
    }

    private fun onEditButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    @SuppressLint("SetTextI18n")
    private fun setAnimation(clicked: Boolean) {
        if(!clicked){
            window.statusBarColor = ColorUtils.setAlphaComponent(getColorFromAttribute(this,R.attr.colorOnPrimary),235)
            binding.editFab.setIconResource(R.drawable.ic_outline_close_24)
            binding.editFab.text = "Close"
            binding.obstructor.startAnimation(AnimationUtils.loadAnimation(this, R.anim.nav_default_pop_enter_anim))
            binding.fabListMenuLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.nav_default_pop_enter_anim))
        }else{
            window.statusBarColor = getColorFromAttribute(this,R.attr.colorSurfaceVariant)
            binding.editFab.setIconResource(R.drawable.ic_outline_edit_24)
            binding.editFab.text = "Edit"
            binding.obstructor.startAnimation(AnimationUtils.loadAnimation(this, R.anim.nav_default_pop_exit_anim))
            binding.fabListMenuLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.nav_default_pop_exit_anim))
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            binding.obstructor.visibility = View.VISIBLE
            binding.fabListMenuLayout.visibility = View.VISIBLE
        }else{
            binding.fabListMenuLayout.visibility = View.INVISIBLE
            binding.obstructor.visibility = View.INVISIBLE
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if(activityViewModel.getQueryState()){
            activityViewModel.setResumeData(arrayList)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("FAB_STATE",binding.editFab.isExtended)
        outState.putBoolean("FAB_LIST_STATE",binding.fabListMenuLayout.isVisible)
        outState.putParcelable("RECYCLER_VIEW_SAVED_POS", (binding.recyclerView.layoutManager as LinearLayoutManager).onSaveInstanceState())
        outState.putString("QUERY",binding.searchView.query.toString())
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        binding.searchView.setQuery(savedInstanceState.getString("QUERY"),false)
//        binding.searchView.setSelection(binding.searchView.text.length)
        if(activityViewModel.getQueryState()){
            arrayList.addAll(activityViewModel.getResumeData())
            recyclerViewAdapter.notifyDataSetChanged()
        }

        if(savedInstanceState.getBoolean("FAB_STATE")){
            binding.editFab.extend()
        }
        else if(!savedInstanceState.getBoolean("FAB_STATE")){
            binding.editFab.shrink()
        }
        if(savedInstanceState.getBoolean("FAB_LIST_STATE")){
            onEditButtonClicked()
        }
    }
}