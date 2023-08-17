package com.example.sumnote

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.sumnote.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


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

        get_permissions() //=> 이곳에 적으면 무한루프 빠져서 바텀 네비게이션 바 비작동처리됨
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


    fun get_permissions(){
        //허용받을 권한을 저장할 리스트
        var permissionList = mutableListOf<String>()

        //허용된 권한(packageManager)으로 부터 카메라 권한 확인 => 허용되어 있지 않다면
        if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(android.Manifest.permission.CAMERA) // 카메라 권한을 허용받기 위해 리스트에 삽입
        //허용된 권한(packageManager)으로 부터 외부저장소 읽기 권한 확인 => 허용되어 있지 않다면
        if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE) // 외부저장소 읽기 권한 허용받기 위해 리스트에 삽입
        //허용된 권한(packageManager)으로 부터 외부저장소 쓰기 권한 확인 => 허용되어 있지 않다면
        if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) // 내부저장소 쓰기 권한 허용받기 위해 리스트에 삽입

        //허용 받을 권한이 존재한다면 => 아직 허용받지 않은 권한들이 있다면 요청
        if(permissionList.size > 0){
            //permissionList에 존재하는 기능들 권한 요청
            requestPermissions(permissionList.toTypedArray(),101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 요청된 각 권한에 대해서
        grantResults.forEach {
            //해당 요청이(CAMERA, READ_EXTERNAL_STORAGE, 등) 허용받지 않은 권한이라면
            if(it != PackageManager.PERMISSION_GRANTED){
                //토스트 메시지로 권한 요구 요청 => 설정 창 이동
                // 사용자가 직접 설정으로 이동해서 권한을 허용할 수 있도록 안내하는 코드 작성 필요
            }
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