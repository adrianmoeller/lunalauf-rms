package lunalauf.rms.modelapi.states

import LunaLaufLanguage.Minigame

class MinigamesState(
    val minigames: List<Minigame>,
    ids: List<Int>? = null
) {
    val ids: List<Int> = ids ?: minigames.map { it.minigameID }
    fun copy() = MinigamesState(
        minigames = minigames,
        ids = ids
    )
}