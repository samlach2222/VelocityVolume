package com.samlach2222.velocityvolume.ui.homePage

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.color.MaterialColors
import com.samlach2222.velocityvolume.ProfileDrawerActivity
import com.samlach2222.velocityvolume.R
import com.samlach2222.velocityvolume.databinding.FragmentHomepageBinding

/**
 * Class who manage the first Fragment, shown when the is no profiles in the application
 */
class HomePageFragment : Fragment() {

    private var _binding: FragmentHomepageBinding? = null
    private val binding get() = _binding!!

    /**
     * function called when the view is created
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setActivityTitle("Velocity Volume")

        _binding = FragmentHomepageBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * function called when the view is destroyed
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * function called when the view is totally created
     * Initialize the text color and content of the fragment
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button: Button = view.findViewById(R.id.Create_Profile)
        button.setOnClickListener {
            (activity as ProfileDrawerActivity).addProfile()
        }

        // colorized text
       val color = MaterialColors.getColor(this.context, com.google.android.material.R.attr.colorPrimary, null)

        val txtPart1 = getString(R.string.home_message_part_1)
        val txtPart2 = getString(R.string.home_message_part_2)
        val txtPart3 = getString(R.string.home_message_part_3)
        val txtPart4 = getString(R.string.home_message_part_4)
        val spannable2 = SpannableString(txtPart2) // String for which you want to change the color
        val spannable4 = SpannableString(txtPart4) // String for which you want to change the color
        spannable2.setSpan(
            ForegroundColorSpan(color), 0, txtPart2.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable4.setSpan(
            ForegroundColorSpan(color), 0, txtPart4.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        view.findViewById<TextView>(R.id.welcome_message).text = TextUtils.concat(txtPart1," " , spannable2, txtPart3, " ", spannable4)
    }

    /**
     * function who allow to change the activity title with [title]
     * @param[title] the new title of the Fragment
     */
    private fun Fragment.setActivityTitle(title: String) {
        (activity as AppCompatActivity?)?.supportActionBar?.title = title
    }
}