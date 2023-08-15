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
        //findNavController().navigate(R.id.action_navigation_note_maker_to_cameraFragement)

        onHiddenChanged(true) //해당 프레그먼트에서는 바텀 바 안보이게 하기
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onHiddenChanged(hidden: Boolean) {
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.nav_view)

        if (hidden) {
            bottomNavigationView?.visibility = View.GONE
        } else {
            bottomNavigationView?.visibility = View.VISIBLE
        }
    }

}