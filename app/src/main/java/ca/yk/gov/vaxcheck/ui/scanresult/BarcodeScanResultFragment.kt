package ca.yk.gov.vaxcheck.ui.scanresult

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.transition.Scene
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ca.bc.gov.shcdecoder.model.VaccinationStatus
import ca.bc.gov.shcdecoder.model.getPatient
import ca.yk.gov.vaxcheck.R
import ca.yk.gov.vaxcheck.databinding.*
import ca.yk.gov.vaxcheck.utils.LanguageConstants.getLocale
import ca.yk.gov.vaxcheck.utils.changeLocale
import ca.yk.gov.vaxcheck.utils.viewBindings
import ca.yk.gov.vaxcheck.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * [BarcodeScanResultFragment]
 *
 * @author Pinakin Kansara
 */
@AndroidEntryPoint
class BarcodeScanResultFragment : Fragment(R.layout.fragment_barcode_scan_result) {

    private val binding by viewBindings(FragmentBarcodeScanResultBinding::bind)

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var sceneFullyVaccinated: Scene

    private lateinit var scenePartiallyVaccinated: Scene

    private lateinit var sceneNotVaccinated: Scene

    private lateinit var sceneNoRecord: Scene

    private lateinit var stringContext: Context


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stringContext = requireContext().changeLocale(getLocale())

        sceneFullyVaccinated = Scene.getSceneForLayout(
            binding.sceneRoot,
            R.layout.scene_fully_vaccinated,
            requireContext()
        )
        scenePartiallyVaccinated = Scene.getSceneForLayout(
            binding.sceneRoot,
            R.layout.scene_partially_vaccinated,
            requireContext()
        )
        sceneNoRecord =
            Scene.getSceneForLayout(binding.sceneRoot, R.layout.scene_no_record, requireContext())

        sceneNotVaccinated = Scene.getSceneForLayout(
            binding.sceneRoot,
            R.layout.scene_not_vaccinated,
            requireContext()
        )

        sharedViewModel.status.observe(viewLifecycleOwner, { status ->
            val (state, shcData) = status
            if (shcData != null) {
                val patient = shcData.getPatient()

                binding.txtFullName.text = patient.firstName?.let {
                    "$it ${patient.lastName.orEmpty()}"
                } ?: patient.lastName.orEmpty()

                binding.txtAppName.text = stringContext.getString(R.string.y_k_vaccine_card_verifier)
                when (state) {
                    VaccinationStatus.FULLY_VACCINATED -> {
                        sceneFullyVaccinated.enter()
                        setFullyVaccinatedData()
                    }

                    VaccinationStatus.PARTIALLY_VACCINATED -> {
                        scenePartiallyVaccinated.enter()
                        setPartialData()
                    }

                    else -> {
                        sceneNoRecord.enter()
                        setNoRecordData()
                    }

                }
            }

            val countDownTimer = object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    lifecycleScope.launchWhenResumed {
                        findNavController().popBackStack()
                    }
                }
            }
            countDownTimer.start()
        })
    }


    private fun setPartialData() {
        val scenePartiallyVaccinatedBinding =
            ScenePartiallyVaccinatedBinding.bind(binding.clRoot)


        scenePartiallyVaccinatedBinding.buttonScanNext.text =
            stringContext.getString(R.string.scan_next)

        scenePartiallyVaccinatedBinding.txtStatus.text =
            stringContext.getString(R.string.partially_meets_requirement)

        scenePartiallyVaccinatedBinding.txtYkOfficialResult.text =
            stringContext.getString(R.string.yukon_official_result)


        scenePartiallyVaccinatedBinding.buttonScanNext
            .setOnClickListener {
                findNavController().popBackStack()
            }

    }

    private fun setNoRecordData() {
        val sceneNoRecordBinding =
            SceneNoRecordBinding.bind(binding.clRoot)

        sceneNoRecordBinding.buttonScanNext.text =
            stringContext.getString(R.string.scan_next)

        sceneNoRecordBinding.txtStatus.text =
            stringContext.getString(R.string.does_not_meet_requirement)

        sceneNoRecordBinding.buttonScanNext
            .setOnClickListener {
                findNavController().popBackStack()
            }

    }


    private fun setFullyVaccinatedData() {
        val sceneFullyVaccinatedBinding =
            SceneFullyVaccinatedBinding.bind(binding.clRoot)

        sceneFullyVaccinatedBinding.buttonScanNext.text =
            stringContext.getString(R.string.scan_next)

        sceneFullyVaccinatedBinding.txtStatus.text =
            stringContext.getString(R.string.meets_requirement)

        sceneFullyVaccinatedBinding.txtYkOfficialResult.text =
            stringContext.getString(R.string.yukon_official_result)


        sceneFullyVaccinatedBinding.buttonScanNext
            .setOnClickListener {
                findNavController().popBackStack()
            }

    }

    private fun setNotVaccinatedData() {
        val sceneNotVaccinatedBinding =
            SceneNotVaccinatedBinding.bind(binding.clRoot)

        sceneNotVaccinatedBinding.buttonScanNext.text =
            stringContext.getString(R.string.scan_next)

        sceneNotVaccinatedBinding.txtStatus.text =
            stringContext.getString(R.string.does_not_meet_requirement)

        sceneNotVaccinatedBinding.txtYkOfficialResult.text =
            stringContext.getString(R.string.yukon_official_result)


        sceneNotVaccinatedBinding.buttonScanNext
            .setOnClickListener {
                findNavController().popBackStack()
            }

    }




}
