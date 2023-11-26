package lunalauf.rms.utilities.logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.spi.LoggingEvent
import java.sql.Timestamp

class FlowAppender : AppenderSkeleton() {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val _flow = MutableStateFlow(emptyList<LogMessage>())
    val flow get() = _flow.asStateFlow()

    override fun close() {
    }

    override fun requiresLayout() = false

    override fun append(event: LoggingEvent?) {
        event?.let {
            val level = it.getLevel()?.toExternal()
            val message = it.renderedMessage
            val logger = it.loggerName
                ?.removePrefix("lunalauf.rms.")
                ?.removeSuffix("\$Companion")
            val timestamp = Timestamp(it.timeStamp)


            if (message != null && level != null && logger != null) {
                scope.launch {
                    _flow.update { list ->
                        list.toMutableList().apply {
                            add(
                                LogMessage(
                                    level = level,
                                    message = message,
                                    logger = logger,
                                    timestamp = timestamp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}