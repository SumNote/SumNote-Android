package com.example.sumnote.ui.MyNote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentMyNoteBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MyNoteFragment : Fragment() {

    private var _binding: FragmentMyNoteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyNoteBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //뷰 생성 시점
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onHiddenChanged(false)
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