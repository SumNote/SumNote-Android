package com.example.sumnote.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sumnote.databinding.FragmentMyWorkbookBinding

class MyWorkbookFragment : Fragment() {

    private var _binding: FragmentMyWorkbookBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(MyWorkbookViewModel::class.java)

        _binding = FragmentMyWorkbookBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textMyWorkbook
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}