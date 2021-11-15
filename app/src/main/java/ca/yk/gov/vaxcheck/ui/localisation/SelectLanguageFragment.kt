package ca.yk.gov.vaxcheck.ui.localisation


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.yk.gov.vaxcheck.R
import ca.yk.gov.vaxcheck.databinding.FragmentSelectLanguageBinding
import ca.yk.gov.vaxcheck.utils.LanguageConstants.LANGUAGE_CODE_EN
import ca.yk.gov.vaxcheck.utils.LanguageConstants.LANGUAGE_CODE_FR
import ca.yk.gov.vaxcheck.utils.viewBindings
import ca.yk.gov.vaxcheck.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * [SelectLanguageFragment]
 *
 * @author Jasvir Partap Singh
 */
@AndroidEntryPoint
class SelectLanguageFragment : Fragment(R.layout.fragment_select_language) {

    private val binding by viewBindings(FragmentSelectLanguageBinding::bind)

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

            }
        }

        binding.radioGroupLanguage.setOnCheckedChangeListener { radioGroup, id ->
            when (id) {
                R.id.rb_english -> {
                    sharedViewModel.setSelectLanguage(LANGUAGE_CODE_EN)
                }
                R.id.rb_french -> {
                    sharedViewModel.setSelectLanguage(LANGUAGE_CODE_FR)
                }

            }
        }


    }

}
