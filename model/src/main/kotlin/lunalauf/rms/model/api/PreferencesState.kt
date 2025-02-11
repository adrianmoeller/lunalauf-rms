package lunalauf.rms.model.api

data class PreferencesState(
    val autoSaveActive: Boolean = false,
    val autoSaveInterval: Float = 30f,
    val roundThreshold: Float,
    val saveConnectionsActive: Boolean = false
)
