package lunalauf.rms.modelapi.util

import lunalauf.rms.modelapi.ProcessLogEntry
import lunalauf.rms.modelapi.ProcessLogEntry.Lvl
import lunalauf.rms.modelapi.ProcessLogger

/**
 * **Code convention:**
 *
 *  * -1: Error that caused the program to terminate the task
 *  * 0: Completed successfully but no result
 *  * 1: Expected behavior
 *  * 2: Task had no effect but result is available
 *  * 3: Task completed with complications
 *  * >3: Task completed passing an individual result code
 *
 */
class Result<R>(taskName: String) {
    var result: R? = null
        private set
    private var code = -1
    private var codeModifiedInAdvance = false
    private val taskName: String
    private var logEntry: ProcessLogEntry? = null
    private val subLogEntries: MutableList<ProcessLogEntry?> = ArrayList()
    private var logEntriesMerged = false

    init {
        this.taskName = "'$taskName' - "
    }

    fun passed(result: R?, code: Int, message: String, level: Lvl): Result<R> {
        logEntry = ProcessLogEntry(taskName + message, null, level)
        this.result = result
        if (!codeModifiedInAdvance) this.code = code
        return this
    }

    fun failed(message: String, optionalExc: Exception?): Result<R> {
        logEntry = ProcessLogEntry(taskName + message, optionalExc, Lvl.ERROR)
        code = -1
        return this
    }

    fun subEntry(message: String, optionalExc: Exception?, level: Lvl): Result<R> {
        subLogEntries.add(ProcessLogEntry(taskName + message, optionalExc, level))
        logEntriesMerged = false
        return this
    }

    fun <S> makeSub(subResult: Result<S>): Result<S> {
        if (subResult.logEntry != null) {
            if (!subResult.logEntriesMerged) {
                subResult.logEntry!!.subLogEntries.clear()
                subResult.logEntry!!.subLogEntries.addAll(subResult.subLogEntries)
                subResult.logEntriesMerged = true
            }
            subLogEntries.add(subResult.logEntry)
        }
        return subResult
    }

    fun setCode(code: Int) {
        this.code = code
        codeModifiedInAdvance = true
    }

    fun hasResult(): Boolean {
        return code > 0 && result != null
    }

    val isFailed: Boolean
        get() = code == -1

    fun getCode(): Int {
        return code
    }

    fun getLogEntry(): ProcessLogEntry? {
        if (logEntry != null) {
            if (!logEntriesMerged) {
                logEntry!!.subLogEntries.clear()
                logEntry!!.subLogEntries.addAll(subLogEntries)
                logEntriesMerged = true
            }
        }
        return logEntry
    }

    fun log(): Result<R> {
        if (logEntry != null)
            ProcessLogger.log(getLogEntry()!!)
        return this
    }
}
