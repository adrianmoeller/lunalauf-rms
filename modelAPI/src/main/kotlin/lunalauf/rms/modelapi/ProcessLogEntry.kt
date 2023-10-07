package lunalauf.rms.modelapi

import java.io.Serializable
import java.sql.Timestamp
import java.time.LocalDateTime

class ProcessLogEntry(
    val message: String,
    val exception: Exception?,
    val level: Lvl
) : Serializable {
    enum class Lvl {
        INFO,
        WARN,
        ERROR
    }

    val timestamp: Timestamp = Timestamp.valueOf(LocalDateTime.now())
    val subLogEntries: MutableList<ProcessLogEntry?> = ArrayList()

    override fun toString(): String {
        val result = StringBuffer(timestamp.toString())
        result.append(" | [")
        result.append(level.toString())
        result.append("] ")
        result.append(message)
        result.append("\n")
        for (entry in subLogEntries) {
            result.append("> ")
            result.append(entry.toString())
        }
        return result.toString()
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}