package lunalauf.rms.modelapi

import LunaLaufLanguage.Runner

sealed class CreateRunnerResult() {
    data class Created(val runner: Runner) : CreateRunnerResult()
    data class Exists(val runner: Runner) : CreateRunnerResult()
}