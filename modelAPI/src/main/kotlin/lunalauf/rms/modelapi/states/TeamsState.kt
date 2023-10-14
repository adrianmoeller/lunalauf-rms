package lunalauf.rms.modelapi.states

import LunaLaufLanguage.Team

class TeamsState(
    val teams: List<Team>
) {
    fun copy() = TeamsState(
        teams = teams
    )
}