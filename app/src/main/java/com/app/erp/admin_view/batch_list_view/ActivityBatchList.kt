package com.app.erp.admin_view.batch_list_view

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.erp.R
import com.app.erp.databinding.ActivityBatchListBinding
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*

class ActivityBatchList : AppCompatActivity() {
    private lateinit var recyclerViewAdapter: BatchListRecyclerViewAdapter
    private lateinit var arrayList: ArrayList<BatchListRecyclerViewDataClass>

    private val db = FirebaseFirestore.getInstance()
    private val fragmentViewModel : ActivityBatchListViewModel by viewModels()
    private lateinit var binding: ActivityBatchListBinding

    private lateinit var tempBatchNameArrayList: ArrayList<String>

    private var clicked = false

    override fun onBackPressed() {
        if(binding.obstructor.isVisible){
            binding.editFab.performClick()
        }
        else{
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBatchListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        window.statusBarColor = getColorFromAttribute(this,R.attr.colorSurfaceVariant)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        arrayList = arrayListOf<BatchListRecyclerViewDataClass>()
        recyclerViewAdapter = BatchListRecyclerViewAdapter(arrayList)

        binding.recyclerView.adapter = recyclerViewAdapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(courseRecyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) binding.editFab.hide() else if (dy < 0) binding.editFab.show()
            }
        })

        getBatchData()

        binding.refreshButton.setOnClickListener {
            getBatchData()
            getBatchArrayListData()
            Snackbar.make(binding.parentFrameLayout,"Data Refreshed",Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttribute(this,R.attr.colorPrimary))
                .show()
        }

        binding.addBatchFab.setOnClickListener {
            val addBatchFragment = FragmentAddBatch()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(addBatchFragment::class.java.simpleName)

            if(fragment !is FragmentAddBatch){
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.relativeLayoutFragmentLayoutContainer,addBatchFragment,
                        FragmentAddBatch::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        binding.removeBatchFab.setOnClickListener {
            val removeBatchFragment = FragmentRemoveBatch()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(removeBatchFragment::class.java.simpleName)

            if(fragment !is FragmentRemoveBatch){
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.relativeLayoutFragmentLayoutContainer,removeBatchFragment,
                        FragmentRemoveBatch::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        getBatchArrayListData()

        binding.editFab.setOnClickListener {
            onEditButtonClicked()
        }
        binding.obstructor.setOnClickListener {
            binding.editFab.performClick()
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
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        arrayList = arrayListOf<BatchListRecyclerViewDataClass>()
        recyclerViewAdapter = BatchListRecyclerViewAdapter(arrayList)

        binding.recyclerView.adapter = recyclerViewAdapter
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
            binding.editFab.setIconResource(R.drawable.ic_outline_close_24)
            binding.obstructor.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.nav_default_pop_enter_anim
            ))
            binding.fabListMenuLayout.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.nav_default_pop_enter_anim
            ))
        }else{
            window.statusBarColor = getColorFromAttribute(this,R.attr.colorSurfaceVariant)
            binding.editFab.setIconResource(R.drawable.ic_outline_edit_24)
            binding.obstructor.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.nav_default_pop_exit_anim
            ))
            binding.fabListMenuLayout.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.nav_default_pop_exit_anim
            ))
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

}