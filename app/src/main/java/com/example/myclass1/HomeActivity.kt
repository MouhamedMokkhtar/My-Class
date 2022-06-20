package com.example.myclass1

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.myclass1.databinding.ActivityHomeBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference


class HomeActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityHomeBinding

    override lateinit var auth: FirebaseAuth
    override lateinit var DataBase : DatabaseReference
    override lateinit var DataBase2 : DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()



       // add it to change the toolbar title's to the activity name
        val actionBar = supportActionBar
        if (actionBar != null) {
            val dynamicTitle: String = HomeActivity::class.java.simpleName
            //Setting a dynamic title at runtime. Here, it displays the current activity name
            actionBar.setTitle("Home")
        }


        // navigate between classes  and join class  :  tabLayout
        val tablayout : TabLayout =findViewById(R.id.tab_layout)
        val viewpager2 : ViewPager2 =findViewById(R.id.viewPager2)
        val adapter =PagerAdapter(supportFragmentManager,lifecycle)
        viewpager2.adapter = adapter
        TabLayoutMediator(tablayout, viewpager2) { tab, position ->
            when(position){
                0->{tab.text="Classes"}
                1->{tab.text="Join Class"}
            }
        }.attach()






    }





}