package lunalauf.rms.centralapp.components.dialogs.details.runner

import lunalauf.rms.model.internal.Round

data class RunnerDetails(
    val stats: List<Pair<String, String>>,
    val roundsData: List<Pair<List<String>, Round>>
)