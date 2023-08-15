package com.example.sumnote

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.sumnote.databinding.ActivityMainBinding
import com.example.sumnote.ui.Camera.CameraFragment
import com.example.sumnote.ui.MyPage.MyPageFragment
import com.example.sumnote.ui.NoteMaker.NoteMakerFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_my_note, R.id.navigation_note_maker, R.id.navigation_my_page
//            )
//        )

        // 엑션바(상단바) 사용 x => 사용하게 될 일이 생기면 주석 풀것
        // setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        //onHiddenChanged(false)
        if (navController.currentDestination?.id == R.id.navigation_note_maker) {
            navController.popBackStack() // 카메라 프래그먼트 스택에서 제거
            //navController.navigate(R.id.action_navigation_note_maker_to_cameraFragement)
            //onHiddenChanged(false)
        }
        else if(navController.currentDestination?.id == R.id.cameraFragement) {
            navController.popBackStack() // 카메라 프래그먼트 스택에서 제거
            // 카메라 프래그먼트가 현재 화면에 보일 때
            //navController.popBackStack() // 카메라 프래그먼트 스택에서 제거
            //onHiddenChanged(false)
        }
    }




    fun onHiddenChanged(hidden: Boolean) {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)

        if (hidden) {
            bottomNavigationView?.visibility = View.GONE
        } else {
            bottomNavigationView?.visibility = View.VISIBLE
        }
    }
}