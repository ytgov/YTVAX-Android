package ca.yk.gov.vaxcheck.ui.webview

import android.content.Context
import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ca.yk.gov.vaxcheck.R
import ca.yk.gov.vaxcheck.databinding.FragmentSelectLanguageBinding
import ca.yk.gov.vaxcheck.databinding.FragmentWebViewBinding
import ca.yk.gov.vaxcheck.utils.LanguageConstants
import ca.yk.gov.vaxcheck.utils.changeLocale
import ca.yk.gov.vaxcheck.utils.viewBindings
import ca.yk.gov.vaxcheck.viewmodel.SharedViewModel

class WebFragment : Fragment(R.layout.fragment_web_view)  {
    private val binding by viewBindings(FragmentWebViewBinding::bind)
    private lateinit var stringContext: Context

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebView()
        setToolBar()
    }

    private fun initWebView(){
        binding.webView.webViewClient = WebViewClient()
        binding.webView.settings.useWideViewPort = false
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.builtInZoomControls = true
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.loadUrl("file:///android_asset/page.html")
    }

    private fun setToolBar(){
        stringContext = requireContext().changeLocale(LanguageConstants.getLocale())
        binding.toolbarWeb.apply {
            title = stringContext.getString(R.string.btn_data_collection_notice)
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
        }

    }


}