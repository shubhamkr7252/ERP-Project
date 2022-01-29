package com.app.erp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_onboard.*
import me.relex.circleindicator.CircleIndicator3
import androidx.annotation.NonNull

import androidx.viewpager.widget.ViewPager





class OnboardActivity : AppCompatActivity() {
    private var titlesList = mutableListOf<String>()
    private var descList = mutableListOf<String>()
    private var imagesList = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard)

        postToList()

        viewPager2.adapter = OnboardActivityViewPagerAdapter(imagesList,titlesList,descList)
        viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        viewPager2.removeOverScroll()

        val indicator = findViewById<CircleIndicator3>(R.id.indicator)
        indicator.setViewPager(viewPager2)

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                if (position == 4){
                    button.startAnimation(AnimationUtils.loadAnimation(this@OnboardActivity,R.anim.nav_default_pop_enter_anim))
                    indicator.visibility = View.GONE
                    button.visibility = View.VISIBLE
                    button.setOnClickListener {
                        val sharedPreferences: SharedPreferences = getSharedPreferences("appConfig",
                            Context.MODE_PRIVATE)
                        val editor: SharedPreferences.Editor = sharedPreferences.edit()
                        editor.apply {
                            putBoolean("ONBOARD_DONE",true)
                        }.apply()
                        startActivity(Intent(this@OnboardActivity,Activity4Login::class.java))
                        finish()
                    }
                }
                else {
                    indicator.visibility = View.VISIBLE
                    button.visibility = View.GONE
                }
                super.onPageSelected(position)
            }
        })
    }

    private fun ViewPager2.removeOverScroll() {
        (getChildAt(0) as? RecyclerView)?.overScrollMode = View.OVER_SCROLL_NEVER
    }

    private fun addToList(title: String, description: String, image: Int){
        titlesList.add(title)
        descList.add(description)
        imagesList.add(image)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if(viewPager2.currentItem == 4){
            button.visibility = View.VISIBLE
        }
        else{
            button.visibility = View.GONE
        }
    }

    private fun postToList(){
        var tempImage: Int = 0
        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {tempImage = R.drawable.app_logo_white_bg}
            Configuration.UI_MODE_NIGHT_NO -> {tempImage = R.drawable.app_logo_black_bg}
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
        addToList("Welcome To ERP","The Complete solution for your University ERP needs",tempImage)
        addToList("For Students","Students can view their Attendance in a Intuitive UI",R.drawable.ic_student)
        addToList("For Teachers","Teachers can get the overview of Attendances with ease",R.drawable.ic_teacher)
        addToList("For Admin","Admins can manage the University Database with a click of few buttons",R.drawable.ic_admin)
        addToList("Let's get Started","Click on 'Login' to continue ",R.drawable.ic_start)
    }
}