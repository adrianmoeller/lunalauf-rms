package lunalauf.rms.centralapp.components

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

abstract class AbstractScreenModel {
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private val defaultScope = CoroutineScope(Dispatchers.Default)
        private val ioScope = CoroutineScope(Dispatchers.IO)

        @OptIn(DelicateCoroutinesApi::class)
        private val modelContext = newSingleThreadContext("model")
        private val modelScope = CoroutineScope(modelContext)

        fun freeResources() {
            modelContext.close()
        }
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