package lunalauf.rms.modelapi.states

import LunaLaufLanguage.Team

class TeamsState(
    val teams: List<Team>,
    names: List<String>? = null
) {
    val names: List<String> = names ?: teams.map { it.name ?: "" }
    fun copy() = TeamsState(
        teams = teams,
        names = names
    )
}