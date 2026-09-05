package com.patselby.scansafe

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class ResultFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)
        
        val verdictCircle = view.findViewById<View>(R.id.verdictCircle)
        val verdictTextView = view.findViewById<TextView>(R.id.verdictTextView)
        val reasonTextView = view.findViewById<TextView>(R.id.reasonTextView)
        val urlTextView = view.findViewById<TextView>(R.id.urlTextView)
        val scanAgainButton = view.findViewById<Button>(R.id.scanAgainButton)

        // Read arguments
        val score = arguments?.getInt(ARG_SCORE) ?: 0
        val verdict = arguments?.getString(ARG_VERDICT) ?: Verdict.GREEN.name
        val reason = arguments?.getString(ARG_REASON) ?: "Safe"
        val url = arguments?.getString(ARG_URL) ?: ""

        // Set text fields
        verdictTextView.text = when(verdict) {
            Verdict.GREEN.name -> "SAFE"
            Verdict.YELLOW.name -> "SUSPICIOUS"
            Verdict.RED.name -> "HIGH RISK"
            else -> "UNKNOWN"
        }
        reasonTextView.text = reason
        urlTextView.text = url

        // Set circle color
        val colorRes = when(verdict) {
            Verdict.GREEN.name -> R.color.green
            Verdict.YELLOW.name -> R.color.yellow
            Verdict.RED.name -> R.color.red
            else -> R.color.green
        }
        val resolvedColor = ContextCompat.getColor(requireContext(), colorRes)
        verdictCircle.backgroundTintList = ColorStateList.valueOf(resolvedColor)

        // Bind scan again button
        scanAgainButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    companion object {
        const val ARG_SCORE = "arg_score"
        const val ARG_VERDICT = "arg_verdict"
        const val ARG_REASON = "arg_reason"
        const val ARG_URL = "arg_url"

        fun newInstance(result: RiskResult, url: String): ResultFragment {
            val fragment = ResultFragment()
            val args = Bundle()
            args.putInt(ARG_SCORE, result.score)
            args.putString(ARG_VERDICT, result.verdict.name)
            args.putString(ARG_REASON, result.reason)
            args.putString(ARG_URL, url)
            fragment.arguments = args
            return fragment
        }
    }
}
