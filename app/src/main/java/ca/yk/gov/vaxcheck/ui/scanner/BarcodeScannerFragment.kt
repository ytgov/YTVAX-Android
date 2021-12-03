package ca.yk.gov.vaxcheck.ui.scanner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Size
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import ca.bc.gov.shcdecoder.model.ImmunizationStatus
import ca.yk.gov.vaxcheck.R
import ca.yk.gov.vaxcheck.barcodeanalyzer.BarcodeAnalyzer
import ca.yk.gov.vaxcheck.barcodeanalyzer.ScanningResultListener
import ca.yk.gov.vaxcheck.databinding.FragmentBarcodeScannerBinding
import ca.yk.gov.vaxcheck.utils.LanguageConstants.LANGUAGE_CODE_EN
import ca.yk.gov.vaxcheck.utils.LanguageConstants.LANGUAGE_CODE_FR
import ca.yk.gov.vaxcheck.utils.LanguageConstants.getLocale
import ca.yk.gov.vaxcheck.viewmodel.BarcodeScanResultViewModel
import ca.yk.gov.vaxcheck.viewmodel.SharedViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import ca.yk.gov.vaxcheck.BuildConfig
import ca.yk.gov.vaxcheck.utils.*
import ca.yk.gov.vaxcheck.utils.LanguageConstants.setLocale
import java.io.File
import java.io.FileOutputStream
import java.io.IOException





/**
 * [BarcodeScannerFragment]
 *
 * @author Pinakin Kansara
 */
@AndroidEntryPoint
class BarcodeScannerFragment : Fragment(R.layout.fragment_barcode_scanner), ScanningResultListener {

    private val binding by viewBindings(FragmentBarcodeScannerBinding::bind)

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var requestPermission: ActivityResultLauncher<String>

    private lateinit var cameraProvider: ProcessCameraProvider

    private lateinit var imageAnalysis: ImageAnalysis

    private lateinit var camera: Camera

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val viewModel: BarcodeScanResultViewModel by viewModels()

    private lateinit var stringContext: Context


