package lunalauf.rms.centralapp.components.dialogs.details.runner

import LunaLaufLanguage.Round

data class RunnerDetails(
    val stats: List<Pair<String, String>>,
    val roundsData: List<Pair<List<String>, Round>>
)