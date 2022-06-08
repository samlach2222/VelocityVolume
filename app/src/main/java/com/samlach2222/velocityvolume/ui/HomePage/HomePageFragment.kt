package com.samlach2222.velocityvolume.ui.HomePage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.samlach2222.velocityvolume.ProfileDrawerActivity
import com.samlach2222.velocityvolume.R
import com.samlach2222.velocityvolume.databinding.FragmentHomepageBinding

class HomePageFragment : Fragment() {

    private var _binding: FragmentHomepageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setActivityTitle("Velocity Volume")

        _binding = FragmentHomepageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button: Button = view.findViewById(R.id.Create_Profile)
        button.setOnClickListener {
            (activity as ProfileDrawerActivity).addProfile()
        }
    }

    private fun Fragment.setActivityTitle(title: String) {
        (activity as AppCompatActivity?)?.supportActionBar?.title = title
    }
}