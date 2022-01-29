package com.app.erp.admin_view.admin_list_view

import android.annotation.SuppressLint
import android.content.Context
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
import com.app.erp.databinding.ActivityAdminListBinding
import com.app.erp.gloabal_functions.getColorFromAttribute
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*

class ActivityAdminList : AppCompatActivity() {
    private lateinit var recyclerViewAdapter: AdminListRecyclerViewAdapter
    private lateinit var arrayList: ArrayList<AdminListRecyclerViewDataClass>

    private val fragmentViewModel : ActivityAdminListViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityAdminListBinding

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
        binding = ActivityAdminListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColorFromAttribute(this,R.attr.colorSurfaceVariant)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        arrayList = arrayListOf<AdminListRecyclerViewDataClass>()
        recyclerViewAdapter = AdminListRecyclerViewAdapter(arrayList,this)

        binding.recyclerView.adapter = recyclerViewAdapter

        binding.editFab.setOnClickListener {
            onEditButtonClicked()
        }

        fragmentViewModel.emailData.postValue(intent.getStringExtra("mailTxt"))

        binding.addAdminBut.setOnClickListener {
            val addAdminFragment = FragmentAddAdmin()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(addAdminFragment::class.java.simpleName)

            if(fragment !is FragmentAddAdmin){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,addAdminFragment, FragmentAddAdmin::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        binding.removeAdminBut.setOnClickListener {
            val removeAdminFragment = FragmentRemoveAdmin()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(removeAdminFragment::class.java.simpleName)

            if(fragment !is FragmentRemoveAdmin){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,removeAdminFragment, FragmentRemoveAdmin::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.refreshButton.setOnClickListener {
            getAdminData()
            Snackbar.make(binding.parentFrameLayout,"Data Refreshed", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttribute(this,R.attr.colorPrimary))
                .show()
        }

        getAdminData()
    }

    internal fun getAdminData() {
        arrayList.clear()
        db.collection("AdminInfo").addSnapshotListener(object: EventListener<QuerySnapshot> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                val adminCodeList = ArrayList<String>()
                if(error != null){
                    Log.e("Firestore Error",error.message.toString())
                    return
                }
                adminCodeList.clear()
                for(dc : DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        arrayList.add(dc.document.toObject(AdminListRecyclerViewDataClass::class.java))
                        adminCodeList.add(dc.document.data.getValue("Email").toString())
                    }
                }
                fragmentViewModel.adminEmailDataList.postValue(adminCodeList)
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