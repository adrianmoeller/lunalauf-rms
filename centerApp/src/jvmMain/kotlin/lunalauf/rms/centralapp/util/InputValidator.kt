package lunalauf.rms.centralapp.util

object InputValidator {
    private val startsWithNumbers = Regex("^\\d+")
    private val unsupportedChars = Regex(".*[\\<\\>\\{\\}\\[\\]°\\^§\\$%&/\\=\\|`´\\*#\\+].*")

    fun validateName(name: String): Boolean {
        if (name.matches(startsWithNumbers))
            return false
        if (name.matches(unsupportedChars))
            return false
        return true
    }
}