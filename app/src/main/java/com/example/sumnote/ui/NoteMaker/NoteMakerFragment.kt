package com.example.sumnote.ui.NoteMaker

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
        // view모델과 연결
        // view : 화면에 대한 정보,
        // model : 데이터 관리, 비즈니스 로직 담당(함수 구현)
        // view model : view와 모델간의 매개체
        val noteMakerViewModel =
            ViewModelProvider(this)[NoteMakerViewModel::class.java]

        _binding = FragmentNoteMakerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNoteMaker
        noteMakerViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}