package com.example.sumnote


import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.sumnote.databinding.ActivityMainBinding
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var kakaoViewModel: KakaoViewModel

    // 허용받을 권한들
    private val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상태바 숨기기
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        // 엑션바(상단바) 사용 x => 사용하게 될 일이 생기면 주석 풀것
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_my_note, R.id.navigation_note_maker, R.id.navigation_my_page
//            )
//        )
//      setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        getPermissions() //앱을 실행했을때 최초 한번만 권한을 요청하도록 변경

    }

    //뒤로가기 키가 눌렸을때에 대한 동작
    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

//       // 만약 navController의 현재 프래그먼트 아이디가.. ~ 라면
        when(navController.currentDestination?.id){
//            // 노트 메이커 프래그먼트에서 뒤로가기 누를경우 스택에서 제거(테스트용)
//            R.id.navigation_note_maker -> navController.popBackStack()
            // 카메라 프래그먼트에서 사용자가 사진 촬영없이 뒤로가기 하는 상황 고려 : 카메라 프래그먼트를 스택에서 제거
            R.id.cameraFragement -> navController.navigate(R.id.action_cameraFragement_to_navigation_my_note)
            else -> navController.popBackStack() //다른 모든 경우 뒤로가기 누를경우, 이전 프래그먼트로 이동
        }
    }


    private fun getPermissions() {
        val permissionList = permissions.filter {
            //filer에 적용될 조건문을 의미, it은 permission의 각 요소들을 의미한다.
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED //해당 권한이 허용되어있지 않은 상태라면
        }

        // 허용 받을 권한이 존재한다면 => 아직 허용받지 않은 권한들이 있다면 요청
        if (permissionList.isNotEmpty()) {
            requestPermissions(permissionList.toTypedArray(), 101)
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
}