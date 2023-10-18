package lunalauf.rms.centralapp.ui.components

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

abstract class AbstractScreenModel {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val defaultScope = CoroutineScope(Dispatchers.Default)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private suspend fun catchAll(block: suspend CoroutineScope.() -> Unit, scope: CoroutineScope) {
        try {
            block(scope)
        } catch (e: Throwable) {
            logger.error("Exception occured inside coroutine.", e)
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