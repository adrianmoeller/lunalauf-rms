package lunalauf.rms.centralapp.data.model.states

import LunaLaufLanguage.Runner

class RunnersState(
    val runners: List<Runner>,
    id2runners: Map<Long, Runner>? = null
) {
    val id2runners = id2runners ?: runners.associateBy { it.id }
    fun getRunner(id: Long) = id2runners[id]
    fun copy() = RunnersState(
        runners = runners,
        id2runners = id2runners
    )
}
