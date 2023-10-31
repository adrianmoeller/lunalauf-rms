package lunalauf.rms.modelapi

import LunaLaufLanguage.Challenge
import LunaLaufLanguage.Minigame
import LunaLaufLanguage.Runner
import LunaLaufLanguage.Team

sealed class CreateRunnerResult {
    data class Created(val runner: Runner) : CreateRunnerResult()
    data class Exists(val runner: Runner) : CreateRunnerResult()
}

sealed class CreateTeamResult {
    data class Created(val team: Team) : CreateTeamResult()
    data object BlankName : CreateTeamResult()
    data class Exists(val team: Team) : CreateTeamResult()
}

sealed class AddRunnerToTeamResult {
    data object Added : AddRunnerToTeamResult()
    data object AlreadyMember : AddRunnerToTeamResult()
}

sealed class RemoveRunnerFromTeamResult {
    data object Removed : RemoveRunnerFromTeamResult()
    data object AlreadyInNoTeam : RemoveRunnerFromTeamResult()
}

sealed class UpdateRunnerIdResult {
    data object Updated : UpdateRunnerIdResult()
    data class Exists(val runner: Runner) : UpdateRunnerIdResult()
}

sealed class DeleteElementResult {
    data object Deleted : DeleteElementResult()
    data class NotDeleted(val cause: String) : DeleteElementResult()
}

sealed class CreateMinigameResult {
    data object BlankName : CreateMinigameResult()
    data class Exists(val minigame: Minigame) : CreateMinigameResult()
    data class Created(val minigame: Minigame) : CreateMinigameResult()
}

sealed class CreateChallengeResult {
    data object BlankName : CreateChallengeResult()
    data class Created(val challenge: Challenge) : CreateChallengeResult()
}
