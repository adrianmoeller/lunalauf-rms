package lunalauf.rms.modelapi

import LunaLaufLanguage.Round
import LunaLaufLanguage.Runner
import lunalauf.rms.modelapi.ProcessLogEntry.Lvl
import lunalauf.rms.modelapi.util.Result
import java.sql.Timestamp
import java.util.concurrent.atomic.AtomicLong

object RoundCountValidator {
    fun validate(
        runner: Runner,
        currentTime: Timestamp,
        roundThreshold: Int
    ): Boolean {
        val lastCountRunner = getLastCount(getRoundLogHistory(runner)) ?: return true
        val timeDifference = currentTime.getTime() - lastCountRunner.getTime()
        return timeDifference > roundThreshold * 1000
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
