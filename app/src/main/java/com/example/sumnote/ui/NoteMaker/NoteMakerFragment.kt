package com.example.sumnote.ui.NoteMaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment


class NoteMakerFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return in
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //카메라 프래그먼트로 임시 이동하여 사진 촬영해오기
        //사진 촬영 -> 장고 -> 안드로이드(cameraFragment) - json 전달 -> 안드로이드(noteMaker Fragment) -> 스프링(GPT API) -> 안드로이드(noteMaker Fragment)
       // findNavController().navigate(R.id.action_navigation_note_maker_to_cameraFragement)
    }

}