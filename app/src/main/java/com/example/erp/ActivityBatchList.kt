package com.example.erp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*

class ActivityBatchList : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: BatchListRecyclerViewAdapter
    private lateinit var arrayList: ArrayList<BatchListRecyclerViewDataClass>

    private val db = FirebaseFirestore.getInstance()
    private val fragmentViewModel : ActivityBatchListViewModel by viewModels()

    private lateinit var editFab: ExtendedFloatingActionButton
    private lateinit var addBatchFab: MaterialCardView
    private lateinit var removeBatchFab: MaterialCardView
    private lateinit var obstructor: RelativeLayout
    private lateinit var fabListMenuLayout: MaterialCardView
    private lateinit var backButton: ImageView
    private lateinit var refreshButton: ImageView
    private lateinit var relativeLayoutFragmentLayoutContainer: RelativeLayout
    private lateinit var parentFrameLayout: FrameLayout

    private lateinit var tempBatchNameArrayList: ArrayList<String>

    private var clicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_batch_list)

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }

        window.statusBarColor = getColorFromAttr(R.attr.colorSurfaceVariant)

        recyclerView = findViewById(R.id.recyclerView)
        editFab = findViewById(R.id.editFab)
        addBatchFab = findViewById(R.id.addBatchFab)
        removeBatchFab = findViewById(R.id.removeBatchFab)
        obstructor = findViewById(R.id.obstructor)
        fabListMenuLayout = findViewById(R.id.fabListMenuLayout)
        refreshButton = findViewById(R.id.refreshButton)
        parentFrameLayout = findViewById(R.id.parentFrameLayout)
        relativeLayoutFragmentLayoutContainer = findViewById(R.id.relativeLayoutFragmentLayoutContainer)

//        recyclerView.layoutManager = LinearLayoutManager(this)
//        recyclerView.setHasFixedSize(true)
//
//        arrayList = arrayListOf<BatchListRecyclerViewDataClass>()
//        recyclerViewAdapter = BatchListRecyclerViewAdapter(arrayList)
//
//        recyclerView.adapter = recyclerViewAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(courseRecyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) editFab.hide() else if (dy < 0) editFab.show()
            }
        })

        getBatchData()

        refreshButton.setOnClickListener {
            getBatchData()
            getBatchArrayListData()
            Snackbar.make(parentFrameLayout,"Data Refreshed",Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                .show()
        }

        addBatchFab.setOnClickListener {
            val addBatchFragment = FragmentAddBatch()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(addBatchFragment::class.java.simpleName)

            if(fragment !is FragmentAddBatch){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.relativeLayoutFragmentLayoutContainer,addBatchFragment,FragmentAddBatch::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        removeBatchFab.setOnClickListener {
            val removeBatchFragment = FragmentRemoveBatch()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(removeBatchFragment::class.java.simpleName)

            if(fragment !is FragmentRemoveBatch){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.relativeLayoutFragmentLayoutContainer,removeBatchFragment,FragmentRemoveBatch::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        getBatchArrayListData()

        editFab.setOnClickListener {
            onEditButtonClicked()
        }
        obstructor.setOnClickListener {
            editFab.performClick()
        }
    }

    internal fun getBatchArrayListData(){
        tempBatchNameArrayList = arrayListOf<String>()
        tempBatchNameArrayList.clear()
        db.collection("BatchInfo").get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    for(doc in it.result!!){
                        tempBatchNameArrayList.add(doc.data.getValue("Name").toString())
                    }
                }
                fragmentViewModel.batchArrayListData.postValue(tempBatchNameArrayList)
            }
    }

    internal fun getBatchData() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        arrayList = arrayListOf<BatchListRecyclerViewDataClass>()
        recyclerViewAdapter = BatchListRecyclerViewAdapter(arrayList)

        recyclerView.adapter = recyclerViewAdapter
        arrayList.clear()
        db.collection("BatchInfo").addSnapshotListener(object: EventListener<QuerySnapshot> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if(error != null){
                    Log.e("Firestore Error",error.message.toString())
                    return
                }
                for(dc : DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        arrayList.add(dc.document.toObject(BatchListRecyclerViewDataClass::class.java))
                    }
                }
                recyclerViewAdapter.notifyDataSetChanged()
            }

        })
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
    @ColorInt
    internal fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}