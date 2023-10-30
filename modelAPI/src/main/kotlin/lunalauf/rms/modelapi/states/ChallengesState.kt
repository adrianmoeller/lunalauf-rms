package lunalauf.rms.modelapi.states

import LunaLaufLanguage.Challenge

class ChallengesState(
    val challenges: List<Challenge>,
    names: List<String>? = null
) {
    val names: List<String> = names ?: challenges.map { it.name ?: "" }
    fun copy() = ChallengesState(
        challenges = challenges,
        names = names
    )
}