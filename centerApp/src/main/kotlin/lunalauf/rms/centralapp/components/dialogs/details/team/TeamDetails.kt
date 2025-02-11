package lunalauf.rms.centralapp.components.dialogs.details.team

import lunalauf.rms.model.internal.FunfactorResult
import lunalauf.rms.model.internal.Round
import lunalauf.rms.model.internal.Runner

data class TeamDetails(
    val stats: List<Pair<String, String>>,
    val membersData: List<Pair<List<String>, Runner>>,
    val funfactorResultsData: List<Pair<List<String>, FunfactorResult>>,
    val roundsData: List<Pair<List<String>, Round>>
)