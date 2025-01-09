package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class Funfactor(
    name: String,
    description: String,
    results: List<FunfactorResult>
) {
    private val _name = MutableStateFlow(name)
    val name get() = _name.asStateFlow()

    private val _description = MutableStateFlow(description)
    val description get() = _description.asStateFlow()

    private val _results = MutableStateFlow(results)
    val results get() = _results.asStateFlow()
}