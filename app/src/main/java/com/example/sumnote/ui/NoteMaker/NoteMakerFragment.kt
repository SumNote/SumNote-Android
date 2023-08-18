package com.example.sumnote.ui.NoteMaker


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentNoteMakerBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class NoteMakerFragment : Fragment() {

    private var _binding: FragmentNoteMakerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteMakerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //카메라 프래그먼트로 임시 이동하여 사진 촬영해오기
        //사진 촬영 -> 장고 -> 안드로이드(cameraFragment) - json 전달 -> 안드로이드(noteMaker Fragment) -> 스프링(GPT API) -> 안드로이드(noteMaker Fragment)
        findNavController().navigate(R.id.action_navigation_note_maker_to_cameraFragement)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}