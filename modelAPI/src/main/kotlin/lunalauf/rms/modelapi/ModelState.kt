package lunalauf.rms.modelapi

import LunaLaufLanguage.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import lunalauf.rms.modelapi.states.*
import org.eclipse.emf.common.notify.Notification
import org.eclipse.emf.common.notify.Notification.*
import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.util.EContentAdapter
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ModelState")

sealed class ModelState {
    data object Unloaded : ModelState()
    data object Loading : ModelState()
    class Loaded(
        mutex: Mutex,
        val fileName: String,
        internal val model: LunaLauf
    ) : ModelState() {
        private val coroutineScope = CoroutineScope(Dispatchers.Default)
        val modelAPI = ModelAPI(mutex, this)

        private val _common = MutableStateFlow(
            CommonState(
                year = model.year,
                sponsorPoolRounds = model.sponsorPoolRounds,
                sponsorPoolAmount = model.sponsorPoolAmount,
                additionalContribution = model.additionalContribution,
                runDuration = model.runDuration
            )
        )
        val common get() = _common.asStateFlow()

        // References:

        private val _runners = MutableStateFlow(RunnersState(model.runners.filterNotNull()))
        val runners get() = _runners.asStateFlow()

        private val _teams = MutableStateFlow(TeamsState(model.teams.filterNotNull()))
        val teams get() = _teams.asStateFlow()

        private val _log = MutableStateFlow(model.log.filterNotNull())
        val log get() = _log.asStateFlow()

        private val _rounds = MutableStateFlow(model.rounds.filterNotNull())
        val rounds get() = _rounds.asStateFlow()

        private val _funfactorResults = MutableStateFlow(model.funfactorResults.filterNotNull())
        val funfactorResults get() = _funfactorResults.asStateFlow()

        private val _challenges = MutableStateFlow(ChallengesState(model.challenges.filterNotNull()))
        val challenges get() = _challenges.asStateFlow()

        private val _minigames = MutableStateFlow(MinigamesState(model.minigames.filterNotNull()))
        val minigames get() = _minigames.asStateFlow()

        private val _connections = MutableStateFlow(model.connections.filterNotNull())
        val connections get() = _connections.asStateFlow()

        // Derived:

        private val _overallRounds = MutableStateFlow(calcOverallRounds())
        val overallRounds get() = _overallRounds.asStateFlow()

        private val _runnersContribution = MutableStateFlow(calcRunnersContribution())
        val runnersContribution get() = _runnersContribution.asStateFlow()

        val currentSponsorPoolAmount = overallRounds.combine(common) { rounds, commonState ->
            calcCurrentSponsorPoolAmount(rounds, commonState.sponsorPoolAmount, commonState.sponsorPoolRounds)
        }.stateIn(coroutineScope, SharingStarted.Eagerly, 0.0)

        val overallContribution get() = runnersContribution.combine(currentSponsorPoolAmount) { runnersContr, poolAmount ->
            runnersContr + poolAmount
        }.stateIn(coroutineScope, SharingStarted.Eagerly, 0.0)

        init {
            model.eAdapters().add(object : EContentAdapter() {
                override fun notifyChanged(n: Notification) {
                    when (n.eventType) {
                        SET, ADD, ADD_MANY, REMOVE, REMOVE_MANY, MOVE -> processNotification(n)
                        else -> {}
                    }
                    super.notifyChanged(n)
                }
            })
        }

        private fun processNotification(n: Notification) {
            val feature = n.feature
            val notifier = n.notifier

            when (notifier) {
                model -> {
                    if (feature is EAttribute) {
                        when (feature.name) {
                            "year" -> _common.update { it.copy(year = n.newIntValue) }
                            "sponsorPoolRounds" -> _common.update { it.copy(sponsorPoolRounds = n.newIntValue) }
                            "sponsorPoolAmount" -> _common.update { it.copy(sponsorPoolAmount = n.newDoubleValue) }
                            "additionalContribution" -> _common.update { it.copy(additionalContribution = n.newDoubleValue) }
                            "runDuration" -> _common.update { it.copy(runDuration = n.newIntValue) }
                            else -> logger.error("Cannot find attribute: ${feature.name}")
                        }
                    } else if (feature is EReference) {
                        when (feature.name) {
                            "runners" -> updateRunners()
                            "teams" -> updateTeams()
                            "log" -> updateLog()
                            "rounds" -> {
                                updateRounds()
                                updateOverallRounds()
                                updateRunnersContribution()
                            }

                            "funfactorResults" -> {
                                updateFunfactorResults()
                                updateOverallRounds()
                                updateRunnersContribution()
                            }

                            "challenges" -> updateChallenges()
                            "minigames" -> updateMinigames()
                            "connections" -> updateConnections()
                            else -> logger.error("Cannot find reference: ${feature.name}")
                        }
                    }
                }

                is Contributor -> {
                    when (notifier) {
                        is Runner -> updateRunners(feature is EAttribute && feature.name == "id")
                        is Team -> updateTeams()
                    }
                    updateRunnersContribution()
                }

                is LogEntry -> {
                    when (notifier) {
                        is Round -> updateRounds()
                        is FunfactorResult -> updateFunfactorResults()
                    }
                    updateLog()
                    updateOverallRounds()
                    updateRunnersContribution()
                }

                is Challenge -> updateChallenges()
                is Minigame -> updateMinigames()
                else -> {}
            }
        }

        private fun updateRunners(idsChanged: Boolean = true) {
            _runners.update {
                if (idsChanged) RunnersState(model.runners.filterNotNull())
                else it.copy()
            }
        }

        private fun updateTeams() {
            _teams.value = TeamsState(model.teams.filterNotNull())
        }

        private fun updateLog() {
            _log.value = model.log.filterNotNull()
        }

        private fun updateRounds() {
            _rounds.value = model.rounds.filterNotNull()
        }

        private fun updateFunfactorResults() {
            _funfactorResults.value = model.funfactorResults.filterNotNull()
        }

        private fun updateChallenges() {
            _challenges.value = ChallengesState(model.challenges.filterNotNull())
        }

        private fun updateMinigames() {
            _minigames.value = MinigamesState(model.minigames.filterNotNull())
        }

        private fun updateConnections() {
            _connections.value = model.connections.filterNotNull()
        }

        private fun updateOverallRounds() {
            _overallRounds.value = calcOverallRounds()
        }

        private fun updateRunnersContribution() {
            _runnersContribution.value = calcRunnersContribution()
        }

        private fun calcOverallRounds(): Int {
            var rounds = 0
            for (runner in model.runners) rounds += runner.numOfRounds()
            for (team in model.teams) rounds += team.numOfFunfactorPoints()
            return rounds
        }

        private fun calcRunnersContribution(): Double {
            var contribution = 0.0
            for (team in model.teams) contribution += team.totalAmount()
            for (runner in model.runners) if (runner.team == null) contribution += runner.totalAmount()
            contribution += model.additionalContribution
            return contribution
        }

        private fun calcCurrentSponsorPoolAmount(
            overallRounds: Int,
            sponsorPoolAmount: Double,
            sponsorPoolRounds: Int
        ): Double {
            return if (overallRounds > sponsorPoolRounds) sponsorPoolAmount
            else if (sponsorPoolRounds == 0) 0.0
            else sponsorPoolAmount / sponsorPoolRounds * overallRounds
        }
    }
}