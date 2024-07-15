package lunalauf.rms.centralapp.components.main

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import lunalauf.rms.centralapp.components.AbstractStatelessScreenModel
import lunalauf.rms.centralapp.components.commons.showSnackbar
import lunalauf.rms.utilities.persistence.PersistenceManager
import lunalauf.rms.utilities.publicviewprefs.PublicViewPrefContainer
import lunalauf.rms.utilities.publicviewprefs.PublicViewPrefState
import lunalauf.rms.utilities.publicviewprefs.toState

class PublicViewScreenModel(
    private val snackBarHostState: SnackbarHostState
) : AbstractStatelessScreenModel() {
    private val persistenceManager = PersistenceManager()

    var prefState by mutableStateOf(loadPrefState())
        private set
    var open by mutableStateOf(false)
        private set
    var presentMode by mutableStateOf(false)
        private set

    private fun loadPrefState(): PublicViewPrefState {
        try {
            return persistenceManager.load(PublicViewPrefContainer::class.java).toState()
        } catch (e: PersistenceManager.PersistenceException) {
            launchInDefaultScope {
                snackBarHostState.showSnackbar(
                    message = e.message,
                    withDismissAction = true,
                    duration = SnackbarDuration.Long,
                    isError = true
                )
            }
        }
        return PublicViewPrefState()
    }

    fun updateOpen(open: Boolean) {
        this.open = open
    }

    fun updatePrefState(
        makeCopy: PublicViewPrefState.() -> PublicViewPrefState
    ) {
        prefState = makeCopy(prefState)
    }

    fun resetPrefState() {
        prefState = PublicViewPrefState()
    }

    fun persistPrefState() {
        launchInIOScope {
            persistenceManager.save(prefState.toPersistenceContainer())
        }
    }

    fun switchPresentMode(windowState: WindowState) {
        presentMode = !presentMode
        if (presentMode)
            windowState.placement = WindowPlacement.Maximized
        else
            windowState.placement = WindowPlacement.Floating
    }
}