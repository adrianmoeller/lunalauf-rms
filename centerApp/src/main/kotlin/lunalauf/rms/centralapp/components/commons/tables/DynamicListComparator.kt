package lunalauf.rms.centralapp.components.commons.tables

class DynamicListComparator(
    private val index: Int,
    private val descending: Boolean = false
) : Comparator<List<String>> {
    override fun compare(l1: List<String>, l2: List<String>): Int {
        val result = compareString(l1[index], l2[index])
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