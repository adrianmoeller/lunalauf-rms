package lunalauf.rms.modelapi

import LunaLaufLanguage.Round
import LunaLaufLanguage.Runner
import lunalauf.rms.modelapi.ProcessLogEntry.Lvl
import lunalauf.rms.modelapi.util.Result
import java.sql.Timestamp
import java.util.concurrent.atomic.AtomicLong

object RoundCountValidator {
    private const val DEFAULT_ROUND_THRESHOLD: Long = 40 // sec
    private val roundThreshold = AtomicLong(DEFAULT_ROUND_THRESHOLD)
    fun getRoundThreshold(): Long {
        return roundThreshold.get()
    }

    fun setRoundThreshold(roundThreshold: Long) {
        RoundCountValidator.roundThreshold.set(roundThreshold)
        Result<Void>("Round Count Validator").passed(null, 0, "Set threshold to $roundThreshold sec", Lvl.INFO).log()
    }

    fun validate(runner: Runner, currentTime: Timestamp): Boolean {
        val lastCountRunner = getLastCount(getRoundLogHistory(runner)) ?: return true
        val timeDifference = currentTime.getTime() - lastCountRunner.getTime()
        return timeDifference > roundThreshold.get() * 1000
    }

    private fun getRoundLogHistory(runner: Runner): List<Round> {
        val team = runner.team ?: return runner.rounds
        return team.rounds
    }

    private fun getLastCount(roundLog: List<Round>): Timestamp? {
        for (i in roundLog.indices.reversed()) {
            if (!roundLog[i].isManualLogged) return roundLog[i].timestamp
        }
        return null
    }
}
