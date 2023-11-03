package lunalauf.rms.centralapp.components.main

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import lunalauf.rms.centralapp.components.AbstractStatelessScreenModel
import lunalauf.rms.centralapp.components.commons.showSnackbar
import lunalauf.rms.utilities.persistence.PersistenceManager
import lunalauf.rms.utilities.publicviewprefs.PublicViewPrefContainer
import lunalauf.rms.utilities.publicviewprefs.PublicViewPrefState
import lunalauf.rms.utilities.publicviewprefs.toState

class PublicViewScreenModel(
    snackBarHostState: SnackbarHostState
) : AbstractStatelessScreenModel() {
    val persistenceManager = PersistenceManager()

    var prefState by mutableStateOf(PublicViewPrefState())
        private set
    var open by mutableStateOf(false)
        private set

    init {
        launchInIOScope {
            try {
                prefState = persistenceManager.load(PublicViewPrefContainer::class.java).toState()
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
        }
    }

    fun updateOpen(open: Boolean) {
        this.open = open
    }

    fun updatePrefState(
        makeCopy: PublicViewPrefState.() -> PublicViewPrefState
    ) {
        prefState = makeCopy(prefState)
    }
}