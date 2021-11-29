package ca.yk.gov.vaxcheck.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.bc.gov.shcdecoder.model.ImmunizationRecord
import ca.yk.gov.vaxcheck.data.local.DataStoreRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *[SharedViewModel]
 *
 * @author Amit Metri
 */
@HiltViewModel
class SharedViewModel @Inject constructor(
    private val dataStoreRepo: DataStoreRepo
) : ViewModel() {

    val isOnBoardingShown = dataStoreRepo.isOnBoardingShown.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    val getSelectedLanguage = dataStoreRepo.getSelectedLanguage

    private val _status: MutableLiveData<ImmunizationRecord> = MutableLiveData()
    val status: LiveData<ImmunizationRecord>
        get() = _status

    fun setStatus(status: ImmunizationRecord) {
        _status.value = status
    }

    fun setOnBoardingShown(shown: Boolean) = viewModelScope.launch {
        dataStoreRepo.setOnBoardingShown(shown)
    }

    fun setSelectLanguage(languageCode: String) = viewModelScope.launch {
        dataStoreRepo.setSelectedLanguage(languageCode)
    }

}
