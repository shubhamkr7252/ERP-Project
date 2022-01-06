package com.example.erp

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
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*

class ActivityAdminList : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: AdminListRecyclerViewAdapter
    private lateinit var arrayList: ArrayList<AdminListRecyclerViewDataClass>

    private lateinit var fabListMenuLayout: MaterialCardView
    private lateinit var removeAdminBut: MaterialCardView
    private lateinit var addAdminBut: MaterialCardView
    private lateinit var editFab: ExtendedFloatingActionButton
    private lateinit var obstructor: RelativeLayout
    private lateinit var parentFrameLayout: FrameLayout
    private lateinit var refreshButton: ImageView

    private var clicked = false

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_list)

        fabListMenuLayout = findViewById(R.id.fabListMenuLayout)
        removeAdminBut = findViewById(R.id.removeAdminBut)
        addAdminBut = findViewById(R.id.addAdminBut)
        editFab = findViewById(R.id.editFab)
        obstructor = findViewById(R.id.obstructor)
        parentFrameLayout = findViewById(R.id.parentFrameLayout)
        refreshButton = findViewById(R.id.refreshButton)
        val temp = intent.getStringExtra("mailTxt")

        window.statusBarColor = getColorFromAttr(R.attr.colorSurfaceVariant)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        arrayList = arrayListOf<AdminListRecyclerViewDataClass>()
        recyclerViewAdapter = AdminListRecyclerViewAdapter(arrayList,this)

        recyclerView.adapter = recyclerViewAdapter

        editFab.setOnClickListener {
            onEditButtonClicked()
        }

        addAdminBut.setOnClickListener {
            val addAdminFragment = FragmentAddAdmin()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(addAdminFragment::class.java.simpleName)

            if(fragment !is FragmentAddAdmin){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,addAdminFragment,FragmentAddAdmin::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }
        removeAdminBut.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("mail",intent.getStringExtra("mailTxt"))
            val removeAdminFragment = FragmentRemoveAdmin()
            val fragment : Fragment? =

                supportFragmentManager.findFragmentByTag(removeAdminFragment::class.java.simpleName)

            if(fragment !is FragmentRemoveAdmin){
                removeAdminFragment.arguments = bundle
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,removeAdminFragment,FragmentRemoveAdmin::class.java.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        refreshButton.setOnClickListener {
            getAdminData()
            Snackbar.make(parentFrameLayout,"Data Refreshed", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColorFromAttr(R.attr.colorPrimary))
                .show()
        }

        getAdminData()
    }

    private fun getAdminData() {
        arrayList.clear()
        db.collection("AdminInfo").addSnapshotListener(object: EventListener<QuerySnapshot> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if(error != null){
                    Log.e("Firestore Error",error.message.toString())
                    return
                }
                for(dc : DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        arrayList.add(dc.document.toObject(AdminListRecyclerViewDataClass::class.java))
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
    fun Context.getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}