package lunalauf.rms.centralapp.data.preferences

data class PreferencesState(
    val autoSaveActive: Boolean = false,
    val autoSaveInterval: Float = 30f,
    val roundThreshold: Float = 40f,
    val saveConnectionsActive: Boolean = true
)
