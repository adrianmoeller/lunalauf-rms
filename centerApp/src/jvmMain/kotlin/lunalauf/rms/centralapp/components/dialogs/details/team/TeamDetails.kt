package lunalauf.rms.centralapp.components.dialogs.details.team

import LunaLaufLanguage.FunfactorResult
import LunaLaufLanguage.Round
import LunaLaufLanguage.Runner

data class TeamDetails(
    val stats: List<Pair<String, String>>,
    val membersData: List<Pair<List<String>, Runner>>,
    val funfactorResultsData: List<Pair<List<String>, FunfactorResult>>,
    val roundsData: List<Pair<List<String>, Round>>
)