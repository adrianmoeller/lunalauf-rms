package lunalauf.rms.utilities.network.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FixedQueue<E : Any>(
    val size: Int
) {
    private val _list = MutableStateFlow(listOf<E>())
    val queue get() = _list.asStateFlow()

    fun push(element: E) {
        _list.update {
            val copy = it.toMutableList()
            copy.add(0, element)
            while (copy.size > size)
                copy.removeAt(copy.size - 1)
            copy
        }

    }
}
