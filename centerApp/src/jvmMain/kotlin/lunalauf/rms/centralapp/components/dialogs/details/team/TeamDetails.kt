package lunalauf.rms.centralapp.components.dialogs.details.team

data class TeamDetails(
    val stats: List<Pair<String, String>>,
    val membersData: List<List<String>>,
    val funfactorResultsData: List<List<String>>,
    val roundsData: List<List<String>>
)