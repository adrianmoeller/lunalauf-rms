package lunalauf.rms.centralapp.components

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lunalauf.rms.modelapi.ModelAPI
import lunalauf.rms.modelapi.ModelState
import org.slf4j.LoggerFactory

abstract class AbstractStatelessScreenModel {
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private val defaultScope = CoroutineScope(Dispatchers.Default)
        private val ioScope = CoroutineScope(Dispatchers.IO)
        private val modelScope = CoroutineScope(ModelAPI.modelContext)
    }

    private suspend fun catchAll(block: suspend CoroutineScope.() -> Unit, scope: CoroutineScope) {
        try {
            block(scope)
        } catch (e: Throwable) {
            logger.error("Exception occurred inside coroutine.", e)
        }
    }

    protected fun launchInModelScope(block: suspend CoroutineScope.() -> Unit) {
        modelScope.launch {
            catchAll(block, this)
        }
    }

    protected fun launchInDefaultScope(block: suspend CoroutineScope.() -> Unit) {
        defaultScope.launch {
            catchAll(block, this)
        }
    }

    protected fun launchInIOScope(block: suspend CoroutineScope.() -> Unit) {
        ioScope.launch {
            catchAll(block, this)
        }
    }
}

abstract class AbstractScreenModel(
    modelState: ModelState.Loaded
) : AbstractStatelessScreenModel() {
    protected val modelAPI = modelState.modelAPI
}
