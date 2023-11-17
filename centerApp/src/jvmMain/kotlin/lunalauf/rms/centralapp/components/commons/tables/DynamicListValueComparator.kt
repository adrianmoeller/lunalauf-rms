package lunalauf.rms.centralapp.components.commons.tables

class DynamicListValueComparator<T>(
    private val index: Int,
    private val descending: Boolean = false
) : Comparator<Pair<List<String>, T>> {
    override fun compare(l1: Pair<List<String>, T>, l2: Pair<List<String>, T>): Int {
        val result = compareString(l1.first[index], l2.first[index])
        return if (descending) -result else result
    }

    private fun compareString(o1: String, o2: String): Int {
        val n1 = o1.toDoubleOrNull()
        val n2 = o2.toDoubleOrNull()

        return if (n1 != null && n2 != null)
            n1.compareTo(n2)
        else
            o1.compareTo(o2)
    }
}