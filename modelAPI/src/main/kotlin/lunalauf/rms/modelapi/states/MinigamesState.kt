package lunalauf.rms.modelapi.states

import LunaLaufLanguage.Minigame

class MinigamesState(
    val minigames: List<Minigame>,
    names: List<String>? = null
) {
    val names: List<String> = names ?: minigames.map { it.name ?: "" }
    fun copy() = MinigamesState(
        minigames = minigames,
        names = names
    )
}