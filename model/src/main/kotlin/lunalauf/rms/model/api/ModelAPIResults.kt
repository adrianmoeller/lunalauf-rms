package lunalauf.rms.model.api

import lunalauf.rms.model.internal.*

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

sealed class UpdateMinigameIdResult {
    data class Exists(val minigame: Minigame) : UpdateMinigameIdResult()
    data object Updated : UpdateMinigameIdResult()
}

sealed class UpdateFunfactorNameResult {
    data object BlankName : UpdateFunfactorNameResult()
    data object Updated : UpdateFunfactorNameResult()
}

sealed class CreateChallengeResult {
    data object BlankName : CreateChallengeResult()
    data object NegativeDuration : CreateChallengeResult()
    data class Created(val challenge: Challenge) : CreateChallengeResult()
}

sealed class UpdateChallengeDurationResult {
    data object NegativeDuration : UpdateChallengeDurationResult()
    data object Updated : UpdateChallengeDurationResult()
}

sealed class StartChallengeResult {
    data object AlreadyStarted : StartChallengeResult()
    data object SendMessageFailed : StartChallengeResult()
    data object Started : StartChallengeResult()
}

sealed class ResetChallengeStateResult {
    data object NotCompleted : ResetChallengeStateResult()
    data object Reset : ResetChallengeStateResult()
}

sealed class LogRoundResult {
    data object RunDisabled : LogRoundResult()
    data object LastRoundAlreadyLogged : LogRoundResult()
    data object ValidationFailed : LogRoundResult()
    data class Logged(val round: Round) : LogRoundResult()
}

sealed class LogMinigameResultResult {
    data object NoMinigameWithId : LogMinigameResultResult()
    data class Logged(val minigameResult: FunfactorResult) : LogMinigameResultResult()
}
