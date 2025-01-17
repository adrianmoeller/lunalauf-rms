package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed class Funfactor(
    name: String,
    description: String
) {
    private val _name = MutableStateFlow(name)
    val name get() = _name.asStateFlow()

    private val _description = MutableStateFlow(description)
    val description get() = _description.asStateFlow()

    private val _results = MutableStateFlow(emptyList<FunfactorResult>())
    val results get() = _results.asStateFlow()

    internal fun initSetResults(results: List<FunfactorResult>) {
        _results.update { results }
    }
}