package com.example.erp

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.google.firebase.firestore.DocumentSnapshot

import com.google.firebase.firestore.QuerySnapshot

import androidx.annotation.NonNull

import com.google.android.gms.tasks.OnCompleteListener




class ActivityAttendanceList : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: AttendanceListRecyclerViewAdapter
    private lateinit var arrayList: ArrayList<AttendanceListRecyclerViewDataClass>

    private val db = FirebaseFirestore.getInstance()

    private lateinit var editFab: ExtendedFloatingActionButton
    private lateinit var obstructor: RelativeLayout
    private lateinit var fabListMenuLayout: MaterialCardView

    private var clicked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_list)

        window.statusBarColor = getColorFromAttr(R.attr.colorSurfaceVariant)

        editFab = findViewById(R.id.editFab)
        obstructor = findViewById(R.id.obstructor)
        fabListMenuLayout = findViewById(R.id.fabListMenuLayout)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        arrayList = arrayListOf<AttendanceListRecyclerViewDataClass>()
        recyclerViewAdapter = AttendanceListRecyclerViewAdapter(arrayList)

        recyclerView.adapter = recyclerViewAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(courseRecyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) editFab.hide() else if (dy < 0) editFab.show()
            }
        })

        getAttendanceData()
        getCollectionExistInfo()

        editFab.setOnClickListener {
            onEditButtonClicked()
        }
    }

    private fun getCollectionExistInfo() {
        db.collection("Attendance").get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    for(doc in it.result!!){
                        val date = doc.data.getValue("Date").toString()
                        val tempAlt = ArrayList<String>()
                        tempAlt.addAll(doc.data.getValue("Time") as Collection<String>)
                        if(tempAlt.size == 0){
                            db.collection("Attendance").document(date).delete()
                        }
                        else{
                            for (i in tempAlt){
                                db.collection("Attendance").document(date).collection(i).get()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            if (task.result.size() > 0) {
                                                //TO CHECK FOR ATTENDANCE COLLECTION EXISTENCE
                                            } else {
                                                val updates = hashMapOf<String, Any>(
                                                    "Time" to FieldValue.arrayRemove(i)
                                                )
                                                db.collection("Attendance").document(date).update(updates).addOnSuccessListener {
                                                    db.collection("Attendance").document(date).get()
                                                        .addOnCompleteListener { itAlt ->
                                                            if(itAlt.isSuccessful){
                                                                val temp = ArrayList<String>()
                                                                temp.addAll(itAlt.result.data?.getValue("Time") as Collection<String>)
                                                                if(temp.size == 0){
                                                                    db.collection("Attendance").document(date).delete()
                                                                }
                                                            }
                                                        }
                                                }
                                            }
                                        } else {
                                            Toast.makeText(this,"Error ${task.exception}",Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        }
                    }
                }
            }
    }

    private fun getAttendanceData() {
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
                                            arrayList.add(dc.document.toObject(AttendanceListRecyclerViewDataClass::class.java))
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
            window.statusBarColor = Color.parseColor("#cc000000")
            editFab.setIconResource(R.drawable.ic_outline_close_24)
            obstructor.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_enter_anim))
            fabListMenuLayout.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_enter_anim))
        }else{
            window.statusBarColor = getColorFromAttr(R.attr.colorSurfaceVariant)
            editFab.setIconResource(R.drawable.ic_outline_edit_24)
            obstructor.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_exit_anim))
            fabListMenuLayout.startAnimation(AnimationUtils.loadAnimation(this,R.anim.nav_default_pop_exit_anim))
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            obstructor.visibility = View.VISIBLE
            fabListMenuLayout.visibility = View.VISIBLE
        }else{
            fabListMenuLayout.visibility = View.INVISIBLE
            obstructor.visibility = View.INVISIBLE
        }
    }

    internal fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}