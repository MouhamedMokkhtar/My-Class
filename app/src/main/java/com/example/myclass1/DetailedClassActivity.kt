package com.example.myclass1

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.myclass1.databinding.ActivityDetailedClassBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class DetailedClassActivity : DrawerBaseActivity() {

    private lateinit var binding: ActivityDetailedClassBinding

    override lateinit var auth: FirebaseAuth

    private lateinit var classID: String
    private lateinit var moderaterID: String
    private lateinit var className: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDetailedClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** i bring the class id from the fragment ClassFragmet to this activity because she start when i click on classe **/
        val intent=getIntent()
        classID= intent.getStringExtra("classId").toString()
        moderaterID=intent.getStringExtra("moderaterId").toString()
        className=intent.getStringExtra("classname").toString()

        // add it to change the toolbar title's to the activity name
        val actionBar = supportActionBar
        if (actionBar != null) {
            val dynamicTitle: String = HomeActivity::class.java.simpleName
            //Setting a dynamic title at runtime. Here, it displays the current activity name
            actionBar.setTitle("$className")
        }


        // navigate between documents ,notification and chatroom  :  tabLayout
        val tablayout2 : TabLayout =findViewById(R.id.tab_layout2)
        val viewpager21 : ViewPager2 =findViewById(R.id.viewPager21)
        val adapter =PagerAdapter2(supportFragmentManager,lifecycle)
        viewpager21.adapter = adapter
        TabLayoutMediator(tablayout2, viewpager21) { tab, position ->
            when(position){
                0->{tab.text="Courses" }
                1->{tab.text="ChatRoom" }
                2->{tab.text="participants" }

            }
        }.attach()



    }


    /** i use this function outside the onCreate beacause i want to use it in fragment DocumentFragmet
     * and this fuction going to return the id of class so we need to declare data above onCreate */

    fun getMyClassID(): String? {
        return classID
    }
    fun getMyModeraterID(): String? {
        return moderaterID
    }


}