    override fun onAttach(context: Context) {
        super.onAttach(context)
        requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->

            if (isGranted) {
                setUpCamera()
            } else {
                showRationalDialog()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stringContext = requireContext().changeLocale(getLocale())

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    collectSelectedLanguageFlow()
                }
                launch {
                    collectImmunizationStatus()
                }
            }
        }

        setStrings()
        binding.swLocale.isChecked = getLocale() == LANGUAGE_CODE_FR
        binding.swLocale.setOnCheckedChangeListener { _, isChecked ->

            if (!isChecked) {
                setLocale(LANGUAGE_CODE_EN)
                sharedViewModel.setSelectLanguage(LANGUAGE_CODE_EN)
            } else {
                setLocale(LANGUAGE_CODE_FR)
                sharedViewModel.setSelectLanguage(LANGUAGE_CODE_FR)
            }

            stringContext = requireContext().changeLocale(getLocale())
            setStrings()
        }

    }

    private fun showPrivacyPolicy(url: String) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            context?.toast(stringContext.getString(R.string.no_app_found))
        }
    }

    private fun setStrings() {

        binding.txtCovidInfo.text = stringContext.getString(R.string.btn_covid_info)
        binding.txtPrivacyPolicy.text = stringContext.getString(R.string.btn_data_collection_notice)

        binding.txtCovidInfo.setSpannableLink {
            showCovidInfo(stringContext.getString(R.string.url_covid_info))
        }

        binding.txtPrivacyPolicy.setSpannableLink {
            showPrivacyHtml()
        }
    }


    private fun showCovidInfo(url: String) {
        val colorInt = ContextCompat.getColor(requireContext(), R.color.dark_blue)
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(colorInt)
            .build()

        val builder = CustomTabsIntent.Builder()
        builder.setDefaultColorSchemeParams(defaultColors)
        AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_arrow_back)?.let {
            builder.setCloseButtonIcon(it.toBitmap())
        }

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
    }


    private fun showPrivacyHtml() {
//        val chromeId = "com.android.chrome"
//        val colorInt = ContextCompat.getColor(requireContext(), R.color.dark_blue)
//        val defaultColors = CustomTabColorSchemeParams.Builder()
//            .setToolbarColor(colorInt)
//            .build()
//
//        val builder = CustomTabsIntent.Builder()
//
//        builder.setDefaultColorSchemeParams(defaultColors)
//        AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_arrow_back)?.let {
//            builder.setCloseButtonIcon(it.toBitmap())
//        }
//
//        builder.setShowTitle(true)
//        val customTabsIntent = builder.build()
//        val uri = Uri.parse("content://"+BuildConfig.APPLICATION_ID +"/index.html")
//
//        customTabsIntent.intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        customTabsIntent.intent.type = "text/plain"
//        customTabsIntent.intent.data = uri
//
//        val packageManager = requireContext().packageManager
//        val resolveInfoList = packageManager.queryIntentActivities(customTabsIntent.intent, PackageManager.MATCH_DEFAULT_ONLY)
//        for (resolveInfo in resolveInfoList) {
//            val packageName = resolveInfo.activityInfo.packageName
//            if (TextUtils.equals(packageName, chromeId))
//                customTabsIntent.intent.setPackage(chromeId)
//
//        }
//
//        customTabsIntent.launchUrl(requireContext(), customTabsIntent.intent.data!!)


        findNavController().navigate(BarcodeScannerFragmentDirections
            .actionBarcodeScannerFragmentToWebFragment())

    }




    private suspend fun collectOnBoardingFlow() {
        sharedViewModel.isOnBoardingShown.collect { shown ->
            when (shown) {
                true -> {
                    cameraExecutor = Executors.newSingleThreadExecutor()

                    checkCameraPermission()

                    binding.overlay.post {
                        binding.overlay.setViewFinder()
                    }
                }

                false -> {
                    val startDestination = findNavController().graph.startDestination
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()
                    findNavController().navigate(R.id.onBoardingFragment, null, navOptions)
                }
            }
        }
    }

    private suspend fun collectSelectedLanguageFlow() {
        sharedViewModel.getSelectedLanguage.collect { code ->
            when (code) {
                LANGUAGE_CODE_EN,
                LANGUAGE_CODE_FR -> {
                    collectOnBoardingFlow()
                }
                else -> {
                    val startDestination = findNavController().graph.startDestination
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()
                    findNavController().navigate(R.id.selectLanguageFragment, null, navOptions)
                }

            }
        }
    }

    private suspend fun collectImmunizationStatus() {
        viewModel.status.collect { status ->
            when (status.status) {
                ImmunizationStatus.FULLY_IMMUNIZED,
                ImmunizationStatus.PARTIALLY_IMMUNIZED -> {
                    sharedViewModel.setStatus(status)
                    findNavController().navigate(
                        R.id.action_barcodeScannerFragment_to_barcodeScanResultFragment
                    )
                }

                ImmunizationStatus.INVALID_QR_CODE -> {
                    onFailure()
                }
            }
        }
    }

    override fun onDestroyView() {

        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }

        super.onDestroyView()
    }

    /**
     * Check if permission for required feature is Granted or not.
     */
    private fun checkCameraPermission() {

        when {

            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                setUpCamera()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showRationalDialog()
            }

            else -> {
                requestPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showRationalDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(stringContext.getString(R.string.yk_permission_required_title))
            .setCancelable(false)
            .setMessage(stringContext.getString(R.string.yk_permission_message))
            .setNegativeButton(stringContext.getString(R.string.exit)) { dialog, which ->
                if (!findNavController().popBackStack() || !findNavController().navigateUp()) {
                    requireActivity().finish()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun setUpCamera() {

        val cameraProviderFeature = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFeature.addListener({

            cameraProvider = cameraProviderFeature.get()

            bindBarcodeScannerUseCase()

            enableFlashControl()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindBarcodeScannerUseCase() {

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val hasCamera = cameraProvider.hasCamera(cameraSelector)

        if (hasCamera) {

            val resolution = Size(
                binding.scannerPreview.width,
                binding.scannerPreview.height
            )
            val preview = Preview.Builder()
                .apply {
                    setTargetResolution(resolution)
                }.build()

            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(resolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor, BarcodeAnalyzer(this))

            cameraProvider.unbindAll()

            camera = cameraProvider.bindToLifecycle(
                viewLifecycleOwner, cameraSelector, preview, imageAnalysis
            )

            preview.setSurfaceProvider(binding.scannerPreview.surfaceProvider)
        } else {
            showNoCameraAlertDialog()
        }
    }

    private fun showNoCameraAlertDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(stringContext.getString(R.string.yk_no_rear_camera_title))
            .setCancelable(false)
            .setMessage(stringContext.getString(R.string.yk_nor_rear_camera_message))
            .setNegativeButton(stringContext.getString(R.string.exit)) { dialog, which ->
                if (!findNavController().popBackStack() || !findNavController().navigateUp()) {
                    requireActivity().finish()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun enableFlashControl() {
        if (camera.cameraInfo.hasFlashUnit()) {
            binding.checkboxFlashLight.visibility = View.VISIBLE

            binding.checkboxFlashLight.setOnCheckedChangeListener { buttonView, isChecked ->

                if (buttonView.isPressed) {
                    camera.cameraControl.enableTorch(isChecked)
                }
            }

            camera.cameraInfo.torchState.observe(viewLifecycleOwner) {
                it?.let { torchState ->
                    binding.checkboxFlashLight.isChecked = torchState == TorchState.ON
                }
            }
        }
    }

    override fun onScanned(shcUri: String) {

        // Since camera is constantly analysing
        // Its good to clear analyzer to avoid duplicate dialogs
        // When barcode is not supported
        imageAnalysis.clearAnalyzer()

        viewModel.processShcUri(shcUri)
    }

    override fun onFailure() {

        // Since camera is constantly analysing
        // Its good to clear analyzer to avoid duplicate dialogs
        // When barcode is not supported
        imageAnalysis.clearAnalyzer()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(stringContext.getString(R.string.yk_invalid_barcode_title))
            .setCancelable(false)
            .setMessage(stringContext.getString(R.string.yk_invalid_barcode_message))
            .setPositiveButton(stringContext.getString(R.string.text_ok)) { dialog, which ->

                // Attach analyzer again to start analysis.
                imageAnalysis.setAnalyzer(cameraExecutor, BarcodeAnalyzer(this))

                dialog.dismiss()
            }
            .show()
    }

    companion object {
        const val ON_BOARDING_SHOWN = "ON_BOARDING_SHOWN"
    }
}
