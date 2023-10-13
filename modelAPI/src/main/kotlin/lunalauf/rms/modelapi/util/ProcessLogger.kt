package lunalauf.rms.modelapi.util

import java.io.Serializable
import java.util.function.Consumer

class ProcessLogger private constructor() : Serializable {
    private val log: MutableList<ProcessLogEntry> = ArrayList()
    private val logEntryAddedListener: MutableList<Consumer<ProcessLogEntry>>

    init {
        logEntryAddedListener = ArrayList()
    }

    fun getLog(): List<ProcessLogEntry> {
        return log
    }

    fun addLogEntryAddedListener(action: Consumer<ProcessLogEntry>) {
        logEntryAddedListener.add(action)
    }

    override fun toString(): String {
        val result = StringBuffer()
        for (entry in log) {
            result.append(entry.toString())
            result.append("\n")
        }
        return result.toString()
    }

    companion object {
        private const val serialVersionUID = 1L
        private var localLogger: ProcessLogger? = null

        fun initLogger(): ProcessLogger {
            localLogger = ProcessLogger()
            return localLogger!!
        }

        @Synchronized
        fun log(logEntry: ProcessLogEntry) {
            if (localLogger == null) return
            localLogger!!.log.add(logEntry)
            localLogger!!.logEntryAddedListener.forEach(Consumer { action: Consumer<ProcessLogEntry> ->
                action.accept(
                    logEntry
                )
            })
        }
    }
}
