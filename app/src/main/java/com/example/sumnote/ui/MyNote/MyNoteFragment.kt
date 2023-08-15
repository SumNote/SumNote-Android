package com.example.sumnote.ui.MyNote

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sumnote.databinding.FragmentMyNoteBinding

class MyNoteFragment : Fragment() {

    private var _binding: FragmentMyNoteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyNoteBinding.inflate(inflater, container, false)

        //권한이 있는지 확인하여 없다면 요청
        //get_permissions()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //뷰 생성 시점
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    //앱 실행시 권한 받아오기
    fun get_permissions(){
        //허용받을 권한을 저장할 리스트
        var permissionList = mutableListOf<String>()

        //허용된 권한(packageManager)으로 부터 카메라 권한 확인 => 허용되어 있지 않다면
        if(activity?.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(android.Manifest.permission.CAMERA) // 카메라 권한을 허용받기 위해 리스트에 삽입
        //허용된 권한(packageManager)으로 부터 외부저장소 읽기 권한 확인 => 허용되어 있지 않다면
        if(activity?.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE) // 외부저장소 읽기 권한 허용받기 위해 리스트에 삽입
        //허용된 권한(packageManager)으로 부터 외부저장소 쓰기 권한 확인 => 허용되어 있지 않다면
        if(activity?.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) // 내부저장소 쓰기 권한 허용받기 위해 리스트에 삽입

        //허용 받을 권한이 존재한다면 => 아직 허용받지 않은 권한들이 있다면 요청
        if(permissionList.size > 0){
            //permissionList에 존재하는 기능들 권한 요청
            requestPermissions(permissionList.toTypedArray(),101)
        }
    }

    //권한 허용 요청에 대한 함수 => 앱 시작시 권한 확인
    //앱 시작시 권한들 확인하고 아직 허용받지 않은 권한 있으면 get_permission함수 호출하여 앱에서 필요한 3가지 권한에 대해서 요청
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
                get_permissions() //권한 요청 함수 호출(내가 작성한)
            }
        }
    }

}