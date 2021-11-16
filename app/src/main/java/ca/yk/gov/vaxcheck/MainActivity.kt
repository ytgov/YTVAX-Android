package ca.yk.gov.vaxcheck

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ca.yk.gov.vaxcheck.utils.changeLocale

import ca.yk.gov.vaxcheck.viewmodel.SharedViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*





/**
 * [MainActivity]
 *
 * @author Pinakin Kansara
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var appUpdateInfo: AppUpdateInfo
    private val sharedViewModel: SharedViewModel by viewModels()


    companion object {
        const val REQUEST_CODE_FLEXIBLE_UPDATE = 1001
    }

    private val installStatusListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackBarForCompleteUpdate()
        }
    }


    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context.changeLocale("fr"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            sharedViewModel.getSelectedLanguage.collect {
                Log.i("Locale",it)
            }
        }

        setContentView(R.layout.activity_main)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(installStatusListener)
    }

    override fun onResume() {
        super.onResume()
        checkForUpdate()
    }

    override fun onDestroy() {
        appUpdateManager.unregisterListener(installStatusListener)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FLEXIBLE_UPDATE) {
            if (resultCode != RESULT_OK) {
                if (::appUpdateInfo.isInitialized) {
                    startForInAppUpdate(appUpdateInfo)
                }
            }
        }
    }

    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            appUpdateInfo = it

            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {

                startForInAppUpdate(appUpdateInfo)
            }
            if (appUpdateInfo.updateAvailability()
                == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {

                startForInAppUpdate(appUpdateInfo)
            }

            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate()
            }
        }
    }

    private fun startForInAppUpdate(updateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            updateInfo, AppUpdateType.FLEXIBLE, this, REQUEST_CODE_FLEXIBLE_UPDATE
        )
    }




    private fun popupSnackBarForCompleteUpdate() {
        Snackbar.make(
            findViewById(R.id.activity_main_layout),
            getString(R.string.in_app_update_message),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(getString(R.string.update)) { appUpdateManager.completeUpdate() }
            setActionTextColor(resources.getColor(R.color.green))
            show()
        }
    }
}
