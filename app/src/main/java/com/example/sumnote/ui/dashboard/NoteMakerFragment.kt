package com.example.sumnote.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sumnote.databinding.FragmentNoteMakerBinding

class NoteMakerFragment : Fragment() {

    private var _binding: FragmentNoteMakerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(NoteMakerViewModel::class.java)

        _binding = FragmentNoteMakerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNoteMaker
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}