package lunalauf.rms.utilities.logging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.apache.log4j.*
import java.io.IOException

object Logger {
    private val flowAppender = FlowAppender()
    val logMessages get() = flowAppender.flow

    private val _currentLogLevel = MutableStateFlow(LogManager.getRootLogger().level.toExternal())
    val currentLogLevel get() = _currentLogLevel.asStateFlow()

    @Throws(IOException::class)
    fun configure() {
        BasicConfigurator.configure()
        BasicConfigurator.configure(
            FileAppender(
                PatternLayout("%-4r [%t] %-5p %c %x - %m%n"),
                "ll.log"
            )
        )
        BasicConfigurator.configure(flowAppender)
        setLevel(Lvl.INFO)
    }

    fun setLevel(level: Lvl) {
        LogManager.getRootLogger().level = level.toInternal()
        _currentLogLevel.value = level
    }
}