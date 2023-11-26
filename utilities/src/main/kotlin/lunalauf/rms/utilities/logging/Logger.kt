package lunalauf.rms.utilities.logging

import org.apache.log4j.*
import java.io.IOException

object Logger {
    private val flowAppender = FlowAppender()
    val logMessages get() = flowAppender.flow

    @Throws(IOException::class)
    fun configure() {
        BasicConfigurator.configure()
        BasicConfigurator.configure(FileAppender(
            PatternLayout("%-4r [%t] %-5p %c %x - %m%n"),
            "ll.log"
        ))
        BasicConfigurator.configure(flowAppender)
    }

    fun setLevel(level: Lvl) {
        LogManager.getRootLogger().level = level.toInternal()
    }
}