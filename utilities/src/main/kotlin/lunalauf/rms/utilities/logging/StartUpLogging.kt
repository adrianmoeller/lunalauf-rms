package lunalauf.rms.utilities.logging

import org.slf4j.LoggerFactory
import java.awt.*

fun configureStartUpErrorLogging() {
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        LoggerFactory.getLogger("Main").error("Fatal error", e)

        Dialog(Frame(), e.message ?: "Error").apply {
            layout = FlowLayout()
            val exceptionLabel = Label(e.toString())
            add(exceptionLabel)
            val seeLogLabel = Label("See log.ll")
            add(seeLogLabel)
            val button = Button("OK").apply {
                addActionListener { dispose() }
            }
            add(button)
            setSize(300, 200)
            isVisible = true
        }
    }
}