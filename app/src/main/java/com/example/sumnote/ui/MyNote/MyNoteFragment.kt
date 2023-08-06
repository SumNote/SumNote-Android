package com.example.sumnote.ui.MyNote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
        // view모델과 연결
        // view : 화면에 대한 정보,
        // model : 데이터 관리, 비즈니스 로직 담당(함수 구현)
        // view model : view와 모델간의 매개체
        val myNoteViewModel =
            ViewModelProvider(this)[MyNoteViewModel::class.java]

        _binding = FragmentMyNoteBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textMyNote
        myNoteViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}