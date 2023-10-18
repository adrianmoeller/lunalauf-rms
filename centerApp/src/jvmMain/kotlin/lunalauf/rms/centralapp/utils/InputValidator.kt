package lunalauf.rms.centralapp.utils

object InputValidator {
    private val onlyNumbers = Regex("\\d+")
    private val unsupportedChars = Regex(".*[\\<\\>\\{\\}\\[\\]°\\^§\\$%&/\\=\\|`´\\*#\\+].*")

    fun validateName(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.matches(onlyNumbers))
            return false
        if (trimmed.matches(unsupportedChars))
            return false
        return true
    }
}