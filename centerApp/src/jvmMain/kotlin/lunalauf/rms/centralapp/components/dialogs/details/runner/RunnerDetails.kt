package lunalauf.rms.centralapp.components.dialogs.details.runner

data class RunnerDetails(
    val stats: List<Pair<String, String>>,
    val roundsData: List<List<String>>
)