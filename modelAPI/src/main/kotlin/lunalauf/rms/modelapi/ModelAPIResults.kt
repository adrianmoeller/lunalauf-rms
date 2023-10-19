package lunalauf.rms.modelapi

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
